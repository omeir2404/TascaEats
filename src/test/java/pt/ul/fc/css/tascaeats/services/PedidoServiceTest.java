// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.services;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;
import pt.ul.fc.css.tascaeats.service.pedidoService.PedidoService;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

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

	// A) criarPedido com DTO nulo deve falhar.
	@Test
	@DisplayName("A: Criar pedido com DTO nulo deve falhar.")
	void criarPedidoWithNullDTO() {
		try {
			pedidoService.criarPedido(null);
			fail("O pedido não pode ser nulo.");
		} catch (IllegalArgumentException e) {
			assertEquals("Pedido inválido.", e.getMessage());
		}
	}

	// B) criarPedido com lista de produtos vazia deve falhar.
	@Test
	@DisplayName("B: Criar pedido com lista de produtos vazia deve falhar.")
	void criarPedidoWithEmptyProdutos() {
		CriarPedidoDTO dto = criarPedidoDTOValido();
		dto.setProdutosIds(List.of());

		try {
			pedidoService.criarPedido(dto);
			fail("O pedido deve conter pelo menos um produto.");
		} catch (IllegalArgumentException e) {
			assertEquals("O pedido deve conter pelo menos um produto.", e.getMessage());
		}
	}

	// C) criarPedido com cliente inexistente deve falhar.
	@Test
	@DisplayName("C: Criar pedido com cliente inexistente deve falhar.")
	void criarPedidoWithClienteInexistente() {
		CriarPedidoDTO dto = criarPedidoDTOValido();

		when(utilizadorRepository.findById(1L)).thenReturn(Optional.empty());

		try {
			pedidoService.criarPedido(dto);
			fail("Cliente inexistente.");
		} catch (IllegalArgumentException e) {
			assertEquals("Cliente não encontrado: 1", e.getMessage());
		}
	}

	// D) criarPedido com restaurante inexistente deve falhar.
	@Test
	@DisplayName("D: Criar pedido com restaurante inexistente deve falhar.")
	void criarPedidoWithRestauranteInexistente() {
		CriarPedidoDTO dto = criarPedidoDTOValido();

		when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(clienteValido()));
		when(restauranteRepository.findById(10L)).thenReturn(Optional.empty());

		try {
			pedidoService.criarPedido(dto);
			fail("Restaurante inexistente.");
		} catch (IllegalArgumentException e) {
			assertEquals("Restaurante não encontrado: 10", e.getMessage());
		}
	}

	// E) criarPedido com produto inexistente deve falhar.
	@Test
	@DisplayName("E: Criar pedido com produto inexistente deve falhar.")
	void criarPedidoWithProdutoInexistente() {
		CriarPedidoDTO dto = criarPedidoDTOValido();

		when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(clienteValido()));
		when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteValido()));
		when(produtoRepository.findById(100L)).thenReturn(Optional.empty());

		try {
			pedidoService.criarPedido(dto);
			fail("Produto inexistente.");
		} catch (IllegalArgumentException e) {
			assertEquals("Produto não encontrado: 100", e.getMessage());
		}
	}

	// F) criarPedido com sucesso deve calcular valorTotal corretamente.
	@Test
	@DisplayName("F: Criar pedido com sucesso deve calcular valor total corretamente.")
	void criarPedidoSuccessCalculaValorTotal() {
		CriarPedidoDTO dto = criarPedidoDTOValido();
		Produto produto1 = new Produto("Massa", 10.0, "Massa fresca", restauranteValido());
		Produto produto2 = new Produto("Pizza", 5.5, "Pizza margherita", restauranteValido());

		when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(clienteValido()));
		when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteValido()));
		when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
		when(produtoRepository.findById(101L)).thenReturn(Optional.of(produto2));
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

		pedidoService.criarPedido(dto);

		ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
		verify(pedidoRepository).save(pedidoCaptor.capture());
		Pedido pedidoGuardado = pedidoCaptor.getValue();

		assertEquals(15.5, pedidoGuardado.getValorTotal());
	}

	// G) criarPedido com sucesso deve criar pedido no estado CRIADO.
	@Test
	@DisplayName("G: Criar pedido com sucesso deve guardar pedido no estado CRIADO.")
	void criarPedidoSuccessEstadoCriado() {
		CriarPedidoDTO dto = criarPedidoDTOValido();
		Produto produto1 = new Produto("Massa", 7.0, "Massa fresca", restauranteValido());
		Produto produto2 = new Produto("Pizza", 3.0, "Pizza margherita", restauranteValido());

		when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(clienteValido()));
		when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteValido()));
		when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
		when(produtoRepository.findById(101L)).thenReturn(Optional.of(produto2));
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

		pedidoService.criarPedido(dto);

		ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
		verify(pedidoRepository).save(pedidoCaptor.capture());
		Pedido pedidoGuardado = pedidoCaptor.getValue();

		assertEquals(EstadoPedido.CRIADO, pedidoGuardado.getEstado());
	}

	// H) cancelarPedido com pedidoId nulo deve falhar.
	@Test
	@DisplayName("H: Cancelar pedido com pedidoId nulo deve falhar.")
	void cancelarPedidoWithNullId() {
		try {
			pedidoService.cancelarPedido(null);
			fail("ID do pedido inválido.");
		} catch (IllegalArgumentException e) {
			assertEquals("ID do pedido inválido.", e.getMessage());
		}
	}

	// I) cancelarPedido com pedido inexistente deve falhar.
	@Test
	@DisplayName("I: Cancelar pedido inexistente deve falhar.")
	void cancelarPedidoInexistente() {
		when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

		try {
			pedidoService.cancelarPedido(99L);
			fail("Pedido inexistente.");
		} catch (IllegalArgumentException e) {
			assertEquals("Pedido não encontrado: 99", e.getMessage());
		}
	}

	// J) cancelarPedido com estado CRIADO deve passar.
	@Test
	@DisplayName("J: Cancelar pedido no estado CRIADO deve passar.")
	void cancelarPedidoEstadoCriado() {
		Pedido pedido = new Pedido(1L, 1L, 10L);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

		pedidoService.cancelarPedido(1L);

		assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
		verify(pedidoRepository).save(pedido);
	}

	// K) cancelarPedido com estado PAGO deve passar.
	@Test
	@DisplayName("K: Cancelar pedido no estado PAGO deve passar.")
	void cancelarPedidoEstadoPago() {
		Pedido pedido = new Pedido(2L, 1L, 10L);
		pedido.setEstado(EstadoPedido.PAGO);

		when(pedidoRepository.findById(2L)).thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

		pedidoService.cancelarPedido(2L);

		assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
		verify(pedidoRepository).save(pedido);
	}

	// L) cancelarPedido com estado A_PREPARAR deve falhar.
	@Test
	@DisplayName("L: Cancelar pedido no estado A_PREPARAR deve falhar.")
	void cancelarPedidoEstadoAPrepararFalha() {
		Pedido pedido = new Pedido(3L, 1L, 10L);
		pedido.setEstado(EstadoPedido.A_PREPARAR);
		when(pedidoRepository.findById(3L)).thenReturn(Optional.of(pedido));

		try {
			pedidoService.cancelarPedido(3L);
			fail("Não pode cancelar pedido em preparação.");
		} catch (IllegalStateException e) {
			assertEquals("Pedido no estado A_PREPARAR não pode ser cancelado.", e.getMessage());
		}

		verify(pedidoRepository, never()).save(any(Pedido.class));
	}

	// M) cancelarPedido com estado PRONTO_PARA_ENTREGA deve falhar.
	@Test
	@DisplayName("M: Cancelar pedido no estado PRONTO_PARA_ENTREGA deve falhar.")
	void cancelarPedidoEstadoProntoParaEntregaFalha() {
		Pedido pedido = new Pedido(4L, 1L, 10L);
		pedido.setEstado(EstadoPedido.PRONTO_PARA_ENTREGA);
		when(pedidoRepository.findById(4L)).thenReturn(Optional.of(pedido));

		try {
			pedidoService.cancelarPedido(4L);
			fail("Não pode cancelar pedido pronto para entrega.");
		} catch (IllegalStateException e) {
			assertEquals("Pedido no estado PRONTO_PARA_ENTREGA não pode ser cancelado.", e.getMessage());
		}

		verify(pedidoRepository, never()).save(any(Pedido.class));
	}

	// N) listarPedidos com clienteId e estado deve usar filtro combinado.
	@Test
	@DisplayName("N: Listar pedidos com clienteId e estado deve usar filtro combinado.")
	void listarPedidosWithClienteAndEstado() {
		Pedido pedido = new Pedido(11L, 1L, 10L);
		pedido.setEstado(EstadoPedido.PAGO);

		when(pedidoRepository.findByClienteIdAndEstado(1L, EstadoPedido.PAGO)).thenReturn(List.of(pedido));

		List<Pedido> result = pedidoService.listarPedidos(1L, EstadoPedido.PAGO);

		assertEquals(1, result.size());
		assertEquals(EstadoPedido.PAGO, result.get(0).getEstado());
		verify(pedidoRepository).findByClienteIdAndEstado(1L, EstadoPedido.PAGO);
	}

	// O) listarPedidos só com clienteId deve filtrar por cliente.
	@Test
	@DisplayName("O: Listar pedidos só com clienteId deve filtrar por cliente.")
	void listarPedidosWithClienteOnly() {
		Pedido pedido = new Pedido(12L, 8L, 10L);
		when(pedidoRepository.findByClienteId(8L)).thenReturn(List.of(pedido));

		List<Pedido> result = pedidoService.listarPedidos(8L, null);

		assertEquals(1, result.size());
		assertEquals(8L, result.get(0).getclienteId());
		verify(pedidoRepository).findByClienteId(8L);
	}

	// P) listarPedidos só com estado deve filtrar por estado.
	@Test
	@DisplayName("P: Listar pedidos só com estado deve filtrar por estado.")
	void listarPedidosWithEstadoOnly() {
		Pedido pedido = new Pedido(13L, 2L, 10L);
		pedido.setEstado(EstadoPedido.CANCELADO);

		when(pedidoRepository.findByEstado(EstadoPedido.CANCELADO)).thenReturn(List.of(pedido));

		List<Pedido> result = pedidoService.listarPedidos(null, EstadoPedido.CANCELADO);

		assertEquals(1, result.size());
		assertEquals(EstadoPedido.CANCELADO, result.get(0).getEstado());
		verify(pedidoRepository).findByEstado(EstadoPedido.CANCELADO);
	}

	// Q) listarPedidos sem filtros deve devolver todos.
	@Test
	@DisplayName("Q: Listar pedidos sem filtros deve devolver todos.")
	void listarPedidosWithoutFilters() {
		Pedido pedido1 = new Pedido(21L, 1L, 10L);
		Pedido pedido2 = new Pedido(22L, 2L, 20L);

		when(pedidoRepository.findAll()).thenReturn(List.of(pedido1, pedido2));

		List<Pedido> result = pedidoService.listarPedidos(null, null);

		assertEquals(2, result.size());
		assertTrue(result.stream().anyMatch(p -> p.getId().equals(21L)));
		assertTrue(result.stream().anyMatch(p -> p.getId().equals(22L)));
		verify(pedidoRepository).findAll();
	}

	private CriarPedidoDTO criarPedidoDTOValido() {
		return new CriarPedidoDTO(1L, 10L, List.of(100L, 101L));
	}

	private Cliente clienteValido() {
		return new Cliente("Cliente", "cliente@tasca.com", "1234", morada("Rua A", 10, "1000-001", "Lisboa"));
	}

	private Restaurante restauranteValido() {
		Restaurante restaurante = new Restaurante(
			"Tasca Eats",
			"123456789",
			morada("Rua B", 20, "1000-010", "Porto"),
			adminValido(),
			new CatalogoEntregas()
		);
		restaurante.abrir();
		return restaurante;
	}

	private RestauranteAdmin adminValido() {
		return new RestauranteAdmin("Admin", "admin@tasca.com", "1234");
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
