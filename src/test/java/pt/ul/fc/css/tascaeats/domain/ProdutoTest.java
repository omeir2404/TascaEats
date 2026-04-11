// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;

public class ProdutoTest {

    @Test
    @DisplayName("A: Novo produto deve iniciar com disponivel=true e apagado=false")
    void novoProdutoDeveIniciarComDisponivelTrueEApagadoFalse() {
        Produto produto = criarProduto();

        assertTrue(produto.isDisponivel());
        assertFalse(produto.isApagado());
    }

    @Test
    @DisplayName("B: Indisponibilizar quando produto disponível deve passar")
    void indisponibilizarQuandoProdutoDisponivelDevePassar() {
        Produto produto = criarProduto();

        produto.indisponibilizar();

        assertFalse(produto.isDisponivel());
    }

    @Test
    @DisplayName("C: Indisponibilizar quando já indisponível deve falhar")
    void indisponibilizarQuandoJaIndisponivelDeveFalhar() {
        Produto produto = criarProduto();
        produto.indisponibilizar();

        IllegalStateException exception = assertThrows(IllegalStateException.class, produto::indisponibilizar);

        assertEquals("O produto já está indisponível!", exception.getMessage());
    }

    @Test
    @DisplayName("D: Disponibilizar quando produto indisponível e ativo deve passar")
    void disponibilizarQuandoProdutoIndisponivelEAtivoDevePassar() {
        Produto produto = criarProduto();
        produto.indisponibilizar();

        produto.disponibilizar();

        assertTrue(produto.isDisponivel());
        assertFalse(produto.isApagado());
    }

    @Test
    @DisplayName("E: Disponibilizar quando já disponível deve falhar")
    void disponibilizarQuandoJaDisponivelDeveFalhar() {
        Produto produto = criarProduto();

        IllegalStateException exception = assertThrows(IllegalStateException.class, produto::disponibilizar);

        assertEquals("O produto já está disponível!", exception.getMessage());
    }

    @Test
    @DisplayName("F: Apagar deve marcar apagado=true e disponivel=false")
    void apagarDeveMarcarApagadoTrueEDisponivelFalse() {
        Produto produto = criarProduto();

        produto.apagar();

        assertTrue(produto.isApagado());
        assertFalse(produto.isDisponivel());
    }

    @Test
    @DisplayName("G: Apagar repetido deve falhar.")
    void apagarRepetidoDeveFalhar() {
        Produto produto = criarProduto();
        produto.apagar();

        IllegalStateException exception = assertThrows(IllegalStateException.class, produto::apagar);

        assertEquals("O produto já está apagado!", exception.getMessage());
    }

    @Test
    @DisplayName("H: Ativar um produto apagado deve reativar")
    void ativarEmProdutoApagadoDeveReativar() {
        Produto produto = criarProduto();
        produto.apagar();

        produto.ativar();

        assertFalse(produto.isApagado());
    }

    @Test
    @DisplayName("I: Ativar em produto já ativo deve falhar")
    void ativarEmProdutoJaAtivoDeveFalhar() {
        Produto produto = criarProduto();

        IllegalStateException exception = assertThrows(IllegalStateException.class, produto::ativar);

        assertEquals("O produto já está ativo!", exception.getMessage());
    }

    @Test
    @DisplayName("J: Validar disponibilidade para pedido em produto apagado deve falhar")
    void validarDisponibilidadeParaPedidoEmProdutoApagadoDeveFalhar() {
        Produto produto = criarProduto();
        produto.apagar();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                produto::validarDisponibilidadeParaPedido
        );

        assertEquals("O produto foi apagado e não pode ser pedido!", exception.getMessage());
    }

    @Test
    @DisplayName("K: Validar disponibilidade para pedido em produto indisponível deve falhar")
    void validarDisponibilidadeParaPedidoEmProdutoIndisponivelDeveFalhar() {
        Produto produto = criarProduto();
        produto.indisponibilizar();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                produto::validarDisponibilidadeParaPedido
        );

        assertEquals("O produto está indisponível para ser pedido!", exception.getMessage());
    }

    @Test
    @DisplayName("L: Validar disponibilidade para pedido em produto disponível e ativo deve passar")
    void validarDisponibilidadeParaPedidoEmProdutoDisponivelEAtivoDevePassar() {
        Produto produto = criarProduto();

        assertDoesNotThrow(produto::validarDisponibilidadeParaPedido);
    }


    //**********Funções auxiliares************

    private Produto criarProduto() {
        return new Produto(
                "Bacalhau",
                12.5,
                "Prato do dia",
                restauranteDummy()
        );
    }

    private Restaurante restauranteDummy() {
        Morada morada = new Morada();
        morada.setRua("Rua A");
        morada.setNumero(1);
        morada.setCodigoPostal("1000-100");
        morada.setCidade("Lisboa");
        morada.setDistrito("Lisboa");
        morada.setPais("Portugal");

        return new Restaurante(
                "Restaurante Teste",
                "123456789",
                morada,
                new RestauranteAdmin("Admin", "admin@test.pt", "pw"),
                new CatalogoEntregas()
        );
    }

}