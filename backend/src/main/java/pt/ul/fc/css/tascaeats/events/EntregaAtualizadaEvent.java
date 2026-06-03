package pt.ul.fc.css.tascaeats.events;

public record EntregaAtualizadaEvent(
        Long pedidoId,
        Long entregadorId,
        String estadoPedido,
        String estadoEntrega
) {}
