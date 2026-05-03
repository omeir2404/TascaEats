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

import static pt.ul.fc.css.tascaeats.ui.UiUtils.*;

public class ProdutosPane {

    private final AppState state;
    private final Stage    owner;
    private final Runnable onVoltar;
    private Label          statusLabel;

    public ProdutosPane(AppState state, Stage owner, Runnable onVoltar) {
        this.state    = state;
        this.owner    = owner;
        this.onVoltar = onVoltar;
    }

    public VBox build(Map<String, Object> restFilter) {
        ObservableList<String> restNames = FXCollections.observableArrayList("Todos");
        state.restaurantes.forEach(r -> restNames.add(s(r, "nome")));
        ComboBox<String> restBox = new ComboBox<>(restNames);
        restBox.getSelectionModel().selectFirst();
        if (restFilter != null) restBox.setValue(s(restFilter, "nome"));

        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TextField        searchF   = field("Nome do produto...", 150);
        TextField        precoMaxF = field("Preço máx.", 80);
        ComboBox<String> dispBox   = combo("Todos", "Sim", "Não");
        ComboBox<String> apagBox   = combo("Todos", "Sim", "Não");

        Runnable applyFilter = () -> {
            String rn   = restBox.getValue();
            String q    = searchF.getText().toLowerCase();
            String disp = dispBox.getValue();
            String apag = apagBox.getValue();
            Double maxP;
            try { maxP = Double.parseDouble(precoMaxF.getText().trim()); }
            catch (Exception ex) { maxP = null; }
            final Double fMax = maxP;
            display.setAll(state.produtos.stream()
                .filter(p -> "Todos".equals(rn) || rn.equals(s(p, "restNome")))
                .filter(p -> q.isBlank() || s(p, "nome").toLowerCase().contains(q))
                .filter(p -> "Todos".equals(disp) || disp.equals(s(p, "disponivel")))
                .filter(p -> "Todos".equals(apag) || apag.equals(s(p, "apagado")))
                .filter(p -> fMax == null || toDouble(p.get("preco")) <= fMax)
                .toList());
        };

        restBox.setOnAction(e -> {
            String chosen = restBox.getValue();
            if ("Todos".equals(chosen)) syncProdutos(null, applyFilter);
            else state.restaurantes.stream()
                .filter(r -> s(r, "nome").equals(chosen)).findFirst()
                .ifPresent(r -> syncProdutos(toLong(r.get("id")), applyFilter));
        });

        TableView<Map<String, Object>> table = tableOf(display,
            col("ID", "id"), col("Restaurante", "restNome"), col("Nome", "nome"),
            col("Preço (€)", "preco"), col("Disponível", "disponivel"), col("Apagado", "apagado")
        );

        Button filtrar   = new Button("Pesquisar"); filtrar.setOnAction(e -> applyFilter.run());
        Button criarBtn  = colorBtn("+ Criar Produto",  "#4caf50");
        Button editarBtn = colorBtn("✎ Editar",          "#2196f3");
        Button dispBtn   = colorBtn("⟳ Disp./Indisp.",  "#ff9800");
        Button apagarBtn = colorBtn("✕ Apagar",         "#f44336");

        criarBtn.setOnAction(e  -> handleCriar(restBox, applyFilter));
        editarBtn.setOnAction(e -> handleEditar(table, applyFilter));
        dispBtn.setOnAction(e   -> handleDisponibilidade(table, applyFilter));
        apagarBtn.setOnAction(e -> handleApagar(table, applyFilter));

        Button voltarBtn = new Button("← Voltar a Restaurantes");
        voltarBtn.setOnAction(e -> onVoltar.run());

        statusLabel = statusLabel("Selecione um restaurante para carregar produtos.");

        if (restFilter != null) syncProdutos(toLong(restFilter.get("id")), applyFilter);
        else syncProdutos(null, applyFilter);

        return pane(sectionTitle("G/H – Gestão de Produtos"),
            hbar(lbl("Restaurante:"), restBox, lbl("Nome:"), searchF,
                 lbl("Preço≤"), precoMaxF, lbl("Disp:"), dispBox, lbl("Apagado:"), apagBox, filtrar),
            hbar(criarBtn, editarBtn, dispBtn, apagarBtn, new Region(), voltarBtn),
            table, statusLabel);
    }

    // ── Sync ─────────────────────────────────────────────────────────────

    public void syncProdutos(Long restauranteId, Runnable afterUpdate) {
        state.bg(() -> {
            try {
                String url = restauranteId != null
                    ? AppState.BACKEND + "/api/produtos?restauranteId=" + restauranteId
                    : AppState.BACKEND + "/api/produtos";
                ResponseEntity<List<Map<String, Object>>> resp = state.rest.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
                return resp.getBody() != null ? resp.getBody() : List.of();
            } catch (Exception e) { return List.of(); }
        }, result -> {
            List<Map<String, Object>> typed = (List<Map<String, Object>>) (List<?>) result;
            List<Map<String, Object>> enriched = typed.stream().map(p -> {
                Map<String, Object> m = new LinkedHashMap<>(p);
                Long rid = toLong(p.getOrDefault("restauranteId", p.getOrDefault("restId", -1)));
                m.put("restId", rid);
                m.put("restNome", state.restaurantes.stream()
                    .filter(r -> rid.equals(toLong(r.get("id"))))
                    .findFirst().map(r -> s(r, "nome")).orElse("-"));
                m.put("disponivel", Boolean.TRUE.equals(p.get("disponivel")) ? "Sim" : "Não");
                m.put("apagado", Boolean.TRUE.equals(p.get("apagado")) ? "Sim" : "Não");
                return m;
            }).toList();

            if (restauranteId != null) {
                state.produtos.removeIf(p -> restauranteId.equals(toLong(p.get("restId"))));
                state.produtos.addAll(enriched);
            } else {
                state.produtos.setAll(enriched);
            }
            if (afterUpdate != null) afterUpdate.run();
            setStatus(statusLabel, "✓ " + state.produtos.size() + " produtos carregados.");
        });
    }

    // ── Handlers ──────────────────────────────────────────────────────────

    private void handleCriar(ComboBox<String> restBox, Runnable applyFilter) {
        String rNome = restBox.getValue();
        Map<String, Object> preRest = "Todos".equals(rNome) ? null :
            state.restaurantes.stream().filter(r -> s(r, "nome").equals(rNome)).findFirst().orElse(null);
        if (preRest == null) { warn("Selecione um restaurante específico para criar um produto.", owner); return; }
        Map<String, Object> novo = produtoDialog(null, preRest);
        if (novo == null) return;
        Long restId = toLong(preRest.get("id"));
        try {
            state.rest.postForEntity(AppState.BACKEND + "/api/restaurantes/" + restId + "/produtos",
                buildCriarPayload(novo), Map.class);
            info("✓ Produto criado.", owner);
            syncProdutos(restId, applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao criar produto: " + ex.getMessage(), owner); }
    }

    private void handleEditar(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        Map<String, Object> edited = produtoDialog(sel, null); if (edited == null) return;
        try {
            // AtualizarProdutoDTO: {id, nome, preco, descricao}
            state.rest.exchange(AppState.BACKEND + "/api/produtos/" + sel.get("id"),
                HttpMethod.PUT, new HttpEntity<>(buildAtualizarPayload(edited, sel.get("id"))), Map.class);
            info("✓ Produto atualizado.", owner);
            syncProdutos(toLong(sel.get("restId")), applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao editar: " + ex.getMessage(), owner); }
    }

    private void handleDisponibilidade(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        boolean novaDisp = "Não".equals(s(sel, "disponivel"));
        try {
            state.rest.exchange(AppState.BACKEND + "/api/produtos/" + sel.get("id") + "/disponibilidade",
                HttpMethod.PATCH, new HttpEntity<>(Map.of("disponivel", novaDisp)), Map.class);
            info("✓ Disponibilidade atualizada.", owner);
            syncProdutos(toLong(sel.get("restId")), applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao atualizar disponibilidade: " + ex.getMessage(), owner); }
    }

    private void handleApagar(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner); if (sel == null) return;
        if (!confirm("Apagar " + s(sel, "nome") + "?", owner)) return;
        try {
            state.rest.delete(AppState.BACKEND + "/api/produtos/" + sel.get("id"));
            info("✓ Produto apagado.", owner);
            syncProdutos(toLong(sel.get("restId")), applyFilter);
        } catch (Exception ex) { warn("✗ Erro ao apagar: " + ex.getMessage(), owner); }
    }

    // ── Payloads ──────────────────────────────────────────────────────────

    private Map<String, Object> buildCriarPayload(Map<String, Object> form) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("nome",     s(form, "nome"));
        p.put("preco",    toDouble(form.get("preco")));
        p.put("descricao", s(form, "descricao").isBlank() ? s(form, "nome") : s(form, "descricao"));
        return p;
    }

    private Map<String, Object> buildAtualizarPayload(Map<String, Object> form, Object id) {
        // AtualizarProdutoDTO: {id, nome, preco, descricao}
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id",       id);
        p.put("nome",     s(form, "nome"));
        p.put("preco",    toDouble(form.get("preco")));
        p.put("descricao", s(form, "descricao").isBlank() ? s(form, "nome") : s(form, "descricao"));
        return p;
    }

    // ── Dialog ────────────────────────────────────────────────────────────

    private Map<String, Object> produtoDialog(Map<String, Object> ex, Map<String, Object> preRest) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Criar Produto" : "Editar Produto", owner);

        ObservableList<String> rNames = FXCollections.observableArrayList();
        state.restaurantes.forEach(r -> rNames.add(s(r, "nome")));
        ComboBox<String> restBox = new ComboBox<>(rNames);
        if (ex != null)           restBox.setValue(s(ex, "restNome"));
        else if (preRest != null) restBox.setValue(s(preRest, "nome"));
        else restBox.getSelectionModel().selectFirst();

        TextField nomeF  = new TextField(s(ex, "nome"));
        TextField precoF = new TextField(ex != null ? String.valueOf(ex.getOrDefault("preco", "")) : "");
        TextField descF  = new TextField(s(ex, "descricao")); descF.setPromptText("Descrição do produto");

        GridPane g = formGrid();
        g.add(lbl("Restaurante:"), 0, 0); g.add(restBox, 1, 0);
        g.add(lbl("Nome:"),        0, 1); g.add(nomeF,   1, 1);
        g.add(lbl("Preço (€):"),   0, 2); g.add(precoF,  1, 2);
        g.add(lbl("Descrição:"),   0, 3); g.add(descF,   1, 3);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            String rn  = restBox.getValue();
            Object rid = state.restaurantes.stream().filter(r -> s(r, "nome").equals(rn))
                .findFirst().map(r -> r.get("id")).orElse(-1);
            return row("restId", rid, "restNome", rn,
                "nome",      nomeF.getText().trim(),
                "preco",     precoF.getText().trim(),
                "descricao", descF.getText().trim());
        });
        return dlg.showAndWait().orElse(null);
    }
}
