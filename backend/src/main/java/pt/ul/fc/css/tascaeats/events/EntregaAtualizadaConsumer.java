// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

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

    @KafkaListener(
            topics = "entrega-atualizada-topic", 
            groupId = "tascaeats-backend",
            properties = {
                    "spring.json.value.default.type=pt.ul.fc.css.tascaeats.events.EntregaAtualizadaEvent",
                    "spring.json.use.type.headers=false" // <-- ADD THIS LINE
            })
    public void consumir(EntregaAtualizadaEvent event) {
        pedidoService.aplicarAtualizacaoEntrega(
                event.pedidoId(),
                event.entregadorId(),
                event.estadoPedido(),
                event.estadoEntrega()
        );
    }
}