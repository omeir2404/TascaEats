package pt.ul.fc.css.tascaeats.controllers; // <-- Corrigido o erro de Package

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping; // <-- Usa RestTemplate em vez de Service
import org.springframework.web.client.RestTemplate;

@Controller
public class WebRestauranteController {

    private final RestTemplate restTemplate;

    // Vai buscar o URL do backend (com fallback para localhost caso estejas a correr fora do Docker)
    @Value("${BACKEND_API_URL:http://localhost:8080}")
    private String backendUrl;

    public WebRestauranteController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/restaurantes")
    public String listarRestaurantes(Model model) {
        // Constrói o URL da tua API REST (Atenção: garante que esta rota existe no backend!)
        String url = backendUrl + "/api/restaurantes";

        try {
            // Faz o pedido GET ao backend e recebe a lista de restaurantes
            List restaurantes = restTemplate.getForObject(url, List.class);
            model.addAttribute("restaurantes", restaurantes);
        } catch (Exception e) {
            // Caso o backend esteja desligado, não rebenta a página toda
            model.addAttribute("restaurantes", null);
            model.addAttribute("erro", "Não foi possível conectar ao backend.");
        }

        // Retorna a vista HTML (restaurantes.html)
        return "restaurantes";
    }
}