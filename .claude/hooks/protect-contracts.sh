#!/usr/bin/env bash
# PreToolUse(Edit|Write|MultiEdit): ask a human before changing shared team contracts.
command -v jq >/dev/null 2>&1 || exit 0
file=$(jq -r '.tool_input.file_path // empty')
[ -n "$file" ] || exit 0

reason=""
if [[ "$file" =~ /docs/(DESIGN|SERVICE_CONTRACTS)\.md$ ]]; then
  reason="team design/contract document"
elif [[ "$file" =~ /ua/kpi/blindpig/[a-z]+/service/[A-Za-z]+Service\.java$ ]]; then
  reason="shared service contract interface (docs/SERVICE_CONTRACTS.md)"
elif [[ "$file" =~ /ua/kpi/blindpig/[a-z]+/dto/[^/]+\.java$ ]]; then
  reason="shared contract DTO/enum (only Bean Validation annotations may be added without team agreement)"
elif [[ "$file" =~ /ua/kpi/blindpig/common/ ]]; then
  reason="shared common package used by all domains"
elif [[ "$file" =~ /pom\.xml$ ]]; then
  reason="shared build file"
fi
[ -n "$reason" ] || exit 0

jq -n --arg r "Protected file: $reason. Confirm this change is agreed with the team." \
  '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "ask", permissionDecisionReason: $r}}'
