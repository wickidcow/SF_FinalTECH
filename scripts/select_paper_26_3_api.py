#!/usr/bin/env python3
"""Choose the highest real stable Paper 26.3 API build; never an RC or alpha.

Maven metadata is not guaranteed sorted. An accidental 26.3-rc selection
changes the source API of an otherwise immutable Java 21 addon build.
"""
from __future__ import annotations

import argparse
import re
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

VERSION_PATTERN = re.compile(r"26\.3\.build\.([1-9][0-9]*)-stable")
API_METADATA_URL = (
    "https://repo.papermc.io/repository/maven-public/"
    "io/papermc/paper/paper-api/maven-metadata.xml"
)


def select_stable_263(metadata: bytes | str) -> str:
    """Return the newest numeric 26.3 stable build, never a release candidate."""
    root = ET.fromstring(metadata)
    versions = root.findall("./versioning/versions/version")
    valid = []
    for element in versions:
        value = (element.text or "").strip()
        match = VERSION_PATTERN.fullmatch(value)
        if match:
            valid.append((int(match.group(1)), value))
    if not valid:
        raise ValueError(
            "No 26.3.build.N-stable Paper API was found: cannot safely compile "
            "against an alpha, RC, another release line or missing metadata"
        )
    return max(valid)[1]


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--github-env", required=True, type=Path,
        help="GitHub Actions GITHUB_ENV file (append-only)",
    )
    args = parser.parse_args()
    request = urllib.request.Request(
        API_METADATA_URL, headers={"User-Agent": "FinalTECH-26.3-Stable-API-CI/1.0"}
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        metadata = response.read()
    version = select_stable_263(metadata)
    # Nothing is written unless the metadata parses and a valid stable exists.
    with args.github_env.open("a", encoding="utf-8") as stream:
        stream.write(f"PAPER_26_3_VERSION={version}\n")
    print(f"Selected newest numeric stable Paper 26.3 API: {version}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
