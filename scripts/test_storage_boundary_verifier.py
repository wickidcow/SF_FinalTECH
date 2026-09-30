#!/usr/bin/env python3
"""Mutation checks for the Part 2/3 storage regression guard."""

from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[1]
VERIFIER = Path("scripts/verify_part2_storage_boundaries.py")
JAVA = Path("src/main/java/io/taraxacum")
COMPAT = JAVA / "libs/slimefun/compat/LegacyBlockDataCompat.java"
SETUP = JAVA / "finaltech/setup/SetupUtil.java"
PLUGIN = JAVA / "finaltech/FinalTechChanged.java"


class StorageBoundaryVerifierTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temporary = tempfile.TemporaryDirectory()
        cls.addClassCleanup(cls.temporary.cleanup)
        cls.root = Path(cls.temporary.name)
        shutil.copytree(ROOT / "src", cls.root / "src")
        (cls.root / "scripts").mkdir()
        shutil.copy2(ROOT / VERIFIER, cls.root / VERIFIER)

    def verify(self):
        return subprocess.run(
            [sys.executable, str(self.root / VERIFIER)],
            capture_output=True,
            text=True,
            check=False,
        )

    def assert_rejected(self, path, old, new, diagnostic):
        target = self.root / path
        original = target.read_text(encoding="utf-8")
        self.assertIn(old, original, "Mutation must change the intended source")
        try:
            target.write_text(original.replace(old, new, 1), encoding="utf-8")
            result = self.verify()
            self.assertNotEqual(result.returncode, 0, result.stdout)
            self.assertIn(diagnostic, result.stderr)
        finally:
            target.write_text(original, encoding="utf-8")

    def test_current_boundary_and_retained_ticker_bridge_pass(self):
        result = self.verify()
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_recovery_option_cannot_resume_storage_writes(self):
        for call in (
            "LegacyBlockDataCompat.setValue(null, null, null);",
            "Slimefun.getDatabaseManager().getBlockDataController().removeBlock(null);",
        ):
            with self.subTest(call=call):
                self.assert_rejected(
                    SETUP, "public static void dataLossFix() {",
                    "public static void dataLossFix() {\n        " + call,
                    "harmless compatibility no-op",
                )

    def test_retired_storage_fallback_cannot_return(self):
        self.assert_rejected(
            COMPAT, "return getLoadedData(location) != null;",
            "return BlockStorage.hasBlockInfo(location);",
            "must not restore the retired RC-37 BlockStorage fallback",
        )
        self.assert_rejected(
            COMPAT, "private LegacyBlockDataCompat() {",
            'private LegacyBlockDataCompat() {\n        Class.forName("legacy.Storage");',
            "retired storage fallback returned",
        )

    def test_modern_storage_operations_remain_required(self):
        for expression in (
            "controller.loadBlockData(blockData)",
            "blockData.removeData(key)",
            "blockData.setData(key, value)",
            "controller().createBlock(location, slimefunId)",
        ):
            with self.subTest(expression=expression):
                self.assert_rejected(COMPAT, expression + ";", ";",
                                     "modern storage boundary is missing")

    def test_menu_less_blocks_do_not_become_inventories(self):
        self.assert_rejected(
            COMPAT, "blockData != null && blockData.getBlockMenu() != null",
            "blockData != null",
            "must distinguish records with and without menus",
        )

    def test_shutdown_flush_cannot_return(self):
        self.assert_rejected(
            PLUGIN, "public void onDisable() {",
            "public void onDisable() {\n        LegacyBlockDataCompat.flushLegacyStorage();",
            "instead of RC-37 flush hooks",
        )

    def test_persisted_machine_key_remains_protected(self):
        self.assert_rejected(
            JAVA / "finaltech/core/helper/MachineMaxStack.java",
            'KEY = "mms"', 'KEY = "changed"',
            "MachineMaxStack key must remain mms",
        )


if __name__ == "__main__":
    unittest.main()
