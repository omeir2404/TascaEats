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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.css.delivery.domain.entrega.Entrega;
import pt.ul.fc.css.delivery.domain.entregador.Entregador;
import pt.ul.fc.css.delivery.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregaDTO.EntregaResumoDTO;
import pt.ul.fc.css.delivery.dto.entregadorDTO.EntregadorDTO;
import pt.ul.fc.css.delivery.service.EntregaService;

@RestController
@RequestMapping
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @PostMapping("/entregadores")
    public ResponseEntity<Object> registarEntregador(@RequestBody Entregador entregador) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(toEntregadorDTO(entregaService.registarEntregador(entregador)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/entregadores/{entregadorId}")
    public ResponseEntity<Object> atualizarEntregador(@PathVariable Long entregadorId, @RequestBody Entregador entregador) {
        try {
            return ResponseEntity.ok(toEntregadorDTO(entregaService.atualizarEntregador(entregadorId, entregador)));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        }
    }

    @GetMapping("/entregadores")
    public ResponseEntity<List<EntregadorDTO>> listarEntregadores() {
        return ResponseEntity.ok(entregaService.listarEntregadores().stream()
                .map(this::toEntregadorDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/entregadores/disponiveis")
    public ResponseEntity<List<EntregadorDTO>> listarEntregadoresDisponiveis() {
        return ResponseEntity.ok(entregaService.listarEntregadoresDisponiveis().stream()
                .map(this::toEntregadorDTO)
                .collect(Collectors.toList()));
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

