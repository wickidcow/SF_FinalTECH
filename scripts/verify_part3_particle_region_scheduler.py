#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


particle = read("src/main/java/io/taraxacum/libs/plugin/util/ParticleUtil.java")

require(
    "import io.taraxacum.libs.slimefun.compat.LegacySlimefunApiCompat;" in particle,
    "ParticleUtil must use the Slimefun scheduler compatibility boundary",
)
require(
    "private record ParticleBatchKey(World world, int chunkX, int chunkZ, long tick)" in particle,
    "ParticleUtil must batch animated particle work by chunk and tick",
)
require(
    "batches.computeIfAbsent(key, ignored -> new ArrayList<>()).add(point);" in particle,
    "ParticleUtil must coalesce particles instead of scheduling one task per particle",
)
require(
    "LegacySlimefunApiCompat.isOwnedByCurrentRegion(anchor)" in particle
    and "LegacySlimefunApiCompat.runAt(anchor, task, Math.max(delayTicks, 0))" in particle,
    "ParticleUtil must run particle batches on the location-owning scheduler",
)
require(
    "List.copyOf(points)" in particle,
    "scheduled particle batches must not retain mutable accumulator lists",
)

for forbidden in (
    "runTaskAsynchronously",
    "runTaskLaterAsynchronously",
    ".getScheduler().runTask(",
    ".getScheduler().runTaskLater(",
):
    require(
        forbidden not in particle,
        f"ParticleUtil reintroduced Bukkit scheduler work: {forbidden}",
    )

for signature in (
    "drawLineByTotalAmount(",
    "drawLineByDistance(",
    "drawCubeByBlock(",
):
    require(signature in particle, f"ParticleUtil lost public behavior surface: {signature}")

require(
    "long tick = (long) (t / 50.0);" in particle,
    "line animation must preserve the existing millisecond-to-tick timing model",
)
require(
    "long tick = time < 50 ? 0 : time / 50;" in particle,
    "cube animation must preserve the existing first-frame/implied tick timing",
)
require(
    "chunkCoordinate(point.x())" in particle
    and "chunkCoordinate(point.z())" in particle,
    "particle batches must be conservative to a single owned chunk",
)

print("FinalTECH Part 3 particle region scheduler safety: PASS")
