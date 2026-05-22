#!/usr/bin/env bash
# Show current Canary + LB state.
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${SCRIPT_DIR}/../deploy/canary-lb"

MODE="$(cat "${DEPLOY_DIR}/mode" 2>/dev/null || echo '(unknown)')"
echo "Mode: ${MODE}"
echo
docker ps -a --filter "name=mysawit-plantation-stable-1" \
            --filter "name=mysawit-plantation-stable-2" \
            --filter "name=mysawit-plantation-canary" \
            --filter "name=mysawit-plantation-nginx" \
            --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}\t{{.Image}}'

echo
echo "Sampling 20 requests through the LB (you should see split if canary active):"
for i in $(seq 1 20); do
  curl -sS "http://127.0.0.1:8089/__backend" || true
done | sort | uniq -c
