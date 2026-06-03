// ##############################################################
// 					  TASCAEATS						#
// 					CSS GRUPO: 25						#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.delivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.delivery.domain.pedido.PedidoPagoRecebido;

public interface PedidoPagoRecebidoRepository extends JpaRepository<PedidoPagoRecebido, Long> {
    Optional<PedidoPagoRecebido> findByPedidoId(Long pedidoId);
}
