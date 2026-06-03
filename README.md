
# TascaEats Fase 3

## Grupo 25

* Omeir Haroon (61810)
* Isa David (61871)
* Diogo Roque (61857)

## LINK DO VÍDEO
* [https://drive.google.com/drive/folders/13frYUwmqDdxDYaoB1LOq3DhkCax7dhJe?usp=sharing](https://drive.google.com/drive/folders/13frYUwmqDdxDYaoB1LOq3DhkCax7dhJe?usp=sharing)

## Ponto de Situação
* **Interface Web:** é servida pelo Nginx em `http://localhost` (porta 80 no host).
* **API pública do backend:** também é exposta pelo Nginx em `http://localhost`.
* **GUI nativa (gRPC):** usa `http://localhost:8082` no host, via Nginx.
* **Base de Dados:** PostgreSQL em contentores separados para o monólito e para o microserviço de entregas.

## Limitações Notáveis
* A interface gráfica (GUI) não pode alterar o estado dos pedidos. As alterações de estado são realizadas apenas pelo fluxo de trabalho do backend.
* Cancelar um pedido a partir da GUI mostra um erro de UI/traceback, mas o cancelamento é concluído com sucesso no servidor; considere isto um *bug* estético.


Este projeto inclui o trabalho de migração para a Fase 3 (ver `docs/fase3.pdf`), incluindo:

* Método de pagamento selecionado pelo cliente persistido com o pedido (`tipo_pagamento_solicitado` + `pedido_pagamento_detalhes`).
* O *checkout* transmite a seleção de pagamento para o backend e esta é armazenada no `Pedido`.
* *Endpoint* dedicado para a conclusão do pagamento: `POST /pagamentos` para adicionar um `Pagamento` concluído a um pedido existente.
* A vista web `pedido_detalhe` mostra o método de pagamento selecionado pelo cliente e fornece um formulário específico ao método para concluir o pagamento.
* A GUI nativa exibe a seleção de pagamento escolhida pelo cliente e permite a submissão dos detalhes de pagamento (a GUI não pode substituir a seleção do cliente).

Estas alterações requerem uma migração na base de dados para adicionar as novas colunas/tabela de coleção (ver Base de Dados / Migrações abaixo).

## Requisitos
* Docker e Docker Compose (preferencial)
* Java 17 e Maven (para *builds* locais sem Docker)

## Executar (Docker Compose)
Construir e executar todos os serviços (monólito, web, microserviço de entregas, Kafka, bases de dados e Nginx):

```bash
./run.sh
# ou
docker-compose up --build
```

A Interface Web estará disponível em: http://localhost
A API pública do backend estará disponível em: http://localhost
A GUI nativa estará disponível em: http://localhost:8082

## Executar localmente sem Docker
* **Backend:** compilar com Maven em `backend/` e executar o ficheiro `.jar` gerado.
* **Web:** compilar com Maven em `web/` e executar o ficheiro `.jar` gerado.
* O `docker-compose.yml` já levanta as instâncias de PostgreSQL necessárias, o Kafka, o Nginx, o monólito e o microserviço de entregas.

### JavaFX (GUI nativa)
A GUI nativa em JavaFX é compilada juntamente com o módulo `web`. Para executar a aplicação JavaFX a partir do código-fonte:

```bash
# a partir da raiz do projeto (recomendado)
cd web
mvn javafx:run

# ou a partir da raiz usando a execução multi-módulo do Maven
mvn -pl web -am javafx:run
```

Se a execução do JavaFX falhar, certifique-se de que tem um JDK compatível (Java 17) e o plugin `org.openjfx` configurado na secção de plugins do ficheiro `web/pom.xml`.

## Estrutura do Projeto (alto nível)
* `backend/` — Backend em Spring Boot, modelo de domínio, serviços, controladores.
* `web/` — Frontend web em Spring MVC + Thymeleaf (templates em `web/src/main/resources/templates`).
* `web/src/main/java/.../ui/` — Componentes da GUI nativa em JavaFX usados separadamente.
* `docs/`, `target/`, recursos de teste e outros artefactos do projeto.

## Estrutura do Projeto (Alinhamento com a Fase 1)
A estrutura deste README segue as convenções de documentação da Fase 1 (ver `docs/p1.pdf`). Áreas principais:

* `backend/` — contém o modelo de domínio em `src/main/java`, repositórios e serviços Spring; `src/test` contém os testes unitários.
* `web/` — contém os controladores web, templates Thymeleaf e código da UI em JavaFX em `ui/`.
* `docs/` — artefactos de design, PDFs de casos de uso e coleções Postman usados para validação e vídeos de demonstração.
* `docker-compose.yml`, `run.sh` e `bootstrapdb.sql` — scripts auxiliares para iniciar a *stack* e preencher a base de dados.

## Base de Dados / Migrações

Este projeto armazena alguns metadados de seleção de pagamento em `Pedido` e usa `Menu` como entidade própria
reutilizável por vários restaurantes/franquias. Se usar uma base de dados persistente, deve garantir que o esquema
contém as novas colunas/tabelas e que as ligações antigas restaurante-produto foram migradas para menu-produto.

Para pagamento, adicione:

* Coluna `tipo_pagamento_solicitado` em `pedido` (VARCHAR)
* Tabela de coleção de elementos `pedido_pagamento_detalhes` com `pedido_id`, `campo`, `valor`

Para menus partilháveis, garanta:

* Tabela `menus` com `id` e `nome`
* Coluna `menu_id` em `restaurante`, permitindo restaurantes sem menu temporariamente
* Coluna `menu_id` em `produto`
* Coluna `restaurante_id` em `produto` sem `NOT NULL`, porque produtos pertencem ao menu e não a uma loja concreta

Sem estas adições, a criação/listagem de produtos por menu e a atribuição de menus a restaurantes poderá falhar.

Exemplo de SQL (migração manual) - executar uma vez contra a base de dados usada pelo backend:

```sql
ALTER TABLE pedido ADD COLUMN tipo_pagamento_solicitado VARCHAR;

CREATE TABLE pedido_pagamento_detalhes (
    pedido_id bigint NOT NULL,
    campo varchar NOT NULL,
    valor varchar,
    CONSTRAINT fk_pedido_pagamento_detalhes_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id)
);

ALTER TABLE restaurante ALTER COLUMN menu_id DROP NOT NULL;
ALTER TABLE produto ADD COLUMN IF NOT EXISTS menu_id bigint;
ALTER TABLE produto ALTER COLUMN restaurante_id DROP NOT NULL;

UPDATE produto p
SET menu_id = r.menu_id
FROM restaurante r
WHERE p.restaurante_id = r.id
  AND p.menu_id IS NULL
  AND r.menu_id IS NOT NULL;

-- Se usar sequências para dados pré-preenchidos, avance as sequências após inserir IDs explícitos
-- ex: SELECT setval('pedido_id_seq', (SELECT MAX(id) FROM pedido));
```

## API (endpoints importantes)
* `POST /pedidos` — criar pedido (o *payload* inclui `tipoPagamentoSolicitado` e `detalhesPagamentoSolicitado`)
* `GET /pedidos?clienteId={id}` — listar pedidos de um cliente
* `POST /pagamentos` — processar/concluir pagamento para um pedido existente

Endpoints adicionais usados pela interface web:
* `POST /checkout/processar` (controlador web) — constrói o *payload* do `POST /pedidos` incluindo a seleção de pagamento
* `GET /pedidos/{id}` (web) — renderiza o template `pedido_detalhe` com as informações de pagamento selecionadas e concluídas

## Resolução de Problemas
* Se a página `/pedidos/{id}` mostrar uma página de erro *Whitelabel*: verifique os registos (*logs*) do backend e confirme se o backend está acessível em `http://localhost:8080` (ou no endereço configurado em `BACKEND_API_URL`).
* Se os campos de pagamento estiverem em falta na interface, confirme se a migração da base de dados foi aplicada (ver Base de Dados / Migrações acima).
* A ação de cancelar na GUI mostra um erro mas é concluída: verifique os *logs* do servidor para confirmar que o cancelamento foi bem-sucedido; o *bug* da interface pode ser corrigido ajustando o tratamento da resposta de cancelamento no controlador `web`.

Se a interface de *checkout* ou de pagamento parecer não atualizar (valores desatualizados): verifique se `checkout.html` inclui os campos de pagamento com os nomes `referencia`, `numeroMbway`, `bandeiraCartao` e se o controlador web está configurado para os mapear para `tipoPagamentoSolicitado` / `detalhesPagamentoSolicitado` no *payload* do pedido.
