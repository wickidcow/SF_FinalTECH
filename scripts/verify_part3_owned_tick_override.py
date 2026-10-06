#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


base = read("src/main/java/io/taraxacum/finaltech/core/item/machine/AbstractMachine.java")
reactor = read("src/main/java/io/taraxacum/finaltech/core/item/machine/MatrixReactor.java")
autocraft = read("src/main/java/io/taraxacum/finaltech/core/item/machine/range/point/face/AdvancedAutoCraft.java")
storage_port = read("src/main/java/io/taraxacum/finaltech/core/item/machine/cargo/storage/StorageInteractPort.java")

require(
    "protected boolean requiresLocationOwnedTick()" in base
    and "return false;" in base,
    "AbstractMachine must expose an opt-in location-owned tick contract",
)
require(
    "boolean requiresLocationOwnedTick = this.requiresLocationOwnedTick();" in base,
    "AbstractMachine must resolve the owned-tick contract before ticker registration",
)
require(
    "boolean forceAsync = FinalTechChanged.getMultiThreadLevel() == 2 && !requiresLocationOwnedTick;" in base,
    "multi-thread level 2 must not force explicitly owned machines async",
)
require(
    "return requiresLocationOwnedTick || AbstractMachine.this.isSynchronized();" in base,
    "owned machines must register a synchronized/location-owned Slimefun ticker",
)
require(
    "if (!requiresLocationOwnedTick && !this.isSynchronized() && FinalTechChanged.getMultiThreadLevel() >= 1)" in base,
    "owned machines must not be registered in FinalTECH's async item set",
)

for source, name in (
    (reactor, "MatrixReactor"),
    (autocraft, "AdvancedAutoCraft"),
    (storage_port, "StorageInteractPort"),
):
    require(
        "protected boolean requiresLocationOwnedTick() {\n        return true;\n    }" in source,
        f"{name} must opt out of forced async ticking",
    )

require(
    "protected boolean isSynchronized() {\n        return true;\n    }" in reactor,
    "MatrixReactor must retain its synchronized ticker request",
)
require(
    "protected boolean isSynchronized() {\n        return true;\n    }" in autocraft,
    "AdvancedAutoCraft must run on the owning location tick",
)
require(
    "Block containerBlock = block.getRelative(BlockFace.DOWN);" in autocraft,
    "AdvancedAutoCraft adjacency contract changed",
)
for forbidden in (
    "getLocationRunnableFactory",
    "isAsyncSlimefunItem(containerId)",
    ".getScheduler().runTask(",
    ".getScheduler().runTaskLater(",
    "runTaskAsynchronously",
    "runTaskLaterAsynchronously",
):
    require(
        forbidden not in autocraft,
        f"AdvancedAutoCraft reintroduced an unnecessary scheduler escape: {forbidden}",
    )

require(
    "Block targetBlock = block.getRelative(BlockFace.UP);" in storage_port,
    "StorageInteractPort adjacency contract changed",
)
require(
    "this.doFunction(targetInventory, blockMenu, block.getLocation());" in storage_port,
    "StorageInteractPort must process the vertically adjacent inventory inside its owned tick",
)
require(
    "blockMenu.dropItems(location, MatrixReactorMenu.ITEM_INPUT_SLOT);" in storage_port,
    "StorageInteractPort invalid input ejection must remain inside its owned tick",
)
for forbidden in (
    "Bukkit.isPrimaryThread",
    "getLocationRunnableFactory",
    ".getScheduler().runTask(",
    ".getScheduler().runTaskLater(",
    "runTaskAsynchronously",
    "runTaskLaterAsynchronously",
):
    require(
        forbidden not in storage_port,
        f"StorageInteractPort reintroduced an unnecessary scheduler escape: {forbidden}",
    )

print("FinalTECH Part 3 owned-tick override safety: PASS")
