#!/usr/bin/env bash
# PreToolUse(Bash), personal: Claude must not commit, push or tag; the user does that.
command -v jq >/dev/null 2>&1 || exit 0
cmd=$(jq -r '.tool_input.command // empty')
re='(^|[;&|(`[:space:]])git([[:space:]]+-[A-Za-z][^[:space:]]*([[:space:]]+[^-[:space:]][^[:space:]]*)?)*[[:space:]]+(commit|push|tag)([[:space:]]|$)'
[[ "$cmd" =~ $re ]] || exit 0
sub="${BASH_REMATCH[4]}"
# Allow read-only tag listing.
if [ "$sub" = "tag" ] && [[ "$cmd" =~ git[[:space:]]+tag([[:space:]]+(-l|--list|-n[0-9]*|--contains[[:space:]][^[:space:]]+))*[[:space:]]*($|[;&|]) ]]; then
  exit 0
fi
jq -n --arg r "git $sub is reserved for the user. Suggest a commit message / tag instead of running it." \
  '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "deny", permissionDecisionReason: $r}}'
