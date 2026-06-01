package pt.ul.fc.css.tascaeats.events;

public record PedidoPagoEvent(
        Long pedidoId,
        Long clienteId,
        String enderecoEntrega
) {}
