#!/usr/bin/env python3
"""Validate every packaged FinalTECH class header for stable Java 21 loading.

A class-header audit cannot prove JVM verification, gameplay or saved-data safety.
"""
from __future__ import annotations

import collections
import struct
import sys
import zipfile
from pathlib import Path

MAX_MAJOR = 65
MAGIC = b"\xca\xfe\xba\xbe"
OWNED = ("io/taraxacum/finaltech/", "io/taraxacum/common/")


def audit(path: Path) -> tuple[int, collections.Counter[int], list[str]]:
    owned = 0
    versions: collections.Counter[int] = collections.Counter()
    errors: list[str] = []
    seen: set[str] = set()
    try:
        with zipfile.ZipFile(path) as jar:
            for info in sorted(jar.infolist(), key=lambda entry: entry.filename):
                name = info.filename
                if not name.endswith(".class"):
                    continue
                if name in seen:
                    errors.append(f"{name}: duplicate class member")
                    continue
                seen.add(name)
                owned += name.startswith(OWNED)
                try:
                    with jar.open(info) as stream:
                        data = stream.read(8)
                except (OSError, RuntimeError, EOFError, zipfile.BadZipFile) as exc:
                    errors.append(f"{name}: unreadable class header ({type(exc).__name__})")
                    continue
                if len(data) != 8 or data[:4] != MAGIC:
                    errors.append(f"{name}: missing or malformed class header")
                    continue
                minor, major = struct.unpack(">HH", data[4:8])
                versions[major] += 1
                if major < 45 or major > MAX_MAJOR:
                    errors.append(f"{name}: class version {major}.{minor} is outside Java 21 major 45..65")
                elif major >= 56 and minor != 0:
                    kind = "preview" if minor == 65535 else "invalid modern minor"
                    errors.append(f"{name}: {kind} class version {major}.{minor} cannot be shipped as stable Java 21")
    except (OSError, EOFError, ValueError, zipfile.BadZipFile) as exc:
        errors.append(f"{path}: unreadable JAR ({type(exc).__name__})")
    if not owned:
        errors.append("No FinalTECH-owned classes in the packaged JAR")
    return owned, versions, errors


def main(argv: list[str] | None = None) -> int:
    from argparse import ArgumentParser

    parser = ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    args = parser.parse_args(argv)
    owned, histogram, errors = audit(args.jar)
    print(f"Java 21 gate scanned {sum(histogram.values())} classes ({owned} FinalTECH-owned)")
    print("Major versions: " + ", ".join(f"{major}={count}" for major, count in sorted(histogram.items())))
    if errors:
        for error in errors:
            print("FAIL: " + error, file=sys.stderr)
        return 1
    print("PASS: stable Java 21-compatible class-file headers throughout raw JAR")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
