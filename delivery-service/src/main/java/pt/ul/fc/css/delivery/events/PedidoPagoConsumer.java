// ##############################################################
// 					  TASCAEATS						#
// 					CSS GRUPO: 25						#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.delivery.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ul.fc.css.delivery.domain.pedido.PedidoPagoRecebido;
import pt.ul.fc.css.delivery.repository.PedidoPagoRecebidoRepository;

@Component
public class PedidoPagoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoPagoConsumer.class);
    private final PedidoPagoRecebidoRepository pedidoPagoRecebidoRepository;

    public PedidoPagoConsumer(PedidoPagoRecebidoRepository pedidoPagoRecebidoRepository) {
        this.pedidoPagoRecebidoRepository = pedidoPagoRecebidoRepository;
    }

    @KafkaListener(topics = "pedido-pago-topic", groupId = "delivery-service")
    public void consumir(PedidoPagoEvent event) {
        if (event == null || event.pedidoId() == null) {
            log.warn("Evento pedido-pago invalido recebido pelo delivery-service.");
            return;
        }

        PedidoPagoRecebido recebido = pedidoPagoRecebidoRepository.findByPedidoId(event.pedidoId())
                .map(existente -> {
                    existente.atualizarRecebimento(event.enderecoEntrega());
                    return existente;
                })
                .orElseGet(() -> new PedidoPagoRecebido(
                        event.pedidoId(),
                        event.clienteId(),
                        event.enderecoEntrega() == null || event.enderecoEntrega().isBlank()
                                ? "ENDERECO_NAO_INFORMADO"
                                : event.enderecoEntrega()));

        pedidoPagoRecebidoRepository.save(recebido);
        log.info("Pagamento confirmado para o pedido {}. Registo persistido para atribuicao de entrega.",
                event.pedidoId());
    }
}
