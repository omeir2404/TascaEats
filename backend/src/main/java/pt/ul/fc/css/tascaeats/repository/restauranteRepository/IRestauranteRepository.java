// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.restauranteRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.TipoRestaurante;

public interface IRestauranteRepository extends JpaRepository<Restaurante, Long> {

	List<Restaurante> findByNomeContainingIgnoreCase(String nome);

	List<Restaurante> findByMoradaCidadeIgnoreCase(String cidade);

	List<Restaurante> findByTipoRestaurante(TipoRestaurante tipoRestaurante);

	Optional<Restaurante> findByNif(String nif);

	List<Restaurante> findByEstado(EstadoRestaurante estado);

	List<Restaurante> findByMenuId(Long menuId);

	@Query(value = """
		SELECT r.*
		FROM restaurante r
		WHERE r.id = (
			SELECT prr.restaurante_id
			FROM pedido_restaurantes prr
			JOIN pedidos p
			  ON p.id = prr.pedido_id
			JOIN restaurante r2
			  ON r2.id = prr.restaurante_id
			WHERE r2.franquia = :franquia
			GROUP BY prr.restaurante_id
			ORDER BY COUNT(p.id) DESC, prr.restaurante_id ASC
			FETCH FIRST 1 ROW ONLY
		)
	""", nativeQuery = true)
	Optional<Restaurante> findRestauranteMaisPopularDaFranquia(@Param("franquia") String franquia);
}
