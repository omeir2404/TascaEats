package pt.ul.fc.css.tascaeats.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.ul.fc.css.tascaeats.domain.produto.CatalogoProdutos;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Morada;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RestauranteTest {

    @Test
    @DisplayName("A: Novo restaurante deve iniciar em FECHADO")
    void novoRestauranteDeveIniciarEmFechado() {
        Restaurante restaurante = criarRestaurante();

        assertEquals(EstadoRestaurante.FECHADO, restaurante.getEstado());
    }

    @Test
    @DisplayName("B: Abrir a partir de FECHADO deve passar")
    void abrirAPartirDeFechadoDevePassar() {
        Restaurante restaurante = criarRestaurante();

        restaurante.abrir();

        assertEquals(EstadoRestaurante.ABERTO, restaurante.getEstado());
    }

    @Test
    @DisplayName("C: Abrir quando já ABERTO deve falhar")
    void abrirQuandoJaAbertoDeveFalhar() {
        Restaurante restaurante = criarRestaurante();
        restaurante.abrir();

        IllegalStateException exception = assertThrows(IllegalStateException.class, restaurante::abrir);

        assertEquals("O restaurante já está aberto!", exception.getMessage());
    }

    @Test
    @DisplayName("D: Fechar a partir de ABERTO deve passar")
    void fecharAPartirDeAbertoDevePassar() {
        Restaurante restaurante = criarRestaurante();
        restaurante.abrir();

        restaurante.fechar();

        assertEquals(EstadoRestaurante.FECHADO, restaurante.getEstado());
    }

    @Test
    @DisplayName("É: Fechar quando ja FECHADO deve falhar.")
    void fecharQuandoJaFechadoDeveFalhar() {
        Restaurante restaurante = criarRestaurante();

        IllegalStateException exception = assertThrows(IllegalStateException.class, restaurante::fechar);

        assertEquals("O restaurante já está fechado!", exception.getMessage());
    }

    @Test
    @DisplayName("F: Reservar a partir de ABERTO e FECHADO deve passar")
    void reservarAPartirDeAbertoEFechadoDevePassar() {
        Restaurante restauranteFechado = criarRestaurante();
        Restaurante restauranteAberto = criarRestaurante();
        restauranteAberto.abrir();

        restauranteFechado.reservar();
        restauranteAberto.reservar();

        assertEquals(EstadoRestaurante.RESERVADO, restauranteFechado.getEstado());
        assertEquals(EstadoRestaurante.RESERVADO, restauranteAberto.getEstado());
    }

    @Test
    @DisplayName("G: Reservar quando ja RESERVADO deve falhar")
    void reservarQuandoJaReservadoDeveFalhar() {
        Restaurante restaurante = criarRestaurante();
        restaurante.reservar();

        IllegalStateException exception = assertThrows(IllegalStateException.class, restaurante::reservar);

        assertEquals("O restaurante já está reservado!", exception.getMessage());
    }

    @Test
    @DisplayName("H: Validar disponibilidade para pedido com estado diferente de ABERTO deve falhar")
    void validarDisponibilidadeParaPedidoComEstadoDiferenteDeAbertoDeveFalhar() {
        Restaurante restauranteFechado = criarRestaurante();
        Restaurante restauranteReservado = criarRestaurante();
        restauranteReservado.reservar();

        IllegalStateException exceptionFechado = assertThrows(
                IllegalStateException.class,
                restauranteFechado::validarDisponibilidadeParaPedido
        );
        IllegalStateException exceptionReservado = assertThrows(
                IllegalStateException.class,
                restauranteReservado::validarDisponibilidadeParaPedido
        );

        assertEquals("O restaurante está fechado. Não é possível fazer pedidos!", exceptionFechado.getMessage());
        assertEquals("O restaurante está fechado. Não é possível fazer pedidos!", exceptionReservado.getMessage());
    }

    @Test
    @DisplayName("I: Validar disponibilidade para pedido com estado ABERTO deve passar")
    void validarDisponibilidadeParaPedidoComEstadoAbertoDevePassar() {
        Restaurante restaurante = criarRestaurante();
        restaurante.abrir();

        assertDoesNotThrow(restaurante::validarDisponibilidadeParaPedido);
    }


    //**********Funções auxiliares************

    private Restaurante criarRestaurante() {
        return new Restaurante(
                "Tasquinha",
                "123456789",
                new Morada("Rua A", 10, "1000-100", "Lisboa"),
                new RestauranteAdmin("Admin", "admin@tascaeats.pt", "password123"),
                new CatalogoProdutos(),
                new CatalogoEntregas()
        );
    }
}
