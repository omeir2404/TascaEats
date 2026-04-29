package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class WebRestauranteController {

    private final RestTemplate restTemplate;

    @Value("${BACKEND_API_URL:http://localhost:8080}")
    private String backendUrl;

    public WebRestauranteController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 1. Default page: Loads all restaurants
    @GetMapping("/restaurantes")
    public String listarRestaurantes(Model model) {
        String url = backendUrl + "/api/restaurantes";

        try {
            List restaurantes = restTemplate.getForObject(url, List.class);
            model.addAttribute("restaurantes", restaurantes);
        } catch (Exception e) {
            model.addAttribute("restaurantes", null);
            model.addAttribute("erro", "Could not connect to the backend API.");
        }

        return "restaurantes";
    }

    // 2. Search page: Handles the form submission with filters
    @GetMapping("/restaurantes/pesquisa")
    public String pesquisarRestaurantes(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipoCozinha,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer pedidosRealizados,
            @RequestParam(required = false) Double avaliacaoMinima,
            @RequestParam(required = false) String morada,
            @RequestParam(required = false) Double precoMedio,
            Model model) {

        // Base URL for the backend search endpoint
        String url = backendUrl + "/api/restaurantes/pesquisa";

        // UriComponentsBuilder helps safely attach query parameters to the URL
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        if (nome != null && !nome.isBlank()) builder.queryParam("nome", nome);
        if (tipoCozinha != null && !tipoCozinha.isBlank()) builder.queryParam("tipoCozinha", tipoCozinha);
        if (estado != null && !estado.isBlank()) builder.queryParam("estado", estado);
        if (pedidosRealizados != null) builder.queryParam("pedidosRealizados", pedidosRealizados);
        if (avaliacaoMinima != null) builder.queryParam("avaliacaoMinima", avaliacaoMinima);
        if (morada != null && !morada.isBlank()) builder.queryParam("morada", morada);
        if (precoMedio != null) builder.queryParam("precoMedio", precoMedio);

        try {
            // Call the backend with the built URL (e.g., http://backend:8080/api/restaurantes/pesquisa?nome=Tasca&...)
            List restaurantes = restTemplate.getForObject(builder.toUriString(), List.class);
            model.addAttribute("restaurantes", restaurantes);
        } catch (Exception e) {
            model.addAttribute("restaurantes", null);
            model.addAttribute("erro", "Could not fetch search results from the backend.");
        }

        // Return the same HTML view, but now populated with filtered data
        return "pesquisa";
    }
    /**
     * API Proxy for the Live Search Dropdown.
     * Returns JSON directly to the frontend JavaScript.
     */
    @GetMapping("/api/web/live-search")
    @ResponseBody
    public ResponseEntity<?> liveSearch(@RequestParam String nome) {
        // Call the backend API
        String targetUrl = backendUrl + "/api/restaurantes/pesquisa?nome=" + nome;
        
        try {
            // Fetch as a generic Object so Spring formats it nicely as JSON
            Object searchResults = restTemplate.getForObject(targetUrl, Object.class);
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching live results");
        }
    }

    @GetMapping("/restaurantes/{id}/produtos")
    public String listarProdutos(@PathVariable Long id, Model model) {
        // 1. Fetch products from backend for this restaurant
        String url = backendUrl + "/api/restaurantes/" + id + "/produtos";
        
        try {
            List produtos = restTemplate.getForObject(url, List.class);
            model.addAttribute("produtos", produtos);
            model.addAttribute("restauranteId", id);
            // You might want to fetch the restaurant name as well
            model.addAttribute("restauranteNome", "Restaurant #" + id); 
        } catch (Exception e) {
            model.addAttribute("erro", "Could not load menu.");
        }
        
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

        // 2. Build the Backend API URL with query params
        String url = backendUrl + "/api/restaurantes/" + id + "/produtos/search";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        if (nome != null && !nome.isBlank()) builder.queryParam("nome", nome);
        if (precoMax != null) builder.queryParam("precoMax", precoMax);
        if (categoria != null && !categoria.isBlank()) builder.queryParam("categoria", categoria);
        if (disponivel) builder.queryParam("disponivel", true);
        if (popularidade != null) builder.queryParam("popularidade", popularidade);

        try {
            List produtos = restTemplate.getForObject(builder.toUriString(), List.class);
            model.addAttribute("produtos", produtos);
            model.addAttribute("restauranteId", id);
        } catch (Exception e) {
            model.addAttribute("erro", "Search failed.");
        }

        return "produtos";
    }
}