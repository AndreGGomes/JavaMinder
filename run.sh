#!/usr/bin/env bash
# Sobe o JavaMinder compilando antes se necessário.
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -d out ]; then
  ./build.sh
fi

exec java -cp out javaminnder.Main "$@"
