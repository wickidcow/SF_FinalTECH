#!/usr/bin/env bash
set -euo pipefail

SLIMEFUN_JAR="${1:?Usage: folia_26_3_runtime_smoke.sh <slimefun-jar> <finaltech-probe-jar> [work-dir]}"
FINALTECH_JAR="${2:?Usage: folia_26_3_runtime_smoke.sh <slimefun-jar> <finaltech-probe-jar> [work-dir]}"
WORK_DIR="${3:-build/folia-26.3-runtime-smoke}"
MC_VERSION="26.3"
STARTUP_TIMEOUT="${SERVER_SMOKE_STARTUP_TIMEOUT:-300}"
SHUTDOWN_TIMEOUT="${SERVER_SMOKE_SHUTDOWN_TIMEOUT:-90}"
USER_AGENT="${SERVER_DOWNLOAD_USER_AGENT:-SF-FinalTECH-Folia-26.3-Readiness/1.0}"

for cmd in curl jq java unzip; do
  command -v "$cmd" >/dev/null 2>&1 || { echo "Missing required command: $cmd" >&2; exit 1; }
done

test -s "$SLIMEFUN_JAR"
test -s "$FINALTECH_JAR"

SLIMEFUN_VERSION="$(unzip -p "$SLIMEFUN_JAR" plugin.yml | sed -n "s/^version:[[:space:]]*['\"]*\([^'\"]*\)['\"]*$/\1/p" | head -n1 | xargs)"
FINALTECH_VERSION="$(unzip -p "$FINALTECH_JAR" plugin.yml | sed -n "s/^version:[[:space:]]*['\"]*\([^'\"]*\)['\"]*$/\1/p" | head -n1 | xargs)"
test -n "$SLIMEFUN_VERSION"
test -n "$FINALTECH_VERSION"
unzip -p "$FINALTECH_JAR" plugin.yml | grep -Eiq '^folia-supported:[[:space:]]*true[[:space:]]*$'

download() {
  local url="$1"
  local output="${2:-}"
  local tmp
  if [[ -n "$output" ]]; then
    tmp="${output}.part"
    rm -f "$tmp"
    curl --fail --silent --show-error --location --retry 5 --retry-all-errors       --connect-timeout 20 --max-time 180 -A "$USER_AGENT" "$url" -o "$tmp"
    test -s "$tmp"
    mv "$tmp" "$output"
  else
    curl --fail --silent --show-error --location --retry 5 --retry-all-errors       --connect-timeout 20 --max-time 180 -A "$USER_AGENT" "$url"
  fi
}

BUILDS_URL="https://fill.papermc.io/v3/projects/folia/versions/${MC_VERSION}/builds"
BUILDS="$(download "$BUILDS_URL")"
if jq -e '.ok == false' >/dev/null 2>&1 <<<"$BUILDS"; then
  jq -r '.message // "Folia downloads service returned an error"' <<<"$BUILDS" >&2
  exit 1
fi
SERVER_URL="$(jq -r 'max_by(.id) | .downloads."server:default".url // empty' <<<"$BUILDS")"
SERVER_BUILD="$(jq -r 'max_by(.id) | .id // empty' <<<"$BUILDS")"
SERVER_CHANNEL="$(jq -r 'max_by(.id) | .channel // "unknown"' <<<"$BUILDS")"
test -n "$SERVER_URL"
test -n "$SERVER_BUILD"

rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR/plugins/bStats"
printf 'enabled: false\n' > "$WORK_DIR/plugins/bStats/config.yml"
cp "$SLIMEFUN_JAR" "$WORK_DIR/plugins/Slimefun-Legacy.jar"
cp "$FINALTECH_JAR" "$WORK_DIR/plugins/SF_FinalTECH-FoliaProbe.jar"
printf 'eula=true\n' > "$WORK_DIR/eula.txt"
cat > "$WORK_DIR/server.properties" <<'PROPS'
online-mode=false
level-name=folia-readiness-world
max-players=1
spawn-protection=0
view-distance=2
simulation-distance=2
pause-when-empty-seconds=-1
enable-query=false
enable-rcon=false
motd=FinalTECH Folia 26.3 readiness
PROPS

download "$SERVER_URL" "$WORK_DIR/server.jar"

printf 'Software: Folia\nMinecraft: %s\nBuild: %s\nChannel: %s\nDownload: %s\nJava: %s\nSlimefun: %s\nFinalTECH: %s\n'   "$MC_VERSION" "$SERVER_BUILD" "$SERVER_CHANNEL" "$SERVER_URL" "$(java -version 2>&1 | head -n1)"   "$SLIMEFUN_VERSION" "$FINALTECH_VERSION" > "$WORK_DIR/runtime-build.txt"

stop_server() {
  local pid="$1" fd="$2"
  if kill -0 "$pid" >/dev/null 2>&1; then
    printf 'stop\n' >&"$fd" || true
  fi
  local deadline=$((SECONDS + SHUTDOWN_TIMEOUT))
  while kill -0 "$pid" >/dev/null 2>&1 && (( SECONDS < deadline )); do sleep 1; done
  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "Folia did not stop cleanly within ${SHUTDOWN_TIMEOUT}s" >&2
    kill "$pid" >/dev/null 2>&1 || true
    sleep 2
  fi
}

cycle() {
  local label="$1" require_clean="$2"
  local fifo="$WORK_DIR/${label}.stdin"
  local log="$WORK_DIR/${label}.console.log"
  rm -f "$fifo"
  mkfifo "$fifo"
  exec 3<>"$fifo"
  (
    cd "$WORK_DIR"
    java -Xms512M -Xmx2G -jar server.jar --nogui <&3 > "${label}.console.log" 2>&1
  ) &
  local pid=$!
  local deadline=$((SECONDS + STARTUP_TIMEOUT))
  local started=false
  while kill -0 "$pid" >/dev/null 2>&1 && (( SECONDS < deadline )); do
    if grep -Fq 'Done (' "$log" 2>/dev/null; then started=true; break; fi
    if grep -Eq 'Error occurred while enabling (Slimefun|FinalTECH-Changed)|Could not load .*FinalTECH' "$log" 2>/dev/null; then break; fi
    sleep 2
  done
  if [[ "$started" != true ]]; then
    echo "Folia ${label}: server did not reach Done." >&2
    stop_server "$pid" 3
    wait "$pid" >/dev/null 2>&1 || true
    exec 3>&-
    cat "$log" >&2 || true
    return 1
  fi

  printf 'sf doctor upgrade\n' >&3
  sleep 5
  stop_server "$pid" 3
  local status=0
  wait "$pid" || status=$?
  exec 3>&-
  if (( status != 0 )); then
    echo "Folia ${label}: server exited with status $status" >&2
    cat "$log" >&2 || true
    return 1
  fi

  grep -Fq "Enabling Slimefun v${SLIMEFUN_VERSION}" "$log"
  grep -Fq "Enabling FinalTECH-Changed v${FINALTECH_VERSION}" "$log"
  grep -Fq 'Slimefun Upgrade Readiness' "$log"
  ! grep -Eq 'Overall status:[[:space:]]+BLOCKED' "$log"
  ! grep -Eq 'Error occurred while enabling (Slimefun|FinalTECH-Changed)|Could not load .*FinalTECH' "$log"
  ! grep -Eq 'io\.taraxacum\..*(IllegalStateException|UnsupportedOperationException|AsyncCatcher)' "$log"
  grep -Fq 'Stopping server' "$log"
  if [[ "$require_clean" == true ]]; then
    grep -Eq 'Storage:[[:space:]]+Ready[[:space:]]+\|[[:space:]]+previous shutdown[[:space:]]+Clean' "$log"
  fi
}

cycle first false
cycle second true

cat > "$WORK_DIR/smoke-result.txt" <<EOF
FinalTECH Folia 26.3 test-only descriptor readiness: PASS
Slimefun Legacy: $SLIMEFUN_VERSION
FinalTECH: $FINALTECH_VERSION
Folia build: $SERVER_BUILD
Channel: $SERVER_CHANNEL
Cycles: 2
FinalTECH production descriptor modified: NO
Disposable probe descriptor: folia-supported true
Doctor BLOCKED: NO
Clean shutdown persistence: observed on second boot
Scope: startup/shutdown readiness only; no addon gameplay or cross-region certification
EOF
cat "$WORK_DIR/smoke-result.txt"
