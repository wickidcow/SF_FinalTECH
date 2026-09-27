#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

recipe = read("src/main/java/io/taraxacum/finaltech/util/RecipeUtil.java")
manual = read("src/main/java/io/taraxacum/finaltech/core/item/machine/manual/craft/ManualCraftingTable.java")
compat = read("src/main/java/io/taraxacum/libs/slimefun/compat/LegacyRecipeApiCompat.java")

require(".getInputMaterial()" not in recipe,
        "RecipeUtil must not use deprecated GoldPan#getInputMaterial")
require("LegacyRecipeApiCompat.getPrimaryGoldPanInput" in recipe,
        "RecipeUtil must preserve GoldPan representative-input behavior through the compatibility helper")

require(".getIngredientMap()" not in manual and ".getIngredientList()" not in manual,
        "ManualCraftingTable must not use deprecated Bukkit ingredient APIs")
require(".getChoiceMap()" in manual and ".getChoiceList()" in manual,
        "ManualCraftingTable must use modern Bukkit recipe-choice APIs")
require("LegacyRecipeApiCompat.getRepresentativeIngredient" in manual,
        "ManualCraftingTable must preserve representative ingredient behavior")

for marker in (
    "goldPan.getInputMaterials()",
    "Material.SOUL_SAND",
    "Material.GRAVEL",
    "RecipeChoice.MaterialChoice",
    "RecipeChoice.ExactChoice",
    '@SuppressWarnings("deprecation")',
):
    require(marker in compat, f"recipe compatibility helper is missing: {marker}")

print("FinalTECH recipe API compatibility boundary: PASS")
