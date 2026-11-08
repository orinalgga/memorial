#!/usr/bin/env bash
# 새 코드와 GitHub Actions가 만든 서버 이미지를 받아 다시 띄운다: bash deploy/update.sh
set -euo pipefail
cd "$(dirname "$0")/.."
git pull --ff-only
docker compose pull
docker compose up -d --no-build
docker image prune -f >/dev/null
docker compose ps
