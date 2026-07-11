# Guião De Gravação - Fase 3 Sem Áudio

Vídeo sem áudio, sem cortes, com texto curto no ecrã quando necessário.

Objetivo: demonstrar os casos de uso diretamente alterados pela Fase 3 e os casos que foram modificados por consequência da nova arquitetura.

## O Que Dá Para Mostrar Por Interface

### Browser

Usar preferencialmente `http://localhost`, porque passa pelo Nginx.

Pode ser mostrado no browser:

- Interface web principal.
- Checkout com seleção do método de pagamento, se já houver sessão de cliente preparada.
- Página de detalhe do pedido em `/pedidos/{id}`, que mostra o acompanhamento do pedido e o entregador projetado.
- Atualização automática da página de detalhe do pedido depois dos eventos Kafka serem consumidos pelo monólito.

Não é demonstrável diretamente no browser neste estado do projeto:

- Mensagens Kafka.
- Base de dados isolada do microserviço.
- Gestão/filtro de entregadores do microserviço.
- Processamento do pagamento por interface web. O endpoint existe em `POST /pagamentos`, mas a gravação deve usar `curl`.

### GUI JavaFX

Pode ser mostrado pela GUI:

- Uma demonstração curta de JavaFX/gRPC via Nginx.
- A área de produtos, para provar que a funcionalidade alterada por consequência continua a funcionar.

Não usar a GUI para repetir fluxos completos antigos de gestão de produtos.

### Terminal

Tem de ser mostrado por terminal:

- Estado dos contentores.
- Tópicos Kafka.
- Consumidores Kafka.
- Queries SQL nas duas bases de dados.
- Chamadas `curl` para pagamento e comandos de entrega.

## Casos A Demonstrar

Diretamente da Fase 3:

- Gestão/filtro de entregadores no microserviço.
- Atribuição de entregador a pedido pago.
- Atualização do estado da entrega.
- Kafka entre monólito e microserviço.
- Base de dados isolada do microserviço.
- Consistência eventual no monólito.

Alterados por consequência:

- Pagamento: ao ser processado, publica evento Kafka.
- Pedidos: passam a mostrar `entregador_id` e `estado_entrega_read_only` vindos do microserviço.
- GUI JavaFX/gRPC via Nginx: demonstrar rapidamente que uma funcionalidade alterada, como produtos em `ProdutosPane.java`, continua a funcionar.
- Query SQL obrigatória.

Não demonstrar como fluxo completo:

- Login.
- Registo completo de clientes/admins.
- Gestão completa de restaurantes, menus ou produtos.
- Avaliações.
- Cancelamento.

## Preparação Antes De Gravar

Fazer isto antes de começar o vídeo.

### Terminal 0 - Stack Docker

Abrir um terminal na raiz do projeto:

```bash
cd /home/oharoon/Desktop/tascaeats
docker compose up --build
```

Deixar este terminal aberto e esperar a stack estabilizar.

### Terminal 1 - Comandos Principais

Abrir outro terminal na raiz do projeto:

```bash
cd /home/oharoon/Desktop/tascaeats
```

Preparar um pedido `CRIADO`:

```bash
PEDIDO_ID=$(docker compose exec -T pgserver psql -U Utilizador -d postgres -Atc "
SELECT id FROM pedidos WHERE estado = 'CRIADO' ORDER BY id DESC LIMIT 1;
")
echo "$PEDIDO_ID"
```

Se não aparecer ID, criar um pedido antes da gravação:

```bash
curl -s -X POST http://localhost/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "itens": [
      { "produtoId": 1, "restauranteId": 1, "quantidade": 1 }
    ],
    "tipoPagamentoSolicitado": "MBWAY",
    "detalhesPagamentoSolicitado": {
      "numeroTelemovel": "912345678"
    }
  }'
```

Voltar a guardar o ID:

```bash
PEDIDO_ID=$(docker compose exec -T pgserver psql -U Utilizador -d postgres -Atc "
SELECT id FROM pedidos WHERE estado = 'CRIADO' ORDER BY id DESC LIMIT 1;
")
echo "$PEDIDO_ID"
```

Copiar este valor, porque também vai ser usado no Terminal 3. Exemplo:

```bash
# substituir 123 pelo valor mostrado no echo anterior
PEDIDO_ID=123
```

### Terminal 2 - Consumidores Kafka

Abrir outro terminal na raiz do projeto:

```bash
cd /home/oharoon/Desktop/tascaeats
```

Este terminal fica parado até ser pedido abrir um consumidor Kafka.

### Terminal 3 - Queries SQL E Logs

Abrir outro terminal na raiz do projeto:

```bash
cd /home/oharoon/Desktop/tascaeats
```

Este terminal fica reservado para queries SQL e, se necessário, logs:

```bash
# substituir 123 pelo valor mostrado no Terminal 1
PEDIDO_ID=123
```

Não iniciar logs durante a gravação normal. Usar apenas para diagnóstico:

```bash
docker compose logs -f backend delivery-backend
```

### Browser Opcional

Abrir:

```text
http://localhost
```

Se já houver sessão de cliente preparada, deixar aberta a página:

```text
http://localhost/pedidos/{PEDIDO_ID}
```

Substituir `{PEDIDO_ID}` pelo ID mostrado no Terminal 1.

## Texto Inicial Para Mostrar

Mostrar 5 segundos num editor:

```text
TascaEats - Fase 3
Sem áudio e sem cortes.
Serão demonstrados os casos alterados pela Fase 3 e os casos afetados pela nova arquitetura:
entregas, pagamento com evento Kafka, pedidos com estado de entrega read-only, JavaFX/gRPC via Nginx e queries SQL.
```

## Passos Da Gravação

### 1. Stack Docker

No Terminal 1:

```bash
docker compose ps
```

Mostrar que existem:

- `nginx`
- `backend`
- `web`
- `kafka`
- `delivery-backend`
- `pgserver`
- `pgserver-entregas`

Texto opcional:

```text
Stack da Fase 3: monólito, microserviço de entregas, Kafka, Nginx e duas bases de dados.
```

### 2. Tópicos Kafka

No Terminal 1:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server kafka:9092 \
  --list
```

Confirmar que aparecem:

```text
pedido-pago-topic
delivery-command-topic
entrega-atualizada-topic
```

Texto opcional:

```text
Kafka é usado para comunicação assíncrona entre monólito e microserviço.
```

### 3. Bases De Dados Separadas

No Terminal 3, mostrar a base de dados do monólito:

```bash
docker compose exec pgserver psql -U Utilizador -d postgres -c "\dt"
```

No Terminal 3, mostrar a base de dados do microserviço:

```bash
docker compose exec pgserver-entregas psql -U Utilizador -d delivery_db -c "\dt"
```

Texto opcional:

```text
O microserviço de entregas tem base de dados própria: delivery_db.
```

### 4. Gestão/Filtro De Entregadores

Este passo é por terminal, porque não existe ecrã web para gerir os entregadores do microserviço.

No Terminal 3, listar todos os entregadores:

```bash
docker compose exec pgserver-entregas psql -U Utilizador -d delivery_db -c "
SELECT id, nome, email, available
FROM entregadores
ORDER BY id;
"
```

No Terminal 3, filtrar só disponíveis:

```bash
docker compose exec pgserver-entregas psql -U Utilizador -d delivery_db -c "
SELECT id, nome, email, available
FROM entregadores
WHERE available = true
ORDER BY id;
"
```

Texto opcional:

```text
Caso alterado: entregadores são geridos e filtrados no microserviço de entregas.
```

### 5. Caso Afetado: Pagamento Publica Evento Kafka

No Terminal 2, abrir o consumidor Kafka:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic pedido-pago-topic \
  --from-beginning
```

No Terminal 1, processar o pagamento:

```bash
curl -s -X POST http://localhost/pagamentos \
  -H "Content-Type: application/json" \
  -d "{
    \"pedidoId\": $PEDIDO_ID,
    \"tipoPagamento\": \"MBWAY\",
    \"detalhes\": {
      \"numeroTelemovel\": \"912345678\"
    }
  }"
```

Mostrar a mensagem nova no consumidor do Terminal 2.

No Terminal 2, parar o consumidor com `Ctrl+C`.

Texto opcional:

```text
Caso afetado: o pagamento continua no monólito, mas agora publica PedidoPagoEvent em Kafka.
```

### 6. Caso Alterado: Atribuir Entregador

No Terminal 2, abrir o consumidor Kafka:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic delivery-command-topic \
  --from-beginning
```

No Terminal 1, publicar o comando de atribuição:z

```bash
curl -s -X PATCH http://localhost/entregas/pedido/$PEDIDO_ID/atribuir-entregador
```

Mostrar a mensagem nova no consumidor do Terminal 2.

No Terminal 2, parar o consumidor com `Ctrl+C`.

No Terminal 3, confirmar a entrega criada na base do microserviço:

```bash
docker compose exec pgserver-entregas psql -U Utilizador -d delivery_db -c "
SELECT e.id, e.pedido_id, e.entregador_id, en.nome, e.estado, e.endereco_entrega
FROM entregas e
JOIN entregadores en ON en.id = e.entregador_id
WHERE e.pedido_id = $PEDIDO_ID;
"
```

Texto opcional:

```text
Caso alterado: o monólito publica um comando Kafka; o microserviço atribui o entregador e grava a entrega.
```

### 7. Caso Alterado: Atualizar Estado Da Entrega

No Terminal 2, abrir o consumidor Kafka:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic entrega-atualizada-topic \
  --from-beginning
```

No Terminal 1, registar recolha e entrega final:

```bash
curl -s -X PATCH http://localhost/entregas/pedido/$PEDIDO_ID/recolha
curl -s -X PATCH http://localhost/entregas/pedido/$PEDIDO_ID/entrega-final
```

Mostrar os eventos novos no consumidor do Terminal 2.

No Terminal 2, parar o consumidor com `Ctrl+C`.

No Terminal 3, confirmar o estado real no microserviço:

```bash
docker compose exec pgserver-entregas psql -U Utilizador -d delivery_db -c "
SELECT pedido_id, estado, hora_recolha, hora_entrega
FROM entregas
WHERE pedido_id = $PEDIDO_ID;
"
```

Texto opcional:

```text
Caso alterado: o estado real da entrega é atualizado no microserviço.
```

### 8. Caso Afetado: Pedidos Mostram Projeção Read-Only

No Terminal 3, consultar o pedido no monólito:

```bash
docker compose exec pgserver psql -U Utilizador -d postgres -c "
SELECT id, estado, entregador_id, estado_entrega_read_only
FROM pedidos
WHERE id = $PEDIDO_ID;
"
```

No Terminal 1, consultar o endpoint público via Nginx:

```bash
curl -s http://localhost/entregas/pedido/$PEDIDO_ID
```

Opcional no browser, se houver sessão de cliente preparada:

```text
http://localhost/pedidos/{PEDIDO_ID}
```

Mostrar o acompanhamento do pedido e o entregador. Se a página ainda não mostrar o último estado, aguardar 2 a 3 segundos ou recarregar, porque a projeção é eventual.

Texto opcional:

```text
Caso afetado: pedidos no monólito mostram entregador_id e estado_entrega_read_only recebidos por Kafka.
Isto demonstra consistência eventual.
```

### 9. Caso Afetado: GUI JavaFX/gRPC Via Nginx

Demonstrar rapidamente uma funcionalidade alterada da GUI, sem explicar o caso completo.

No Terminal 1, se a GUI estiver disponível localmente:

```bash
cd /home/oharoon/Desktop/tascaeats/web
mvn javafx:run
```

Na GUI:

1. Abrir a área de produtos.
2. Selecionar um restaurante.
3. Clicar em pesquisar/listar produtos.
4. Mostrar que os produtos carregam via gRPC.
5. Se for seguro, abrir o diálogo de criar/editar produto e mostrar o campo/associação de menu.
6. Fechar o diálogo sem gravar, se não for necessário alterar dados.

Depois, se for continuar a usar o Terminal 1 para comandos:

```bash
cd /home/oharoon/Desktop/tascaeats
```

Texto para mostrar antes desta parte:

```text
Caso afetado por consequência: a GUI JavaFX continua a comunicar por gRPC através do Nginx.
ProdutosPane foi alterado para funcionar com produtos associados a menus.
Demonstração curta: apenas abrir/listar produtos, sem repetir a gestão completa de produtos da Fase 2.
```

Se a GUI não puder ser executada no ambiente da gravação, mostrar o ficheiro:

```text
web/src/main/java/pt/ul/fc/css/tascaeats/ui/ProdutosPane.java
```

E destacar visualmente:

- `syncProdutos(...)`
- `state.grpc.invokeList("listProducts", ...)`
- `createProduct` com `menuId`
- `updateProduct`
- `updateProductAvailability`

### 10. Queries SQL Obrigatórias

No Terminal 3, executar a query do monólito:

```bash
docker compose exec pgserver psql -U Utilizador -d postgres -c "
SELECT
  pg.tipo_pagamento,
  COUNT(*) AS total_utilizacoes
FROM pagamentos pg
GROUP BY pg.tipo_pagamento
ORDER BY total_utilizacoes DESC, pg.tipo_pagamento;
"
```

No Terminal 3, executar a query do microserviço:

```bash
docker compose exec pgserver-entregas psql -U Utilizador -d delivery_db -c "
SELECT
  en.id AS entregador_id,
  en.nome,
  COUNT(e.id) AS total_entregas,
  COUNT(e.hora_entrega) AS entregas_concluidas
FROM entregadores en
LEFT JOIN entregas e ON e.entregador_id = en.id
GROUP BY en.id, en.nome
ORDER BY entregas_concluidas DESC, total_entregas DESC;
"
```

Texto opcional:

```text
Queries SQL: validação de dados do monólito e do microserviço de entregas.
```

### 11. Texto Final

Mostrar 5 segundos:

```text
Fim da demonstração da Fase 3.
Demonstrado:
- entregadores no microserviço;
- pagamento com evento Kafka;
- atribuição de entregador;
- atualização da entrega;
- pedidos com projeção read-only;
- JavaFX/gRPC via Nginx em funcionalidade afetada;
- bases de dados separadas;
- queries SQL.
```

## Ordem Resumida Para Gravar

1. Mostrar texto inicial.
2. Terminal 1: `docker compose ps`.
3. Terminal 1: listar tópicos Kafka.
4. Terminal 3: mostrar tabelas das duas bases de dados.
5. Terminal 3: mostrar entregadores e filtro de disponíveis.
6. Terminal 2: abrir consumidor `pedido-pago-topic`.
7. Terminal 1: processar pagamento.
8. Terminal 2: parar consumidor.
9. Terminal 2: abrir consumidor `delivery-command-topic`.
10. Terminal 1: atribuir entregador.
11. Terminal 2: parar consumidor.
12. Terminal 3: consultar entrega criada em `delivery_db`.
13. Terminal 2: abrir consumidor `entrega-atualizada-topic`.
14. Terminal 1: registar recolha e finalizar entrega.
15. Terminal 2: parar consumidor.
16. Terminal 3: consultar entrega no microserviço.
17. Terminal 3: consultar pedido no monólito com `estado_entrega_read_only`.
18. Terminal 1: consultar endpoint `/entregas/pedido/$PEDIDO_ID`.
19. Browser opcional: mostrar `/pedidos/{PEDIDO_ID}`.
20. Demonstrar rapidamente GUI JavaFX/gRPC em produtos, ou mostrar `ProdutosPane.java` se a GUI não correr.
21. Terminal 3: executar queries SQL.
22. Mostrar texto final.

## Notas Para Evitar Erros

- Usar `http://localhost` para passar pelo Nginx no fluxo principal.
- Não usar `http://localhost:8080` no fluxo principal.
- `http://localhost:8081` abre diretamente a web UI, mas não demonstra Nginx.
- Se Kafka mostrar mensagens antigas por causa de `--from-beginning`, focar na mensagem nova após executar o comando.
- Parar sempre o consumidor Kafka com `Ctrl+C` antes de abrir outro consumidor no Terminal 2.
- Se `PEDIDO_ID` estiver vazio, criar um pedido novo antes de gravar.
- Se o pedido já estiver pago ou já tiver entrega, preparar outro pedido `CRIADO` antes de gravar.
- Depois de correr `mvn javafx:run` dentro de `web`, voltar à raiz com `cd /home/oharoon/Desktop/tascaeats`.
- A página web de detalhe pode demorar alguns segundos a refletir a atualização, porque o monólito recebe a projeção por Kafka.
