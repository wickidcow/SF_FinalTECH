package io.taraxacum.libs.plugin.dto;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ServerRunnableLockFactoryDrainTest {
    @Test
    void emptyDrainDoesNotCreateLocksOrSubmitTasks() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.factory.waitAllTask();
            assertEquals(0, fixture.factory.taskSize());
            assertEquals(0, fixture.diagnostics.get());
        }
    }

    @Test
    void completedTasksAreSkippedAndKeepTheirIdentities() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<Key> keys = fixture.pendingKeys(3);
            for (Key key : keys) {
                fixture.task(key).run();
            }
            fixture.factory.waitAllTask();
            assertTrue(fixture.order.isEmpty());
            assertEquals(3, fixture.factory.taskSize());
            for (Key key : keys) {
                assertSame(fixture.factory, fixture.map.getFactory(key));
                assertTrue(fixture.map.getTask(key).isDone());
            }
        }
    }

    @Test
    void selfRemovalDuringWaitDoesNotInvalidateTraversal() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<Key> keys = fixture.pendingKeys(3);
            for (Key key : keys) {
                fixture.task(key).action = () -> fixture.map.remove(key);
            }
            fixture.factory.waitAllTask();
            assertEquals(labels(keys), fixture.order);
            assertEquals(0, fixture.factory.taskSize());
            for (Key key : keys) {
                assertNull(fixture.map.getFactory(key));
            }
        }
    }

    @Test
    void removalOfLaterKeySkipsItAndContinues() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<Key> keys = fixture.pendingKeys(3);
            Task removed = fixture.task(keys.get(1));
            fixture.task(keys.get(0)).action = () -> fixture.map.remove(keys.get(1));
            fixture.factory.waitAllTask();
            assertEquals(List.of(keys.get(0).label(), keys.get(2).label()), fixture.order);
            assertFalse(removed.isDone());
            assertNull(fixture.map.getTask(keys.get(1)));
            assertEquals(2, fixture.factory.taskSize());
        }
    }

    @Test
    void keysAddedDuringDrainAreDeferredUntilNextInvocation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<Key> keys = fixture.pendingKeys(2);
            Key added = fixture.key("added");
            Task addedTask = new Task(fixture, added.label());
            fixture.task(keys.get(0)).action = () -> fixture.map.put(added, addedTask, fixture.factory);
            fixture.factory.waitAllTask();
            assertEquals(labels(keys), fixture.order);
            assertSame(addedTask, fixture.map.getTask(added));
            assertFalse(addedTask.isDone());
            fixture.factory.waitAllTask();
            List<String> expected = new ArrayList<>(labels(keys));
            expected.add(added.label());
            assertEquals(expected, fixture.order);
            assertTrue(addedTask.isDone());
        }
    }

    @Test
    void replacementAtExistingKeyIsLookedUpWhenVisited() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<Key> keys = fixture.pendingKeys(2);
            Task previous = fixture.task(keys.get(1));
            Task replacement = new Task(fixture, "replacement");
            fixture.task(keys.get(0)).action = () -> fixture.map.put(keys.get(1), replacement, fixture.factory);
            fixture.factory.waitAllTask();
            assertEquals(List.of(keys.get(0).label(), "replacement"), fixture.order);
            assertFalse(previous.isDone());
            assertSame(replacement, fixture.map.getTask(keys.get(1)));
            assertSame(fixture.factory, fixture.map.getFactory(keys.get(1)));
        }
    }

    @Test
    void sharedFutureUnderSeveralKeysIsWaitedOnlyOnce() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<Key> keys = fixture.pendingKeys(3);
            Task shared = new Task(fixture, "shared");
            for (Key key : keys) {
                fixture.map.put(key, shared, fixture.factory);
            }
            fixture.factory.waitAllTask();
            assertEquals(List.of("shared"), fixture.order);
            for (Key key : keys) {
                assertSame(shared, fixture.map.getTask(key));
                assertSame(fixture.factory, fixture.map.getFactory(key));
            }
        }
    }

    @Test
    void timeoutKeepsFiveSecondBudgetDiagnosticsAndContinuation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<Key> keys = fixture.pendingKeys(2);
            Task timedOut = fixture.task(keys.get(0));
            timedOut.action = () -> { throw new TimeoutException("Expected drain timeout"); };
            fixture.factory.waitAllTask();
            assertEquals(labels(keys), fixture.order);
            assertEquals(1, fixture.diagnostics.get());
            assertFalse(timedOut.isDone());
            assertSame(timedOut, fixture.map.getTask(keys.get(0)));
            assertSame(fixture.factory, fixture.map.getFactory(keys.get(0)));
        }
    }

    @Test
    void executionFailureStillPropagatesWithoutRemovingIdentity() {
        try (Fixture fixture = new Fixture()) {
            Key key = fixture.key("failure");
            IllegalStateException cause = new IllegalStateException("Expected drain failure");
            FutureTask<Void> failed = new FutureTask<>(() -> { throw cause; }) {
                @Override
                public Void get(long timeout, TimeUnit unit)
                        throws InterruptedException, ExecutionException, TimeoutException {
                    run();
                    return super.get(timeout, unit);
                }
            };
            fixture.map.put(key, failed, fixture.factory);
            ExecutionException actual = assertThrows(ExecutionException.class, fixture.factory::waitAllTask);
            assertSame(cause, actual.getCause());
            assertSame(failed, fixture.map.getTask(key));
            assertSame(fixture.factory, fixture.map.getFactory(key));
        }
    }

    @Test
    void interruptionStillPropagatesWithoutRemovingIdentity() {
        try (Fixture fixture = new Fixture()) {
            Key key = fixture.key("interrupted");
            FutureTask<Void> pending = new FutureTask<>(() -> null);
            fixture.map.put(key, pending, fixture.factory);
            try {
                Thread.currentThread().interrupt();
                assertThrows(InterruptedException.class, fixture.factory::waitAllTask);
            } finally {
                Thread.interrupted();
            }
            assertSame(pending, fixture.map.getTask(key));
            assertFalse(pending.isDone());
        }
    }

    @Test
    void historicalNullMapKeyRemainsUsable() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.keys.add(null);
            Task task = new Task(fixture, "null-key");
            fixture.map.put(null, task, fixture.factory);
            fixture.factory.waitAllTask();
            assertEquals(List.of("null-key"), fixture.order);
            assertSame(task, fixture.map.getTask(null));
            assertSame(fixture.factory, fixture.map.getFactory(null));
        }
    }

    @Test
    void workerCanRemoveItsKeyBeforeCompletingWhileDrainWaits() throws Exception {
        try (Fixture fixture = new Fixture()) {
            List<Key> keys = fixture.pendingKeys(2);
            CountDownLatch getEntered = new CountDownLatch(1);
            FutureTask<Void> predecessor = new FutureTask<>(() -> null) {
                @Override
                public Void get(long timeout, TimeUnit unit)
                        throws InterruptedException, ExecutionException, TimeoutException {
                    assertFalse(Thread.holdsLock(fixture.map), "Drain must release the map monitor before waiting");
                    getEntered.countDown();
                    return super.get(timeout, unit);
                }
            };
            fixture.map.put(keys.get(0), predecessor, fixture.factory);
            FutureTask<Void> waiter = new FutureTask<>(() -> {
                fixture.factory.waitAllTask();
                return null;
            });
            Thread thread = new Thread(waiter, "finaltech-drain-regression");
            thread.setDaemon(true);
            thread.start();
            try {
                assertTrue(getEntered.await(5, TimeUnit.SECONDS), "Drain never entered the predecessor wait");
                fixture.map.remove(keys.get(0));
                predecessor.run();
                assertNull(waiter.get(5, TimeUnit.SECONDS));
                assertEquals(List.of(keys.get(1).label()), fixture.order);
                assertNull(fixture.map.getTask(keys.get(0)));
                assertSame(fixture.factory, fixture.map.getFactory(keys.get(1)));
            } finally {
                predecessor.cancel(false);
                waiter.cancel(true);
                thread.interrupt();
                thread.join(5000);
            }
            assertFalse(thread.isAlive(), "Drain thread leaked");
        }
    }

    private static List<String> labels(List<Key> keys) {
        return keys.stream().map(Key::label).toList();
    }

    private record Key(UUID id, String label) {}

    @FunctionalInterface
    private interface WaitAction {
        void run() throws InterruptedException, ExecutionException, TimeoutException;
    }

    /** Runs a controlled map mutation at the real timed-Future.get boundary. */
    private static final class Task extends FutureTask<Void> {
        private final Fixture fixture;
        private final String label;
        private WaitAction action = () -> {};

        private Task(Fixture fixture, String label) {
            super(() -> null);
            this.fixture = fixture;
            this.label = label;
        }

        @Override
        public Void get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            assertEquals(5L, timeout);
            assertSame(TimeUnit.SECONDS, unit);
            assertFalse(Thread.holdsLock(fixture.map), "Drain must not wait with the map monitor held");
            fixture.order.add(label);
            action.run();
            run();
            return super.get(timeout, unit);
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final List<Key> keys = new ArrayList<>();
        private final List<String> order = new ArrayList<>();
        private final AtomicInteger diagnostics = new AtomicInteger();
        private final ServerRunnableLockFactory.ObjectMap<Key> map =
                ServerRunnableLockFactory.ObjectMap.getInstance(Key.class);
        private final ServerRunnableLockFactory<Key> factory;

        private Fixture() {
            Logger logger = Logger.getAnonymousLogger();
            logger.setUseParentHandlers(false);
            logger.addHandler(new Handler() {
                @Override public void publish(LogRecord record) { diagnostics.incrementAndGet(); }
                @Override public void flush() {}
                @Override public void close() {}
            });
            BukkitScheduler scheduler = proxy(BukkitScheduler.class, null, null, logger);
            Server server = proxy(Server.class, null, scheduler, logger);
            Plugin plugin = proxy(Plugin.class, server, scheduler, logger);
            factory = ServerRunnableLockFactory.getInstance(plugin, Key.class);
        }

        private Key key(String label) {
            Key key = new Key(UUID.randomUUID(), label);
            keys.add(key);
            return key;
        }

        private List<Key> pendingKeys(int count) {
            for (int i = 0; i < count; i++) {
                Key key = key("key-" + i);
                map.put(key, new Task(this, key.label()), factory);
            }
            // Observe the map's existing order rather than assuming a HashMap sort order.
            synchronized (map) {
                return new ArrayList<>(map.keySet());
            }
        }

        private Task task(Key key) {
            return (Task) map.getTask(key);
        }

        @Override
        public void close() {
            for (Key key : keys) {
                map.remove(key);
            }
        }

        private static <T> T proxy(Class<T> type, Server server, BukkitScheduler scheduler, Logger logger) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                    (instance, method, args) -> switch (method.getName()) {
                        case "hashCode" -> System.identityHashCode(instance);
                        case "equals" -> instance == args[0];
                        case "toString" -> "FinalTECH drain test " + type.getSimpleName();
                        case "getServer" -> server;
                        case "getScheduler" -> scheduler;
                        case "getLogger" -> logger;
                        default -> throw new AssertionError("Unexpected server API call: " + method);
                    }));
        }
    }
}
