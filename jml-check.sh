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

# Classes anotadas, na ordem em que foram provadas. As demais entram pela
# classpath (binárias, sem specs) e não são verificadas pelo ESC.
FILES=(
  src/javaminnder/model/Describable.java
  src/javaminnder/model/UserRole.java
  src/javaminnder/model/SubscriptionStatus.java
  src/javaminnder/model/BillingCycle.java
  src/javaminnder/util/Validators.java
  src/javaminnder/model/BaseEntity.java
  src/javaminnder/model/User.java
  src/javaminnder/model/Subscription.java
  src/javaminnder/service/Session.java
  src/javaminnder/service/AuthService.java
)

"$OPENJML" --esc -cp out "${FILES[@]}"
