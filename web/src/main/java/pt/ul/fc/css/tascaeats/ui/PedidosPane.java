// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################


package pt.ul.fc.css.tascaeats.ui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.col;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.colorBtn;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.combo;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.confirm;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.field;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.hbar;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.info;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.lbl;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.pane;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.s;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.sectionTitle;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.selected;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.setStatus;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.statusLabel;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.tableOf;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.toLong;
import static pt.ul.fc.css.tascaeats.ui.UiUtils.warn;

public class PedidosPane {

    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};

    private final AppState state;
    private final Stage    owner;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private Label          statusLabel;

    public PedidosPane(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public VBox build() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TableView<Map<String, Object>> table = tableOf(display,
            col("ID", "id"), col("Cliente", "clienteNome"), col("Restaurante", "restNome"),
            col("Estado", "estado"), col("Entrega", "estadoEntrega"), col("Total (€)", "total"),
            col("Pagamento", "pagamento"), col("Entregador", "entregador")
        );

        TextArea details = new TextArea();
        details.setEditable(false); details.setPrefRowCount(4);
        details.setPromptText("Selecione um pedido para ver detalhes.");
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) details.setText(
                "ID: " + n.get("id") + "\nCliente: " + n.get("clienteNome") +
                "\nRestaurante: " + n.get("restNome") + "\nEstado: " + n.get("estado") +
                "\nEstado da entrega: " + n.get("estadoEntrega") +
                "\nTotal: " + n.get("total") + "€\nPagamento: " + n.get("pagamento") +
                "\nDados do cliente: " + n.get("detalhesPagamentoSolicitado") +
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
        atribBtn.setOnAction(e      -> handleAtribuirEntregador(table, applyFilter));
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
        state.bg(this::loadPedidos, load -> {
            if (state.users.isEmpty()) {
                state.users.setAll(load.users());
            }
            if (state.restaurantes.isEmpty()) {
                state.restaurantes.setAll(load.restaurantes());
            }
            List<Map<String, Object>> typed = (List<Map<String, Object>>) (List<?>) load.pedidos();
            state.pedidos.setAll(typed.stream().map(this::normalizePedido).toList());
            if (afterUpdate != null) afterUpdate.run();
            String source = load.fallbackUsado() ? " via REST" : "";
            setStatus(statusLabel, "✓ " + state.pedidos.size() + " pedidos carregados" + source + ".");
        }, error -> setStatus(statusLabel, "✗ Erro ao carregar pedidos: " + error.getMessage()));
    }

    private PedidoLoadResult loadPedidos() throws Exception {
        List<Map<String, Object>> users = state.users.isEmpty()
            ? state.utilizadorClient.listarUtilizadores()
            : List.copyOf(state.users);
        List<Map<String, Object>> restaurantes = state.restaurantes.isEmpty()
            ? state.grpc.invokeList("listRestaurants", Map.of())
            : List.copyOf(state.restaurantes);

        NativeGrpcClient.GrpcResult res = state.grpc.invoke("listOrders", Map.of());
        if (res.success()) {
            return new PedidoLoadResult(state.grpc.readList(res.bodyJson()), false, users, restaurantes);
        }

        Exception grpcError = new IllegalStateException(res.message());
        for (String baseUrl : restBaseUrlCandidates()) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/pedidos"))
                    .GET()
                    .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return new PedidoLoadResult(mapper.readValue(response.body(), LIST_TYPE), true, users, restaurantes);
                }
            } catch (Exception ignored) {
                // Try the next configured URL.
            }
        }
        throw grpcError;
    }

    private List<String> restBaseUrlCandidates() {
        String configured = System.getenv().getOrDefault("NATIVE_REST_URL",
            System.getenv().getOrDefault("BACKEND_API_URL", ""));
        List<String> urls = new java.util.ArrayList<>();
        addBaseUrl(urls, configured);
        addBaseUrl(urls, "http://localhost:8080");
        addBaseUrl(urls, "http://localhost:80");
        return urls;
    }

    private void addBaseUrl(List<String> urls, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!urls.contains(normalized)) {
            urls.add(normalized);
        }
    }

    private record PedidoLoadResult(
        List<Map<String, Object>> pedidos,
        boolean fallbackUsado,
        List<Map<String, Object>> users,
        List<Map<String, Object>> restaurantes) {}

    // ── Handlers ──────────────────────────────────────────────────────────

    // K – Payment
    private void handlePagamento(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!"CRIADO".equals(s(sel, "estado"))) { warn("Só pedidos CRIADO podem ser pagos.", owner); return; }
        String metodo = s(sel, "tipoPagamentoSolicitado").toUpperCase();
        if (metodo.isBlank() || "-".equals(metodo)) {
            warn("Este pedido nao tem metodo de pagamento escolhido pelo cliente no checkout.", owner);
            return;
        }

        criarDialogoPagamento(sel, metodo).showAndWait().ifPresent(detalhes -> {
            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("pedidoId",      toLong(sel.get("id")));
                payload.put("tipoPagamento", metodo);
                payload.put("detalhes",      detalhes);
                NativeGrpcClient.GrpcResult res = state.grpc.invoke("processPayment", payload);
                if (!res.success()) {
                    warn("✗ Erro ao processar pagamento: " + res.message(), owner);
                    return;
                }
                info("✓ Pagamento via " + metodo + " processado.", owner);
                syncPedidos(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao processar pagamento: " + ex.getMessage(), owner); }
        });
    }

    private Dialog<Map<String, Object>> criarDialogoPagamento(Map<String, Object> pedido, String metodo) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Processar Pagamento");
        dialog.setHeaderText("Metodo bloqueado pelo cliente: " + metodo);
        dialog.initOwner(owner);

        ButtonType processar = new ButtonType("Processar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(processar, ButtonType.CANCEL);

        Map<String, String> detalhesCliente = detalhesPagamentoCliente(pedido.get("detalhesPagamentoSolicitado"));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        Map<String, Control> controls = new LinkedHashMap<>();
        int row = 0;
        grid.add(lbl("Pedido:"), 0, row);
        grid.add(lbl("#" + pedido.get("id") + " · Total " + s(pedido, "total") + "€"), 1, row++);

        switch (metodo) {
            case "MBWAY" -> row = addTextField(grid, controls, row, "numeroTelemovel", "Telemovel",
                    detalhesCliente.getOrDefault("numeroTelemovel", ""));
            case "MULTIBANCO" -> {
                row = addTextField(grid, controls, row, "entidade", "Entidade",
                        detalhesCliente.getOrDefault("entidade", "11111"));
                row = addTextField(grid, controls, row, "referencia", "Referencia",
                        detalhesCliente.getOrDefault("referencia", "123456789"));
                ComboBox<String> bandeira = combo("MULTIBANCO", "VISA", "MASTERCARD", "AMEX");
                bandeira.setValue(detalhesCliente.getOrDefault("bandeiraCartao", "MULTIBANCO"));
                controls.put("bandeiraCartao", bandeira);
                grid.add(lbl("Bandeira:"), 0, row);
                grid.add(bandeira, 1, row++);
            }
            case "DINHEIRO" -> row = addTextField(grid, controls, row, "valorRecebido", "Valor recebido",
                    detalhesCliente.getOrDefault("valorRecebido", s(pedido, "total")));
            case "CARTAO" -> row = addTextField(grid, controls, row, "numeroCartao", "Numero do cartao",
                    detalhesCliente.getOrDefault("numeroCartao", ""));
            default -> grid.add(lbl("Metodo nao suportado pela GUI: " + metodo), 0, row, 2, 1);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != processar) {
                return null;
            }
            Map<String, Object> detalhes = new LinkedHashMap<>();
            controls.forEach((campo, control) -> {
                if (control instanceof TextField textField) {
                    String value = textField.getText() == null ? "" : textField.getText().trim();
                    if ("DINHEIRO".equals(metodo) && "valorRecebido".equals(campo) && value.isBlank()) {
                        value = s(pedido, "total");
                    }
                    detalhes.put(campo, isNumericPaymentField(campo) ? normalizeDecimal(value) : value);
                } else if (control instanceof ComboBox<?> comboBox) {
                    detalhes.put(campo, comboBox.getValue());
                }
            });
            return detalhes;
        });
        return dialog;
    }

    private int addTextField(GridPane grid, Map<String, Control> controls, int row, String key, String label, String value) {
        TextField field = field("", 180);
        field.setText(value);
        controls.put(key, field);
        grid.add(lbl(label + ":"), 0, row);
        grid.add(field, 1, row);
        return row + 1;
    }

    private boolean isNumericPaymentField(String campo) {
        return "valorRecebido".equals(campo);
    }

    private String normalizeDecimal(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
            .replace("€", "")
            .replace(" ", "")
            .replace(",", ".");
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
            info("✓ Pedido pronto e entregador atribuído automaticamente.", owner);
            syncPedidos(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao marcar pronto: " + ex.getMessage(), owner); }
    }

    // M – Assign driver
    private void handleAtribuirEntregador(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!"PRONTO_PARA_ENTREGA".equals(s(sel, "estado"))) {
            warn("Pedido deve estar PRONTO_PARA_ENTREGA.", owner); return;
        }
        try {
            patch("/entregas/pedido/" + sel.get("id") + "/atribuir-entregador");
            info("✓ Entregador atribuído automaticamente.", owner);
            syncPedidos(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao atribuir entregador: " + ex.getMessage(), owner); }
    }

    // M – Register pickup
    private void handleRecolha(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!"ATRIBUIDO_ENTREGADOR".equals(s(sel, "estado"))) {
            warn("Pedido deve ter entregador atribuído para registar recolha.", owner); return;
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
        if (!"A_CAMINHO".equals(s(sel, "estadoEntrega"))) {
            warn("Pedido deve ter recolha registada para finalizar a entrega.", owner); return;
        }
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

    private void patch(String path) {
        String operation = path.contains("/cancelar") ? "cancelOrder" : "deliveryPatch";
        Map<String, Object> payload = path.contains("/cancelar")
            ? Map.of("pedidoId", path.split("/")[2])
            : Map.of("path", path);
        NativeGrpcClient.GrpcResult res = state.grpc.invoke(operation, payload);
        if (!res.success()) {
            throw new IllegalStateException(res.message());
        }
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
        m.put("estadoEntrega", p.getOrDefault("estadoEntregaReadOnly", "-"));
        m.put("total",       p.getOrDefault("valorTotal", p.getOrDefault("total", "0.00")));
        m.put("tipoPagamentoSolicitado", p.getOrDefault("tipoPagamentoSolicitado", "-"));
        m.put("detalhesPagamentoSolicitado", p.getOrDefault("detalhesPagamentoSolicitado", Map.of()));
        m.put("pagamento",   p.getOrDefault("tipoPagamentoSolicitado", p.getOrDefault("tipoPagamento", p.getOrDefault("pagamento", "-"))));
        Object entregadorId = p.get("entregadorId");
        String entregadorNome = state.users.stream()
            .filter(u -> entregadorId != null && entregadorId.toString().equals(Objects.toString(u.get("id"))))
            .findFirst().map(u -> s(u, "nome")).orElse(Objects.toString(entregadorId, "-"));
        m.put("entregador",  p.getOrDefault("entregador", entregadorNome));
        m.put("entregadorId", entregadorId);
        return m;
    }

    private Map<String, String> detalhesPagamentoCliente(Object raw) {
        Map<String, String> detalhes = new LinkedHashMap<>();
        if (raw instanceof Map<?, ?> map) {
            map.forEach((key, value) -> {
                if (key != null && value != null) {
                    detalhes.put(String.valueOf(key), String.valueOf(value));
                }
            });
        }
        return detalhes;
    }
}
