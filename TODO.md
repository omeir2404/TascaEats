
## TODO create tests for:

- test create Utilizador
  - [x] valid registration
  - [x] invalid registration (email already exists)
  - [x] invalid registration (name is missing)
  - [x] invalid registration (email is missing)
  - [x] invalid registration (password is missing)
  - [x] invalid registration (type is missing)
  - [x] invalid registration (address is missing)
  - [x] all types of Utilizadors (client, restaurant owner, driver)

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
- [ ] criarPedido com DTO nulo deve falhar (`IllegalArgumentException`)
- [ ] criarPedido com lista de produtos vazia deve falhar
- [ ] criarPedido com cliente inexistente deve falhar
- [ ] criarPedido com restaurante inexistente deve falhar
- [ ] criarPedido com produto inexistente deve falhar
- [ ] criarPedido com sucesso deve calcular `valorTotal` corretamente
- [ ] criarPedido com sucesso deve criar pedido no estado `CRIADO`
- [ ] cancelarPedido com `pedidoId` nulo deve falhar
- [ ] cancelarPedido com pedido inexistente deve falhar
- [ ] cancelarPedido com estado `CRIADO` deve passar
- [ ] cancelarPedido com estado `PAGO` deve passar
- [ ] cancelarPedido com estado `A_PREPARAR` deve falhar
- [ ] cancelarPedido com estado `PRONTO_PARA_ENTREGA` deve falhar
- [ ] listarPedidos com `clienteId` e `estado` deve usar filtro combinado
- [ ] listarPedidos só com `clienteId` deve filtrar por cliente
- [ ] listarPedidos só com `estado` deve filtrar por estado
- [ ] listarPedidos sem filtros deve devolver todos

#### 2) PagamentoService
- [ ] processarPagamento com `pedidoId` nulo deve falhar
- [ ] processarPagamento com pedido inexistente deve falhar
- [ ] processarPagamento com factory a devolver `null` deve falhar
- [ ] processarPagamento com sucesso deve guardar pagamento
- [ ] processarPagamento com sucesso deve associar pagamento ao pedido
- [ ] processarPagamento com sucesso deve mudar estado para `PAGO`
- [ ] processarPagamento não deve permitir pagar pedido já pago (via regra de domínio)

#### 3) EntregaService
- [ ] atribuirEntregador com DTO nulo deve falhar
- [ ] atribuirEntregador com `pedidoId` nulo deve falhar
- [ ] atribuirEntregador com pedido inexistente deve falhar
- [ ] atribuirEntregador com pedido fora de `PRONTO_PARA_ENTREGA` deve falhar
- [ ] atribuirEntregador com entrega já existente para o pedido deve falhar
- [ ] atribuirEntregador sem entregadores disponíveis deve falhar
- [ ] atribuirEntregador com sucesso deve marcar entregador indisponível
- [ ] atribuirEntregador com sucesso deve mudar pedido para `ATRIBUIDO_ENTREGADOR`
- [ ] iniciarPreparacao com pedido inexistente deve falhar
- [ ] iniciarPreparacao com estado inválido deve falhar
- [ ] iniciarPreparacao com estado `PAGO` deve passar e gravar `A_PREPARAR`
- [ ] marcarPedidoComoPronto com estado inválido deve falhar
- [ ] marcarPedidoComoPronto com estado `A_PREPARAR` deve passar
- [ ] registarRecolha sem entrega deve falhar
- [ ] registarRecolha com pedido fora de `ATRIBUIDO_ENTREGADOR` deve falhar
- [ ] registarRecolha com sucesso deve definir `horaRecolha`
- [ ] registarEntregaFinal sem entrega deve falhar
- [ ] registarEntregaFinal sem entregador válido deve falhar
- [ ] registarEntregaFinal com sucesso deve definir `horaEntrega`
- [ ] registarEntregaFinal com sucesso deve mudar pedido para `ENTREGUE`
- [ ] registarEntregaFinal com sucesso deve libertar entregador (`available=true`)

### Controllers

#### 1) PedidoController
- [ ] `POST /pedidos` com payload válido deve retornar 200 e mensagem de sucesso
- [ ] `POST /pedidos` com payload inválido deve retornar erro HTTP apropriado
- [ ] `PATCH /pedidos/{id}/cancelar` com sucesso deve retornar 200
- [ ] `PATCH /pedidos/{id}/cancelar` com erro de negócio deve retornar 4xx/5xx conforme handler
- [ ] `GET /pedidos` sem filtros deve retornar lista
- [ ] `GET /pedidos` com `clienteId` deve filtrar
- [ ] `GET /pedidos` com `estado` deve filtrar
- [ ] `GET /pedidos` com ambos filtros deve filtrar corretamente

#### 2) PagamentoController
- [ ] `POST /pagamentos` com payload válido deve retornar 200 e mensagem de sucesso
- [ ] `POST /pagamentos` com `pedidoId` ausente/inválido deve retornar erro
- [ ] `POST /pagamentos` com tipo de pagamento inválido deve retornar erro
- [ ] `POST /pagamentos` deve delegar para `PagamentoService`

#### 3) EntregaController
- [ ] `POST /entregas/atribuir` com payload válido deve retornar resumo da entrega
- [ ] `POST /entregas/atribuir` com payload inválido deve retornar erro
- [ ] `PATCH /entregas/pedido/{id}/iniciar-preparacao` com sucesso deve retornar 200
- [ ] `PATCH /entregas/pedido/{id}/marcar-pronto` com sucesso deve retornar 200
- [ ] `PATCH /entregas/pedido/{id}/recolha` com sucesso deve retornar resumo com `horaRecolha`
- [ ] `PATCH /entregas/pedido/{id}/entrega-final` com sucesso deve retornar resumo com `horaEntrega`
- [ ] endpoints de entrega devem propagar erros de negócio corretamente

### Domain (entities + enum)

#### 1) EstadoPedido
- [ ] deve conter todos os valores esperados (`CRIADO`, `PAGO`, `A_PREPARAR`, `PRONTO_PARA_ENTREGA`, `CANCELADO`, `ATRIBUIDO_ENTREGADOR`, `ENTREGUE`)

#### 2) Pagamento (abstrata)
- [ ] subclasses devem implementar `validarDetalhes`
- [ ] subclasses devem implementar `obterInstrucoesPagamento`
- [ ] getters/setters devem preservar dados corretamente

#### 3) PagamentoMBWay
- [ ] `validarDetalhes` com número válido (9 dígitos iniciados por 9) deve passar
- [ ] `validarDetalhes` com número inválido deve falhar
- [ ] `obterInstrucoesPagamento` deve incluir valor e número

#### 4) PagamentoDinheiro
- [ ] `validarDetalhes` com valor recebido suficiente deve calcular troco
- [ ] `validarDetalhes` com valor insuficiente deve falhar
- [ ] `obterInstrucoesPagamento` deve incluir troco esperado

#### 5) PagamentoMultibanco
- [ ] `validarDetalhes` com entidade/referência válidas deve passar
- [ ] `validarDetalhes` com entidade/referência inválidas deve falhar
- [ ] `obterInstrucoesPagamento` deve conter entidade e referência

#### 6) PagamentoCartao
- [ ] `validarDetalhes` com número/cartão/data/cvv válidos deve passar
- [ ] `validarDetalhes` com dados inválidos deve falhar
- [ ] `obterInstrucoesPagamento` deve retornar instruções consistentes

#### 7) Pedido
- [ ] `confirmarPagamento` de `CRIADO` para `PAGO` deve passar
- [ ] `confirmarPagamento` fora de `CRIADO` deve falhar
- [ ] `iniciarPreparacao` de `PAGO` para `A_PREPARAR` deve passar
- [ ] `iniciarPreparacao` fora de `PAGO` deve falhar
- [ ] `marcarComoPronto` de `A_PREPARAR` para `PRONTO_PARA_ENTREGA` deve passar
- [ ] `atribuirEntregador` de `PRONTO_PARA_ENTREGA` para `ATRIBUIDO_ENTREGADOR` deve passar
- [ ] `marcarComoEntregue` de `ATRIBUIDO_ENTREGADOR` para `ENTREGUE` deve passar
- [ ] `cancelar` em estados permitidos deve mudar para `CANCELADO`
- [ ] `cancelar` em estados bloqueados deve falhar

#### 8) Entrega
- [ ] `registarRecolha` primeira vez deve definir `horaRecolha`
- [ ] `registarRecolha` segunda vez deve falhar
- [ ] `registarConclusao` sem recolha deve falhar
- [ ] `registarConclusao` após recolha deve definir `horaEntrega`
- [ ] `registarConclusao` repetida deve falhar
- [ ] `calcularTempoDeViagem` deve devolver duração correta

### Factory

#### 1) PagamentoFactory
- [ ] criar `MBWAY` com detalhes válidos deve retornar `PagamentoMBWay`
- [ ] criar `DINHEIRO` com detalhes válidos deve retornar `PagamentoDinheiro`
- [ ] criar `MULTIBANCO` com detalhes válidos deve retornar `PagamentoMultibanco`
- [ ] criar `CARTAO` com detalhes válidos deve retornar `PagamentoCartao`
- [ ] tipo de pagamento inválido deve lançar exceção
- [ ] detalhes obrigatórios ausentes por tipo devem lançar exceção

### Repositories

#### 1) IPedidoRepository (`@DataJpaTest`)
- [ ] `findByClienteeId` deve devolver pedidos do cliente
- [ ] `findByRestauranteId` deve devolver pedidos do restaurante
- [ ] `findByEstado` deve filtrar por estado
- [ ] `findByClienteeIdAndEstado` deve aplicar filtro combinado
- [ ] `existsByIdAndEstado` deve retornar verdadeiro/falso corretamente

#### 2) IPagamentoRepository (`@DataJpaTest`)
- [ ] guardar e obter pagamento por id
- [ ] persistência por subtipo (`MBWAY`, `DINHEIRO`, `MULTIBANCO`, `CARTAO`)
- [ ] verificar discriminação correta dos tipos no retrieval

#### 3) IEntregaRepository (`@DataJpaTest`)
- [ ] `findByEntregadorId` deve devolver entregas do entregador
- [ ] `findByPedidoId` deve devolver entrega associada ao pedido
- [ ] `findByHoraEntregaIsNull` deve devolver entregas ativas
- [ ] `existsByEntregadorIdAndHoraEntregaIsNull` deve refletir estado ativo do entregador

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
- [ ] `POST /api/catalogos/{catalogoId}/produtos` com payload válido deve retornar `201`
- [ ] `POST /api/catalogos/{catalogoId}/produtos` com catálogo inexistente deve retornar `400`
- [ ] `PUT /api/produtos/{id}` com sucesso deve retornar `200`
- [ ] `PUT /api/produtos/{id}` com produto inexistente deve retornar `404`
- [ ] `PUT /api/produtos/{id}` com produto apagado deve retornar `409`
- [ ] `PATCH /api/produtos/{id}/disponibilidade` com sucesso deve retornar `200`
- [ ] `PATCH /api/produtos/{id}/disponibilidade` com `disponivel` ausente deve retornar `400`
- [ ] `PATCH /api/produtos/{id}/disponibilidade` em produto apagado deve retornar `409`
- [ ] `GET /api/produtos/{id}` com id válido deve retornar `200`
- [ ] `GET /api/produtos/{id}` com id inexistente deve retornar `404`
- [ ] `GET /api/produtos?nome=...` deve pesquisar por nome
- [ ] `GET /api/produtos?ativos=true` deve devolver apenas não apagados
- [ ] `GET /api/produtos?disponiveis=true` deve devolver apenas disponíveis
- [ ] `DELETE /api/produtos/{id}` com sucesso deve retornar `204`
- [ ] `DELETE /api/produtos/{id}` com id inexistente deve retornar `404`

### Domain (entities + enum)

#### 1) EstadoRestaurante
- [ ] deve conter os valores esperados (`ABERTO`, `FECHADO`, `RESERVADO`)

#### 2) Restaurante
- [ ] novo `Restaurante` deve iniciar em `FECHADO`
- [ ] `abrir` a partir de `FECHADO` deve passar
- [ ] `abrir` quando já `ABERTO` deve falhar
- [ ] `fechar` a partir de `ABERTO` deve passar
- [ ] `fechar` quando já `FECHADO` deve falhar
- [ ] `reservar` a partir de `ABERTO`/`FECHADO` deve passar
- [ ] `reservar` quando já `RESERVADO` deve falhar
- [ ] `validarDisponibilidadeParaPedido` com estado diferente de `ABERTO` deve falhar
- [ ] `validarDisponibilidadeParaPedido` com estado `ABERTO` deve passar

#### 3) Produto
- [ ] novo `Produto` deve iniciar com `disponivel=true` e `apagado=false`
- [ ] `indisponibilizar` quando produto disponível deve passar
- [ ] `indisponibilizar` quando já indisponível deve falhar
- [ ] `disponibilizar` quando produto indisponível e ativo deve passar
- [ ] `disponibilizar` quando já disponível deve falhar
- [ ] `apagar` deve marcar `apagado=true` e `disponivel=false`
- [ ] `apagar` repetido deve falhar
- [ ] `ativar` em produto apagado deve reativar (`apagado=false`)
- [ ] `ativar` em produto já ativo deve falhar
- [ ] `validarDisponibilidadeParaPedido` em produto apagado deve falhar
- [ ] `validarDisponibilidadeParaPedido` em produto indisponível deve falhar
- [ ] `validarDisponibilidadeParaPedido` em produto disponível e ativo deve passar

#### 4) CatalogoProdutos
- [ ] construtor com `restaurante=null` deve falhar
- [ ] `setRestaurante` deve manter associação bidirecional consistente
- [ ] `addProduto` com produto nulo não deve alterar lista
- [ ] `addProduto` com produto apagado deve falhar
- [ ] `addProduto` deve associar produto ao catálogo
- [ ] `removeProduto` deve remover da lista e limpar associação
- [ ] `setProdutos` deve substituir coleção mantendo consistência das associações

### Factory

#### 1) Restaurante/Produto Factory
- [ ] confirmar que não existe factory dedicada para `Restaurante`/`Produto` no desenho atual
- [ ] se for introduzida factory no futuro, validar criação com campos obrigatórios e erros de domínio

### Repositories

#### 1) IRestauranteRepository (`@DataJpaTest`)
- [ ] `findByNomeContainingIgnoreCase` deve pesquisar por nome sem case-sensitive
- [ ] `findByNif` deve encontrar restaurante por NIF único
- [ ] `findByNif` com NIF inexistente deve devolver vazio
- [ ] `findByEstado` deve devolver apenas restaurantes no estado pedido

#### 2) IProdutoRepository (`@DataJpaTest`)
- [ ] `findByNomeContainingIgnoreCase` deve pesquisar por nome sem case-sensitive
- [ ] `findByDisponivelTrue` deve devolver apenas produtos disponíveis
- [ ] `findByApagadoFalse` deve excluir produtos soft-deleted

#### 3) ICatalogoProdutosRepository (`@DataJpaTest`)
- [ ] guardar e obter catálogo por id
- [ ] persistir catálogo com múltiplos produtos deve manter relação `OneToMany`
- [ ] remover produto do catálogo deve refletir `orphanRemoval=true`

#### 4) IRestauranteAdminRepository (`@DataJpaTest`)
- [ ] `findByEmail` deve devolver admin existente
- [ ] `findByEmail` com email inexistente deve devolver `null`
