#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")

def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)

line = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LineTransfer.java")
advanced = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLineTransfer.java")

for source, name in ((line, "LineTransfer"), (advanced, "AdvancedLineTransfer")):
    require(
        "boolean primaryThread = LegacySlimefunApiCompat.isOwnedByCurrentRegion(location);" in source
        and "if (!primaryThread) {\n            return;\n        }" in source,
        f"{name} must refuse execution outside the source region's owning tick",
    )
    require(
        source.count("LegacySlimefunApiCompat.isOwnedByCurrentRegion(block.getLocation())") >= 3,
        f"{name} must check region ownership while scanning line targets",
    )
    require(
        "while (LegacySlimefunApiCompat.isOwnedByCurrentRegion(block.getLocation())" in source,
        f"{name} must stop line traversal at a foreign region boundary",
    )
    require(
        "CargoUtil.doSimpleCargo(simpleCargoDTO, cargoMode)" in source,
        f"{name} must retain the existing simple-cargo algorithm",
    )
    require(
        "LocationUtil.transferToLocation(blockList)" in source,
        f"{name} must retain permission/topology handling for the discovered line",
    )

print("FinalTECH Part 3 line cargo region safety: PASS")
