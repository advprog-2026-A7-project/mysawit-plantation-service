#!/usr/bin/env bash
# Stop the canary: nginx back to LB-only (stable pool 100%), then remove canary container.
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${SCRIPT_DIR}/../deploy/canary-lb"

# First, switch nginx away from canary so no new traffic hits it.
cp "${DEPLOY_DIR}/nginx.lb.conf.tmpl" "${DEPLOY_DIR}/nginx.conf"
echo "stable_only" > "${DEPLOY_DIR}/mode"

if docker ps --filter "name=mysawit-plantation-nginx" --format '{{.Names}}' | grep -q .; then
  docker exec mysawit-plantation-nginx nginx -t >/dev/null 2>&1 \
    || { echo "ERROR: nginx config invalid"; exit 1; }
  docker exec mysawit-plantation-nginx nginx -s reload
  echo "Nginx now routes 100% to stable_pool"
fi

# Drain briefly, then remove canary container.
sleep 2
docker rm -f mysawit-plantation-canary >/dev/null 2>&1 || true
echo "Canary container stopped & removed."
