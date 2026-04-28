// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;

public class PedidoTest {

    @Test
    @DisplayName("Pedido: confirmarPagamento de CRIADO para PAGO deve passar")
    void confirmarPagamentoDeCriadoParaPago() {
        Pedido pedido = pedidoComEstado(EstadoPedido.CRIADO);

        pedido.confirmarPagamento();

        assertEquals(EstadoPedido.PAGO, pedido.getEstado());
    }

    @Test
    @DisplayName("Pedido: confirmarPagamento fora de CRIADO deve falhar")
    void confirmarPagamentoForaDeCriado() {
        Pedido pedido = pedidoComEstado(EstadoPedido.PAGO);

        IllegalStateException exception = assertThrows(IllegalStateException.class, pedido::confirmarPagamento);
        assertEquals("O pedido só pode ser pago se estiver no estado CRIADO.", exception.getMessage());
    }

    @Test
    @DisplayName("Pedido: iniciarPreparacao de PAGO para A_PREPARAR deve passar")
    void iniciarPreparacaoDePagoParaAPreparar() {
        Pedido pedido = pedidoComEstado(EstadoPedido.PAGO);

        pedido.iniciarPreparacao();

        assertEquals(EstadoPedido.A_PREPARAR, pedido.getEstado());
    }

    @Test
    @DisplayName("Pedido: iniciarPreparacao fora de PAGO deve falhar")
    void iniciarPreparacaoForaDePago() {
        Pedido pedido = pedidoComEstado(EstadoPedido.CRIADO);

        IllegalStateException exception = assertThrows(IllegalStateException.class, pedido::iniciarPreparacao);
        assertEquals("O pedido precisa de ser pago antes de ser preparado.", exception.getMessage());
    }

    @Test
    @DisplayName("Pedido: marcarComoPronto de A_PREPARAR para PRONTO_PARA_ENTREGA deve passar")
    void marcarComoProntoDeAPrepararParaPronto() {
        Pedido pedido = pedidoComEstado(EstadoPedido.A_PREPARAR);

        pedido.marcarComoPronto();

        assertEquals(EstadoPedido.PRONTO_PARA_ENTREGA, pedido.getEstado());
    }

    @Test
    @DisplayName("Pedido: atribuirEntregador de PRONTO_PARA_ENTREGA para ATRIBUIDO_ENTREGADOR deve passar")
    void atribuirEntregadorDeProntoParaAtribuido() {
        Pedido pedido = pedidoComEstado(EstadoPedido.PRONTO_PARA_ENTREGA);

        pedido.atribuirEntregador();

        assertEquals(EstadoPedido.ATRIBUIDO_ENTREGADOR, pedido.getEstado());
    }

    @Test
    @DisplayName("Pedido: marcarComoEntregue de ATRIBUIDO_ENTREGADOR para ENTREGUE deve passar")
    void marcarComoEntregueDeAtribuidoParaEntregue() {
        Pedido pedido = pedidoComEstado(EstadoPedido.ATRIBUIDO_ENTREGADOR);

        pedido.marcarComoEntregue();

        assertEquals(EstadoPedido.ENTREGUE, pedido.getEstado());
    }

    @Test
    @DisplayName("Pedido: cancelar em estados permitidos deve mudar para CANCELADO")
    void cancelarEmEstadosPermitidos() {
        Pedido criado = pedidoComEstado(EstadoPedido.CRIADO);
        Pedido pago = pedidoComEstado(EstadoPedido.PAGO);

        criado.cancelar();
        pago.cancelar();

        assertEquals(EstadoPedido.CANCELADO, criado.getEstado());
        assertEquals(EstadoPedido.CANCELADO, pago.getEstado());
    }

    @Test
    @DisplayName("Pedido: cancelar em estados bloqueados deve falhar")
    void cancelarEmEstadosBloqueados() {
        IllegalStateException e1 = assertThrows(IllegalStateException.class, () -> pedidoComEstado(EstadoPedido.A_PREPARAR).cancelar());
        IllegalStateException e2 = assertThrows(IllegalStateException.class, () -> pedidoComEstado(EstadoPedido.PRONTO_PARA_ENTREGA).cancelar());
        IllegalStateException e3 = assertThrows(IllegalStateException.class, () -> pedidoComEstado(EstadoPedido.ATRIBUIDO_ENTREGADOR).cancelar());
        IllegalStateException e4 = assertThrows(IllegalStateException.class, () -> pedidoComEstado(EstadoPedido.ENTREGUE).cancelar());

        assertEquals("Não é possível cancelar um pedido que já está em preparo ou a caminho.", e1.getMessage());
        assertEquals("Não é possível cancelar um pedido que já está em preparo ou a caminho.", e2.getMessage());
        assertEquals("Não é possível cancelar um pedido que já está em preparo ou a caminho.", e3.getMessage());
        assertEquals("Não é possível cancelar um pedido que já está em preparo ou a caminho.", e4.getMessage());
    }

    private Pedido pedidoComEstado(EstadoPedido estado) {
        Pedido pedido = new Pedido(1L, 1L, 2L);
        pedido.setEstado(estado);
        return pedido;
    }
}
