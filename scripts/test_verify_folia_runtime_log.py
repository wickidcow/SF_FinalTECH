#!/usr/bin/env python3

from __future__ import annotations

from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

import verify_folia_runtime_log as verifier


VALID_LOG = """[12:00:00 INFO]: [Slimefun] Enabling Slimefun v4.1.75
[12:00:00 INFO]: [FinalTECH-Changed] Enabling FinalTECH-Changed v3.0.15
[12:00:01 INFO]: Done (1.23s)! For help, type "help"
[12:00:02 INFO]: Slimefun Upgrade Readiness
[12:00:02 INFO]: Overall status: READY
[12:00:02 INFO]: Storage: Ready | previous shutdown Clean
[12:00:03 INFO]: Stopping server
"""


class TestFoliaRuntimeLog(unittest.TestCase):
    def failures(self, text: str, require_clean: bool = False) -> list[str]:
        return verifier.readiness_failures(text, "4.1.75", "3.0.15", require_clean)

    def test_clean_cycles_pass(self) -> None:
        self.assertEqual(self.failures(VALID_LOG), [])
        self.assertEqual(self.failures(VALID_LOG, require_clean=True), [])
        self.assertEqual(self.failures(VALID_LOG.replace("previous shutdown Clean", "previous shutdown Unknown")), [])

    def test_console_colors_do_not_hide_status(self) -> None:
        self.assertEqual(self.failures(VALID_LOG.replace("READY", "\x1b[32m\u00a7aREADY\x1b[0m")), [])
        blocked = VALID_LOG.replace("READY", "\x1b[31m\u00a7cBLOCKED\x1b[0m")
        self.assertIn("Doctor overall status is BLOCKED", self.failures(blocked))

    def test_every_required_record_is_enforced(self) -> None:
        for line in VALID_LOG.splitlines():
            if "Storage:" in line:
                continue
            with self.subTest(line=line):
                self.assertTrue(self.failures(VALID_LOG.replace(line + "\n", "")))

    def test_exact_plugin_versions_are_required(self) -> None:
        for old, new in [("4.1.75", "4.1.750"), ("3.0.15", "3.0.15-rc1")]:
            with self.subTest(new=new):
                self.assertTrue(self.failures(VALID_LOG.replace(old, new)))

    def test_blocked_status_cannot_be_hidden_by_a_later_ready_report(self) -> None:
        blocked = VALID_LOG + "[12:00:04 INFO]: Overall status: BLOCKED\n" + VALID_LOG
        self.assertIn("Doctor overall status is BLOCKED", self.failures(blocked))

    def test_blocked_status_with_punctuation_is_rejected(self) -> None:
        self.assertIn("Doctor overall status is BLOCKED", self.failures(VALID_LOG.replace("READY", "BLOCKED, see details")))

    def test_plugin_failures_are_rejected_after_done(self) -> None:
        for message in [
            "Error occurred while enabling Slimefun v4.1.75",
            "Error occurred while enabling FinalTECH-Changed v3.0.15",
            "Error occurred while disabling FinalTECH-Changed v3.0.15",
            "Could not load 'plugins/SF_FinalTECH-FoliaProbe.jar'",
            "Could not initialize plugin Slimefun",
        ]:
            with self.subTest(message=message):
                self.assertIn(
                    "Slimefun or FinalTECH plugin load/enable/disable failure",
                    self.failures(VALID_LOG + f"[12:00:04 ERROR]: {message}\n"),
                )

    def test_multiline_finaltech_thread_exceptions_are_rejected(self) -> None:
        for exception in ["IllegalStateException", "UnsupportedOperationException"]:
            for prefix in ["", "[12:00:04 WARN]: ", "[12:00:04] [Region Scheduler Thread #3/ERROR]: "]:
                with self.subTest(exception=exception, prefix=prefix):
                    trace = "\n".join(prefix + line for line in [
                        f"java.lang.{exception}: incorrect region owner",
                        "\tat io.papermc.paper.threadedregions.TickRegionScheduler.checkTickThread(TickRegionScheduler.java:42)",
                        "\tat io.taraxacum.finaltech.core.SomeMachine.tick(SomeMachine.java:23)",
                    ])
                    self.assertIn("FinalTECH thread/region exception detected", self.failures(VALID_LOG + trace))

    def test_nested_async_catcher_and_suppressed_traces_are_rejected(self) -> None:
        for nesting in ["Caused by:", "Suppressed:"]:
            with self.subTest(nesting=nesting):
                trace = f"""java.util.concurrent.CompletionException: task failed
\tat java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:42)
{nesting} java.lang.IllegalStateException: Asynchronous entity access!
\tat org.spigotmc.AsyncCatcher.catchOp(AsyncCatcher.java:23)
\tat io.taraxacum.finaltech.core.SomeMachine.tick(SomeMachine.java:23)
\t... 3 more
"""
                self.assertIn("FinalTECH thread/region exception detected", self.failures(VALID_LOG + trace))

    def test_plugin_failure_context_identifies_a_trace_without_owned_frames(self) -> None:
        trace = """[12:00:04 WARN]: [FinalTECH-Changed] Task #42 generated an exception
java.lang.UnsupportedOperationException: use region scheduler
\tat io.papermc.paper.threadedregions.scheduler.FoliaScheduler.run(FoliaScheduler.java:42)
"""
        self.assertIn("FinalTECH thread/region exception detected", self.failures(VALID_LOG + trace))

    def test_single_line_finaltech_exception_is_rejected(self) -> None:
        trace = "[12:00:04 ERROR]: [FinalTECH-Changed] java.lang.IllegalStateException: bad region\n"
        self.assertIn("FinalTECH thread/region exception detected", self.failures(VALID_LOG + trace))

    def test_uncaught_thread_exception_is_rejected(self) -> None:
        trace = 'Exception in thread "Region Scheduler Thread #3" java.lang.IllegalStateException: bad owner\n'
        trace += "\tat io.taraxacum.finaltech.core.SomeMachine.tick(SomeMachine.java:23)\n"
        self.assertIn("FinalTECH thread/region exception detected", self.failures(VALID_LOG + trace))

    def test_unrelated_exception_is_not_assigned_to_finaltech(self) -> None:
        trace = """[12:00:04 INFO]: [FinalTECH-Changed] startup complete
[12:00:05 ERROR]: [UnrelatedPlugin] scheduled task failed
java.lang.IllegalStateException: unrelated task
\tat example.unrelated.Task.run(Task.java:42)
[12:00:06 INFO]: [FinalTECH-Changed] heartbeat
"""
        self.assertEqual(self.failures(VALID_LOG + trace), [])

    def test_separate_log_events_are_not_combined_into_a_fake_owned_exception(self) -> None:
        trace = """java.lang.IllegalStateException: unrelated task
\tat example.unrelated.Task.run(Task.java:42)
[12:00:04 ERROR]: another diagnostic
java.lang.IllegalArgumentException: an unrelated non-thread error
\tat io.taraxacum.finaltech.core.SomeMachine.tick(SomeMachine.java:23)
"""
        self.assertEqual(self.failures(VALID_LOG + trace), [])

    def test_second_boot_requires_clean_storage(self) -> None:
        self.assertIn(
            "Previous shutdown was not reported clean on the second boot",
            self.failures(VALID_LOG.replace("previous shutdown Clean", "previous shutdown Unknown"), True),
        )

    def test_command_exit_codes_enforce_failures(self) -> None:
        script = Path(verifier.__file__)
        with tempfile.TemporaryDirectory() as directory:
            log = Path(directory) / "cycle.log"
            command = [sys.executable, str(script), str(log), "--slimefun-version", "4.1.75",
                       "--finaltech-version", "3.0.15", "--require-clean"]
            for contents, expected in [
                (VALID_LOG, 0),
                (VALID_LOG.replace("READY", "BLOCKED"), 1),
                (VALID_LOG + "java.lang.IllegalStateException: bad region\n\tat io.taraxacum.Machine.tick(Machine.java:1)\n", 1),
                ("", 1),
            ]:
                with self.subTest(contents=contents):
                    log.write_text(contents, encoding="utf-8")
                    result = subprocess.run(command, capture_output=True, text=True, check=False)
                    self.assertEqual(result.returncode, expected, result.stderr)
            log.unlink()
            self.assertNotEqual(subprocess.run(command, capture_output=True, check=False).returncode, 0)


if __name__ == "__main__":
    unittest.main()
