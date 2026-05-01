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

	@Query("""
		SELECT r
		FROM Restaurante r
		WHERE (:nome IS NULL OR LOWER(r.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
		  AND (:tipoRestaurante IS NULL OR r.tipoRestaurante = :tipoRestaurante)
		  AND (:estado IS NULL OR r.estado = :estado)
		  AND (:avaliacaoMinima IS NULL OR r.avaliacaoMedia >= :avaliacaoMinima)
		  AND (:morada IS NULL OR LOWER(CONCAT(
				COALESCE(r.morada.rua, ''), ' ',
				COALESCE(r.morada.cidade, ''), ' ',
				COALESCE(r.morada.distrito, ''), ' ',
				COALESCE(r.morada.pais, ''), ' ',
				COALESCE(r.morada.codigoPostal, '')
		  )) LIKE LOWER(CONCAT('%', :morada, '%')))
		  AND (:precoMedio IS NULL OR r.precoMedio <= :precoMedio)
		  AND (:pedidosRealizados IS NULL OR
				(SELECT COUNT(p.id)
				 FROM Pedido p
				 JOIN p.restaurantesIds restauranteId
				 WHERE restauranteId = r.id) >= :pedidosRealizados)
	""")
	List<Restaurante> pesquisarRestaurantes(@Param("nome") String nome,
											@Param("tipoRestaurante") TipoRestaurante tipoRestaurante,
											@Param("estado") EstadoRestaurante estado,
											@Param("pedidosRealizados") Integer pedidosRealizados,
											@Param("avaliacaoMinima") Double avaliacaoMinima,
											@Param("morada") String morada,
											@Param("precoMedio") Double precoMedio);

	Optional<Restaurante> findByNif(String nif);

	List<Restaurante> findByEstado(EstadoRestaurante estado);

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
