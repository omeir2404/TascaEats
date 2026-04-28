// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.pedidoRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByRestaurantesIdsContaining(Long id);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByClienteIdAndEstado(Long clienteId, EstadoPedido estado);
    boolean existsByIdAndEstado(Long id, EstadoPedido estado);
}
