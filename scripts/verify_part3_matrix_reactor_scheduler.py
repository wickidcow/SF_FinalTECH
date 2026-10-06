#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


reactor = read("src/main/java/io/taraxacum/finaltech/core/item/machine/MatrixReactor.java")

require(
    "protected boolean isSynchronized() {\n        return true;\n    }" in reactor,
    "MatrixReactor must request Slimefun's location-owned synchronous tick path",
)
require(
    "blockMenu.dropItems(location, MatrixReactorMenu.ITEM_INPUT_SLOT);" in reactor,
    "MatrixReactor invalid-input ejection must stay inside its owned tick",
)
for forbidden in (
    "runTaskAsynchronously",
    "runTaskLaterAsynchronously",
    ".getScheduler().runTask(",
    ".getScheduler().runTaskLater(",
    "ServerRunnableLockFactory",
    "getLocationRunnableFactory",
):
    require(
        forbidden not in reactor,
        f"MatrixReactor reintroduced a scheduler escape from its location-owned tick: {forbidden}",
    )

for key in ('private final String keyItem = "item";', 'private final String keyCount = "count";'):
    require(key in reactor, f"MatrixReactor persisted key changed: {key}")

require(
    "LegacyTickerDataCompat.setValue(data, keyItem, null);" in reactor
    and 'LegacyTickerDataCompat.setValue(data, keyCount, "0");' in reactor,
    "MatrixReactor reset sequence must retain item removal followed by zero count",
)
require(
    "Cargo" not in reactor,
    "MatrixReactor scheduler batch must not absorb unrelated cargo changes",
)

print("FinalTECH Part 3 Matrix Reactor scheduler safety: PASS")
