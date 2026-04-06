package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.domain.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.Pedido;
import pt.ul.fc.css.tascaeats.dto.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.service.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@PostMapping
	public ResponseEntity<String> criarPedido(@Valid @RequestBody CriarPedidoDTO dto) {
		pedidoService.criarPedido(dto);
		return ResponseEntity.ok("Pedido criado com sucesso.");
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
