#!/usr/bin/env bash
# Stop: if Java sources changed since the last green run, run the test suite; report failures back to Claude.
command -v jq >/dev/null 2>&1 || exit 0
[ "${BLINDPIG_SKIP_STOP_TESTS:-0}" = "1" ] && exit 0
input=$(cat)
[ "$(jq -r '.stop_hook_active // false' <<<"$input")" = "true" ] && exit 0

cd "${CLAUDE_PROJECT_DIR:-$(pwd)}" || exit 0
git rev-parse --git-dir >/dev/null 2>&1 || exit 0
changed=$( { git diff --name-only HEAD; git ls-files --others --exclude-standard; } | grep -E '(\.java|pom\.xml)$' )
[ -n "$changed" ] || exit 0

fingerprint=$( { git diff HEAD -- '*.java' pom.xml; git ls-files --others --exclude-standard -- '*.java' | while read -r f; do echo "$f"; cat "$f"; done; } | shasum | cut -d' ' -f1 )
stamp="${TMPDIR:-/tmp}/blindpig-last-green-$(pwd | shasum | cut -c1-12)"
[ -f "$stamp" ] && [ "$(cat "$stamp")" = "$fingerprint" ] && exit 0

output=$(./mvnw -q -B test 2>&1)
if [ $? -eq 0 ]; then
  echo "$fingerprint" > "$stamp"
  exit 0
fi
echo "./mvnw test failed after your changes. Fix before finishing (or explain why). Last lines:" >&2
grep -vE '^\[INFO\]|^Downloading|^Downloaded' <<<"$output" | tail -n 60 >&2
exit 2
