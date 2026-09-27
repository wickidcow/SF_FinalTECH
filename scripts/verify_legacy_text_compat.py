#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

TARGETS = [
    "src/main/java/io/taraxacum/finaltech/util/GuideItemLoreUtil.java",
    "src/main/java/io/taraxacum/finaltech/core/item/unusable/CopyCard.java",
    "src/main/java/io/taraxacum/finaltech/core/item/unusable/StorageCard.java",
    "src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java",
]

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

for target in TARGETS:
    text = (ROOT / target).read_text(encoding="utf-8")
    require("org.bukkit.ChatColor" not in text, f"{target} must not depend on deprecated ChatColor")
    for direct in (".getDisplayName()", ".getLore()", ".setLore("):
        require(direct not in text, f"{target} still uses deprecated ItemMeta text API: {direct}")

text_util = (ROOT / "src/main/java/io/taraxacum/libs/plugin/util/TextUtil.java").read_text(encoding="utf-8")
for marker in (
    'LEGACY_DARK_GRAY = "§8"',
    'LEGACY_GREEN = "§a"',
    'LEGACY_YELLOW = "§e"',
    'LEGACY_GRAY = "§7"',
    'Pattern.compile("(?i)§[0-9A-FK-ORX]")',
    "public static String stripColor",
):
    require(marker in text_util, f"TextUtil legacy compatibility is missing: {marker}")

item_util = (ROOT / "src/main/java/io/taraxacum/libs/plugin/util/ItemStackUtil.java").read_text(encoding="utf-8")
for marker in (
    "public static List<String> getLore(@Nullable ItemStack item)",
    "public static List<String> getLore(@Nonnull ItemMeta itemMeta)",
    "public static void setLore(@Nonnull ItemMeta itemMeta",
    "return getLegacyLore(itemMeta);",
    "setLegacyLore(itemMeta, lore);",
):
    require(marker in item_util, f"ItemStackUtil lore compatibility is missing: {marker}")

print("FinalTECH legacy text compatibility boundary: PASS")
