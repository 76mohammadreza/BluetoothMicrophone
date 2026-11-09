#!/usr/bin/env sh
set -eu
HERE=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
VERSION=8.11.1
DIST="$HOME/.gradle/manual-distributions/gradle-$VERSION"
if [ ! -x "$DIST/bin/gradle" ]; then
  command -v curl >/dev/null || { echo 'Install curl or Gradle 8.11.1 first.' >&2; exit 1; }
  command -v unzip >/dev/null || { echo 'Install unzip or Gradle 8.11.1 first.' >&2; exit 1; }
  mkdir -p "$HOME/.gradle/manual-distributions"
  TMP=$(mktemp -d)
  trap 'rm -rf "$TMP"' EXIT HUP INT TERM
  curl -fL --retry 2 "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$TMP/gradle.zip"
  unzip -q "$TMP/gradle.zip" -d "$HOME/.gradle/manual-distributions"
fi
cd "$HERE"
exec "$DIST/bin/gradle" "$@"
