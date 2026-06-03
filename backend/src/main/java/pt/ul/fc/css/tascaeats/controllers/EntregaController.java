// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.publishers.DeliveryCommandPublisher;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;

@RestController
@RequestMapping({"/entregas", "/api/entregas"})
public class EntregaController {

    private final IPedidoRepository pedidoRepository;
    private final DeliveryCommandPublisher deliveryCommandPublisher;

    public EntregaController(IPedidoRepository pedidoRepository,
                             DeliveryCommandPublisher deliveryCommandPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.deliveryCommandPublisher = deliveryCommandPublisher;
    }

    @GetMapping("/pedido/{pedidoId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> obterEntregaPorPedido(@PathVariable Long pedidoId) {
        if (pedidoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID do pedido invalido."));
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElse(null);
        if (pedido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Pedido nao encontrado: " + pedidoId));
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("pedidoId", pedido.getId());
        resposta.put("clienteId", pedido.getClienteId());
        resposta.put("entregadorId", pedido.getEntregadorId());
        resposta.put("estadoPedido", pedido.getEstado() == null ? null : pedido.getEstado().name());
        resposta.put("estadoEntrega", pedido.getEstadoEntregaReadOnly());
        resposta.put("entregadorNome", null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/pedido/{pedidoId}/iniciar-preparacao")
    public ResponseEntity<Object> iniciarPreparacao(@PathVariable Long pedidoId) {
        return publicarComando(pedidoId, "INICIAR_PREPARACAO");
    }

    @PatchMapping("/pedido/{pedidoId}/iniciar-preparacao")
    public ResponseEntity<Object> iniciarPreparacaoPatch(@PathVariable Long pedidoId) {
        return iniciarPreparacao(pedidoId);
    }

    @PostMapping("/pedido/{pedidoId}/marcar-pronto")
    public ResponseEntity<Object> marcarPronto(@PathVariable Long pedidoId) {
        return publicarComando(pedidoId, "MARCAR_PRONTO");
    }

    @PatchMapping("/pedido/{pedidoId}/marcar-pronto")
    public ResponseEntity<Object> marcarProntoPatch(@PathVariable Long pedidoId) {
        return marcarPronto(pedidoId);
    }

    @PostMapping("/pedido/{pedidoId}/atribuir-entregador")
    public ResponseEntity<Object> atribuirEntregador(@PathVariable Long pedidoId) {
        return publicarComando(pedidoId, "ATRIBUIR_ENTREGADOR");
    }

    @PatchMapping("/pedido/{pedidoId}/atribuir-entregador")
    public ResponseEntity<Object> atribuirEntregadorPatch(@PathVariable Long pedidoId) {
        return atribuirEntregador(pedidoId);
    }

    @PostMapping("/pedido/{pedidoId}/recolha")
    public ResponseEntity<Object> registarRecolha(@PathVariable Long pedidoId) {
        return publicarComando(pedidoId, "REGISTAR_RECOLHA");
    }

    @PatchMapping("/pedido/{pedidoId}/recolha")
    public ResponseEntity<Object> registarRecolhaPatch(@PathVariable Long pedidoId) {
        return registarRecolha(pedidoId);
    }

    @PostMapping("/pedido/{pedidoId}/entrega-final")
    public ResponseEntity<Object> entregaFinal(@PathVariable Long pedidoId) {
        return publicarComando(pedidoId, "ENTREGA_FINAL");
    }

    @PatchMapping("/pedido/{pedidoId}/entrega-final")
    public ResponseEntity<Object> entregaFinalPatch(@PathVariable Long pedidoId) {
        return entregaFinal(pedidoId);
    }

    private ResponseEntity<Object> publicarComando(Long pedidoId, String action) {
        if (pedidoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID do pedido invalido."));
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElse(null);
        if (pedido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Pedido nao encontrado: " + pedidoId));
        }

        deliveryCommandPublisher.publicar(pedidoId, action);
        return ResponseEntity.accepted().body(Map.of(
                "message", "Comando de entrega publicado.",
                "pedidoId", pedidoId,
                "action", action));
    }
}
