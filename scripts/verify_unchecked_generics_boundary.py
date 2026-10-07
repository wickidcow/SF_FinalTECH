#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


pom = read("pom.xml")
iface = read("src/main/java/io/taraxacum/common/api/RunnableLockFactory.java")
impl = read("src/main/java/io/taraxacum/libs/plugin/dto/ServerRunnableLockFactory.java")

require(
    "<arg>-Xlint:unchecked</arg>" in pom,
    "FinalTECH must keep unchecked lint enabled in release compilation",
)
require(
    '@SuppressWarnings("unchecked")\npublic interface RunnableLockFactory<T>' in iface,
    "RunnableLockFactory must document its generic-varargs compatibility boundary",
)
require(
    impl.count('@SuppressWarnings("unchecked")') >= 2,
    "ServerRunnableLockFactory must isolate both class-keyed unchecked casts",
)
require(
    "@SafeVarargs\n        protected final void remove(@Nonnull T... objects)" in impl,
    "ObjectMap varargs removal must remain explicitly safe/final",
)
require(
    "return (ServerRunnableLockFactory<T>) serverRunnableLockFactory;" in impl,
    "ServerRunnableLockFactory must preserve the class-keyed instance cache behavior",
)
require(
    "return (ObjectMap<T>) INSTANCE_MAP.get(clazz);" in impl,
    "ObjectMap must preserve the class-keyed shared map behavior",
)

print("FinalTECH unchecked generics boundary: PASS")
