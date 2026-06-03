// ##############################################################
// 					  TASCAEATS						#
// 					CSS GRUPO: 25						#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.delivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.delivery.domain.entrega.Entrega;
import pt.ul.fc.css.delivery.domain.entrega.EstadoEntrega;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByEntregadorId(Long entregadorId);

    Optional<Entrega> findByPedidoId(Long pedidoId);

    List<Entrega> findByHoraEntregaIsNull();

    List<Entrega> findByEstado(EstadoEntrega estado);

    boolean existsByEntregadorIdAndHoraEntregaIsNull(Long entregadorId);

    boolean existsByEntregadorIdAndEstado(Long entregadorId, EstadoEntrega estado);
}
