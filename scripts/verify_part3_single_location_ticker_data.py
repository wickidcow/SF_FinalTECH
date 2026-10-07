#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


multi = read("src/main/java/io/taraxacum/finaltech/core/item/machine/MultiFrameMachine.java")
time_cap = read("src/main/java/io/taraxacum/finaltech/core/item/machine/TimeCapacitor.java")

for source, name in ((multi, "MultiFrameMachine"), (time_cap, "TimeCapacitor")):
    require(
        "protected boolean requiresLocationOwnedTick() {\n        return true;\n    }" in source,
        f"{name} must opt out of forced async ticking",
    )
    require(
        "protected boolean isSynchronized() {\n        return false;\n    }" in source,
        f"{name} historical synchronization declaration unexpectedly changed",
    )
    for forbidden in (
        "runTaskAsynchronously",
        "runTaskLaterAsynchronously",
        ".getScheduler().runTask(",
        ".getScheduler().runTaskLater(",
        "getLocationRunnableFactory",
        "ServerRunnableLockFactory",
    ):
        require(
            forbidden not in source,
            f"{name} reintroduced an unnecessary scheduler escape: {forbidden}",
        )

require(
    "BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(block.getLocation());" in multi
    and "Inventory inventory = blockMenu.toInventory();" in multi,
    "MultiFrameMachine must retain its same-location inventory processing",
)
require(
    "simpleCargoDTO.setInputBlock(block);" in multi
    and "simpleCargoDTO.setOutputBlock(block);" in multi,
    "MultiFrameMachine internal cargo must remain within its own block inventory",
)

require(
    "World world = location.getWorld();" in time_cap
    and "LegacySlimefunApiCompat.setCharge(this, location, charge);" in time_cap,
    "TimeCapacitor must retain same-location time/charge behavior",
)
require(
    "BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(location);" in time_cap,
    "TimeCapacitor must retain its own-menu status update",
)

print("FinalTECH Part 3 single-location ticker-data safety: PASS")
