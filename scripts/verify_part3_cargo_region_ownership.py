#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


compat = read("src/main/java/io/taraxacum/libs/slimefun/compat/LegacySlimefunApiCompat.java")
cargo_base = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AbstractCargo.java")
cargo_util = read("src/main/java/io/taraxacum/finaltech/util/CargoUtil.java")
point = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/PointTransfer.java")
advanced_point = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedPointTransfer.java")
location = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LocationTransfer.java")
advanced_location = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLocationTransfer.java")

require(
    "Slimefun.getSchedulerService().isOwnedByCurrentRegion(location)" in compat,
    "FinalTECH must use Slimefun's platform scheduler for region ownership checks",
)
require(
    "public static boolean areOwnedByCurrentRegion(@Nonnull Location... locations)" in compat,
    "FinalTECH must expose a multi-location ownership guard",
)
require(
    "protected boolean requiresLocationOwnedTick() {\n        return true;\n    }" in cargo_base,
    "all cargo machines must be protected from FinalTECH's forced-async override",
)

for marker in (
    "doCargoStrongSymmetry",
    "doCargoWeakSymmetry",
    "doCargoInputMain",
    "doCargoOutputMain",
):
    require(marker in cargo_util, f"CargoUtil lost cargo entry point: {marker}")
require(
    cargo_util.count("LegacySlimefunApiCompat.areOwnedByCurrentRegion(") == 4,
    "each CargoUtil doCargo mode must require current-region ownership for both endpoints",
)
for forbidden in (
    "isPrimaryThread",
    "ServerRunnableLockFactory",
    ".getScheduler().runTask(",
    ".getScheduler().runTaskLater(",
):
    require(
        forbidden not in cargo_util,
        f"CargoUtil reintroduced an unsafe scheduler fallback: {forbidden}",
    )

for source, name in ((point, "PointTransfer"), (advanced_point, "AdvancedPointTransfer")):
    require(
        "if (!LegacySlimefunApiCompat.isOwnedByCurrentRegion(location))" in source,
        f"{name} must start on its owning region",
    )
    require(
        source.count("LegacySlimefunApiCompat.isOwnedByCurrentRegion(result.getLocation())") >= 2,
        f"{name} search traversal must stop before reading foreign-region blocks",
    )
    require(
        "inputBlock == null || outputBlock == null" in source,
        f"{name} must safely abort a region-boundary search",
    )
    require(
        "LegacySlimefunApiCompat.areOwnedByCurrentRegion(" in source,
        f"{name} must revalidate both transfer endpoints before permission/inventory access",
    )
    for forbidden in (
        "boolean primaryThread",
        "ServerRunnableLockFactory",
        "SimpleCargoDTO",
        "getVanillaInventory(",
        ".getScheduler().runTask(javaPlugin,",
    ):
        require(
            forbidden not in source,
            f"{name} reintroduced its old non-owned transfer path: {forbidden}",
        )

for source, name in ((location, "LocationTransfer"), (advanced_location, "AdvancedLocationTransfer")):
    guard = "!LegacySlimefunApiCompat.isOwnedByCurrentRegion(targetLocation)"
    require(guard in source, f"{name} must reject a foreign-region recorded target")
    require(
        source.index(guard) < source.index("targetLocation.getBlock()"),
        f"{name} must check target ownership before obtaining the target block",
    )
    require(
        "CargoUtil.doCargo(cargoDTO" in source,
        f"{name} must preserve its existing cargo-mode dispatch",
    )

# Multi-location line/mesh machines are intentionally a later tranche.
for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LineTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLineTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/MeshTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedMeshTransfer.java",
):
    source = read(path)
    require(
        "ServerRunnableLockFactory" in source or "getLocationRunnableFactory" in source,
        f"{path} changed unexpectedly; line/mesh cargo must remain outside the 3.0.9 tranche",
    )

print("FinalTECH Part 3 cargo region ownership safety: PASS")
