import unittest

from select_paper_26_3_api import select_stable_263


def metadata(versions):
    values = "".join(f"<version>{entry}</version>" for entry in versions)
    return f"<metadata><versioning><versions>{values}</versions></versioning></metadata>"


class SelectPaper263ApiTests(unittest.TestCase):

    def test_numeric_build_priority_even_when_maven_metadata_is_unsorted(self):
        text = metadata(["26.3.build.205-stable", "26.3.build.8-stable",
                         "26.3.build.194-stable", "26.3.build.220-stable",
                         "26.3.build.207-stable"])
        self.assertEqual("26.3.build.220-stable", select_stable_263(text))

    def test_release_candidate_alpha_and_other_versions_are_never_promoted(self):
        text = metadata(["26.3.build.205-stable", "26.3-rc-3.build.1-alpha",
                         "26.3.build.400-alpha", "26.3.build.400-rc",
                         "26.30.build.500-stable", "26.2.build.600-stable",
                         "27.1.build.99-stable"])
        self.assertEqual("26.3.build.205-stable", select_stable_263(text))

    def test_missing_stable_version_fails_closed(self):
        for entries in ([], ["26.3-rc-3.build.1-alpha"],
                        ["26.2.build.100-stable"], ["26.3.build.0-stable"]):
            with self.subTest(entries=entries):
                with self.assertRaisesRegex(ValueError, "No 26.3.build.N-stable"):
                    select_stable_263(metadata(entries))

    def test_rejects_malformed_metadata(self):
        with self.assertRaises(Exception):
            select_stable_263("<metadata><versioning>")


if __name__ == "__main__":
    unittest.main()
