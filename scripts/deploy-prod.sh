#!/usr/bin/env sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
repo_root="$(dirname -- "$script_dir")"

compose_file="${COMPOSE_FILE:-${1:-$repo_root/compose.prod.yaml}}"
env_file="${ENV_FILE:-${2:-$repo_root/.env.prod}}"

if [ ! -f "$compose_file" ]; then
  echo "Arquivo $compose_file nao encontrado."
  exit 1
fi

if [ ! -f "$env_file" ]; then
  echo "Arquivo $env_file nao encontrado. Gere-o com scripts/init-prod-env.sh."
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker nao foi encontrado no PATH."
  exit 1
fi

docker compose --env-file "$env_file" -f "$compose_file" pull
docker compose --env-file "$env_file" -f "$compose_file" up -d
docker compose --env-file "$env_file" -f "$compose_file" ps
