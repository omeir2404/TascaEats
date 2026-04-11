package pt.ul.fc.css.tascaeats.services;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorRegistoDTO;
import pt.ul.fc.css.tascaeats.repository.entregaRepository.IEntregaRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;
import pt.ul.fc.css.tascaeats.service.entregaService.EntregaService;
import pt.ul.fc.css.tascaeats.service.pedidoService.PedidoService;
import pt.ul.fc.css.tascaeats.service.produtoService.ProdutoService;
import pt.ul.fc.css.tascaeats.service.utilizadorServices.AutorService;
import pt.ul.fc.css.tascaeats.service.utilizadorServices.UtilizadorService;

@ExtendWith(MockitoExtension.class)
class PdfAlignmentExtraRulesTest {

    @Mock
    private IPedidoRepository pedidoRepository;
    @Mock
    private IUtilizadorRepository utilizadorRepository;
    @Mock
    private IRestauranteRepository restauranteRepository;
    @Mock
    private IProdutoRepository produtoRepository;
    @InjectMocks
    private PedidoService pedidoService;

    @Mock
    private AutorService autorService;
    @InjectMocks
    private UtilizadorService utilizadorService;

    @Mock
    private IEntregaRepository entregaRepository;
    @InjectMocks
    private EntregaService entregaService;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    @DisplayName("PDF rule: creating an order on a closed restaurant should be rejected")
    void shouldRejectOrderWhenRestaurantIsClosed() {
        Restaurante closedRestaurant = restaurante(); // default state is FECHADO
        Produto produto = new Produto("Sopa", 4.0, "Do dia", closedRestaurant);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(cliente(1L)));
        when(restauranteRepository.findById(2L)).thenReturn(Optional.of(closedRestaurant));

        assertThatThrownBy(() -> pedidoService.criarPedido(new CriarPedidoDTO(1L, 2L, List.of(10L))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fechado");
    }

    @Test
    @DisplayName("PDF rule: deleting product linked to old orders must remain soft-delete")
    void shouldSoftDeleteProductLinkedToOldOrders() {
        Produto produto = new Produto("Bife", 12.0, "Grelhado", restauranteAberto());
        Pedido pedidoAntigo = new Pedido(50L, 1L, 2L);
        produto.setPedidos(List.of(pedidoAntigo));

        when(produtoRepository.findById(12L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        produtoService.apagarProduto(12L);

        assertTrue(produto.isApagado());
        assertFalse(produto.isDisponivel());
        verify(produtoRepository).save(produto);
        verify(produtoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("PDF rule: one user cannot have multiple roles simultaneously")
    void shouldRejectRegistrationWithMultipleRoles() {
        UtilizadorRegistoDTO dto = new UtilizadorRegistoDTO(
                "Ana",
                "ana@tasca.pt",
                "pw",
                "client,driver",
                new MoradaDTO("Rua A", 1, null, "1000-001", "Lisboa", "Lisboa", "Portugal")
        );

        assertThatThrownBy(() -> utilizadorService.createUtilizador(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Utilizador type");
    }

    @Test
    @DisplayName("PDF concurrency guard: same driver cannot keep two active deliveries")
    void shouldPreventAssigningSameDriverToTwoActiveDeliveriesBackToBack() {
        Pedido pedido1 = new Pedido(70L, 1L, 2L);
        pedido1.setEstado(EstadoPedido.PRONTO_PARA_ENTREGA);
        Pedido pedido2 = new Pedido(71L, 1L, 2L);
        pedido2.setEstado(EstadoPedido.PRONTO_PARA_ENTREGA);

        Entregador driver = new Entregador("Driver", "driver@tasca.pt", "pw");
        driver.setId(900L);
        driver.setAvailable(true);

        when(pedidoRepository.findById(70L)).thenReturn(Optional.of(pedido1));
        when(pedidoRepository.findById(71L)).thenReturn(Optional.of(pedido2));
        when(entregaRepository.findByPedidoId(70L)).thenReturn(Optional.empty());
        when(entregaRepository.findByPedidoId(71L)).thenReturn(Optional.empty());
        when(utilizadorRepository.findAll()).thenReturn(List.of(driver));
        when(entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(900L)).thenReturn(false);
        when(utilizadorRepository.save(any(Entregador.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> invocation.getArgument(0));

        entregaService.atribuirEntregador(new AtribuirEntregadorDTO(70L));

        assertThatThrownBy(() -> entregaService.atribuirEntregador(new AtribuirEntregadorDTO(71L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não há entregadores disponíveis");
    }

    private Cliente cliente(Long id) {
        Cliente cliente = new Cliente("Cliente", "cliente@tasca.pt", "pw", morada("Rua A", 10, "1000-001", "Lisboa"));
        cliente.setId(id);
        return cliente;
    }

    private Restaurante restaurante() {
        return new Restaurante("Tasca", "123456789", morada("Rua B", 1, "1000-010", "Lisboa"), admin(), new CatalogoEntregas());
    }

    private Restaurante restauranteAberto() {
        Restaurante restaurante = restaurante();
        restaurante.abrir();
        return restaurante;
    }

    private RestauranteAdmin admin() {
        return new RestauranteAdmin("Admin", "admin@tasca.pt", "pw");
    }

    private Morada morada(String rua, int numero, String codigoPostal, String cidade) {
        Morada morada = new Morada();
        morada.setRua(rua);
        morada.setNumero(numero);
        morada.setCodigoPostal(codigoPostal);
        morada.setCidade(cidade);
        morada.setPais("Portugal");
        return morada;
    }
}
