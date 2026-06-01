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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class WebMenuController {

    private final RestTemplate restTemplate;

    @Value("${BACKEND_API_URL:http://localhost:80}")
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
            model.addAttribute("erro", WebErrorMessages.backendFailure("Nao foi possivel carregar os menus", e, backendUrl));
        }

        List<Map<String, Object>> filtrados = menus.stream()
                .filter(m -> nome == null
                        || nome.isBlank()
                        || String.valueOf(m.getOrDefault("nome", "")).toLowerCase().contains(nome.toLowerCase())
                        || String.valueOf(m.getOrDefault("restauranteNome", "")).toLowerCase().contains(nome.toLowerCase()))
                .filter(m -> numeroProdutosMin == null || toInt(m.get("numeroProdutos")) >= numeroProdutosMin)
                .filter(m -> precoMedioMax == null || toDouble(m.get("precoMedio")) <= precoMedioMax)
                .toList();

        model.addAttribute("menus", filtrados);
        model.addAttribute("nome", nome);
        model.addAttribute("numeroProdutosMin", numeroProdutosMin);
        model.addAttribute("precoMedioMax", precoMedioMax);

        return "menus";
    }

    private List<Map<String, Object>> carregarMenusDoBackend() {
        List<Map<String, Object>> menus = restTemplate.getForObject(backendUrl + "/api/menus", List.class);
        if (menus == null) {
            return List.of();
        }

        for (Map<String, Object> menu : menus) {
            String texto = String.valueOf(menu.getOrDefault("restauranteNome", menu.getOrDefault("nome", ""))).toLowerCase();
            String imagePath = "/img/tasca.jpg";
            String rn = texto == null ? "" : texto.toLowerCase();
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
        }
        return menus;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
