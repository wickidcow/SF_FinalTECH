#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


LINE_FILES = (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/LineTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedLineTransfer.java",
)

MESH_FILES = (
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/MeshTransfer.java",
    "src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/AdvancedMeshTransfer.java",
)

for path in LINE_FILES + MESH_FILES:
    source = read(path)
    require(
        "LegacySlimefunApiCompat.isOwnedByCurrentRegion(location)" in source,
        f"{path} must start its cargo tick on the owning region",
    )
    for forbidden in (
        "boolean primaryThread",
        "if (primaryThread)",
        "ServerRunnableLockFactory",
        ".getScheduler().runTask(javaPlugin,",
    ):
        require(
            forbidden not in source,
            f"{path} reintroduced the old global/async cargo fallback: {forbidden}",
        )

for path in LINE_FILES:
    source = read(path)
    require(
        "if (blockList.isEmpty()) {\n            return;\n        }" in source,
        f"{path} must fail closed when line search cannot remain in the current region",
    )
    require(
        source.count("LegacySlimefunApiCompat.isOwnedByCurrentRegion(block.getLocation())") >= 3,
        f"{path} must region-check every line traversal branch before inventory/menu access",
    )
    require(
        "CargoUtil.doSimpleCargo(simpleCargoDTO, cargoMode)" in source,
        f"{path} must preserve the existing line cargo algorithm",
    )

for path in MESH_FILES:
    source = read(path)
    require(
        "import javax.annotation.Nullable;" in source,
        f"{path} mesh search must explicitly allow a region-boundary null result",
    )
    require(
        "public Block searchBlock(" in source
        and "@Nullable" in source,
        f"{path} mesh search must expose nullable region-boundary failure",
    )
    require(
        source.count("LegacySlimefunApiCompat.isOwnedByCurrentRegion(result.getLocation())") >= 2,
        f"{path} must check region ownership before each mesh traversal read",
    )
    require(
        "if (outputBlocks[i] == null) {\n                return;\n            }" in source
        and "if (inputBlocks[i] == null) {\n                return;\n            }" in source,
        f"{path} must abort the whole tick if any mesh endpoint crosses a region boundary",
    )
    require(
        "CargoUtil.doSimpleCargoInputMain(simpleCargoDTO)" in source
        and "CargoUtil.doSimpleCargoOutputMain(simpleCargoDTO)" in source
        and "CargoUtil.doSimpleCargoStrongSymmetry(simpleCargoDTO)" in source,
        f"{path} must preserve all existing mesh cargo algorithms",
    )

particle = read("src/main/java/io/taraxacum/libs/plugin/util/ParticleUtil.java")
require(
    "runTaskLaterAsynchronously" in particle,
    "particle scheduling changed unexpectedly; particle modernization is intentionally a later tranche",
)

print("FinalTECH Part 3 line/mesh region ownership safety: PASS")
