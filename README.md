# Sobre o projeto modelo

## Docker quick start (run in any machine)

1. Install Docker Engine/Desktop and Docker Compose plugin.
2. Start Docker daemon:

    ```bash
    sudo systemctl start docker
    sudo systemctl enable docker
    ```

3. (Optional, recommended) allow running docker without sudo:

    ```bash
    sudo usermod -aG docker "$USER"
    # log out and log in again
    ```

4. Create environment file:

    ```bash
    cp .env.example .env
    ```

5. Start everything:

    ```bash
    ./run.sh
    ```

6. Stop and remove containers:

    ```bash
    docker compose down
    ```

### Common error: Docker socket not found

If you see:

`failed to connect to the docker API at unix:///var/run/docker.sock ... no such file or directory`

it means the Docker daemon is not running. Start it with:

```bash
sudo systemctl start docker
```

O projeto modelo apresenta uma estrutura de pacotes indicativa. Apesar de não ser obrigatória, é recomendada para simplificar o desenvolvimento desta fase e das posteriores. A equipa tem liberdade de alterar qualquer ficheiro do projeto durante o seu desenvolvimento.

# Implementacao do Projeto (Estado Atual)

Esta seccao descreve o que esta efetivamente implementado no repositorio, com base no codigo atual.

## Stack Tecnologica

- Backend: Spring Boot 3.0.5 (Java 17)
- Persistencia: Spring Data JPA + Hibernate
- Base de dados: PostgreSQL (em Docker)
- Documentacao da API: SpringDoc OpenAPI/Swagger UI
- Testes: JUnit + Spring Boot Test

Dependencias principais em `pom.xml`: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `springdoc-openapi-starter-webmvc-ui`, `postgresql`.

## Arquitetura e Organizacao

O projeto segue uma arquitetura em camadas:

- `controllers`: camada HTTP/REST
- `service`: regras de negocio e validacoes
- `domain`: modelo de dominio rico (entidades, estados e comportamentos)
- `repository`: acesso a dados com interfaces Spring Data
- `dto`: contratos de entrada/saida da API
- `config`: configuracao (incluindo OpenAPI)

Pacote base: `src/main/java/pt/ul/fc/css/tascaeats`.

## Funcionalidades Implementadas por Modulo

### 1) Utilizadores e Autenticacao Mock

Implementado em `UtilizadorController` + `UtilizadorService`:

- Registo de utilizadores (`client`, `driver/entregador`, `admin/restaurante admin`)
- Login mock (password aceite se o email existir)
- Consultar, atualizar e remover utilizadores
- Validacoes de email, tipo de utilizador e morada para cliente

Endpoints base: `/api/register`, `/api/login`, `/api/Utilizadors`.

### 2) Restaurantes

Implementado em `RestauranteController` + `RestauranteService`:

- Criacao de restaurante associada a administrador
- Atualizacao de dados (nome, nif, morada)
- Gestao de estado (`ABERTO`, `FECHADO`, `RESERVADO`)
- Pesquisa/listagem por nome, cidade e estado

Endpoint base: `/api/restaurantes`.

### 3) Produtos (Cardapio)

Implementado em `ProdutoController` + `ProdutoService`:

- Criacao de produto por restaurante
- Atualizacao de produto
- Atualizacao de disponibilidade
- Soft delete de produto (`apagar`) no dominio
- Listagens com filtros (nome, restaurante, ativos, disponiveis)

Endpoints base: `/api/restaurantes/{restauranteId}/produtos` e `/api/produtos`.

### 4) Pedidos

Implementado em `PedidoController` + `PedidoService`:

- Criacao de pedido com validacao de cliente, restaurante e produtos
- Validacao de disponibilidade de restaurante e produtos
- Calculo do valor total do pedido
- Cancelamento condicionado ao estado (`CRIADO` ou `PAGO`)
- Listagem de pedidos com filtros opcionais (`clienteId`, `estado`)

Endpoint base: `/pedidos`.

### 5) Pagamentos

Implementado em `PagamentoController` + `PagamentoService` + `PagamentoFactory`:

- Processamento de pagamento para pedido
- Criacao polimorfica do pagamento (ex.: MBWay, Multibanco, Dinheiro, Cartao)
- Associacao do pagamento ao pedido e transicao de estado para pago

Endpoint base: `/pagamentos`.

### 6) Entregas e Fluxo Logistico

Implementado em `EntregaController` + `EntregaService`:

- Inicio da preparacao do pedido
- Marcacao de pedido pronto para entrega
- Atribuicao automatica de entregador disponivel
- Registo de recolha e entrega final
- Atualizacao da disponibilidade do entregador no fim da entrega

Endpoints base: `/entregas`.

## Regras de Negocio Cobertas

Entre as regras atualmente implementadas:

- Nao permitir pedido para restaurante fechado/indisponivel
- Nao permitir adicionar produto indisponivel a pedido
- Nao permitir cancelar pedido fora dos estados permitidos
- Nao atribuir entrega se o pedido nao estiver pronto para entrega
- Nao atribuir pedido se nao houver entregadores disponiveis
- Nao permitir alterar disponibilidade/atualizar produto apagado

## API e Documentacao

- OpenAPI configurado em `OpenApiConfig`
- Swagger UI disponivel em runtime (tipicamente):
    - `http://localhost:8080/swagger-ui/index.html`

Lista detalhada dos casos de uso e rotas: ficheiro `USE_CASES_API_ROUTES.md`.

## Persistencia e Execucao

- Configuracao JPA/DB em `src/main/resources/application.properties`
- Infraestrutura Docker em `docker-compose.yml` com:
    - Servico PostgreSQL (`pgserver`)
    - Servico aplicacao Spring Boot (`springbootapp`)

Comando recomendado para correr o projeto:

```bash
./run.sh
```

## Testes Implementados

O projeto contem testes para dominio, servicos, controllers e repositories, incluindo:

- Testes de dominio (`Pedido`, `Produto`, `Restaurante`, `Pagamento*`, `Entrega`, estados)
- Testes de servicos (`PedidoService`, `PagamentoService`, `EntregaService`, `ProdutoService`, `RestauranteService`, utilizadores)
- Testes de controllers (utilizadores, restaurantes, produtos, pedidos, pagamentos, entregas)
- Testes de repositories Spring Data

Diretorio principal de testes: `src/test/java/pt/ul/fc/css/tascaeats`.

## Equipa

Grupo CSS 25:

- Omeir Haroon (61810)
- Isa David (61871)
- Diogo Roque (61857)


# Contextualização

A consultoria espanhola **Compadres Sempre Saciados (CSS)** identificou uma oportunidade no mercado de entregas ao domicílio em Portugal e decidiu criar o **TascaEats**, um sistema de delivery de comida. O objetivo é desenvolver uma plataforma robusta que conecte clientes, restaurantes e entregadores.

Um utilizador pode interagir com o sistema com diferentes perfis: como **Clientee**, como **Administrador do Restaurante** ou como **Entregador**. Todos partilham dados base de autenticação e identificação, mas têm permissões e papéis distintos. Para já, um cliente terá só uma morada (no futuro poderá ter várias). O sistema deve permitir o registo, a atualização e a remoção de utilizadores, bem como a autenticação.

Para a gestão de restaurantes, o sistema permite a um administrador criar e gerir estabelecimentos (nome, NIF e morada). O restaurante possui um cardápio composto por vários produtos (nome, descrição, preço e estado de disponibilidade) e pode abrir e fechar para receber pedidos. 

No processo de compra, o cliente seleciona produtos e cria um pedido (associado a data/hora, valor total, endereço de entrega e estado atual). É obrigatório que cada pedido tenha um pagamento associado (Multibanco, MBWay, Dinheiro, etc.). Por fim, a plataforma gere a logística: aloca entregadores disponíveis a pedidos prontos e mantém o registo das entregas (entregador, pedido e horários de retirada e entrega).

Nesta **Fase 1**, o foco será o desenvolvimento do backend como um sistema monolítico, expondo a lógica de negócio exclusivamente através de uma API REST.

# Casos de Uso

- A. **Login com autenticação:** Vamos fazer *mock*. Qualquer palavra-passe será aceite contanto que o utilizador seja válido.
- B. **Registo de utilizadores:** Criação de perfis de Clientees, Administradores e Entregadores.
- C. **Gerir utilizadores:** Verificar, remover e atualizar utilizadores.
- D. **Buscar restaurantes:** Pesquisa por nome ou cidade.
- E. **Gestão de restaurantes:** Criação de restaurantes e gestão do seu estado (aberto/fechado).
- F. **Gestão de cardápio:** Adicionar, remover e atualizar produtos no cardápio de um restaurante.
- G. **Buscar produtos:** Listar os produtos de um restaurante.
- H. **Criação de pedido:** Efetuar um pedido no sistema.
- I. **Processar pagamento:** Registo e processamento do pagamento associado a um pedido.
- J. **Atualizar pedido:** Atualização do estado do pedido (ex: Criado, Pago, Em preparo, etc.).
- K. **Alocação logística:** Atribuição de um entregador a um pedido.
- L. **Cancelar pedido:** Cancelamento de um pedido existente (mediante regras de negócio).

# Requisitos Não Funcionais

O projeto tem ainda os seguintes requisitos:

- O projeto deve ser desenvolvido por grupos de **no máximo três alunos**.
- A autenticação poderá ser feita via *mock* (futura integração com sistema real).
- Toda a informação deverá ser armazenada numa **base de dados relacional**.
- A plataforma deverá ser implementada em **Spring Boot**, usando Java. A camada de dados será implementada utilizando **JPA/Spring Data**.
- A aplicação deverá lidar com **pedidos concorrentes**, sem criar inconsistências.
- A camada de negócios deverá usar um **Domain Model rico** e fazer o uso correto de anotações JPA (incluindo herança e composição onde aplicável) para facilitar o mapeamento.
- A implementação deve utilizar os conceitos vistos em aula (ex., camadas, DTOs, controllers, testes, etc.) e seguir as boas práticas de desenvolvimento de software.
- A interface será através de *endpoints REST*. Deve ser possível aceder diretamente a esses *endpoints* através do **Swagger** (`https://swagger.io/`). Não haverá desenvolvimento de interface gráfica (HTML/GUI) nesta fase.
- O repositório deve aceitar apenas código com o nível de qualidade aceite pela equipa (ex.: *pre-commit*, testes unitários/integração com 80% de cobertura mínima recomendada).
- O projeto correrá **sempre num ambiente Docker**.

# Tarefas da Fase 1

A equipa deverá:
- Fazer fork do repositório original (`https://git.alunos.di.fc.ul.pt/css000/tascaeats`), para a conta de um dos elementos do grupo.
- Dar acesso a todos membros do grupo, como *Maintainer*.
- Dar acesso à conta css000, como *Reporter*.
- Implementar os *endpoints REST* necessários (com acesso através do Swagger) para os casos de uso exigidos.
- **Gravar um pequeno vídeo** demonstrando os casos de uso a funcionar. A demonstração deve ser feita consumindo a API REST via Swagger (mostrando o envio do JSON de requisição e a resposta/alteração na base de dados).
- Alterar o ficheiro `README.md` para identificar a equipa e **incluir os links para o(s) vídeo(s) demonstrativo(s)** (Google Drive, YouTube ou no próprio repositório).

### Validação e Regras de Negócio
Devem garantir que as regras de negócio da aplicação estão implementadas e demonstradas nos vídeos. Exemplos:
- Um pedido não pode ser cancelado se já estiver em preparo.
- Um produto associado a um pedido antigo não pode ser completamente removido do cardápio (soft-delete).
- Não é possível fazer pedido a restaurantes fechados.
- Não devem permitir adicionar um produto ao pedido se estiver esgotado.
- Não devem associar uma entrega a um entregador que não está disponível.

Para validar o funcionamento e a modelação, a base de dados gerada deve ser capaz de responder a questões de negócio como:
1. Quais os restaurantes que possuem o maior volume de vendas em euros?
2. Qual é a morada do restaurante com mais vendas? E do cliente com mais pedidos?
3. Em média, quantos pedidos um cliente realiza por mês?
4. Quais os produtos mais vendidos da plataforma?
5. Qual é o método de pagamento mais utilizado pelos clientes?
6. Quais os clientes que se registaram mas ainda não realizaram nenhuma compra?

# Como Entregar

Para entregar o trabalho, basta criar uma tag chamada *fase1* e enviá-la para o repositório. O repositório deverá conter o código-fonte do projeto, o ficheiro `docker-compose.yml` necessário para correr a aplicação e a base de dados, e o ficheiro `README.md` devidamente atualizado com os links dos vídeos.

    git tag fase1
    git push origin fase1

Deve-se confirmar que o vosso projeto está acessível à conta CSS000 na tag fase1. Caso contrário, terão 0 nesta entrega. É importante garantir que o ambiente de execução possa ser reproduzido pela equipa docente sem erros de compilação. O projeto **obrigatoriamente** deverá correr num ambiente Docker. Falhas em **EXECUTAR** o projeto, num ambiente Docker, pela equipa docente, resultarão em **grave penalização nesta entrega**. Casos de uso não apresentados ou com falhas de execução serão considerados como não implementados.

*Nota sobre IA e Plágio: É permitido o uso de IA (ChatGPT, Copilot, DeepSeek, etc.), mas todos os membros devem ser capazes de compreender e explicar o projeto na sua totalidade. Código não explicável será considerado plágio.*

# Critérios de Avaliação

Esta entrega corresponde a **35% da nota do projeto**. Segue uma lista de critérios que serão avaliados:

## Processo de Desenvolvimento e Demonstração
- Granularidade e frequência dos *commits*. Todos os membros do grupo devem contribuir de forma equitativa. *Disparidades significativas serão penalizadas*.
- Qualidade das mensagens de *commit*.
- Clareza na demonstração em vídeo, funcionamento real da API REST apresentada e capacidade de explicação do trabalho desenvolvido pelo grupo.
- Quantidade e qualidade das regras de negócio implementadas e demonstradas nos vídeos.

## Requisitos e Implementação
- Em que medida os requisitos funcionais e não funcionais são abrangidos pela modelação do *Domain Model*.
- Implementação da REST API com os *endpoints* adequados e boas práticas (status codes, verbos HTTP corretos).
- Uso correto de anotações JPA, herança e relações de cardinalidade.
- Inclusão de testes unitários e de integração que validem a lógica de negócio e os cenários de exceção.


## GIT ORGANIZATION

| Goal | Command | What happens? | Works offline? |
| --- | --- | --- | --- |
| Save work locally | `git commit -m "msg"` | Saves a snapshot to your laptop. | Yes |
| Push to BOTH | `git push origin` | Tries GitLab first, then GitHub. | Partial (GitHub succeeds, GitLab times out) |
| Check status | `git remote -v` | Shows your current push/fetch URLs. | Yes |
| Get latest code | `git pull origin` | Downloads updates from GitLab. | No (needs Uni network) |
| See hidden history | `git log --oneline` | Shows all commits you've made (even if not pushed). | Yes |