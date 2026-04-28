#!/usr/bin/env bash
set -euo pipefail

usage() {
    echo "Usage: $0 --yes [--ports 3000,5432,...]"
    echo "Warning: this will delete all Docker containers, images, volumes, networks, build cache,"
    echo "project build artifacts, and Maven caches."
    echo "If --ports is provided, processes listening on those ports will be terminated."
}

CONFIRM="no"
PORTS_CSV=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --yes)
            CONFIRM="yes"
            shift
            ;;
        --ports)
            PORTS_CSV="${2:-}"
            shift 2
            ;;
        *)
            usage
            exit 1
            ;;
    esac
done

if [[ "$CONFIRM" != "yes" ]]; then
    usage
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker not found"
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "Docker daemon is not running or not accessible"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Stopping and removing all containers..."
containers="$(docker ps -aq)"
if [[ -n "${containers}" ]]; then
    docker stop ${containers} >/dev/null 2>&1 || true
    docker rm -f ${containers} >/dev/null 2>&1 || true
fi

echo "Removing all images..."
images="$(docker images -aq)"
if [[ -n "${images}" ]]; then
    docker rmi -f ${images} >/dev/null 2>&1 || true
fi

echo "Removing all volumes..."
volumes="$(docker volume ls -q)"
if [[ -n "${volumes}" ]]; then
    docker volume rm -f ${volumes} >/dev/null 2>&1 || true
fi

echo "Removing custom networks..."
networks="$(docker network ls --filter type=custom -q)"
if [[ -n "${networks}" ]]; then
    docker network rm ${networks} >/dev/null 2>&1 || true
fi

echo "Pruning build cache and unused Docker resources..."
docker builder prune -a -f >/dev/null 2>&1 || true
docker system prune -a --volumes -f >/dev/null 2>&1 || true

echo "Cleaning project build artifacts..."
rm -rf "${SCRIPT_DIR}/target" || true
rm -rf "${SCRIPT_DIR}/.mvn/.cache" || true

echo "Cleaning Maven local caches..."
rm -rf "${HOME}/.m2/repository" || true
rm -rf "${HOME}/.m2/wrapper" || true
mkdir -p "${HOME}/.m2"

free_port() {
    local port="$1"

    if ! [[ "$port" =~ ^[0-9]+$ ]] || (( port < 1 || port > 65535 )); then
        echo "Skipping invalid port: $port"
        return
    fi

    echo "Freeing port $port..."
    local pids=""
    if command -v lsof >/dev/null 2>&1; then
        pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
    elif command -v fuser >/dev/null 2>&1; then
        pids="$(fuser -n tcp "$port" 2>/dev/null || true)"
    else
        echo "Neither lsof nor fuser found; cannot free port $port"
        return
    fi

    if [[ -n "$pids" ]]; then
        kill -TERM $pids >/dev/null 2>&1 || true
        sleep 1
        kill -KILL $pids >/dev/null 2>&1 || true
    fi
}

if [[ -n "$PORTS_CSV" ]]; then
    IFS=',' read -r -a ports <<< "$PORTS_CSV"
    for port in "${ports[@]}"; do
        free_port "$port"
    done
fi

echo "Done."