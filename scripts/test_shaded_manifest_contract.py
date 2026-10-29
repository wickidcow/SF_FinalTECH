#!/usr/bin/env python3
"""Guard the project manifest and signatures against broad Maven Shade filters."""
from pathlib import Path
import unittest
from xml.etree import ElementTree as ET

POM = Path(__file__).resolve().parents[1] / "pom.xml"
NS = {"m": "http://maven.apache.org/POM/4.0.0"}
EXPECTED = ["org.bstats:bstats-base", "org.bstats:bstats-bukkit"]
EXCLUDES = ["META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/MANIFEST.MF"]


def verify_shade_manifest_contract(source: str) -> None:
    root = ET.fromstring(source)
    shade = next((p for p in root.findall("m:build/m:plugins/m:plugin", NS)
                  if p.findtext("m:artifactId", namespaces=NS) == "maven-shade-plugin"), None)
    if shade is None:
        raise ValueError("Maven Shade must remain configured")
    filters = shade.findall("m:configuration/m:filters/m:filter", NS)
    artifacts = [f.findtext("m:artifact", namespaces=NS) for f in filters]
    if artifacts != EXPECTED:
        raise ValueError("Only the two bundled bStats dependencies may be filtered")
    for item in filters:
        excluded = [x.text for x in item.findall("m:excludes/m:exclude", NS)]
        if excluded != EXCLUDES:
            raise ValueError("Dependency signature/manifest filtering contract changed")
    if root.findtext("m:properties/m:maven.compiler.release", namespaces=NS) != "21":
        raise ValueError("FinalTECH must continue to emit Java 21 bytecode")
    if root.findtext("m:build/m:finalName", namespaces=NS) != "SF_FinalTECH${project.version}":
        raise ValueError("Canonical FinalTECH raw JAR name changed")


class ShadeManifestContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self.source = POM.read_text(encoding="utf-8")

    def test_current_manifest_filter_is_dependency_scoped(self):
        verify_shade_manifest_contract(self.source)

    def test_wildcard_filter_cannot_strip_project_manifest(self):
        with self.assertRaises(ValueError):
            verify_shade_manifest_contract(
                self.source.replace("<artifact>org.bstats:bstats-base</artifact>", "<artifact>*:*</artifact>"))

    def test_missing_bstats_base_exclusion_fails_closed(self):
        with self.assertRaises(ValueError):
            verify_shade_manifest_contract(
                self.source.replace("<artifact>org.bstats:bstats-base</artifact>", "<artifact>org.bstats:other</artifact>"))

    def test_missing_manifest_or_signature_exclusion_fails_closed(self):
        for missing in ("META-INF/MANIFEST.MF", "META-INF/*.RSA"):
            with self.subTest(missing=missing), self.assertRaises(ValueError):
                verify_shade_manifest_contract(self.source.replace(f"<exclude>{missing}</exclude>", ""))

    def test_adding_plugin_yml_to_excluded_resources_fails_closed(self):
        with self.assertRaises(ValueError):
            verify_shade_manifest_contract(self.source.replace(
                "<exclude>META-INF/MANIFEST.MF</exclude>",
                "<exclude>META-INF/MANIFEST.MF</exclude><exclude>plugin.yml</exclude>"))

    def test_java21_and_release_identity_are_not_weakened(self):
        for old, new in (
            ("<maven.compiler.release>21</maven.compiler.release>", "<maven.compiler.release>25</maven.compiler.release>"),
            ("SF_FinalTECH${project.version}", "FinalTECH${project.version}")
        ):
            with self.subTest(old=old), self.assertRaises(ValueError):
                verify_shade_manifest_contract(self.source.replace(old, new))


if __name__ == "__main__":
    unittest.main()
