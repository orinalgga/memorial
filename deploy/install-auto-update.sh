#!/usr/bin/env bash
# auto-update.sh를 ubuntu 사용자의 cron에 5분마다 돌게 등록한다. 여러 번 실행해도 한 줄만 남는다.
# 끄기: crontab -l | grep -v memorial/deploy/auto-update.sh | crontab -
set -euo pipefail
dir=$(cd "$(dirname "$0")/.." && pwd)
chmod +x "$dir/deploy/auto-update.sh"
line="*/5 * * * * $dir/deploy/auto-update.sh >> $HOME/memorial-auto-update.log 2>&1"
{ crontab -l 2>/dev/null | grep -vF "$dir/deploy/auto-update.sh" || true; echo "$line"; } | crontab -
echo "자동 업데이트를 켰다 (5분마다). 로그: $HOME/memorial-auto-update.log"
