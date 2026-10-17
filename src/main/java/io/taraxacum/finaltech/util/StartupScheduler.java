package io.taraxacum.finaltech.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Schedules plugin-owned startup work without using Folia's unsupported Bukkit scheduler.
 * Global callbacks are limited to configuration and registry setup, never location work.
 */
public final class StartupScheduler {
    private final Plugin plugin;
    private final boolean folia;
    private final List<Runnable> cancellations = new ArrayList<>();
    private boolean stopped;

    public StartupScheduler(Plugin plugin, boolean folia) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.folia = folia;
    }

    public synchronized void runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        checkTask(task, delayTicks);
        if (periodTicks <= 0) {
            throw new IllegalArgumentException("Timer period must be positive");
        }
        if (this.folia) {
            ScheduledTask scheduled = this.plugin.getServer().getAsyncScheduler().runAtFixedRate(
                    this.plugin, ignored -> task.run(), Math.multiplyExact(delayTicks, 50L),
                    Math.multiplyExact(periodTicks, 50L), TimeUnit.MILLISECONDS);
            this.cancellations.add(scheduled::cancel);
        } else {
            BukkitTask scheduled = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    this.plugin, task, delayTicks, periodTicks);
            this.cancellations.add(scheduled::cancel);
        }
    }

    public synchronized void runGlobalLater(Runnable task, long delayTicks) {
        runGlobalLater(this.plugin, task, delayTicks);
    }

    public synchronized void runGlobalLater(Plugin owner, Runnable task, long delayTicks) {
        Objects.requireNonNull(owner, "owner");
        checkTask(task, delayTicks);
        if (this.folia) {
            ScheduledTask scheduled = delayTicks == 0
                    ? owner.getServer().getGlobalRegionScheduler().run(owner, ignored -> task.run())
                    : owner.getServer().getGlobalRegionScheduler().runDelayed(
                            owner, ignored -> task.run(), delayTicks);
            this.cancellations.add(scheduled::cancel);
        } else {
            BukkitTask scheduled = owner.getServer().getScheduler().runTaskLater(owner, task, delayTicks);
            this.cancellations.add(scheduled::cancel);
        }
    }

    public synchronized void cancelAll() {
        this.stopped = true;
        for (Runnable cancellation : this.cancellations) {
            cancellation.run();
        }
        this.cancellations.clear();
    }

    private void checkTask(Runnable task, long delayTicks) {
        Objects.requireNonNull(task, "task");
        if (this.stopped) {
            throw new IllegalStateException("FinalTECH startup scheduler has stopped");
        }
        if (delayTicks < 0) {
            throw new IllegalArgumentException("Task delay cannot be negative");
        }
    }
}
