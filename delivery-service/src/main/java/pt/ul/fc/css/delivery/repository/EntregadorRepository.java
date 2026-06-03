// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.delivery.domain.entregador.Entregador;

public interface EntregadorRepository extends JpaRepository<Entregador, Long> {
    Entregador findByEmail(String email);

    List<Entregador> findByAvailable(boolean available);

    List<Entregador> findByNomeContainingIgnoreCase(String nome);
}
