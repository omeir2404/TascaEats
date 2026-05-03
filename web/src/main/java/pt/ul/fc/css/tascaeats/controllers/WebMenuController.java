// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class WebMenuController {

    private final RestTemplate restTemplate;

    @Value("${BACKEND_API_URL:http://localhost:8080}")
    private String backendUrl;

    public WebMenuController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/menus")
    public String listarMenus(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer numeroProdutosMin,
            @RequestParam(required = false) Double precoMedioMax,
            Model model) {
        List<Map<String, Object>> menus;
        try {
            menus = carregarMenusDoBackend();
        } catch (Exception e) {
            menus = List.of();
            model.addAttribute("erro", "Could not load menus from backend.");
        }

        List<Map<String, Object>> filtrados = menus.stream()
                .filter(m -> nome == null
                        || nome.isBlank()
                        || m.get("nome").toString().toLowerCase().contains(nome.toLowerCase())
                        || m.get("restauranteNome").toString().toLowerCase().contains(nome.toLowerCase()))
                .filter(m -> numeroProdutosMin == null || ((Integer) m.get("numeroProdutos")) >= numeroProdutosMin)
                .filter(m -> precoMedioMax == null || ((Double) m.get("precoMedio")) <= precoMedioMax)
                .toList();

        model.addAttribute("menus", filtrados);
        model.addAttribute("nome", nome);
        model.addAttribute("numeroProdutosMin", numeroProdutosMin);
        model.addAttribute("precoMedioMax", precoMedioMax);

        return "menus";
    }

    private List<Map<String, Object>> carregarMenusDoBackend() {
        List<Map<String, Object>> restaurantes = restTemplate.getForObject(backendUrl + "/api/restaurantes", List.class);
        List<Map<String, Object>> produtos = restTemplate.getForObject(backendUrl + "/api/produtos", List.class);

        if (restaurantes == null || produtos == null) {
            return List.of();
        }

        Map<Long, Map<String, Object>> restaurantePorId = restaurantes.stream()
                .filter(r -> r.get("id") != null)
                .collect(Collectors.toMap(
                        r -> toLong(r.get("id")),
                        Function.identity(),
                        (a, b) -> a,
                        HashMap::new
                ));

        Map<Long, List<Map<String, Object>>> produtosPorRestaurante = new HashMap<>();
        for (Map<String, Object> produto : produtos) {
            if (produto.get("restauranteId") == null) {
                continue;
            }
            Long restauranteId = toLong(produto.get("restauranteId"));
            produtosPorRestaurante.computeIfAbsent(restauranteId, key -> new ArrayList<>()).add(produto);
        }

        List<Map<String, Object>> menus = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Object>> entry : restaurantePorId.entrySet()) {
            Long restauranteId = entry.getKey();
            Map<String, Object> restaurante = entry.getValue();
            List<Map<String, Object>> itens = produtosPorRestaurante.getOrDefault(restauranteId, List.of());

            double precoMedio = 0.0;
            if (!itens.isEmpty()) {
                precoMedio = itens.stream()
                        .mapToDouble(p -> toDouble(p.get("preco")))
                        .average()
                        .orElse(0.0);
            }

            String restauranteNome = String.valueOf(restaurante.getOrDefault("nome", "Restaurante #" + restauranteId));
            Map<String, Object> menu = new HashMap<>();
            menu.put("restauranteId", restauranteId);
            menu.put("nome", "Menu de " + restauranteNome);
            menu.put("restauranteNome", restauranteNome);
            menu.put("numeroProdutos", itens.size());
            menu.put("precoMedio", Math.round(precoMedio * 100.0) / 100.0);

            // Determine an image path for the menu based on the restaurant name (case-insensitive)
            String imagePath = "/img/tasca.jpg"; // default
            String rn = restauranteNome == null ? "" : restauranteNome.toLowerCase();
            if (rn.contains("sushi")) {
                imagePath = "/img/sushi.jpeg";
            } else if (rn.contains("pizzaria") || rn.contains("pizza")) {
                imagePath = "/img/pizza.png";
            } else if (rn.contains("tasca verde") || rn.contains("verde")) {
                imagePath = "/img/economico.jpeg";
            } else if (rn.contains("tasca central") || rn.contains("tasca")) {
                imagePath = "/img/tasca.jpg";
            }

            menu.put("imagePath", imagePath);
            menus.add(menu);
        }

        return menus;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }
}
