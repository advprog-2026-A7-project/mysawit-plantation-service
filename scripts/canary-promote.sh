#!/usr/bin/env bash
# Promote canary → all stable replicas now run the canary's image tag.
# Process: rolling-replace each stable replica with the canary image, one at a time.
# Then call canary-rollback.sh to remove the canary container.
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

CANARY_IMAGE="$(docker inspect --format '{{.Config.Image}}' mysawit-plantation-canary 2>/dev/null || true)"
if [ -z "$CANARY_IMAGE" ]; then
  echo "ERROR: canary container not found — nothing to promote."
  exit 1
fi
echo "Promoting image: ${CANARY_IMAGE}"

ENV_FILE="${HOME}/apps/mysawit-plantation-service/shared/app.env"

replace_stable() {
  local name="$1" port="$2"
  echo "  Replacing ${name} (port ${port})..."
  docker rm -f "$name" >/dev/null 2>&1 || true
  docker run -d \
    --name "$name" \
    -p "${port}:8082" \
    --env-file "$ENV_FILE" \
    -e "SERVER_PORT=8082" \
    -l "com.mysawit.role=demo-stable" \
    --restart unless-stopped \
    --log-opt max-size=10m --log-opt max-file=3 \
    "$CANARY_IMAGE" >/dev/null

  for attempt in $(seq 1 36); do
    if curl -fsS "http://127.0.0.1:${port}/actuator/health/readiness" >/dev/null 2>&1; then
      echo "    ${name} READY"
      return 0
    fi
    sleep 5
  done
  echo "    ERROR: ${name} failed readiness after replacement"
  exit 1
}

# Rolling: replace one at a time so nginx can keep serving from the other.
replace_stable mysawit-plantation-stable-1 8182
replace_stable mysawit-plantation-stable-2 8192

echo "Both stable replicas now run promoted image. Removing canary..."
"${SCRIPT_DIR}/canary-rollback.sh"

echo "Promotion complete."
