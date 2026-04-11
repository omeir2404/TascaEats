package pt.ul.fc.css.tascaeats.repository.produtoRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;

public interface IProdutoRepository extends JpaRepository<Produto, Long> {

	List<Produto> findByNomeContainingIgnoreCase(String nome);

	List<Produto> findByDisponivelTrue();

	List<Produto> findByApagadoFalse();

	List<Produto> findByRestauranteId(Long restauranteId);
}