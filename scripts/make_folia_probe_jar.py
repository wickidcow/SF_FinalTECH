#!/usr/bin/env python3
"""Create a disposable FinalTECH JAR with Folia loading enabled for CI only."""

from __future__ import annotations

import argparse
from pathlib import Path
import re
import sys
import zipfile

KEY_RE = re.compile(r"(?im)^\s*folia-supported\s*:")


def make_probe(source: Path, output: Path) -> None:
    source_bytes = source.read_bytes()
    with zipfile.ZipFile(source) as src:
        names = src.namelist()
        if "plugin.yml" not in names:
            raise ValueError("FinalTECH JAR is missing plugin.yml")
        descriptor = src.read("plugin.yml").decode("utf-8")
        if KEY_RE.search(descriptor):
            raise ValueError("Production descriptor already declares folia-supported; probe copier must be retired")
        descriptor = descriptor.rstrip() + "\nfolia-supported: true\n"

        output.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(output, "w") as dst:
            for info in src.infolist():
                data = descriptor.encode("utf-8") if info.filename == "plugin.yml" else src.read(info.filename)
                copied = zipfile.ZipInfo(info.filename, date_time=info.date_time)
                copied.compress_type = info.compress_type
                copied.comment = info.comment
                copied.extra = info.extra
                copied.internal_attr = info.internal_attr
                copied.external_attr = info.external_attr
                copied.create_system = info.create_system
                dst.writestr(copied, data)

    if source.read_bytes() != source_bytes:
        raise ValueError("Source JAR changed while producing the disposable probe")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    try:
        make_probe(args.source, args.output)
    except Exception as exc:
        print(f"Could not create Folia probe JAR: {exc}", file=sys.stderr)
        return 1
    print(args.output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
