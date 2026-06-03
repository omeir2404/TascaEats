package pt.ul.fc.css.delivery.events;

public record DeliveryCommandEvent(
        Long pedidoId,
        String action
) {}
