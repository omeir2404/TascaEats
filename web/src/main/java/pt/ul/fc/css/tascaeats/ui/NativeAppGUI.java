package pt.ul.fc.css.tascaeats.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * TascaEats Native App — Use Cases A, C, D, E, F, G, H, K, L, M, N.
 * All data is fetched from the backend REST API.
 * Mock data is only used as a last-resort fallback when the backend is unreachable.
 */
public class NativeAppGUI extends Application {

    // ── Backend URL ───────────────────────────────────────────────────────
    private static final String BACKEND =
            System.getenv().getOrDefault("BACKEND_API_URL", "http://localhost:8080");

    // ── Live data lists (populated from backend) ──────────────────────────
    private final ObservableList<Map<String, Object>> users        = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> restaurantes = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> produtos     = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> pedidos      = FXCollections.observableArrayList();

    // ── Session ───────────────────────────────────────────────────────────
    private Map<String, Object> currentUser = null;

    // ── HTTP clients ──────────────────────────────────────────────────────
    private RestTemplate          rest;
    private UtilizadorApiClient   utilizadorClient;
    private RestauranteApiClient  restauranteClient;

    // ── UI ────────────────────────────────────────────────────────────────
    private Stage  mainStage;
    private VBox   contentArea;
    private Label  headerUserLabel;
    private Button navRestBtn, navProdBtn, navOrderBtn, exitBtn;

    // status labels (updated by background sync)
    private Label userStatusLabel;
    private Label restStatusLabel;
    private Label prodStatusLabel;
    private Label orderStatusLabel;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        rest              = new RestTemplate();
        utilizadorClient  = new UtilizadorApiClient(rest, BACKEND);
        restauranteClient = new RestauranteApiClient(rest, BACKEND);

        contentArea = new VBox();
        contentArea.setStyle("-fx-background-color: #f8f9fa;");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");
        root.setTop(buildNavHeader());
        root.setCenter(contentArea);

        stage.setTitle("TascaEats – Native App");
        stage.setScene(new Scene(root, 1400, 800));
        stage.setMinWidth(960);
        stage.setMinHeight(520);
        stage.show();

        showLoginPane();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────────────
    private HBox buildNavHeader() {
        HBox header = new HBox(8);
        header.setStyle("-fx-background-color: #ff5722;");
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("TascaEats");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerUserLabel = new Label("Não autenticado");
        headerUserLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11px;");

        exitBtn = new Button("Sair");
        exitBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6 14; -fx-background-color: #c62828; -fx-text-fill: white;");

        Button loginBtn = navBtn("A – Login",         this::showLoginPane);
        Button usersBtn = navBtn("C/D – Utilizadores", this::showUsersPane);
        navRestBtn  = navBtn("E/F – Restaurantes",  this::showRestaurantesPane);
        navProdBtn  = navBtn("G/H – Produtos",       () -> show(buildProductsPane(null)));
        navOrderBtn = navBtn("K/L/M/N – Pedidos",    this::showOrdersPane);

        updateNavState();

        header.getChildren().addAll(title, loginBtn, usersBtn,
                navRestBtn, navProdBtn, navOrderBtn, spacer, headerUserLabel, exitBtn);
        return header;
    }

    private Button navBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size: 11px; -fx-padding: 6 11; -fx-background-color: transparent;" +
                "-fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 4;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private void updateNavState() {
        boolean loggedIn = currentUser != null;
        navRestBtn.setDisable(!loggedIn);
        navProdBtn.setDisable(!loggedIn);
        navOrderBtn.setDisable(!loggedIn);
        exitBtn.setOnAction(e -> {
            if (loggedIn) {
                currentUser = null;
                headerUserLabel.setText("Não autenticado");
                updateNavState();
                showLoginPane();
            } else {
                Platform.exit();
            }
        });
        exitBtn.setText(loggedIn ? "Logout" : "Sair");
    }

    private void show(javafx.scene.Node node) { contentArea.getChildren().setAll(node); }
    private void showLoginPane()        { show(buildLoginPane()); }
    private void showUsersPane()        { show(buildUsersPane()); }
    private void showRestaurantesPane() { show(buildRestaurantesPane()); }
    private void showOrdersPane()       { show(buildOrdersPane()); }

    // ─────────────────────────────────────────────────────────────────────
    // A – Login (mock: any password accepted if user exists in backend)
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildLoginPane() {
        Label title = sectionTitle("A – Login");

        TextField     emailF = new TextField();  emailF.setPromptText("email@exemplo.com");
        PasswordField passF  = new PasswordField(); passF.setPromptText("password");

        GridPane form = formGrid();
        form.add(lbl("Email:"),    0, 0); form.add(emailF, 1, 0);
        form.add(lbl("Password:"), 0, 1); form.add(passF,  1, 1);

        Label feedback = new Label();
        Button loginBtn = primaryButton("Entrar");
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> {
            String email = emailF.getText().trim();
            String pass  = passF.getText();
            if (email.isEmpty()) { err(feedback, "✗ Digite o email."); return; }

            UtilizadorApiClient.LoginResult result = utilizadorClient.login(email, pass);

            if (result.sucesso()) {
                // fetch real user data from backend to get id/role
                finishLogin(email, feedback);
            } else if (result.fallbackUsado()) {
                err(feedback, "⚠ Backend indisponível.");
            } else {
                err(feedback, "✗ " + result.mensagem());
            }
        });

        Button regBtn = new Button("Ainda não tens conta? Registar");
        regBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1565c0; -fx-border-color: transparent;");
        regBtn.setOnAction(e -> showUsersPane());

        VBox box = centeredCard(title, form, loginBtn, feedback, regBtn);
        box.setMaxWidth(420);
        VBox wrapper = new VBox(box);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(40));
        wrapper.setStyle("-fx-background-color: #f8f9fa;");
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    /** After a successful login POST, fetch the user record and set currentUser. */
    private void finishLogin(String email, Label feedback) {
        bg(() -> utilizadorClient.listarUtilizadores(), allUsers -> {
            Optional<Map<String, Object>> found = allUsers.stream()
                    .filter(u -> email.equalsIgnoreCase(s(u, "email")))
                    .findFirst();
            if (found.isPresent()) {
                currentUser = found.get();
                updateNavState();
                headerUserLabel.setText("Olá, " + s(currentUser, "nome") + " (" + s(currentUser, "papel") + ")");
                ok(feedback, "✓ Login bem-sucedido.");
            } else {
                // user authenticated but not in list — create minimal entry
                currentUser = row("id", "?", "nome", email.split("@")[0], "email", email, "papel", "CLIENT", "ativo", "Sim", "password", "");
                updateNavState();
                headerUserLabel.setText("Olá, " + s(currentUser, "nome"));
                ok(feedback, "✓ Login bem-sucedido.");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────
    // C / D – Utilizadores (Admins & Entregadores only for registration)
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildUsersPane() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TableView<Map<String, Object>> table = tableOf(display,
                col("ID", "id"), col("Nome", "nome"), col("Email", "email"),
                col("Papel", "papel"), col("Ativo", "ativo")
        );

        TextField       searchF  = field("Filtrar por nome...", 180);
        ComboBox<String> papelBox = combo("Todos", "CLIENT", "ADMIN", "COURIER");

        Runnable applyFilter = () -> {
            String q = searchF.getText().toLowerCase();
            String p = papelBox.getValue();
            display.setAll(users.stream()
                    .filter(u -> q.isBlank() || s(u, "nome").toLowerCase().contains(q))
                    .filter(u -> "Todos".equals(p) || p.equals(s(u, "papel")))
                    .toList());
        };
        Button filtrar = new Button("Filtrar");
        filtrar.setOnAction(e -> applyFilter.run());

        // Only ADMIN and COURIER can be registered here (use case C)
        Button criarBtn   = colorBtn("+ Registar Admin/Entregador", "#4caf50");
        Button editarBtn  = colorBtn("✎ Editar",   "#2196f3");
        Button removerBtn = colorBtn("✕ Remover",  "#f44336");

        boolean isAdmin = currentUser != null && "ADMIN".equals(s(currentUser, "papel"));
        editarBtn.setDisable(!isAdmin);
        removerBtn.setDisable(!isAdmin);

        criarBtn.setOnAction(e -> {
            Map<String, Object> novo = userDialog(null);
            if (novo == null) return;
            String papel = s(novo, "papel");
            if ("CLIENT".equals(papel)) { warn("Use Case C: só pode registar Admins e Entregadores aqui."); return; }
            String tipo = mapPapelToTipo(papel);
            UtilizadorApiClient.RegistoResult res = utilizadorClient.registarUtilizador(
                    s(novo, "nome"), s(novo, "email"), s(novo, "password"), tipo);
            if (res.fallbackUsado()) {
                warn("⚠ " + res.mensagem());
            } else if (res.id() != null) {
                info("✓ " + res.mensagem());
                syncUsers(applyFilter);
            } else {
                warn("✗ " + res.mensagem());
            }
        });

        editarBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um utilizador."); return; }
            Map<String, Object> edited = userDialog(sel);
            if (edited == null) return;
            UtilizadorApiClient.SimpleResult res = utilizadorClient.atualizarUtilizador(
                    sel.get("id"), s(edited, "nome"), s(edited, "email"), s(edited, "password"));
            if (res.sucesso()) { info("✓ " + res.mensagem()); syncUsers(applyFilter); }
            else                { warn("✗ " + res.mensagem()); }
        });

        removerBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um utilizador."); return; }
            if (!confirm("Remover " + s(sel, "nome") + "?")) return;
            UtilizadorApiClient.SimpleResult res = utilizadorClient.removerUtilizador(sel.get("id"));
            if (res.sucesso()) { info("✓ " + res.mensagem()); syncUsers(applyFilter); }
            else                { warn("✗ " + res.mensagem()); }
        });

        userStatusLabel = statusLabel("A carregar utilizadores...");

        // initial load
        syncUsers(applyFilter);

        return pane(sectionTitle("C/D – Gestão de Utilizadores"),
                hbar(lbl("Nome:"), searchF, lbl("Papel:"), papelBox, filtrar),
                hbar(criarBtn, editarBtn, removerBtn),
                table, userStatusLabel);
    }

    private void syncUsers(Runnable afterUpdate) {
        bg(() -> utilizadorClient.listarUtilizadores(), result -> {
            users.setAll(result);
            if (afterUpdate != null) afterUpdate.run();
            setStatus(userStatusLabel,
                    result.isEmpty() ? "⚠ Sem dados (backend indisponível?)" : "✓ " + result.size() + " utilizadores carregados.");
        });
    }

    private Map<String, Object> userDialog(Map<String, Object> ex) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Registar Utilizador" : "Editar Utilizador");

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

    // ─────────────────────────────────────────────────────────────────────
    // E / F – Restaurantes
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildRestaurantesPane() {
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

        // E – Search filters
        TextField       nomeF   = field("Nome do restaurante...", 200);
        ComboBox<String> estBox = combo("Todos", "ABERTO", "FECHADO", "RESERVADO");

        Runnable applyFilter = () -> {
            String q = nomeF.getText().toLowerCase();
            String e = estBox.getValue();
            display.setAll(restaurantes.stream()
                    .filter(r -> q.isBlank() || s(r, "nome").toLowerCase().contains(q))
                    .filter(r -> "Todos".equals(e) || e.equals(s(r, "estado")))
                    .toList());
        };
        Button pesquisar = new Button("Pesquisar"); pesquisar.setDefaultButton(true);
        pesquisar.setOnAction(e -> applyFilter.run());
        Button limpar = new Button("Limpar");
        limpar.setOnAction(e -> { nomeF.clear(); estBox.getSelectionModel().selectFirst(); applyFilter.run(); });

        // F – Management (admin only)
        boolean isAdmin = currentUser != null && "ADMIN".equals(s(currentUser, "papel"));
        Button criarBtn   = colorBtn("+ Criar",         "#4caf50");
        Button editarBtn  = colorBtn("✎ Editar",         "#2196f3");
        Button estadoBtn  = colorBtn("⟳ Alterar Estado", "#ff9800");
        Button removerBtn = colorBtn("✕ Remover",        "#f44336");
        Button prodBtn    = colorBtn("🍽 Ver Produtos",   "#7b1fa2");
        criarBtn.setDisable(!isAdmin); editarBtn.setDisable(!isAdmin);
        estadoBtn.setDisable(!isAdmin); removerBtn.setDisable(!isAdmin);

        criarBtn.setOnAction(e -> {
            Map<String, Object> novo = restauranteDialog(null);
            if (novo == null) return;
            // Backend requires adminEmail as query param
            String adminEmail = s(currentUser, "email");
            if (adminEmail.isBlank()) { warn("Não foi possível determinar o email do administrador."); return; }
            try {
                Map<String, Object> payload = buildRestaurantePayload(novo);
                rest.postForEntity(BACKEND + "/api/restaurantes?adminEmail=" + adminEmail, payload, Map.class);
                info("✓ Restaurante criado.");
                syncRestaurantes(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao criar restaurante: " + ex.getMessage()); }
        });

        editarBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um restaurante."); return; }
            Map<String, Object> edited = restauranteDialog(sel);
            if (edited == null) return;
            try {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(buildRestaurantePayload(edited));
                rest.exchange(BACKEND + "/api/restaurantes/" + sel.get("id"), HttpMethod.PUT, entity, Map.class);
                info("✓ Restaurante atualizado.");
                syncRestaurantes(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao editar: " + ex.getMessage()); }
        });

        estadoBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um restaurante."); return; }
            ChoiceDialog<String> cd = new ChoiceDialog<>(s(sel, "estado"), "ABERTO", "FECHADO", "RESERVADO");
            cd.setTitle("Alterar Estado"); cd.setHeaderText("Novo estado:"); cd.initOwner(mainStage);
            cd.showAndWait().ifPresent(st -> {
                try {
                    Map<String, Object> payload = Map.of("estado", st);
                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload);
                    rest.exchange(BACKEND + "/api/restaurantes/" + sel.get("id") + "/estado",
                            HttpMethod.PATCH, entity, Map.class);
                    syncRestaurantes(applyFilter);
                } catch (Exception ex) { warn("✗ Erro ao alterar estado: " + ex.getMessage()); }
            });
        });

        removerBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um restaurante."); return; }
            if (!confirm("Remover " + s(sel, "nome") + "?")) return;
            try {
                rest.delete(BACKEND + "/api/restaurantes/" + sel.get("id"));
                info("✓ Restaurante removido.");
                syncRestaurantes(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao remover: " + ex.getMessage()); }
        });

        prodBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um restaurante."); return; }
            show(buildProductsPane(sel));
        });

        restStatusLabel = statusLabel("A carregar restaurantes...");
        syncRestaurantes(applyFilter);

        return pane(sectionTitle("E/F – Restaurantes"),
                hbar(lbl("Nome:"), nomeF, lbl("Estado:"), estBox, pesquisar, limpar),
                hbar(criarBtn, editarBtn, estadoBtn, removerBtn, prodBtn),
                table, new Label("Detalhes:"), details, restStatusLabel);
    }

    private void syncRestaurantes(Runnable afterUpdate) {
        bg(() -> {
            try {
                ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                        BACKEND + "/api/restaurantes", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {});
                return resp.getBody() != null ? resp.getBody() : List.of();
            } catch (Exception e) { return List.of(); }
        }, result -> {
            List<Map<String, Object>> typed = (List<Map<String, Object>>) (List<?>) result;
            restaurantes.setAll(typed.stream().map(this::normalizeRestaurante).toList());
            if (afterUpdate != null) afterUpdate.run();
            setStatus(restStatusLabel, result.isEmpty()
                    ? "⚠ Sem dados (backend indisponível?)"
                    : "✓ " + result.size() + " restaurantes carregados.");
        });
    }

    private Map<String, Object> normalizeRestaurante(Map<String, Object> r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",     r.getOrDefault("id", "?"));
        m.put("nome",   r.getOrDefault("nome", ""));
        m.put("estado", r.getOrDefault("estado", ""));
        m.put("tipo",   r.getOrDefault("tipoRestaurante", r.getOrDefault("tipoCozinha", "OUTRO")));
        m.put("nif",    r.getOrDefault("nif", "-"));
        // keep full original data for editing
        m.putAll(r);
        return m;
    }

    private Map<String, Object> buildRestaurantePayload(Map<String, Object> form) {
        // CriarRestauranteDTO / AtualizarRestauranteDTO: required fields are nome, nif, morada
        // estado and tipoRestaurante are NOT part of these DTOs
        Map<String, Object> morada = new LinkedHashMap<>();
        morada.put("rua",          s(form, "rua").isBlank()    ? "Rua Principal" : s(form, "rua"));
        morada.put("numero",       form.get("numero") != null  ? ((Number)form.get("numero")).intValue() : 1);
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

    private Map<String, Object> restauranteDialog(Map<String, Object> ex) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Criar Restaurante" : "Editar Restaurante");
        TextField nomeF         = new TextField(s(ex, "nome"));
        TextField nifF          = new TextField(s(ex, "nif"));
        TextField ruaF          = new TextField(s(ex, "rua"));
        TextField numF          = new TextField(ex != null ? String.valueOf(ex.getOrDefault("numero", "1")) : "1");
        TextField cpF           = new TextField(s(ex, "codigoPostal"));
        TextField cidadeF       = new TextField(s(ex, "cidade"));
        TextField distritoF     = new TextField(s(ex, "distrito"));
        TextField tipoF = new TextField(s(ex,"tipo"));

        GridPane g = formGrid();
        g.add(lbl("Nome:"),         0, 0); g.add(nomeF,     1, 0);
        g.add(lbl("NIF:"),          0, 1); g.add(nifF,      1, 1);
        g.add(lbl("Rua:"),          0, 2); g.add(ruaF,      1, 2);
        g.add(lbl("Número:"),       0, 3); g.add(numF,      1, 3);
        g.add(lbl("Cód. Postal:"),  0, 4); g.add(cpF,       1, 4);
        g.add(lbl("Cidade:"),       0, 5); g.add(cidadeF,   1, 5);
        g.add(lbl("Distrito:"),     0, 6); g.add(distritoF, 1, 6);
        g.add(lbl("Tipo:"),     0, 7); g.add(tipoF, 1, 7);

        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            int num = 1;
            try { num = Integer.parseInt(numF.getText().trim()); } catch (NumberFormatException ignored) {}
            return row(
                    "nome",        nomeF.getText().trim(),
                    "nif",         nifF.getText().trim(),
                    "rua",         ruaF.getText().trim(),
                    "numero",      num,
                    "codigoPostal",cpF.getText().trim(),
                    "cidade",      cidadeF.getText().trim(),
                    "distrito",    distritoF.getText().trim(),
                    "pais",        "Portugal",
                    "complemento", "",
                    "Tipo",       tipoF.getText().trim().isBlank() ? "OUTRO" : tipoF.getText().trim()
            );
        });
        return dlg.showAndWait().orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────
    // G / H – Produtos (Menus & Products)
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildProductsPane(Map<String, Object> restFilter) {
        ObservableList<String> restNames = FXCollections.observableArrayList("Todos");
        restaurantes.forEach(r -> restNames.add(s(r, "nome")));
        ComboBox<String> restBox = new ComboBox<>(restNames);
        restBox.getSelectionModel().selectFirst();
        if (restFilter != null) restBox.setValue(s(restFilter, "nome"));

        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TextField       searchF   = field("Nome do produto...", 150);
        TextField       precoMaxF = field("Preço máx.", 80);
        ComboBox<String> catBox   = combo("Todas", "PRATO", "BEBIDA", "SOBREMESA", "ENTRADA");
        ComboBox<String> dispBox  = combo("Todos", "Sim", "Não");

        Runnable applyFilter = () -> {
            String rest = restBox.getValue();
            String q    = searchF.getText().toLowerCase();
            String cat  = catBox.getValue();
            String disp = dispBox.getValue();
            Double maxP; try { maxP = Double.parseDouble(precoMaxF.getText().trim()); } catch (Exception ex) { maxP = null; }
            final Double fMax = maxP;
            display.setAll(produtos.stream()
                    .filter(p -> "Todos".equals(rest) || rest.equals(s(p, "restNome")))
                    .filter(p -> q.isBlank() || s(p, "nome").toLowerCase().contains(q))
                    .filter(p -> "Todas".equals(cat) || cat.equals(s(p, "categoria")))
                    .filter(p -> "Todos".equals(disp) || disp.equals(s(p, "disponivel")))
                    .filter(p -> fMax == null || toDouble(p.get("preco")) <= fMax)
                    .toList());
        };

        // Reload products when restaurant filter changes
        restBox.setOnAction(e -> {
            String chosen = restBox.getValue();
            if ("Todos".equals(chosen)) {
                syncProdutos(null, applyFilter);
            } else {
                restaurantes.stream().filter(r -> s(r, "nome").equals(chosen)).findFirst()
                        .ifPresent(r -> syncProdutos(toLong(r.get("id")), applyFilter));
            }
        });

        TableView<Map<String, Object>> table = tableOf(display,
                col("ID", "id"), col("Restaurante", "restNome"), col("Nome", "nome"),
                col("Preço (€)", "preco"), col("Categoria", "categoria"), col("Disponível", "disponivel")
        );

        Button filtrar   = new Button("Pesquisar"); filtrar.setOnAction(e -> applyFilter.run());
        Button criarBtn  = colorBtn("+ Criar Produto",   "#4caf50");
        Button editarBtn = colorBtn("✎ Editar",           "#2196f3");
        Button dispBtn   = colorBtn("⟳ Disp./Indisp.",   "#ff9800");
        Button apagarBtn = colorBtn("✕ Apagar",          "#f44336");

        criarBtn.setOnAction(e -> {
            String rNome = restBox.getValue();
            Map<String, Object> preRest = "Todos".equals(rNome) ? null :
                    restaurantes.stream().filter(r -> s(r, "nome").equals(rNome)).findFirst().orElse(null);
            if (preRest == null) { warn("Selecione um restaurante específico para criar um produto."); return; }
            Map<String, Object> novo = produtoDialog(null, preRest);
            if (novo == null) return;
            Long restId = toLong(preRest.get("id"));
            try {
                // Correct endpoint: POST /api/restaurantes/{restauranteId}/produtos
                rest.postForEntity(BACKEND + "/api/restaurantes/" + restId + "/produtos",
                        buildProdutoPayload(novo), Map.class);
                info("✓ Produto criado.");
                syncProdutos(restId, applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao criar produto: " + ex.getMessage()); }
        });

        editarBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um produto."); return; }
            Map<String, Object> edited = produtoDialog(sel, null);
            if (edited == null) return;
            try {
                // AtualizarProdutoDTO requires: {id, nome, preco, descricao}
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(buildAtualizarProdutoPayload(edited, sel.get("id")));
                rest.exchange(BACKEND + "/api/produtos/" + sel.get("id"), HttpMethod.PUT, entity, Map.class);
                info("✓ Produto atualizado.");
                syncProdutos(toLong(sel.get("restId")), applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao editar produto: " + ex.getMessage()); }
        });

        dispBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um produto."); return; }
            // AtualizarDisponibilidadeProdutoDTO: {disponivel: boolean}
            boolean novaDisp = "Não".equals(s(sel, "disponivel"));
            try {
                Map<String, Object> payload = Map.of("disponivel", novaDisp);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload);
                rest.exchange(BACKEND + "/api/produtos/" + sel.get("id") + "/disponibilidade",
                        HttpMethod.PATCH, entity, Map.class);
                info("✓ Disponibilidade atualizada.");
                syncProdutos(toLong(sel.get("restId")), applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao atualizar disponibilidade: " + ex.getMessage()); }
        });

        apagarBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um produto."); return; }
            if (!confirm("Apagar " + s(sel, "nome") + "?")) return;
            try {
                rest.delete(BACKEND + "/api/produtos/" + sel.get("id"));
                info("✓ Produto apagado.");
                syncProdutos(toLong(sel.get("restId")), applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao apagar: " + ex.getMessage()); }
        });

        Button voltarBtn = new Button("← Voltar a Restaurantes");
        voltarBtn.setOnAction(e -> showRestaurantesPane());

 prodStatusLabel = statusLabel("Selecione um restaurante para carregar produtos.");

        // initial load
        if (restFilter != null) syncProdutos(toLong(restFilter.get("id")), applyFilter);
        else syncProdutos(null, applyFilter);

        return pane(sectionTitle("G/H – Gestão de Produtos"),
                hbar(lbl("Restaurante:"), restBox, lbl("Nome:"), searchF,
                        lbl("Preço≤"), precoMaxF, lbl("Cat:"), catBox, lbl("Disp:"), dispBox, filtrar),
                hbar(criarBtn, editarBtn, dispBtn, apagarBtn, new Region(), voltarBtn),
                table, prodStatusLabel);
    }

    private void syncProdutos(Long restauranteId, Runnable afterUpdate) {
        bg(() -> {
            try {
                String url = restauranteId != null
                        ? BACKEND + "/api/produtos?restauranteId=" + restauranteId
                        : BACKEND + "/api/produtos";
                ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                        url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
                return resp.getBody() != null ? resp.getBody() : List.of();
            } catch (Exception e) { return List.of(); }
        }, result -> {
            // Enrich each product with restNome for display
            List<Map<String, Object>> typed = (List<Map<String, Object>>) (List<?>) result;
            List<Map<String, Object>> enriched = typed.stream().map(p -> {
                Map<String, Object> m = new LinkedHashMap<String, Object>(p);
                Long rid = toLong(p.getOrDefault("restauranteId", p.getOrDefault("restId", -1)));
                m.put("restId", rid);
                m.put("restNome", restaurantes.stream()
                        .filter(r -> rid.equals(toLong(r.get("id"))))
                        .findFirst().map(r -> s(r, "nome")).orElse("-"));
                m.put("disponivel", Boolean.TRUE.equals(p.get("disponivel")) ? "Sim" : "Não");
                return m;
            }).toList();

            if (restauranteId != null) {
                // replace only products of this restaurant
                produtos.removeIf(p -> restauranteId.equals(toLong(p.get("restId"))));
                produtos.addAll(enriched);
            } else {
                produtos.setAll(enriched);
            }
            if (afterUpdate != null) afterUpdate.run();
            setStatus(prodStatusLabel, "✓ " + produtos.size() + " produtos carregados.");
        });
    }

    private Map<String, Object> buildProdutoPayload(Map<String, Object> form) {
        // CriarProdutoDTO: {nome, preco, descricao} — no categoria, no disponivel, no restauranteId in body
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("nome",     s(form, "nome"));
        p.put("preco",    toDouble(form.get("preco")));
        p.put("descricao", s(form, "descricao").isBlank() ? s(form, "nome") : s(form, "descricao"));
        return p;
    }

    private Map<String, Object> buildAtualizarProdutoPayload(Map<String, Object> form, Object id) {
        // AtualizarProdutoDTO: {id, nome, preco, descricao}
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id",       id);
        p.put("nome",     s(form, "nome"));
        p.put("preco",    toDouble(form.get("preco")));
        p.put("descricao", s(form, "descricao").isBlank() ? s(form, "nome") : s(form, "descricao"));
        return p;
    }

    private Map<String, Object> produtoDialog(Map<String, Object> ex, Map<String, Object> preRest) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Criar Produto" : "Editar Produto");

        ObservableList<String> rNames = FXCollections.observableArrayList();
        restaurantes.forEach(r -> rNames.add(s(r, "nome")));
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
            Object rid = restaurantes.stream().filter(r -> s(r, "nome").equals(rn))
                    .findFirst().map(r -> r.get("id")).orElse(-1);
            return row("restId", rid, "restNome", rn,
                    "nome",     nomeF.getText().trim(),
                    "preco",    precoF.getText().trim(),
                    "descricao",descF.getText().trim());
        });
        return dlg.showAndWait().orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────
    // K / L / M / N – Pedidos
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildOrdersPane() {
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

        ComboBox<String> estadoBox = combo("Todos", "CRIADO", "PAGO", "A_PREPARAR", "PRONTO_PARA_ENTREGA", "ATRIBUIDO_ENTREGADOR", "ENTREGUE", "CANCELADO");
        TextField clienteF = field("Filtrar por cliente...", 160);

        Runnable applyFilter = () -> {
            String st = estadoBox.getValue();
            String cl = clienteF.getText().toLowerCase();
            display.setAll(pedidos.stream()
                    .filter(p -> "Todos".equals(st) || st.equals(s(p, "estado")))
                    .filter(p -> cl.isBlank() || s(p, "clienteNome").toLowerCase().contains(cl))
                    .toList());
        };
        Button filtrar = new Button("Filtrar"); filtrar.setOnAction(e -> applyFilter.run());
        Button refreshBtn = colorBtn("↻ Atualizar", "#607d8b");
        refreshBtn.setOnAction(e -> syncPedidos(applyFilter));

        // K – Pagamento
        Button pagarBtn  = colorBtn("💳 Pagar",             "#1976d2");
        // L – Estado
        Button prepBtn   = colorBtn("⌛ Iniciar Prep.",      "#e65100");
        Button prontoBtn = colorBtn("✓ Marcar Pronto",      "#558b2f");
        // M – Entregador
        Button atribBtn  = colorBtn("🚗 Atribuir Entregador", "#6a1b9a");
        Button entregBtn = colorBtn("📦 Registar Entrega",   "#00838f");
        // N – Cancelar
        Button cancelBtn = colorBtn("✕ Cancelar Pedido",    "#c62828");

        // K
        pagarBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"CRIADO".equals(s(sel, "estado"))) { warn("Só pedidos CRIADO podem ser pagos."); return; }
            ChoiceDialog<String> cd = new ChoiceDialog<>("MBWAY", "MBWAY", "MULTIBANCO", "DINHEIRO");
            cd.setTitle("Processar Pagamento"); cd.setHeaderText("Método de pagamento:"); cd.initOwner(mainStage);
            cd.showAndWait().ifPresent(metodo -> {
                try {
                    // ProcessarPagamentoDTO: pedidoId, tipoPagamento, detalhes (required map)
                    Map<String, Object> detalhes = new LinkedHashMap<>();
                    if ("MBWAY".equals(metodo))      detalhes.put("numeroMbway", "910000000");
                    else if ("MULTIBANCO".equals(metodo)) { detalhes.put("entidade", "11111"); detalhes.put("referencia", "123456789"); }
                    else                             detalhes.put("valorTroco", 0.0);

                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("pedidoId",      toLong(sel.get("id")));
                    payload.put("tipoPagamento", metodo);
                    payload.put("detalhes",      detalhes);

                    rest.postForEntity(BACKEND + "/pagamentos", payload, Map.class);
                    info("✓ Pagamento via " + metodo + " processado.");
                    syncPedidos(applyFilter);
                } catch (Exception ex) { warn("✗ Erro ao processar pagamento: " + ex.getMessage()); }
            });
        });

        // L – start preparation: PATCH /entregas/pedido/{id}/iniciar-preparacao
        prepBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"PAGO".equals(s(sel, "estado"))) { warn("Só pedidos PAGO podem iniciar preparação."); return; }
            try {
                rest.exchange(BACKEND + "/entregas/pedido/" + sel.get("id") + "/iniciar-preparacao",
                        HttpMethod.PATCH, HttpEntity.EMPTY, Object.class);
                info("✓ Preparação iniciada.");
                syncPedidos(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao iniciar preparação: " + ex.getMessage()); }
        });

        prontoBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"A_PREPARAR".equals(s(sel, "estado"))) { warn("Só pedidos em A_PREPARAR podem ser marcados prontos."); return; }
            try {
                rest.exchange(BACKEND + "/entregas/pedido/" + sel.get("id") + "/marcar-pronto",
                        HttpMethod.PATCH, HttpEntity.EMPTY, Object.class);
                info("✓ Pedido marcado como pronto para entrega.");
                syncPedidos(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao marcar pronto: " + ex.getMessage()); }
        });

        // M – assign driver (atribuir entregador endpoint is commented out in backend)
        // The backend EntregaController has this endpoint commented out.
        // We show the assignment locally and note it's not persisted until backend implements it.
        atribBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"PRONTO_PARA_ENTREGA".equals(s(sel, "estado"))) {
                warn("Pedido deve estar PRONTO_PARA_ENTREGA."); return;
            }
            List<String> couriers = users.stream()
                    .filter(u -> "COURIER".equals(s(u, "papel")) && "Sim".equals(s(u, "ativo")))
                    .map(u -> s(u, "nome")).toList();
            if (couriers.isEmpty()) { warn("Nenhum entregador disponível."); return; }
            ChoiceDialog<String> cd = new ChoiceDialog<>(couriers.get(0), couriers);
            cd.setTitle("Atribuir Entregador"); cd.setHeaderText("Escolha o entregador:"); cd.initOwner(mainStage);
            cd.showAndWait().ifPresent(nome -> info("✓ Entregador " + nome + " anotado (persistência pendente no backend)."));
        });

        // M – register pickup: PATCH /entregas/pedido/{id}/recolha
        entregBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"PRONTO_PARA_ENTREGA".equals(s(sel, "estado"))) {
                warn("Pedido deve estar PRONTO_PARA_ENTREGA para registar recolha."); return;
            }
            try {
                rest.exchange(BACKEND + "/entregas/pedido/" + sel.get("id") + "/recolha",
                        HttpMethod.PATCH, HttpEntity.EMPTY, Object.class);
                info("✓ Recolha registada.");
                syncPedidos(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao registar recolha: " + ex.getMessage()); }
        });

        // N – cancel: PATCH /pedidos/{id}/cancelar
        cancelBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            String st = s(sel, "estado");
            if (!"CRIADO".equals(st) && !"PAGO".equals(st)) {
                warn("Só pedidos CRIADO ou PAGO podem ser cancelados."); return;
            }
            if (!confirm("Cancelar pedido #" + sel.get("id") + "?")) return;
            try {
                rest.exchange(BACKEND + "/pedidos/" + sel.get("id") + "/cancelar",
                        HttpMethod.PATCH, HttpEntity.EMPTY, Map.class);
                info("✓ Pedido cancelado.");
                syncPedidos(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao cancelar: " + ex.getMessage()); }
        });

        // Extra: register final delivery: PATCH /entregas/pedido/{id}/entrega-final
        Button entregaFinalBtn = colorBtn("📬 Entrega Final", "#004d40");
        entregaFinalBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            try {
                rest.exchange(BACKEND + "/entregas/pedido/" + sel.get("id") + "/entrega-final",
                        HttpMethod.PATCH, HttpEntity.EMPTY, Object.class);
                info("✓ Entrega final registada.");
                syncPedidos(applyFilter);
            } catch (Exception ex) { warn("✗ Erro ao registar entrega final: " + ex.getMessage()); }
        });

        orderStatusLabel = statusLabel("A carregar pedidos...");
        syncPedidos(applyFilter);
        // also ensure users (couriers) are loaded for the assign-driver dialog
        if (users.isEmpty()) syncUsers(null);

        return pane(sectionTitle("K/L/M/N – Gestão de Pedidos"),
                hbar(lbl("Estado:"), estadoBox, lbl("Cliente:"), clienteF, filtrar, refreshBtn),
                hbar(pagarBtn, prepBtn, prontoBtn, atribBtn, entregBtn, entregaFinalBtn, cancelBtn),
                table, new Label("Detalhes do pedido selecionado:"), details,
                orderStatusLabel);
    }

    private void syncPedidos(Runnable afterUpdate) {
        bg(() -> {
            try {
                ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                        BACKEND + "/pedidos", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {});
                return resp.getBody() != null ? resp.getBody() : List.of();
            } catch (Exception e) { return List.of(); }
        }, result -> {
            List<Map<String, Object>> typed = (List<Map<String, Object>>) (List<?>) result;
            pedidos.setAll(typed.stream().map(this::normalizePedido).toList());
            if (afterUpdate != null) afterUpdate.run();
            setStatus(orderStatusLabel, "✓ " + pedidos.size() + " pedidos carregados.");
        });
    }

    private Map<String, Object> normalizePedido(Map<String, Object> p) {
        // Resolve restaurant name
        Long restId = null;
        Object rid = p.get("restauranteId");
        if (rid == null) {
            Object ids = p.get("restaurantesIds");
            if (ids instanceof List<?> list && !list.isEmpty()) rid = list.get(0);
        }
        if (rid != null) restId = toLong(rid);
        final Long fRestId = restId;
        String restNome = restaurantes.stream()
                .filter(r -> fRestId != null && fRestId.equals(toLong(r.get("id"))))
                .findFirst().map(r -> s(r, "nome")).orElse("Restaurante #" + fRestId);

        // Resolve client name
        Object cid = p.get("clienteId");
        String clienteNome = users.stream()
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

    // ─────────────────────────────────────────────────────────────────────
    // Background task helper
    // ─────────────────────────────────────────────────────────────────────
    private <T> void bg(java.util.concurrent.Callable<T> work, java.util.function.Consumer<T> onDone) {
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return work.call(); }
        };
        task.setOnSucceeded(ev -> onDone.accept(task.getValue()));
        task.setOnFailed(ev -> System.err.println("BG task failed: " + task.getException()));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ─────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────
    private Label lbl(String t) { return new Label(t); }

    private Label sectionTitle(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222;");
        return l;
    }

    private Label statusLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        return l;
    }

    private void setStatus(Label l, String text) {
        if (l != null) Platform.runLater(() -> l.setText(text));
    }

    private Button primaryButton(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-font-size: 13px; -fx-padding: 10 28; -fx-background-color: #ff5722;" +
                "-fx-text-fill: white; -fx-background-radius: 6;");
        return b;
    }

    private Button colorBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-padding: 6 10; -fx-background-color: " + color + ";" +
                "-fx-text-fill: white; -fx-font-size: 11px;");
        return b;
    }

    @SafeVarargs
    private <T> TableView<T> tableOf(ObservableList<T> items, TableColumn<T, String>... cols) {
        TableView<T> t = new TableView<>(items);
        t.getColumns().addAll(cols);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Sem dados para mostrar."));
        VBox.setVgrow(t, Priority.ALWAYS);
        return t;
    }

    private TableColumn<Map<String, Object>, String> col(String title, String key) {
        TableColumn<Map<String, Object>, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getOrDefault(key, "").toString()));
        return c;
    }

    private GridPane formGrid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        g.setPadding(new Insets(16));
        g.setStyle("-fx-background-color: white; -fx-background-radius: 8;" +
                "-fx-border-color: #ddd; -fx-border-radius: 8;");
        g.getColumnConstraints().addAll(new ColumnConstraints(110), new ColumnConstraints(230));
        return g;
    }

    private TextField field(String prompt, double w) {
        TextField f = new TextField(); f.setPromptText(prompt); f.setPrefWidth(w); return f;
    }

    @SuppressWarnings("unchecked")
    private <T> ComboBox<T> combo(T... items) {
        ComboBox<T> c = new ComboBox<>(FXCollections.observableArrayList(items));
        c.getSelectionModel().selectFirst(); return c;
    }

    private Dialog<Map<String, Object>> dialog(String title) {
        Dialog<Map<String, Object>> d = new Dialog<>();
        d.setTitle(title); d.initOwner(mainStage); return d;
    }

    private VBox centeredCard(javafx.scene.Node... nodes) {
        VBox box = new VBox(12, nodes);
        box.setPadding(new Insets(24));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-border-color: #ddd; -fx-border-radius: 10;");
        return box;
    }

    private VBox pane(javafx.scene.Node... nodes) {
        VBox v = new VBox(10, nodes);
        v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color: #f8f9fa;");
        VBox.setVgrow(v, Priority.ALWAYS);
        return v;
    }

    private HBox hbar(javafx.scene.Node... nodes) {
        HBox h = new HBox(8, nodes);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private Map<String, Object> selected(TableView<Map<String, Object>> t) {
        Map<String, Object> sel = t.getSelectionModel().getSelectedItem();
        if (sel == null) warn("Selecione um item da tabela.");
        return sel;
    }

    private String s(Map<String, Object> m, String k) {
        return m == null ? "" : Objects.toString(m.getOrDefault(k, ""), "");
    }

    private Map<String, Object> row(Object... pairs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) m.put(pairs[i].toString(), pairs[i + 1]);
        return m;
    }

    private Long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(Objects.toString(v, "-1")); } catch (NumberFormatException e) { return -1L; }
    }

    private double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(Objects.toString(v, "0")); } catch (NumberFormatException e) { return 0.0; }
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(mainStage); a.setTitle("Sucesso"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.initOwner(mainStage); a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.initOwner(mainStage); a.setTitle("Confirmar"); a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait().map(b -> b == ButtonType.OK).orElse(false);
    }

    private void ok(Label l, String msg)  { l.setStyle("-fx-text-fill: #388e3c; -fx-font-size: 13px;"); l.setText(msg); }
    private void err(Label l, String msg) { l.setStyle("-fx-text-fill: #c62828; -fx-font-size: 13px;"); l.setText(msg); }

    private String mapPapelToTipo(String papel) {
        return switch (papel) {
            case "ADMIN"  -> "Admin";
            case "COURIER"-> "Entregador";
            default       -> "Cliente";
        };
    }
}