#!/usr/bin/env bash
# Tear down LB stack: nginx, stable replicas, and canary (if running).
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${SCRIPT_DIR}/../deploy/canary-lb"

(cd "$DEPLOY_DIR" && docker compose -f docker-compose.demo.yml down) || true
docker rm -f mysawit-plantation-stable-1 >/dev/null 2>&1 || true
docker rm -f mysawit-plantation-stable-2 >/dev/null 2>&1 || true
docker rm -f mysawit-plantation-canary   >/dev/null 2>&1 || true

echo "Plantation demo stack stopped."
