# TascaEats - Fase 3

Projeto da disciplina de Construcao de Sistemas de Software.

## Grupo 25

- Omeir Haroon (61810)
- Isa David (61871)
- Diogo Roque (61857)

## Video de demonstracao

- https://drive.google.com/drive/folders/13frYUwmqDdxDYaoB1LOq3DhkCax7dhJe?usp=sharing

## Stack final

- Interface Web servida pelo Nginx em `http://localhost`
- API publica do backend servida pelo Nginx em `http://localhost`
- GUI nativa via gRPC em `http://localhost:8082`
- PostgreSQL separado para o monolito e para o microservico de entregas
- Comunicacao entre backend e microservico apenas por Kafka

## Execucao

```bash
docker compose up --build
```

Tambem podes usar:

```bash
./run.sh
```

## Estrutura

- `backend/` - monolito Spring Boot
- `web/` - frontend web e GUI JavaFX
- `delivery-service/` - microservico de gestao de entregas
- `docker-compose.yml` - orquestracao local
- `nginx.conf` - reverse proxy

## Notas de entrega

- A tag final deve chamar-se `fase3`
- O microservico de entregas usa a sua propria base de dados
- O monolito nao pode aceder diretamente a base de dados do microservico
