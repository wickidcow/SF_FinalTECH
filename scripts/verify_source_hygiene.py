#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "src/main/java"

violations: list[str] = []

for path in sorted(JAVA_ROOT.rglob("*.java")):
    relative = path.relative_to(ROOT).as_posix()
    lines = path.read_text(encoding="utf-8").splitlines()

    seen_imports: set[str] = set()
    for line_number, line in enumerate(lines, 1):
        if not line.startswith("import "):
            continue
        if line in seen_imports:
            violations.append(
                f"{relative}:{line_number} duplicates import: {line}"
            )
        else:
            seen_imports.add(line)

for stale_path in (
    "dependency-reduced-pom.xml",
    "FinalTECH-Fixed.iml",
    "src/main/java/io/taraxacum/common/Test.java",
    "src/main/java/io/taraxacum/finaltech/core/test",
    "src/main/java/io/taraxacum/finaltech/setup/TemplateParser.java",
    "src/main/java/io/taraxacum/finaltech/core/exception/TemplateParserErrorException.java",
    "src/main/resources/template.yml",
    ".github/workflows/apply-26.3-compat.yml",
):
    if (ROOT / stale_path).exists():
        violations.append(f"stale development artifact returned: {stale_path}")


main_plugin = (JAVA_ROOT / "io/taraxacum/finaltech/FinalTechChanged.java").read_text(encoding="utf-8")
if 'return "???";' in main_plugin:
    violations.append("FinalTechChanged#getBugTrackerURL must point to the maintained issue tracker")
if "e.printStackTrace();" in main_plugin:
    violations.append("FinalTechChanged must use structured plugin logging instead of printStackTrace")

if violations:
    raise SystemExit(
        "FinalTECH source hygiene violations:\n- " + "\n- ".join(violations)
    )

print("FinalTECH source hygiene: PASS")
