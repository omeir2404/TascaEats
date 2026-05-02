// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repository.produtoRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.ul.fc.css.tascaeats.domain.produto.Produto;

public interface IProdutoRepository extends JpaRepository<Produto, Long> {

	List<Produto> findByNomeContainingIgnoreCase(String nome);

	List<Produto> findByDisponivelTrue();

	List<Produto> findByApagadoFalse();

	List<Produto> findByRestauranteId(Long restauranteId);

	List<Produto> findByMenuId(Long menuId);

	List<Produto> findByPrecoLessThanEqual(Double preco);

	@Query("""
		SELECT p
		FROM Produto p
		WHERE p.apagado = false
		AND EXISTS (
			SELECT 1 FROM p.pedidos ped
			WHERE ped.data BETWEEN :dataInicio AND :dataFim
		)
		ORDER BY SIZE(p.pedidos) DESC
	""")
	List<Produto> findByPopularityInRange(@Param("dataInicio") LocalDate dataInicio,
										   @Param("dataFim") LocalDate dataFim);
}
