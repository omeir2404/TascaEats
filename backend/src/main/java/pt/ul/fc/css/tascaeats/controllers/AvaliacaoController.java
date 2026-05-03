// ##############################################################
// 					  TASCAEATS								#
// 					CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.dto.avaliacaoDTO.AvaliacaoDTO;
import pt.ul.fc.css.tascaeats.service.avaliacaoServices.AvaliacaoService;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<Object> criarAvaliacao(@Valid @RequestBody AvaliacaoDTO dto) {
        try {
            avaliacaoService.criarAvaliacao(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Avaliação criada com sucesso.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pedido/{pedidoId}/existe")
    public ResponseEntity<Object> pedidoJaAvaliado(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(Map.of("avaliado", avaliacaoService.pedidoJaAvaliado(pedidoId)));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Object> getAvaliacaoByPedido(@PathVariable Long pedidoId) {
        AvaliacaoDTO dto = avaliacaoService.getAvaliacaoByPedido(pedidoId);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Avaliação não encontrada"));
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/restaurante/{restauranteId}")
    public ResponseEntity<Object> getAvaliacoesByRestaurante(@PathVariable Long restauranteId) {
        return ResponseEntity.ok(avaliacaoService.getAvaliacoesRestaurante(restauranteId));
    }
}
