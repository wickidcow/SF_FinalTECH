#!/usr/bin/env bash
# Publish a new stable asset only by confirmed main dispatch; never move an existing release.
set -euo pipefail

if [[ "${GITHUB_EVENT_NAME:-}" != workflow_dispatch || "${GITHUB_REF:-}" != refs/heads/main ]]; then
  echo 'Stable publication requires explicit workflow_dispatch from main.' >&2
  exit 1
fi

JAR="${1:?Usage: publish_release.sh <jar> <notes-file> <version>}"
NOTES="${2:?Release notes file is required}"
VERSION="${3:?Release version is required}"
[[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]
if [[ "${CONFIRM_VERSION:-}" != "$VERSION" ]]; then
  echo "Refusing publication: confirm_version must exactly match $VERSION" >&2
  exit 1
fi
[[ "${GITHUB_SHA:?}" =~ ^[0-9a-f]{40}$ ]]
test -n "${GITHUB_REPOSITORY:?}"
test -s "$JAR"
test -s "$NOTES"
NAME="$(basename "$JAR")"
[[ "$NAME" == "SF_FinalTECH${VERSION}.jar" ]]
TAG="v${VERSION}"

MAIN_SOURCE="$(git ls-remote --heads origin refs/heads/main | cut -f1)"
if [[ "$MAIN_SOURCE" != "$GITHUB_SHA" ]]; then
  echo 'Main advanced after this build; refusing stale publication.' >&2
  exit 1
fi

REMOTE_TAG="$(git ls-remote --refs origin "refs/tags/$TAG")"
if [[ -n "$REMOTE_TAG" ]]; then
  git fetch --no-tags origin "refs/tags/$TAG"
  TAG_SOURCE="$(git rev-parse 'FETCH_HEAD^{commit}')"
  if [[ "$TAG_SOURCE" != "$GITHUB_SHA" ]]; then
    echo "Immutable tag $TAG belongs to another source; bump the version." >&2
    exit 1
  fi
else
  git tag "$TAG" "$GITHUB_SHA"
  git push origin "refs/tags/$TAG"
fi

CHECK_DIR="$(mktemp -d "${RUNNER_TEMP:-${TMPDIR:-/tmp}}/finaltech-release.XXXXXX")"
trap 'rm -rf "$CHECK_DIR"' EXIT
SHA256="$(sha256sum "$JAR" | cut -d' ' -f1)"
cp "$NOTES" "$CHECK_DIR/release-notes.md"
printf '\n## Build provenance\n\n- Source: https://github.com/%s/commit/%s\n- Artifact SHA-256: `%s`\n' \
  "$GITHUB_REPOSITORY" "$GITHUB_SHA" "$SHA256" >> "$CHECK_DIR/release-notes.md"

if gh release view "$TAG" --repo "$GITHUB_REPOSITORY" --json assets --jq '.assets[].name' \
    > "$CHECK_DIR/assets.txt"; then
  if grep -Fxq "$NAME" "$CHECK_DIR/assets.txt"; then
    gh release download "$TAG" --repo "$GITHUB_REPOSITORY" --pattern "$NAME" --dir "$CHECK_DIR"
    if ! cmp -s "$JAR" "$CHECK_DIR/$NAME"; then
      echo "Immutable asset $NAME differs from this build; bump the version." >&2
      exit 1
    fi
    echo 'Existing release has the exact source and artifact bytes.'
  else
    # Recover a partial publication by adding its missing asset, without replacement.
    gh release upload "$TAG" "$JAR" --repo "$GITHUB_REPOSITORY"
  fi
else
  gh release create "$TAG" "$JAR" --repo "$GITHUB_REPOSITORY" --verify-tag \
    --target "$GITHUB_SHA" --title "SF_FinalTECH $TAG" --notes-file "$CHECK_DIR/release-notes.md"
fi
