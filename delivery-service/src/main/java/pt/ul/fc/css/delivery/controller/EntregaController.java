// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.delivery.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.css.delivery.domain.entrega.Entrega;
import pt.ul.fc.css.delivery.domain.entregador.Entregador;
import pt.ul.fc.css.delivery.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregaDTO.AtualizarEstadoEntregaDTO;
import pt.ul.fc.css.delivery.dto.entregaDTO.EntregaResumoDTO;
import pt.ul.fc.css.delivery.service.EntregaService;

@RestController
@RequestMapping
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @PostMapping("/entregas/atribuir")
    public ResponseEntity<Object> atribuirEntregador(@RequestBody AtribuirEntregadorDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(toResumoDTO(entregaService.atribuirEntregador(dto)));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/entregas/pedido/{pedidoId}/recolha")
    public ResponseEntity<Object> registarRecolha(@PathVariable Long pedidoId) {
        try {
            return ResponseEntity.ok(toResumoDTO(entregaService.registarRecolha(pedidoId)));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/entregas/pedido/{pedidoId}/iniciar-preparacao")
    public ResponseEntity<Object> iniciarPreparacao(@PathVariable Long pedidoId) {
        try {
            entregaService.iniciarPreparacao(pedidoId);
            return ResponseEntity.ok(Map.of(
                    "message", "Preparacao iniciada.",
                    "pedidoId", pedidoId,
                    "estado", "A_PREPARAR"));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/entregas/pedido/{pedidoId}/marcar-pronto")
    public ResponseEntity<Object> marcarPronto(@PathVariable Long pedidoId) {
        try {
            Optional<Entrega> entrega = entregaService.marcarPronto(pedidoId);
            return entrega.<ResponseEntity<Object>>map(value -> ResponseEntity.ok(toResumoDTO(value)))
                    .orElseGet(() -> ResponseEntity.ok(Map.of(
                            "message", "Pedido marcado como pronto para entrega.",
                            "pedidoId", pedidoId,
                            "estado", "PRONTO_PARA_ENTREGA")));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/entregas/pedido/{pedidoId}/entrega-final")
    public ResponseEntity<Object> registarEntregaFinal(@PathVariable Long pedidoId) {
        try {
            return ResponseEntity.ok(toResumoDTO(entregaService.registarEntregaFinal(pedidoId)));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/entregas/pedido/{pedidoId}/estado")
    public ResponseEntity<Object> atualizarEstadoEntrega(@PathVariable Long pedidoId,
                                                         @RequestBody AtualizarEstadoEntregaDTO dto) {
        try {
            return ResponseEntity.ok(toResumoDTO(entregaService.atualizarEstadoEntrega(pedidoId, dto)));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/entregas/pedido/{pedidoId}")
    public ResponseEntity<Object> obterEntregaPorPedido(@PathVariable Long pedidoId) {
        try {
            Optional<Entrega> entrega = entregaService.obterEntregaPorPedido(pedidoId);
            return entrega.<ResponseEntity<Object>>map(value -> ResponseEntity.ok(toResumoDTO(value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Entrega nao encontrada para pedido: " + pedidoId)));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        }
    }

    @GetMapping("/entregas/em-curso")
    public ResponseEntity<List<EntregaResumoDTO>> listarEntregasEmCurso() {
        return ResponseEntity.ok(entregaService.listarEntregasEmCurso().stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList()));
    }



    private ResponseEntity<Object> mapArgumentError(IllegalArgumentException e) {
        if (isNotFoundMessage(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    private boolean isNotFoundMessage(String message) {
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("nao encontrado") || normalized.contains("nao encontrada");
    }

    private EntregaResumoDTO toResumoDTO(Entrega entrega) {
        Entregador entregador = entregaService.obterEntregadorPorId(entrega.getEntregadorId()).orElse(null);
        return new EntregaResumoDTO(
                entrega.getId(),
                entrega.getPedidoId(),
                entrega.getEntregadorId(),
                entregador == null ? null : entregador.getNome(),
                entregador == null ? null : entregador.getEmail(),
                entrega.getEstado(),
                entrega.getHoraRecolha(),
                entrega.getHoraEntrega(),
                entrega.getEnderecoEntrega());
    }

}
