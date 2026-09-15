from pathlib import Path
import argparse

LOCALE = Path("src/main/resources/language/en-US.yml")


def escape_single_quoted_body(body: str) -> str:
    result: list[str] = []
    i = 0
    while i < len(body):
        if body[i] != "'":
            result.append(body[i])
            i += 1
            continue

        run_start = i
        while i < len(body) and body[i] == "'":
            i += 1

        run_length = i - run_start
        if run_length % 2 == 0:
            result.append("'" * run_length)
        elif run_length == 1:
            result.append("''")
        else:
            # Old 3.0 locale files could contain malformed runs such as '''.
            # Remove the unmatched quote instead of expanding the run and
            # displaying an extra apostrophe after SnakeYAML decodes it.
            result.append("'" * (run_length - 1))

    return "".join(result)


def repair_line(line: str) -> str:
    list_marker = line.find("- '")
    value_marker = line.find(": '")

    if list_marker >= 0:
        scalar_start = list_marker + 2
    elif value_marker >= 0:
        scalar_start = value_marker + 2
    else:
        return line

    if scalar_start >= len(line) or line[scalar_start] != "'":
        return line

    scalar_end = line.rfind("'")
    if scalar_end <= scalar_start:
        return line

    body = line[scalar_start + 1 : scalar_end]
    repaired_body = escape_single_quoted_body(body)
    if repaired_body == body:
        return line

    return line[: scalar_start + 1] + repaired_body + line[scalar_end:]


def main() -> None:
    parser = argparse.ArgumentParser(description="Normalize apostrophes in FinalTECH English YAML single-quoted scalars.")
    parser.add_argument("--check", action="store_true", help="Fail instead of modifying the locale if normalization is needed.")
    args = parser.parse_args()

    source_lines = LOCALE.read_text(encoding="utf-8").splitlines()
    repaired_lines = [repair_line(line) for line in source_lines]

    changed = [index + 1 for index, (before, after) in enumerate(zip(source_lines, repaired_lines)) if before != after]
    if not changed:
        print("English YAML quoting is normalized.")
        return

    if args.check:
        print("English YAML contains unescaped apostrophes on line(s): " + ", ".join(map(str, changed)))
        raise SystemExit(1)

    LOCALE.write_text("\n".join(repaired_lines) + "\n", encoding="utf-8")
    print("Normalized English YAML apostrophes on line(s): " + ", ".join(map(str, changed)))


if __name__ == "__main__":
    main()
