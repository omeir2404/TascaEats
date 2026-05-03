// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;

@DataJpaTest
public class IProdutoRepositoryTest {

    @Autowired
    private IProdutoRepository produtoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("A: Pesquisar produto por nome sem case-sensitive.")
    void findByNomeContainingIgnoreCaseDevePesquisarSemCaseSensitive() {
        guardarProduto("Massa Carbonara", true, false, "111111111", "admin1@gmail.com");
        guardarProduto("Pizza Pepperoni", true, false, "222222222", "admin2@gmail.com");
        guardarProduto("Massa da casa", true, false, "333333333", "admin3@gmail.com");

        List<Produto> resultados = produtoRepository.findByNomeContainingIgnoreCase("mAsSa");

        assertThat(resultados)
                .hasSize(2)
                .extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Massa Carbonara", "Massa da casa");
    }

    @Test
    @DisplayName("B: Devolver apenas produtos disponíveis.")
    void findByDisponivelTrueDeveDevolverApenasProdutosDisponiveis() {
        guardarProduto("Disponivel 1", true, false, "444444441", "admin4@gmail.com");
        guardarProduto("Indisponivel", false, false, "444444442", "admin5@gmail.com");
        guardarProduto("Disponivel 2", true, false, "444444443", "admin6@gmail.com");

        List<Produto> resultados = produtoRepository.findByDisponivelTrue();

        assertThat(resultados)
                .extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Disponivel 1", "Disponivel 2");
        assertThat(resultados).allMatch(Produto::isDisponivel);
    }

    @Test
    @DisplayName("C: Excluir produtos (soft-delete)")
    void findByApagadoFalseDeveExcluirProdutosSoftDeleted() {
        guardarProduto("Ativo", true, false, "555555551", "admin7@gmail.com");
        guardarProduto("Soft Deleted", false, true, "555555552", "admin8@gmail.com");
        guardarProduto("Outro Ativo", false, false, "555555553", "admin9@gmail.com");

        List<Produto> resultados = produtoRepository.findByApagadoFalse();

        assertThat(resultados)
                .extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Ativo", "Outro Ativo");
        assertThat(resultados).allMatch(produto -> !produto.isApagado());
    }


    //**********Funções auxiliares************

    private Produto guardarProduto(
            String nome,
            boolean disponivel,
            boolean apagado,
            String nifRestaurante,
            String emailAdmin
    ) {
        Restaurante restaurante = guardarRestaurante(nifRestaurante, emailAdmin);
        Produto produto = new Produto(nome, 12.5, "Descrição " + nome, restaurante);

        if (!disponivel && !apagado) {
            produto.indisponibilizar();
        }
        if (apagado) {
            produto.apagar();
        }

        entityManager.persist(produto);
        entityManager.flush();
        return produto;
    }

    private Restaurante guardarRestaurante(String nif, String emailAdmin) {
        RestauranteAdmin admin = new RestauranteAdmin("Admin " + nif, emailAdmin, "password123");
        entityManager.persist(admin);

        Restaurante restaurante = new Restaurante(
                "Restaurante " + nif,
                nif,
                criarMorada(nif),
                admin,
                new CatalogoEntregas()
        );

        entityManager.persist(restaurante);
        entityManager.flush();
        return restaurante;
    }

    private Morada criarMorada(String sufixo) {
        Morada morada = new Morada();
        morada.setRua("Rua " + sufixo);
        morada.setNumero(1);
        morada.setCodigoPostal("1000-000");
        morada.setCidade("Lisboa");
        morada.setDistrito("Lisboa");
        morada.setPais("Portugal");
        return morada;
    }

}