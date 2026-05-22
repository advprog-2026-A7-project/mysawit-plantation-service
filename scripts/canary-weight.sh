#!/usr/bin/env bash
# Adjust canary traffic percentage. Requires canary container already running.
#
# Usage: canary-weight.sh <pct>     pct = 1..99
set -Eeuo pipefail

WEIGHT="${1:?Usage: $0 <pct 1..99>}"
if ! [[ "$WEIGHT" =~ ^[0-9]+$ ]] || [ "$WEIGHT" -lt 1 ] || [ "$WEIGHT" -gt 99 ]; then
  echo "ERROR: weight must be integer 1..99 (use canary-rollback.sh to go to 0)"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${SCRIPT_DIR}/../deploy/canary-lb"

docker ps --filter "name=mysawit-plantation-canary" --format '{{.Names}}' | grep -q . \
  || { echo "ERROR: canary not running. Use canary-deploy.sh first."; exit 1; }

sed "s/__CANARY_PCT__/${WEIGHT}/g" "${DEPLOY_DIR}/nginx.canary.conf.tmpl" > "${DEPLOY_DIR}/nginx.conf"
echo "canary:${WEIGHT}" > "${DEPLOY_DIR}/mode"

docker exec mysawit-plantation-nginx nginx -t >/dev/null 2>&1 \
  || { echo "ERROR: nginx config invalid"; exit 1; }
docker exec mysawit-plantation-nginx nginx -s reload

echo "Canary weight set to ${WEIGHT}% (nginx reloaded)"
