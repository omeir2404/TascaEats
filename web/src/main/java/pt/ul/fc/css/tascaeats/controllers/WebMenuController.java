package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebMenuController {

    // --- TEMPORARY MOCK MENU GENERATOR ---
    private List<Map<String, Object>> getMockMenus() {
        return List.of(
            Map.of("restauranteId", 1, "nome", "Menu Especial Tasca", "restauranteNome", "Tasca do Zé", "numeroProdutos", 3, "precoMedio", 5.30),
            Map.of("restauranteId", 2, "nome", "Menu Italiano Clássico", "restauranteNome", "Bella Napoli", "numeroProdutos", 12, "precoMedio", 18.50),
            Map.of("restauranteId", 3, "nome", "Menu Zen Premium", "restauranteNome", "Sushi Zen", "numeroProdutos", 2, "precoMedio", 12.50),
            Map.of("restauranteId", 4, "nome", "Menu Económico Central", "restauranteNome", "Café Central", "numeroProdutos", 8, "precoMedio", 4.50)
        );
    }

    @GetMapping("/menus")
    public String listarMenus(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer numeroProdutosMin,
            @RequestParam(required = false) Double precoMedioMax,
            Model model) {

        // 1. Grab all mock menus
        List<Map<String, Object>> todos = getMockMenus();

        // 2. Apply the filters dynamically based on assignment requirements
        List<Map<String, Object>> filtrados = todos.stream()
                .filter(m -> nome == null || nome.isBlank() || m.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
                .filter(m -> numeroProdutosMin == null || (Integer) m.get("numeroProdutos") >= numeroProdutosMin)
                .filter(m -> precoMedioMax == null || (Double) m.get("precoMedio") <= precoMedioMax)
                .toList();

        // 3. Send the filtered list and form state back to the view
        model.addAttribute("menus", filtrados);
        model.addAttribute("nome", nome);
        model.addAttribute("numeroProdutosMin", numeroProdutosMin);
        model.addAttribute("precoMedioMax", precoMedioMax);

        return "menus";
    }
}