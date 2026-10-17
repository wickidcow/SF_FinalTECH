#!/usr/bin/env python3

from __future__ import annotations

import io
from pathlib import Path
import tempfile
import unittest
import zipfile

import make_folia_probe_jar as probe


def jar_bytes(plugin_yml: str, extra: bytes = b"payload") -> bytes:
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("plugin.yml", plugin_yml)
        zf.writestr("example.bin", extra)
    return buffer.getvalue()


class TestFoliaProbeJar(unittest.TestCase):
    def setUp(self) -> None:
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.src = self.root / "source.jar"
        self.dst = self.root / "probe.jar"

    def test_adds_flag_only_to_copy(self) -> None:
        source = jar_bytes("name: FinalTECH-Changed\nversion: 3.0.15\n")
        self.src.write_bytes(source)
        probe.make_probe(self.src, self.dst)
        self.assertEqual(self.src.read_bytes(), source)
        with zipfile.ZipFile(self.dst) as zf:
            descriptor = zf.read("plugin.yml").decode()
            self.assertIn("folia-supported: true", descriptor)
            self.assertEqual(zf.read("example.bin"), b"payload")

    def test_existing_flag_refused(self) -> None:
        self.src.write_bytes(jar_bytes("name: FinalTECH-Changed\nfolia-supported: true\n"))
        with self.assertRaisesRegex(ValueError, "already declares folia-supported"):
            probe.make_probe(self.src, self.dst)

    def test_missing_descriptor_refused(self) -> None:
        with zipfile.ZipFile(self.src, "w") as zf:
            zf.writestr("example.bin", b"x")
        with self.assertRaisesRegex(ValueError, "missing plugin.yml"):
            probe.make_probe(self.src, self.dst)


if __name__ == "__main__":
    unittest.main()
