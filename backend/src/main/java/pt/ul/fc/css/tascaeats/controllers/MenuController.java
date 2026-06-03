package pt.ul.fc.css.tascaeats.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pt.ul.fc.css.tascaeats.domain.menu.Menu;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.dto.menuDTO.MenuDTO;
import pt.ul.fc.css.tascaeats.service.menuService.MenuService;

@RestController
@RequestMapping("/api")
public class MenuController {

    private static final String ERROR_FIELD = "error";

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menus")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listarMenus(@RequestParam(required = false) String nome) {
        List<Menu> menus = nome == null || nome.isBlank()
                ? menuService.listarMenus()
                : menuService.procurarMenuPorNome(nome);
        return ResponseEntity.ok(menus.stream().map(this::resumoMenu).toList());
    }

    @GetMapping("/menus/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> obterMenu(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(resumoMenu(menuService.obterMenuPorId(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    @PostMapping("/menus")
    @Transactional
    public ResponseEntity<Object> criarMenu(@Valid @RequestBody MenuDTO dto) {
        try {
            Menu criado = menuService.criarMenu(dto.getNome(), dto.getProdutosIds());
            return ResponseEntity.status(HttpStatus.CREATED).body(resumoMenu(criado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    @PutMapping("/menus/{id}")
    @Transactional
    public ResponseEntity<Object> atualizarMenu(@PathVariable Long id, @RequestBody MenuDTO dto) {
        try {
            Menu atualizado = menuService.atualizarMenu(id, dto.getNome(), dto.getProdutosIds());
            return ResponseEntity.ok(resumoMenu(atualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    @DeleteMapping("/menus/{id}")
    public ResponseEntity<Object> removerMenu(@PathVariable Long id) {
        try {
            menuService.removerMenu(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    @PutMapping("/restaurantes/{restauranteId}/menu/{menuId}")
    public ResponseEntity<Object> associarMenuAoRestaurante(@PathVariable Long restauranteId, @PathVariable Long menuId) {
        try {
            Restaurante restaurante = menuService.associarMenuAoRestaurante(restauranteId, menuId);
            Map<String, Object> response = new HashMap<>();
            response.put("id", restaurante.getId());
            response.put("nome", restaurante.getNome());
            response.put("menuId", restaurante.getMenu() == null ? null : restaurante.getMenu().getId());
            response.put("menuNome", restaurante.getMenu() == null ? "" : restaurante.getMenu().getNome());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_FIELD, e.getMessage()));
        }
    }

    private Map<String, Object> resumoMenu(Menu menu) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", menu.getId());
        response.put("nome", menu.getNome());
        response.put("numeroProdutos", menu.getProdutos().size());
        response.put("numeroProdutosAtivos", numeroProdutosAtivos(menu.getProdutos()));
        response.put("precoMedio", precoMedio(menu.getProdutos()));
        response.put("produtosIds", menu.getProdutos().stream().map(Produto::getId).toList());
        response.put("restaurantesIds", menu.getRestaurantes().stream().map(Restaurante::getId).toList());
        response.put("numeroRestaurantes", menu.getRestaurantes().size());
        response.put("restaurantesNomes", menu.getRestaurantes().stream().map(Restaurante::getNome).toList());
        response.put("restauranteId", menu.getRestaurantes().isEmpty() ? null : menu.getRestaurantes().get(0).getId());
        response.put("restauranteNome", menu.getRestaurantes().isEmpty()
                ? "Sem restaurante atribuído"
                : menu.getRestaurantes().stream().map(Restaurante::getNome).reduce((a, b) -> a + ", " + b).orElse(""));
        return response;
    }

    private long numeroProdutosAtivos(List<Produto> produtos) {
        if (produtos == null) {
            return 0L;
        }
        return produtos.stream().filter(produto -> !produto.isApagado()).count();
    }

    private double precoMedio(List<Produto> produtos) {
        if (produtos == null || produtos.isEmpty()) {
            return 0.0;
        }
        double media = produtos.stream().mapToDouble(Produto::getPreco).average().orElse(0.0);
        return Math.round(media * 100.0) / 100.0;
    }
}
