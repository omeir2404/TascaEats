package pt.ul.fc.css.tascaeats.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebPedidoController {

    // --- MUTABLE MOCK ORDERS DATABASE ---
    private List<Map<String, Object>> mockPedidos = new ArrayList<>(Arrays.asList(
        new HashMap<>(Map.of(
            "id", 1, "restauranteNome", "Tasca do Zé", "total", 14.50,
            "estado", "DELIVERED", "entregador", "Carlos Ferreira",
            "data", "2026-04-28 19:30", "avaliado", false
        ))
    ));
    private int nextPedidoId = 2;

    // --- MOCK CART TO CHECKOUT ---
    @PostMapping("/carrinho/adicionar")
    public String simularAdicionarAoCarrinho() {
        return "redirect:/checkout";
    }

    // --- CHECKOUT PAGE ---
    @GetMapping("/checkout")
    public String mostrarCheckout(Model model) {
        List<Map<String, Object>> itensCarrinho = List.of(
            Map.of("nome", "Bacalhau à Brás", "quantidade", 1, "preco", 12.50),
            Map.of("nome", "Super Bock 33cl", "quantidade", 2, "preco", 4.00)
        );

        model.addAttribute("itens", itensCarrinho);
        model.addAttribute("total", 16.50);

        return "checkout";
    }

    @PostMapping("/checkout/processar")
    public String processarCheckout(
            @RequestParam String metodoPagamento,
            @RequestParam(required = false) String telemovel,
            @RequestParam(required = false) String bandeiraCartao) {

        Map<String, Object> novoPedido = new HashMap<>();
        novoPedido.put("id", nextPedidoId++);
        novoPedido.put("restauranteNome", "Sushi Zen");
        novoPedido.put("total", 24.50);
        novoPedido.put("estado", "PENDING");
        novoPedido.put("entregador", "Waiting for courier...");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        novoPedido.put("data", dtf.format(LocalDateTime.now()));
        novoPedido.put("avaliado", false);
        novoPedido.put("pagamento", metodoPagamento);

        mockPedidos.add(novoPedido);

        return "redirect:/pedidos/" + novoPedido.get("id");
    }

    // --- ORDER DETAILS ---
    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable Integer id, Model model) {
        Optional<Map<String, Object>> pedido = mockPedidos.stream().filter(p -> p.get("id").equals(id)).findFirst();

        if (pedido.isPresent()) {
            model.addAttribute("pedido", pedido.get());
            return "pedido_detalhe";
        }
        return "redirect:/restaurantes";
    }

    // --- CANCEL ORDER ---
    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelarPedido(@PathVariable Integer id) {
        mockPedidos.stream().filter(p -> p.get("id").equals(id)).findFirst().ifPresent(p -> {
            p.put("estado", "CANCELLED");
            p.put("entregador", "None");
        });
        return "redirect:/pedidos/" + id;
    }

    // --- EVALUATE RESTAURANT ---
    @PostMapping("/pedidos/{id}/avaliar")
    public String avaliarRestaurante(
            @PathVariable Integer id,
            @RequestParam Integer pontuacao,
            @RequestParam String comentario) {

        mockPedidos.stream().filter(p -> p.get("id").equals(id)).findFirst().ifPresent(p -> {
            p.put("avaliado", true);
        });
        return "redirect:/pedidos/" + id;
    }

    // --- MY ORDERS HISTORY ---
    @GetMapping("/meus-pedidos")
    public String listarMeusPedidos(Model model, HttpSession session) {
        if (session.getAttribute("loggedUser") == null) {
            return "redirect:/login";
        }

        model.addAttribute("pedidos", mockPedidos);
        return "meus_pedidos";
    }
}
