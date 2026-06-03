// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.AtualizarEstadoPedidoDTO;
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
	public ResponseEntity<Object> criarPedido(@Valid @RequestBody CriarPedidoDTO dto) {
		try {
			pedidoService.criarPedido(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body("Pedido criado com sucesso.");
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Object> criarPedidoTexto(@RequestBody String rawBody) {
		try {
			CriarPedidoDTO dto = objectMapper.readValue(rawBody, CriarPedidoDTO.class);
			pedidoService.criarPedido(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body("Pedido criado com sucesso.");
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (JsonProcessingException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Payload inválido para criação de pedido."));
		}
	}

	@PostMapping("/{pedidoId}/cancelar")
	public ResponseEntity<Object> cancelarPedido(@PathVariable Long pedidoId) {
		return cancelarPedidoInterno(pedidoId);
	}

	@PatchMapping("/{pedidoId}/cancelar")
	public ResponseEntity<Object> cancelarPedidoPatch(@PathVariable Long pedidoId) {
		return cancelarPedidoInterno(pedidoId);
	}

	private ResponseEntity<Object> cancelarPedidoInterno(Long pedidoId) {
		try {
			pedidoService.cancelarPedido(pedidoId);
			return ResponseEntity.ok("Pedido cancelado com sucesso.");
		} catch (IllegalArgumentException e) {
			return mapArgumentError(e);
		}
	}

	@GetMapping
	@Transactional(readOnly = true)
	public ResponseEntity<Object> listarPedidos(
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) EstadoPedido estado) {
		try {
			List<Pedido> pedidos = pedidoService.listarPedidos(clienteId, estado);
			List<Map<String, Object>> resposta = new ArrayList<>();

			for (Pedido pedido : pedidos) {
				resposta.add(respostaPedido(pedido));
			}

			return ResponseEntity.ok(resposta);
		} catch (IllegalArgumentException e) {
			return mapArgumentError(e);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{pedidoId}")
	@Transactional(readOnly = true)
	public ResponseEntity<Object> obterPedido(@PathVariable Long pedidoId) {
		try {
			return ResponseEntity.ok(respostaPedido(pedidoService.obterPedido(pedidoId)));
		} catch (IllegalArgumentException e) {
			return mapArgumentError(e);
		}
	}

	private Map<String, Object> respostaPedido(Pedido pedido) {
		Map<String, Object> item = new HashMap<>();
		item.put("id", pedido.getId());
		item.put("data", pedido.getData());
		item.put("hora", pedido.getHora());
		item.put("valorTotal", pedido.getValorTotal());
		item.put("estado", pedido.getEstado());
		item.put("clienteId", pedido.getClienteId());
		item.put("entregadorId", pedido.getEntregadorId());
		item.put("estadoEntregaReadOnly", pedido.getEstadoEntregaReadOnly());
		item.put("moradaEntrega", pedido.getMoradaEntrega());
		item.put("enderecoEntrega", pedido.getEnderecoEntregaFormatado());
		item.put("restaurantesIds", pedido.getRestaurantesIds());
		item.put("produtosIds", pedido.getProdutosIds());
		item.put("tipoPagamentoSolicitado", pedido.getTipoPagamentoSolicitado());
		item.put("detalhesPagamentoSolicitado", pedido.getDetalhesPagamentoSolicitado());
		return item;
	}

	private ResponseEntity<Object> mapArgumentError(IllegalArgumentException e) {
		if (isNotFoundMessage(e.getMessage())) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
		}
		return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	}

	@PatchMapping("/{pedidoId}/estado")
	public ResponseEntity<Object> atualizarEstadoPedido(@PathVariable Long pedidoId,
														 @RequestBody AtualizarEstadoPedidoDTO dto) {
		try {
			pedidoService.atualizarEstadoPedido(pedidoId, dto.getNovoEstado());
			return ResponseEntity.ok(Map.of("message", "Estado do pedido atualizado.", "estado", dto.getNovoEstado()));
		} catch (IllegalArgumentException e) {
			return mapArgumentError(e);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/{pedidoId}/estado")
	public ResponseEntity<Object> atualizarEstadoPedidoPost(@PathVariable Long pedidoId,
											 @RequestBody AtualizarEstadoPedidoDTO dto) {
		return atualizarEstadoPedido(pedidoId, dto);
	}

	private boolean isNotFoundMessage(String message) {
		if (message == null) {
			return false;
		}
		String normalized = message.toLowerCase();
		return normalized.contains("não encontrado") || normalized.contains("nao encontrado");
	}

}
