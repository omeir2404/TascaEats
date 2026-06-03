// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.pedidoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByRestaurantesIdsContaining(Long id);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByClienteIdAndEstado(Long clienteId, EstadoPedido estado);
    boolean existsByIdAndEstado(Long id, EstadoPedido estado);

    @Query("""
        SELECT COUNT(p) > 0
        FROM Pedido p
        WHERE p.entregadorId = :entregadorId
          AND (
            UPPER(COALESCE(p.estadoEntregaReadOnly, '')) IN ('ATRIBUIDA', 'A_CAMINHO')
            OR p.estado = pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido.ATRIBUIDO_ENTREGADOR
          )
    """)
    boolean existsEntregaAtivaByEntregadorId(@Param("entregadorId") Long entregadorId);

    @Query("""
        SELECT p.clienteId
        FROM Pedido p
        WHERE p.data BETWEEN :dataInicio AND :dataFim
        GROUP BY p.clienteId
        ORDER BY COUNT(p.id) DESC, p.clienteId ASC
    """)
    List<Long> findClientesComMaisPedidosNoPeriodo(@Param("dataInicio") LocalDate dataInicio,
                                                   @Param("dataFim") LocalDate dataFim,
                                                   org.springframework.data.domain.Pageable pageable);

    default Optional<Long> findClienteComMaisPedidosNoPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return findClientesComMaisPedidosNoPeriodo(dataInicio, dataFim, PageRequest.of(0, 1)).stream().findFirst();
    }
}
