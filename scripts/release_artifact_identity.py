#!/usr/bin/env python3
"""Resolve a FinalTECH release artifact from the exact checked-out Maven source."""
from __future__ import annotations

import argparse
from pathlib import Path
import re
import xml.etree.ElementTree as ET


def release_identity(pom: Path) -> tuple[str, str]:
    root = ET.parse(pom).getroot()
    version = root.findtext("{http://maven.apache.org/POM/4.0.0}version")
    if not version or re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+", version) is None:
        raise ValueError(f"Invalid numeric FinalTECH release version in {pom}: {version!r}")
    return version, f"SF_FinalTECH{version}.jar"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--pom", type=Path, default=Path("pom.xml"))
    parser.add_argument("--github-output", type=Path, required=True)
    args = parser.parse_args()
    version, jar = release_identity(args.pom)
    with args.github_output.open("a", encoding="utf-8") as output:
        output.write(f"version={version}\njar={jar}\n")
    print(f"Exact-source FinalTECH artifact: {jar}")


if __name__ == "__main__":
    main()
