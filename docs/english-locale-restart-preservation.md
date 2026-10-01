# Preserve the English locale across Bukkit save and restart

## Observed runtime defect

The four-addon integration run 36783227345 enabled FinalTECH on Paper 1.21.11, 26.2 and 26.3, but inspection of each second-boot log revealed `InvalidConfigurationException` while reading `plugins/FinalTECH-Changed/language/en-US.yml`. The existing full-stack smoke script checked enable/linkage errors and did not reject this configuration error, so its green result was not a clean-restart certificate.

The old ConfigFileManager attempted line-by-line apostrophe repair even when the complete YAML document was already valid. Bukkit can save single-quoted strings over multiple physical lines. Treating those lines as independent scalars corrupted valid saved language text; the subsequent permissive YAML loader could log the error and supply an empty configuration to update code.

## Scope of the correction

Parse the complete English locale first. If it is valid, leave every original byte unchanged, including folded strings, CRLF, comments and owner customization. Only invalid en-US.yml files enter the limited historical apostrophe-repair path. That candidate must parse as a complete document before any replacement is allowed.

The helper retains a byte-exact backup, stages the corrected file and uses atomic replacement when the filesystem supports it. Unrepairable structure or missing closing quotes leave the original intact and report failure rather than proceeding with an empty language configuration. It does not reset to bundled defaults, rename keys, convert other languages or attempt general arbitrary YAML reconstruction.

Only configuration-loading code changes. The completed Part 3 ticker migration, item/research IDs, recipes, menus, stored machine data, item formats and output rates remain unchanged. This is not an inventory/world-data migration or a new production dependency.

## Actual pre-promotion validation

[Run 36784084328](https://github.com/wickidcow/SF_FinalTECH/actions/runs/36784084328) built against the pinned Legacy test merge d600e077552baab2086a056d04b1cb0a6a9051a5, ran the retained English/storage/API guards and completed all 20 JUnit tests without failures/errors/skips: 12 new real Bukkit YAML cases plus the existing 8 ticker cases.

The negative control restored the original ConfigFileManager while retaining the new test. It reproduced exactly one assertion failure because a valid Bukkit-saved copy of the actual bundled English resource was modified. A compiler/setup failure was not accepted as the defect.

Tests cover whole-resource save/reload, folded quotes and comments, CRLF, known malformed apostrophes with exact backups, unchanged custom values and list order, repeated idempotent save cycles, valid double-quoted/literal blocks, rejection of unrepairable structure, no accidental cross-key quote repair, and no edits to unrelated or missing files. They exercise the actual manager entry point and native YAML parser, not a parallel test-only parser.

Downloaded evidence artifact 11129456081 has SHA-256 152ec64a16a65bc996828dd7629431c4b20156b2356eaadfe3ea004bb1b4ee34. Its XML and all three source blob hashes were independently checked. Temporary validation workflow and compressed source transports are excluded from this change.

The promoted commit must still pass its normal version/build matrix and a strengthened combined restart test that rejects configuration errors. File-repair tests are not a claim of protection against physical disk failures, arbitrary concurrent external edits, every malformed YAML dialect or a full old-world upgrade. A valid file is preserved; an invalid file outside the supported repair scope requires owner recovery from its backup.
