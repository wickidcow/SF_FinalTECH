#!/usr/bin/env python3
"""Negative regression gates for exact-green-main FinalTECH automatic publisher."""
from __future__ import annotations

import hashlib
from pathlib import Path
import tempfile
import unittest
import zipfile

from verify_immutable_release_jar import verify

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/publish-green-main.yml"
BUILDER = ROOT / ".github/workflows/release.yml"


def verify_policy(source: str) -> None:
    required = (
        "workflows: ['Build and Publish FinalTECH JAR']",
        "branches: [main]",
        "github.event.workflow_run.conclusion == 'success'",
        "github.event.workflow_run.event == 'push'",
        "github.event.workflow_run.head_branch == 'main'",
        "github.event.workflow_run.head_repository.full_name == github.repository",
        "github.event.workflow_run.head_sha",
        'git/ref/heads/main" --jq',
        'test "$LIVE_SHA" = "$TESTED_SHA"',
        'test "$(git rev-parse HEAD)" = "$TESTED_SHA"',
        "for name in ('release',):",
        "hits[0]['conclusion'] != 'success'",
        "artifacts?per_page=100",
        "item.get('workflow_run', {}).get('head_sha') == sys.argv[3]",
        "sha256:[0-9a-f]{64}",
        'releases/tags/$TAG',
        'git/ref/tags/$TAG',
        "HTTP 404",
        "skip-decompress: true",
        "run-id: " + chr(36) + "{{ github.event.workflow_run.id }}",
        'gh api --method POST "repos/$GITHUB_REPOSITORY/git/refs"',
        'gh release create "$TAG" "dist/$NAME" --verify-tag',
        'gh release download "$TAG" --repo "$GITHUB_REPOSITORY"',
        "docs/releases/$VERSION.md",
    )
    for fragment in required:
        if fragment not in source:
            raise ValueError("Required exact-source immutable release gate missing: " + fragment)
    if source.count("python3 scripts/verify_immutable_release_jar.py") != 2:
        raise ValueError("Require raw JAR validation before and after GitHub release download")
    if source.index("releases/tags/$TAG") > source.index('gh release create "$TAG"'):
        raise ValueError("Must reject existing releases before any tag is created")
    if source.index("git/ref/tags/$TAG") > source.index('gh release create "$TAG"'):
        raise ValueError("Orphan tag must be rejected before new tag")
    for forbidden in ("gh release upload", "--clobber", "gh release edit",
                      "gh release delete", "git tag -f", "git push --force"):
        if forbidden in source:
            raise ValueError("Cannot mutate prior published release: " + forbidden)


class VerifiedPublisherTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.publisher = WORKFLOW.read_text(encoding="utf-8")

    def test_exact_green_main_publisher(self):
        verify_policy(self.publisher)

    def test_artifact_not_from_same_run(self):
        with self.assertRaises(ValueError):
            verify_policy(self.publisher.replace(
                "item.get('workflow_run', {}).get('head_sha') == sys.argv[3]",
                "True", 1))

    def test_wrong_build_job_rejected(self):
        with self.assertRaises(ValueError):
            verify_policy(self.publisher.replace(
                "for name in ('release',):",
                "for name in ('build',):", 1))

    def test_must_verify_master_sha_before_tagging(self):
        with self.assertRaises(ValueError):
            verify_policy(self.publisher.replace(
                'test "$(git rev-parse HEAD)" = "$TESTED_SHA"', "true", 1))

    def test_old_asset_clobber_forbidden(self):
        with self.assertRaises(ValueError):
            verify_policy(self.publisher.replace(
                'gh release create "$TAG" "dist/$NAME" --verify-tag',
                'gh release upload "$TAG" "dist/$NAME" --clobber', 1))

    def test_pinned_maven_archive_fallback_preserves_sha512(self):
        installer = (ROOT / "scripts/install_maven_3_10.sh").read_text(encoding="utf-8")
        self.assertIn("archive.apache.org/dist/maven/maven-3/", installer)
        self.assertIn("sha512sum --check --status", installer)
        self.assertIn("908b1501bfb420bf7c8affb855534a9c", installer)

    def test_builder_can_produce_exact_run_artifact(self):
        builder = BUILDER.read_text(encoding="utf-8")
        self.assertIn("name: Build and Publish FinalTECH JAR", builder)
        self.assertIn("name: " + chr(36) + "{{ steps.version.outputs.jar }}", builder)
        self.assertIn("archive: false", builder)
        self.assertIn(
            "python3 -m unittest discover -s scripts -p 'test_verified_green_main_publisher.py' -v",
            builder)


class RawJarTest(unittest.TestCase):
    @staticmethod
    def fixture(directory: Path, major: int = 65) -> Path:
        path = directory / "SF_FinalTECH3.0.17.jar"
        header = b"\xca\xfe\xba\xbe\x00\x00" + major.to_bytes(2, "big")
        with zipfile.ZipFile(path, "w") as z:
            z.writestr("plugin.yml",
                       "name: FinalTECH-Changed\nversion: 3.0.17\n"
                       "main: io.taraxacum.finaltech.FinalTechChanged\n")
            z.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n")
            z.writestr("io/taraxacum/finaltech/FinalTechChanged.class", header)
            z.writestr("io/taraxacum/finaltech/shaded/bstats/Metrics.class", header)
        return path

    def test_valid_synthetic_jar(self):
        with tempfile.TemporaryDirectory() as d:
            jar = self.fixture(Path(d))
            verify(jar, "3.0.17", hashlib.sha256(jar.read_bytes()).hexdigest())

    def test_source_version_mismatch_fails(self):
        with tempfile.TemporaryDirectory() as d:
            jar = self.fixture(Path(d))
            with self.assertRaises(ValueError):
                verify(jar, "3.0.16", hashlib.sha256(jar.read_bytes()).hexdigest())

    def test_wrong_digest_fails(self):
        with tempfile.TemporaryDirectory() as d:
            jar = self.fixture(Path(d))
            with self.assertRaisesRegex(ValueError, "SHA-256 mismatch"):
                verify(jar, "3.0.17", "0"*64)

    def test_class_newer_than_java21_fails(self):
        with tempfile.TemporaryDirectory() as d:
            jar = self.fixture(Path(d), major=69)
            with self.assertRaisesRegex(ValueError, "Java 21"):
                verify(jar, "3.0.17", hashlib.sha256(jar.read_bytes()).hexdigest())


if __name__ == "__main__":
    unittest.main()
