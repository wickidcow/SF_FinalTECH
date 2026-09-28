#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

MODERN_TARGETS = [
    "src/main/java/io/taraxacum/finaltech/core/helper/MachineMaxStack.java",
    "src/main/java/io/taraxacum/finaltech/core/helper/PositionInfo.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/DustFactoryDirt.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/AbstractCubeElectricGenerator.java",
    "src/main/java/io/taraxacum/finaltech/core/menu/machine/ItemDismantleTableMenu.java",
    "src/main/java/io/taraxacum/finaltech/core/menu/manual/CardOperationPortMenu.java",
]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

for target in MODERN_TARGETS:
    text = read(target)
    require(
        "LegacyItemStackCompat.withType(" in text,
        f"{target} must use replacement-stack material updates on active inventory paths",
    )
    for direct in ("itemStack.setType(", "item.setType(", "iconItem.setType("):
        require(direct not in text, f"{target} still uses deprecated ItemStack#setType directly: {direct}")

base = read("src/main/java/io/taraxacum/libs/slimefun/dto/BlockStorageLoreHelper.java")
require(
    "public ItemStack getUpdatedIcon(" in base
    and "inventory.setItem(slot, this.getUpdatedIcon(item, value))" in base,
    "BlockStorageLoreHelper must support replacement-stack icon updates",
)

machine_max = read("src/main/java/io/taraxacum/finaltech/core/helper/MachineMaxStack.java")
require(
    "inventory.setItem(slot, MachineMaxStack.HELPER.getUpdatedIcon(" in machine_max,
    "MachineMaxStack click updates must reinsert the replacement icon stack",
)

position = read("src/main/java/io/taraxacum/finaltech/core/helper/PositionInfo.java")
require(
    "inventory.setItem(slot, BlockStorageLoreMaterialHelper.this.getUpdatedIcon(item, value))" in position,
    "PositionInfo menu updates must reinsert replacement icon stacks",
)

card = read("src/main/java/io/taraxacum/finaltech/core/menu/manual/CardOperationPortMenu.java")
require(
    "inventory.setItem(CRAFT_SLOT, craft.getUpdatedIcon(iconItem))" in card,
    "CardOperationPortMenu must replace its craft icon with the modernized stack",
)

compat = read("src/main/java/io/taraxacum/libs/plugin/util/LegacyItemStackCompat.java")
for marker in (
    "public static ItemStack withType(",
    'findMethod("withType")',
    'findMethod("setType")',
    "ItemStack replacement = item.clone()",
):
    require(marker in compat, f"LegacyItemStackCompat is missing: {marker}")

require(
    ".setType(" not in compat,
    "LegacyItemStackCompat must not directly link to deprecated ItemStack#setType",
)
require(
    '@SuppressWarnings("deprecation")' not in compat,
    "LegacyItemStackCompat must not require deprecation suppression",
)

print("FinalTECH ItemStack type compatibility boundary: PASS")
