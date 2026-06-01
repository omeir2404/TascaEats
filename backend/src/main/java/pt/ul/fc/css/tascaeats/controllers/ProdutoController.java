// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @PostMapping("/restaurantes/{restauranteId}/produtos")
    public ResponseEntity<Map<String, Object>> criarProduto(@PathVariable Long restauranteId, @Valid @RequestBody CriarProdutoDTO dto) {
        try {
            Produto criado = produtoService.criarProduto(restauranteId, dto);
            Map<String, Object> response = new HashMap<>();
            response.put(MSG_FIELD, "Produto criado com sucesso");
            response.put("id", criado.getId());
            response.put("nome", criado.getNome());
            response.put("preco", criado.getPreco());
            response.put("descricao", criado.getDescricao());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    @PostMapping("/menus/{menuId}/produtos")
    public ResponseEntity<Map<String, Object>> criarProdutoNoMenu(@PathVariable Long menuId, @Valid @RequestBody CriarProdutoDTO dto) {
        try {
            Produto criado = produtoService.criarProdutoNoMenu(menuId, dto);
            Map<String, Object> response = new HashMap<>();
            response.put(MSG_FIELD, "Produto criado com sucesso");
            response.put("id", criado.getId());
            response.put("nome", criado.getNome());
            response.put("preco", criado.getPreco());
            response.put("descricao", criado.getDescricao());
            response.put("menuId", criado.getMenu() == null ? null : criado.getMenu().getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
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
    public ResponseEntity<List<Map<String, Object>>> listarProdutos(@RequestParam(required = false) String nome,
            @RequestParam(required = false) Long restauranteId,
            @RequestParam(required = false) Long menuId,
            @RequestParam(required = false, defaultValue = "false") boolean ativos,
            @RequestParam(required = false, defaultValue = "false") boolean disponiveis) {

        List<Produto> produtos;

        if (restauranteId != null) {
            produtos = produtoService.listarProdutosPorRestaurante(restauranteId);
        } else if (menuId != null) {
            produtos = produtoService.listarProdutosPorMenu(menuId);
        } else if (nome != null && !nome.isBlank()) {
            produtos = produtoService.procurarProdutoPorNome(nome);
        } else if (disponiveis) {
            produtos = produtoService.listarProdutosDisponiveis();
        } else if (ativos) {
            produtos = produtoService.listarProdutosAtivos();
        } else {
            produtos = produtoService.listarProdutos();
        }

        List<Map<String, Object>> response = produtos.stream()
                .map(this::resumoProduto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Pesquisar produtos com filtros
    @GetMapping("/produtos/pesquisa")
    public ResponseEntity<List<Map<String, Object>>> pesquisarProdutos(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Double preco,
            @RequestParam(required = false) Long menuId,
            @RequestParam(required = false) Boolean disponivel,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim) {

        List<Produto> produtos = produtoService.pesquisarProdutos(nome, preco, menuId, disponivel, dataInicio, dataFim);

        List<Map<String, Object>> response = produtos.stream()
                .map(this::resumoProduto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    //Apagar produtos
    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Object> apagarProduto(@PathVariable Long id) {
        try {
            produtoService.apagarProduto(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_FIELD, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, PRODUTO_NOT_FOUND));
        }
    }


    //Funções Auxiliares
    private ResponseEntity<Object> respondWithProduto(Produto produto) {
        if (produto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, PRODUTO_NOT_FOUND));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", produto.getId());
        response.put("nome", produto.getNome());
        response.put("preco", produto.getPreco());
        response.put("descricao", produto.getDescricao());
        response.put("disponivel", produto.isDisponivel());
        response.put("apagado", produto.isApagado());
        if (produto.getRestaurante() != null) {
            response.put("restauranteId", produto.getRestaurante().getId());
        }
        if (produto.getMenu() != null) {
            response.put("menuId", produto.getMenu().getId());
            response.put("menuNome", produto.getMenu().getNome());
        }
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> resumoProduto(Produto produto) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", produto.getId());
        response.put("nome", produto.getNome());
        response.put("preco", produto.getPreco());
        response.put("descricao", produto.getDescricao());
        response.put("disponivel", produto.isDisponivel());
        response.put("apagado", produto.isApagado());

        if (produto.getRestaurante() != null) {
            response.put("restauranteId", produto.getRestaurante().getId());
        }
        if (produto.getMenu() != null) {
            response.put("menuId", produto.getMenu().getId());
            response.put("menuNome", produto.getMenu().getNome());
        }

        return response;
    }
}
