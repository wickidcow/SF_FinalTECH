from pathlib import Path

path = Path("src/main/resources/language/en-US.yml")
text = path.read_text(encoding="utf-8")

bad = "The transfer amount will not exceed the item's maximum stack size"
good = "The transfer amount will not exceed the item''s maximum stack size"

if bad not in text:
    raise SystemExit("Known malformed English YAML line was not found")

path.write_text(text.replace(bad, good), encoding="utf-8")
print("Repaired the FinalTECH English YAML apostrophe")
