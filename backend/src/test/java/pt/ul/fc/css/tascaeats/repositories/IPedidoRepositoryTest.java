// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;

@DataJpaTest
public class IPedidoRepositoryTest {

    @Autowired
    private IPedidoRepository pedidoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("A: findByClienteId deve devolver pedidos do cliente")
    void findByClienteIdDeveDevolverPedidosDoCliente() {
        guardarPedido(1L, 10L, EstadoPedido.CRIADO, LocalDate.of(2026, 1, 10));
        guardarPedido(1L, 11L, EstadoPedido.PAGO, LocalDate.of(2026, 1, 11));
        guardarPedido(2L, 10L, EstadoPedido.CRIADO, LocalDate.of(2026, 1, 12));

        List<Pedido> resultados = pedidoRepository.findByClienteId(1L);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).allMatch(p -> p.getClienteId().equals(1L));
    }

    @Test
    @DisplayName("B: findByRestaurantesIdsContaining deve devolver pedidos do restaurante")
    void findByRestaurantesIdsContainingDeveDevolverPedidosDoRestaurante() {
        guardarPedido(1L, 20L, EstadoPedido.CRIADO, LocalDate.of(2026, 2, 1));
        guardarPedido(2L, 20L, EstadoPedido.PAGO, LocalDate.of(2026, 2, 2));
        guardarPedido(3L, 21L, EstadoPedido.CRIADO, LocalDate.of(2026, 2, 3));

        List<Pedido> resultados = pedidoRepository.findByRestaurantesIdsContaining(20L);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).allMatch(p -> p.getRestaurantesIds().contains(20L));
    }

    @Test
    @DisplayName("C: findByEstado deve filtrar por estado")
    void findByEstadoDeveFiltrarPorEstado() {
        guardarPedido(1L, 10L, EstadoPedido.CRIADO, LocalDate.of(2026, 3, 1));
        guardarPedido(2L, 10L, EstadoPedido.PAGO, LocalDate.of(2026, 3, 2));
        guardarPedido(3L, 10L, EstadoPedido.PAGO, LocalDate.of(2026, 3, 3));

        List<Pedido> resultados = pedidoRepository.findByEstado(EstadoPedido.PAGO);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).allMatch(p -> p.getEstado() == EstadoPedido.PAGO);
    }

    @Test
    @DisplayName("D: findByClienteIdAndEstado deve aplicar filtro combinado")
    void findByClienteIdAndEstadoDeveAplicarFiltroCombinado() {
        guardarPedido(5L, 30L, EstadoPedido.CRIADO, LocalDate.of(2026, 4, 1));
        guardarPedido(5L, 31L, EstadoPedido.PAGO, LocalDate.of(2026, 4, 2));
        guardarPedido(6L, 30L, EstadoPedido.CRIADO, LocalDate.of(2026, 4, 3));

        List<Pedido> resultados = pedidoRepository.findByClienteIdAndEstado(5L, EstadoPedido.CRIADO);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getClienteId()).isEqualTo(5L);
        assertThat(resultados.get(0).getEstado()).isEqualTo(EstadoPedido.CRIADO);
    }

    @Test
    @DisplayName("E: existsByIdAndEstado deve retornar verdadeiro/falso corretamente")
    void existsByIdAndEstadoDeveRetornarCorretamente() {
        Pedido pedidoCriado = guardarPedido(7L, 40L, EstadoPedido.CRIADO, LocalDate.of(2026, 5, 1));

        boolean existeComEstadoCriado = pedidoRepository.existsByIdAndEstado(pedidoCriado.getId(), EstadoPedido.CRIADO);
        boolean existeComEstadoPago = pedidoRepository.existsByIdAndEstado(pedidoCriado.getId(), EstadoPedido.PAGO);

        assertThat(existeComEstadoCriado).isTrue();
        assertThat(existeComEstadoPago).isFalse();
    }

    @Test
    @DisplayName("F: deve identificar o cliente com mais pedidos num intervalo de tempo especifico")
    void findClienteComMaisPedidosNoPeriodoDeveIdentificarClienteCorreto() {
        guardarPedido(100L, 50L, EstadoPedido.PAGO, LocalDate.of(2026, 6, 1));
        guardarPedido(100L, 51L, EstadoPedido.PAGO, LocalDate.of(2026, 6, 5));
        guardarPedido(100L, 52L, EstadoPedido.ENTREGUE, LocalDate.of(2026, 6, 9));
        guardarPedido(200L, 50L, EstadoPedido.PAGO, LocalDate.of(2026, 6, 10));
        guardarPedido(200L, 51L, EstadoPedido.PAGO, LocalDate.of(2026, 6, 12));
        guardarPedido(300L, 51L, EstadoPedido.PAGO, LocalDate.of(2026, 7, 1));

        Optional<Long> resultado = pedidoRepository.findClienteComMaisPedidosNoPeriodo(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        );

        assertThat(resultado).contains(100L);
    }

    private Pedido guardarPedido(Long clienteId, Long restauranteId, EstadoPedido estado, LocalDate data) {
        Pedido pedido = new Pedido(null, clienteId);
        pedido.adicionarProduto(restauranteId * 100, restauranteId);
        pedido.setEstado(estado);
        pedido.setData(data);
        entityManager.persist(pedido);
        entityManager.flush();
        return pedido;
    }
}
