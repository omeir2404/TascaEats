// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.entregaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;

public interface IEntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByEntregadorId(Long entregadorId);
    Optional<Entrega> findByPedidoId(Long pedidoId);
    List<Entrega> findByHoraEntregaIsNull();
    boolean existsByEntregadorIdAndHoraEntregaIsNull(Long entregadorId);
}
