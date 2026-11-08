#!/usr/bin/env bash
# VM에서 cron으로 5분마다 돈다. main에 새 커밋이나 새 서버 이미지가 있으면 받아서 다시 띄운다.
# 바뀐 게 없으면 docker compose up은 아무것도 다시 띄우지 않는다.
# 설치: bash deploy/install-auto-update.sh   로그: ~/memorial-auto-update.log
set -euo pipefail
cd "$(dirname "$0")/.."

# 앞 실행이 아직 도는 중이면 이번은 건너뛴다.
exec 9>/tmp/memorial-auto-update.lock
flock -n 9 || exit 0

git fetch -q origin main
if [ "$(git rev-parse HEAD)" != "$(git rev-parse origin/main)" ]; then
  echo "$(date -u +%FT%TZ) 새 코드: $(git rev-parse --short origin/main)"
  git merge -q --ff-only origin/main
fi

before=$(docker compose images -q server 2>/dev/null || true)
docker compose pull -q
docker compose up -d --no-build --remove-orphans 2>&1 | grep -v -E 'Running|Up-to-date' || true
after=$(docker compose images -q server 2>/dev/null || true)
if [ "$before" != "$after" ]; then
  echo "$(date -u +%FT%TZ) 서버 이미지 바뀜: $after"
  docker image prune -f >/dev/null
fi
