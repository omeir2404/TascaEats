package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pt.ul.fc.css.tascaeats.service.utilizadorServices.UtilizadorService; // [cite: 304]

@Controller
public class WebUtilizadorController {

    private final UtilizadorService utilizadorService;

    public WebUtilizadorController(UtilizadorService utilizadorService) {
        this.utilizadorService = utilizadorService;
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