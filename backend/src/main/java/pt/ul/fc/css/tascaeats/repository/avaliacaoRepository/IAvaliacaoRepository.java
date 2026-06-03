// ##############################################################
// 					  TASCAEATS								#
// 					CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.repository.avaliacaoRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.ul.fc.css.tascaeats.domain.avaliacao.Avaliacao;

public interface IAvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    boolean existsByPedidoId(Long pedidoId);
    Optional<Avaliacao> findByPedidoId(Long pedidoId);

    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.restaurante.id = :restauranteId")
    Integer countByRestauranteId(@Param("restauranteId") Long restauranteId);

    @Query("SELECT AVG(a.classificacao) FROM Avaliacao a WHERE a.restaurante.id = :restauranteId")
    Double calculateAverageRatingByRestauranteId(@Param("restauranteId") Long restauranteId);

    List<Avaliacao> findByRestauranteId(Long restauranteId);
}
