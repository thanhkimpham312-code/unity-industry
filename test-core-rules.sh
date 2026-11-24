#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
javac -encoding UTF-8 -d "$TMP"   "$ROOT/src/main/java/com/unityindustry/rules/CoreType.java"   "$ROOT/src/main/java/com/unityindustry/rules/CoreRules.java"   "$ROOT/src/main/java/com/unityindustry/rules/EnergyLedger.java"   "$ROOT/src/test-manual/java/com/unityindustry/rules/RulesSelfTest.java"
java -cp "$TMP" com.unityindustry.rules.RulesSelfTest
