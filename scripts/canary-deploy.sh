#!/usr/bin/env bash
# Deploy a canary instance of plantation-service on port 8282
# and switch nginx to canary-aware config with an initial weight (default 10%).
#
# Usage: canary-deploy.sh [image-tag] [initial-weight-pct]
#   image-tag        — defaults to "latest"
#   initial-weight   — 1..50, defaults to 10
set -Eeuo pipefail

IMAGE_TAG="${1:-latest}"
WEIGHT="${2:-10}"

if ! [[ "$WEIGHT" =~ ^[0-9]+$ ]] || [ "$WEIGHT" -lt 1 ] || [ "$WEIGHT" -gt 99 ]; then
  echo "ERROR: weight must be integer 1..99"
  exit 1
fi

IMAGE="mysawit-plantation:${IMAGE_TAG}"
ENV_FILE="${HOME}/apps/mysawit-plantation-service/shared/app.env"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${SCRIPT_DIR}/../deploy/canary-lb"

[ -f "$ENV_FILE" ] || { echo "ERROR: env file missing — run CD first."; exit 1; }
docker image inspect "$IMAGE" >/dev/null 2>&1 || { echo "ERROR: image ${IMAGE} not found."; exit 1; }

# Stable pool must already be up
docker ps --filter "name=mysawit-plantation-stable-1" --format '{{.Names}}' | grep -q . \
  || { echo "ERROR: stable pool not up. Run lb-up.sh first."; exit 1; }

echo "Starting canary (image=${IMAGE}, weight=${WEIGHT}%)"
docker rm -f mysawit-plantation-canary >/dev/null 2>&1 || true
docker run -d \
  --name mysawit-plantation-canary \
  -p 8282:8082 \
  --env-file "$ENV_FILE" \
  -e "SERVER_PORT=8082" \
  -l "com.mysawit.role=demo-canary" \
  -l "com.mysawit.image-tag=${IMAGE_TAG}" \
  --restart unless-stopped \
  --log-opt max-size=10m --log-opt max-file=3 \
  "$IMAGE" >/dev/null

# Wait for canary to be ready
for attempt in $(seq 1 36); do
  if curl -fsS "http://127.0.0.1:8282/actuator/health/readiness" >/dev/null 2>&1; then
    break
  fi
  sleep 5
  [ "$attempt" -eq 36 ] && { echo "ERROR: canary not ready"; docker logs --tail 100 mysawit-plantation-canary; exit 1; }
done
echo "Canary READY on port 8282"

# Generate nginx canary config with the requested weight
sed "s/__CANARY_PCT__/${WEIGHT}/g" "${DEPLOY_DIR}/nginx.canary.conf.tmpl" > "${DEPLOY_DIR}/nginx.conf"
echo "canary:${WEIGHT}" > "${DEPLOY_DIR}/mode"

docker exec mysawit-plantation-nginx nginx -t >/dev/null 2>&1 \
  || { echo "ERROR: nginx config invalid"; exit 1; }
docker exec mysawit-plantation-nginx nginx -s reload

echo
echo "Canary live at ${WEIGHT}% traffic via nginx :8089"
echo "Verify split:  for i in {1..20}; do curl -s http://localhost:8089/__backend; done | sort | uniq -c"
