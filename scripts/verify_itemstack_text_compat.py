#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / "src/main/java/io/taraxacum/libs/plugin/util/ItemStackUtil.java"
text = path.read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

for deprecated in (
    "itemMeta.getDisplayName()",
    "itemMeta1.getDisplayName()",
    "itemMeta2.getDisplayName()",
    "itemMeta.getLore()",
    "itemMeta1.getLore()",
    "itemMeta2.getLore()",
    "itemMeta.setLore(",
):
    require(deprecated not in text, f"ItemStackUtil still uses deprecated text API: {deprecated}")

for marker in (
    "LegacyComponentSerializer.legacySection()",
    "itemMeta.displayName()",
    "itemMeta.lore()",
    "LEGACY.serialize(",
    "LEGACY.deserialize(",
    "private static List<String> getLegacyLore",
    "private static void setLegacyLore",
):
    require(marker in text, f"ItemStackUtil legacy text bridge is missing: {marker}")

for signature in (
    "public static void addLoreToFirst",
    "public static void addLoreToLast",
    "public static void addLoresToLast",
    "public static String getLastLore",
    "public static void setLore",
    "public static void replaceLore",
):
    require(signature in text, f"ItemStackUtil public String-based contract changed: {signature}")

print("FinalTECH ItemStack text compatibility boundary: PASS")
