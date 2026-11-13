#!/usr/bin/env bash
# PostToolUse(Edit|Write|MultiEdit) on .java: enforce domain boundaries and BigDecimal money in billing.
command -v jq >/dev/null 2>&1 || exit 0
file=$(jq -r '.tool_input.file_path // empty')
[[ "$file" == *.java && -f "$file" ]] || exit 0
domain_re='/ua/kpi/blindpig/([a-z]+)/'
[[ "$file" =~ $domain_re ]] || exit 0
domain="${BASH_REMATCH[1]}"

problems=""
import_re='^import[[:space:]]+(static[[:space:]]+)?ua\.kpi\.blindpig\.([a-z]+)\.(repository|model)([.;])'
while IFS= read -r line || [ -n "$line" ]; do
  if [[ "$line" =~ $import_re ]]; then
    other="${BASH_REMATCH[2]}"
    if [[ "$other" != "$domain" ]]; then
      problems+="  - cross-domain import in '$domain': $line"$'\n'
      problems+="    -> use ua.kpi.blindpig.$other.service.*Service and its DTOs instead"$'\n'
    fi
  fi
done < "$file"

if [[ "$domain" == "billing" && "$file" == */src/main/* ]]; then
  money=$(grep -nE '(^|[^A-Za-z_])(double|float|Double|Float)([^A-Za-z_]|$)' "$file" | grep -vE '^[0-9]+:[[:space:]]*(//|\*|/\*)')
  if [ -n "$money" ]; then
    problems+="  - floating-point type in billing (money must be BigDecimal, scale 2, HALF_UP):"$'\n'
    problems+="$(sed 's/^/      /' <<<"$money")"$'\n'
  fi
fi

if [ -n "$problems" ]; then
  echo "Architecture guard: ${file##*/}" >&2
  printf '%s' "$problems" >&2
  exit 2
fi
