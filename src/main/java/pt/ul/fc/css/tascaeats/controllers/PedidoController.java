// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.service.pedidoService.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;
	private final ObjectMapper objectMapper;

	public PedidoController(PedidoService pedidoService, ObjectMapper objectMapper) {
		this.pedidoService = pedidoService;
		this.objectMapper = objectMapper;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> criarPedido(@Valid @RequestBody CriarPedidoDTO dto) {
		try {
			pedidoService.criarPedido(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body("Pedido criado com sucesso.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> criarPedidoTexto(@RequestBody String rawBody) {
		try {
			CriarPedidoDTO dto = objectMapper.readValue(rawBody, CriarPedidoDTO.class);
			pedidoService.criarPedido(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body("Pedido criado com sucesso.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payload inválido para criação de pedido.");
		}
	}

	@PatchMapping("/{pedidoId}/cancelar")
	public ResponseEntity<String> cancelarPedido(@PathVariable Long pedidoId) {
		pedidoService.cancelarPedido(pedidoId);
		return ResponseEntity.ok("Pedido cancelado com sucesso.");
	}

	@GetMapping
	public ResponseEntity<List<Pedido>> listarPedidos(
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) EstadoPedido estado) {
		List<Pedido> pedidos = pedidoService.listarPedidos(clienteId, estado);
		return ResponseEntity.ok(pedidos);
	}

}
