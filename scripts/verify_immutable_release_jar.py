#!/usr/bin/env python3
"""Fail-closed identity and SHA-256 gate for a new immutable FinalTECH raw JAR."""
from __future__ import annotations

import hashlib
from pathlib import Path
import re
import sys
import zipfile

from check_java21_bytecode import audit

MAIN = "io/taraxacum/finaltech/FinalTechChanged.class"


def verify(path: Path, version: str, expected_sha256: str) -> None:
    if re.fullmatch(r"[0-9]+(?:\.[0-9]+){2}", version) is None:
        raise ValueError("Stable FinalTECH version must have three numeric segments")
    if path.name != f"SF_FinalTECH{version}.jar" or not path.is_file():
        raise ValueError("Missing canonical FinalTECH raw JAR")
    if re.fullmatch("[0-9a-f]{64}", expected_sha256) is None:
        raise ValueError("Missing required raw SHA-256")
    with path.open("rb") as f:
        actual = hashlib.file_digest(f, "sha256").hexdigest()
    if actual != expected_sha256:
        raise ValueError("Raw FinalTECH artifact SHA-256 mismatch")

    with zipfile.ZipFile(path) as jar:
        names = jar.namelist()
        if jar.testzip() is not None:
            raise ValueError("Corrupt FinalTECH shaded JAR member")
        if names.count("plugin.yml") != 1 or names.count("META-INF/MANIFEST.MF") != 1:
            raise ValueError("Missing or duplicate plugin descriptor/manifest")
        if MAIN not in names:
            raise ValueError("FinalTECH entrypoint is absent")
        plugin = jar.read("plugin.yml").decode("utf-8")
        expected = (("name", "FinalTECH-Changed"),
                    ("main", "io.taraxacum.finaltech.FinalTechChanged"),
                    ("version", version))
        for key, value in expected:
            if re.search(r"(?m)^" + re.escape(key) + r":\s*['\"]?"
                         + re.escape(value) + r"['\"]?\s*$", plugin) is None:
                raise ValueError(f"FinalTECH plugin.yml {key} changed")
        if b"Manifest-Version:" not in jar.read("META-INF/MANIFEST.MF"):
            raise ValueError("Invalid FinalTECH manifest")
        if not any(name.startswith("io/taraxacum/finaltech/shaded/bstats/")
                   and name.endswith(".class") for name in names):
            raise ValueError("Relocated bStats absent")
        if any(name.startswith("org/bstats/") and name.endswith(".class") for name in names):
            raise ValueError("Unrelocated bStats leaked")

    count, versions, errors = audit(path)
    if errors:
        raise ValueError("Java 21 bytecode check failed: " + "; ".join(errors[:4]))
    if count < 1:
        raise ValueError("No FinalTECH classes")
    print(f"PASS SF_FinalTECH{version}.jar sha256={actual}, owned_classes={count}, bytecode={dict(versions)}")


if __name__ == "__main__":
    if len(sys.argv) != 4:
        raise SystemExit("Usage: verify_immutable_release_jar.py FILE VERSION SHA256")
    try:
        verify(Path(sys.argv[1]), sys.argv[2], sys.argv[3])
    except (ValueError, OSError, zipfile.BadZipFile) as e:
        raise SystemExit(f"FAIL verified FinalTECH raw release: {e}") from e
