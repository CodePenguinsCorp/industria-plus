#!/usr/bin/env sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
repo_root="$(dirname -- "$script_dir")"

output_path="${OUTPUT_PATH:-${1:-$repo_root/.env.prod}}"
backend_image="${BACKEND_IMAGE:-industria-plus-backend:ci}"
frontend_image="${FRONTEND_IMAGE:-industria-plus-frontend:ci}"
force="${FORCE:-false}"

if [ -f "$output_path" ] && [ "$force" != "true" ]; then
  echo "Arquivo $output_path ja existe. Use FORCE=true para recriar."
  exit 1
fi

new_hex_secret() {
  bytes="${1:-32}"
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex "$bytes"
    return
  fi

  od -An -N "$bytes" -tx1 /dev/urandom | tr -d ' \n'
}

mysql_password="$(new_hex_secret 24)"
mysql_root_password="$(new_hex_secret 24)"

cat > "$output_path" <<EOF
# Production Compose
FRONTEND_PORT=80
MYSQL_DATABASE=industria_plus
MYSQL_USER=industria_app
MYSQL_PASSWORD=$mysql_password
MYSQL_ROOT_PASSWORD=$mysql_root_password

# Images published by the delivery pipeline
BACKEND_IMAGE=$backend_image
FRONTEND_IMAGE=$frontend_image

# Backend / Spring Boot
LOG_LEVEL=INFO
EOF

echo "Arquivo $output_path criado com senhas fortes."
echo "Revise as imagens e guarde o arquivo fora do controle de versao."
