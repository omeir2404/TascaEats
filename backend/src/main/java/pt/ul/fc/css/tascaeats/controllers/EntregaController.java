// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
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
			return mapArgumentError(e);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/pedido/{pedidoId}/iniciar-preparacao")
	public ResponseEntity<Object> iniciarPreparacao(@PathVariable Long pedidoId) {
		Pedido pedido = entregaService.iniciarPreparacao(pedidoId);
		return ResponseEntity.ok("Pedido " + pedido.getId() + " em preparação.");
	}

	@PatchMapping("/pedido/{pedidoId}/marcar-pronto")
	public ResponseEntity<Object> marcarPedidoComoPronto(@PathVariable Long pedidoId) {
		try {
			Pedido pedido = entregaService.marcarPedidoComoPronto(pedidoId);
			return ResponseEntity.ok("Pedido " + pedido.getId() + " pronto para entrega.");
		} catch (IllegalArgumentException e) {
			return mapArgumentError(e);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/pedido/{pedidoId}/recolha")
	public ResponseEntity<Object> registarRecolha(@PathVariable Long pedidoId) {
		try {
			Entrega entrega = entregaService.registarRecolha(pedidoId);
			return ResponseEntity.ok(toResumoDTO(entrega));
		} catch (IllegalArgumentException e) {
			return mapArgumentError(e);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/pedido/{pedidoId}/entrega-final")
	public ResponseEntity<Object> registarEntregaFinal(@PathVariable Long pedidoId) {
		try {
			Entrega entrega = entregaService.registarEntregaFinal(pedidoId);
			return ResponseEntity.ok(toResumoDTO(entrega));
		} catch (IllegalArgumentException e) {
			return mapArgumentError(e);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		}
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
		return normalized.contains("não encontrado") || normalized.contains("nao encontrado");
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
