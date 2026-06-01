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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;

import java.util.Map;

import static pt.ul.fc.css.tascaeats.ui.UiUtils.*;


public class UtilizadoresPane {

    private final AppState state;
    private final Stage    owner;
    private Label          statusLabel;

    public UtilizadoresPane(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public VBox build() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TableView<Map<String, Object>> table = tableOf(display,
            col("ID", "id"), col("Nome", "nome"), col("Email", "email"),
            col("Papel", "papel"), col("Ativo", "ativo"), col("Disponível", "disponivel")
        );

        TextField        searchF  = field("Filtrar por nome...", 180);
        ComboBox<String> papelBox = combo("Todos", "CLIENT", "ADMIN", "COURIER");

        Runnable applyFilter = () -> {
            String q = searchF.getText().toLowerCase();
            String p = papelBox.getValue();
            display.setAll(state.users.stream()
                .filter(u -> q.isBlank() || s(u, "nome").toLowerCase().contains(q))
                .filter(u -> "Todos".equals(p) || p.equals(s(u, "papel")))
                .toList());
        };
        Button filtrar = new Button("Filtrar");
        filtrar.setOnAction(e -> applyFilter.run());

        Button criarBtn   = colorBtn("+ Registar Admin/Entregador", "#4caf50");
        Button editarBtn  = colorBtn("✎ Editar",  "#2196f3");
        Button removerBtn = colorBtn("✕ Remover", "#f44336");

        editarBtn.setDisable(!state.isAdmin());
        removerBtn.setDisable(!state.isAdmin());

        criarBtn.setOnAction(e -> handleCriar(applyFilter));
        editarBtn.setOnAction(e -> handleEditar(table, applyFilter));
        removerBtn.setOnAction(e -> handleRemover(table, applyFilter));

        statusLabel = statusLabel("A carregar utilizadores...");
        syncUsers(applyFilter);

        return pane(sectionTitle("C/D – Gestão de Utilizadores"),
            hbar(lbl("Nome:"), searchF, lbl("Papel:"), papelBox, filtrar),
            hbar(criarBtn, editarBtn, removerBtn),
            table, statusLabel);
    }

    // ── Sync ─────────────────────────────────────────────────────────────

    public void syncUsers(Runnable afterUpdate) {
        state.bg(() -> state.utilizadorClient.listarUtilizadores(), result -> {
            state.users.setAll(result);
            if (afterUpdate != null) afterUpdate.run();
            setStatus(statusLabel, result.isEmpty()
                ? "⚠ Sem dados (backend indisponível?)"
                : "✓ " + result.size() + " utilizadores carregados.");
        });
    }

    // ── Handlers ──────────────────────────────────────────────────────────

    private void handleCriar(Runnable applyFilter) {
        Map<String, Object> novo = userDialog(null);
        if (novo == null) return;
        String papel = s(novo, "papel");
        if ("CLIENT".equals(papel)) { warn("Use Case C: só pode registar Admins e Entregadores aqui.", owner); return; }
        String tipo = mapPapelToTipo(papel);
        UtilizadorApiClient.RegistoResult res = state.utilizadorClient.registarUtilizador(
            s(novo, "nome"), s(novo, "email"), s(novo, "password"), tipo);
        if (res.fallbackUsado()) {
            warn("⚠ " + res.mensagem(), owner);
        } else if (res.id() != null) {
            info("✓ " + res.mensagem(), owner);
            syncUsers(applyFilter);
        } else {
            warn("✗ " + res.mensagem(), owner);
        }
    }

    private void handleEditar(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner);
        if (sel == null) return;
        Map<String, Object> edited = userDialog(sel);
        if (edited == null) return;
        UtilizadorApiClient.SimpleResult res = state.utilizadorClient.atualizarUtilizador(
            sel.get("id"), s(edited, "nome"), s(edited, "email"), s(edited, "password"));
        if (res.sucesso()) { info("✓ " + res.mensagem(), owner); syncUsers(applyFilter); }
        else               { warn("✗ " + res.mensagem(), owner); }
    }

    private void handleRemover(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner);
        if (sel == null) return;
        if (!confirm("Remover " + s(sel, "nome") + "?", owner)) return;
        UtilizadorApiClient.SimpleResult res = state.utilizadorClient.removerUtilizador(sel.get("id"));
        if (res.sucesso()) { info("✓ " + res.mensagem(), owner); syncUsers(applyFilter); }
        else               { warn("✗ " + res.mensagem(), owner); }
    }

    // ── Dialog ────────────────────────────────────────────────────────────

    private Map<String, Object> userDialog(Map<String, Object> ex) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Registar Utilizador" : "Editar Utilizador", owner);

        TextField     nomeF  = new TextField(s(ex, "nome"));
        TextField     emailF = new TextField(s(ex, "email"));
        PasswordField passF  = new PasswordField();
        ComboBox<String> papelBox = combo("ADMIN", "COURIER");
        if (ex != null) papelBox.setValue(s(ex, "papel"));

        GridPane g = formGrid();
        g.add(lbl("Nome:"),     0, 0); g.add(nomeF,    1, 0);
        g.add(lbl("Email:"),    0, 1); g.add(emailF,   1, 1);
        g.add(lbl("Password:"), 0, 2); g.add(passF,    1, 2);
        g.add(lbl("Papel:"),    0, 3); g.add(papelBox, 1, 3);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> b == ButtonType.OK ? row(
            "nome",     nomeF.getText().trim(),
            "email",    emailF.getText().trim(),
            "password", passF.getText().trim(),
            "papel",    papelBox.getValue()
        ) : null);
        return dlg.showAndWait().orElse(null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String mapPapelToTipo(String papel) {
        return switch (papel) {
            case "ADMIN"   -> "restaurant owner";
            case "COURIER" -> "driver";
            default        -> "client";
        };
    }
}
