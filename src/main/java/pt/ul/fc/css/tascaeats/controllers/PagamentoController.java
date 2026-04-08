package pt.ul.fc.css.tascaeats.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.css.tascaeats.dto.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.service.PagamentoService;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<String> pagarPedido(@RequestBody ProcessarPagamentoDTO dto) {
        pagamentoService.processarPagamento(dto);
        return ResponseEntity.ok("Pagamento processado com sucesso.");
    }
}