package pt.ul.fc.css.delivery.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import pt.ul.fc.css.delivery.domain.entrega.Entrega;

@Service
public class EntregaAtualizadaPublisher {

    private final KafkaTemplate<String, EntregaAtualizadaEvent> kafkaTemplate;

    public EntregaAtualizadaPublisher(KafkaTemplate<String, EntregaAtualizadaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(Entrega entrega, String estadoPedido) {
        if (entrega == null) {
            return;
        }
        EntregaAtualizadaEvent event = new EntregaAtualizadaEvent(
                entrega.getPedidoId(),
                entrega.getEntregadorId(),
                estadoPedido,
                entrega.getEstado() == null ? null : entrega.getEstado().name()
        );
        kafkaTemplate.send("entrega-atualizada-topic", String.valueOf(event.pedidoId()), event);
    }

    public void publicarPedido(Long pedidoId, String estadoPedido) {
        if (pedidoId == null) {
            return;
        }
        EntregaAtualizadaEvent event = new EntregaAtualizadaEvent(pedidoId, null, estadoPedido, null);
        kafkaTemplate.send("entrega-atualizada-topic", String.valueOf(pedidoId), event);
    }
}
