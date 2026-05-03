# TascaEats Fase 1

## Grupo 25

- Omeir Haroon (61810)
- Isa David (61871)
- Diogo Roque (61857)

## LINK DO VÍDEO
- https://drive.google.com/drive/folders/13frYUwmqDdxDYaoB1LOq3DhkCax7dhJe?usp=sharing


## Índice

- [TascaEats Fase 1](#tascaeats-fase-1)
  - [LINK DO VIDEO](#link-do-video)
  - [Grupo 25](#grupo-25)
  - [Índice](#índice)
  - [Tecnologias](#tecnologias)
  - [Arquitetura](#arquitetura)
  - [Como executar:](#como-executar)
    - [Docker](#docker)
    - [Docker daemon indisponível](#docker-daemon-indisponível)
  - [API e Swagger](#api-e-swagger)
  - [Funcionalidades implementadas:](#funcionalidades-implementadas)
    - [Utilizadores](#utilizadores)
    - [Restaurantes](#restaurantes)
    - [Produtos](#produtos)
    - [Pedidos](#pedidos)
    - [Pagamentos](#pagamentos)
    - [Entregas](#entregas)
  - [Regras de negócio](#regras-de-negócio)
  - [Testes](#testes)

## Tecnologias

- Java 17
- Spring Boot 3.0.5
- Spring Data JPA + Hibernate
- PostgreSQL
- OpenAPI/Swagger (`springdoc-openapi`)
- JUnit + Spring Boot Test

Dependências principais em `pom.xml`:

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `springdoc-openapi-starter-webmvc-ui`
- `postgresql`

## Arquitetura

O projeto está organizado por camadas no pacote `src/main/java/pt/ul/fc/css/tascaeats`:

- `controllers`: endpoints HTTP/REST
- `service`: regras de negócio e validações
- `domain`: modelo de domínio
- `repository`: acesso a dados (Spring Data)
- `dto`: contratos de entrada/saída
- `config`: configuração da aplicação

## Como executar:

### Docker

1. Instalar Docker Engine/Desktop e Docker Compose.
2. Criar ficheiro de ambiente:

```bash
cp .env.example .env
```

3. Iniciar a aplicação:

```bash
./run.sh
```

4. Parar os serviços:

```bash
docker compose down
```

### Docker daemon indisponível

Se surgir um erro semelhante a `failed to connect to the docker API ...`, iniciar o daemon Docker (Linux):

```bash
sudo systemctl start docker
```

### GUI nativa com JavaFX

A aplicação desktop JavaFX está no módulo `web` e consome a API REST do backend.

1. Iniciar o backend normalmente (Docker ou `spring-boot:run`).
2. No diretório `web`, arrancar a GUI:

```bash
BACKEND_API_URL=http://localhost:8080 mvn javafx:run
```

Se o backend estiver noutro endereço, ajustar a variável `BACKEND_API_URL`.

#### Funcionalidades atuais

- **Visão de Restaurantes**: lista, pesquisa por nome/estado, detalhes de cada restaurante
- **Visão de Casos de Uso**: comparação do estado de implementação entre web browser e native JavaFX
- **Menu de navegação**: alternar entre vistas; botão Sair para encerrar

## API e Swagger

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Rotas e exemplos de payload: `USE_CASES_API_ROUTES.md`

## Nota sobre o desenvolvimento

- A **webapp browser (Thymeleaf)** está a ser desenvolvida por um colega. Use o módulo `web` com a webapp base que
  ficou (ficheiros restaurantes.html e 404.html).
- A **app nativa JavaFX** dispõe de uma aba "Casos de Uso" onde se pode acompanhar o progresso de implementação dos
  diferentes caso de uso.

## Funcionalidades implementadas:

### Utilizadores

- Registo de utilizadores (`client`, `admin`, `driver`)
- Login mock
- Consulta, atualização e remoção de utilizadores

Endpoints base: `/api/register`, `/api/login`, `/api/Utilizadors`

### Restaurantes

- Criação de restaurante por administrador
- Atualização de nome, NIF e morada
- Gestão de estado (`ABERTO`, `FECHADO`, `RESERVADO`)
- Pesquisa por nome/cidade e listagem por estado

Endpoint base: `/api/restaurantes`

### Produtos

- Criação/atualização de produtos
- Atualização de disponibilidade
- Soft delete (`apagar`)
- Filtros por nome, restaurante, ativos e disponíveis

Endpoints base: `/api/restaurantes/{restauranteId}/produtos` e `/api/produtos`

### Pedidos

- Criação de pedido com validação de cliente/restaurante/produtos
- Validação de disponibilidade de restaurante e produtos
- Cálculo de valor total no serviço
- Cancelamento permitido apenas em `CRIADO` e `PAGO`
- Listagem por `clienteId` e `estado`

Endpoint base: `/pedidos`

### Pagamentos

- Processamento de pagamento por pedido
- Criação via `PagamentoFactory` (MBWay, Multibanco, Dinheiro, Cartão)
- Associação do pagamento ao pedido e transição para `PAGO`

Endpoint base: `/pagamentos`

### Entregas

- Iniciar preparação
- Marcar pedido como pronto
- Atribuição automática de entregador disponível
- Registar recolha e entrega final
- Libertar entregador no fim da entrega

Endpoint base: `/entregas`

## Regras de negócio

- Não permitir pedido para restaurante fechado/indisponível
- Não permitir pedido com produtos indisponíveis
- Não permitir cancelar pedido fora dos estados permitidos
- Não permitir atribuição de entrega a pedido ainda não pronto
- Não permitir atribuição quando não existem entregadores disponíveis
- Não permitir atualizar disponibilidade de produto apagado

## Testes

Existem testes de:

- Domínio
- Serviços
- Controllers
- -DTO's
- Repositórios (`@DataJpaTest`)

Diretório principal: `src/test/java/pt/ul/fc/css/tascaeats`