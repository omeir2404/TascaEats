package pt.ul.fc.css.tascaeats.repository.produtoRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.produto.CatalogoProdutos;

public interface ICatalogoProdutosRepository extends JpaRepository<CatalogoProdutos, Long> {
}