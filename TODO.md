# native app

- [x] A. Login com autenticação (Nota: vamos fazer mock. Qualquer palavra-passe será aceite contanto que o utilizador
  seja válido)
- [x] C. Registro de Administradores e Entregadores.
- [x] D. Verificar, remover e atualizar utilizadores.
- [x] E. Buscar por restaurantes (filtro definido pelo utilizador).
- [ ] Listar restaurantes, o nif não está a ser retornado do backend.
- [ ] F. Gerir Restaurantes.
    - [ ] Criar restaurante (apenas admin) (dá erro por causa do menu ser null na base de dados)
    - [ ] Remover restaurante (apenas admin) dá erro por causa do menu ter constraint na base de dados
    - [x] Atualizar nome, NIF e morada (apenas admin)
  - [x] Gerir estado (apenas admin): `ABERTO`, `FECHADO`, `RESERVADO`  patch não está a ser reconhecido pelo java???
- [ ] G. Gerir Menus. apenas existe tab de produtos não de menus. tem de ser criado um menu para cada restaurante e
  depois os produtos são adicionados a esse menu.
    - [ ] Criar menu (apenas admin) dá erro por causa do restaurante ser null na base de dados
    - [ ] Remover menu (apenas admin) dá erro por causa dos produtos terem constraint na base de dados
    - [ ] Adicionar produto ao menu (apenas admin)
    - [ ] Remover produto do menu (apenas admin)
- [ ] H. Gerir Produtos.
  - [x] Criar produto (apenas admin) apenas pode ser criado com um restaurante associado
  - [x] Remover produto (apenas admin)
    - [x] Atualizar nome, descrição, preço e disponibilidade (apenas admin)
  - [ ] Soft delete (`apagar`) (apenas admin) não confirmado ainda, é preciso ver os orders antigos depois de apagar um
    produto
    - [x] Filtros por nome, restaurante, ativos e disponíveis
- [ ] K. Registo e processamento do pagamento associado a um pedido. provavelmente funciona mas precisa de testes. não
  tinha o web para criar pedidos a funcioanr ainda
- [ ] L. Atualização do estado do pedido. funcionava com dados mock mas precisa de testes com pedidos reais
- [ ] M. Atribuição de um entregador a um pedido funcionava com dados mock mas precisa de testes com pedidos reais
- [ ] N. Cancelamento de um pedido provavelmente funciona mas precisa de testes com pedidos reais