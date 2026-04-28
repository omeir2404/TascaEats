package pt.ul.fc.css.tascaeats.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pt.ul.fc.css.tascaeats.service.restauranteServices.RestauranteService; // [cite: 303]

@Controller
public class WebRestauranteController {

    private final RestauranteService restauranteService;

    public WebRestauranteController(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }

    // Rota para listar todos os restaurantes
    @GetMapping("/restaurantes")
    public String listarRestaurantes(Model model) {
        // Assume-se que o serviço tem um método para listar todos
        model.addAttribute("restaurantes", restauranteService.getRestaurantes());
        return "restaurantes"; // Retorna o template restaurantes.html
    }

    // Rota para a pesquisa com filtros (Caso de Uso E)
    @GetMapping("/restaurantes/pesquisa")
    public String pesquisarRestaurantes(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipoCozinha,
            @RequestParam(required = false) String estado,
            Model model) {

        // Aqui chamas a lógica de filtros que o Membro 1 implementou
        model.addAttribute("restaurantes", restauranteService.procurarRestaurantes(nome, tipoCozinha, estado));
        return "restaurantes";
    }
}