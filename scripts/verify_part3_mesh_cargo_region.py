#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


mesh = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/MeshTransfer.java")
advanced = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedMeshTransfer.java")

for source, name in ((mesh, "MeshTransfer"), (advanced, "AdvancedMeshTransfer")):
    require(
        "if (!LegacySlimefunApiCompat.isOwnedByCurrentRegion(location))" in source,
        f"{name} must refuse execution outside the source region's owning tick",
    )
    require(
        "BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(location);" in source,
        f"{name} must obtain its source menu only after the ownership gate",
    )
    require(
        "@Nullable\n    public Block searchBlock(" in source,
        f"{name} search must be able to report a foreign-region boundary",
    )
    require(
        source.count("LegacySlimefunApiCompat.isOwnedByCurrentRegion(result.getLocation())") >= 2,
        f"{name} must check region ownership before reading traversed blocks",
    )
    require(
        source.count("if (outputBlocks[i] == null)") >= 2
        and source.count("if (inputBlocks[i] == null)") >= 2,
        f"{name} must abort before permission/inventory access when any mesh arm crosses a region",
    )
    require(
        "CargoUtil.doSimpleCargoInputMain(simpleCargoDTO)" in source
        and "CargoUtil.doSimpleCargoOutputMain(simpleCargoDTO)" in source
        and "CargoUtil.doSimpleCargoStrongSymmetry(simpleCargoDTO)" in source,
        f"{name} must preserve the existing mesh cargo algorithms",
    )
    require(
        "LocationUtil.transferToLocation(inputBlocks)" in source
        and "LocationUtil.transferToLocation(outputBlocks)" in source,
        f"{name} must preserve permission/topology handling for discovered mesh arms",
    )

for path in (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LineTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLineTransfer.java",
):
    source = read(path)
    require(
        "LegacySlimefunApiCompat.isOwnedByCurrentRegion" in source,
        f"{path} lost the previously validated line-cargo boundary",
    )

print("FinalTECH Part 3 mesh cargo region safety: PASS")
