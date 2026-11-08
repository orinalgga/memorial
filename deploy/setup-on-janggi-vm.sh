#!/usr/bin/env bash
# 장기 앱이 이미 돌고 있는 Oracle VM에서 한 번만 실행한다: bash deploy/setup-on-janggi-vm.sh https://<프로젝트>.pages.dev
# .env를 만들고(DB 비밀번호는 VM 안에서 무작위로 만들어 어디에도 보내지 않는다) 추모 서버를 띄우고 자동 업데이트를 켠다.
set -euo pipefail
cd "$(dirname "$0")/.."
WEB=${1:?Cloudflare Pages 웹 주소를 인자로 주세요 (예: https://memorial.pages.dev)}

if ! docker network inspect janggigame_default >/dev/null 2>&1; then
  echo "장기 앱(janggigame_default 네트워크)이 떠 있지 않습니다. ~/janggigame 에서 docker compose up -d 를 먼저 하세요." >&2
  exit 1
fi

if [ ! -f .env ]; then
  cat > .env <<ENV
COMPOSE_FILE=docker-compose.yml:deploy/compose.janggi.yml
MEMORIAL_CORS_ALLOWED_ORIGINS=$WEB
POSTGRES_PASSWORD=$(openssl rand -hex 24)
JAVA_TOOL_OPTIONS=-Xmx256m
ENV
  chmod 600 .env
fi

for i in $(seq 1 60); do docker compose pull && break; sleep 30; done
docker compose up -d --no-build
bash deploy/install-auto-update.sh

IP=$(curl -fsS https://ifconfig.me)
echo
echo "추모 서버를 띄웠습니다. 이제 장기 앱 caddy에 주소를 더합니다(docs/deploy.md 3단계):"
echo "  ~/janggigame/.env 에  MEMORIAL_API_HOST=memorial.$IP.sslip.io"
