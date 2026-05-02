// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.repository.menuRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.ul.fc.css.tascaeats.domain.menu.Menu;

public interface IMenuRepository extends JpaRepository<Menu, Long> {

    // Procura por nome (parcial e case-insensitive)
    List<Menu> findByNomeContainingIgnoreCase(String nome);

    //Procura por nome exato (ignora maiúsculas)
    Optional<Menu> findByNomeIgnoreCase(String nome);

    // Procura menus pelo número de produtos associados.
    @Query("SELECT m FROM Menu m WHERE size(m.produtos) = :quantidade")
    List<Menu> findByQuantidadeDeProdutos(@Param("quantidade") int quantidade);

    //Busca menus cujo preço médio dos produtos associados seja menor ou igual a um valor.
    @Query("SELECT m FROM Menu m JOIN m.produtos p GROUP BY m HAVING AVG(p.preco) <= :precoMaximo")
    List<Menu> findByPrecoMedioProdutosMenorQue(@Param("precoMaximo") double precoMaximo);

    //Procura menus com preço médio num intervalo.
    @Query("SELECT m FROM Menu m JOIN m.produtos p GROUP BY m HAVING AVG(p.preco) BETWEEN :min AND :max")
    List<Menu> findByPrecoMedioNoIntervalo(@Param("min") double min, @Param("max") double max);
}

