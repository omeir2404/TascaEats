package pt.ul.fc.css.tascaeats.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.dto.pagamentoDTO.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.service.pagamentoServices.PagamentoService;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<?> pagarPedido(@Valid @RequestBody ProcessarPagamentoDTO dto) {
        try {
            pagamentoService.processarPagamento(dto);
            return ResponseEntity.ok("Pagamento processado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}