package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class WebRestauranteController {

    private final RestTemplate restTemplate;

    @Value("${BACKEND_API_URL:http://localhost:8080}")
    private String backendUrl;

    public WebRestauranteController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // --- TEMPORARY MOCK DATA GENERATOR ---
    private List<Map<String, Object>> getMockRestaurantes() {
        return List.of(
            Map.of("id", 1, "nome", "Tasca do Zé", "tipoCozinha", "PORTUGUESA", "estado", "ABERTO", "numeroAvaliacoes", 150),
            Map.of("id", 2, "nome", "Bella Napoli", "tipoCozinha", "ITALIANA", "estado", "FECHADO", "numeroAvaliacoes", 85),
            Map.of("id", 3, "nome", "Sushi Zen", "tipoCozinha", "JAPONESA", "estado", "ABERTO", "numeroAvaliacoes", 320),
            Map.of("id", 4, "nome", "Café Central", "tipoCozinha", "PORTUGUESA", "estado", "ABERTO", "numeroAvaliacoes", 12)
        );
    }

    // --- TEMPORARY MOCK PRODUCTS GENERATOR ---
    private List<Map<String, Object>> getMockProdutos(Long restauranteId) {
        if (restauranteId == 1) { // Tasca do Zé
            return List.of(
                Map.of(
                    "id", 1, "nome", "Bacalhau à Brás", "preco", 12.50, "categoria", "MAIN_COURSE", "disponivel", true,
                    "descricao", "Traditional Portuguese dish made with shredded cod, onions, and scrambled eggs.", "alergenios", "Fish, Eggs"
                ),
                Map.of(
                    "id", 2, "nome", "Super Bock 33cl", "preco", 2.00, "categoria", "DRINK", "disponivel", true,
                    "descricao", "Refreshing cold Portuguese beer.", "alergenios", "Gluten"
                ),
                Map.of(
                    "id", 3, "nome", "Pastel de Nata", "preco", 1.50, "categoria", "DESSERT", "disponivel", false,
                    "descricao", "Famous Portuguese egg custard tart pastry.", "alergenios", "Eggs, Milk, Gluten"
                )
            );
        } else if (restauranteId == 3) { // Sushi Zen
            return List.of(
                Map.of(
                    "id", 4, "nome", "Combinado Salmon 15 peças", "preco", 22.00, "categoria", "MAIN_COURSE", "disponivel", true,
                    "descricao", "Chef's selection of 15 premium fresh salmon pieces.", "alergenios", "Fish"
                ),
                Map.of(
                    "id", 5, "nome", "Chá Verde", "preco", 3.00, "categoria", "DRINK", "disponivel", true,
                    "descricao", "Authentic Japanese hot green tea.", "alergenios", "None"
                )
            );
        }
        return List.of(); // Return empty menu for other restaurants
    }

    // 1. Default page: Loads all mock restaurants
    @GetMapping("/restaurantes")
    public String listarRestaurantes(Model model) {
        model.addAttribute("restaurantes", getMockRestaurantes());
        return "restaurantes";
    }

    // 2. Search page: Advanced filters
    @GetMapping("/restaurantes/pesquisa")
    public String pesquisarRestaurantes(
            @RequestParam(required = false) String nome,
            Model model) {
        model.addAttribute("restaurantes", getMockRestaurantes());
        return "pesquisa";
    }

    // 3. Live Search API for JavaScript Dropdown
    @GetMapping("/api/web/live-search")
    @ResponseBody
    public ResponseEntity<?> liveSearch(@RequestParam String nome) {
        List<Map<String, Object>> filtrados = getMockRestaurantes().stream()
                .filter(r -> r.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
                .toList();
        return ResponseEntity.ok(filtrados);
    }

    @GetMapping("/restaurantes/{id}/produtos")
    public String listarProdutos(@PathVariable Long id, Model model) {
        model.addAttribute("produtos", getMockProdutos(id));
        model.addAttribute("restauranteId", id);
        model.addAttribute("restauranteNome", "Mocked Restaurant #" + id);
        return "produtos";
    }

    @GetMapping("/restaurantes/{id}/produtos/search")
    public String pesquisarProdutos(
            @PathVariable Long id,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Double precoMax,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false, defaultValue = "false") Boolean disponivel,
            @RequestParam(required = false) Double popularidade,
            Model model) {

        List<Map<String, Object>> filtrados = getMockProdutos(id).stream()
            .filter(p -> nome == null || nome.isBlank() || p.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
            .filter(p -> precoMax == null || ((Double) p.get("preco")) <= precoMax)
            .filter(p -> categoria == null || categoria.isBlank() || p.get("categoria").toString().equalsIgnoreCase(categoria))
            .filter(p -> !disponivel || (Boolean) p.get("disponivel") == true)
            .toList();

        model.addAttribute("produtos", filtrados);
        model.addAttribute("restauranteId", id);
        model.addAttribute("restauranteNome", id == 1 ? "Tasca do Zé" : (id == 3 ? "Sushi Zen" : "Mocked Restaurant"));
        model.addAttribute("nome", nome);
        model.addAttribute("precoMax", precoMax);
        model.addAttribute("categoria", categoria);
        model.addAttribute("disponivel", disponivel);
        model.addAttribute("popularidade", popularidade);
        return "produtos";
    }
}
