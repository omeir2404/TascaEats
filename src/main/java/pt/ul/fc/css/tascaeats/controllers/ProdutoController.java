package pt.ul.fc.css.tascaeats.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarDisponibilidadeProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.CriarProdutoDTO;
import pt.ul.fc.css.tascaeats.service.produtoService.ProdutoService;

@RestController
@RequestMapping("/api")
public class ProdutoController {

    private final ProdutoService produtoService;
    private static final String MSG_FIELD = "message";
    private static final String ERROR_FIELD = "error";
    private static final String PRODUTO_NOT_FOUND = "Produto não encontrado";

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    //Criar produto
    @PostMapping("/catalogos/{catalogoId}/produtos")
    public ResponseEntity<Map<String, Object>> criarProduto(@PathVariable Long catalogoId, @Valid @RequestBody CriarProdutoDTO dto) {
        try {
            Produto criado = produtoService.criarProduto(catalogoId, dto);
            Map<String, Object> response = new HashMap<>();
            response.put(MSG_FIELD, "Produto criado com sucesso");
            response.put("id", criado.getId());
            response.put("nome", sanitizeForOutput(criado.getNome()));
            response.put("preco", criado.getPreco());
            response.put("descricao", sanitizeForOutput(criado.getDescricao()));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    //Atualizar produto
    @PutMapping("/produtos/{id}")
    public ResponseEntity<Object> atualizarProduto(@PathVariable Long id, @Valid @RequestBody AtualizarProdutoDTO dto) {
        try {
            Produto atualizado = produtoService.atualizarProduto(id, dto);
            return respondWithProduto(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, PRODUTO_NOT_FOUND));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    //Atualizar disponibilidade
    @PatchMapping("/produtos/{id}/disponibilidade")
    public ResponseEntity<Object> atualizarDisponibilidade(@PathVariable Long id, @Valid @RequestBody AtualizarDisponibilidadeProdutoDTO dto) {
        try {
            Produto atualizado = produtoService.atualizarDisponibilidade(id, dto);
            return respondWithProduto(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_FIELD, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    //Obter produto
    @GetMapping("/produtos/{id}")
    public ResponseEntity<Object> obterProduto(@PathVariable Long id) {
        try {
            Produto produto = produtoService.obterProdutoPorId(id);
            return respondWithProduto(produto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, PRODUTO_NOT_FOUND));
        }
    }

    //Listar produtos
    @GetMapping("/produtos")
    public ResponseEntity<List<Produto>> listarProdutos(@RequestParam(required = false) String nome,
            @RequestParam(required = false) Long restauranteId,
            @RequestParam(required = false, defaultValue = "false") boolean ativos,
            @RequestParam(required = false, defaultValue = "false") boolean disponiveis) {

        if (restauranteId != null) {
            return ResponseEntity.ok(produtoService.listarProdutosPorRestaurante(restauranteId));
        }
        if (nome != null && !nome.isBlank()) {
            return ResponseEntity.ok(produtoService.procurarProdutoPorNome(nome));
        }
        if (disponiveis) {
            return ResponseEntity.ok(produtoService.listarProdutosDisponiveis());
        }
        if (ativos) {
            return ResponseEntity.ok(produtoService.listarProdutosAtivos());
        }

        return ResponseEntity.ok(produtoService.listarProdutos());
    }

    //Apagar produtos
    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Object> apagarProduto(@PathVariable Long id) {
        try {
            produtoService.apagarProduto(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, PRODUTO_NOT_FOUND));
        }
    }


    //Funções Auxiliares
    private String sanitizeForOutput(String value) {
        if (value == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(value);
    }

    private ResponseEntity<Object> respondWithProduto(Produto produto) {
        if (produto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, PRODUTO_NOT_FOUND));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", produto.getId());
        response.put("nome", sanitizeForOutput(produto.getNome()));
        response.put("preco", produto.getPreco());
        response.put("descricao", sanitizeForOutput(produto.getDescricao()));
        response.put("disponivel", produto.isDisponivel());
        response.put("apagado", produto.isApagado());
        return ResponseEntity.ok(response);
    }
}