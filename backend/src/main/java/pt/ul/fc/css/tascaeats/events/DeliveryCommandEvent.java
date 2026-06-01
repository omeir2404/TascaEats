package pt.ul.fc.css.tascaeats.events;

public record DeliveryCommandEvent(
        Long pedidoId,
        String action
) {}
