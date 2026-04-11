// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.tascaeats.dto.entregaDTO.EntregaResumoDTO;
import pt.ul.fc.css.tascaeats.service.entregaService.EntregaService;

@RestController
@RequestMapping("/entregas")
public class EntregaController {

	private final EntregaService entregaService;

	public EntregaController(EntregaService entregaService) {
		this.entregaService = entregaService;
	}

	@PostMapping("/atribuir")
	public ResponseEntity<Object> atribuirEntregador(@Valid @RequestBody AtribuirEntregadorDTO dto) {
		try {
			Entrega entrega = entregaService.atribuirEntregador(dto);
			return ResponseEntity.ok(toResumoDTO(entrega));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(409).body(java.util.Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/pedido/{pedidoId}/iniciar-preparacao")
	public ResponseEntity<String> iniciarPreparacao(@PathVariable Long pedidoId) {
		Pedido pedido = entregaService.iniciarPreparacao(pedidoId);
		return ResponseEntity.ok("Pedido " + pedido.getId() + " em preparação.");
	}

	@PatchMapping("/pedido/{pedidoId}/marcar-pronto")
	public ResponseEntity<String> marcarPedidoComoPronto(@PathVariable Long pedidoId) {
		Pedido pedido = entregaService.marcarPedidoComoPronto(pedidoId);
		return ResponseEntity.ok("Pedido " + pedido.getId() + " pronto para entrega.");
	}

	@PatchMapping("/pedido/{pedidoId}/recolha")
	public ResponseEntity<EntregaResumoDTO> registarRecolha(@PathVariable Long pedidoId) {
		Entrega entrega = entregaService.registarRecolha(pedidoId);
		return ResponseEntity.ok(toResumoDTO(entrega));
	}

	@PatchMapping("/pedido/{pedidoId}/entrega-final")
	public ResponseEntity<EntregaResumoDTO> registarEntregaFinal(@PathVariable Long pedidoId) {
		Entrega entrega = entregaService.registarEntregaFinal(pedidoId);
		return ResponseEntity.ok(toResumoDTO(entrega));
	}

	private EntregaResumoDTO toResumoDTO(Entrega entrega) {
		String entregadorNome = entrega.getEntregadorId() != null ? String.valueOf(entrega.getEntregadorId()) : null;
		return new EntregaResumoDTO(
				entrega.getId(),
				entrega.getHoraRecolha(),
				entrega.getHoraEntrega(),
				entregadorNome);
	}

}
