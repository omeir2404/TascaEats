package pt.ul.fc.css.delivery.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ul.fc.css.delivery.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.delivery.service.EntregaService;

@Component
public class PedidoPagoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoPagoConsumer.class);

    private final EntregaService entregaService;

    public PedidoPagoConsumer(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @KafkaListener(topics = "pedido-pago-topic", groupId = "delivery-service")
    public void consumir(PedidoPagoEvent event) {
        if (event == null || event.pedidoId() == null) {
            log.warn("Evento pedido-pago invalido recebido pelo delivery-service.");
            return;
        }

        try {
            entregaService.atribuirEntregador(
                    new AtribuirEntregadorDTO(event.pedidoId(), event.enderecoEntrega()));
            log.info("Entrega atribuida automaticamente para o pedido {}", event.pedidoId());
        } catch (IllegalStateException e) {
            log.warn("Nao foi possivel atribuir entregador ao pedido {}: {}", event.pedidoId(), e.getMessage());
        }
    }
}
