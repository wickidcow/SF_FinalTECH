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
compat = read(
    "src/main/java/io/taraxacum/libs/slimefun/compat/LegacyBlockDataCompat.java"
)

require('KEY = "mms"' in machine_max_stack, "MachineMaxStack key must remain mms")
require('KEY = "rl"' in machine_recipe_lock, "MachineRecipeLock key must remain rl")
require(
    "LegacyBlockDataCompat.getMenu(location)" in frame_menu,
    "AdvancedAutoCraftFrameMenu must use the block-data menu boundary",
)

for marker in (
    'getMethod("getDatabaseManager")',
    'getMethod("getBlockDataController")',
    'getMethod("getBlockData", Location.class)',
    'getMethod("loadBlockData", blockDataType)',
    'getMethod("getBlockMenu")',
    "LegacyAccess",
    '@SuppressWarnings("deprecation")',
):
    require(marker in compat, f"storage compatibility boundary is missing: {marker}")

print("FinalTECH Part 2 storage boundaries: PASS")
