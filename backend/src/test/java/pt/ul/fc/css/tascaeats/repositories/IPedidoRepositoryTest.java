// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repositories;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        guardarPedido(1L, 10L, EstadoPedido.CRIADO);
        guardarPedido(1L, 11L, EstadoPedido.PAGO);
        guardarPedido(2L, 10L, EstadoPedido.CRIADO);

        List<Pedido> resultados = pedidoRepository.findByClienteId(1L);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).allMatch(p -> p.getclienteId().equals(1L));
    }

    @Test
    @DisplayName("B: findByRestauranteId deve devolver pedidos do restaurante")
    void findByRestauranteIdDeveDevolverPedidosDoRestaurante() {
        guardarPedido(1L, 20L, EstadoPedido.CRIADO);
        guardarPedido(2L, 20L, EstadoPedido.PAGO);
        guardarPedido(3L, 21L, EstadoPedido.CRIADO);

        List<Pedido> resultados = pedidoRepository.findByRestauranteId(20L);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).allMatch(p -> p.getRestauranteId().equals(20L));
    }

    @Test
    @DisplayName("C: findByEstado deve filtrar por estado")
    void findByEstadoDeveFiltrarPorEstado() {
        guardarPedido(1L, 10L, EstadoPedido.CRIADO);
        guardarPedido(2L, 10L, EstadoPedido.PAGO);
        guardarPedido(3L, 10L, EstadoPedido.PAGO);

        List<Pedido> resultados = pedidoRepository.findByEstado(EstadoPedido.PAGO);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).allMatch(p -> p.getEstado() == EstadoPedido.PAGO);
    }

    @Test
    @DisplayName("D: findByClienteIdAndEstado deve aplicar filtro combinado")
    void findByClienteIdAndEstadoDeveAplicarFiltroCombinado() {
        guardarPedido(5L, 30L, EstadoPedido.CRIADO);
        guardarPedido(5L, 31L, EstadoPedido.PAGO);
        guardarPedido(6L, 30L, EstadoPedido.CRIADO);

        List<Pedido> resultados = pedidoRepository.findByClienteIdAndEstado(5L, EstadoPedido.CRIADO);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getclienteId()).isEqualTo(5L);
        assertThat(resultados.get(0).getEstado()).isEqualTo(EstadoPedido.CRIADO);
    }

    @Test
    @DisplayName("E: existsByIdAndEstado deve retornar verdadeiro/falso corretamente")
    void existsByIdAndEstadoDeveRetornarCorretamente() {
        Pedido pedidoCriado = guardarPedido(7L, 40L, EstadoPedido.CRIADO);

        boolean existeComEstadoCriado = pedidoRepository.existsByIdAndEstado(pedidoCriado.getId(), EstadoPedido.CRIADO);
        boolean existeComEstadoPago = pedidoRepository.existsByIdAndEstado(pedidoCriado.getId(), EstadoPedido.PAGO);

        assertThat(existeComEstadoCriado).isTrue();
        assertThat(existeComEstadoPago).isFalse();
    }

    private Pedido guardarPedido(Long clienteId, Long restauranteId, EstadoPedido estado) {
        Pedido pedido = new Pedido(null, clienteId, restauranteId);
        pedido.setEstado(estado);
        entityManager.persist(pedido);
        entityManager.flush();
        return pedido;
    }
}
