# FinalTECH Paper 26.3 stable API selection and immutable core source

This is build/publishing reproducibility work only; it never changes gameplay, recipes, player data or the existing FinalTECH v3.0.16 release.

Previously the release workflow selected `versions[-1]` among every Maven version beginning with `26.3`. Maven metadata order is not a stable-version contract, and it could include obsolete `26.3-rc-3.build.1-alpha` or 26.3 prereleases. The new `select_paper_26_3_api.py` accepts only `26.3.build.N-stable` and chooses the numerically highest N. It fails closed if the API repository publishes no matching stable version; never silently falls back to an alpha or arbitrary other Minecraft line.

Four offline unit cases guard unsorted metadata, candidate/alpha/RC rejection, no-stable failure and malformed XML; run before resolving Maven metadata. CI persists its selected version in `GITHUB_ENV` to drive the existing Maven build. Latest runtime/compiler compatibility for future 26.4+ still requires separate evidence, not inference from this selector.

Also pin the Slimefun Legacy compile/source input to the immutable v4.1.78 published source commit `b14a3bcc5ef221c013f93d49b6b3a1bbdb3101e9` instead of mutable `master`, verifying the exact commit checked out. Keep Java 21 target, 1.21.11 baseline and existing 26.2 compiler gates.

The user explicitly prioritizes 26.3+ but requires that 1.21.11 and 26.2/26.1.2 compatibility be preserved, and that future version bumps only follow exact-head validation and immutable release-asset review.

## Manual release gate

Builds on ordinary `main` pushes and pull requests are now read-only and never publish. New releases require a separate write-scoped job reached only through a current-main `workflow_dispatch` with `publish=true` and matching `confirm_version`. The job obtains the exact tested raw JAR by `download-artifact@v8` / `skip-decompress: true` and calls the same immutable `scripts/publish_release.sh` guard. The script independently refuses unconfirmed dispatches, stale main, differing previous tags/bytes and invalid artifacts. The fake-CLI test suite includes a wrong-confirmation failure case. No 3.0.16 release assets are touched.
