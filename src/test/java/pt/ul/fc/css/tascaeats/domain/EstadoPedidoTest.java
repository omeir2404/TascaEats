package pt.ul.fc.css.tascaeats.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;

public class EstadoPedidoTest {

    @Test
    @DisplayName("EstadoPedido deve conter os valores esperados")
    void deveConterOsValoresEsperados() {
        assertArrayEquals(
                new EstadoPedido[]{
                        EstadoPedido.CRIADO,
                        EstadoPedido.PAGO,
                        EstadoPedido.A_PREPARAR,
                        EstadoPedido.PRONTO_PARA_ENTREGA,
                        EstadoPedido.CANCELADO,
                        EstadoPedido.ATRIBUIDO_ENTREGADOR,
                        EstadoPedido.ENTREGUE
                },
                EstadoPedido.values()
        );
    }
}
