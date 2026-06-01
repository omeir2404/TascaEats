package pt.ul.fc.css.tascaeats.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static pt.ul.fc.css.tascaeats.ui.UiUtils.*;

public class MenusPane {

    private final AppState state;
    private final Stage owner;
    private Label statusLabel;

    public MenusPane(AppState state, Stage owner) {
        this.state = state;
        this.owner = owner;
    }

    public VBox build() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TextField searchF = field("Nome do menu...", 220);

        Runnable applyFilter = () -> {
            String q = searchF.getText().toLowerCase();
            display.setAll(state.menus.stream()
                    .filter(menu -> q.isBlank() || s(menu, "nome").toLowerCase().contains(q))
                    .toList());
        };

        TableView<Map<String, Object>> table = tableOf(display,
                col("ID", "id"),
                col("Nome", "nome"),
                col("Produtos", "numeroProdutos"),
                col("Lojas", "numeroRestaurantes"),
                col("Restaurantes", "restauranteNome"),
                col("Preco medio", "precoMedio")
        );

        TextArea details = new TextArea();
        details.setEditable(false);
        details.setPrefRowCount(4);
        details.setPromptText("Selecione um menu para ver detalhes.");
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                details.setText(
                        "ID: " + selected.get("id") +
                        "\nNome: " + selected.get("nome") +
                        "\nProdutos: " + selected.get("numeroProdutos") +
                        "\nRestaurantes: " + selected.get("restauranteNome") +
                        "\nPreco medio: " + selected.get("precoMedio")
                );
            }
        });

        Button pesquisarBtn = new Button("Pesquisar");
        Button limparBtn = new Button("Limpar");
        Button criarBtn = colorBtn("+ Criar Menu", "#4caf50");
        Button editarBtn = colorBtn("Editar", "#2196f3");
        Button removerBtn = colorBtn("Remover", "#f44336");

        pesquisarBtn.setOnAction(e -> applyFilter.run());
        limparBtn.setOnAction(e -> {
            searchF.clear();
            applyFilter.run();
        });
        criarBtn.setOnAction(e -> handleCriar(applyFilter));
        editarBtn.setOnAction(e -> handleEditar(table, applyFilter));
        removerBtn.setOnAction(e -> handleRemover(table, applyFilter));

        boolean isAdmin = state.isAdmin();
        criarBtn.setDisable(!isAdmin);
        editarBtn.setDisable(!isAdmin);
        removerBtn.setDisable(!isAdmin);

        statusLabel = statusLabel("A carregar menus...");
        syncMenus(applyFilter);

        return pane(sectionTitle("Gestao de Menus"),
                hbar(lbl("Nome:"), searchF, pesquisarBtn, limparBtn),
                hbar(criarBtn, editarBtn, removerBtn),
                table,
                new Label("Detalhes:"),
                details,
                statusLabel);
    }

    public void syncMenus(Runnable afterUpdate) {
        state.bg(() -> state.grpc.invoke("listMenus", Map.of()), result -> {
            if (!result.success()) {
                state.menus.clear();
                setStatus(statusLabel, "Erro gRPC ao carregar menus: " + result.message());
                return;
            }
            List<Map<String, Object>> typed = state.grpc.readList(result.bodyJson());
            state.menus.setAll(typed.stream().map(this::normalize).toList());
            if (afterUpdate != null) {
                afterUpdate.run();
            }
            setStatus(statusLabel, typed.isEmpty()
                    ? "Sem menus registados."
                    : typed.size() + " menus carregados.");
        });
    }

    private void handleCriar(Runnable applyFilter) {
        Map<String, Object> novo = menuDialog(null);
        if (novo == null) {
            return;
        }
        NativeGrpcClient.GrpcResult res = state.grpc.invoke("createMenu", buildPayload(novo));
        if (res.success()) {
            info("Menu criado.", owner);
            syncMenus(applyFilter);
        } else {
            warn("Erro ao criar menu: " + res.message(), owner);
        }
    }

    private void handleEditar(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner);
        if (sel == null) {
            return;
        }
        Map<String, Object> edited = menuDialog(sel);
        if (edited == null) {
            return;
        }
        Map<String, Object> payload = buildPayload(edited);
        payload.put("id", sel.get("id"));
        NativeGrpcClient.GrpcResult res = state.grpc.invoke("updateMenu", payload);
        if (res.success()) {
            info("Menu atualizado.", owner);
            syncMenus(applyFilter);
        } else {
            warn("Erro ao editar menu: " + res.message(), owner);
        }
    }

    private void handleRemover(TableView<Map<String, Object>> table, Runnable applyFilter) {
        Map<String, Object> sel = selected(table, owner);
        if (sel == null) {
            return;
        }
        if (!confirm("Remover o menu " + s(sel, "nome") + "?", owner)) {
            return;
        }
        NativeGrpcClient.GrpcResult res = state.grpc.invoke("deleteMenu", Map.of("id", sel.get("id")));
        if (res.success()) {
            info("Menu removido.", owner);
            syncMenus(applyFilter);
        } else {
            warn("Erro ao remover menu: " + res.message(), owner);
        }
    }

    private Map<String, Object> buildPayload(Map<String, Object> form) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nome", s(form, "nome"));
        return payload;
    }

    private Map<String, Object> normalize(Map<String, Object> menu) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", menu.getOrDefault("id", "?"));
        m.put("nome", menu.getOrDefault("nome", ""));
        m.put("numeroProdutos", menu.getOrDefault("numeroProdutos", 0));
        m.put("numeroRestaurantes", menu.getOrDefault("numeroRestaurantes", 0));
        m.put("restauranteNome", restaurantesTexto(menu));
        m.put("precoMedio", menu.getOrDefault("precoMedio", 0.0));
        m.putAll(menu);
        m.put("restauranteNome", restaurantesTexto(m));
        return m;
    }

    private String restaurantesTexto(Map<String, Object> menu) {
        Object nomes = menu.get("restaurantesNomes");
        if (nomes instanceof List<?> list && !list.isEmpty()) {
            return list.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("");
        }
        return s(menu, "restauranteNome").isBlank() ? "Sem restaurante atribuido" : s(menu, "restauranteNome");
    }

    private Map<String, Object> menuDialog(Map<String, Object> ex) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Criar Menu" : "Editar Menu", owner);
        TextField nomeF = new TextField(s(ex, "nome"));

        GridPane g = formGrid();
        g.add(lbl("Nome:"), 0, 0);
        g.add(nomeF, 1, 0);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b != ButtonType.OK) {
                return null;
            }
            return row("nome", nomeF.getText().trim());
        });
        return dlg.showAndWait().orElse(null);
    }
}
