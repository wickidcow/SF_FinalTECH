#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

MIGRATED = [
    "src/main/java/io/taraxacum/libs/slimefun/dto/BlockStorageHelper.java",
    "src/main/java/io/taraxacum/libs/slimefun/dto/BlockStorageIconHelper.java",
    "src/main/java/io/taraxacum/libs/slimefun/dto/BlockStorageLoreHelper.java",
    "src/main/java/io/taraxacum/finaltech/core/helper/MachineMaxStack.java",
    "src/main/java/io/taraxacum/finaltech/core/helper/MachineRecipeLock.java",
    "src/main/java/io/taraxacum/finaltech/core/helper/PositionInfo.java",
    "src/main/java/io/taraxacum/finaltech/core/helper/SlotSearchLine.java",
    "src/main/java/io/taraxacum/finaltech/core/menu/AbstractMachineMenu.java",
]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

for path in MIGRATED:
    text = read(path)
    require(
        "me.mrCookieSlime.Slimefun.api.BlockStorage" not in text
        and "BlockStorage." not in text,
        f"{path} must not directly use deprecated BlockStorage",
    )
    require(
        "FinalTechBlockStorage" in text,
        f"{path} must route storage access through FinalTechBlockStorage",
    )

compat = read(
    "src/main/java/io/taraxacum/libs/slimefun/compat/FinalTechBlockStorage.java"
)
for marker in (
    'getMethod("getDatabaseManager")',
    'getMethod("getBlockDataController")',
    'getMethod("getBlockData", Location.class)',
    'getMethod("loadBlockData", blockDataType)',
    '"id".equals(key)',
    'getMethod("getBlockMenu")',
    'LegacyAccessor',
    '@SuppressWarnings("deprecation")',
):
    require(marker in compat, f"storage compatibility facade is missing: {marker}")

require(
    "removeData.invoke(blockData, key)" in compat
    and "setData.invoke(blockData, key, value)" in compat,
    "modern storage writes must preserve null-as-remove and string value semantics",
)

print("FinalTECH storage compatibility boundary: PASS")
