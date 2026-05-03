package pt.ul.fc.css.tascaeats.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static pt.ul.fc.css.tascaeats.ui.UiUtils.*;

/**
 * Use Cases K / L / M / N – Payment, order state, driver assignment, cancellation.
 */
public class PedidosPane {

    private final AppState state;
    private final Stage    owner;
    private Label          statusLabel;

    public PedidosPane(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public VBox build() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TableView<Map<String, Object>> table = tableOf(display,
            col("ID", "id"), col("Cliente", "clienteNome"), col("Restaurante", "restNome"),
            col("Estado", "estado"), col("Total (€)", "total"),
            col("Pagamento", "pagamento"), col("Entregador", "entregador")
        );

        TextArea details = new TextArea();
        details.setEditable(false); details.setPrefRowCount(4);
        details.setPromptText("Selecione um pedido para ver detalhes.");
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) details.setText(
                "ID: " + n.get("id") + "\nCliente: " + n.get("clienteNome") +
                "\nRestaurante: " + n.get("restNome") + "\nEstado: " + n.get("estado") +
                "\nTotal: " + n.get("total") + "€\nPagamento: " + n.get("pagamento") +
                "\nEntregador: " + n.get("entregador"));
        });

        ComboBox<String> estadoBox = combo("Todos", "CRIADO", "PAGO", "A_PREPARAR",
            "PRONTO_PARA_ENTREGA", "ATRIBUIDO_ENTREGADOR", "ENTREGUE", "CANCELADO");
        TextField clienteF = field("Filtrar por cliente...", 160);

        Runnable applyFilter = () -> {
            String st = estadoBox.getValue();
            String cl = clienteF.getText().toLowerCase();
            display.setAll(state.pedidos.stream()
                .filter(p -> "Todos".equals(st) || st.equals(s(p, "estado")))
                .filter(p -> cl.isBlank() || s(p, "clienteNome").toLowerCase().contains(cl))
                .toList());
        };

        Button filtrar    = new Button("Filtrar");    filtrar.setOnAction(e -> applyFilter.run());
        Button refreshBtn = colorBtn("↻ Atualizar",  "#607d8b"); refreshBtn.setOnAction(e -> syncPedidos(applyFilter));

        // K
        Button pagarBtn      = colorBtn("💳 Pagar",              "#1976d2");
        // L
        Button prepBtn       = colorBtn("⌛ Iniciar Prep.",       "#e65100");
        Button prontoBtn     = colorBtn("✓ Marcar Pronto",       "#558b2f");
        // M
        Button atribBtn      = colorBtn("🚗 Atribuir Entregador", "#6a1b9a");
        Button entregBtn     = colorBtn("📦 Registar Recolha",   "#00838f");
        Button entregFinalBtn = colorBtn("📬 Entrega Final",      "#004d40");
        // N
        Button cancelBtn     = colorBtn("✕ Cancelar Pedido",    "#c62828");

        pagarBtn.setOnAction(e      -> handlePagamento(table, applyFilter));
        prepBtn.setOnAction(e       -> handleIniciarPrep(table, applyFilter));
        prontoBtn.setOnAction(e     -> handleMarcarPronto(table, applyFilter));
        atribBtn.setOnAction(e      -> handleAtribuirEntregador(table));
        entregBtn.setOnAction(e     -> handleRecolha(table, applyFilter));
        entregFinalBtn.setOnAction(e -> handleEntregaFinal(table, applyFilter));
        cancelBtn.setOnAction(e     -> handleCancelar(table, applyFilter));

        statusLabel = statusLabel("A carregar pedidos...");
        syncPedidos(applyFilter);
        if (state.users.isEmpty()) {
            state.bg(() -> state.utilizadorClient.listarUtilizadores(),
                result -> state.users.setAll(result));
        }

        return pane(sectionTitle("K/L/M/N – Gestão de Pedidos"),
            hbar(lbl("Estado:"), estadoBox, lbl("Cliente:"), clienteF, filtrar, refreshBtn),
            hbar(pagarBtn, prepBtn, prontoBtn, atribBtn, entregBtn, entregFinalBtn, cancelBtn),
            table, new Label("Detalhes do pedido selecionado:"), details, statusLabel);
    }

    // ── Sync ─────────────────────────────────────────────────────────────

    public void syncPedidos(Runnable afterUpdate) {
        state.bg(() -> {
            try {
                ResponseEntity<List<Map<String, Object>>> resp = state.rest.exchange(
                    AppState.BACKEND + "/pedidos", HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
                return resp.getBody() != null ? resp.getBody() : List.of();
            } catch (Exception e) { return List.of(); }
        }, result -> {
            List<Map<String, Object>> typed = (List<Map<String, Object>>) (List<?>) result;
            state.pedidos.setAll(typed.stream().map(this::normalizePedido).toList());
            if (afterUpdate != null) afterUpdate.run();
            setStatus(statusLabel, "✓ " + state.pedidos.size() + " pedidos carregados.");
        });
    }

    // ── Handlers ──────────────────────────────────────────────────────────

    // K – Payment
    private void handlePagamento(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!"CRIADO".equals(s(sel, "estado"))) { warn("Só pedidos CRIADO podem ser pagos.", owner); return; }
        ChoiceDialog<String> cd = new ChoiceDialog<>("MBWAY", "MBWAY", "MULTIBANCO", "DINHEIRO");
        cd.setTitle("Processar Pagamento"); cd.setHeaderText("Método de pagamento:"); cd.initOwner(owner);
        cd.showAndWait().ifPresent(metodo -> {
            try {
                // ProcessarPagamentoDTO: {pedidoId, tipoPagamento, detalhes (required)}
                Map<String, Object> detalhes = new LinkedHashMap<>();
                if ("MBWAY".equals(metodo))           detalhes.put("numeroMbway", "910000000");
                else if ("MULTIBANCO".equals(metodo)) { detalhes.put("entidade", "11111"); detalhes.put("referencia", "123456789"); }
                else                                  detalhes.put("valorTroco", 0.0);

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("pedidoId",      toLong(sel.get("id")));
                payload.put("tipoPagamento", metodo);
                payload.put("detalhes",      detalhes);
                state.rest.postForEntity(AppState.BACKEND + "/pagamentos", payload, Map.class);
                info("✓ Pagamento via " + metodo + " processado.", owner);
                syncPedidos(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao processar pagamento: " + ex.getMessage(), owner); }
        });
    }

    // L – Start preparation
    private void handleIniciarPrep(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!"PAGO".equals(s(sel, "estado"))) { warn("Só pedidos PAGO podem iniciar preparação.", owner); return; }
        try {
            patch("/entregas/pedido/" + sel.get("id") + "/iniciar-preparacao");
            info("✓ Preparação iniciada.", owner);
            syncPedidos(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao iniciar preparação: " + ex.getMessage(), owner); }
    }

    // L – Mark ready
    private void handleMarcarPronto(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!"A_PREPARAR".equals(s(sel, "estado"))) { warn("Só pedidos em A_PREPARAR podem ser marcados prontos.", owner); return; }
        try {
            patch("/entregas/pedido/" + sel.get("id") + "/marcar-pronto");
            info("✓ Pedido marcado como pronto para entrega.", owner);
            syncPedidos(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao marcar pronto: " + ex.getMessage(), owner); }
    }

    // M – Assign driver (endpoint commented out in backend, shows note)
    private void handleAtribuirEntregador(TableView<Map<String, Object>> table) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!"PRONTO_PARA_ENTREGA".equals(s(sel, "estado"))) {
            warn("Pedido deve estar PRONTO_PARA_ENTREGA.", owner); return;
        }
        List<String> couriers = state.users.stream()
            .filter(u -> "COURIER".equals(s(u, "papel")) && "Sim".equals(s(u, "ativo")))
            .map(u -> s(u, "nome")).toList();
        if (couriers.isEmpty()) { warn("Nenhum entregador disponível.", owner); return; }
        ChoiceDialog<String> cd = new ChoiceDialog<>(couriers.get(0), couriers);
        cd.setTitle("Atribuir Entregador"); cd.setHeaderText("Escolha o entregador:"); cd.initOwner(owner);
        cd.showAndWait().ifPresent(nome ->
            info("✓ Entregador " + nome + " anotado (persistência pendente no backend).", owner));
    }

    // M – Register pickup
    private void handleRecolha(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!"PRONTO_PARA_ENTREGA".equals(s(sel, "estado"))) {
            warn("Pedido deve estar PRONTO_PARA_ENTREGA para registar recolha.", owner); return;
        }
        try {
            patch("/entregas/pedido/" + sel.get("id") + "/recolha");
            info("✓ Recolha registada.", owner);
            syncPedidos(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao registar recolha: " + ex.getMessage(), owner); }
    }

    // M – Register final delivery
    private void handleEntregaFinal(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        try {
            patch("/entregas/pedido/" + sel.get("id") + "/entrega-final");
            info("✓ Entrega final registada.", owner);
            syncPedidos(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao registar entrega final: " + ex.getMessage(), owner); }
    }

    // N – Cancel
    private void handleCancelar(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        String st = s(sel, "estado");
        if (!"CRIADO".equals(st) && !"PAGO".equals(st)) {
            warn("Só pedidos CRIADO ou PAGO podem ser cancelados.", owner); return;
        }
        if (!confirm("Cancelar pedido #" + sel.get("id") + "?", owner)) return;
        try {
            patch("/pedidos/" + sel.get("id") + "/cancelar");
            info("✓ Pedido cancelado.", owner);
            syncPedidos(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao cancelar: " + ex.getMessage(), owner); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Fire a PATCH with no body to the given path (relative to BACKEND). */
    private void patch(String path) {
        state.rest.exchange(AppState.BACKEND + path, HttpMethod.PATCH, HttpEntity.EMPTY, Object.class);
    }

    private Map<String, Object> normalizePedido(Map<String, Object> p) {
        Long restId = null;
        Object rid = p.get("restauranteId");
        if (rid == null) {
            Object ids = p.get("restaurantesIds");
            if (ids instanceof List<?> list && !list.isEmpty()) rid = list.get(0);
        }
        if (rid != null) restId = toLong(rid);
        final Long fRestId = restId;

        String restNome = state.restaurantes.stream()
            .filter(r -> fRestId != null && fRestId.equals(toLong(r.get("id"))))
            .findFirst().map(r -> s(r, "nome")).orElse("Restaurante #" + fRestId);

        Object cid = p.get("clienteId");
        String clienteNome = state.users.stream()
            .filter(u -> cid != null && cid.toString().equals(Objects.toString(u.get("id"))))
            .findFirst().map(u -> s(u, "nome")).orElse(Objects.toString(cid, "-"));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          p.getOrDefault("id", "?"));
        m.put("clienteNome", clienteNome);
        m.put("clienteId",   cid);
        m.put("restNome",    restNome);
        m.put("restId",      fRestId);
        m.put("estado",      p.getOrDefault("estado", "-"));
        m.put("total",       p.getOrDefault("valorTotal", p.getOrDefault("total", "0.00")));
        m.put("pagamento",   p.getOrDefault("tipoPagamento", p.getOrDefault("pagamento", "-")));
        m.put("entregador",  p.getOrDefault("entregador", "-"));
        return m;
    }
}
