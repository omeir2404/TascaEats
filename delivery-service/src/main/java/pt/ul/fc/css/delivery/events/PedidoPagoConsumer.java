package pt.ul.fc.css.delivery.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PedidoPagoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoPagoConsumer.class);

    @KafkaListener(topics = "pedido-pago-topic", groupId = "delivery-service")
    public void consumir(PedidoPagoEvent event) {
        if (event == null || event.pedidoId() == null) {
            log.warn("Evento pedido-pago invalido recebido pelo delivery-service.");
            return;
        }

        log.info("Pagamento confirmado para o pedido {}. A entrega aguarda atribuicao manual.",
                event.pedidoId());
    }
}
