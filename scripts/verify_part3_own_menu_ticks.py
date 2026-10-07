#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


OWN_LOCATION_BASES = (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/AbstractElectricMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/AbstractEnergyProviderMachine.java",
)

for path in OWN_LOCATION_BASES:
    source = read(path)
    require(
        "protected boolean requiresLocationOwnedTick() {\n        return true;\n    }" in source,
        f"{path} must opt its single-location hierarchy out of forced asynchronous ticking",
    )

OWN_MENU_MACHINES = (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/basic/AbstractBasicMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/advanced/AbstractAdvanceMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/conversion/AbstractConversionMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/extraction/AbstractExtractionMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/generator/AbstractGeneratorMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/logic/AbstractLogicComparator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/AdvancedAutoCraftFrame.java",
)

for path in OWN_MENU_MACHINES:
    source = read(path)
    require(
        "LegacyBlockDataCompat.getMenu(" in source,
        f"{path} must remain an own-menu machine for this scheduler contract",
    )
    require(
        "protected boolean requiresLocationOwnedTick() {\n        return true;\n    }" in source,
        f"{path} must opt out of forced asynchronous ticking",
    )
    require(
        "protected boolean isSynchronized() {\n        return false;\n    }" in source,
        f"{path} historical machine synchronization declaration unexpectedly changed",
    )

for path in OWN_MENU_MACHINES[:-1]:
    source = read(path)
    require(
        "blockMenu.toInventory()" in source
        or "blockMenu.pushItem(" in source
        or "blockMenu.consumeItem(" in source
        or "blockMenu.replaceExistingItem(" in source,
        f"{path} no longer mutates its own live menu; revisit whether the owned-tick override is still needed",
    )

frame = read(OWN_MENU_MACHINES[-1])
require(
    "blockMenu.hasViewer()" in frame
    and "Icon.updateQuantityModule(" in frame,
    "AdvancedAutoCraftFrame must retain its live-menu viewer/module update behavior",
)

for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/DustGenerator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/TimeGenerator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireCapacitor.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireResistance.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/AbstractElectricCapacitor.java",
):
    source = read(path)
    require(
        "LegacyBlockDataCompat.getMenu(" in source,
        f"{path} must remain a single-location live-menu consumer under its owned base",
    )

for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/PointTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedPointTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LineTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLineTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/MeshTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedMeshTransfer.java",
):
    source = read(path)
    require(
        "requiresLocationOwnedTick()" not in source,
        f"{path} must remain outside the single-location owned-menu batch until cross-region cargo coordination is designed",
    )

print("FinalTECH Part 3 own-menu owned-tick safety: PASS")
