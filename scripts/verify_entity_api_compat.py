#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

HEALTH_SURFACES = [
    "src/main/java/io/taraxacum/finaltech/core/item/machine/tower/CureTower.java",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/MachineAccelerateCardL3.java",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/MachineActivateCardL3.java",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/MatrixMachineActivateCard.java",
    "src/main/java/io/taraxacum/finaltech/core/listener/ShineListener.java",
    "src/main/java/io/taraxacum/finaltech/core/task/effect/VoidCurse.java",
]
for path in HEALTH_SURFACES:
    text = read(path)
    require(".getMaxHealth()" not in text, f"{path} must not use deprecated Damageable#getMaxHealth")
    require("EntityAttributeCompat.getMaxHealth" in text, f"{path} must use EntityAttributeCompat")

attribute = read("src/main/java/io/taraxacum/libs/plugin/util/EntityAttributeCompat.java")
require("Attribute.MAX_HEALTH" in attribute and "entity.getAttribute(" in attribute,
        "EntityAttributeCompat must prefer Attribute.MAX_HEALTH")
require('@SuppressWarnings("deprecation")' in attribute and "entity.getMaxHealth()" in attribute,
        "EntityAttributeCompat must keep the legacy getter isolated as a fallback")

magic = read("src/main/java/io/taraxacum/finaltech/core/item/usable/MagicHypnotic.java")
require("PotionEffectType.values()" not in magic, "MagicHypnotic must not use deprecated potion values()")
require("RegistryAccess.registryAccess()" in magic and "RegistryKey.MOB_EFFECT" in magic,
        "MagicHypnotic must enumerate the mob-effect registry")

shine = read("src/main/java/io/taraxacum/finaltech/core/listener/ShineListener.java")
require(".setDeathMessage(" not in shine, "ShineListener must not use deprecated String death messages")
require(".deathMessage(LegacyTextCompat.fromLegacy(" in shine,
        "ShineListener must preserve legacy formatting through Adventure deathMessage")

compat = read("src/main/java/io/taraxacum/libs/plugin/util/LegacyTextCompat.java")
require("public static Component fromLegacy" in compat and "LEGACY.deserialize(value)" in compat,
        "LegacyTextCompat must expose the legacy-to-Adventure bridge")

print("FinalTECH modern entity API compatibility: PASS")
