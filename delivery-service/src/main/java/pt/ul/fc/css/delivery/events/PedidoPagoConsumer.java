package pt.ul.fc.css.delivery.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import pt.ul.fc.css.delivery.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.delivery.service.EntregaService;

@Component
public class PedidoPagoConsumer {

    private final EntregaService entregaService;

    public PedidoPagoConsumer(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @KafkaListener(topics = "pedido-pago-topic", groupId = "delivery-service")
    public void consumir(PedidoPagoEvent event) {
        try {
            entregaService.atribuirEntregador(
                    new AtribuirEntregadorDTO(event.pedidoId(), event.enderecoEntrega())
            );
        } catch (IllegalStateException e) {
            if (!e.getMessage().contains("ja tem uma entrega")) {
                throw e;
            }
        }
    }
}
