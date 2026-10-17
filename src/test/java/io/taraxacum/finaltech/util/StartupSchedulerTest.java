package io.taraxacum.finaltech.util;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class StartupSchedulerTest {
    @Test
    void paperKeepsBukkitTickTimingOwnershipAndCancellation() {
        Fixture fixture = new Fixture(false);
        AtomicInteger executions = new AtomicInteger();
        fixture.scheduler.runAsyncTimer(executions::incrementAndGet, 19, 20);
        fixture.scheduler.runGlobalLater(executions::incrementAndGet, 0);
        fixture.scheduler.runGlobalLater(executions::incrementAndGet, 10);

        assertEquals(List.of("bukkit-async:19:20", "bukkit-global:0", "bukkit-global:10"), fixture.calls);
        assertEquals(0, executions.get());
        fixture.callbacks.forEach(Runnable::run);
        assertEquals(3, executions.get());
        fixture.scheduler.cancelAll();
        fixture.scheduler.cancelAll();
        assertEquals(3, fixture.cancellations.get());
    }

    @Test
    void foliaUsesNativeAsyncAndGlobalSchedulersWithoutBukkitFallback() {
        Fixture fixture = new Fixture(true);
        AtomicInteger executions = new AtomicInteger();
        fixture.scheduler.runAsyncTimer(executions::incrementAndGet, 19, 20);
        fixture.scheduler.runGlobalLater(executions::incrementAndGet, 0);
        fixture.scheduler.runGlobalLater(executions::incrementAndGet, 10);

        assertEquals(List.of("folia-async:950:1000:MILLISECONDS", "folia-global:next", "folia-global:10"), fixture.calls);
        assertEquals(0, executions.get());
        fixture.callbacks.forEach(Runnable::run);
        assertEquals(3, executions.get());
        fixture.scheduler.cancelAll();
        assertEquals(3, fixture.cancellations.get());
    }

    @Test
    void oneTickCounterKeepsZeroInitialDelayAndFiftyMillisecondPeriod() {
        Fixture fixture = new Fixture(true);
        fixture.scheduler.runAsyncTimer(() -> {}, 0, 1);
        assertEquals(List.of("folia-async:0:50:MILLISECONDS"), fixture.calls);
    }

    @Test
    void recipeRegistrationRetainsTheRegisteringAddonOwner() {
        for (boolean folia : new boolean[]{false, true}) {
            Fixture fixture = new Fixture(folia);
            Plugin addon = Fixture.proxy(Plugin.class, (object, method, args) -> {
                if (method.getName().equals("getServer")) {
                    return fixture.plugin.getServer();
                }
                throw new AssertionError("Unexpected addon method " + method.getName());
            });
            fixture.expectedOwner = addon;
            fixture.scheduler.runGlobalLater(addon, () -> {}, 7);
            assertEquals(List.of(folia ? "folia-global:7" : "bukkit-global:7"), fixture.calls);
            fixture.scheduler.cancelAll();
            assertEquals(1, fixture.cancellations.get());
        }
    }

    @Test
    void registrationAndCallbackFailuresPropagateInsteadOfClaimingReadiness() {
        Fixture fixture = new Fixture(true);
        IllegalStateException registration = new IllegalStateException("scheduler rejected registration");
        fixture.registrationFailure = registration;
        assertSame(registration, assertThrows(IllegalStateException.class,
                () -> fixture.scheduler.runGlobalLater(() -> {}, 1)));
        assertTrue(fixture.callbacks.isEmpty());
        fixture.registrationFailure = null;
        IllegalStateException setup = new IllegalStateException("setup failed");
        fixture.scheduler.runGlobalLater(() -> { throw setup; }, 1);
        assertSame(setup, assertThrows(IllegalStateException.class, fixture.callbacks.get(0)::run));
    }

    @Test
    void stopRejectsNewTasksAndInvalidTimingNeverReachesNativeScheduler() {
        Fixture fixture = new Fixture(true);
        assertThrows(IllegalArgumentException.class, () -> fixture.scheduler.runGlobalLater(() -> {}, -1));
        assertThrows(IllegalArgumentException.class, () -> fixture.scheduler.runAsyncTimer(() -> {}, 0, 0));
        assertThrows(ArithmeticException.class, () -> fixture.scheduler.runAsyncTimer(() -> {}, 0, Long.MAX_VALUE));
        assertTrue(fixture.calls.isEmpty());
        fixture.scheduler.cancelAll();
        assertThrows(IllegalStateException.class, () -> fixture.scheduler.runAsyncTimer(() -> {}, 0, 1));
        assertThrows(IllegalStateException.class, () -> fixture.scheduler.runGlobalLater(() -> {}, 1));
        assertTrue(fixture.calls.isEmpty());
    }

    private static final class Fixture {
        private final List<String> calls = new ArrayList<>();
        private final List<Runnable> callbacks = new ArrayList<>();
        private final AtomicInteger cancellations = new AtomicInteger();
        private final Plugin plugin;
        private final StartupScheduler scheduler;
        private Plugin expectedOwner;
        private RuntimeException registrationFailure;

        private Fixture(boolean folia) {
            BukkitTask bukkitTask = proxy(BukkitTask.class, (object, method, args) -> {
                if (method.getName().equals("cancel")) {
                    this.cancellations.incrementAndGet();
                    return null;
                }
                throw new AssertionError("Unexpected Bukkit task method " + method.getName());
            });
            ScheduledTask foliaTask = proxy(ScheduledTask.class, (object, method, args) -> {
                if (method.getName().equals("cancel")) {
                    this.cancellations.incrementAndGet();
                    return ScheduledTask.CancelledState.CANCELLED_BY_CALLER;
                }
                throw new AssertionError("Unexpected Folia task method " + method.getName());
            });
            BukkitScheduler bukkit = proxy(BukkitScheduler.class, (object, method, args) -> {
                assertFalse(folia, "Folia must never use Bukkit scheduler methods");
                checkRegistration(args);
                switch (method.getName()) {
                    case "runTaskTimerAsynchronously" -> this.calls.add("bukkit-async:" + args[2] + ":" + args[3]);
                    case "runTaskLater" -> this.calls.add("bukkit-global:" + args[2]);
                    default -> throw new AssertionError("Unexpected Bukkit scheduler method " + method.getName());
                }
                this.callbacks.add((Runnable) args[1]);
                return bukkitTask;
            });
            AsyncScheduler async = proxy(AsyncScheduler.class, (object, method, args) -> {
                assertTrue(folia, "Paper must retain its original Bukkit timing");
                assertEquals("runAtFixedRate", method.getName());
                checkRegistration(args);
                assertSame(TimeUnit.MILLISECONDS, args[4]);
                this.calls.add("folia-async:" + args[2] + ":" + args[3] + ":" + args[4]);
                addFoliaCallback(args[1], foliaTask);
                return foliaTask;
            });
            GlobalRegionScheduler global = proxy(GlobalRegionScheduler.class, (object, method, args) -> {
                assertTrue(folia, "Paper must retain its original Bukkit scheduler");
                checkRegistration(args);
                switch (method.getName()) {
                    case "run" -> this.calls.add("folia-global:next");
                    case "runDelayed" -> this.calls.add("folia-global:" + args[2]);
                    default -> throw new AssertionError("Unexpected global scheduler method " + method.getName());
                }
                addFoliaCallback(args[1], foliaTask);
                return foliaTask;
            });
            Server server = proxy(Server.class, (object, method, args) -> switch (method.getName()) {
                case "getScheduler" -> bukkit;
                case "getAsyncScheduler" -> async;
                case "getGlobalRegionScheduler" -> global;
                default -> throw new AssertionError("Unexpected server method " + method.getName());
            });
            this.plugin = proxy(Plugin.class, (object, method, args) -> {
                if (method.getName().equals("getServer")) {
                    return server;
                }
                throw new AssertionError("Unexpected plugin method " + method.getName());
            });
            this.scheduler = new StartupScheduler(this.plugin, folia);
        }

        private void checkRegistration(Object[] args) {
            assertSame(this.expectedOwner == null ? this.plugin : this.expectedOwner, args[0],
                    "Every task must retain its registering addon owner");
            if (this.registrationFailure != null) {
                throw this.registrationFailure;
            }
        }

        @SuppressWarnings("unchecked")
        private void addFoliaCallback(Object callback, ScheduledTask task) {
            Consumer<ScheduledTask> consumer = (Consumer<ScheduledTask>) callback;
            this.callbacks.add(() -> consumer.accept(task));
        }

        private static <T> T proxy(Class<T> type, InvocationHandler handler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
        }
    }
}
