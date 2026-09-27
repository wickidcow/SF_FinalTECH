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
