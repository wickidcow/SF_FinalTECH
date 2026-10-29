"""Exercise the real all-class raw-JAR Java 21 scanner without server dependencies."""
from __future__ import annotations

import hashlib
import struct
import subprocess
import sys
import tempfile
import unittest
import warnings
import zipfile
from pathlib import Path

SCRIPT = Path(__file__).with_name("check_java21_bytecode.py")
OWNED = "io/taraxacum/finaltech/FinalTechChanged.class"


def header(major: int = 65, minor: int = 0, magic: bytes = b"\xca\xfe\xba\xbe") -> bytes:
    return magic + struct.pack(">HH", minor, major) + b"test-header-only"


class Java21BytecodeTests(unittest.TestCase):
    def run_cli(self, entries: list[tuple[str, bytes]], expected: int) -> str:
        with tempfile.TemporaryDirectory() as dirname:
            jar = Path(dirname) / "candidate.jar"
            with zipfile.ZipFile(jar, "w") as archive:
                for name, data in entries:
                    with warnings.catch_warnings():
                        warnings.simplefilter("ignore", UserWarning)
                        archive.writestr(name, data)
            original = hashlib.sha256(jar.read_bytes()).hexdigest()
            result = subprocess.run([sys.executable, str(SCRIPT), str(jar)],
                                    capture_output=True, text=True, check=False)
            self.assertEqual(original, hashlib.sha256(jar.read_bytes()).hexdigest(), "Audit changed JAR bytes")
            self.assertEqual(expected, result.returncode, result.stdout + result.stderr)
            return result.stdout + result.stderr

    def test_java21_and_shaded_java17_pass(self):
        self.assertIn("PASS:", self.run_cli([(OWNED, header()), ("shaded/Compat.class", header(61))], 0))

    def test_legacy_nonzero_minor_passes(self):
        self.run_cli([(OWNED, header(55, 65535))], 0)

    def test_common_package_counts_as_owned(self):
        self.run_cli([("io/taraxacum/common/MathUtil.class", header())], 0)

    def test_newer_bytecode_is_rejected(self):
        self.assertIn("66.0", self.run_cli([(OWNED, header(66))], 1))

    def test_preview_java21_is_rejected(self):
        output = self.run_cli([(OWNED, header(65, 65535))], 1)
        self.assertIn("preview", output)
        self.assertNotIn("PASS:", output)

    def test_earlier_preview_rejected(self):
        self.assertIn("61.65535", self.run_cli([(OWNED, header(61, 65535))], 1))

    def test_invalid_minor_rejected(self):
        self.assertIn("65.1", self.run_cli([(OWNED, header(65, 1))], 1))

    def test_invalid_shaded_class_cannot_hide_behind_valid_main(self):
        self.assertIn("shaded/Bad.class", self.run_cli([(OWNED, header()), ("shaded/Bad.class", header(66))], 1))

    def test_bad_magic_fails(self):
        self.assertIn("malformed", self.run_cli([(OWNED, header(magic=b"FAIL"))], 1))

    def test_short_header_fails(self):
        self.run_cli([(OWNED, b"\xca\xfe\xba\xbe\x00")], 1)

    def test_duplicate_class_member_rejected(self):
        self.assertIn("duplicate", self.run_cli([(OWNED, header()), (OWNED, header())], 1))

    def test_empty_or_wrong_jar_fails(self):
        self.run_cli([("plugin.yml", b"name: FinalTECH")], 1)
        self.run_cli([("external/Only.class", header())], 1)

    def test_invalid_major_rejected(self):
        self.run_cli([(OWNED, header(44))], 1)

    def test_not_a_zip_fails(self):
        with tempfile.TemporaryDirectory() as dirname:
            jar = Path(dirname) / "broken.jar"
            jar.write_bytes(b"not a jar")
            result = subprocess.run([sys.executable, str(SCRIPT), str(jar)], capture_output=True, text=True)
            self.assertEqual(result.returncode, 1)
            self.assertIn("unreadable JAR", result.stderr)


if __name__ == "__main__":
    unittest.main()
