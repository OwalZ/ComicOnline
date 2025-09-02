#!/usr/bin/env bash
set -euo pipefail
# ComicOnline launcher (Unix)
# Ejecuta: mvn -DskipTests clean spring-boot:run

if [[ ! -f pom.xml ]]; then
  echo "[ERROR] Ejecuta este script en la carpeta que contiene pom.xml" >&2
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "[ERROR] Maven no encontrado en PATH" >&2
  exit 1
fi

echo "[INFO] Limpiando y arrancando Spring Boot..."
exec mvn -DskipTests clean spring-boot:run
