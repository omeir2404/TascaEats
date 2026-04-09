package pt.ul.fc.css.tascaeats.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoDinheiro;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.produto.CatalogoProdutos;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador;
import pt.ul.fc.css.tascaeats.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;
import pt.ul.fc.css.tascaeats.dto.pagamentoDTO.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarDisponibilidadeProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.CriarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarEstadoRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.CriarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorLoginDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorRegistoDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorUpdateDTO;
import pt.ul.fc.css.tascaeats.repository.entregaRepository.IEntregaRepository;
import pt.ul.fc.css.tascaeats.repository.pagamentoRepository.IPagamentoRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.ICatalogoProdutosRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IRestauranteAdminRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;
import pt.ul.fc.css.tascaeats.service.entregaService.EntregaService;
import pt.ul.fc.css.tascaeats.service.pagamentoServices.PagamentoFactory;
import pt.ul.fc.css.tascaeats.service.pagamentoServices.PagamentoService;
import pt.ul.fc.css.tascaeats.service.pedidoService.PedidoService;
import pt.ul.fc.css.tascaeats.service.produtoService.ProdutoService;
import pt.ul.fc.css.tascaeats.service.restauranteServices.RestauranteService;
import pt.ul.fc.css.tascaeats.service.utilizadorServices.AutorService;
import pt.ul.fc.css.tascaeats.service.utilizadorServices.UtilizadorService;

@ExtendWith(MockitoExtension.class)
class UseCasesAToLServiceTest {

    @Mock
    private IUtilizadorRepository utilizadorRepository;
    @Mock
    private AutorService autorService;
    @InjectMocks
    private UtilizadorService utilizadorService;

    @Mock
    private IRestauranteRepository restauranteRepository;
    @Mock
    private IRestauranteAdminRepository restauranteAdminRepository;
    @InjectMocks
    private RestauranteService restauranteService;

    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private ICatalogoProdutosRepository catalogoProdutosRepository;
    @InjectMocks
    private ProdutoService produtoService;

    @Mock
    private IPedidoRepository pedidoRepository;
    @InjectMocks
    private PedidoService pedidoService;

    @Mock
    private IPagamentoRepository pagamentoRepository;
    @Mock
    private PagamentoFactory pagamentoFactory;
    @InjectMocks
    private PagamentoService pagamentoService;

    @Mock
    private IEntregaRepository entregaRepository;
    @InjectMocks
    private EntregaService entregaService;

    // A) Login com autenticacao mock
    @Test
    @DisplayName("A: login succeeds for valid user even with arbitrary password")
    void loginShouldAcceptAnyPasswordWhenUserExists() {
        when(autorService.login("valid@tasca.pt")).thenReturn(cliente(1L, "valid@tasca.pt"));

        boolean result = utilizadorService.login(new UtilizadorLoginDTO("valid@tasca.pt", "anything"));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("A: login fails when user does not exist")
    void loginShouldFailWhenUserDoesNotExist() {
        when(autorService.login("ghost@tasca.pt")).thenReturn(null);

        boolean result = utilizadorService.login(new UtilizadorLoginDTO("ghost@tasca.pt", "any"));

        assertThat(result).isFalse();
    }

    // B) Registo de utilizadores
    @Test
    @DisplayName("B: registration creates client/admin/driver with valid inputs")
    void registrationShouldCreateAllSupportedUserTypes() {
        when(utilizadorRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(utilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador client = utilizadorService.createUtilizador(registo("Ana", "ana@tasca.pt", "pw", "client", moradaDto("Rua A", 10, "1000-100", "Lisboa")));
        Utilizador admin = utilizadorService.createUtilizador(registo("Boss", "boss@tasca.pt", "pw", "admin", null));
        Utilizador driver = utilizadorService.createUtilizador(registo("Dan", "dan@tasca.pt", "pw", "driver", null));

        assertThat(client).isInstanceOf(Cliente.class);
        assertThat(admin).isInstanceOf(RestauranteAdmin.class);
        assertThat(driver).isInstanceOf(Entregador.class);
    }

    @Test
    @DisplayName("B: registration fails when client has no address")
    void registrationShouldFailWhenClientAddressMissing() {
        assertThatThrownBy(() -> utilizadorService.createUtilizador(registo("Ana", "ana@tasca.pt", "pw", "client", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Morada is required for client Utilizadors");
    }

    // C) Verificar, remover e atualizar utilizadores
    @Test
    @DisplayName("C: update and delete user should work for existing user")
    void updateAndDeleteUserShouldWork() {
        Cliente existing = cliente(10L, "old@tasca.pt");
        when(utilizadorRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(utilizadorRepository.findByEmail("new@tasca.pt")).thenReturn(Optional.empty());
        when(utilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilizadorRepository.existsById(10L)).thenReturn(true);

        Utilizador updated = utilizadorService.updateUtilizador(10L, update("Updated", "new@tasca.pt", "newpw"));
        boolean deleted = utilizadorService.deleteUtilizador(10L);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getEmail()).isEqualTo("new@tasca.pt");
        assertThat(deleted).isTrue();
    }

    @Test
    @DisplayName("C: update fails when email already belongs to another user")
    void updateShouldFailOnEmailConflict() {
        Cliente existing = cliente(10L, "old@tasca.pt");
        Cliente other = cliente(20L, "taken@tasca.pt");
        when(utilizadorRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(utilizadorRepository.findByEmail("taken@tasca.pt")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> utilizadorService.updateUtilizador(10L, update("Ana", "taken@tasca.pt", "pw")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");
    }

    // D) Buscar restaurantes (nome/cidade)
    @Test
    @DisplayName("D: search restaurants by name returns matching entries")
    void searchRestaurantByNameShouldReturnMatches() {
        Restaurante r1 = restaurante("Tasca Central", "123456789", "Lisboa", EstadoRestaurante.ABERTO);
        Restaurante r2 = restaurante("Sushi Spot", "987654321", "Porto", EstadoRestaurante.FECHADO);
        when(restauranteRepository.findByNomeContainingIgnoreCase("tasca")).thenReturn(List.of(r1));

        List<Restaurante> result = restauranteService.procurarRestaurantePorNome("tasca");

        assertThat(result).containsExactly(r1);
        assertThat(result).doesNotContain(r2);
    }

    //D) Buscar restaurantes de determinada cidade
    @Test
    @DisplayName("D: search restaurants by city should return restaurants in that city")
    void searchRestaurantByCityShouldReturnMatches() {
        Restaurante r1 = restaurante("Tasca Lisboa", "111111111", "Lisboa", EstadoRestaurante.ABERTO);
        Restaurante r2 = restaurante("Porto Grill", "222222222", "Porto", EstadoRestaurante.FECHADO);
        when(restauranteRepository.findByMoradaLocalidadeIgnoreCase("lisboa")).thenReturn(List.of(r1));

        List<Restaurante> result = restauranteService.procurarRestaurantePorCidade("lisboa");

        assertThat(result).containsExactly(r1);
        assertThat(result).doesNotContain(r2);
    }

    // E) Criacao de restaurantes e gestao de estado
    @Test
    @DisplayName("E: create restaurant and update state to open")
    void createRestaurantAndOpenShouldWork() {
        RestauranteAdmin admin = new RestauranteAdmin("Admin", "admin@tasca.pt", "pw");
        when(restauranteAdminRepository.findByEmail("admin@tasca.pt")).thenReturn(admin);
        when(restauranteRepository.findByNif("111222333")).thenReturn(Optional.empty());
        when(restauranteRepository.save(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Restaurante created = restauranteService.criarRestaurante(criarRestauranteDto("Nova Tasca", "111222333", "Lisboa"), "admin@tasca.pt");

        AtualizarEstadoRestauranteDTO estadoDto = new AtualizarEstadoRestauranteDTO();
        estadoDto.setEstado(EstadoRestaurante.ABERTO);
        when(restauranteRepository.findById(anyLong())).thenReturn(Optional.of(created));

        Restaurante opened = restauranteService.atualizarEstado(1L, estadoDto);

        assertThat(created.getNome()).isEqualTo("Nova Tasca");
        assertThat(opened.getEstado()).isEqualTo(EstadoRestaurante.ABERTO);
    }

    @Test
    @DisplayName("E: state update fails when state is unchanged")
    void updateRestaurantStateShouldFailWhenSameState() {
        Restaurante existing = restaurante("Already Open", "444555666", "Lisboa", EstadoRestaurante.ABERTO);
        AtualizarEstadoRestauranteDTO estadoDto = new AtualizarEstadoRestauranteDTO();
        estadoDto.setEstado(EstadoRestaurante.ABERTO);
        when(restauranteRepository.findById(7L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> restauranteService.atualizarEstado(7L, estadoDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está nesse estado");
    }

    // F) Adicionar, remover e atualizar produtos
    @Test
    @DisplayName("F: create update and soft-delete product should work")
    void createUpdateDeleteProductShouldWork() {
        CatalogoProdutos catalogo = new CatalogoProdutos();
        when(catalogoProdutosRepository.findById(1L)).thenReturn(Optional.of(catalogo));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Produto created = produtoService.criarProduto(1L, criarProdutoDto("Bitoque", 10.0, "Com ovo"));

        AtualizarProdutoDTO updateDto = new AtualizarProdutoDTO();
        updateDto.setNome("Bitoque Especial");
        updateDto.setPreco(12.5);
        updateDto.setDescricao("Com ovo e batata");

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(created));
        Produto updated = produtoService.atualizarProduto(1L, updateDto);
        produtoService.apagarProduto(1L);

        assertThat(updated.getNome()).isEqualTo("Bitoque Especial");
        assertThat(updated.isApagado()).isTrue();
    }

    @Test
    @DisplayName("F: create product fails when catalog does not exist")
    void createProductShouldFailWhenCatalogMissing() {
        when(catalogoProdutosRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.criarProduto(999L, criarProdutoDto("Pizza", 9.0, "Margherita")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("catálogo não foi encontrado");
    }

    @Test
    @DisplayName("F: update availability fails when product is soft-deleted")
    void updateAvailabilityShouldFailWhenProductDeleted() {
        Produto deleted = new Produto("Soup", 3.0, "Hot", new CatalogoProdutos());
        deleted.apagar();
        AtualizarDisponibilidadeProdutoDTO disponibilidadeDto = new AtualizarDisponibilidadeProdutoDTO();
        disponibilidadeDto.setDisponivel(true);
        when(produtoRepository.findById(8L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> produtoService.atualizarDisponibilidade(8L, disponibilidadeDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("produto apagado");
    }

    // G) Buscar produtos de um restaurante
    @Test
    @DisplayName("G: search product by name returns matching products")
    void searchProductByNameShouldReturnMatches() {
        Produto bacalhau = new Produto("Bacalhau", 14.0, "Forno", new CatalogoProdutos());
        when(produtoRepository.findByNomeContainingIgnoreCase("bac")).thenReturn(List.of(bacalhau));

        List<Produto> found = produtoService.procurarProdutoPorNome("bac");

        assertThat(found).containsExactly(bacalhau);
    }

    @Test
    @DisplayName("G: list products for a specific restaurant should return only that restaurant menu")
    void listProductsByRestaurantShouldReturnOnlyRestaurantMenu() {
        Produto p1 = new Produto("Massa", 5.5, "Ótima", new CatalogoProdutos());
        Produto p2 = new Produto("Hamburguer", 7.0, "Boa", new CatalogoProdutos());
        when(produtoRepository.findByCatalogoProdutosRestauranteId(1L)).thenReturn(List.of(p1));

        List<Produto> result = produtoService.listarProdutosPorRestaurante(1L);

        assertThat(result).containsExactly(p1);
        assertThat(result).doesNotContain(p2);

    }

    // H) Criacao de pedido
    @Test
    @DisplayName("H: create order computes total from product prices")
    void createOrderShouldComputeTotalCorrectly() {
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(cliente(1L, "c@tasca.pt")));
        when(restauranteRepository.findById(2L)).thenReturn(Optional.of(restaurante("R", "123123123", "Lisboa", EstadoRestaurante.ABERTO)));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(new Produto("Prato A", 5.5, "A", new CatalogoProdutos())));
        when(produtoRepository.findById(11L)).thenReturn(Optional.of(new Produto("Prato B", 7.0, "B", new CatalogoProdutos())));

        CriarPedidoDTO dto = new CriarPedidoDTO(1L, 2L, List.of(10L, 11L));
        pedidoService.criarPedido(dto);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        assertThat(captor.getValue().getValorTotal()).isEqualTo(12.5);
        assertThat(captor.getValue().getEstado()).isEqualTo(EstadoPedido.CRIADO);
    }

    @Test
    @DisplayName("H: create order fails when one product is missing")
    void createOrderShouldFailWhenProductMissing() {
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(cliente(1L, "c@tasca.pt")));
        when(restauranteRepository.findById(2L)).thenReturn(Optional.of(restaurante("R", "123123123", "Lisboa", EstadoRestaurante.ABERTO)));
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criarPedido(new CriarPedidoDTO(1L, 2L, List.of(99L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    // I) Registo e processamento de pagamento
    @Test
    @DisplayName("I: process payment links payment and moves order to PAGO")
    void processPaymentShouldMoveOrderToPaid() {
        Pedido pedido = new Pedido(1L, 1L, 2L);
        pedido.setValorTotal(20.0);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pagamentoFactory.create("DINHEIRO", 20.0, Map.of("valorRecebido", 20.0)))
                .thenReturn(new PagamentoDinheiro(null, 20.0, LocalDate.now(), "R1", 20.0));

        pagamentoService.processarPagamento(new ProcessarPagamentoDTO(1L, "DINHEIRO", Map.of("valorRecebido", 20.0)));

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PAGO);
        assertThat(pedido.getPagamento()).isNotNull();
        verify(pagamentoRepository).save(any());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("I: process payment fails when order id is unknown")
    void processPaymentShouldFailWhenOrderMissing() {
        when(pedidoRepository.findById(500L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagamentoService.processarPagamento(new ProcessarPagamentoDTO(500L, "MBWAY", Map.of("numeroTelemovel", "912345678"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

    // J) Atualizacao do estado do pedido
    @Test
    @DisplayName("J: order status should move from PAGO to A_PREPARAR to PRONTO_PARA_ENTREGA")
    void orderStatusTransitionsShouldWorkInSequence() {
        Pedido pedido = new Pedido(70L, 1L, 2L);
        pedido.setEstado(EstadoPedido.PAGO);
        when(pedidoRepository.findById(70L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido emPreparacao = entregaService.iniciarPreparacao(70L);
        assertThat(emPreparacao.getEstado()).isEqualTo(EstadoPedido.A_PREPARAR);

        Pedido pronto = entregaService.marcarPedidoComoPronto(70L);
        assertThat(pronto.getEstado()).isEqualTo(EstadoPedido.PRONTO_PARA_ENTREGA);

    }

    @Test
    @DisplayName("J: iniciar preparacao fails if order is not paid")
    void startPreparationShouldFailWhenOrderNotPaid() {
        Pedido pedido = new Pedido(71L, 1L, 2L);
        pedido.setEstado(EstadoPedido.CRIADO);
        when(pedidoRepository.findById(71L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> entregaService.iniciarPreparacao(71L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("precisa de ser pago");
    }

    // K) Atribuicao de entregador a pedido
    @Test
    @DisplayName("K: assign available driver to ready order")
    void assignDriverShouldWorkWhenOrderReadyAndDriverFree() {
        Pedido pedido = new Pedido(80L, 1L, 2L);
        pedido.setEstado(EstadoPedido.PRONTO_PARA_ENTREGA);
        Entregador driver = new Entregador("Driver", "driver@tasca.pt", "pw");
        driver.setId(900L);
        driver.setAvailable(true);

        when(pedidoRepository.findById(80L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.findByPedidoId(80L)).thenReturn(Optional.empty());
        when(utilizadorRepository.findAll()).thenReturn(List.of(driver));
        when(entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(900L)).thenReturn(false);
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Entrega entrega = entregaService.atribuirEntregador(new AtribuirEntregadorDTO(80L));

        assertThat(entrega.getEntregadorId()).isEqualTo(900L);
        assertThat(driver.isAvailable()).isFalse();
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ATRIBUIDO_ENTREGADOR);
    }

    @Test
    @DisplayName("K: assign driver fails when no available drivers exist")
    void assignDriverShouldFailWhenNoDriverAvailable() {
        Pedido pedido = new Pedido(81L, 1L, 2L);
        pedido.setEstado(EstadoPedido.PRONTO_PARA_ENTREGA);

        Entregador busy = new Entregador("Busy", "busy@tasca.pt", "pw");
        busy.setId(901L);
        busy.setAvailable(false);

        when(pedidoRepository.findById(81L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.findByPedidoId(81L)).thenReturn(Optional.empty());
        when(utilizadorRepository.findAll()).thenReturn(List.of(busy));

        assertThatThrownBy(() -> entregaService.atribuirEntregador(new AtribuirEntregadorDTO(81L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não há entregadores disponíveis");
    }

    // L) Cancelamento de pedido
    @Test
    @DisplayName("L: cancel order succeeds when order is CRIADO")
    void cancelOrderShouldSucceedWhenCreated() {
        Pedido pedido = new Pedido(90L, 1L, 2L);
        pedido.setEstado(EstadoPedido.CRIADO);
        when(pedidoRepository.findById(90L)).thenReturn(Optional.of(pedido));

        pedidoService.cancelarPedido(90L);

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("L: cancel order fails when order is already in preparation")
    void cancelOrderShouldFailWhenPreparing() {
        Pedido pedido = new Pedido(91L, 1L, 2L);
        pedido.setEstado(EstadoPedido.A_PREPARAR);
        when(pedidoRepository.findById(91L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelarPedido(91L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não pode ser cancelado");
    }

    private UtilizadorRegistoDTO registo(String nome, String email, String password, String type, MoradaDTO moradaDTO) {
        return new UtilizadorRegistoDTO(nome, email, password, type, moradaDTO);
    }

    private UtilizadorUpdateDTO update(String nome, String email, String password) {
        return new UtilizadorUpdateDTO(nome, email, password, "client", null, false);
    }

    private MoradaDTO moradaDto(String rua, int numeroPorta, String codigoPostal, String localidade) {
        MoradaDTO dto = new MoradaDTO();
        dto.setRua(rua);
        dto.setNumeroPorta(numeroPorta);
        dto.setCodigoPostal(codigoPostal);
        dto.setLocalidade(localidade);
        return dto;
    }

    private CriarRestauranteDTO criarRestauranteDto(String nome, String nif, String localidade) {
        CriarRestauranteDTO dto = new CriarRestauranteDTO();
        dto.setNome(nome);
        dto.setNif(nif);
        dto.setMorada(moradaDto("Rua Principal", 1, "1000-001", localidade));
        return dto;
    }

    private CriarProdutoDTO criarProdutoDto(String nome, double preco, String descricao) {
        CriarProdutoDTO dto = new CriarProdutoDTO();
        dto.setNome(nome);
        dto.setPreco(preco);
        dto.setDescricao(descricao);
        return dto;
    }

    private Cliente cliente(long id, String email) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setName("Cliente");
        cliente.setEmail(email);
        cliente.setPassword("pw");
        return cliente;
    }

    private Restaurante restaurante(String nome, String nif, String cidade, EstadoRestaurante estado) {
        RestauranteAdmin admin = new RestauranteAdmin("Admin", "admin@tasca.pt", "pw");
        Restaurante restaurante = new Restaurante(
                nome,
                nif,
                new pt.ul.fc.css.tascaeats.domain.restaurante.Morada("Rua X", 1, "1000-001", cidade),
                admin,
                new CatalogoProdutos(),
                new CatalogoEntregas());

        if (estado == EstadoRestaurante.ABERTO) {
            restaurante.abrir();
        }
        if (estado == EstadoRestaurante.RESERVADO) {
            restaurante.reservar();
        }

        return restaurante;
    }
}
