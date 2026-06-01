package pt.ul.fc.css.delivery.events;

public record EntregaAtualizadaEvent(
        Long pedidoId,
        Long entregadorId,
        String estadoPedido,
        String estadoEntrega
) {}
