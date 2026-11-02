#!/usr/bin/env bash
# Publishes the three jars for one tag: NeoForge 1.21.1, Fabric 26.3, and NeoForge 26.3.
# Each loader gets its own Modrinth version and CurseForge file, so the download button
# is the jar for that loader. GitHub gets one release with all three attached.
# Usage:
#   release-publish.sh check-env
#   release-publish.sh classify <jars-dir> <version>
#   release-publish.sh upload <version> <changelog-file> <jars-dir>
set -euo pipefail

die() { echo "$1" >&2; exit 1; }

user_agent='modcrafters/bed-and-branch (release)'
# Fabric API on Modrinth. The loader itself is selected with the loaders field.
fabric_api='P7dR8mSH'

check_env() {
  local missing=()
  [[ -n "${MODRINTH_TOKEN:-}" ]] || missing+=(MODRINTH_TOKEN)
  [[ -n "${CURSEFORGE_TOKEN:-}" ]] || missing+=(CURSEFORGE_TOKEN)
  [[ -n "${MODRINTH_PROJECT_ID:-}" ]] || missing+=(MODRINTH_PROJECT_ID)
  [[ -n "${CURSEFORGE_PROJECT_ID:-}" ]] || missing+=(CURSEFORGE_PROJECT_ID)
  if [[ ${#missing[@]} -gt 0 ]]; then
    die "Missing ${missing[*]}. Tokens are repository secrets. Project ids are repository variables."
  fi
  [[ "$MODRINTH_PROJECT_ID" =~ ^[A-Za-z0-9]+$ ]] || die "MODRINTH_PROJECT_ID should be the id from the Modrinth project page"
  [[ "$CURSEFORGE_PROJECT_ID" =~ ^[0-9]+$ ]] || die "CURSEFORGE_PROJECT_ID should be the number from the CurseForge project page"
}

# Sets neo_121, fabric_263, and neo_263. A sources jar, or a jar from another version, is ignored.
classify() {
  local dir=$1 version=$2 jar base
  neo_121='' fabric_263='' neo_263=''
  [[ -d "$dir" ]] || die "No jar directory at ${dir}"
  while IFS= read -r jar; do
    base=$(basename "$jar")
    case "$base" in
      "bed_and_branch-neoforge-1.21.1-${version}.jar")
        [[ -z "$neo_121" ]] || die "Two NeoForge 1.21.1 jars named ${base}"
        neo_121=$jar
        ;;
      "bed_and_branch-fabric-26.3-${version}.jar")
        [[ -z "$fabric_263" ]] || die "Two Fabric 26.3 jars named ${base}"
        fabric_263=$jar
        ;;
      "bed_and_branch-neoforge-26.3-${version}.jar")
        [[ -z "$neo_263" ]] || die "Two NeoForge 26.3 jars named ${base}"
        neo_263=$jar
        ;;
    esac
  done < <(find "$dir" -type f -name '*.jar' | sort)
  [[ -n "$neo_121" ]] || die "Missing bed_and_branch-neoforge-1.21.1-${version}.jar"
  [[ -n "$fabric_263" ]] || die "Missing bed_and_branch-fabric-26.3-${version}.jar"
  [[ -n "$neo_263" ]] || die "Missing bed_and_branch-neoforge-26.3-${version}.jar"
}

http_code() {
  local out=$1
  shift
  local code
  code=$(curl -sS -o "$out" -w "%{http_code}" "$@") || die "The request did not complete"
  printf '%s' "$code"
}

curseforge_version_id() {
  local name=$1 required=$2 result
  result=$(jq -r --arg name "$name" '
    [ .[] | select(.name == $name) | .id ] | unique
    | if length == 1 then "ok \(.[0])" else "bad \(length)" end
  ' "$versions_file")
  case "$result" in
    ok\ *) echo "${result#ok }" ;;
    *)
      if [[ "$required" == yes ]]; then
        echo "CurseForge has no single game version named ${name} (${result})." >&2
        jq -r --arg name "$name" '.[] | select(.name | contains($name)) | .name' "$versions_file" | sort -u | head >&2 || true
        exit 1
      fi
      echo "CurseForge has no single entry named ${name}; leaving it off this file." >&2
      ;;
  esac
}

curseforge_game_versions() {
  local game=$1 loader=$2 java_name=$3 id ids=()
  id=$(curseforge_version_id "$game" yes) || exit 1
  ids+=("$id")
  id=$(curseforge_version_id "$loader" yes) || exit 1
  ids+=("$id")
  id=$(curseforge_version_id "$java_name" no)
  [[ -n "$id" ]] && ids+=("$id")
  printf '%s\n' "${ids[@]}" | jq -R . | jq -s 'map(tonumber)'
}

modrinth_upload() {
  local version_number=$1 name=$2 loader=$3 game=$4 jar=$5
  local encoded code deps
  encoded=$(jq -nr --arg value "$version_number" '$value|@uri')
  code=$(http_code "$work/exists.json" \
    -H "User-Agent: ${user_agent}" \
    -H "Authorization: ${MODRINTH_TOKEN}" \
    "https://api.modrinth.com/v2/project/${MODRINTH_PROJECT_ID}/version/${encoded}")
  if [[ "$code" == 200 ]]; then
    echo "Modrinth ${version_number} already exists"
    return 0
  fi
  [[ "$code" == 404 ]] || {
    echo "Modrinth lookup for ${version_number} returned ${code}" >&2
    cat "$work/exists.json" >&2
    exit 1
  }
  if [[ "$loader" == fabric ]]; then
    deps="[{\"project_id\":\"${fabric_api}\",\"dependency_type\":\"required\"}]"
  else
    deps='[]'
  fi
  jq -n \
    --rawfile changelog "$notes" \
    --arg name "$name" \
    --arg version_number "$version_number" \
    --arg project_id "$MODRINTH_PROJECT_ID" \
    --arg version_type "$release_type" \
    --arg game "$game" \
    --arg loader "$loader" \
    --argjson dependencies "$deps" \
    '{
      name: $name,
      version_number: $version_number,
      changelog: $changelog,
      dependencies: $dependencies,
      game_versions: [$game],
      version_type: $version_type,
      loaders: [$loader],
      featured: true,
      project_id: $project_id,
      file_parts: ["jar"],
      primary_file: "jar",
      status: "listed",
      requested_status: "listed",
      environment: "client_and_server"
    }' > "$work/modrinth.json"
  code=$(http_code "$work/created.json" \
    -H "User-Agent: ${user_agent}" \
    -H "Authorization: ${MODRINTH_TOKEN}" \
    -F "data=<${work}/modrinth.json;type=application/json" \
    -F "jar=@${jar};filename=$(basename "$jar")" \
    https://api.modrinth.com/v2/version)
  [[ "$code" == 200 ]] || {
    echo "Modrinth upload of ${version_number} returned ${code}" >&2
    cat "$work/created.json" >&2
    exit 1
  }
  echo "Modrinth ${version_number} published ($(jq -r .id "$work/created.json"))"
}

curseforge_listed() {
  local file_name=$1 display=$2
  [[ "$files_listed" == yes ]] || return 1
  jq -e --arg file "$file_name" --arg display "$display" '
    def files:
      if type == "array" then .
      elif .data then .data
      elif .files then .files
      else [] end;
    files | any(.[]; .fileName == $file or .displayName == $display)
  ' "$work/files.json" >/dev/null
}

curseforge_upload() {
  local display=$1 game=$2 loader=$3 java_name=$4 jar=$5
  local code relations='null'
  if curseforge_listed "$(basename "$jar")" "$display"; then
    echo "CurseForge already has ${display}"
    return 0
  fi
  [[ "$loader" == Fabric ]] && relations='{"projects":[{"slug":"fabric-api","type":"requiredDependency"}]}'
  jq -n \
    --rawfile changelog "$notes" \
    --arg display "$display" \
    --arg release_type "$release_type" \
    --argjson game_versions "$(curseforge_game_versions "$game" "$loader" "$java_name")" \
    --argjson relations "$relations" \
    '{
      changelog: $changelog,
      changelogType: "markdown",
      displayName: $display,
      gameVersions: $game_versions,
      releaseType: $release_type
    } + (if $relations == null then {} else {relations: $relations} end)' > "$work/curseforge.json"
  code=$(http_code "$work/uploaded.json" \
    -H "X-Api-Token: ${CURSEFORGE_TOKEN}" \
    -F "metadata=<${work}/curseforge.json;type=application/json" \
    -F "file=@${jar};filename=$(basename "$jar")" \
    "https://minecraft.curseforge.com/api/projects/${CURSEFORGE_PROJECT_ID}/upload-file")
  [[ "$code" == 200 ]] || {
    echo "CurseForge upload of ${display} returned ${code}" >&2
    cat "$work/uploaded.json" >&2
    exit 1
  }
  echo "CurseForge ${display} published ($(jq -r .id "$work/uploaded.json"))"
}

github_release() {
  local tag=$1
  shift
  if gh release view "$tag" >/dev/null 2>&1; then
    echo "GitHub release ${tag} already exists"
  else
    local -a args=(release create "$tag" --title "Bed & Branch ${tag#v}" --notes-file "$notes")
    [[ "$release_type" == release ]] || args+=(--prerelease)
    gh "${args[@]}"
  fi
  gh release upload "$tag" --clobber "$@"
  echo "GitHub release ${tag} has the jars"
}

upload() {
  local version=$1
  notes=$2
  local jars=$3 code
  check_env
  [[ -f "$notes" ]] || die "No changelog at ${notes}"
  [[ -n "${TAG:-}" ]] || die "TAG is missing"
  case "${RELEASE_TYPE:-}" in
    alpha|beta|release) release_type=$RELEASE_TYPE ;;
    *) die "RELEASE_TYPE has to be alpha, beta, or release" ;;
  esac
  classify "$jars" "$version"
  work=$(mktemp -d)
  trap 'rm -rf "$work"' EXIT

  code=$(http_code "$work/versions.json" \
    -H "X-Api-Token: ${CURSEFORGE_TOKEN}" \
    https://minecraft.curseforge.com/api/game/versions)
  [[ "$code" == 200 ]] || {
    echo "CurseForge game versions returned ${code}" >&2
    cat "$work/versions.json" >&2
    exit 1
  }
  versions_file=$work/version-list.json
  jq 'if type == "array" then . elif .data then .data else empty end' "$work/versions.json" > "$versions_file"
  [[ "$(jq 'length' "$versions_file")" -gt 0 ]] || die "CurseForge returned no game versions"

  code=$(http_code "$work/files.json" \
    -H "X-Api-Token: ${CURSEFORGE_TOKEN}" \
    "https://minecraft.curseforge.com/api/projects/${CURSEFORGE_PROJECT_ID}/files")
  if [[ "$code" == 200 ]]; then
    files_listed=yes
  else
    files_listed=no
    echo "CurseForge file list returned ${code}. A repeated run may upload the same jar again." >&2
  fi

  modrinth_upload "${version}+1.21.1" "Bed & Branch ${version} for NeoForge 1.21.1" neoforge 1.21.1 "$neo_121"
  modrinth_upload "${version}+26.3+fabric" "Bed & Branch ${version} for Fabric 26.3" fabric 26.3 "$fabric_263"
  modrinth_upload "${version}+26.3+neoforge" "Bed & Branch ${version} for NeoForge 26.3" neoforge 26.3 "$neo_263"

  curseforge_upload "Bed & Branch ${version} for NeoForge 1.21.1" 1.21.1 NeoForge "Java 21" "$neo_121"
  curseforge_upload "Bed & Branch ${version} for Fabric 26.3" 26.3 Fabric "Java 25" "$fabric_263"
  curseforge_upload "Bed & Branch ${version} for NeoForge 26.3" 26.3 NeoForge "Java 25" "$neo_263"

  github_release "$TAG" "$neo_121" "$fabric_263" "$neo_263"
}

[[ $# -ge 1 ]] || die "Usage: release-publish.sh check-env|classify|upload ..."
cmd=$1
shift
case "$cmd" in
  check-env) check_env ;;
  classify)
    [[ $# -eq 2 ]] || die "Usage: release-publish.sh classify <jars-dir> <version>"
    classify "$1" "$2"
    printf '%s\n' "$neo_121" "$fabric_263" "$neo_263"
    ;;
  upload)
    [[ $# -eq 3 ]] || die "Usage: release-publish.sh upload <version> <changelog> <jars-dir>"
    upload "$1" "$2" "$3"
    ;;
  *) die "Unknown command: ${cmd}" ;;
esac
