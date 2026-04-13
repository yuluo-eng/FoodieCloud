#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"

BACKEND_JAR_DIR="${BACKEND_JAR_DIR:-$ROOT_DIR/target}"
BACKEND_LOG="${BACKEND_LOG:-$ROOT_DIR/app.log}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_DIST_TARGET="${FRONTEND_DIST_TARGET:-}"

echo "[1/5] Build backend"
"$ROOT_DIR/mvnw" -f "$ROOT_DIR/pom.xml" clean package -DskipTests

JAR_PATH="$(ls "$BACKEND_JAR_DIR"/*.jar | head -n 1)"
if [[ -z "$JAR_PATH" ]]; then
  echo "No backend jar found in $BACKEND_JAR_DIR"
  exit 1
fi

echo "[2/5] Restart backend on port $BACKEND_PORT"
OLD_PID="$(lsof -ti tcp:"$BACKEND_PORT" || true)"
if [[ -n "$OLD_PID" ]]; then
  kill -9 $OLD_PID || true
fi
nohup java -jar "$JAR_PATH" > "$BACKEND_LOG" 2>&1 &

echo "[3/5] Build frontend"
cd "$FRONTEND_DIR"
npm ci
npm run build

if [[ -n "$FRONTEND_DIST_TARGET" ]]; then
  echo "[4/5] Sync frontend dist to $FRONTEND_DIST_TARGET"
  mkdir -p "$FRONTEND_DIST_TARGET"
  rsync -av --delete "$FRONTEND_DIR/dist/" "$FRONTEND_DIST_TARGET/"
else
  echo "[4/5] Skip dist sync (set FRONTEND_DIST_TARGET to enable)"
fi

echo "[5/5] Done"
echo "Backend log: $BACKEND_LOG"
