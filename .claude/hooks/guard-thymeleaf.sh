#!/usr/bin/env bash
# PostToolUse(Edit|Write|MultiEdit) on templates: forbid unescaped output (XSS).
command -v jq >/dev/null 2>&1 || exit 0
file=$(jq -r '.tool_input.file_path // empty')
[[ "$file" == */templates/*.html && -f "$file" ]] || exit 0
hits=$(grep -n 'th:utext' "$file")
if [ -n "$hits" ]; then
  echo "Thymeleaf guard: ${file##*/} uses th:utext (unescaped HTML, XSS risk). Use th:text instead:" >&2
  echo "$hits" >&2
  exit 2
fi
