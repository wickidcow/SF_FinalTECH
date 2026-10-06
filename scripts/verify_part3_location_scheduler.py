#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


compat = read("src/main/java/io/taraxacum/libs/slimefun/compat/LegacySlimefunApiCompat.java")
capacitor = read("src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireCapacitor.java")
resistance = read("src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireResistance.java")
entropy = read("src/main/java/io/taraxacum/finaltech/core/item/machine/EntropySeed.java")
concept = read("src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/EquivalentConcept.java")

require(
    "Slimefun.runSyncAt(location, runnable);" in compat
    and "Slimefun.runSyncAt(location, runnable, delay);" in compat,
    "LegacySlimefunApiCompat must use Slimefun's location-owned scheduler",
)
require(
    "public static void runAt(@Nonnull Location location, @Nonnull Runnable runnable)" in compat,
    "LegacySlimefunApiCompat lost the immediate location-owned scheduler boundary",
)
require(
    "@Nonnull Runnable runnable,\n            long delay" in compat,
    "LegacySlimefunApiCompat lost the delayed location-owned scheduler boundary",
)

for source, name in (
    (capacitor, "VariableWireCapacitor"),
    (resistance, "VariableWireResistance"),
    (entropy, "EntropySeed"),
    (concept, "EquivalentConcept"),
):
    require(
        "LegacySlimefunApiCompat.runAt(" in source,
        f"{name} must schedule identity/block transitions through the location-owned boundary",
    )
    for forbidden in (
        "runTaskLaterAsynchronously",
        "runTaskAsynchronously",
        ".getScheduler().runTask(",
        ".getScheduler().runTaskLater(",
        "getLocationRunnableFactory().waitThenRun",
        "BlockTickerUtil.runTask(",
    ):
        require(
            forbidden not in source,
            f"{name} reintroduced non-location-owned transition scheduling: {forbidden}",
        )

for source, name in (
    (capacitor, "VariableWireCapacitor"),
    (resistance, "VariableWireResistance"),
):
    require(
        "if (!this.getId().equals(LegacyBlockDataCompat.getSlimefunId(location)))" in source,
        f"{name} must revalidate source identity on the owning scheduler before replacement",
    )
    require(
        source.index("LegacyBlockDataCompat.removeBlock(location)")
        < source.index("LegacyBlockDataCompat.setSlimefunId(location"),
        f"{name} must retain remove-before-recreate identity ordering",
    )
    require(
        source.count("LegacySlimefunApiCompat.runAt(location") >= 2,
        f"{name} must own both the identity transition and deferred physical block update",
    )

require(
    "LegacySlimefunApiCompat.runAt(location, () -> transformAt(block));" in entropy,
    "EntropySeed must hand source transformation work to the owning scheduler",
)
require(
    "if (!this.getId().equals(LegacyBlockDataCompat.getSlimefunId(location)))" in entropy,
    "EntropySeed must revalidate its identity before removing block data",
)
require(
    entropy.count("!LegacyBlockDataCompat.hasBlockData(location)") == 2,
    "EntropySeed replacements must refuse occupied block-data records",
)
require(
    "Slimefun.getTickerTask().getTickRate() + 1L" in entropy,
    "EntropySeed must preserve its existing delayed replacement timing",
)

require(
    "LegacySlimefunApiCompat.runAt(block.getLocation(), () -> tickAt(block));" in concept,
    "EquivalentConcept source state work must execute on the owning scheduler",
)
require(
    "if (!this.getId().equals(LegacyBlockDataCompat.getSlimefunId(source)))" in concept,
    "EquivalentConcept must revalidate its source identity before mutation",
)
require(
    "LegacySlimefunApiCompat.runAt(location, () -> {" in concept,
    "EquivalentConcept target transitions must use each target location's owning scheduler",
)
require(
    "Slimefun.getTickerTask().getTickRate() + 1L" in concept,
    "EquivalentConcept must preserve delayed source replacement timing",
)
require(
    "targetBlock.getType() == Material.AIR" in concept
    and "EquivalentConcept.this.getId().equals(LegacyBlockDataCompat.getSlimefunId(location))" in concept,
    "EquivalentConcept must revalidate target block and identity before the deferred physical block update",
)

print("FinalTECH Part 3 location-owned transitions: PASS")
