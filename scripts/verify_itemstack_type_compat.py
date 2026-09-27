#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

TARGETS = [
    "src/main/java/io/taraxacum/finaltech/core/item/machine/operation/DustFactoryDirt.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/AbstractCubeElectricGenerator.java",
    "src/main/java/io/taraxacum/finaltech/core/menu/machine/ItemDismantleTableMenu.java",
    "src/main/java/io/taraxacum/finaltech/core/menu/manual/CardOperationPortMenu.java",
]

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

for target in TARGETS:
    text = (ROOT / target).read_text(encoding="utf-8")
    require(
        "LegacyItemStackCompat.setType(" in text,
        f"{target} must preserve in-place type mutation through LegacyItemStackCompat",
    )
    for direct in ("itemStack.setType(", "item.setType(", "iconItem.setType("):
        require(direct not in text, f"{target} still uses deprecated ItemStack#setType directly: {direct}")

compat = (ROOT / "src/main/java/io/taraxacum/libs/plugin/util/LegacyItemStackCompat.java").read_text(encoding="utf-8")
require('@SuppressWarnings("deprecation")' in compat, "LegacyItemStackCompat must explicitly isolate the deprecated API")
require("item.setType(material);" in compat, "LegacyItemStackCompat must preserve exact in-place mutation semantics")

print("FinalTECH ItemStack type compatibility boundary: PASS")
