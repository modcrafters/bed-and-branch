#!/usr/bin/env bash
# The text under "## Unreleased" is the changelog a tag publishes. After that publish, the
# heading becomes the released version and a new empty Unreleased is inserted above it.
# Usage:
#   release-changelog.sh extract CHANGELOG.md
#   release-changelog.sh bump CHANGELOG.md 0.2.0
set -euo pipefail

die() { echo "$1" >&2; exit 1; }

semver='^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-alpha|-beta)?$'

extract() {
  local file=$1 body
  body=$(awk '
    /^## Unreleased$/ { in_section = 1; found = 1; next }
    /^## / && in_section { exit }
    in_section { lines[++n] = $0 }
    END {
      if (!found) exit 1
      start = 1
      while (start <= n && lines[start] ~ /^[[:space:]]*$/) start++
      end = n
      while (end >= start && lines[end] ~ /^[[:space:]]*$/) end--
      for (i = start; i <= end; i++) print lines[i]
    }
  ' "$file") || die "${file} has no ## Unreleased section"
  printf '%s\n' "$body" | grep -q '^[[:space:]]*[-*] ' || die "${file} has no bullets under ## Unreleased"
  printf '%s\n' "$body"
}

bump() {
  local file=$1 version=$2 tmp
  [[ "$version" =~ $semver ]] || die "Version ${version} has to look like 0.0.1 or 0.0.1-alpha"
  grep -Fxq "## ${version}" "$file" && die "${file} already has a ## ${version} section"
  extract "$file" >/dev/null
  tmp=$(mktemp)
  awk -v version="$version" '
    /^## Unreleased$/ && !done {
      print "## Unreleased"
      print ""
      print "## " version
      done = 1
      next
    }
    { print }
    END { if (!done) exit 1 }
  ' "$file" > "$tmp" && mv "$tmp" "$file" || {
    rm -f "$tmp"
    die "Could not rename ## Unreleased in ${file}"
  }
}

[[ $# -ge 1 ]] || die "Usage: release-changelog.sh extract|bump ..."
cmd=$1
shift
case "$cmd" in
  extract) [[ $# -eq 1 ]] || die "Usage: release-changelog.sh extract CHANGELOG.md"; extract "$1" ;;
  bump) [[ $# -eq 2 ]] || die "Usage: release-changelog.sh bump CHANGELOG.md 0.2.0"; bump "$1" "$2" ;;
  *) die "Unknown command: ${cmd}" ;;
esac
