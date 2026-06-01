package pt.ul.fc.css.delivery.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import pt.ul.fc.css.delivery.service.EntregaService;

@Component
public class DeliveryCommandConsumer {

    private final EntregaService entregaService;

    public DeliveryCommandConsumer(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @KafkaListener(
            topics = "delivery-command-topic",
            groupId = "delivery-service-commands",
            properties = {
                    "spring.json.value.default.type=pt.ul.fc.css.delivery.events.DeliveryCommandEvent"
            })
    public void consumir(DeliveryCommandEvent event) {
        if (event == null || event.pedidoId() == null || event.action() == null) {
            return;
        }
        switch (event.action()) {
            case "INICIAR_PREPARACAO" -> entregaService.iniciarPreparacao(event.pedidoId());
            case "MARCAR_PRONTO" -> entregaService.marcarPronto(event.pedidoId());
            case "ATRIBUIR_ENTREGADOR" -> entregaService.atribuirEntregador(event.pedidoId());
            case "REGISTAR_RECOLHA" -> entregaService.registarRecolha(event.pedidoId());
            case "ENTREGA_FINAL" -> entregaService.registarEntregaFinal(event.pedidoId());
            default -> throw new IllegalArgumentException("Comando de entrega invalido: " + event.action());
        }
    }
}
