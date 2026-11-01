"""Release artifact selection must follow pom.xml, never a previous release."""
from __future__ import annotations

from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from release_artifact_identity import release_identity


ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/release.yml"
SCRIPT = Path(__file__).with_name("release_artifact_identity.py")


class ReleaseArtifactIdentityTest(unittest.TestCase):
    def test_real_pom_identity(self) -> None:
        version, jar = release_identity(ROOT / "pom.xml")
        self.assertEqual(f"SF_FinalTECH{version}.jar", jar)

    def test_distinct_future_version_changes_artifact(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            pom = Path(directory) / "pom.xml"
            pom.write_text('<project xmlns="http://maven.apache.org/POM/4.0.0"><version>3.0.17</version></project>')
            self.assertEqual(("3.0.17", "SF_FinalTECH3.0.17.jar"), release_identity(pom))

    def test_non_numeric_or_missing_versions_are_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            pom = Path(directory) / "pom.xml"
            for version in ("", "3.0.17-SNAPSHOT", "3.0.17/evil", "3.0"):
                with self.subTest(version=version):
                    pom.write_text('<project xmlns="http://maven.apache.org/POM/4.0.0"><version>'
                                   + version + '</version></project>')
                    with self.assertRaises(ValueError):
                        release_identity(pom)

    def test_cli_writes_exact_output_without_erasing_other_outputs(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            pom = Path(directory) / "pom.xml"
            output = Path(directory) / "outputs"
            pom.write_text('<project xmlns="http://maven.apache.org/POM/4.0.0"><version>4.0.0</version></project>')
            output.write_text("prior=kept\n")
            result = subprocess.run([sys.executable, str(SCRIPT), "--pom", str(pom),
                                     "--github-output", str(output)], capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            self.assertEqual("prior=kept\nversion=4.0.0\njar=SF_FinalTECH4.0.0.jar\n",
                             output.read_text())

    def test_workflow_downloads_artifact_from_exact_source(self) -> None:
        workflow = WORKFLOW.read_text()
        self.assertIn("python3 scripts/release_artifact_identity.py --github-output \"$GITHUB_OUTPUT\"", workflow)
        self.assertIn("name: ${{ steps.identity.outputs.jar }}", workflow)
        self.assertIn("test \"$CONFIRM_VERSION\" = \"$VERSION\"", workflow)
        self.assertNotIn("name: SF_FinalTECH3.0.16.jar", workflow)
        self.assertIn("github.event_name == 'workflow_dispatch'", workflow)
        self.assertIn("inputs.publish == true", workflow)


if __name__ == "__main__":
    unittest.main()
