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

    // --- 1. MOCK CART TO CHECKOUT ---
    @PostMapping("/carrinho/adicionar")
    public String simularAdicionarAoCarrinho() {
        // In a real app, this adds to session cart. For now, jump straight to checkout!
        return "redirect:/checkout";
    }

// --- 2. CHECKOUT PAGE (Payment details) ---
    @GetMapping("/checkout")
    public String mostrarCheckout(Model model) {
        // Create a mock list of items in the cart
        List<Map<String, Object>> itensCarrinho = List.of(
            Map.of("nome", "Bacalhau à Brás", "quantidade", 1, "preco", 12.50),
            Map.of("nome", "Super Bock 33cl", "quantidade", 2, "preco", 4.00) // 2x 2.00€
        );

        model.addAttribute("itens", itensCarrinho);
        model.addAttribute("total", 16.50); // Updated to match the items (12.50 + 4.00)
        
        return "checkout";
    }

    @PostMapping("/checkout/processar")
    public String processarCheckout(
            @RequestParam String metodoPagamento,
            @RequestParam(required = false) String telemovel,
            @RequestParam(required = false) String bandeiraCartao) {

        // Create a new mock order
        Map<String, Object> novoPedido = new HashMap<>();
        novoPedido.put("id", nextPedidoId++);
        novoPedido.put("restauranteNome", "Sushi Zen"); // Mocked
        novoPedido.put("total", 24.50);
        novoPedido.put("estado", "PENDING"); // Starts as pending
        novoPedido.put("entregador", "Waiting for courier...");
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        novoPedido.put("data", dtf.format(LocalDateTime.now()));
        novoPedido.put("avaliado", false);
        novoPedido.put("pagamento", metodoPagamento);

        mockPedidos.add(novoPedido);
        
        return "redirect:/pedidos/" + novoPedido.get("id");
    }

    // --- 3. ORDER DETAILS (View Status, Cancel, Review) ---
    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable Integer id, Model model) {
        Optional<Map<String, Object>> pedido = mockPedidos.stream().filter(p -> p.get("id").equals(id)).findFirst();
        
        if (pedido.isPresent()) {
            model.addAttribute("pedido", pedido.get());
            return "pedido_detalhe";
        }
        return "redirect:/restaurantes"; 
    }

    // --- 4. CANCEL ORDER ---
    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelarPedido(@PathVariable Integer id) {
        mockPedidos.stream().filter(p -> p.get("id").equals(id)).findFirst().ifPresent(p -> {
            p.put("estado", "CANCELLED");
            p.put("entregador", "None");
        });
        return "redirect:/pedidos/" + id;
    }

    // --- 5. EVALUATE RESTAURANT ---
    @PostMapping("/pedidos/{id}/avaliar")
    public String avaliarRestaurante(
            @PathVariable Integer id,
            @RequestParam Integer pontuacao,
            @RequestParam String comentario) {
        
        mockPedidos.stream().filter(p -> p.get("id").equals(id)).findFirst().ifPresent(p -> {
            p.put("avaliado", true);
            // In a real app, save the review to the database here!
            System.out.println("Review saved: " + pontuacao + " stars. Comment: " + comentario);
        });
        return "redirect:/pedidos/" + id;
    }

// --- 6. MY ORDERS HISTORY ---
    @GetMapping("/meus-pedidos")
    public String listarMeusPedidos(Model model, HttpSession session) {
        // Ensure the user is logged in before showing orders
        if (session.getAttribute("loggedUser") == null) {
            return "redirect:/login";
        }
        
        // In a real app, we would filter mockPedidos by the logged-in user's ID.
        // For now with our mock data, we will just show all mock orders.
        model.addAttribute("pedidos", mockPedidos);
        return "meus_pedidos";
    }
}