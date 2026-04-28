package pt.ul.fc.css.tascaeats.controllers; // Pacote corrigido

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate; // Usar RestTemplate em vez de Service

@Controller
public class WebUtilizadorController {

    private final RestTemplate restTemplate;

    // Vai buscar o URL do backend injetado pelo Docker
    @Value("${BACKEND_API_URL:http://localhost:8080}")
    private String backendUrl;

    public WebUtilizadorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Página de Login (Mock - Caso A)
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Retorna login.html
    }

    // Página de Registo de Cliente (Caso B)
    @GetMapping("/registo")
    public String registoPage() {
        return "registo"; // Retorna registo.html
    }
}