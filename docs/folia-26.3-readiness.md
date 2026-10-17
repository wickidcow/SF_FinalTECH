# FinalTECH Folia 26.3 experimental readiness

**Status: test-only readiness work. Production descriptor still does not opt into Folia.**

FinalTECH already contains substantial region-ownership hardening for Slimefun Legacy's
Folia boundary: location-owned ticks, point/location/line/mesh cargo boundary checks,
same-location ticker-data machines, Matrix Reactor scheduling, delayed location
transitions, and region-owned particle scheduling.

This readiness lane deliberately does **not** add `folia-supported: true` to the
shipping `plugin.yml`.

## How the test works

CI builds the normal FinalTECH release JAR first and records its SHA-256. A helper then
creates a disposable copy of that JAR and adds only:

`folia-supported: true`

to the copied `plugin.yml`.

The helper refuses to run if the production descriptor ever contains a Folia declaration
and verifies that the source release JAR bytes are not modified while producing the
probe copy.

The runtime test pairs that disposable FinalTECH probe with exact validated Slimefun
Legacy 4.1.75 source `6f5aa31302eb46d2afe8f77e27fa9e8ab90f7ee2`, downloads an official
Folia 26.3 server build, and boots twice.

The gate requires:

- existing FinalTECH Part 3 region-ownership verifiers to pass;
- Slimefun and FinalTECH to both enable;
- Folia to reach normal startup on both cycles;
- `/sf doctor upgrade` to run without a BLOCKED result;
- no FinalTECH enable/load failure;
- no detected FinalTECH region/thread `IllegalStateException`, `UnsupportedOperationException`,
  or AsyncCatcher signal;
- normal shutdown on both cycles;
- Slimefun storage to report the previous shutdown clean on the second boot.

The runtime log verifier requires a complete Doctor status and fails explicitly on
BLOCKED results or plugin load/enable/disable errors. It checks FinalTECH-owned Java
exception traces across lines, including nested causes and logger-prefixed frames,
without assigning a separate plugin's exception to FinalTECH. Offline fixtures run
before server startup and prove that these failures return a nonzero exit status.

## What passing means

A green test means the current FinalTECH binary can pass this limited startup/shutdown
readiness probe when loading is explicitly enabled in disposable CI bytes.

It does **not** establish:

- every FinalTECH machine or cargo topology is Folia-safe;
- cross-region transfer support;
- live-player menu safety;
- arbitrary region movement while a machine is active;
- compatibility with every Slimefun addon;
- populated historical-world safety.

The production descriptor should remain unadmitted until representative machine and
cargo gameplay is tested on Folia 26.3. If that later evidence is green, descriptor
opt-in should be a separate reviewed change.
