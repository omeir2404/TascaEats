package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebUtilizadorController {

    // --- TEMPORARY MOCK USER GENERATOR ---
    private List<Map<String, Object>> getMockUtilizadores() {
        return List.of(
            Map.of("id", 1, "nome", "João Silva", "email", "joao@email.com", "papel", "CLIENT", "ativo", true),
            Map.of("id", 2, "nome", "Maria Santos", "email", "maria@email.com", "papel", "ADMIN", "ativo", true),
            Map.of("id", 3, "nome", "Carlos Ferreira", "email", "carlos@email.com", "papel", "COURIER", "ativo", false),
            Map.of("id", 4, "nome", "Ana Costa", "email", "ana@email.com", "papel", "CLIENT", "ativo", false)
        );
    }

    @GetMapping("/utilizadores")
    public String listarUtilizadores(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String papel,
            @RequestParam(required = false, defaultValue = "false") Boolean apenasAtivos,
            Model model) {

        // 1. Grab all mock users
        List<Map<String, Object>> todos = getMockUtilizadores();

        // 2. Apply filters dynamically
        List<Map<String, Object>> filtrados = todos.stream()
                .filter(u -> nome == null || nome.isBlank() || u.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
                .filter(u -> papel == null || papel.isBlank() || u.get("papel").toString().equalsIgnoreCase(papel))
                .filter(u -> !apenasAtivos || (Boolean) u.get("ativo") == true)
                .toList();

        // 3. Send data and form state back to the frontend
        model.addAttribute("utilizadores", filtrados);
        model.addAttribute("nome", nome);
        model.addAttribute("papel", papel);
        model.addAttribute("apenasAtivos", apenasAtivos);

        return "utilizadores";
    }
}