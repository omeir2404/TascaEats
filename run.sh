if command -v docker-compose >/dev/null 2>&1; then
    docker-compose up --build
else
    docker compose up --build
fi
