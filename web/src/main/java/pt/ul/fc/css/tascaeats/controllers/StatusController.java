package pt.ul.fc.css.tascaeats.controllers;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pt.ul.fc.css.tascaeats.model.UseCaseComparison;
@Controller
public class StatusController {
    @GetMapping({"/", "/status"})
    public String statusPage(Model model) {
        List<UseCaseComparison> useCases = List.of(
                new UseCaseComparison("A", "Login com autenticação",
                        "Parcial", "Rota mock disponível; falta fluxo completo na interface browser.", "status-partial",
                        "Pendente", "Ainda não existe ecrã JavaFX dedicado ao login.", "status-pending"),
                new UseCaseComparison("B", "Registo de Clientes",
                        "Parcial", "Existe controller/rota; falta formulário Thymeleaf completo.", "status-partial",
                        "N/A", "Não é requisito da app nativa.", "status-na"),
                new UseCaseComparison("C", "Registo de Administradores e Entregadores",
                        "N/A", "Não faz parte do browser.", "status-na",
                        "Pendente", "Ainda não existe ecrã JavaFX para registo destes utilizadores.", "status-pending"),
                new UseCaseComparison("D", "Verificar, remover e atualizar utilizadores",
                        "N/A", "Não faz parte do browser.", "status-na",
                        "Pendente", "Falta ecrã de gestão de utilizadores na app nativa.", "status-pending"),
                new UseCaseComparison("E", "Buscar por restaurantes",
                        "Parcial", "A página de restaurantes existe, mas ainda pode ser refinada.", "status-partial",
                        "Implementado", "A app JavaFX já faz pesquisa/listagem de restaurantes.", "status-done"),
                new UseCaseComparison("F", "Gerir Restaurantes",
                        "N/A", "Não faz parte do browser.", "status-na",
                        "Pendente", "Ainda não existe ecrã JavaFX para criar/editar/abrir/fechar restaurantes.", "status-pending"),
                new UseCaseComparison("G", "Gerir Menus",
                        "N/A", "Não faz parte do browser.", "status-na",
                        "Pendente", "Menus ainda não têm interface JavaFX dedicada.", "status-pending"),
                new UseCaseComparison("H", "Gerir Produtos",
                        "N/A", "Não faz parte do browser.", "status-na",
                        "Pendente", "Falta interface JavaFX para CRUD de produtos.", "status-pending"),
                new UseCaseComparison("I", "Buscar produtos de um restaurante",
                        "Pendente", "Ainda não existe vista browser para pesquisa de produtos.", "status-pending",
                        "N/A", "Não é requisito da app nativa.", "status-na"),
                new UseCaseComparison("J", "Criação de um pedido",
                        "Pendente", "Ainda falta o fluxo web de criação de pedido.", "status-pending",
                        "N/A", "Não é requisito da app nativa.", "status-na"),
                new UseCaseComparison("K", "Registo e processamento do pagamento",
                        "N/A", "Não faz parte do browser.", "status-na",
                        "Pendente", "Ainda não há ecrã JavaFX para pagamentos.", "status-pending"),
                new UseCaseComparison("L", "Atualização do estado do pedido",
                        "N/A", "Não faz parte do browser.", "status-na",
                        "Pendente", "Falta ecrã JavaFX para atualizar estados do pedido.", "status-pending"),
                new UseCaseComparison("M", "Atribuição de entregador a um pedido",
                        "N/A", "Não faz parte do browser.", "status-na",
                        "Pendente", "Ainda não existe ecrã JavaFX para atribuição de entregador.", "status-pending"),
                new UseCaseComparison("N", "Cancelamento de um pedido",
                        "Pendente", "Ainda não há fluxo browser para cancelar pedidos.", "status-pending",
                        "Pendente", "Ainda não existe ação nativa para cancelamento.", "status-pending"));
        long browserDone = useCases.stream().filter(item -> "Implementado".equals(item.browserStatus())).count();
        long browserPartial = useCases.stream().filter(item -> "Parcial".equals(item.browserStatus())).count();
        long browserPending = useCases.stream().filter(item -> "Pendente".equals(item.browserStatus())).count();
        long nativeDone = useCases.stream().filter(item -> "Implementado".equals(item.nativeStatus())).count();
        long nativePartial = useCases.stream().filter(item -> "Parcial".equals(item.nativeStatus())).count();
        long nativePending = useCases.stream().filter(item -> "Pendente".equals(item.nativeStatus())).count();
        model.addAttribute("useCases", useCases);
        model.addAttribute("browserDone", browserDone);
        model.addAttribute("browserPartial", browserPartial);
        model.addAttribute("browserPending", browserPending);
        model.addAttribute("nativeDone", nativeDone);
        model.addAttribute("nativePartial", nativePartial);
        model.addAttribute("nativePending", nativePending);
        return "index";
    }
}
