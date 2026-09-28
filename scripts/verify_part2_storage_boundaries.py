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
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/EnergizedAccelerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/OverloadedAccelerator.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/craft/AbstractManualCraftMachine.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/AbstractMachineChargeCard.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/manual/ManualCraftMachineMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/helper/PositionInfo.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/ItemSerializationConstructor.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/MatrixItemSerializationConstructor.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/libs/slimefun/dto/LocationInfo.java":
        "LegacyTickerDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/AbstractMachineAccelerateCard.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/AbstractMachineActivateCard.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/AdvancedAutoCraft.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/storage/StorageInteractPort.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/util/BlockTickerUtil.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/AbstractMachineMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireCapacitor.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireResistance.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/EntropySeed.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/util/ItemConfigurationUtil.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/util/CargoUtil.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/AreaAccessorMenu.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/PointTransfer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedPointTransfer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LineTransfer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLineTransfer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/MeshTransfer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedMeshTransfer.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/EquivalentConcept.java":
        "LegacyBlockDataCompat",
    "src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java":
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
        "@Nonnull Object config" in read(path),
        f"{path} must use the opaque internal ticker-data signature",
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
        "@Nonnull Object config" in source,
        f"{path} must use the opaque internal ticker-data signature",
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
        "@Nonnull Object config" in source,
        f"{path} must use the opaque internal ticker-data signature",
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
        "@Nonnull Object config" in source,
        f"{path} must use the opaque internal ticker-data signature",
    )

for path, expected_count in {
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/MatrixAccelerator.java": 1,
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/EtherMiner.java": 2,
}.items():
    source = read(path)
    require(
        source.count("LegacyBlockDataCompat.getMenu(") == expected_count,
        f"{path} lost one or more validated menu compatibility lookups",
    )
    require(
        "Object config" in source or "Object data" in source,
        f"{path} must use its opaque internal ticker-data signature",
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
        "@Nonnull Object config" in source,
        f"{name} must use the opaque internal ticker-data signature",
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
    "@Nonnull Object config" in expanded_capacitor,
    "AbstractExpandedElectricCapacitor must use the opaque internal ticker-data signature",
)
require(
    "public int getStack(@Nonnull Config config)" in expanded_capacitor
    and "public int getStack(@Nonnull Object data)" in expanded_capacitor,
    "AbstractExpandedElectricCapacitor must retain the RC-37 getStack overload while using Object internally",
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
        "@Nonnull Object config" in source,
        f"{name} must use the opaque internal ticker-data signature",
    )

energized_accelerator = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/EnergizedAccelerator.java"
)
overloaded_accelerator = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/OverloadedAccelerator.java"
)
manual_craft = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/craft/AbstractManualCraftMachine.java"
)
charge_card = read(
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/AbstractMachineChargeCard.java"
)
manual_craft_menu = read(
    "src/main/java/io/taraxacum/finaltech/core/menu/manual/ManualCraftMachineMenu.java"
)

for source, name in (
    (energized_accelerator, "EnergizedAccelerator"),
    (overloaded_accelerator, "OverloadedAccelerator"),
):
    require(
        "LegacyBlockDataCompat.getMenu(" in source,
        f"{name} must route its own menu lookup through the compatibility boundary",
    )
    require(
        "LegacyBlockDataCompat.getSlimefunId(locationInfo.getLocation())" in source,
        f"{name} must verify accelerated machine identity through the dedicated Slimefun-id boundary",
    )
    require(
        "@Nonnull Object config" in source,
        f"{name} must use the opaque internal ticker-data signature",
    )

require(
    'LegacyBlockDataCompat.setValue(blockPlaceEvent.getBlock().getLocation(), ManualCraftMachineMenu.KEY, "0")' in manual_craft,
    "AbstractManualCraftMachine must preserve its existing manual-craft state key on placement",
)
require(
    manual_craft.count("LegacyBlockDataCompat.getSlimefunId(location)") == 2,
    "AbstractManualCraftMachine must resolve both energy identity paths through the dedicated Slimefun-id boundary",
)
require(
    "LegacyBlockDataCompat.getMenu(block.getLocation())" in manual_craft,
    "AbstractManualCraftMachine must use the compatibility menu boundary",
)
require(
    "@Nonnull Object config" in manual_craft,
    "AbstractManualCraftMachine must use the opaque internal ticker-data signature",
)

require(
    "String slimefunId = LegacyBlockDataCompat.getSlimefunId(location);" in charge_card
    and "SlimefunItem slimefunItem = SlimefunItem.getById(slimefunId);" in charge_card,
    "AbstractMachineChargeCard must use the dedicated Slimefun-id boundary once per activation",
)
require(
    "ConstantTableUtil.CONFIG_ID" not in charge_card,
    "AbstractMachineChargeCard must not treat the special Slimefun id as ordinary block data",
)

require(
    "LegacyBlockDataCompat.setValue(l, key, value)" in manual_craft_menu
    and "LegacyBlockDataCompat.getValue(l, key)" in manual_craft_menu,
    "ManualCraftMachineMenu must route generic recipe-state access through the compatibility boundary",
)
require(
    "Configuration.Config" not in manual_craft_menu
    and "BlockStorage." not in manual_craft_menu,
    "ManualCraftMachineMenu must not retain deprecated full Config or BlockStorage access",
)
require(
    "add(l, KEY, get(l, KEY_L[finalSlotP]))" in manual_craft_menu,
    "ManualCraftMachineMenu must preserve left-slot recipe selection semantics",
)

position_info = read(
    "src/main/java/io/taraxacum/finaltech/core/helper/PositionInfo.java"
)
require(
    position_info.count("LegacyBlockDataCompat.getValue(") == 6
    and position_info.count("LegacyBlockDataCompat.setValue(") == 1,
    "PositionInfo must preserve all cargo position-map reads and its serialized write through the compatibility boundary",
)
require(
    "BlockStorageHelper.ID_CARGO" in position_info,
    "PositionInfo must retain the existing cargo helper id and serialized cargo state contract",
)
require(
    "BlockStorage." not in position_info,
    "PositionInfo must not reintroduce direct deprecated BlockStorage access",
)

serialization_constructor = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/ItemSerializationConstructor.java"
)
matrix_serialization_constructor = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/MatrixItemSerializationConstructor.java"
)

for source, name in (
    (serialization_constructor, "ItemSerializationConstructor"),
    (matrix_serialization_constructor, "MatrixItemSerializationConstructor"),
):
    require(
        'blockStorageItemKey = "item"' in source
        and 'blockStorageAmountKey = "amount"' in source,
        f"{name} must retain the existing item/amount persistence keys",
    )
    require(
        source.count("LegacyBlockDataCompat.getMenu(") == 2,
        f"{name} must route break/tick menu access through the compatibility boundary",
    )
    require(
        source.count("LegacyBlockDataCompat.setValue(") == 4,
        f"{name} must preserve item/amount clear and save writes through the compatibility boundary",
    )
    require(
        "LegacyBlockDataCompat.setValue(location, this.blockStorageItemKey, null)" in source
        and "LegacyBlockDataCompat.setValue(location, this.blockStorageAmountKey, null)" in source,
        f"{name} must preserve operation-state clearing",
    )
    require(
        "@Nonnull Object config" in source,
        f"{name} must use the opaque internal ticker-data signature",
    )
    require(
        "BlockStorage." not in source,
        f"{name} must not reintroduce direct deprecated BlockStorage access",
    )

legacy_ticker_data = read(
    "src/main/java/io/taraxacum/libs/slimefun/compat/LegacyTickerDataCompat.java"
)
location_info = read(
    "src/main/java/io/taraxacum/libs/slimefun/dto/LocationInfo.java"
)
accelerate_card = read(
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/AbstractMachineAccelerateCard.java"
)
activate_card = read(
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/AbstractMachineActivateCard.java"
)

require(
    "private static final ClassValue<DataAccess> ACCESS" in legacy_ticker_data
    and "LegacyBlockDataCompat.getModernDataContainer(location)" in legacy_ticker_data
    and "LegacyBlockDataCompat.getLegacyDataView(location)" in legacy_ticker_data
    and 'method(type, "getData", String.class)' in legacy_ticker_data
    and 'method(type, "setData", String.class, String.class)' in legacy_ticker_data
    and 'method(type, "removeData", String.class)' in legacy_ticker_data
    and 'method(type, "getAllData")' in legacy_ticker_data,
    "LegacyTickerDataCompat must use cached modern-container access with an opaque RC-37 fallback",
)
require(
    "me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config" not in legacy_ticker_data
    and "me.mrCookieSlime.Slimefun.api.BlockStorage" not in legacy_ticker_data,
    "LegacyTickerDataCompat must not compile against deprecated Config or BlockStorage APIs",
)

require(
    location_info.count("LegacyTickerDataCompat.getData(location)") == 2
    and location_info.count("LegacyBlockDataCompat.getSlimefunId(location)") == 2,
    "LocationInfo must route opaque data and identity retrieval through the compatibility boundaries",
)
require(
    "private Object data;" in location_info
    and "public Object getData()" in location_info,
    "LocationInfo must store and expose opaque ticker data for internal callers",
)
require(
    "public Config getConfig()" in location_info
    and "LegacyBlockDataCompat.getLegacyDataView(location)" in location_info,
    "LocationInfo must retain only its explicit historical Config accessor",
)
require(
    "BlockStorage." not in location_info,
    "LocationInfo must not reintroduce direct deprecated BlockStorage access",
)

for source, name, data_calls in (
    (accelerate_card, "AbstractMachineAccelerateCard", 1),
    (activate_card, "AbstractMachineActivateCard", 2),
):
    require(
        "String slimefunId = LegacyBlockDataCompat.getSlimefunId(location);" in source
        and "SlimefunItem slimefunItem = SlimefunItem.getById(slimefunId);" in source,
        f"{name} must resolve machine identity once through the dedicated Slimefun-id boundary",
    )
    require(
        "BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(location);" in source,
        f"{name} must route menu permission checks through the compatibility boundary",
    )
    require(
        source.count("LegacyTickerDataCompat.getData(location)") == data_calls,
        f"{name} must route internal ticker data through the opaque LegacyTickerDataCompat boundary",
    )
    require(
        "BlockStorage." not in source
        and "ConstantTableUtil.CONFIG_ID" not in source,
        f"{name} must not reintroduce direct deprecated storage identity access",
    )

advanced_auto_craft = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/AdvancedAutoCraft.java"
)
storage_interact_port = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/storage/StorageInteractPort.java"
)

require(
    advanced_auto_craft.count("LegacyBlockDataCompat.getSlimefunId(containerLocation)") == 1,
    "AdvancedAutoCraft must resolve the single adjacent container identity through the compatibility boundary",
)
require(
    advanced_auto_craft.count("LegacyBlockDataCompat.getMenu(") == 3,
    "AdvancedAutoCraft must preserve own-menu, preflight container-menu, and runnable container-menu lookups",
)
require(
    "containerId == null || LegacyBlockDataCompat.getMenu(containerLocation) == null" in advanced_auto_craft,
    "AdvancedAutoCraft must retain its preflight container existence/menu guard",
)
require(
    "BlockMenu containerMenu = LegacyBlockDataCompat.getMenu(containerLocation);" in advanced_auto_craft
    and "if (containerMenu == null)" in advanced_auto_craft,
    "AdvancedAutoCraft must re-resolve the adjacent menu before crafting",
)
require(
    "@Nonnull Object config" in advanced_auto_craft,
    "AdvancedAutoCraft must use the opaque internal ticker-data signature",
)

require(
    storage_interact_port.count("LegacyBlockDataCompat.getMenu(") == 2,
    "StorageInteractPort must route its own and adjacent-block menu lookups through the compatibility boundary",
)
require(
    "BlockMenu targetBlockMenu = LegacyBlockDataCompat.getMenu(targetBlock.getLocation());" in storage_interact_port
    and "if (targetBlockMenu == null)" in storage_interact_port,
    "StorageInteractPort must preserve vanilla-inventory fallback only when the adjacent block has no Slimefun menu",
)
require(
    "Object config" in storage_interact_port,
    "StorageInteractPort must use the opaque internal ticker-data signature",
)

legacy_block_data = read(
    "src/main/java/io/taraxacum/libs/slimefun/compat/LegacyBlockDataCompat.java"
)
legacy_slimefun_api = read(
    "src/main/java/io/taraxacum/libs/slimefun/compat/LegacySlimefunApiCompat.java"
)
block_ticker_util = read(
    "src/main/java/io/taraxacum/finaltech/util/BlockTickerUtil.java"
)
require(
    "public static void removeBlock(@Nonnull Location location)" in legacy_block_data
    and 'controllerType.getMethod("removeBlock", Location.class)' in legacy_block_data
    and 'method("clearBlockInfo", Location.class)' in legacy_block_data,
    "LegacyBlockDataCompat must preserve modern-controller removal with a reflective RC-37 BlockStorage fallback",
)
require(
    "public static Object getModernDataContainer(@Nonnull Location location)" in legacy_block_data
    and "return MODERN.getDataContainer(location);" in legacy_block_data,
    "LegacyBlockDataCompat must expose current storage containers opaquely without raising the RC-37 type floor",
)
require(
    'Class.forName("me.mrCookieSlime.Slimefun.api.BlockStorage")' in legacy_block_data
    and "me.mrCookieSlime.Slimefun.api.BlockStorage." not in legacy_block_data,
    "LegacyBlockDataCompat must resolve historical BlockStorage calls reflectively",
)

for expected in (
    "public static int getResearchLevelCost(",
    "public static void setResearchLevelCost(",
    "public static int getCharge(",
    "public static int getGeneratedOutput(",
    "public static boolean willExplode(",
    "public static void setCharge(",
    'tryInvoke(research, "getLevelCost")',
    'tryInvoke(research, "setLevelCost", cost)',
    'tryInvoke(component, "getChargeLong", location)',
    "LegacyBlockDataCompat.getModernDataContainer(location)",
):
    require(
        expected in legacy_slimefun_api,
        f"LegacySlimefunApiCompat lost modern-first compatibility behavior: {expected}",
    )
require(
    'invokeRequired(research, "getCost")' in legacy_slimefun_api
    and 'invokeRequired(research, "setCost", cost)' in legacy_slimefun_api
    and 'invokeRequired(component, "getCharge", location)' in legacy_slimefun_api,
    "LegacySlimefunApiCompat must retain reflective RC-37 method-name fallbacks",
)
require(
    "me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config" not in legacy_slimefun_api
    and '@SuppressWarnings("deprecation")' not in legacy_slimefun_api,
    "LegacySlimefunApiCompat must not compile against deprecated Research/Energy/Config APIs",
)

require(
    "LegacyBlockDataCompat.getSlimefunId(location)" in block_ticker_util
    and "LegacyBlockDataCompat.removeBlock(block.getLocation())" in block_ticker_util,
    "BlockTickerUtil must route identity/removal through the block-data compatibility boundary",
)
require(
    '@SuppressWarnings("deprecation")' in block_ticker_util,
    "BlockTickerUtil must document/suppress its intentionally retained RC-37 Config callback signatures",
)
require(
    "BlockStorage." not in block_ticker_util,
    "BlockTickerUtil must not directly use deprecated BlockStorage",
)

require(
    "public static void tickCompat(" in block_ticker_util
    and "blockTicker.tick(block, item, (Config) data);" in block_ticker_util,
    "BlockTickerUtil must isolate the RC-37 BlockTicker callback behind tickCompat",
)

abstract_machine_menu = read(
    "src/main/java/io/taraxacum/finaltech/core/menu/AbstractMachineMenu.java"
)
variable_wire_capacitor = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireCapacitor.java"
)
variable_wire_resistance = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireResistance.java"
)

require(
    "LegacyBlockDataCompat.setSlimefunId(location, this.slimefunItem.getId())" in abstract_machine_menu,
    "AbstractMachineMenu data-loss repair must recreate the Slimefun identity through the dedicated identity boundary",
)
require(
    "ConstantTableUtil.CONFIG_ID" not in abstract_machine_menu
    and "BlockStorage." not in abstract_machine_menu,
    "AbstractMachineMenu must not treat Slimefun id as ordinary persisted data",
)

for source, name, target_id in (
    (
        variable_wire_capacitor,
        "VariableWireCapacitor",
        "FinalTechItemStacks.VARIABLE_WIRE_RESISTANCE.getItemId()",
    ),
    (
        variable_wire_resistance,
        "VariableWireResistance",
        "FinalTechItemStacks.VARIABLE_WIRE_CAPACITOR.getItemId()",
    ),
):
    require(
        "LegacyBlockDataCompat.removeBlock(location)" in source,
        f"{name} must preserve its remove-before-recreate identity swap",
    )
    require(
        f"LegacyBlockDataCompat.setSlimefunId(location, {target_id})" in source,
        f"{name} must recreate the target Slimefun identity through the dedicated boundary",
    )
    require(
        f"{target_id}.equals(LegacyBlockDataCompat.getSlimefunId(location))" in source,
        f"{name} must verify the recreated identity through getSlimefunId",
    )
    require(
        "BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(location);" in source,
        f"{name} must route status-menu access through the compatibility boundary",
    )
    require(
        "@Nonnull Object config" in source,
        f"{name} must use the opaque internal ticker-data signature",
    )
    require(
        "ConstantTableUtil.CONFIG_ID" not in source
        and "BlockStorage." not in source,
        f"{name} must not reintroduce generic or direct special-id storage calls",
    )

entropy_seed = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/EntropySeed.java"
)
item_configuration = read(
    "src/main/java/io/taraxacum/finaltech/util/ItemConfigurationUtil.java"
)

require(
    'private final String key = "key";' in entropy_seed
    and 'private final String value = "value";' in entropy_seed,
    "EntropySeed trigger key/value must remain unchanged",
)
for expected in (
    "LegacyBlockDataCompat.setValue(location, EntropySeed.this.key, EntropySeed.this.value)",
    "LegacyBlockDataCompat.getValue(block.getLocation(), this.key)",
    "LegacyBlockDataCompat.setValue(location, this.key, null)",
    "LegacyBlockDataCompat.removeBlock(location)",
    "LegacyBlockDataCompat.setSlimefunId(location, FinalTechItemStacks.EQUIVALENT_CONCEPT.getItemId())",
    "LegacyBlockDataCompat.setValue(location, EquivalentConcept.KEY_LIFE, String.valueOf(EntropySeed.this.equivalentConceptLife))",
    "LegacyBlockDataCompat.setValue(location, EquivalentConcept.KEY_RANGE, String.valueOf(EntropySeed.this.equivalentConceptRange))",
    "LegacyBlockDataCompat.setSlimefunId(location, FinalTechItemStacks.JUSTIFIABILITY.getItemId())",
):
    require(expected in entropy_seed, f"EntropySeed lost identity/state behavior: {expected}")
require(
    "BlockStorage." not in entropy_seed
    and "ConstantTableUtil.CONFIG_ID" not in entropy_seed,
    "EntropySeed must not reintroduce direct/generic special-id storage access",
)

require(
    '"_FINALTECH_CONFIGURATION"' in item_configuration
    and '"_FINALTECH_BLOCK_STORAGE_ID"' in item_configuration,
    "ItemConfigurationUtil item PDC keys must remain unchanged",
)
require(
    "ConstantTableUtil.CONFIG_ID.equals(key)" in item_configuration
    and "LegacyBlockDataCompat.getSlimefunId(l)" in item_configuration
    and "LegacyBlockDataCompat.getValue(l, key)" in item_configuration,
    "ItemConfigurationUtil must distinguish Slimefun identity from ordinary values",
)
require(
    "LegacyTickerDataCompat.getKeys(LegacyTickerDataCompat.getData(location))" in item_configuration
    and "LegacyTickerDataCompat.getKeys(locationInfo.getData())" in item_configuration
    and "LegacyTickerDataCompat.getString(locationInfo.getData(), key)" in item_configuration
    and "LegacyTickerDataCompat.contains(locationInfo.getData(), entry.getKey())" in item_configuration,
    "ItemConfigurationUtil must enumerate/read/check opaque ticker data through LegacyTickerDataCompat",
)
require(
    ".getConfig()" not in item_configuration,
    "ItemConfigurationUtil must not call the historical LocationInfo Config accessor",
)
require(
    "LegacyBlockDataCompat.setValue(location, entry.getKey(), entry.getValue())" in item_configuration
    and "LegacyBlockDataCompat.setValue(locationInfo.getLocation(), entry.getKey(), entry.getValue())" in item_configuration,
    "ItemConfigurationUtil must preserve both configuration restore paths",
)
require(
    "BlockStorage." not in item_configuration,
    "ItemConfigurationUtil must not directly use deprecated BlockStorage",
)

cargo_util = read(
    "src/main/java/io/taraxacum/finaltech/util/CargoUtil.java"
)
area_accessor_menu = read(
    "src/main/java/io/taraxacum/finaltech/core/menu/clicker/AreaAccessorMenu.java"
)
point_transfer = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/PointTransfer.java"
)
advanced_point_transfer = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedPointTransfer.java"
)
line_transfer = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LineTransfer.java"
)
advanced_line_transfer = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLineTransfer.java"
)
mesh_transfer = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/MeshTransfer.java"
)
advanced_mesh_transfer = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedMeshTransfer.java"
)
equivalent_concept = read(
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/EquivalentConcept.java"
)
setup_util = read(
    "src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java"
)

for expected in (
    "public static boolean hasBlockData(@Nonnull Location location)",
    "public static boolean hasMenu(@Nonnull Location location)",
    "return MODERN.hasBlockData(location);",
    "return MODERN.hasMenu(location);",
    'method("hasBlockInfo", Location.class)',
    'method("hasInventory", Block.class)',
):
    require(
        expected in legacy_block_data,
        f"LegacyBlockDataCompat lost topology-existence compatibility behavior: {expected}",
    )
require(
    "return getLoadedData(location) != null;" in legacy_block_data,
    "LegacyBlockDataCompat.hasBlockData must preserve legacy data-loading existence semantics",
)
require(
    "Object menu = getMenu(location);" in legacy_block_data
    and "menu != NO_RECORD_OBJECT && menu != null" in legacy_block_data,
    "LegacyBlockDataCompat.hasMenu must distinguish absent records from menu-less Slimefun blocks",
)
require(
    "public static Map<Location, BlockMenu> getLegacyWorldInventories(@Nonnull World world)" in legacy_block_data
    and "LegacyAccess.getStorage(world)" in legacy_block_data
    and 'BLOCK_STORAGE.getDeclaredField("inventories")' in legacy_block_data
    and 'method("getStorage", World.class)' in legacy_block_data,
    "LegacyBlockDataCompat must isolate the RC-37 world-inventory registry behind reflection",
)

require(
    cargo_util.count("LegacyBlockDataCompat.hasMenu(") == 6
    and cargo_util.count("LegacyBlockDataCompat.getMenu(") == 1,
    "CargoUtil must route all Slimefun-menu existence/retrieval checks through the compatibility boundary",
)
require("BlockStorage." not in cargo_util, "CargoUtil must not directly use deprecated BlockStorage")

require(
    area_accessor_menu.count("LegacyBlockDataCompat.hasBlockData(") == 2
    and area_accessor_menu.count("LegacyBlockDataCompat.hasMenu(") == 2
    and area_accessor_menu.count("LegacyBlockDataCompat.getMenu(") == 1,
    "AreaAccessorMenu must preserve scan/click block-data and menu-existence semantics",
)
require("BlockStorage." not in area_accessor_menu, "AreaAccessorMenu must not directly use deprecated BlockStorage")

for source, name, has_menu_count, get_menu_count in (
    (point_transfer, "PointTransfer", 3, 1),
    (advanced_point_transfer, "AdvancedPointTransfer", 3, 1),
    (line_transfer, "LineTransfer", 7, 2),
    (advanced_line_transfer, "AdvancedLineTransfer", 7, 2),
    (mesh_transfer, "MeshTransfer", 5, 2),
    (advanced_mesh_transfer, "AdvancedMeshTransfer", 5, 2),
):
    require(
        source.count("LegacyBlockDataCompat.hasBlockData(location)") == 1,
        f"{name} must preserve its block-data existence gate",
    )
    require(
        source.count("LegacyBlockDataCompat.hasMenu(") == has_menu_count,
        f"{name} lost one or more menu-existence topology checks",
    )
    require(
        source.count("LegacyBlockDataCompat.getMenu(") == get_menu_count,
        f"{name} lost one or more validated menu retrievals",
    )
    require(
        "LegacyBlockDataCompat.setValue(location, ConstantTableUtil.CONFIG_UUID, blockPlaceEvent.getPlayer().getUniqueId().toString())" in source,
        f"{name} must preserve owner UUID persistence",
    )
    require(
        "@Nonnull Object config" in source,
        f"{name} must use the opaque internal ticker-data signature",
    )

for source, name in (
    (mesh_transfer, "MeshTransfer"),
    (advanced_mesh_transfer, "AdvancedMeshTransfer"),
):
    require(
        'LegacyBlockDataCompat.setValue(block.getLocation(), PositionInfo.KEY, "")' in source,
        f"{name} must preserve empty mesh-position state initialization",
    )

require(
    'public static final String KEY_LIFE = "l";' in equivalent_concept
    and 'public static final String KEY_RANGE = "r";' in equivalent_concept,
    "EquivalentConcept life/range persistence keys must remain unchanged",
)
for expected in (
    "LegacyBlockDataCompat.hasBlockData(location)",
    "LegacyBlockDataCompat.getSlimefunId(location) == null",
    "LegacyBlockDataCompat.setSlimefunId(location, FinalTechItemStacks.JUSTIFIABILITY.getItemId())",
    "LegacyBlockDataCompat.setSlimefunId(location, EquivalentConcept.this.getId())",
    "LegacyTickerDataCompat.getData(location)",
    "LegacyBlockDataCompat.setValue(location, KEY_LIFE, String.valueOf(finalLife * attenuationRate))",
    "LegacyBlockDataCompat.setValue(location, KEY_RANGE, String.valueOf(range + 1))",
):
    require(expected in equivalent_concept, f"EquivalentConcept lost state/topology behavior: {expected}")
require(
    equivalent_concept.count("LegacyBlockDataCompat.removeBlock(") == 2,
    "EquivalentConcept must preserve both full block-data removal transitions",
)
require(
    "@Nonnull Object config" in equivalent_concept,
    "EquivalentConcept must use the opaque internal ticker-data signature",
)
require("BlockStorage." not in equivalent_concept, "EquivalentConcept must not directly use deprecated BlockStorage")

require(
    "LegacyBlockDataCompat.getLegacyWorldInventories(world)" in setup_util,
    "SetupUtil must route RC-37 world-inventory recovery through the compatibility boundary",
)
require(
    "LegacyBlockDataCompat.setSlimefunId(location, id)" in setup_util
    and "LegacyBlockDataCompat.setValue(location, configEntry.getKey(), configEntry.getValue())" in setup_util,
    "SetupUtil data-loss repair must preserve identity and custom state restoration",
)
require(
    "ReflectionUtil" not in setup_util
    and "BlockStorage." not in setup_util,
    "SetupUtil must not directly access the deprecated BlockStorage registry",
)

for marker in (
    'getMethod("getDatabaseManager")',
    'getMethod("getBlockDataController")',
    'getMethod("getBlockData", Location.class)',
    'getMethod("loadBlockData", blockDataType)',
    'getMethod("getBlockMenu")',
    'getMethod("getSfId")',
    "LegacyAccess",
    'Class.forName("me.mrCookieSlime.Slimefun.api.BlockStorage")',
):
    require(marker in compat, f"storage compatibility boundary is missing: {marker}")

finaltech_changed = read(
    "src/main/java/io/taraxacum/finaltech/FinalTechChanged.java"
)
require(
    "public static void flushLegacyStorage()" in legacy_block_data
    and 'Class.forName("me.mrCookieSlime.Slimefun.api.BlockStorage")' in legacy_block_data,
    "LegacyBlockDataCompat must isolate the optional RC-37 shutdown storage flush hooks",
)
require(
    "LegacyBlockDataCompat.flushLegacyStorage()" in finaltech_changed,
    "FinalTechChanged must delegate legacy shutdown persistence to the compatibility boundary",
)
require(
    "me.mrCookieSlime.Slimefun.api.BlockStorage" not in finaltech_changed
    and "BlockStorage." not in finaltech_changed,
    "FinalTechChanged must not directly reference legacy BlockStorage",
)

# Global regression guards for the completed Part 2 cleanup.
java_root = ROOT / "src/main/java"
direct_storage_allowlist = {
    "io/taraxacum/libs/slimefun/compat/LegacyBlockDataCompat.java",
}

config_machine_allowlist = {
    "io/taraxacum/finaltech/core/item/machine/AbstractMachine.java",
    "io/taraxacum/finaltech/core/item/machine/AbstractEnergyProviderMachine.java",
    "io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/AbstractExpandedElectricCapacitor.java",
}

global_violations = []

for java_path in java_root.rglob("*.java"):
    relative = java_path.relative_to(java_root).as_posix()
    source = java_path.read_text(encoding="utf-8")

    if relative not in direct_storage_allowlist and (
        "me.mrCookieSlime.Slimefun.api.BlockStorage" in source
        or "BlockStorage." in source
    ):
        global_violations.append(
            f"{relative} reintroduced direct deprecated BlockStorage access"
        )

    if (
        relative.startswith("io/taraxacum/finaltech/core/item/machine/")
        and relative not in config_machine_allowlist
        and "import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;" in source
    ):
        global_violations.append(
            f"{relative} reintroduced Config into the internal machine ticker contract"
        )

    if (
        "import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;" in source
        and '@SuppressWarnings("deprecation")' not in source
    ):
        global_violations.append(
            f"{relative} uses the deprecated RC-37 Config type without an explicit compatibility suppression"
        )

    if (
        relative != "io/taraxacum/libs/slimefun/dto/LocationInfo.java"
        and ".getConfig()" in source
        and "LocationInfo" in source
    ):
        global_violations.append(
            f"{relative} calls the historical LocationInfo.getConfig() compatibility accessor"
        )

    if (
        "me.mrCookieSlime.CSCoreLibPlugin.general.Inventory." in source
        and ("ChestMenu" in source or "ClickAction" in source)
        and '@SuppressWarnings("deprecation")' not in source
    ):
        global_violations.append(
            f"{relative} uses the supported legacy menu contract without an explicit compatibility suppression"
        )

require(
    not global_violations,
    "Part 2 global compatibility violations:\n- " + "\n- ".join(global_violations),
)

print("FinalTECH Part 2 storage boundaries: PASS")
