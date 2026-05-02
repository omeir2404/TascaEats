package pt.ul.fc.css.tascaeats.controllers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebPedidoController {

    private static final String CART_SESSION_KEY = "cartItems";
    private static final String LOGGED_USER_SESSION_KEY = "loggedUser";

    private final RestTemplate restTemplate;

    @Value("${BACKEND_API_URL:http://localhost:8080}")
    private String backendUrl;

    public WebPedidoController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // --- 1. ADD TO CART ---
    @PostMapping("/carrinho/adicionar")
    public String simularAdicionarAoCarrinho(
            @RequestParam Long produtoId,
            @RequestParam(defaultValue = "1") Integer quantidade,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Double preco,
            @RequestParam(required = false) Long restauranteId,
            HttpSession session) {
        if (quantidade == null || quantidade < 1) {
            quantidade = 1;
        }

        Map<String, Object> produto = null;
        try {
            produto = restTemplate.getForObject(backendUrl + "/api/produtos/" + produtoId, Map.class);
        } catch (Exception ignored) {
            // we'll try to fallback to form-provided data below
        }

        if (produto == null) {
            // fallback to provided form values
            if (nome == null || preco == null || restauranteId == null) {
                return "redirect:/restaurantes";
            }
            produto = new HashMap<>();
            produto.put("nome", nome);
            produto.put("preco", preco);
            produto.put("restauranteId", restauranteId);
        }

        List<Map<String, Object>> carrinho = getCarrinho(session);
        Long restId = null;
        Object restObj = produto.get("restauranteId");
        if (restObj != null) {
            try {
                restId = toLong(restObj);
            } catch (Exception e) {
                restId = restauranteId;
            }
        } else {
            restId = restauranteId;
        }

        if (restId == null) {
            return "redirect:/restaurantes";
        }
        Optional<Map<String, Object>> existente = carrinho.stream()
                .filter(item -> produtoId.equals(toLong(item.get("produtoId"))))
                .findFirst();

        if (existente.isPresent()) {
            Integer qtdAtual = Integer.parseInt(String.valueOf(existente.get().get("quantidade")));
            existente.get().put("quantidade", qtdAtual + quantidade);
        } else {
            Map<String, Object> item = new HashMap<>();
            item.put("produtoId", produtoId);
            item.put("restauranteId", restId);
            item.put("nome", produto.getOrDefault("nome", "Produto"));
            item.put("preco", toDouble(produto.get("preco")));
            item.put("quantidade", quantidade);
            carrinho.add(item);
        }

        session.setAttribute(CART_SESSION_KEY, carrinho);
        return "redirect:/checkout";
    }

    // --- 2. CHECKOUT PAGE (Payment details) ---
    @GetMapping("/checkout")
    public String mostrarCheckout(Model model, HttpSession session) {
        List<Map<String, Object>> itensCarrinho = getCarrinho(session);
        double total = itensCarrinho.stream()
                .mapToDouble(item -> toDouble(item.get("preco")) * toInt(item.get("quantidade")))
                .sum();

        Object checkoutError = session.getAttribute("checkoutError");
        if (checkoutError != null) {
            model.addAttribute("error", checkoutError.toString());
            session.removeAttribute("checkoutError");
        }

        model.addAttribute("itens", itensCarrinho);
        model.addAttribute("total", Math.round(total * 100.0) / 100.0);
        return "checkout";
    }

    @PostMapping("/checkout/processar")
    public String processarCheckout(
            @RequestParam String metodoPagamento,
            @RequestParam(required = false) String telemovel,
            @RequestParam(required = false) String bandeiraCartao,
            HttpSession session) {

        Long clienteId = getLoggedUserId(session);
        if (clienteId == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> carrinho = getCarrinho(session);
        if (carrinho.isEmpty()) {
            session.setAttribute("checkoutError", "Carrinho vazio. Adicione produtos antes de finalizar.");
            return "redirect:/checkout";
        }

        try {
            List<Map<String, Object>> itensPedido = carrinho.stream().map(item -> {
                Map<String, Object> itemPedido = new HashMap<>();
                itemPedido.put("produtoId", toLong(item.get("produtoId")));
                itemPedido.put("restauranteId", toLong(item.get("restauranteId")));
                itemPedido.put("quantidade", toInt(item.get("quantidade")));
                return itemPedido;
            }).toList();

            Map<String, Object> pedidoPayload = new HashMap<>();
            pedidoPayload.put("clienteId", clienteId);
            pedidoPayload.put("itens", itensPedido);

            restTemplate.postForEntity(backendUrl + "/pedidos", pedidoPayload, Object.class);

            Long pedidoId = findPedidoMaisRecente(clienteId);
            if (pedidoId == null) {
                session.setAttribute("checkoutError", "Pedido criado, mas nao foi possivel obter o ID.");
                return "redirect:/meus-pedidos";
            }

            Map<String, Object> detalhesPagamento = buildDetalhesPagamento(metodoPagamento, telemovel, bandeiraCartao, pedidoId);
            Map<String, Object> pagamentoPayload = new HashMap<>();
            pagamentoPayload.put("pedidoId", pedidoId);
            pagamentoPayload.put("tipoPagamento", metodoPagamento);
            pagamentoPayload.put("detalhes", detalhesPagamento);

            restTemplate.postForEntity(backendUrl + "/pagamentos", pagamentoPayload, Object.class);

            session.removeAttribute(CART_SESSION_KEY);
            return "redirect:/pedidos/" + pedidoId;
        } catch (Exception e) {
            session.setAttribute("checkoutError", "Falha ao processar checkout: " + e.getMessage());
            return "redirect:/checkout";
        }

    }

    // --- 3. ORDER DETAILS (View Status, Cancel, Review) ---
    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable Long id, Model model, HttpSession session) {
        Long clienteId = getLoggedUserId(session);
        if (clienteId == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> pedidos = getPedidosDoCliente(clienteId);
        Optional<Map<String, Object>> pedido = pedidos.stream()
                .filter(p -> id.equals(toLong(p.get("id"))))
                .findFirst();

        if (pedido.isPresent()) {
            model.addAttribute("pedido", normalizePedido(pedido.get()));
            return "pedido_detalhe";
        }

        return "redirect:/meus-pedidos";
    }

    // --- 4. CANCEL ORDER ---
    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelarPedido(@PathVariable Long id) {
        try {
            restTemplate.exchange(
                    backendUrl + "/pedidos/" + id + "/cancelar",
                    HttpMethod.PATCH,
                    HttpEntity.EMPTY,
                    Object.class
            );
        } catch (Exception e) {
            return "redirect:/pedidos/" + id;
        }
        return "redirect:/pedidos/" + id;
    }

    // --- 5. EVALUATE RESTAURANT ---
    @PostMapping("/pedidos/{id}/avaliar")
    public String avaliarRestaurante(
            @PathVariable Long id,
            @RequestParam Integer pontuacao,
            @RequestParam String comentario) {

        // Placeholder until backend rating endpoint exists.
        if (pontuacao == null || comentario == null) {
            return "redirect:/pedidos/" + id;
        }
        return "redirect:/pedidos/" + id;
    }

    // --- 6. MY ORDERS HISTORY ---
    @GetMapping("/meus-pedidos")
    public String listarMeusPedidos(Model model, HttpSession session) {
        Long clienteId = getLoggedUserId(session);
        if (clienteId == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> pedidos = getPedidosDoCliente(clienteId).stream()
                .map(this::normalizePedido)
                .toList();

        model.addAttribute("pedidos", pedidos);
        return "meus_pedidos";
    }

    private List<Map<String, Object>> getCarrinho(HttpSession session) {
        Object cartObj = session.getAttribute(CART_SESSION_KEY);
        if (cartObj instanceof List<?> cartList) {
            return (List<Map<String, Object>>) cartList;
        }
        return new java.util.ArrayList<>();
    }

    private Long getLoggedUserId(HttpSession session) {
        Object loggedUserObj = session.getAttribute(LOGGED_USER_SESSION_KEY);
        if (loggedUserObj instanceof Map<?, ?> userMap) {
            Object id = userMap.get("id");
            if (id != null) {
                return toLong(id);
            }
        }
        return null;
    }

    private List<Map<String, Object>> getPedidosDoCliente(Long clienteId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(backendUrl + "/pedidos")
                    .queryParam("clienteId", clienteId)
                    .toUriString();
            List<Map<String, Object>> pedidos = restTemplate.getForObject(url, List.class);
            return pedidos == null ? List.of() : pedidos;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Long findPedidoMaisRecente(Long clienteId) {
        return getPedidosDoCliente(clienteId).stream()
                .map(p -> toLong(p.get("id")))
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private Map<String, Object> normalizePedido(Map<String, Object> pedido) {
        Long restauranteId = null;
        Object restaurantesIds = pedido.get("restaurantesIds");
        if (restaurantesIds instanceof List<?> ids && !ids.isEmpty()) {
            restauranteId = toLong(ids.get(0));
        }

        String data = String.valueOf(pedido.getOrDefault("data", ""));
        String hora = String.valueOf(pedido.getOrDefault("hora", ""));
        String dataComHora = (data + " " + hora).trim();

        Map<String, Object> normalized = new HashMap<>();
        normalized.put("id", pedido.getOrDefault("id", 0));
        normalized.put("restauranteNome", restauranteId != null ? fetchRestaurantName(restauranteId) : "Restaurante");
        normalized.put("total", toDouble(pedido.get("valorTotal")));
        normalized.put("estado", pedido.getOrDefault("estado", "PENDING"));
        normalized.put("entregador", "A definir");
        normalized.put("data", dataComHora);
        normalized.put("avaliado", false);
        return normalized;
    }

    private String fetchRestaurantName(Long restauranteId) {
        try {
            Map<String, Object> restaurante = restTemplate.getForObject(backendUrl + "/api/restaurantes/" + restauranteId, Map.class);
            if (restaurante == null) {
                return "Restaurante #" + restauranteId;
            }
            return String.valueOf(restaurante.getOrDefault("nome", "Restaurante #" + restauranteId));
        } catch (Exception e) {
            return "Restaurante #" + restauranteId;
        }
    }

    private Map<String, Object> buildDetalhesPagamento(String metodoPagamento, String telemovel, String bandeiraCartao, Long pedidoId) {
        Map<String, Object> detalhes = new HashMap<>();
        if ("MBWAY".equalsIgnoreCase(metodoPagamento)) {
            detalhes.put("numeroTelemovel", telemovel == null ? "910000000" : telemovel);
            return detalhes;
        }

        if ("MULTIBANCO".equalsIgnoreCase(metodoPagamento)) {
            detalhes.put("entidade", "WEB");
            detalhes.put("referencia", "MB-" + pedidoId);
            detalhes.put("bandeiraCartao", bandeiraCartao == null ? "VISA" : bandeiraCartao);
            return detalhes;
        }

        detalhes.put("valorRecebido", 0.0);
        return detalhes;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }
}