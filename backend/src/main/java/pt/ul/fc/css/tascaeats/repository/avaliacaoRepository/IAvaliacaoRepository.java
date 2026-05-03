// ##############################################################
// 					  TASCAEATS								#
// 					CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.avaliacaoRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.avaliacao.Avaliacao;

public interface IAvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    boolean existsByPedidoId(Long pedidoId);
    Optional<Avaliacao> findByPedidoId(Long pedidoId);
}
