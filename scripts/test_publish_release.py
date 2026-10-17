from __future__ import annotations

import base64
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).with_name("publish_release.sh").resolve()
SOURCE = "a" * 40
OTHER_SOURCE = "b" * 40
NAME = "SF_FinalTECH3.0.16.jar"
ARTIFACT = b"the validated candidate bytes"
FAKE_CLI = r'''#!/usr/bin/env python3
import base64, json, os, pathlib, sys
tool = pathlib.Path(sys.argv[0]).name
args = sys.argv[1:]
state_path = pathlib.Path(os.environ['PUBLISH_TEST_STATE'])
state = json.loads(state_path.read_text())
with pathlib.Path(os.environ['PUBLISH_TEST_CALLS']).open('a') as stream:
    stream.write(json.dumps({'tool': tool, 'args': args}) + '\n')
if '--force' in args or '--clobber' in args or args[:2] == ['release', 'edit']:
    raise SystemExit('Mutating existing release content is forbidden')
if tool == 'git':
    if args[:2] == ['ls-remote', '--heads']:
        print(state['main'] + '\trefs/heads/main')
    elif args[:2] == ['ls-remote', '--refs']:
        if state['tag']:
            print(state['tag'] + '\trefs/tags/v3.0.16')
    elif args[0] == 'fetch':
        pass
    elif args[0] == 'rev-parse':
        print(state['tag'])
    elif args[0] == 'tag':
        if state['tag']:
            raise SystemExit('Tag already exists')
        state['tag'] = args[2]
    elif args[0] == 'push':
        pass
    else:
        raise SystemExit('Unexpected git command: ' + repr(args))
elif tool == 'gh':
    if args[:2] == ['release', 'view']:
        if not state['release']:
            raise SystemExit(1)
        if state['asset'] is not None:
            print('SF_FinalTECH3.0.16.jar')
    elif args[:2] == ['release', 'download']:
        directory = pathlib.Path(args[args.index('--dir') + 1])
        (directory / 'SF_FinalTECH3.0.16.jar').write_bytes(base64.b64decode(state['asset']))
    elif args[:2] == ['release', 'upload']:
        if state['asset'] is not None:
            raise SystemExit('Asset already exists')
        state['asset'] = base64.b64encode(pathlib.Path(args[3]).read_bytes()).decode()
    elif args[:2] == ['release', 'create']:
        if state['release']:
            raise SystemExit('Release already exists')
        assert '--verify-tag' in args
        assert args[args.index('--target') + 1] == state['tag']
        state['release'] = True
        state['asset'] = base64.b64encode(pathlib.Path(args[3]).read_bytes()).decode()
        state['notes'] = pathlib.Path(args[args.index('--notes-file') + 1]).read_text()
    else:
        raise SystemExit('Unexpected gh command: ' + repr(args))
else:
    raise SystemExit('Unexpected tool')
state_path.write_text(json.dumps(state))
'''


class TestImmutablePublication(unittest.TestCase):
    def setUp(self) -> None:
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.bin = self.root / "bin"
        self.bin.mkdir()
        for name in ("git", "gh"):
            path = self.bin / name
            path.write_text(FAKE_CLI)
            path.chmod(0o755)
        self.jar = self.root / NAME
        self.jar.write_bytes(ARTIFACT)
        self.notes = self.root / "notes.md"
        self.notes.write_text("# FinalTECH 3.0.16\n\nReviewed release notes.\n")
        self.state_path = self.root / "state.json"
        self.calls_path = self.root / "calls.jsonl"
        self.state = {"main": SOURCE, "tag": None, "release": False, "asset": None}
        self.env = dict(os.environ, PATH=str(self.bin) + os.pathsep + os.environ["PATH"],
                        GITHUB_EVENT_NAME="push", GITHUB_REF="refs/heads/main", GITHUB_SHA=SOURCE,
                        GITHUB_REPOSITORY="wickidcow/SF_FinalTECH", RUNNER_TEMP=str(self.root),
                        PUBLISH_TEST_STATE=str(self.state_path), PUBLISH_TEST_CALLS=str(self.calls_path))

    def publish(self) -> subprocess.CompletedProcess[str]:
        self.state_path.write_text(json.dumps(self.state))
        result = subprocess.run(["bash", str(SCRIPT), str(self.jar), str(self.notes), "3.0.16"],
                                env=self.env, text=True, capture_output=True)
        self.state = json.loads(self.state_path.read_text())
        return result

    def calls(self) -> list[dict[str, object]]:
        if not self.calls_path.exists():
            return []
        return [json.loads(line) for line in self.calls_path.read_text().splitlines()]

    def mutations(self) -> list[dict[str, object]]:
        return [call for call in self.calls()
                if (call["tool"] == "git" and call["args"][0] in ("tag", "push"))
                or (call["tool"] == "gh" and call["args"][1] in ("create", "upload", "edit"))]

    def existing_release(self, artifact: bytes | None = ARTIFACT) -> None:
        self.state.update(tag=SOURCE, release=True,
                          asset=None if artifact is None else base64.b64encode(artifact).decode())

    def test_current_main_creates_source_bound_release_with_reviewed_notes(self) -> None:
        result = self.publish()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(SOURCE, self.state["tag"])
        self.assertEqual(ARTIFACT, base64.b64decode(self.state["asset"]))
        self.assertIn("Reviewed release notes.", self.state["notes"])
        self.assertIn("/commit/" + SOURCE, self.state["notes"])
        self.assertIn("Artifact SHA-256:", self.state["notes"])
        self.assertEqual(3, len(self.mutations()))

    def test_pr_manual_and_other_branches_cannot_publish(self) -> None:
        for event, ref in [("pull_request", "refs/heads/main"), ("workflow_dispatch", "refs/heads/main"),
                           ("push", "refs/heads/topic"), ("push", "refs/tags/v3.0.16")]:
            with self.subTest(event=event, ref=ref):
                self.env.update(GITHUB_EVENT_NAME=event, GITHUB_REF=ref)
                result = self.publish()
                self.assertNotEqual(0, result.returncode)
                self.assertEqual([], self.calls())

    def test_stale_main_cannot_create_a_tag_or_release(self) -> None:
        self.state["main"] = OTHER_SOURCE
        result = self.publish()
        self.assertNotEqual(0, result.returncode)
        self.assertIn("Main advanced", result.stderr)
        self.assertEqual([], self.mutations())

    def test_existing_tag_cannot_move_to_a_different_source(self) -> None:
        self.existing_release()
        self.state["tag"] = OTHER_SOURCE
        result = self.publish()
        self.assertNotEqual(0, result.returncode)
        self.assertIn("another source", result.stderr)
        self.assertEqual(OTHER_SOURCE, self.state["tag"])
        self.assertEqual([], self.mutations())

    def test_identical_retry_only_reads_existing_release(self) -> None:
        self.existing_release()
        result = self.publish()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual([], self.mutations())

    def test_different_existing_asset_is_never_replaced(self) -> None:
        self.existing_release(b"original stable bytes")
        result = self.publish()
        self.assertNotEqual(0, result.returncode)
        self.assertIn("differs from this build", result.stderr)
        self.assertEqual(b"original stable bytes", base64.b64decode(self.state["asset"]))
        self.assertEqual([], self.mutations())

    def test_partial_release_can_add_only_its_missing_asset(self) -> None:
        self.existing_release(None)
        result = self.publish()
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(ARTIFACT, base64.b64decode(self.state["asset"]))
        self.assertEqual(1, len(self.mutations()))
        self.assertEqual(["release", "upload"], self.mutations()[0]["args"][:2])


if __name__ == "__main__":
    unittest.main()
