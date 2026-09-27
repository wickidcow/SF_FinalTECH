#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

MIGRATED = {
    "src/main/java/io/taraxacum/finaltech/core/helper/MachineMaxStack.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/helper/MachineRecipeLock.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/cargo/AdvancedAutoCraftFrameMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/libs/slimefun/util/EnergyUtil.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/util/MachineUtil.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/util/PermissionUtil.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/MenuViewer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/RandomAccessorMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/RemoteAccessorMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/limit/lock/AbstractLockMachineMenu.java":
        "MachineRecipeLock.HELPER",
    "src/main/java/io/taraxacum/finaltech/core/menu/manual/EquivalentExchangeTableMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/patch/EnergyRegulatorDetailMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/AbstractElectricCapacitor.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/logic/AbstractLogicComparator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/CardOperationTable.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/listener/ConfigSaveListener.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/listener/ExpandedElectricCapacitorEnergyListener.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/cargo/AdvancedAutoCraftMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/LocationRecorder.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/PortableEnergyStorage.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/ConfigurableRemoteAccessorMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/ConsumableRemoteAccessorMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/ExpandedConfigurableRemoteAccessorMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/ExpandedConsumableRemoteAccessorMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLocationTransfer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LocationTransfer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/MatrixCraftingTable.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/ConfigurationCopier.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/ConfigurationPaster.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/NormalConfigurableElectricityShootPile.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/NormalConsumableElectricityShootPile.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/MatrixGenerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/line/pile/AbstractElectricityShootPile.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/EnergizedOperationAccelerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/OperationAccelerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/OverloadedOperationAccelerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/conversion/AbstractConversionMachine.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/extraction/AbstractExtractionMachine.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/generator/AbstractGeneratorMachine.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/CureTower.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/PurifyLevelTower.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/PurifyTimeTower.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/DistributeLeftStorageUnit.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/DistributeRightStorageUnit.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/DividedStackStorageUnit.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/LimitedStackStorageUnit.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/StackStorageUnit.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/BasicGenerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/AbstractCubeElectricGenerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/EnergizedChargeBase.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/OverloadedChargeBase.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/ConsumableSimulateClickMachine.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/SimulateClickMachine.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/DustFactoryDirt.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/MatrixAccelerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/patch/EnergyRegulatorBlockTicker.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/EtherMiner.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/MatrixExpandedCapacitor.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/EquivalentExchangeTable.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/ItemDismantleTable.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/machine/ItemDismantleTableMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/AbstractExpandedElectricCapacitor.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/advanced/AbstractAdvanceMachine.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/basic/AbstractBasicMachine.java":
        "LegacyBlockDataCompat",
}


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


for path, boundary in MIGRATED.items():
    source = read(path)
    require(
        "me.mrCookieSlime.Slimefun.api.BlockStorage" not in source
        and "BlockStorage." not in source,
        f"{path} must not directly use deprecated BlockStorage",
    )
    require(
        boundary in source,
        f"{path} must route storage access through {boundary}",
    )

machine_max_stack = read(
    "src/main/java/io/taraxacum/finaltech/core/helper/MachineMaxStack.java"
)
machine_recipe_lock = read(
    "src/main/java/io/taraxacum/finaltech/core/helper/MachineRecipeLock.java"
)
frame_menu = read(
    "src/main/java/io/taraxacum/finaltech/core/menu/cargo/AdvancedAutoCraftFrameMenu.java"
)
energy_util = read(
    "src/main/java/io/taraxacum/libs/slimefun/util/EnergyUtil.java"
)
machine_util = read(
    "src/main/java/io/taraxacum/finaltech/util/MachineUtil.java"
)
permission_util = read(
    "src/main/java/io/taraxacum/finaltech/util/PermissionUtil.java"
)
compat = read(
    "src/main/java/io/taraxacum/libs/slimefun/compat/LegacyBlockDataCompat.java"
)

require('KEY = "mms"' in machine_max_stack, "MachineMaxStack key must remain mms")
require('KEY = "rl"' in machine_recipe_lock, "MachineRecipeLock key must remain rl")
require(
    "LegacyBlockDataCompat.getMenu(location)" in frame_menu,
    "AdvancedAutoCraftFrameMenu must use the block-data menu boundary",
)
require(
    "LegacyBlockDataCompat.getSlimefunId(location)" in energy_util,
    "EnergyUtil must resolve machine identity through the dedicated identity boundary",
)
require(
    "LegacySlimefunApiCompat.getCharge" in energy_util
    and "LegacySlimefunApiCompat.setCharge" in energy_util,
    "EnergyUtil location-based energy access must use the Slimefun API compatibility boundary",
)
require(
    'LegacyBlockDataCompat.getValue(location, "id")' not in energy_util,
    'EnergyUtil must never treat the special Slimefun "id" field as ordinary block data',
)
require(
    "LegacyBlockDataCompat.getMenu(location)" in machine_util,
    "MachineUtil block-break handlers must use the modern block-menu compatibility boundary",
)
require(
    "LegacyBlockDataCompat.getValue(sourceLocation, ConstantTableUtil.CONFIG_UUID)" in permission_util,
    "PermissionUtil location-based owner lookup must use the modern block-data compatibility boundary",
)
require(
    "IgnorePermission.HELPER.getOrDefaultValue(sourceLocation)" in permission_util,
    "PermissionUtil location-based permission cache must use the location compatibility boundary",
)
require(
    "@Nonnull Config config" in permission_util,
    "PermissionUtil must retain the RC-37 Config overload for ticker compatibility",
)

for path, expected_call in {
    "src/main/java/io/taraxacum/finaltech/core/item/usable/MenuViewer.java":
        "LegacyBlockDataCompat.getMenu(location)",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/RandomAccessorMenu.java":
        "LegacyBlockDataCompat.getMenu(targetBlock.getLocation())",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/RemoteAccessorMenu.java":
        "LegacyBlockDataCompat.getMenu(targetBlock.getLocation())",
    "src/main/java/io/taraxacum/finaltech/core/menu/limit/lock/AbstractLockMachineMenu.java":
        "MachineRecipeLock.HELPER.getOrDefaultValue(location)",
    "src/main/java/io/taraxacum/finaltech/core/menu/manual/EquivalentExchangeTableMenu.java":
        'LegacyBlockDataCompat.getValue(location, "value")',
    "src/main/java/io/taraxacum/finaltech/core/patch/EnergyRegulatorDetailMenu.java":
        "LegacyBlockDataCompat.getMenu(componentLocation)",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/AbstractElectricCapacitor.java":
        "LegacyBlockDataCompat.getMenu(block.getLocation())",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/logic/AbstractLogicComparator.java":
        "LegacyBlockDataCompat.getMenu(block.getLocation())",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/CardOperationTable.java":
        "LegacyBlockDataCompat.getMenu(block.getLocation())",
}.items():
    source = read(path)
    require(expected_call in source, f"{path} lost its validated Part 2 storage boundary")

for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/AbstractElectricCapacitor.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/logic/AbstractLogicComparator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/CardOperationTable.java",
):
    require(
        "@Nonnull Config config" in read(path),
        f"{path} must retain the RC-37 ticker Config signature",
    )

for path, expected_call in {
    "src/main/java/io/taraxacum/finaltech/core/listener/ConfigSaveListener.java":
        "LegacyBlockDataCompat.getMenu(configSaveActionEvent.getLocation())",
    "src/main/java/io/taraxacum/finaltech/core/listener/ExpandedElectricCapacitorEnergyListener.java":
        'LegacyBlockDataCompat.getValue(locationInfo.getLocation(), "s")',
    "src/main/java/io/taraxacum/finaltech/core/menu/cargo/AdvancedAutoCraftMenu.java":
        "LegacyBlockDataCompat.getMenu(location)",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/LocationRecorder.java":
        "LegacyBlockDataCompat.getMenu(location)",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/PortableEnergyStorage.java":
        "LegacyBlockDataCompat.getMenu(location)",
}.items():
    source = read(path)
    require(expected_call in source, f"{path} lost its validated Part 2 storage boundary")

for path in (
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/ConfigurableRemoteAccessorMenu.java",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/ConsumableRemoteAccessorMenu.java",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/ExpandedConfigurableRemoteAccessorMenu.java",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/ExpandedConsumableRemoteAccessorMenu.java",
):
    source = read(path)
    require(
        source.count("LegacyBlockDataCompat.getMenu(targetBlock.getLocation())") == 2,
        f"{path} must route both remote-access menu lookups through the compatibility boundary",
    )
    require(
        "BlockStorage.hasInventory" not in source,
        f"{path} must not retain the deprecated inventory-existence probe",
    )

for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLocationTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LocationTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/MatrixCraftingTable.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/ConfigurationCopier.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/ConfigurationPaster.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/NormalConfigurableElectricityShootPile.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/NormalConsumableElectricityShootPile.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/MatrixGenerator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/line/pile/AbstractElectricityShootPile.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/EnergizedOperationAccelerator.java",
):
    source = read(path)
    require(
        "LegacyBlockDataCompat.getMenu(block.getLocation())" in source,
        f"{path} must route its ticker menu lookup through the compatibility boundary",
    )
    require(
        "@Nonnull Config config" in source,
        f"{path} must retain the RC-37 ticker Config signature",
    )

for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/OperationAccelerator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/OverloadedOperationAccelerator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/conversion/AbstractConversionMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/extraction/AbstractExtractionMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/generator/AbstractGeneratorMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/CureTower.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/PurifyLevelTower.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/PurifyTimeTower.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/DistributeLeftStorageUnit.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/DistributeRightStorageUnit.java",
):
    source = read(path)
    require(
        "LegacyBlockDataCompat.getMenu(" in source,
        f"{path} must route its ticker menu lookup through the compatibility boundary",
    )
    require(
        "@Nonnull Config config" in source,
        f"{path} must retain the RC-37 ticker Config signature",
    )

for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/DividedStackStorageUnit.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/LimitedStackStorageUnit.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/unit/StackStorageUnit.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/BasicGenerator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/AbstractCubeElectricGenerator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/EnergizedChargeBase.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/OverloadedChargeBase.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/ConsumableSimulateClickMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/SimulateClickMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/DustFactoryDirt.java",
):
    source = read(path)
    require(
        "LegacyBlockDataCompat.getMenu(" in source,
        f"{path} must route its menu lookup through the compatibility boundary",
    )
    require(
        "@Nonnull Config config" in source,
        f"{path} must retain the RC-37 ticker Config signature",
    )

for path, expected_count in {
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/MatrixAccelerator.java": 1,
    "src/main/java/io/taraxacum/finaltech/core/patch/EnergyRegulatorBlockTicker.java": 1,
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/EtherMiner.java": 2,
}.items():
    source = read(path)
    require(
        source.count("LegacyBlockDataCompat.getMenu(") == expected_count,
        f"{path} lost one or more validated menu compatibility lookups",
    )
    require(
        "Config config" in source or "Config data" in source,
        f"{path} must retain its RC-37 Config ticker signature",
    )

matrix_expanded = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/MatrixExpandedCapacitor.java"
)
equivalent_exchange = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/EquivalentExchangeTable.java"
)
item_dismantle = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/ItemDismantleTable.java"
)
item_dismantle_menu = read(
    "src/main/java/io/taraxacum/finaltech/core/menu/machine/ItemDismantleTableMenu.java"
)

require(
    "LegacyBlockDataCompat.getMenu(location)" in matrix_expanded
    and "LegacyBlockDataCompat.setValue(location, this.key" in matrix_expanded,
    "MatrixExpandedCapacitor must preserve its existing stack-key storage through the compatibility boundary",
)
require(
    'private final String key = "value";' in equivalent_exchange,
    "EquivalentExchangeTable persisted key must remain value",
)
require(
    "LegacyBlockDataCompat.getValue(block.getLocation(), this.key)" in equivalent_exchange
    and "LegacyBlockDataCompat.setValue(block.getLocation(), this.key, value)" in equivalent_exchange,
    "EquivalentExchangeTable must preserve its value read/write semantics",
)
require(
    'private final String key = "c";' in item_dismantle,
    "ItemDismantleTable persisted key must remain c",
)
require(
    "LegacyBlockDataCompat.getValue(block.getLocation(), key)" in item_dismantle
    and "LegacyBlockDataCompat.setValue(block.getLocation(), key, StringNumberUtil.add(count))" in item_dismantle,
    "ItemDismantleTable must preserve its dismantle-count read/write semantics",
)
require(
    "LegacyBlockDataCompat.getValue(block.getLocation(), FinalTechItems.ITEM_DISMANTLE_TABLE.getKey())" in item_dismantle_menu
    and "LegacyBlockDataCompat.setValue(" in item_dismantle_menu
    and "LegacyBlockDataCompat.getValue(location, FinalTechItems.ITEM_DISMANTLE_TABLE.getKey())" in item_dismantle_menu,
    "ItemDismantleTableMenu must preserve the same dismantle counter key for click and display paths",
)
require(
    "Configuration.Config" not in item_dismantle_menu
    and "BlockStorage." not in item_dismantle_menu,
    "ItemDismantleTableMenu must not reintroduce deprecated Config or BlockStorage access",
)
for source, name in (
    (matrix_expanded, "MatrixExpandedCapacitor"),
    (equivalent_exchange, "EquivalentExchangeTable"),
    (item_dismantle, "ItemDismantleTable"),
):
    require(
        "@Nonnull Config config" in source,
        f"{name} must retain the RC-37 ticker Config signature",
    )

expanded_capacitor = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/AbstractExpandedElectricCapacitor.java"
)
advanced_machine = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/advanced/AbstractAdvanceMachine.java"
)
basic_machine = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/template/basic/AbstractBasicMachine.java"
)

require(
    'protected final String key = "s";' in expanded_capacitor,
    "AbstractExpandedElectricCapacitor persisted stack key must remain s",
)
for expected in (
    "LegacyBlockDataCompat.setValue(blockPlaceEvent.getBlock().getLocation(), AbstractExpandedElectricCapacitor.this.key, StringNumberUtil.ZERO)",
    "LegacyBlockDataCompat.getMenu(block.getLocation())",
    "LegacyBlockDataCompat.setValue(location, this.key, String.valueOf(stack))",
):
    require(expected in expanded_capacitor, f"AbstractExpandedElectricCapacitor lost storage behavior: {expected}")
require(
    "@Nonnull Config config" in expanded_capacitor,
    "AbstractExpandedElectricCapacitor must retain the RC-37 ticker Config signature",
)

for source, name in (
    (advanced_machine, "AbstractAdvanceMachine"),
    (basic_machine, "AbstractBasicMachine"),
):
    require(
        "LegacyBlockDataCompat.getMenu(block.getLocation())" in source,
        f"{name} must use the compatibility menu boundary",
    )
    require(
        "LegacyBlockDataCompat.setValue(blockMenu.getLocation(), MachineRecipeLock.KEY, String.valueOf(craft.getOffset()))" in source,
        f"{name} must preserve recipe-lock persisted writes",
    )
    require(
        "LegacyBlockDataCompat.setValue(blockMenu.getLocation(), this.offsetKey, String.valueOf(craft.getOffset()))" in source,
        f"{name} must preserve recipe-offset writes",
    )
    require(
        "LegacyBlockDataCompat.setValue(blockMenu.getLocation(), this.offsetKey, null)" in source,
        f"{name} must preserve recipe-offset clearing",
    )
    require(
        "@Nonnull Config config" in source,
        f"{name} must retain the RC-37 ticker Config signature",
    )

for marker in (
    'getMethod("getDatabaseManager")',
    'getMethod("getBlockDataController")',
    'getMethod("getBlockData", Location.class)',
    'getMethod("loadBlockData", blockDataType)',
    'getMethod("getBlockMenu")',
    'getMethod("getSfId")',
    "LegacyAccess",
    '@SuppressWarnings("deprecation")',
):
    require(marker in compat, f"storage compatibility boundary is missing: {marker}")

print("FinalTECH Part 2 storage boundaries: PASS")
