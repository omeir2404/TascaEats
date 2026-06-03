// ##############################################################
// 					  TASCAEATS						#
// 					CSS GRUPO: 25						#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.delivery.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.css.delivery.domain.entregador.Entregador;
import pt.ul.fc.css.delivery.dto.entregadorDTO.AtualizarEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregadorDTO.CriarEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregadorDTO.EntregadorDTO;
import pt.ul.fc.css.delivery.service.EntregaService;

@RestController
@RequestMapping("/entregadores")
public class EntregadorController {

    private final EntregaService entregaService;

    public EntregadorController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @PostMapping
    public ResponseEntity<Object> registarEntregador(@RequestBody CriarEntregadorDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(entregaService.registarEntregador(dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{entregadorId}")
    public ResponseEntity<Object> atualizarEntregador(@PathVariable Long entregadorId,
                                                      @RequestBody AtualizarEntregadorDTO dto) {
        try {
            return ResponseEntity.ok(toDTO(entregaService.atualizarEntregador(entregadorId, dto)));
        } catch (IllegalArgumentException e) {
            return mapArgumentError(e);
        }
    }

    @GetMapping
    public ResponseEntity<List<EntregadorDTO>> listarEntregadores() {
        return ResponseEntity.ok(entregaService.listarEntregadores().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<EntregadorDTO>> listarEntregadoresDisponiveis() {
        return ResponseEntity.ok(entregaService.listarEntregadoresDisponiveis().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<EntregadorDTO>> buscarEntregadores(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean available) {
        return ResponseEntity.ok(entregaService.buscarEntregadores(nome, available).stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{entregadorId}")
    public ResponseEntity<Object> obterEntregadorPorId(@PathVariable Long entregadorId) {
        return entregaService.obterEntregadorPorId(entregadorId)
                .<ResponseEntity<Object>>map(entregador -> ResponseEntity.ok(toDTO(entregador)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entregador nao encontrado: " + entregadorId)));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Object> obterEntregadorPorEmail(@PathVariable String email) {
        return entregaService.obterEntregadorPorEmail(email)
                .<ResponseEntity<Object>>map(entregador -> ResponseEntity.ok(toDTO(entregador)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entregador nao encontrado: " + email)));
    }

    private ResponseEntity<Object> mapArgumentError(IllegalArgumentException e) {
        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("nao encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    private EntregadorDTO toDTO(Entregador entregador) {
        return new EntregadorDTO(
                entregador.getId(),
                entregador.getNome(),
                entregador.getEmail(),
                entregador.isAvailable());
    }
}
