// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################


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
import java.util.function.Consumer;

import static pt.ul.fc.css.tascaeats.ui.UiUtils.*;

public class RestaurantesPane {

    private final AppState      state;
    private final Stage         owner;
    private final Consumer<Map<String, Object>> onViewProdutos;
    private Label statusLabel;

    public RestaurantesPane(AppState state, Stage owner, Consumer<Map<String, Object>> onViewProdutos) {
        this.state          = state;
        this.owner          = owner;
        this.onViewProdutos = onViewProdutos;
    }

    public VBox build() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TableView<Map<String, Object>> table = tableOf(display,
            col("ID", "id"), col("Nome", "nome"), col("Estado", "estado"),
            col("Tipo", "tipo"), col("NIF", "nif")
        );

        TextArea details = new TextArea();
        details.setEditable(false); details.setPrefRowCount(4);
        details.setPromptText("Selecione um restaurante para ver detalhes.");
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) details.setText(
                "ID: " + n.get("id") + "\nNome: " + n.get("nome") +
                "\nEstado: " + n.get("estado") + "\nTipo: " + n.get("tipo") + "\nNIF: " + n.get("nif"));
        });

        // E – Search
        TextField        nomeF  = field("Nome do restaurante...", 200);
        ComboBox<String> estBox = combo("Todos", "ABERTO", "FECHADO", "RESERVADO");

        Runnable applyFilter = () -> {
            String q = nomeF.getText().toLowerCase();
            String e = estBox.getValue();
            display.setAll(state.restaurantes.stream()
                .filter(r -> q.isBlank() || s(r, "nome").toLowerCase().contains(q))
                .filter(r -> "Todos".equals(e) || e.equals(s(r, "estado")))
                .toList());
        };

        Button pesquisar = new Button("Pesquisar"); pesquisar.setDefaultButton(true);
        pesquisar.setOnAction(e -> applyFilter.run());
        Button limpar = new Button("Limpar");
        limpar.setOnAction(e -> { nomeF.clear(); estBox.getSelectionModel().selectFirst(); applyFilter.run(); });

        // F – Management
        Button criarBtn   = colorBtn("+ Criar",          "#4caf50");
        Button editarBtn  = colorBtn("✎ Editar",          "#2196f3");
        Button estadoBtn  = colorBtn("⟳ Alterar Estado",  "#ff9800");
        Button removerBtn = colorBtn("✕ Remover",         "#f44336");
        Button prodBtn    = colorBtn("🍽 Ver Produtos",    "#7b1fa2");

        boolean isAdmin = state.isAdmin();
        criarBtn.setDisable(!isAdmin); editarBtn.setDisable(!isAdmin);
        estadoBtn.setDisable(!isAdmin); removerBtn.setDisable(!isAdmin);

        criarBtn.setOnAction(e   -> handleCriar(applyFilter));
        editarBtn.setOnAction(e  -> handleEditar(table, applyFilter));
        estadoBtn.setOnAction(e  -> handleEstado(table, applyFilter));
        removerBtn.setOnAction(e -> handleRemover(table, applyFilter));
        prodBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table, owner);
            if (sel != null) onViewProdutos.accept(sel);
        });

        statusLabel = statusLabel("A carregar restaurantes...");
        syncRestaurantes(applyFilter);

        return pane(sectionTitle("E/F – Restaurantes"),
            hbar(lbl("Nome:"), nomeF, lbl("Estado:"), estBox, pesquisar, limpar),
            hbar(criarBtn, editarBtn, estadoBtn, removerBtn, prodBtn),
            table, new Label("Detalhes:"), details, statusLabel);
    }

    // ── Sync ─────────────────────────────────────────────────────────────

    public void syncRestaurantes(Runnable afterUpdate) {
        state.bg(() -> {
            try {
                ResponseEntity<List<Map<String, Object>>> resp = state.rest.exchange(
                    AppState.BACKEND + "/api/restaurantes", HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
                return resp.getBody() != null ? resp.getBody() : List.of();
            } catch (Exception e) { return List.of(); }
        }, result -> {
            List<Map<String, Object>> typed = (List<Map<String, Object>>) (List<?>) result;
            state.restaurantes.setAll(typed.stream().map(this::normalize).toList());
            if (afterUpdate != null) afterUpdate.run();
            setStatus(statusLabel, result.isEmpty()
                ? "⚠ Sem dados (backend indisponível?)"
                : "✓ " + result.size() + " restaurantes carregados.");
        });
    }

    // ── Handlers ──────────────────────────────────────────────────────────

    private void handleCriar(Runnable applyFilter) {
        Map<String, Object> novo = restauranteDialog(null);
        if (novo == null) return;
        String adminEmail = s(state.currentUser, "email");
        if (adminEmail.isBlank()) { warn("Não foi possível determinar o email do administrador.", owner); return; }
        try {
            state.rest.postForEntity(AppState.BACKEND + "/api/restaurantes?adminEmail=" + adminEmail,
                buildPayload(novo), Map.class);
            info("✓ Restaurante criado.", owner);
            syncRestaurantes(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao criar: " + ex.getMessage(), owner); }
    }

    private void handleEditar(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        Map<String, Object> edited = restauranteDialog(sel); if (edited == null) return;
        try {
            state.rest.exchange(AppState.BACKEND + "/api/restaurantes/" + sel.get("id"),
                HttpMethod.PUT, new HttpEntity<>(buildPayload(edited)), Map.class);
            info("✓ Restaurante atualizado.", owner);
            syncRestaurantes(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao editar: " + ex.getMessage(), owner); }
    }

    private void handleEstado(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        ChoiceDialog<String> cd = new ChoiceDialog<>(s(sel, "estado"), "ABERTO", "FECHADO", "RESERVADO");
        cd.setTitle("Alterar Estado"); cd.setHeaderText("Novo estado:"); cd.initOwner(owner);
        cd.showAndWait().ifPresent(st -> {
            try {
                state.rest.exchange(AppState.BACKEND + "/api/restaurantes/" + sel.get("id") + "/estado",
                    HttpMethod.PATCH, new HttpEntity<>(Map.of("estado", st)), Map.class);
                syncRestaurantes(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao alterar estado: " + ex.getMessage(), owner); }
        });
    }

    private void handleRemover(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!confirm("Remover " + s(sel, "nome") + "?", owner)) return;
        try {
            state.rest.delete(AppState.BACKEND + "/api/restaurantes/" + sel.get("id"));
            info("✓ Restaurante removido.", owner);
            syncRestaurantes(applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao remover: " + ex.getMessage(), owner); }
    }

    // ── Payload / Dialog / Normalize ──────────────────────────────────────

    private Map<String, Object> buildPayload(Map<String, Object> form) {
        Map<String, Object> morada = new LinkedHashMap<>();
        morada.put("rua",          s(form, "rua").isBlank()    ? "Rua Principal" : s(form, "rua"));
        morada.put("numero",       form.get("numero") != null  ? ((Number) form.get("numero")).intValue() : 1);
        morada.put("complemento",  s(form, "complemento"));
        morada.put("codigoPostal", s(form, "codigoPostal").isBlank() ? "1000-001" : s(form, "codigoPostal"));
        morada.put("cidade",       s(form, "cidade").isBlank() ? "Lisboa" : s(form, "cidade"));
        morada.put("distrito",     s(form, "distrito").isBlank() ? "Lisboa" : s(form, "distrito"));
        morada.put("pais",         s(form, "pais").isBlank()   ? "Portugal" : s(form, "pais"));

        Map<String, Object> p = new LinkedHashMap<>();
        p.put("nome",   s(form, "nome"));
        p.put("nif",    s(form, "nif"));
        p.put("morada", morada);
        return p;
    }

    private Map<String, Object> normalize(Map<String, Object> r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",     r.getOrDefault("id", "?"));
        m.put("nome",   r.getOrDefault("nome", ""));
        m.put("estado", r.getOrDefault("estado", ""));
        m.put("tipo",   r.getOrDefault("tipoRestaurante", r.getOrDefault("tipoCozinha", "OUTRO")));
        m.put("nif",    r.getOrDefault("nif", "-"));
        m.putAll(r); // keep original fields for editing (morada, etc.)
        return m;
    }

    private Map<String, Object> restauranteDialog(Map<String, Object> ex) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Criar Restaurante" : "Editar Restaurante", owner);

        TextField nomeF     = new TextField(s(ex, "nome"));
        TextField nifF      = new TextField(s(ex, "nif"));
        TextField ruaF      = new TextField(s(ex, "rua"));
        TextField numF      = new TextField(ex != null ? String.valueOf(ex.getOrDefault("numero", "1")) : "1");
        TextField cpF       = new TextField(s(ex, "codigoPostal"));
        TextField cidadeF   = new TextField(s(ex, "cidade"));
        TextField distritoF = new TextField(s(ex, "distrito"));
        TextField tipoF     = new TextField(s(ex, "tipo"));

        GridPane g = formGrid();
        g.add(lbl("Nome:"),        0, 0); g.add(nomeF,     1, 0);
        g.add(lbl("NIF:"),         0, 1); g.add(nifF,      1, 1);
        g.add(lbl("Rua:"),         0, 2); g.add(ruaF,      1, 2);
        g.add(lbl("Número:"),      0, 3); g.add(numF,      1, 3);
        g.add(lbl("Cód. Postal:"), 0, 4); g.add(cpF,       1, 4);
        g.add(lbl("Cidade:"),      0, 5); g.add(cidadeF,   1, 5);
        g.add(lbl("Distrito:"),    0, 6); g.add(distritoF, 1, 6);
        g.add(lbl("Tipo:"),        0, 7); g.add(tipoF,     1, 7);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            int num = 1;
            try { num = Integer.parseInt(numF.getText().trim()); } catch (NumberFormatException ignored) {}
            return row(
                "nome", nomeF.getText().trim(), "nif", nifF.getText().trim(),
                "rua", ruaF.getText().trim(), "numero", num,
                "codigoPostal", cpF.getText().trim(), "cidade", cidadeF.getText().trim(),
                "distrito", distritoF.getText().trim(), "pais", "Portugal",
                "complemento", "", "tipo", tipoF.getText().trim().isBlank() ? "OUTRO" : tipoF.getText().trim()
            );
        });
        return dlg.showAndWait().orElse(null);
    }
}
