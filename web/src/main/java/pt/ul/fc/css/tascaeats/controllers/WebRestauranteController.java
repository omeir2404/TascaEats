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

    // #######################  APAGAR ESTE BLOCO E DESCOMENTAR O DE BAIXO ########################################################################

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
                    "id", 1, "nome", "Bitoque", "preco", 12.50, "categoria", "MAIN_COURSE", "disponivel", true,
                    "descricao", "Suculento bife grelhado no ponto, servido com ovo estrelado, batata frita crocante, arroz e salada fresca. Um clássico português cheio de sabor.", "alergenios", "Ovos, Glúten"
                ),
                Map.of(
                    "id", 2, "nome", "Super Bock 33cl", "preco", 2.00, "categoria", "DRINK", "disponivel", true,
                    "descricao", "Cerveja portuguesa gelada e refrescante.", "alergenios", "Glúten"
                ),
                Map.of(
                    "id", 3, "nome", "Pastel de Nata", "preco", 1.50, "categoria", "DESSERT", "disponivel", false,
                    "descricao", "Ícone da doçaria portuguesa, caracterizado por uma base de massa folhada estaladiça e um recheio cremoso à base de ovos, leite, açúcar, limão e canela.", "alergenios", "Glúten, ovos, leite"
                )
            );
        } else if (restauranteId == 3) { // Sushi Zen
            return List.of(
                Map.of(
                    "id", 4, "nome", "Combinado Salmon 15 peças", "preco", 22.00, "categoria", "MAIN_COURSE", "disponivel", true,
                    "descricao", "\n" +
                                "Seleção do chef com 15 peças de sushi fresco de ótima qualidade.", "alergenios", "Fish"
                ),
                Map.of(
                    "id", 5, "nome", "Chá Verde", "preco", 3.00, "categoria", "DRINK", "disponivel", true,
                    "descricao", "\n" + "Autêntico chá verde quente japonês.", "alergenios", "None"
                )
            );
        }
        return List.of(); // Return empty menu for other restaurants
    }

        // 1. Default page: Loads all mock restaurants
    @GetMapping("/restaurantes")
    public String listarRestaurantes(Model model) {
        // TEMPORARY: Bypass backend and use mock data
        model.addAttribute("restaurantes", getMockRestaurantes());
        return "restaurantes";
    }

    // 2. Search page: Advanced filters
    @GetMapping("/restaurantes/pesquisa")
    public String pesquisarRestaurantes(
            @RequestParam(required = false) String nome,
            // ... other params ...
            Model model) {

        // TEMPORARY: Bypass backend and use mock data
        // Note: For testing the UI, we just return all of them. 
        // Real filtering will happen in the backend later.
        model.addAttribute("restaurantes", getMockRestaurantes());
        return "pesquisa";
    }
// 3. Live Search API for JavaScript Dropdown
    @GetMapping("/api/web/live-search")
    @ResponseBody
    public ResponseEntity<?> liveSearch(@RequestParam String nome) {
        
        // Grab the full list of mock restaurants
        List<Map<String, Object>> todosOsRestaurantes = getMockRestaurantes();
        
        // Filter the list to only include restaurants where the name contains the search string
        List<Map<String, Object>> restaurantesFiltrados = todosOsRestaurantes.stream()
                .filter(restaurante -> restaurante.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
                .toList();

        // Return the filtered list to the frontend JS
        return ResponseEntity.ok(restaurantesFiltrados);
    }


    @GetMapping("/restaurantes/{id}/produtos")
    public String listarProdutos(@PathVariable Long id, Model model) {
        // TEMPORARY: Bypass backend and use mock products
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

        // 1. Grab the full mock menu for this specific restaurant
        List<Map<String, Object>> todosProdutos = getMockProdutos(id);

        // 2. Filter the data based on the parameters sent by the HTML form
        List<Map<String, Object>> produtosFiltrados = todosProdutos.stream()
            // Filter by Name (if provided)
            .filter(p -> nome == null || nome.isBlank() || p.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
            
            // Filter by Max Price (if provided)
            .filter(p -> precoMax == null || ((Double) p.get("preco")) <= precoMax)
            
            // Filter by Category (if provided)
            .filter(p -> categoria == null || categoria.isBlank() || p.get("categoria").toString().equalsIgnoreCase(categoria))
            
            // Filter by Availability (if checkbox is checked, only show available items)
            .filter(p -> !disponivel || (Boolean) p.get("disponivel") == true)
            
            .toList();

        // 3. Send the filtered list back to the frontend
        model.addAttribute("produtos", produtosFiltrados);
        model.addAttribute("restauranteId", id);
        model.addAttribute("restauranteNome", id == 1 ? "Tasca do Zé" : (id == 3 ? "Sushi Zen" : "Mocked Restaurant"));

        // ---> NEW: Send the active filters back to the HTML <---
        model.addAttribute("nome", nome);
        model.addAttribute("precoMax", precoMax);
        model.addAttribute("categoria", categoria);
        model.addAttribute("disponivel", disponivel);
        model.addAttribute("popularidade", popularidade);
        return "produtos";
    }
// #################################################  NAO APAGAR O QUE ESTA ABAIXO   ##########################################################################################


    //     // 1. Default page: Loads all restaurants
    //     @GetMapping("/restaurantes")
    //     public String listarRestaurantes(Model model) {
    //         String url = backendUrl + "/api/restaurantes";

    //         try {
    //             List restaurantes = restTemplate.getForObject(url, List.class);
    //             model.addAttribute("restaurantes", restaurantes);
    //         } catch (Exception e) {
    //             model.addAttribute("restaurantes", null);
    //             model.addAttribute("erro", "Could not connect to the backend API.");
    //         }

    //         return "restaurantes";
    //     }

    //     // 2. Search page: Handles the form submission with filters
    //     @GetMapping("/restaurantes/pesquisa")
    //     public String pesquisarRestaurantes(
    //             @RequestParam(required = false) String nome,
    //             @RequestParam(required = false) String tipoCozinha,
    //             @RequestParam(required = false) String estado,
    //             @RequestParam(required = false) Integer pedidosRealizados,
    //             @RequestParam(required = false) Double avaliacaoMinima,
    //             @RequestParam(required = false) String morada,
    //             @RequestParam(required = false) Double precoMedio,
    //             Model model) {

    //         // Base URL for the backend search endpoint
    //     String url = backendUrl + "/api/restaurantes/pesquisa";

    //     // UriComponentsBuilder helps safely attach query parameters to the URL
    //     UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

    //     if (nome != null && !nome.isBlank()) builder.queryParam("nome", nome);
    //     if (tipoCozinha != null && !tipoCozinha.isBlank()) builder.queryParam("tipoCozinha", tipoCozinha);
    //     if (estado != null && !estado.isBlank()) builder.queryParam("estado", estado);
    //     if (pedidosRealizados != null) builder.queryParam("pedidosRealizados", pedidosRealizados);
    //     if (avaliacaoMinima != null) builder.queryParam("avaliacaoMinima", avaliacaoMinima);
    //     if (morada != null && !morada.isBlank()) builder.queryParam("morada", morada);
    //     if (precoMedio != null) builder.queryParam("precoMedio", precoMedio);

    //     try {
    //         // Call the backend with the built URL (e.g., http://backend:8080/api/restaurantes/pesquisa?nome=Tasca&...)
    //         List restaurantes = restTemplate.getForObject(builder.toUriString(), List.class);
    //         model.addAttribute("restaurantes", restaurantes);
    //     } catch (Exception e) {
    //         model.addAttribute("restaurantes", null);
    //         model.addAttribute("erro", "Could not fetch search results from the backend.");
    //     }

    //     // Return the same HTML view, but now populated with filtered data
    //     return "pesquisa";
    // }
    // /**
    //  * API Proxy for the Live Search Dropdown.
    //  * Returns JSON directly to the frontend JavaScript.
    //  */
    // @GetMapping("/api/web/live-search")
    // @ResponseBody
    // public ResponseEntity<?> liveSearch(@RequestParam String nome) {
    //     // Call the backend API
    //     String targetUrl = backendUrl + "/api/restaurantes/pesquisa?nome=" + nome;
        
    //     try {
    //         // Fetch as a generic Object so Spring formats it nicely as JSON
    //         Object searchResults = restTemplate.getForObject(targetUrl, Object.class);
    //         return ResponseEntity.ok(searchResults);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Error fetching live results");
    //     }
    // }

    // @GetMapping("/restaurantes/{id}/produtos")
    // public String listarProdutos(@PathVariable Long id, Model model) {
    //     // 1. Fetch products from backend for this restaurant
    //     String url = backendUrl + "/api/restaurantes/" + id + "/produtos";
        
    //     try {
    //         List produtos = restTemplate.getForObject(url, List.class);
    //         model.addAttribute("produtos", produtos);
    //         model.addAttribute("restauranteId", id);
    //         // You might want to fetch the restaurant name as well
    //         model.addAttribute("restauranteNome", "Restaurant #" + id); 
    //     } catch (Exception e) {
    //         model.addAttribute("erro", "Could not load menu.");
    //     }
        
    //     return "produtos";
    // }

    // @GetMapping("/restaurantes/{id}/produtos/search")
    // public String pesquisarProdutos(
    //         @PathVariable Long id,
    //         @RequestParam(required = false) String nome,
    //         @RequestParam(required = false) Double precoMax,
    //         @RequestParam(required = false) String categoria,
    //         @RequestParam(required = false, defaultValue = "false") Boolean disponivel,
    //         @RequestParam(required = false) Double popularidade,
    //         Model model) {

    //     // 2. Build the Backend API URL with query params
    //     String url = backendUrl + "/api/restaurantes/" + id + "/produtos/search";
    //     UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

    //     if (nome != null && !nome.isBlank()) builder.queryParam("nome", nome);
    //     if (precoMax != null) builder.queryParam("precoMax", precoMax);
    //     if (categoria != null && !categoria.isBlank()) builder.queryParam("categoria", categoria);
    //     if (disponivel) builder.queryParam("disponivel", true);
    //     if (popularidade != null) builder.queryParam("popularidade", popularidade);

    //     try {
    //         List produtos = restTemplate.getForObject(builder.toUriString(), List.class);
    //         model.addAttribute("produtos", produtos);
    //         model.addAttribute("restauranteId", id);
    //     } catch (Exception e) {
    //         model.addAttribute("erro", "Search failed.");
    //     }

    //     return "produtos";
    // }
}