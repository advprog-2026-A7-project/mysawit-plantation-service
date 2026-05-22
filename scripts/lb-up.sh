#!/usr/bin/env bash
# Start 2 stable plantation replicas + the load-balancing nginx.
# Replicas listen on host ports 8182 and 8192. Nginx fronts them on 8089.
#
# Usage: lb-up.sh [image-tag]   (default: latest)
set -Eeuo pipefail

IMAGE_TAG="${1:-latest}"
IMAGE="mysawit-plantation:${IMAGE_TAG}"
ENV_FILE="${HOME}/apps/mysawit-plantation-service/shared/app.env"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${SCRIPT_DIR}/../deploy/canary-lb"

[ -f "$ENV_FILE" ] || { echo "ERROR: ${ENV_FILE} missing — run CD first."; exit 1; }
docker image inspect "$IMAGE" >/dev/null 2>&1 || { echo "ERROR: image ${IMAGE} not found locally."; exit 1; }

start_replica() {
  local name="$1" port="$2"
  docker rm -f "$name" >/dev/null 2>&1 || true
  docker run -d \
    --name "$name" \
    -p "${port}:8082" \
    --env-file "$ENV_FILE" \
    -e "SERVER_PORT=8082" \
    -l "com.mysawit.role=demo-stable" \
    --restart unless-stopped \
    --log-opt max-size=10m --log-opt max-file=3 \
    "$IMAGE" >/dev/null
  echo "Started ${name} on host port ${port}"
}

start_replica mysawit-plantation-stable-1 8182
start_replica mysawit-plantation-stable-2 8192

echo "Waiting for both replicas to be READY..."
for port in 8182 8192; do
  for attempt in $(seq 1 36); do
    if curl -fsS "http://127.0.0.1:${port}/actuator/health/readiness" >/dev/null 2>&1; then
      echo "  port ${port} READY"
      break
    fi
    sleep 5
    [ "$attempt" -eq 36 ] && { echo "ERROR: port ${port} not ready"; docker logs --tail 100 "mysawit-plantation-stable-$( [ $port = 8182 ] && echo 1 || echo 2 )"; exit 1; }
  done
done

# Initial nginx config: LB only, no canary
cp "${DEPLOY_DIR}/nginx.lb.conf.tmpl" "${DEPLOY_DIR}/nginx.conf"
echo "stable_only" > "${DEPLOY_DIR}/mode"

(cd "$DEPLOY_DIR" && docker compose -f docker-compose.demo.yml up -d)

echo
echo "Load balancer UP on :8089"
echo "Try:  for i in {1..10}; do curl -s http://localhost:8089/actuator/health -o /dev/null -w '%{http_code} → %{remote_ip}:%{remote_port}\\n'; done"
