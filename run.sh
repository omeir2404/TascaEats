#!/usr/bin/env bash
set -euo pipefail

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker CLI not found. Install Docker Engine/Desktop first."
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "Docker daemon is not running or not accessible."
    echo "Linux fix: sudo systemctl start docker && sudo systemctl enable docker"
    echo "If permission denied: sudo usermod -aG docker \"$USER\" and log out/in."
    exit 1
fi

if docker compose version >/dev/null 2>&1; then
    docker compose up --build
elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose up --build
else
    echo "Neither 'docker compose' nor 'docker-compose' is available."
    exit 1
fi
