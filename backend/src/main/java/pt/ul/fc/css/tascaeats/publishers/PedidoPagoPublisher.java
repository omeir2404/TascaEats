package pt.ul.fc.css.tascaeats.publishers;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import pt.ul.fc.css.tascaeats.events.PedidoPagoEvent;

@Service
public class PedidoPagoPublisher {

    private final KafkaTemplate<String, PedidoPagoEvent> kafkaTemplate;

    public PedidoPagoPublisher(KafkaTemplate<String, PedidoPagoEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(PedidoPagoEvent event) {
        kafkaTemplate.send("pedido-pago-topic", String.valueOf(event.pedidoId()), event);
    }
}
