// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.publishers;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import pt.ul.fc.css.tascaeats.events.DeliveryCommandEvent;

@Service
public class DeliveryCommandPublisher {

    private final KafkaTemplate<String, DeliveryCommandEvent> kafkaTemplate;

    public DeliveryCommandPublisher(KafkaTemplate<String, DeliveryCommandEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(Long pedidoId, String action) {
        DeliveryCommandEvent event = new DeliveryCommandEvent(pedidoId, action);
        kafkaTemplate.send("delivery-command-topic", String.valueOf(pedidoId), event);
    }
}
