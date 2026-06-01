package pt.ul.fc.css.delivery.events;

public record PedidoPagoEvent(
        Long pedidoId,
        Long clienteId,
        String enderecoEntrega
) {}
