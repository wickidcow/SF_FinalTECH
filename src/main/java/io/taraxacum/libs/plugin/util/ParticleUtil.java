package io.taraxacum.libs.plugin.util;

import io.taraxacum.common.util.JavaUtil;
import io.taraxacum.libs.slimefun.compat.LegacySlimefunApiCompat;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParticleUtil {
    private static final double[] BLOCK_CUBE_OFFSET_X = new double[]{0, 1, 0, 0, 1, 1, 0, 1};
    private static final double[] BLOCK_CUBE_OFFSET_Y = new double[]{0, 0, 1, 0, 1, 0, 1, 1};
    private static final double[] BLOCK_CUBE_OFFSET_Z = new double[]{0, 0, 0, 1, 0, 1, 1, 1};

    private record ParticlePoint(World world, double x, double y, double z) {
        @Nonnull
        private Location location() {
            return new Location(world, x, y, z);
        }

        private void spawn(@Nonnull Particle particle) {
            world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    private record ParticleBatchKey(World world, int chunkX, int chunkZ, long tick) {
    }

    private ParticleUtil() {
    }

    public static void drawLineByTotalAmount(
            @Nonnull Particle particle,
            int totalAmount,
            @Nonnull Location... locations) {
        Map<ParticleBatchKey, List<ParticlePoint>> batches = new LinkedHashMap<>();

        for (int i = 0; i + 1 < locations.length; i++) {
            Location location1 = locations[i];
            Location location2 = locations[i + 1];

            if (totalAmount < 1
                    || location1.getWorld() == null
                    || location1.getWorld() != location2.getWorld()) {
                return;
            }

            World world = location1.getWorld();
            double[] x = JavaUtil.disperse(totalAmount, location1.getX(), location2.getX());
            double[] y = JavaUtil.disperse(totalAmount, location1.getY(), location2.getY());
            double[] z = JavaUtil.disperse(totalAmount, location1.getZ(), location2.getZ());

            for (int j = 0; j < totalAmount; j++) {
                addPoint(batches, new ParticlePoint(world, x[j], y[j], z[j]), 0);
            }
        }

        scheduleBatches(particle, batches);
    }

    public static void drawLineByTotalAmount(
            @Nonnull Particle particle,
            int totalAmount,
            @Nonnull List<Location> locationList) {
        Location[] locations = new Location[locationList.size()];
        for (int i = 0; i < locations.length; i++) {
            locations[i] = locationList.get(i);
        }
        ParticleUtil.drawLineByTotalAmount(particle, totalAmount, locations);
    }

    public static void drawLineByDistance(
            @Nonnull Plugin plugin,
            @Nonnull Particle particle,
            long interval,
            double distance,
            @Nonnull Location... locations) {
        Map<ParticleBatchKey, List<ParticlePoint>> batches = new LinkedHashMap<>();
        long time = 0;

        for (int i = 0; i + 1 < locations.length; i++) {
            Location location1 = locations[i];
            Location location2 = locations[i + 1];

            if (distance == 0
                    || location1.getWorld() == null
                    || location1.getWorld() != location2.getWorld()) {
                return;
            }

            World world = location1.getWorld();
            double x = location1.getX();
            double y = location1.getY();
            double z = location1.getZ();

            double d = location1.distance(location2);
            int particleCount = (int) (d / distance);
            if (particleCount <= 0) {
                time += interval;
                continue;
            }

            double px = (location2.getX() - x) / particleCount;
            double py = (location2.getY() - y) / particleCount;
            double pz = (location2.getZ() - z) / particleCount;

            double t = time;
            double stepDuration = (double) interval / particleCount;
            for (int j = 0; j < particleCount; j++) {
                x += px;
                y += py;
                z += pz;

                long tick = (long) (t / 50.0);
                addPoint(batches, new ParticlePoint(world, x, y, z), tick);
                t += stepDuration;
            }

            time += interval;
        }

        scheduleBatches(particle, batches);
    }

    public static void drawLineByDistance(
            @Nonnull Plugin plugin,
            @Nonnull Particle particle,
            long interval,
            double distance,
            @Nonnull List<Location> locationList) {
        Location[] locations = new Location[locationList.size()];
        for (int i = 0; i < locations.length; i++) {
            locations[i] = locationList.get(i);
        }
        ParticleUtil.drawLineByDistance(plugin, particle, interval, distance, locations);
    }

    public static void drawCubeByBlock(
            @Nonnull Plugin plugin,
            @Nonnull Particle particle,
            long interval,
            Block... blocks) {
        long time = 0;
        for (Block block : blocks) {
            Location location = block.getLocation();
            World world = location.getWorld();
            if (world == null) {
                continue;
            }

            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            long tick = time < 50 ? 0 : time / 50;

            List<ParticlePoint> points = new ArrayList<>(BLOCK_CUBE_OFFSET_X.length);
            for (int i = 0; i < BLOCK_CUBE_OFFSET_X.length; i++) {
                points.add(new ParticlePoint(
                        world,
                        x + BLOCK_CUBE_OFFSET_X[i],
                        y + BLOCK_CUBE_OFFSET_Y[i],
                        z + BLOCK_CUBE_OFFSET_Z[i]));
            }
            scheduleBatch(particle, points, tick);
            time += interval;
        }
    }

    public static void drawCubeByBlock(
            @Nonnull Plugin plugin,
            @Nonnull Particle particle,
            long interval,
            List<Block> blockList) {
        Block[] blocks = new Block[blockList.size()];
        for (int i = 0; i < blockList.size(); i++) {
            blocks[i] = blockList.get(i);
        }
        ParticleUtil.drawCubeByBlock(plugin, particle, interval, blocks);
    }

    private static void addPoint(
            @Nonnull Map<ParticleBatchKey, List<ParticlePoint>> batches,
            @Nonnull ParticlePoint point,
            long tick) {
        ParticleBatchKey key = new ParticleBatchKey(
                point.world(),
                chunkCoordinate(point.x()),
                chunkCoordinate(point.z()),
                tick);
        batches.computeIfAbsent(key, ignored -> new ArrayList<>()).add(point);
    }

    private static void scheduleBatches(
            @Nonnull Particle particle,
            @Nonnull Map<ParticleBatchKey, List<ParticlePoint>> batches) {
        for (Map.Entry<ParticleBatchKey, List<ParticlePoint>> entry : batches.entrySet()) {
            scheduleBatch(particle, entry.getValue(), entry.getKey().tick());
        }
    }

    private static void scheduleBatch(
            @Nonnull Particle particle,
            @Nonnull List<ParticlePoint> points,
            long delayTicks) {
        if (points.isEmpty()) {
            return;
        }

        List<ParticlePoint> immutablePoints = List.copyOf(points);
        Location anchor = immutablePoints.get(0).location();
        Runnable task = () -> immutablePoints.forEach(point -> point.spawn(particle));

        if (delayTicks <= 0 && LegacySlimefunApiCompat.isOwnedByCurrentRegion(anchor)) {
            task.run();
        } else {
            LegacySlimefunApiCompat.runAt(anchor, task, Math.max(delayTicks, 0));
        }
    }

    private static int chunkCoordinate(double coordinate) {
        return ((int) Math.floor(coordinate)) >> 4;
    }
}
