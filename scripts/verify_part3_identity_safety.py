#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


compat = read("src/main/java/io/taraxacum/libs/slimefun/compat/LegacyBlockDataCompat.java")
menu = read("src/main/java/io/taraxacum/finaltech/core/menu/AbstractMachineMenu.java")
capacitor = read("src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireCapacitor.java")
resistance = read("src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireResistance.java")
entropy = read("src/main/java/io/taraxacum/finaltech/core/item/machine/EntropySeed.java")
concept = read("src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/EquivalentConcept.java")

for marker in (
    "enum IdentityDecision",
    "IdentityDecision.CREATE",
    "IdentityDecision.ALREADY_TARGET",
    "IdentityDecision.CONFLICT",
    "createSlimefunIdIfAbsent",
    "Refusing to replace existing Slimefun block identity",
):
    require(marker in compat, f"identity safety boundary is missing: {marker}")

require(
    "identityDecision(getLoadedData(location), slimefunId)" in compat,
    "identity creation must inspect the canonical loaded record before mutation",
)
require(
    "controller().createBlock(location, slimefunId)" in compat,
    "identity creation must still use the current BlockDataController",
)
require(
    "if (getLoadedData(location) != null)" in compat,
    "repair creation must preserve a record that wins a create race",
)

require(
    "LegacyBlockDataCompat.hasBlockData(location)" in menu
    and "LegacyBlockDataCompat.createSlimefunIdIfAbsent(location, this.slimefunItem.getId())" in menu,
    "menu data-loss repair must only use the non-destructive create-if-absent boundary",
)
require(
    "LegacyBlockDataCompat.setSlimefunId(" not in menu,
    "menu repair must never use the identity-transition boundary",
)
require(
    "LocationInfo" not in menu,
    "menu repair must distinguish missing block data directly instead of treating unresolved identity as missing data",
)

for source, name in (
    (capacitor, "VariableWireCapacitor"),
    (resistance, "VariableWireResistance"),
):
    require(
        source.index("LegacyBlockDataCompat.removeBlock(location)")
        < source.index("LegacyBlockDataCompat.setSlimefunId(location"),
        f"{name} must keep remove-before-recreate identity transition ordering",
    )

require(
    entropy.count("LegacyBlockDataCompat.removeBlock(location)") >= 2
    and entropy.count("LegacyBlockDataCompat.setSlimefunId(location") >= 2,
    "EntropySeed must retain its explicit remove-before-recreate transitions",
)
require(
    concept.count("LegacyBlockDataCompat.removeBlock(") == 2
    and concept.count("LegacyBlockDataCompat.setSlimefunId(") == 2,
    "EquivalentConcept must retain both explicit identity transitions",
)

java_root = ROOT / "src/main/java"
direct_create = []
for java_path in java_root.rglob("*.java"):
    source = java_path.read_text(encoding="utf-8")
    if "controller().createBlock(" in source:
        direct_create.append(java_path.relative_to(ROOT).as_posix())

require(
    direct_create == ["src/main/java/io/taraxacum/libs/slimefun/compat/LegacyBlockDataCompat.java"],
    "BlockDataController identity creation escaped the FinalTECH compatibility boundary: "
    + ", ".join(direct_create),
)

print("FinalTECH Part 3 identity safety: PASS")
