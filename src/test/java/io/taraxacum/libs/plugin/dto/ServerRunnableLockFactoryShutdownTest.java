package io.taraxacum.libs.plugin.dto;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/** Uses the real lock runner with a recording scheduler; never boots or edits a server. */
@Isolated("Restores the production global shutdown flag after every case")
class ServerRunnableLockFactoryShutdownTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void normalTasksKeepDelayResultsAndReleaseTheirOwnKeys(boolean callable) throws Exception {
        try (Fixture fixture = new Fixture()) {
            AtomicInteger calls = new AtomicInteger();
            FutureTask<?> future = fixture.submit(callable, calls::incrementAndGet);
            fixture.startCallback().get(5, TimeUnit.SECONDS);
            assertEquals(1, calls.get());
            assertFalse(future.isCancelled());
            assertEquals(callable ? 1 : null, future.get(1, TimeUnit.SECONDS));
            assertNull(fixture.map.getTask(fixture.key));
            assertNull(fixture.map.getFactory(fixture.key));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stopBeforeQueuedCallbackCancelsWithoutExecutingOrSpinning(boolean callable) throws Exception {
        try (Fixture fixture = new Fixture()) {
            AtomicInteger calls = new AtomicInteger();
            FutureTask<?> future = fixture.submit(callable, calls::incrementAndGet);
            ServerRunnableLockFactory.stop();
            fixture.startCallback().get(5, TimeUnit.SECONDS);
            assertCancelledWithoutPayload(future, calls);
            assertEquals(0, fixture.factory.taskSize());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stopDuringPredecessorCompletionDoesNotSpinOrReplaceItsIdentity(boolean callable) throws Exception {
        try (Fixture fixture = new Fixture()) {
            AtomicInteger calls = new AtomicInteger();
            FutureTask<Void> predecessor = new FutureTask<>(() -> null) {
                @Override
                public Void get() throws InterruptedException, ExecutionException {
                    run();
                    ServerRunnableLockFactory.stop();
                    return super.get();
                }
            };
            fixture.installPredecessor(predecessor);
            FutureTask<?> future = fixture.submit(callable, calls::incrementAndGet);
            fixture.startCallback().get(5, TimeUnit.SECONDS);
            assertCancelledWithoutPayload(future, calls);
            assertTrue(predecessor.isDone());
            assertSame(predecessor, fixture.map.getTask(fixture.key));
            assertSame(fixture.factory, fixture.map.getFactory(fixture.key));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stoppedUnclaimedCallbackNeverCancelsOrRemovesAnotherTask(boolean callable) throws Exception {
        try (Fixture fixture = new Fixture()) {
            AtomicInteger calls = new AtomicInteger();
            FutureTask<Void> predecessor = new FutureTask<>(() -> null);
            fixture.installPredecessor(predecessor);
            FutureTask<?> future = fixture.submit(callable, calls::incrementAndGet);
            ServerRunnableLockFactory.stop();
            fixture.startCallback().get(5, TimeUnit.SECONDS);
            assertCancelledWithoutPayload(future, calls);
            assertFalse(predecessor.isDone());
            assertFalse(predecessor.isCancelled());
            assertSame(predecessor, fixture.map.getTask(fixture.key));
            assertSame(fixture.factory, fixture.map.getFactory(fixture.key));
            assertEquals(1, fixture.factory.taskSize());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void stopDoesNotInterruptWorkThatAlreadyStarted(boolean callable) throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (Fixture fixture = new Fixture()) {
            AtomicInteger calls = new AtomicInteger();
            FutureTask<?> future = fixture.submit(callable, () -> {
                entered.countDown();
                assertTrue(release.await(5, TimeUnit.SECONDS), "Test did not release the running task");
                assertFalse(Thread.currentThread().isInterrupted());
                return calls.incrementAndGet();
            });
            FutureTask<Void> callback = fixture.startCallback();
            try {
                assertTrue(entered.await(5, TimeUnit.SECONDS), "Payload did not start");
                ServerRunnableLockFactory.stop();
            } finally {
                release.countDown();
            }
            callback.get(5, TimeUnit.SECONDS);
            assertEquals(1, calls.get());
            assertFalse(future.isCancelled());
            assertEquals(callable ? 1 : null, future.get(1, TimeUnit.SECONDS));
            assertNull(fixture.map.getTask(fixture.key));
        } finally {
            release.countDown();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void submissionsAfterStopReturnDistinctCancelledFuturesWithoutScheduling(boolean callable) throws Exception {
        try (Fixture fixture = new Fixture()) {
            AtomicInteger calls = new AtomicInteger();
            FutureTask<Void> predecessor = new FutureTask<>(() -> null);
            fixture.installPredecessor(predecessor);
            ServerRunnableLockFactory.stop();
            Callable<Integer> supplier = calls::incrementAndGet;
            Runnable action = calls::incrementAndGet;
            FutureTask<?> immediate = callable
                    ? fixture.factory.waitThenRun(supplier, fixture.key)
                    : fixture.factory.waitThenRun(action, fixture.key);
            FutureTask<?> delayed = callable
                    ? fixture.factory.waitThenRun(7L, supplier, fixture.key)
                    : fixture.factory.waitThenRun(7L, action, fixture.key);
            assertCancelledWithoutPayload(immediate, calls);
            assertCancelledWithoutPayload(delayed, calls);
            assertNotSame(immediate, delayed, "Cancellation state must not be shared between submissions");
            assertNull(fixture.scheduled, "A stopped factory must not ask the server to schedule work");
            assertFalse(predecessor.isDone());
            assertFalse(predecessor.isCancelled());
            assertSame(predecessor, fixture.map.getTask(fixture.key));
            assertSame(fixture.factory, fixture.map.getFactory(fixture.key));
            assertEquals(1, fixture.factory.taskSize());
        }
    }

    private static void assertCancelledWithoutPayload(FutureTask<?> future, AtomicInteger calls) {
        assertEquals(0, calls.get());
        assertTrue(future.isDone(), "Cancelled future must not strand a caller");
        assertTrue(future.isCancelled());
        assertThrows(CancellationException.class, () -> future.get(1, TimeUnit.SECONDS));
    }

    private record Key(UUID id) {}

    private static final class Fixture implements AutoCloseable {
        private final Field stopFlag;
        private final boolean previouslyStopped;
        private final Key key = new Key(UUID.randomUUID());
        private final ServerRunnableLockFactory.ObjectMap<Key> map =
                ServerRunnableLockFactory.ObjectMap.getInstance(Key.class);
        private final ServerRunnableLockFactory<Key> factory;
        private final Logger logger = Logger.getAnonymousLogger();
        private Runnable scheduled;
        private FutureTask<?> submitted;
        private FutureTask<?> predecessor;
        private Thread worker;

        private Fixture() throws ReflectiveOperationException {
            stopFlag = ServerRunnableLockFactory.class.getDeclaredField("serverStop");
            stopFlag.setAccessible(true);
            previouslyStopped = stopFlag.getBoolean(null);
            stopFlag.setBoolean(null, false);
            BukkitScheduler scheduler = proxy(BukkitScheduler.class, null, null);
            Server server = proxy(Server.class, null, scheduler);
            Plugin plugin = proxy(Plugin.class, server, scheduler);
            factory = ServerRunnableLockFactory.getInstance(plugin, Key.class);
        }

        private FutureTask<?> submit(boolean callable, Callable<Integer> payload) {
            submitted = callable
                    ? factory.waitThenRun(7L, payload, key)
                    : factory.waitThenRun(7L, (Runnable) () -> {
                        try {
                            payload.call();
                        } catch (Exception failure) {
                            throw new AssertionError(failure);
                        }
                    }, key);
            assertNotNull(scheduled, "The real factory must submit its scheduler callback");
            return submitted;
        }

        private void installPredecessor(FutureTask<?> task) {
            predecessor = task;
            map.put(key, task, factory);
        }

        private FutureTask<Void> startCallback() {
            FutureTask<Void> callback = new FutureTask<>(scheduled, null);
            worker = new Thread(callback, "FinalTECH shutdown regression");
            worker.setDaemon(true);
            worker.start();
            return callback;
        }

        @Override
        public void close() throws Exception {
            try {
                // If a future regression times out, release this isolated fixture without
                // interrupting a real worker or allowing its queued payload to execute.
                if (submitted != null) submitted.cancel(false);
                if (predecessor != null) predecessor.cancel(false);
                if (worker != null && worker.isAlive()) {
                    stopFlag.setBoolean(null, false);
                    worker.interrupt();
                    worker.join(5000);
                    assertFalse(worker.isAlive(), "Fixture callback leaked after failure cleanup");
                }
            } finally {
                map.remove(key);
                stopFlag.setBoolean(null, previouslyStopped);
            }
        }

        private <T> T proxy(Class<T> type, Server server, BukkitScheduler scheduler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                    (instance, method, args) -> switch (method.getName()) {
                        case "hashCode" -> System.identityHashCode(instance);
                        case "equals" -> instance == args[0];
                        case "toString" -> "FinalTECH shutdown fixture " + type.getSimpleName();
                        case "getServer" -> server;
                        case "getScheduler" -> scheduler;
                        case "getLogger" -> logger;
                        case "runTaskLaterAsynchronously" -> {
                            assertSame(factory.getPlugin(), args[0]);
                            assertEquals(7L, args[2]);
                            assertNull(scheduled, "Unexpected duplicate scheduling");
                            scheduled = (Runnable) args[1];
                            yield null;
                        }
                        default -> throw new AssertionError("Unexpected server API call: " + method);
                    }));
        }
    }
}
