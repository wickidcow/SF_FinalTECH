#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "src/main/java"

changed_files = []

for path in sorted(JAVA_ROOT.rglob("*.java")):
    source = path.read_text(encoding="utf-8")
    lines = source.splitlines(keepends=True)

    seen_imports = set()
    output = []
    changed = False

    for line in lines:
        normalized = line.rstrip("\r\n")
        if normalized.startswith("import "):
            if normalized in seen_imports:
                changed = True
                continue
            seen_imports.add(normalized)
        output.append(line)

    if changed:
        path.write_text("".join(output), encoding="utf-8")
        changed_files.append(path.relative_to(ROOT).as_posix())

print(f"Removed duplicate imports from {len(changed_files)} Java files.")
for path in changed_files:
    print(path)
