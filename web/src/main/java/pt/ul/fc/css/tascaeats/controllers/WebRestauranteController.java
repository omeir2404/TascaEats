// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/restaurantes")
    public String listarRestaurantes(Model model) {
        String url = backendUrl + "/api/restaurantes";

        try {
            List<Map<String, Object>> restaurantes = restTemplate.getForObject(url, List.class);
            List<Map<String, Object>> restaurantesSanitizados = sanitizeRestaurantes(restaurantes);
            model.addAttribute("restaurantes", restaurantesSanitizados);
            model.addAttribute("restauranteDestaque",
                    restaurantesSanitizados.stream().findFirst().orElse(null));
        } catch (Exception e) {
            model.addAttribute("restaurantes", List.of());
            model.addAttribute("restauranteDestaque", null);
            model.addAttribute("erro", "Could not connect to the backend API.");
        }

        return "restaurantes";
    }

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
        String url = backendUrl + "/api/restaurantes/pesquisa";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        if (nome != null && !nome.isBlank()) {
            builder.queryParam("nome", nome);
        }
        if (tipoCozinha != null && !tipoCozinha.isBlank()) {
            builder.queryParam("tipoCozinha", tipoCozinha);
        }
        if (estado != null && !estado.isBlank()) {
            builder.queryParam("estado", estado);
        }
        if (pedidosRealizados != null) {
            builder.queryParam("pedidosRealizados", pedidosRealizados);
        }
        if (avaliacaoMinima != null) {
            builder.queryParam("avaliacaoMinima", avaliacaoMinima);
        }
        if (morada != null && !morada.isBlank()) {
            builder.queryParam("morada", morada);
        }
        if (precoMedio != null) {
            builder.queryParam("precoMedio", precoMedio);
        }

        try {
            List<Map<String, Object>> restaurantes = restTemplate.getForObject(builder.toUriString(), List.class);
            model.addAttribute("restaurantes", sanitizeRestaurantes(restaurantes));
        } catch (Exception e) {
            model.addAttribute("restaurantes", List.of());
            model.addAttribute("erro", "Could not fetch search results from the backend.");
        }

        model.addAttribute("nome", nome);
        model.addAttribute("tipoCozinha", tipoCozinha);
        model.addAttribute("estado", estado);
        model.addAttribute("pedidosRealizados", pedidosRealizados);
        model.addAttribute("avaliacaoMinima", avaliacaoMinima);
        model.addAttribute("morada", morada);
        model.addAttribute("precoMedio", precoMedio);
        return "pesquisa";
    }

    @GetMapping("/api/web/live-search")
    @ResponseBody
    public ResponseEntity<?> liveSearch(@RequestParam String nome) {
        String targetUrl = UriComponentsBuilder
                .fromHttpUrl(backendUrl + "/api/restaurantes/pesquisa")
                .queryParam("nome", nome)
                .toUriString();

        try {
            List<Map<String, Object>> searchResults = restTemplate.getForObject(targetUrl, List.class);
            return ResponseEntity.ok(sanitizeRestaurantes(searchResults));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching live results");
        }
    }

    @GetMapping("/restaurantes/{id}/produtos")
    public String listarProdutos(@PathVariable Long id, Model model) {
        String produtosUrl = UriComponentsBuilder.fromHttpUrl(backendUrl + "/api/produtos")
                .queryParam("restauranteId", id)
                .toUriString();

        try {
            List<Map<String, Object>> produtos = restTemplate.getForObject(produtosUrl, List.class);
            model.addAttribute("produtos", sanitizeProdutos(produtos));
        } catch (Exception e) {
            model.addAttribute("produtos", List.of());
            model.addAttribute("erro", "Could not load menu products from backend.");
        }

        model.addAttribute("restauranteId", id);
        model.addAttribute("restauranteNome", fetchRestaurantName(id));
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

        String produtosUrl = UriComponentsBuilder.fromHttpUrl(backendUrl + "/api/produtos")
                .queryParam("restauranteId", id)
                .toUriString();

        try {
            List<Map<String, Object>> produtos = restTemplate.getForObject(produtosUrl, List.class);
            List<Map<String, Object>> produtosFiltrados = sanitizeProdutos(produtos).stream()
                    .filter(p -> nome == null || nome.isBlank() || p.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
                    .filter(p -> precoMax == null || toDouble(p.get("preco")) <= precoMax)
                    .filter(p -> categoria == null || categoria.isBlank() || categoria.equalsIgnoreCase(String.valueOf(p.get("categoria"))))
                    .filter(p -> !Boolean.TRUE.equals(disponivel) || Boolean.TRUE.equals(p.get("disponivel")))
                    .toList();

            model.addAttribute("produtos", produtosFiltrados);
        } catch (Exception e) {
            model.addAttribute("produtos", List.of());
            model.addAttribute("erro", "Product search failed.");
        }

        model.addAttribute("restauranteId", id);
        model.addAttribute("restauranteNome", fetchRestaurantName(id));
        model.addAttribute("nome", nome);
        model.addAttribute("precoMax", precoMax);
        model.addAttribute("categoria", categoria);
        model.addAttribute("disponivel", disponivel);
        model.addAttribute("popularidade", popularidade);
        return "produtos";
    }

    private List<Map<String, Object>> sanitizeRestaurantes(List<Map<String, Object>> restaurantes) {
        if (restaurantes == null) {
            return List.of();
        }

        return restaurantes.stream().map(rest -> {
            Object nomeObj = rest.getOrDefault("nome", "Sem nome");
            String nome = nomeObj == null ? "" : nomeObj.toString();
            Object tipoRawObj = rest.getOrDefault("tipoRestaurante", rest.getOrDefault("tipoCozinha", "OUTRO"));
            String tipoRaw = tipoRawObj == null ? "OUTRO" : tipoRawObj.toString();
            Object tipoObj = rest.getOrDefault("tipoCozinha", tipoRaw);
            String tipo = formatTipoCozinha(tipoObj);

            // determine image path based on restaurant name or type (prefer local images)
            String imagePath = "/img/tasca_local.png"; // default local image
            String lower = nome.toLowerCase();
            String lowerTipo = tipo.toLowerCase();
            if (lower.contains("sushi") || lowerTipo.contains("japones") || lowerTipo.contains("sushi")) {
                imagePath = "/img/sushi_local.png";
            } else if (lower.contains("pizz") || lowerTipo.contains("italiano") || lowerTipo.contains("pizza") || lowerTipo.contains("pizzaria")) {
                imagePath = "/img/italiano_local.jpg";
            } else if (lower.contains("central") || lowerTipo.contains("portugues") || lowerTipo.contains("portuguesa")) {
                imagePath = "/img/tasca_local.png";
            } else if (lower.contains("verde") || lowerTipo.contains("verde")) {
                imagePath = "/img/cafe_local.jpg"; // economical image as tasca verde
            }

            return Map.<String, Object>of(
                    "id", rest.getOrDefault("id", 0),
                    "nome", nome,
                    "tipoRestaurante", tipoRaw,
                    "tipoCozinha", tipo,
                    "estado", rest.getOrDefault("estado", "DESCONHECIDO"),
                    "numeroAvaliacoes", rest.getOrDefault("numeroAvaliacoes", 0),
                    "morada", rest.getOrDefault("morada", Map.of()),
                    "imagePath", imagePath
            );
        }).collect(Collectors.toList());
    }

    private String formatTipoCozinha(Object tipoObj) {
        if (tipoObj == null) {
            return "Outro";
        }

        return switch (tipoObj.toString().trim().toUpperCase()) {
            case "PORTUGUES" -> "Portuguesa";
            case "ITALIANO" -> "Italiana";
            case "JAPONES" -> "Japonesa";
            case "HAMBURGUERIA" -> "Hamburgueria";
            case "VEGETARIANA" -> "Vegetariana";
            case "CHINES" -> "Chinesa";
            case "OUTRO" -> "Outro";
            default -> tipoObj.toString();
        };
    }

    private List<Map<String, Object>> sanitizeProdutos(List<Map<String, Object>> produtos) {
        if (produtos == null) {
            return List.of();
        }

        return produtos.stream().map(produto -> Map.<String, Object>of(
                "id", produto.getOrDefault("id", 0),
                "nome", produto.getOrDefault("nome", "Sem nome"),
                "preco", toDouble(produto.get("preco")),
                "categoria", resolveCategoria(produto),
                "disponivel", Boolean.TRUE.equals(produto.get("disponivel")),
                "descricao", produto.getOrDefault("descricao", ""),
                "alergenios", resolveAlergenios(produto)
        )).collect(Collectors.toList());
    }

    private String resolveCategoria(Map<String, Object> produto) {
        Object categoria = produto.get("categoria");
        if (categoria != null && !categoria.toString().isBlank()) {
            return categoria.toString().trim().toUpperCase();
        }

        String nome = String.valueOf(produto.getOrDefault("nome", "")).trim().toLowerCase();

        return switch (nome) {
            case "super bock 33cl", "chá verde", "cha verde", "coca-cola 33cl" -> "DRINK";
            case "pastel de nata" -> "DESSERT";
            case "bitoque",
                 "combinado sushi 15 peças",
                 "combinado sushi 15 pecas",
                 "gyoza",
                 "margherita",
                 "portuguesa pizza",
                 "hambúrguer vegetariano",
                 "hamburguer vegetariano",
                 "salada mix" -> "MAIN_COURSE";
            default -> "OUTRO";
        };
    }

    private String resolveAlergenios(Map<String, Object> produto) {
        Object alergenios = produto.get("alergenios");
        if (alergenios != null && !alergenios.toString().isBlank()) {
            return alergenios.toString();
        }

        String nome = String.valueOf(produto.getOrDefault("nome", "")).trim().toLowerCase();

        return switch (nome) {
            case "bitoque" -> "Ovos";
            case "super bock 33cl" -> "Glúten";
            case "pastel de nata" -> "Glúten, ovos, leite";
            case "combinado sushi 15 peças", "combinado sushi 15 pecas" -> "Peixe, soja, glúten, sésamo";
            case "chá verde", "cha verde" -> "Nenhum indicado";
            case "gyoza" -> "Glúten, soja, sésamo";
            case "margherita" -> "Glúten, leite";
            case "portuguesa pizza" -> "Glúten, leite, ovos";
            case "coca-cola 33cl" -> "Nenhum indicado";
            case "hambúrguer vegetariano", "hamburguer vegetariano" -> "Glúten, soja";
            case "salada mix" -> "Nenhum indicado";
            default -> "A confirmar";
        };
    }

    private String fetchRestaurantName(Long restauranteId) {
        try {
            String restauranteUrl = backendUrl + "/api/restaurantes/" + restauranteId;
            Map<String, Object> restaurante = restTemplate.getForObject(restauranteUrl, Map.class);
            if (restaurante == null) {
                return "Restaurante #" + restauranteId;
            }
            Object nome = restaurante.get("nome");
            return nome != null ? nome.toString() : "Restaurante #" + restauranteId;
        } catch (Exception e) {
            return "Restaurante #" + restauranteId;
        }
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
