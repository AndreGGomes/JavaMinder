#!/usr/bin/env bash
# Compila o JavaMinder usando apenas o javac da JDK (sem Maven, sem dependências).
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out
mkdir -p out

find src -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d out

echo "Compilado em ./out"
