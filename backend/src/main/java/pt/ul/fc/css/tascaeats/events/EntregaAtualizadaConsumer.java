package pt.ul.fc.css.tascaeats.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import pt.ul.fc.css.tascaeats.service.pedidoService.PedidoService;

@Component
public class EntregaAtualizadaConsumer {

    private final PedidoService pedidoService;

    public EntregaAtualizadaConsumer(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @KafkaListener(topics = "entrega-atualizada-topic", groupId = "tascaeats-backend")
    public void consumir(EntregaAtualizadaEvent event) {
        pedidoService.aplicarAtualizacaoEntrega(
                event.pedidoId(),
                event.entregadorId(),
                event.estadoPedido(),
                event.estadoEntrega()
        );
    }
}
