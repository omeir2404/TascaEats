
## TODO create tests for:

- test create Utilizador
  - [x] valid registration
  - [x] invalid registration (email already exists)
  - [x] invalid registration (name is missing)
  - [x] invalid registration (email is missing)
  - [x] invalid registration (password is missing)
  - [x] invalid registration (type is missing)
  - [x] invalid registration (address is missing)
  - [x] all types of Utilizadores (client, restaurant owner, driver)

- test login Utilizador
  - [x] invalid login (email is missing)
  - [x] valid login (email and password are correct)
  - [x] valid login (email and password are correct, but Utilizador doesn't exist)

- manage Utilizadors
  - [x] update Utilizador
  - [x] get all Utilizadors
  - [x] delete Utilizador
  - [x] get Utilizador by id
  - [x] get Utilizador by email
  - [x] update Utilizador with email that already exists
  - [x] update Utilizador with email that doesn't exist
  - [x] update a Utilizador with a name that already exists
  - [x] update a Utilizador with a name that doesn't exist
  - [x] update a Utilizador with a password that doesn't match
  - [x] update a Utilizador with a password that matches
  - [x] update a Utilizador with a type that doesn't exist
  - [x] update a Utilizador with a type that exists
  - [x] update a Utilizador with an address that doesn't exist
  - [x] update a Utilizador with an address that exists
  - [x] update a Utilizador with an address that is null
  - [x] update a Utilizador with an address that is not null

## Tests made (UseCasesAToLServiceTest)

- [x] A: login succeeds for valid user with arbitrary password
- [x] A: login fails when user does not exist
- [x] B: registration creates client/admin/driver
- [x] B: registration fails when client address is missing
- [x] C: update and delete user for existing user
- [x] C: update fails on email conflict
- [x] D: search restaurants by name
- [x] D: search restaurants by city (pending implementation)
- [x] E: create restaurant and update state to open
- [x] E: update state fails when state is unchanged
- [x] F: create/update/soft-delete product
- [x] F: create product fails when catalog is missing
- [x] F: update availability fails for soft-deleted product
- [x] G: search product by name
- [x] G: list products by restaurant (pending implementation)
- [x] H: create order computes total from products
- [x] H: create order fails when product is missing
- [x] I: process payment links payment and moves order to PAGO
- [x] I: process payment fails when order is unknown
- [x] J: order transitions PAGO -> A_PREPARAR -> PRONTO_PARA_ENTREGA
- [x] J: start preparation fails when order is not paid
- [x] K: assign available driver to ready order
- [x] K: assign driver fails when no available driver exists
- [x] L: cancel order succeeds when order is CRIADO
- [x] L: cancel order fails when order is A_PREPARAR

## Test Checklist By Class

### Services

#### 1) PedidoService
- [x] criarPedido com DTO nulo deve falhar (`IllegalArgumentException`)
- [x] criarPedido com lista de produtos vazia deve falhar
- [x] criarPedido com cliente inexistente deve falhar
- [x] criarPedido com restaurante inexistente deve falhar
- [x] criarPedido com produto inexistente deve falhar
- [x] criarPedido com sucesso deve calcular `valorTotal` corretamente
- [x] criarPedido com sucesso deve criar pedido no estado `CRIADO`
- [x] cancelarPedido com `pedidoId` nulo deve falhar
- [x] cancelarPedido com pedido inexistente deve falhar
- [x] cancelarPedido com estado `CRIADO` deve passar
- [x] cancelarPedido com estado `PAGO` deve passar
- [x] cancelarPedido com estado `A_PREPARAR` deve falhar
- [x] cancelarPedido com estado `PRONTO_PARA_ENTREGA` deve falhar
- [x] listarPedidos com `clienteId` e `estado` deve usar filtro combinado
- [x] listarPedidos só com `clienteId` deve filtrar por cliente
- [x] listarPedidos só com `estado` deve filtrar por estado
- [x] listarPedidos sem filtros deve devolver todos

#### 2) PagamentoService
- [x] processarPagamento com `pedidoId` nulo deve falhar
- [x] processarPagamento com pedido inexistente deve falhar
- [x] processarPagamento com factory a devolver `null` deve falhar
- [x] processarPagamento com sucesso deve guardar pagamento
- [x] processarPagamento com sucesso deve associar pagamento ao pedido
- [x] processarPagamento com sucesso deve mudar estado para `PAGO`
- [x] processarPagamento não deve permitir pagar pedido já pago (via regra de domínio)

#### 3) EntregaService
- [x] atribuirEntregador com DTO nulo deve falhar
- [x] atribuirEntregador com `pedidoId` nulo deve falhar
- [x] atribuirEntregador com pedido inexistente deve falhar
- [x] atribuirEntregador com pedido fora de `PRONTO_PARA_ENTREGA` deve falhar
- [x] atribuirEntregador com entrega já existente para o pedido deve falhar
- [x] atribuirEntregador sem entregadores disponíveis deve falhar
- [x] atribuirEntregador com sucesso deve marcar entregador indisponível
- [x] atribuirEntregador com sucesso deve mudar pedido para `ATRIBUIDO_ENTREGADOR`
- [x] iniciarPreparacao com pedido inexistente deve falhar
- [x] iniciarPreparacao com estado inválido deve falhar
- [x] iniciarPreparacao com estado `PAGO` deve passar e gravar `A_PREPARAR`
- [x] marcarPedidoComoPronto com estado inválido deve falhar
- [x] marcarPedidoComoPronto com estado `A_PREPARAR` deve passar
- [x] registarRecolha sem entrega deve falhar
- [x] registarRecolha com pedido fora de `ATRIBUIDO_ENTREGADOR` deve falhar
- [x] registarRecolha com sucesso deve definir `horaRecolha`
- [x] registarEntregaFinal sem entrega deve falhar
- [x] registarEntregaFinal sem entregador válido deve falhar
- [x] registarEntregaFinal com sucesso deve definir `horaEntrega`
- [x] registarEntregaFinal com sucesso deve mudar pedido para `ENTREGUE`
- [x] registarEntregaFinal com sucesso deve libertar entregador (`available=true`)

### Controllers

#### 1) PedidoController
- [x] `POST /pedidos` com payload válido deve retornar 200 e mensagem de sucesso
- [x] `POST /pedidos` com payload inválido deve retornar erro HTTP apropriado
- [x] `PATCH /pedidos/{id}/cancelar` com sucesso deve retornar 200
- [x] `PATCH /pedidos/{id}/cancelar` com erro de negócio deve retornar 4xx/5xx conforme handler
- [x] `GET /pedidos` sem filtros deve retornar lista
- [x] `GET /pedidos` com `clienteId` deve filtrar
- [x] `GET /pedidos` com `estado` deve filtrar
- [x] `GET /pedidos` com ambos filtros deve filtrar corretamente

#### 2) PagamentoController
- [x] `POST /pagamentos` com payload válido deve retornar 200 e mensagem de sucesso
- [x] `POST /pagamentos` com `pedidoId` ausente/inválido deve retornar erro
- [x] `POST /pagamentos` com tipo de pagamento inválido deve retornar erro
- [x] `POST /pagamentos` deve delegar para `PagamentoService`

#### 3) EntregaController
- [x] `POST /entregas/atribuir` com payload válido deve retornar resumo da entrega
- [x] `POST /entregas/atribuir` com payload inválido deve retornar erro
- [x] `PATCH /entregas/pedido/{id}/iniciar-preparacao` com sucesso deve retornar 200
- [x] `PATCH /entregas/pedido/{id}/marcar-pronto` com sucesso deve retornar 200
- [x] `PATCH /entregas/pedido/{id}/recolha` com sucesso deve retornar resumo com `horaRecolha`
- [x] `PATCH /entregas/pedido/{id}/entrega-final` com sucesso deve retornar resumo com `horaEntrega`
- [x] endpoints de entrega devem propagar erros de negócio corretamente

### Domain (entities + enum)

#### 1) EstadoPedido
- [x] deve conter todos os valores esperados (`CRIADO`, `PAGO`, `A_PREPARAR`, `PRONTO_PARA_ENTREGA`, `CANCELADO`, `ATRIBUIDO_ENTREGADOR`, `ENTREGUE`)

#### 2) Pagamento (abstrata)
- [x] subclasses devem implementar `validarDetalhes`
- [x] subclasses devem implementar `obterInstrucoesPagamento`
- [x] getters/setters devem preservar dados corretamente

#### 3) PagamentoMBWay
- [x] `validarDetalhes` com número válido (9 dígitos iniciados por 9) deve passar
- [x] `validarDetalhes` com número inválido deve falhar
- [x] `obterInstrucoesPagamento` deve incluir valor e número

#### 4) PagamentoDinheiro
- [x] `validarDetalhes` com valor recebido suficiente deve calcular troco
- [x] `validarDetalhes` com valor insuficiente deve falhar
- [x] `obterInstrucoesPagamento` deve incluir troco esperado

#### 5) PagamentoMultibanco
- [x] `validarDetalhes` com entidade/referência válidas deve passar
- [x] `validarDetalhes` com entidade/referência inválidas deve falhar
- [x] `obterInstrucoesPagamento` deve conter entidade e referência

#### 6) PagamentoCartao
- [x] `validarDetalhes` com número/cartão/data/cvv válidos deve passar
- [x] `validarDetalhes` com dados inválidos deve falhar
- [x] `obterInstrucoesPagamento` deve retornar instruções consistentes

#### 7) Pedido
- [x] `confirmarPagamento` de `CRIADO` para `PAGO` deve passar
- [x] `confirmarPagamento` fora de `CRIADO` deve falhar
- [x] `iniciarPreparacao` de `PAGO` para `A_PREPARAR` deve passar
- [x] `iniciarPreparacao` fora de `PAGO` deve falhar
- [x] `marcarComoPronto` de `A_PREPARAR` para `PRONTO_PARA_ENTREGA` deve passar
- [x] `atribuirEntregador` de `PRONTO_PARA_ENTREGA` para `ATRIBUIDO_ENTREGADOR` deve passar
- [x] `marcarComoEntregue` de `ATRIBUIDO_ENTREGADOR` para `ENTREGUE` deve passar
- [x] `cancelar` em estados permitidos deve mudar para `CANCELADO`
- [x] `cancelar` em estados bloqueados deve falhar

#### 8) Entrega
- [x] `registarRecolha` primeira vez deve definir `horaRecolha`
- [x] `registarRecolha` segunda vez deve falhar
- [x] `registarConclusao` sem recolha deve falhar
- [x] `registarConclusao` após recolha deve definir `horaEntrega`
- [x] `registarConclusao` repetida deve falhar
- [x] `calcularTempoDeViagem` deve devolver duração correta

### Factory

#### 1) PagamentoFactory
- [x] criar `MBWAY` com detalhes válidos deve retornar `PagamentoMBWay`
- [x] criar `DINHEIRO` com detalhes válidos deve retornar `PagamentoDinheiro`
- [x] criar `MULTIBANCO` com detalhes válidos deve retornar `PagamentoMultibanco`
- [x] criar `CARTAO` com detalhes válidos deve retornar `PagamentoCartao`
- [x] tipo de pagamento inválido deve lançar exceção
- [x] detalhes obrigatórios ausentes por tipo devem lançar exceção

### Repositories

#### 1) IPedidoRepository (`@DataJpaTest`)
- [x] `findByclienteId` deve devolver pedidos do cliente
- [x] `findByRestauranteId` deve devolver pedidos do restaurante
- [x] `findByEstado` deve filtrar por estado
- [x] `findByclienteIdAndEstado` deve aplicar filtro combinado
- [x] `existsByIdAndEstado` deve retornar verdadeiro/falso corretamente

#### 2) IPagamentoRepository (`@DataJpaTest`)
- [x] guardar e obter pagamento por id
- [x] persistência por subtipo (`MBWAY`, `DINHEIRO`, `MULTIBANCO`, `CARTAO`)
- [x] verificar discriminação correta dos tipos no retrieval

#### 3) IEntregaRepository (`@DataJpaTest`)
- [x] `findByEntregadorId` deve devolver entregas do entregador
- [x] `findByPedidoId` deve devolver entrega associada ao pedido
- [x] `findByHoraEntregaIsNull` deve devolver entregas ativas
- [x] `existsByEntregadorIdAndHoraEntregaIsNull` deve refletir estado ativo do entregador

## Test Checklist By Class (Restaurante e Produto)

### Services

#### 1) RestauranteService
- [x] `criarRestaurante` com DTO nulo deve falhar (`IllegalArgumentException`)
- [x] `criarRestaurante` com `adminEmail` nulo/vazio deve falhar
- [x] `criarRestaurante` com admin inexistente deve falhar
- [x] `criarRestaurante` com `nome` em branco deve falhar
- [x] `criarRestaurante` com `nif` não numérico ou com tamanho diferente de 9 deve falhar
- [x] `criarRestaurante` com `nif` já existente deve falhar
- [x] `criarRestaurante` com morada nula deve falhar
- [x] `criarRestaurante` com `numeroPorta <= 0` deve falhar
- [x] `criarRestaurante` com sucesso deve criar restaurante em estado `FECHADO`
- [x] `criarRestaurante` com sucesso deve associar `CatalogoProdutos` e `CatalogoEntregas`
- [x] `atualizarRestaurante` com DTO nulo deve falhar
- [x] `atualizarRestaurante` com restaurante inexistente deve falhar
- [x] `atualizarRestaurante` com `nif` de outro restaurante deve falhar
- [x] `atualizarRestaurante` com sucesso deve persistir `nome`, `nif` e `morada`
- [x] `atualizarEstado` com DTO nulo ou `estado` nulo deve falhar
- [x] `atualizarEstado` com restaurante inexistente deve falhar
- [x] `atualizarEstado` para o mesmo estado atual deve falhar (`IllegalStateException`)
- [x] `atualizarEstado` para `ABERTO` deve chamar regra de domínio e persistir
- [x] `atualizarEstado` para `FECHADO` deve chamar regra de domínio e persistir
- [x] `atualizarEstado` para `RESERVADO` deve chamar regra de domínio e persistir
- [x] `procurarRestaurantePorNome` com nome nulo/vazio deve devolver todos
- [x] `procurarRestaurantePorNome` com nome válido deve usar pesquisa case-insensitive
- [x] `listarPorEstado` com estado nulo deve devolver todos
- [x] `listarPorEstado` com estado válido deve filtrar corretamente

#### 2) ProdutoService
- [x] `criarProduto` com DTO nulo deve falhar (`IllegalArgumentException`)
- [x] `criarProduto` com catálogo inexistente deve falhar
- [x] `criarProduto` com sucesso deve criar produto com `disponivel=true` e `apagado=false`
- [x] `atualizarProduto` com DTO nulo deve falhar
- [x] `atualizarProduto` com produto inexistente deve falhar
- [x] `atualizarProduto` em produto apagado deve falhar (`IllegalStateException`)
- [x] `atualizarProduto` com sucesso deve persistir `nome`, `preco` e `descricao`
- [x] `atualizarDisponibilidade` com DTO nulo ou `disponivel` nulo deve falhar
- [x] `atualizarDisponibilidade` com produto inexistente deve falhar
- [x] `atualizarDisponibilidade` em produto apagado deve falhar
- [x] `atualizarDisponibilidade` para `false` deve indisponibilizar produto ativo
- [x] `atualizarDisponibilidade` para `true` deve disponibilizar produto previamente indisponível
- [x] `obterProdutoPorId` com id inexistente deve falhar
- [x] `listarProdutosAtivos` deve excluir produtos apagados (soft delete)
- [x] `listarProdutosDisponiveis` deve devolver apenas `disponivel=true`
- [x] `procurarProdutoPorNome` deve usar pesquisa case-insensitive
- [x] `apagarProduto` com id inexistente deve falhar
- [x] `apagarProduto` com sucesso deve marcar `apagado=true` e `disponivel=false`
- [x] regra P1: produto indisponível/apagado não deve poder entrar em pedido (validado no fluxo H)

### Controllers

#### 1) RestauranteController
- [x] `POST /api/restaurantes` com payload válido deve retornar `201` com dados do restaurante
- [x] `POST /api/restaurantes` sem `adminEmail` deve retornar `400`
- [x] `POST /api/restaurantes` com NIF inválido/duplicado deve retornar `400`
- [x] `PUT /api/restaurantes/{id}` com sucesso deve retornar `200`
- [x] `PUT /api/restaurantes/{id}` com id inexistente deve retornar `404`
- [x] `PATCH /api/restaurantes/{id}/estado` com sucesso deve retornar `200`
- [x] `PATCH /api/restaurantes/{id}/estado` com estado repetido deve retornar `409`
- [x] `PATCH /api/restaurantes/{id}/abrir` deve delegar para atualização de estado `ABERTO`
- [x] `PATCH /api/restaurantes/{id}/fechar` deve delegar para atualização de estado `FECHADO`
- [x] `GET /api/restaurantes/{id}` com id válido deve retornar `200`
- [x] `GET /api/restaurantes/{id}` com id inválido deve retornar `404`
- [x] `GET /api/restaurantes?nome=...` deve pesquisar por nome
- [x] `GET /api/restaurantes?estado=ABERTO` deve filtrar por estado
- [x] `GET /api/restaurantes` sem filtros deve devolver lista completa

#### 2) ProdutoController
- [x] `POST /api/catalogos/{catalogoId}/produtos` com payload válido deve retornar `201`
- [x] `POST /api/catalogos/{catalogoId}/produtos` com catálogo inexistente deve retornar `400`
- [x] `PUT /api/produtos/{id}` com sucesso deve retornar `200`
- [x] `PUT /api/produtos/{id}` com produto inexistente deve retornar `404`
- [x] `PUT /api/produtos/{id}` com produto apagado deve retornar `409`
- [x] `PATCH /api/produtos/{id}/disponibilidade` com sucesso deve retornar `200`
- [x] `PATCH /api/produtos/{id}/disponibilidade` com `disponivel` ausente deve retornar `400`
- [x] `PATCH /api/produtos/{id}/disponibilidade` em produto apagado deve retornar `409`
- [x] `GET /api/produtos/{id}` com id válido deve retornar `200`
- [x] `GET /api/produtos/{id}` com id inexistente deve retornar `404`
- [x] `GET /api/produtos?nome=...` deve pesquisar por nome
- [x] `GET /api/produtos?ativos=true` deve devolver apenas não apagados
- [x] `GET /api/produtos?disponiveis=true` deve devolver apenas disponíveis
- [x] `DELETE /api/produtos/{id}` com sucesso deve retornar `204`
- [x] `DELETE /api/produtos/{id}` com id inexistente deve retornar `404`

### Domain (entities + enum)

#### 1) EstadoRestaurante
- [x] deve conter os valores esperados (`ABERTO`, `FECHADO`, `RESERVADO`)

#### 2) Restaurante
- [x] novo `Restaurante` deve iniciar em `FECHADO`
- [x] `abrir` a partir de `FECHADO` deve passar
- [x] `abrir` quando já `ABERTO` deve falhar
- [x] `fechar` a partir de `ABERTO` deve passar
- [x] `fechar` quando já `FECHADO` deve falhar
- [x] `reservar` a partir de `ABERTO`/`FECHADO` deve passar
- [x] `reservar` quando já `RESERVADO` deve falhar
- [x] `validarDisponibilidadeParaPedido` com estado diferente de `ABERTO` deve falhar
- [x] `validarDisponibilidadeParaPedido` com estado `ABERTO` deve passar

#### 3) Produto
- [x] novo `Produto` deve iniciar com `disponivel=true` e `apagado=false`
- [x] `indisponibilizar` quando produto disponível deve passar
- [x] `indisponibilizar` quando já indisponível deve falhar
- [x] `disponibilizar` quando produto indisponível e ativo deve passar
- [x] `disponibilizar` quando já disponível deve falhar
- [x] `apagar` deve marcar `apagado=true` e `disponivel=false`
- [x] `apagar` repetido deve falhar
- [x] `ativar` em produto apagado deve reativar (`apagado=false`)
- [x] `ativar` em produto já ativo deve falhar
- [x] `validarDisponibilidadeParaPedido` em produto apagado deve falhar
- [x] `validarDisponibilidadeParaPedido` em produto indisponível deve falhar
- [x] `validarDisponibilidadeParaPedido` em produto disponível e ativo deve passar

### Factory

#### 1) Restaurante/Produto Factory
- [x] confirmar que não existe factory dedicada para `Restaurante`/`Produto` no desenho atual
- [x] se for introduzida factory no futuro, validar criação com campos obrigatórios e erros de domínio

### Repositories

#### 1) IRestauranteRepository (`@DataJpaTest`)
- [x] `findByNomeContainingIgnoreCase` deve pesquisar por nome sem case-sensitive
- [x] `findByNif` deve encontrar restaurante por NIF único
- [x] `findByNif` com NIF inexistente deve devolver vazio
- [x] `findByEstado` deve devolver apenas restaurantes no estado pedido

#### 2) IProdutoRepository (`@DataJpaTest`)
- [x] `findByNomeContainingIgnoreCase` deve pesquisar por nome sem case-sensitive
- [x] `findByDisponivelTrue` deve devolver apenas produtos disponíveis
- [x] `findByApagadoFalse` deve excluir produtos soft-deleted

#### 3) IRestauranteAdminRepository (`@DataJpaTest`)
- [x] `findByEmail` deve devolver admin existente
- [x] `findByEmail` com email inexistente deve devolver `null`
