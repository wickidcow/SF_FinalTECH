#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

SURFACES = [
    "src/main/java/io/taraxacum/finaltech/core/item/unusable/CopyCard.java",
    "src/main/java/io/taraxacum/finaltech/core/item/unusable/StorageCard.java",
    "src/main/java/io/taraxacum/finaltech/setup/SetupUtil.java",
    "src/main/java/io/taraxacum/finaltech/util/GuideItemLoreUtil.java",
    "src/main/java/io/taraxacum/finaltech/util/LocationUtil.java",
]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

for path in SURFACES:
    text = read(path)
    require("LegacyTextCompat" in text, f"{path} must use LegacyTextCompat")
    require("import org.bukkit.ChatColor;" not in text, f"{path} must not import deprecated ChatColor")
    require("ChatColor." not in text, f"{path} must not use deprecated ChatColor")

for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/unusable/CopyCard.java",
    "src/main/java/io/taraxacum/finaltech/core/item/unusable/StorageCard.java",
    "src/main/java/io/taraxacum/finaltech/util/GuideItemLoreUtil.java",
    "src/main/java/io/taraxacum/finaltech/util/LocationUtil.java",
):
    text = read(path)
    require(".getLore()" not in text, f"{path} must not use deprecated String lore reads")
    require(".setLore(" not in text, f"{path} must not use deprecated String lore writes")

guide = read("src/main/java/io/taraxacum/finaltech/util/GuideItemLoreUtil.java")
require(".getDisplayName()" not in guide, "GuideItemLoreUtil must not use deprecated String display-name reads")

compat = read("src/main/java/io/taraxacum/libs/plugin/util/LegacyTextCompat.java")
for marker in (
    "LegacyComponentSerializer.legacySection()",
    "itemMeta.displayName()",
    "itemMeta.lore()",
    "itemMeta.lore(componentLore)",
    'public static final String DARK_GRAY = "§8"',
    'public static final String GREEN = "§a"',
    'public static final String YELLOW = "§e"',
    'public static final String GRAY = "§7"',
):
    require(marker in compat, f"LegacyTextCompat is missing: {marker}")

print("FinalTECH legacy text compatibility boundary: PASS")
