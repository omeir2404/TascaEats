package pt.ul.fc.css.delivery.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoPagoConsumer {

    @KafkaListener(topics = "pedido-pago-topic", groupId = "delivery-service")
    public void consumir(PedidoPagoEvent event) {
        // O entregador passa a ser atribuido quando o pedido fica pronto para entrega.
    }
}
