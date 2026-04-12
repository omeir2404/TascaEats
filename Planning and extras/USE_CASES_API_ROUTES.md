# TascaEats Use Cases - API Routes

Base URL: `http://localhost:8080`

| Use Case | API Call | Method | Route | Query Params | Request Body | Notes |
|---|---|---|---|---|---|---|
| A) Login | Login user | `POST` | `/api/login` | - | `UtilizadorLoginDTO` | Returns mock login message. |
| B) Registo de utilizadores | Register user | `POST` | `/api/register` | - | `UtilizadorRegistoDTO` | Supports `client`, `admin`, `driver`. |
| C) Verificar utilizadores | List users | `GET` | `/api/Utilizadors` | - | - | Returns `UtilizadorRespostaDTO[]`. |
| C) Verificar utilizadores | Get user by id | `GET` | `/api/Utilizadors/{id}` | - | - | Returns one `UtilizadorRespostaDTO`. |
| C) Atualizar utilizadores | Update user | `PUT` | `/api/Utilizadors/{id}` | - | `UtilizadorUpdateDTO` | If `type=driver`, uses driver update flow. |
| C) Remover utilizadores | Delete user | `DELETE` | `/api/Utilizadors/{id}` | - | - | Soft/hard behavior handled in service. |
| D) Buscar restaurantes por nome | Search restaurants by name | `GET` | `/api/restaurantes` | `nome` | - | Uses `procurarRestaurantePorNome`. |
| D) Buscar restaurantes por cidade | Search restaurants by city | `GET` | `/api/restaurantes` | `cidade` | - | Uses `procurarRestaurantePorCidade`. |
| D) Listar restaurantes por estado | Filter restaurants by state | `GET` | `/api/restaurantes` | `estado` (`ABERTO`, `FECHADO`, `RESERVADO`) | - | Uses `listarPorEstado`. |
| D) Listar todos restaurantes | List all restaurants | `GET` | `/api/restaurantes` | - | - | No filters applied. |
| E) Criacao de restaurantes | Create restaurant | `POST` | `/api/restaurantes` | `adminEmail` | `CriarRestauranteDTO` | Requires admin email query param. |
| E) Atualizar restaurante | Update restaurant | `PUT` | `/api/restaurantes/{id}` | - | `AtualizarRestauranteDTO` | Updates name, nif, address fields. |
| E) Gestao de estado (geral) | Update restaurant state | `PATCH` | `/api/restaurantes/{id}/estado` | - | `AtualizarEstadoRestauranteDTO` | Generic state update endpoint. |
| E) Gestao de estado (abrir) | Open restaurant | `PATCH` | `/api/restaurantes/{id}/abrir` | - | - | Internally sets `ABERTO`. |
| E) Gestao de estado (fechar) | Close restaurant | `PATCH` | `/api/restaurantes/{id}/fechar` | - | - | Internally sets `FECHADO`. |
| E) Ver detalhes restaurante | Get restaurant by id | `GET` | `/api/restaurantes/{id}` | - | - | Detailed response includes `morada`. |
| F) Adicionar produto ao cardapio | Create product for restaurant | `POST` | `/api/restaurantes/{restauranteId}/produtos` | - | `CriarProdutoDTO` | Product linked directly to restaurant. |
| F) Atualizar produto | Update product | `PUT` | `/api/produtos/{id}` | - | `AtualizarProdutoDTO` | Updates name, price, description. |
| F) Atualizar disponibilidade | Toggle product availability | `PATCH` | `/api/produtos/{id}/disponibilidade` | - | `AtualizarDisponibilidadeProdutoDTO` | `disponivel` required. |
| F) Remover produto | Delete product | `DELETE` | `/api/produtos/{id}` | - | - | Returns `204` on success. |
| G) Buscar produto por id | Get product by id | `GET` | `/api/produtos/{id}` | - | - | Returns flat product response. |
| G) Buscar produtos por nome | Search products by name | `GET` | `/api/produtos` | `nome` | - | Case-insensitive search. |
| G) Buscar produtos por restaurante | List products by restaurant | `GET` | `/api/produtos` | `restauranteId` | - | Returns restaurant-specific menu. |
| G) Listar produtos ativos | List non-deleted products | `GET` | `/api/produtos` | `ativos=true` | - | Uses active filter. |
| G) Listar produtos disponiveis | List available products | `GET` | `/api/produtos` | `disponiveis=true` | - | Uses availability filter. |
| G) Listar todos produtos | List all products | `GET` | `/api/produtos` | - | - | No filters applied. |
| H) Criacao de pedido | Create order | `POST` | `/pedidos` | - | `CriarPedidoDTO` | Creates order from client, restaurant, products. Returns `201 Created`. |
| L) Cancelamento de pedido | Cancel order | `PATCH` | `/pedidos/{pedidoId}/cancelar` | - | - | Allowed states validated in service. |
| N) Consultar pedidos | List orders | `GET` | `/pedidos` | `clienteId`, `estado` | - | Optional filters. |
| I) Processamento de pagamento | Process payment | `POST` | `/pagamentos` | - | `ProcessarPagamentoDTO` | Moves order to paid state. |
| N) Iniciar preparacao | Start preparation | `PATCH` | `/entregas/pedido/{pedidoId}/iniciar-preparacao` | - | - | Usually requires order paid. |
| N) Marcar pedido pronto | Mark order ready | `PATCH` | `/entregas/pedido/{pedidoId}/marcar-pronto` | - | - | Sets `PRONTO_PARA_ENTREGA`. |
| P) Atribuir entregador | Assign driver | `POST` | `/entregas/atribuir` | - | `AtribuirEntregadorDTO` | Returns `EntregaResumoDTO`. |
| Q) Registar recolha | Register pickup | `PATCH` | `/entregas/pedido/{pedidoId}/recolha` | - | - | Returns `EntregaResumoDTO`. |
| R) Registar entrega final | Register final delivery | `PATCH` | `/entregas/pedido/{pedidoId}/entrega-final` | - | - | Returns `EntregaResumoDTO`. |

## Minimal Body Templates

### `UtilizadorLoginDTO`
```json
{
  "email": "user@example.com",
  "password": "secret"
}
```

### `UtilizadorRegistoDTO`
```json
{
  "name": "Ana",
  "email": "ana@example.com",
  "password": "secret",
  "type": "client",
  "address": {
    "rua": "Rua A",
    "numero": 10,
    "complemento": "1o Dto",
    "codigoPostal": "1000-100",
    "cidade": "Lisboa",
    "distrito": "Lisboa",
    "pais": "Portugal"
  }
}
```

### `CriarRestauranteDTO`
```json
{
  "nome": "Tasca Eats",
  "nif": "123456789",
  "morada": {
    "rua": "Rua da Tasca",
    "numero": 10,
    "complemento": "1o Dto",
    "codigoPostal": "1000-100",
    "cidade": "Lisboa",
    "distrito": "Lisboa",
    "pais": "Portugal"
  }
}
```

### `CriarProdutoDTO`
```json
{
  "nome": "Bacalhau",
  "preco": 12.5,
  "descricao": "Prato do dia"
}
```

### `CriarPedidoDTO`
```json
{
  "clienteId": 1,
  "restauranteId": 2,
  "produtosIds": [10, 11]
}
```

### `ProcessarPagamentoDTO`
```json
{
  "pedidoId": 1,
  "tipoPagamento": "DINHEIRO",
  "detalhes": {
    "valorRecebido": 20.0
  }
}
```

### `AtribuirEntregadorDTO`
```json
{
  "pedidoId": 1
}
```
