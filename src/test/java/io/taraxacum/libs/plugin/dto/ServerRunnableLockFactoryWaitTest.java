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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ServerRunnableLockFactoryWaitTest {
    @Test
    void missingKeysReturnWithoutCreatingLocks() {
        try (Fixture fixture = new Fixture()) {
            Key missing = fixture.key("missing");
            fixture.factory.waitFor(missing);
            assertNull(fixture.map.getTask(missing));
            assertNull(fixture.map.getFactory(missing));
            assertEquals(0, fixture.diagnostics.get());
        }
    }

    @Test
    void completedPredecessorsRetainIdentity() {
        try (Fixture fixture = new Fixture()) {
            Key key = fixture.key("completed");
            FutureTask<Void> completed = new FutureTask<>(() -> null);
            completed.run();
            fixture.map.put(key, completed, fixture.factory);
            fixture.factory.waitFor(key);
            assertSame(completed, fixture.map.getTask(key));
            assertSame(fixture.factory, fixture.map.getFactory(key));
            assertEquals(0, fixture.diagnostics.get());
        }
    }

    @Test
    void cancelledFinalPredecessorDoesNotRecursePastEnd() {
        try (Fixture fixture = new Fixture()) {
            Key key = fixture.racing("cancelled", Outcome.CANCEL);
            FutureTask<?> predecessor = fixture.map.getTask(key);
            fixture.factory.waitFor(key);
            assertTrue(predecessor.isCancelled());
            assertSame(predecessor, fixture.map.getTask(key));
            assertSame(fixture.factory, fixture.map.getFactory(key));
            assertEquals(List.of("cancelled"), fixture.getOrder);
            assertEquals(1, fixture.diagnostics.get());
        }
    }

    @Test
    void failedFinalPredecessorDoesNotRecursePastEnd() {
        try (Fixture fixture = new Fixture()) {
            Key key = fixture.racing("failed", Outcome.FAIL);
            FutureTask<?> predecessor = fixture.map.getTask(key);
            fixture.factory.waitFor(key);
            assertSame(predecessor, fixture.map.getTask(key));
            assertSame(fixture.factory, fixture.map.getFactory(key));
            assertEquals(List.of("failed"), fixture.getOrder);
            assertEquals(1, fixture.diagnostics.get());
        }
    }

    @Test
    void cancelledFirstPredecessorStillWaitsForLaterKey() {
        try (Fixture fixture = new Fixture()) {
            Key first = fixture.racing("first", Outcome.CANCEL);
            Key second = fixture.racing("second", Outcome.COMPLETE);
            fixture.factory.waitFor(first, second);
            assertEquals(List.of("first", "second"), fixture.getOrder);
            assertEquals(1, fixture.diagnostics.get());
            assertSame(fixture.factory, fixture.map.getFactory(first));
            assertSame(fixture.factory, fixture.map.getFactory(second));
        }
    }

    @Test
    void failedMiddlePredecessorPreservesRemainingOrder() {
        try (Fixture fixture = new Fixture()) {
            Key first = fixture.racing("first", Outcome.COMPLETE);
            Key second = fixture.racing("second", Outcome.FAIL);
            Key third = fixture.racing("third", Outcome.COMPLETE);
            fixture.factory.waitFor(first, second, third);
            assertEquals(List.of("first", "second", "third"), fixture.getOrder);
            assertEquals(1, fixture.diagnostics.get());
        }
    }

    @Test
    void consecutiveFailuresAreEachVisitedOnce() {
        try (Fixture fixture = new Fixture()) {
            Key first = fixture.racing("first", Outcome.FAIL);
            Key second = fixture.racing("second", Outcome.CANCEL);
            Key third = fixture.racing("third", Outcome.FAIL);
            fixture.factory.waitFor(first, second, third);
            assertEquals(List.of("first", "second", "third"), fixture.getOrder);
            assertEquals(3, fixture.diagnostics.get());
        }
    }

    @Test
    void duplicateKeysDoNotWaitAgainAfterCompletion() {
        try (Fixture fixture = new Fixture()) {
            Key key = fixture.racing("duplicate", Outcome.COMPLETE);
            fixture.factory.waitFor(key, key);
            assertEquals(List.of("duplicate"), fixture.getOrder);
            assertEquals(0, fixture.diagnostics.get());
        }
    }

    @Test
    void emptyPublicWaitRetainsExistingDiagnostic() {
        try (Fixture fixture = new Fixture()) {
            fixture.factory.waitFor();
            assertEquals(1, fixture.diagnostics.get());
            assertTrue(fixture.getOrder.isEmpty());
        }
    }

    @Test
    void cancellationWhileActuallyWaitingTerminates() throws Exception {
        checkBlockedOutcome(Outcome.CANCEL);
    }

    @Test
    void failureWhileActuallyWaitingTerminates() throws Exception {
        checkBlockedOutcome(Outcome.FAIL);
    }

    private void checkBlockedOutcome(Outcome outcome) throws Exception {
        try (Fixture fixture = new Fixture()) {
            Key key = fixture.key("blocked");
            WaitingFutureTask predecessor = new WaitingFutureTask();
            fixture.map.put(key, predecessor, fixture.factory);
            FutureTask<Void> waiter = new FutureTask<>(() -> {
                fixture.factory.waitFor(key);
                return null;
            });
            Thread thread = new Thread(waiter, "finaltech-waiter-regression");
            thread.setDaemon(true);
            thread.start();
            try {
                assertTrue(predecessor.getEntered.await(5, TimeUnit.SECONDS), "Waiter did not enter get()");
                if (outcome == Outcome.CANCEL) {
                    assertTrue(predecessor.cancel(false));
                } else {
                    predecessor.fail();
                }
                assertNull(waiter.get(5, TimeUnit.SECONDS));
                assertSame(predecessor, fixture.map.getTask(key));
                assertSame(fixture.factory, fixture.map.getFactory(key));
                assertEquals(1, fixture.diagnostics.get());
            } finally {
                predecessor.cancel(false);
                thread.interrupt();
                thread.join(5000);
            }
            assertFalse(thread.isAlive(), "Waiter thread leaked");
        }
    }

    private enum Outcome { COMPLETE, CANCEL, FAIL }

    private record Key(UUID id, String label) {}

    /** Reproduces a completion racing the isDone()/get() boundary using real JDK future states. */
    private static final class TransitionFutureTask extends FutureTask<Void> {
        private final String label;
        private final Outcome outcome;
        private final List<String> getOrder;

        private TransitionFutureTask(String label, Outcome outcome, List<String> getOrder) {
            super(() -> {
                if (outcome == Outcome.FAIL) {
                    throw new IllegalStateException("Expected predecessor failure");
                }
                return null;
            });
            this.label = label;
            this.outcome = outcome;
            this.getOrder = getOrder;
        }

        @Override
        public boolean isDone() {
            boolean observed = super.isDone();
            if (!observed) {
                if (outcome == Outcome.CANCEL) {
                    cancel(false);
                } else {
                    run();
                }
            }
            return observed;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            getOrder.add(label);
            return super.get();
        }
    }

    private static final class WaitingFutureTask extends FutureTask<Void> {
        private final CountDownLatch getEntered = new CountDownLatch(1);

        private WaitingFutureTask() {
            super(() -> null);
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            getEntered.countDown();
            return super.get();
        }

        private void fail() {
            setException(new IllegalStateException("Expected blocked predecessor failure"));
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final List<Key> keys = new ArrayList<>();
        private final List<String> getOrder = new ArrayList<>();
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

        private Key racing(String label, Outcome outcome) {
            Key key = key(label);
            map.put(key, new TransitionFutureTask(label, outcome, getOrder), factory);
            return key;
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
                        case "toString" -> "FinalTECH waiter test " + type.getSimpleName();
                        case "getServer" -> server;
                        case "getScheduler" -> scheduler;
                        case "getLogger" -> logger;
                        default -> throw new AssertionError("Unexpected server API call: " + method);
                    }));
        }
    }
}
