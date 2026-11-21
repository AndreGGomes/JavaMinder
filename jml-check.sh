#!/usr/bin/env bash
# Verifica as anotações JML do JavaMinder com o OpenJML.
# A ferramenta é baixada à parte em .tools/openjml (veja o README).
set -euo pipefail
cd "$(dirname "$0")"

OPENJML=".tools/openjml/openjml"
if [ ! -x "$OPENJML" ]; then
  echo "OpenJML não encontrado em $OPENJML." >&2
  echo "Baixe o release Linux em https://github.com/OpenJML/OpenJML/releases e extraia em .tools/openjml" >&2
  exit 1
fi

# javac ignora as anotações /*@ ... @*/: o build normal precisa continuar passando.
./build.sh

# User/AuthException entram pela classpath (binárias, sem specs):
# o ESC verifica apenas a Session.
"$OPENJML" --esc -cp out src/javaminnder/service/Session.java
