#!/usr/bin/env python3
"""Validate one disposable Folia readiness cycle, including negative assertions."""

from __future__ import annotations

import argparse
from pathlib import Path
import re
import sys


ANSI_RE = re.compile(r"\x1b\[[0-?]*[ -/]*[@-~]")
COLOR_RE = re.compile(r"\u00a7[0-9a-fk-orx]", re.IGNORECASE)
LOG_PREFIX_RE = re.compile(
    r"^\[\d{2}:\d{2}:\d{2}(?:\.\d+)?(?: [A-Z]+)?\]"
    r"(?: \[[^\]\r\n]+\])?:?\s*"
)
STACK_LINE_RE = re.compile(
    r"^(?:at\s+|Caused by:|Suppressed:|Exception in thread\b|\.\.\.\s+\d+\s+(?:more|common frames omitted)"
    r"|(?:[\w$]+\.)*[\w$]+(?:Exception|Error)(?::|$))"
)
THREAD_FAILURE_RE = re.compile(r"\b(?:IllegalStateException|UnsupportedOperationException|AsyncCatcher)\b")
FINALTECH_RE = re.compile(r"io\.taraxacum\.|finaltech", re.IGNORECASE)
FAILURE_CONTEXT_RE = re.compile(r"exception|error|fail|could not|task\s+#?\d+", re.IGNORECASE)
PLUGIN_FAILURE_RE = re.compile(
    r"Error occurred while (?:enabling|disabling) (?:Slimefun|FinalTECH(?:-Changed)?)\b"
    r"|Could not (?:load|initialize)[^\n]*(?:Slimefun|FinalTECH)",
    re.IGNORECASE,
)


def exception_blocks(text: str):
    """Keep a Java trace together even when the logger prefixes each stack line."""
    block: list[str] = []
    context = ""
    for line in text.splitlines():
        body = LOG_PREFIX_RE.sub("", line).strip()
        if STACK_LINE_RE.match(body):
            if not block and context:
                block.append(context)
            block.append(body)
            continue
        if block:
            yield "\n".join(block)
            block = []
        # Only an immediately preceding plugin failure can identify a trace whose
        # own frames do not name FinalTECH. Ordinary startup messages cannot.
        context = body if FINALTECH_RE.search(body) and FAILURE_CONTEXT_RE.search(body) else ""
        if FINALTECH_RE.search(body) and THREAD_FAILURE_RE.search(body):
            yield body
    if block:
        yield "\n".join(block)


def readiness_failures(
    text: str,
    slimefun_version: str,
    finaltech_version: str,
    require_clean: bool = False,
) -> list[str]:
    text = COLOR_RE.sub("", ANSI_RE.sub("", text))
    failures: list[str] = []
    required = [
        (r"\bDone \(", "server startup completion"),
        (rf"Enabling Slimefun v{re.escape(slimefun_version)}(?![\w.-])", "Slimefun enable record"),
        (rf"Enabling FinalTECH-Changed v{re.escape(finaltech_version)}(?![\w.-])", "FinalTECH enable record"),
        (r"Slimefun Upgrade Readiness", "Doctor readiness report"),
        (r"Stopping server", "normal shutdown record"),
    ]
    for pattern, description in required:
        if not re.search(pattern, text):
            failures.append(f"Missing {description}")

    statuses = re.findall(r"Overall status:[ \t]+([^\s]+)", text)
    if not statuses:
        failures.append("Missing Doctor overall status")
    elif re.search(r"Overall status:[ \t]+BLOCKED\b", text, re.IGNORECASE):
        failures.append("Doctor overall status is BLOCKED")
    if PLUGIN_FAILURE_RE.search(text):
        failures.append("Slimefun or FinalTECH plugin load/enable/disable failure")
    if any(FINALTECH_RE.search(block) and THREAD_FAILURE_RE.search(block) for block in exception_blocks(text)):
        failures.append("FinalTECH thread/region exception detected")
    if require_clean and not re.search(
        r"Storage:[ \t]+Ready[ \t]+\|[ \t]+previous shutdown[ \t]+Clean\b", text
    ):
        failures.append("Previous shutdown was not reported clean on the second boot")
    return failures


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("log", type=Path)
    parser.add_argument("--slimefun-version", required=True)
    parser.add_argument("--finaltech-version", required=True)
    parser.add_argument("--require-clean", action="store_true")
    args = parser.parse_args()
    try:
        failures = readiness_failures(
            args.log.read_text(encoding="utf-8"),
            args.slimefun_version,
            args.finaltech_version,
            args.require_clean,
        )
    except (OSError, UnicodeError) as exc:
        print(f"Could not read readiness log {args.log}: {exc}", file=sys.stderr)
        return 1
    if failures:
        for failure in failures:
            print(f"Folia readiness failed: {failure}", file=sys.stderr)
        return 1
    print(f"Folia readiness log checks passed: {args.log}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
