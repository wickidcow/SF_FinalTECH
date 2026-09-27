#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

SURFACES = [
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/VariableWireResistance.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/electric/capacitor/expanded/AbstractExpandedElectricCapacitor.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/manual/craft/AbstractManualCraftMachine.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/range/cube/generator/AbstractCubeElectricGenerator.java",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/AbstractMachineActivateCard.java",
    "src/main/java/io/taraxacum/finaltech/core/item/usable/machine/AbstractMachineChargeCard.java",
    "src/main/java/io/taraxacum/finaltech/core/patch/EnergyRegulatorDetailMenu.java",
]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

for path in SURFACES:
    text = read(path)
    require("LegacySlimefunApiCompat" in text, f"{path} must use LegacySlimefunApiCompat")
    for line in text.splitlines():
        stripped = line.strip()
        if (
            ".getCharge(" in stripped
            or ".setCharge(" in stripped
            or ".addCharge(" in stripped
            or ".removeCharge(" in stripped
        ):
            allowed_facade = "EnergyUtil." in stripped or "LegacySlimefunApiCompat." in stripped
            require(
                allowed_facade,
                f"{path} contains a direct legacy energy call: {stripped}",
            )

network = read("src/main/java/io/taraxacum/finaltech/core/networks/AlteredEnergyNet.java")
require(
    "LegacySlimefunApiCompat" in network
    and "LegacyTickerDataCompat" in network
    and "LegacyBlockDataCompat" in network,
    "AlteredEnergyNet must use the reviewed energy, ticker-data, and block-data compatibility boundaries",
)
require(
    "BlockStorage." not in network,
    "AlteredEnergyNet must not reintroduce direct deprecated BlockStorage access",
)
require(
    "public Summary tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config)" in network,
    "AlteredEnergyNet must retain its RC-37 Config-facing tick signature",
)
require(
    '@SuppressWarnings("deprecation")' in network,
    "AlteredEnergyNet must explicitly mark its intentional RC-37 Config compatibility surface",
)
for line in network.splitlines():
    stripped = line.strip()
    if (
        ".getCharge(" in stripped
        or ".setCharge(" in stripped
        or ".getGeneratedOutput(" in stripped
        or ".willExplode(" in stripped
    ):
        require(
            "LegacySlimefunApiCompat." in stripped,
            f"AlteredEnergyNet contains a direct legacy energy/provider call: {stripped}",
        )

print("FinalTECH non-network energy compatibility boundary: PASS")
