package pt.ul.fc.css.tascaeats.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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

    // ── UI state ──────────────────────────────────────────────────────────
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
        b.setStyle("-fx-font-size: 11px; -fx-padding: 6 11; -fx-background-color: transparent; " +
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
    // A – Login
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
     // B / C / D – Utilizadores
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildUsersPane() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TableView<Map<String, Object>> table = tableOf(display,
            col("ID", "id"), col("Nome", "nome"), col("Email", "email"),
            col("Papel", "papel"), col("Ativo", "ativo")
        );

        TextField  searchF  = field("Filtrar por nome...", 180);
        ComboBox<String> papelBox = combo("Todos","CLIENT","ADMIN","COURIER");
        Button filtrar = new Button("Filtrar");
        filtrar.setOnAction(e -> {
            String q = searchF.getText().toLowerCase();
            String p = papelBox.getValue();
            display.setAll(mockUsers.stream()
                .filter(u -> q.isBlank() || u.get("nome").toString().toLowerCase().contains(q))
                .filter(u -> "Todos".equals(p) || p.equals(u.get("papel")))
                .toList());
        });

        Button criarBtn   = colorBtn("+ Registar",  "#4caf50");
        Button editarBtn  = colorBtn("✎ Editar",     "#2196f3");
        Button removerBtn = colorBtn("✕ Remover",   "#f44336");

        criarBtn.setOnAction(e -> {
            Map<String, Object> novo = userDialog(null);
            if (novo != null) {
                // Tentar registar no backend (com fallback para mock)
                String nome = novo.get("nome").toString();
                String email = novo.get("email").toString();
                String password = novo.get("password").toString();
                String papel = novo.get("papel").toString();

                // Converter papel GUI (CLIENT, ADMIN, COURIER) para tipo API (Cliente, Admin, Entregador)
                String tipo = mapPapelToTipo(papel);

                // Fazer o registo via API (com fallback automático se backend indisponível)
                UtilizadorApiClient.RegistoResult result = utilizadorApiClient.registarUtilizador(nome, email, password, tipo);

                // Usar o ID devolvido pelo backend ou o mock
                novo.put("id", result.id() != null ? result.id() : nextUserId++);
                mockUsers.add(novo);
                display.setAll(mockUsers);

                // Mostrar mensagem com indicação se foi fallback ou sucesso
                if (result.fallbackUsado()) {
                    warn("⚠ " + result.mensagem());
                } else {
                    info("✓ " + result.mensagem());
                }
            }
        });
        if(currentUser != null && !currentUser.get("papel").equals("CLIENT")) {
            criarBtn.setDisable(false);
            editarBtn.setDisable(false);
            removerBtn.setDisable(false);
        } else {
            editarBtn.setDisable(true);
            removerBtn.setDisable(true);
        }
        editarBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um utilizador."); return; }
            Map<String, Object> edited = userDialog(sel);
            if (edited != null) { sel.putAll(edited); display.setAll(mockUsers); table.refresh(); }
        });
         removerBtn.setOnAction(e -> {
             Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
             if (sel == null) { warn("Selecione um utilizador."); return; }
             if (confirm("Remover " + sel.get("nome") + "?")) {
                 mockUsers.remove(sel); display.setAll(mockUsers);
             }
         });

         userStatusLabel = new Label("Modo demo — backend não consultado.");
         userStatusLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");

         Platform.runLater(this::syncUtilizadoresFromBackend);

         return pane(sectionTitle("B/C/D – Gestão de Utilizadores"),
             hbar(lbl("Nome:"), searchF, lbl("Papel:"), papelBox, filtrar),
             hbar(criarBtn, editarBtn, removerBtn),
             table,
             userStatusLabel
         );
     }

    private Map<String, Object> userDialog(Map<String, Object> ex) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Registar Utilizador" : "Editar Utilizador");

        TextField    nomeF  = new TextField(s(ex,"nome"));
        TextField    emailF = new TextField(s(ex,"email"));
        PasswordField passF = new PasswordField(); passF.setText(s(ex,"password"));
        ComboBox<String> papelBox = combo("CLIENT","ADMIN","COURIER");
        papelBox.setValue(ex != null ? s(ex,"papel") : "CLIENT");
        ComboBox<String> ativoBox = combo("Sim","Não");
        ativoBox.setValue(ex != null ? s(ex,"ativo") : "Sim");

        GridPane g = formGrid();
        g.add(lbl("Nome:"),     0,0); g.add(nomeF,    1,0);
        g.add(lbl("Email:"),    0,1); g.add(emailF,   1,1);
        g.add(lbl("Password:"), 0,2); g.add(passF,    1,2);
        g.add(lbl("Papel:"),    0,3); g.add(papelBox, 1,3);
        g.add(lbl("Ativo:"),    0,4); g.add(ativoBox, 1,4);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> b == ButtonType.OK ? row(
            "nome", nomeF.getText().trim(), "email", emailF.getText().trim(),
            "password", passF.getText().isBlank() && ex != null ? s(ex,"password") : passF.getText().trim(),
            "papel", papelBox.getValue(), "ativo", ativoBox.getValue()
        ) : null);
        return dlg.showAndWait().orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────
    // E / F – Restaurantes
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildRestaurantesPane() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList(mockRestaurantes);
        TableView<Map<String, Object>> table = tableOf(display,
            col("ID","id"), col("Nome","nome"), col("Estado","estado"),
            col("Cidade","cidade"), col("NIF","nif")
        );

        javafx.scene.control.TextArea details = new javafx.scene.control.TextArea();
        details.setEditable(false); details.setPrefRowCount(5);
        details.setPromptText("Selecione um restaurante para ver detalhes.");
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) details.setText(
                "ID: " + n.get("id") + "\nNome: " + n.get("nome") +
                "\nEstado: " + n.get("estado") + "\nCidade: " + n.get("cidade") + "\nNIF: " + n.get("nif"));
        });

        TextField nomeF = field("Nome do restaurante...", 200);
        ComboBox<String> estBox = combo("Todos","ABERTO","FECHADO","RESERVADO");
        Runnable filter = () -> {
            String q = nomeF.getText().toLowerCase();
            String e = estBox.getValue();
            display.setAll(mockRestaurantes.stream()
                .filter(r -> q.isBlank() || r.get("nome").toString().toLowerCase().contains(q))
                .filter(r -> "Todos".equals(e) || e.equals(r.get("estado")))
                .toList());
        };
        Button filtrar = new Button("Pesquisar"); filtrar.setDefaultButton(true); filtrar.setOnAction(e -> filter.run());
        Button limpar  = new Button("Limpar"); limpar.setOnAction(e -> { nomeF.clear(); estBox.getSelectionModel().selectFirst(); filter.run(); });

        Button criarBtn   = colorBtn("+ Criar",          "#4caf50");
        Button editarBtn  = colorBtn("✎ Editar",          "#2196f3");
        Button estadoBtn  = colorBtn("⟳ Alterar Estado",  "#ff9800");
        Button removerBtn = colorBtn("✕ Remover",         "#f44336");
        Button prodBtn    = colorBtn("🍽 Ver Produtos",    "#7b1fa2");

        if(currentUser != null && currentUser.get("papel").equals("ADMIN")) {
            criarBtn.setDisable(false);
            editarBtn.setDisable(false);
            estadoBtn.setDisable(false);
            removerBtn.setDisable(false);
            prodBtn.setDisable(false);
        } else {
            criarBtn.setDisable(true);
            editarBtn.setDisable(true);
            estadoBtn.setDisable(true);
            removerBtn.setDisable(true);
            prodBtn.setDisable(true);
        }

        criarBtn.setOnAction(e -> {
            Map<String, Object> novo = restauranteDialog(null);
            if (novo != null) { novo.put("id", nextRestId++); mockRestaurantes.add(novo); filter.run(); info("Restaurante criado."); }
        });
        editarBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um restaurante."); return; }
            Map<String, Object> edited = restauranteDialog(sel);
            if (edited != null) { sel.putAll(edited); display.setAll(mockRestaurantes); table.refresh(); }
        });
        estadoBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um restaurante."); return; }
            ChoiceDialog<String> cd = new ChoiceDialog<>(s(sel,"estado"), "ABERTO","FECHADO","RESERVADO");
            cd.setTitle("Alterar Estado"); cd.setHeaderText("Novo estado:"); cd.initOwner(mainStage);
            cd.showAndWait().ifPresent(st -> { sel.put("estado",st); display.setAll(mockRestaurantes); table.refresh(); });
        });
        removerBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um restaurante."); return; }
            if (confirm("Remover " + sel.get("nome") + "?")) { mockRestaurantes.remove(sel); filter.run(); }
        });
        prodBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um restaurante."); return; }
            show(buildProductsPane(sel));
        });

        restStatusLabel = new Label("Modo demo — backend não consultado.");
        restStatusLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");
        Platform.runLater(this::syncRestaurantesFromBackend);

        return pane(sectionTitle("E/F – Gestão de Restaurantes"),
            hbar(lbl("Nome:"), nomeF, lbl("Estado:"), estBox, filtrar, limpar),
            hbar(criarBtn, editarBtn, estadoBtn, removerBtn, prodBtn),
            table,
            new Label("Detalhes:"), details,
            restStatusLabel
        );
    }

    private void syncUtilizadoresFromBackend() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override protected List<Map<String, Object>> call() {
                // Call your existing API client to get all users
                return utilizadorApiClient.listarUtilizadores();
            }
        };
        task.setOnSucceeded(ev -> {
            List<Map<String, Object>> users = task.getValue();
            if (users != null && !users.isEmpty()) {
                mockUsers.setAll(users);
                if (userStatusLabel != null)
                    userStatusLabel.setText("✅ Sincronizado com backend.");
            }
        });
        task.setOnFailed(ev -> {
            if (userStatusLabel != null)
                userStatusLabel.setText("⚠️ Backend indisponível — modo demo ativo.");
        });
        Thread t = new Thread(task, "users-sync"); t.setDaemon(true); t.start();
    }

    private void syncRestaurantesFromBackend() {
        Task<RestauranteApiClient.RestaurantSearchResult> task = new Task<>() {
            @Override protected RestauranteApiClient.RestaurantSearchResult call() {
                return apiClient.listarRestaurantesComFallback(null, null);
            }
        };
        task.setOnSucceeded(ev -> {
            RestauranteApiClient.RestaurantSearchResult r = task.getValue();
            if (!r.fallbackUsado() && !r.restaurantes().isEmpty()) {
                mockRestaurantes.clear();
                r.restaurantes().forEach(res -> mockRestaurantes.add(
                    row("id", res.id(), "nome", res.nome(), "estado", res.estado(), "cidade", "-", "nif", "-")
                ));
                if (restStatusLabel != null) restStatusLabel.setText("✓ Sincronizado com backend: " + r.mensagem());
            } else {
                if (restStatusLabel != null) restStatusLabel.setText("Backend indisponível — modo demo ativo.");
            }
        });
        task.setOnFailed(ev -> { if (restStatusLabel != null) restStatusLabel.setText("Backend indisponível — modo demo ativo."); });
        Thread t = new Thread(task, "rest-sync"); t.setDaemon(true); t.start();
    }

    private Map<String, Object> restauranteDialog(Map<String, Object> ex) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Criar Restaurante" : "Editar Restaurante");
        TextField nomeF   = new TextField(s(ex,"nome"));
        TextField nifF    = new TextField(s(ex,"nif"));
        TextField cidadeF = new TextField(s(ex,"cidade"));
        ComboBox<String> estBox = combo("ABERTO","FECHADO","RESERVADO");
        estBox.setValue(ex != null ? s(ex,"estado") : "ABERTO");

        GridPane g = formGrid();
        g.add(lbl("Nome:"),   0,0); g.add(nomeF,   1,0);
        g.add(lbl("NIF:"),    0,1); g.add(nifF,    1,1);
        g.add(lbl("Cidade:"), 0,2); g.add(cidadeF, 1,2);
        g.add(lbl("Estado:"), 0,3); g.add(estBox,  1,3);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> b == ButtonType.OK ? row(
            "nome", nomeF.getText().trim(), "nif", nifF.getText().trim(),
            "cidade", cidadeF.getText().trim(), "estado", estBox.getValue()
        ) : null);
        return dlg.showAndWait().orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────
    // G / H / I – Produtos
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildProductsPane(Map<String, Object> restFilter) {
        ObservableList<String> restNames = FXCollections.observableArrayList("Todos");
        mockRestaurantes.forEach(r -> restNames.add(r.get("nome").toString()));
        ComboBox<String> restBox = new ComboBox<>(restNames);
        restBox.getSelectionModel().selectFirst();
        if (restFilter != null) restBox.setValue(s(restFilter, "nome"));

        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList();
        TextField  searchF   = field("Nome do produto...", 150);
        TextField  precoMaxF = field("Preço máx.", 80);
        ComboBox<String> catBox  = combo("Todas","PRATO","BEBIDA","SOBREMESA","ENTRADA");
        ComboBox<String> dispBox = combo("Todos","Sim","Não");

        Runnable filter = () -> {
            String rest = restBox.getValue();
            String q    = searchF.getText().toLowerCase();
            String cat  = catBox.getValue();
            String disp = dispBox.getValue();
            Double maxP; try { maxP = Double.parseDouble(precoMaxF.getText().trim()); } catch (Exception ex) { maxP = null; }
            final Double fMaxP = maxP;
            display.setAll(mockProdutos.stream()
                .filter(p -> "Todos".equals(rest) || rest.equals(p.get("restNome")))
                .filter(p -> q.isBlank() || p.get("nome").toString().toLowerCase().contains(q))
                .filter(p -> "Todas".equals(cat) || cat.equals(p.get("categoria")))
                .filter(p -> "Todos".equals(disp) || disp.equals(p.get("disponivel")))
                .filter(p -> fMaxP == null || Double.parseDouble(p.get("preco").toString()) <= fMaxP)
                .toList());
        };
        filter.run();
        restBox.setOnAction(e -> filter.run());

        TableView<Map<String, Object>> table = tableOf(display,
            col("ID","id"), col("Restaurante","restNome"), col("Nome","nome"),
            col("Preço (€)","preco"), col("Categoria","categoria"), col("Disponível","disponivel")
        );

        Button filtrar    = new Button("Pesquisar"); filtrar.setOnAction(e -> filter.run());
        Button criarBtn   = colorBtn("+ Criar Produto",      "#4caf50");
        Button editarBtn  = colorBtn("✎ Editar",              "#2196f3");
        Button dispBtn    = colorBtn("⟳ Disp. / Indisp.",    "#ff9800");
        Button apagarBtn  = colorBtn("✕ Apagar",             "#f44336");

        criarBtn.setOnAction(e -> {
            String rNome = restBox.getValue();
            Map<String, Object> preRest = "Todos".equals(rNome) ? null
                : mockRestaurantes.stream().filter(r -> r.get("nome").equals(rNome)).findFirst().orElse(null);
            Map<String, Object> novo = produtoDialog(null, preRest);
            if (novo != null) { novo.put("id", nextProdId++); mockProdutos.add(novo); filter.run(); info("Produto criado."); }
        });
        editarBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um produto."); return; }
            Map<String, Object> edited = produtoDialog(sel, null);
            if (edited != null) { sel.putAll(edited); filter.run(); table.refresh(); }
        });
        dispBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um produto."); return; }
            sel.put("disponivel", "Sim".equals(sel.get("disponivel")) ? "Não" : "Sim");
            table.refresh();
        });
        apagarBtn.setOnAction(e -> {
            Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warn("Selecione um produto."); return; }
            if (confirm("Apagar " + sel.get("nome") + "?")) { mockProdutos.remove(sel); filter.run(); }
        });

        Button voltarBtn = new Button("← Voltar a Restaurantes");
        voltarBtn.setOnAction(e -> showRestaurantesPane());

        return pane(sectionTitle("G/H/I – Gestão de Produtos"),
            hbar(lbl("Restaurante:"), restBox, lbl("Nome:"), searchF,
                 lbl("Preço≤"), precoMaxF, lbl("Cat:"), catBox, lbl("Disp:"), dispBox, filtrar),
            hbar(criarBtn, editarBtn, dispBtn, apagarBtn, new Region(), voltarBtn),
            table
        );
    }

    private Map<String, Object> produtoDialog(Map<String, Object> ex, Map<String, Object> preRest) {
        Dialog<Map<String, Object>> dlg = dialog(ex == null ? "Criar Produto" : "Editar Produto");

        ObservableList<String> rNames = FXCollections.observableArrayList();
        mockRestaurantes.forEach(r -> rNames.add(r.get("nome").toString()));
        ComboBox<String> restBox = new ComboBox<>(rNames);
        if (ex != null)        restBox.setValue(s(ex,"restNome"));
        else if (preRest != null) restBox.setValue(s(preRest,"nome"));
        else restBox.getSelectionModel().selectFirst();

        TextField nomeF  = new TextField(s(ex,"nome"));
        TextField precoF = new TextField(s(ex,"preco"));
        ComboBox<String> catBox  = combo("PRATO","BEBIDA","SOBREMESA","ENTRADA");
        catBox.setValue(ex != null ? s(ex,"categoria") : "PRATO");
        ComboBox<String> dispBox = combo("Sim","Não");
        dispBox.setValue(ex != null ? s(ex,"disponivel") : "Sim");

        GridPane g = formGrid();
        g.add(lbl("Restaurante:"), 0,0); g.add(restBox, 1,0);
        g.add(lbl("Nome:"),        0,1); g.add(nomeF,   1,1);
        g.add(lbl("Preço (€):"),   0,2); g.add(precoF,  1,2);
        g.add(lbl("Categoria:"),   0,3); g.add(catBox,  1,3);
        g.add(lbl("Disponível:"),  0,4); g.add(dispBox, 1,4);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            String rn = restBox.getValue();
            Object rid = mockRestaurantes.stream().filter(r -> r.get("nome").equals(rn))
                .findFirst().map(r -> r.get("id")).orElse(-1);
            return row("restId", rid, "restNome", rn,
                "nome", nomeF.getText().trim(), "preco", precoF.getText().trim(),
                "categoria", catBox.getValue(), "disponivel", dispBox.getValue());
        });
        return dlg.showAndWait().orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────
    // J / K / L / M / N – Pedidos e Entregas
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildOrdersPane() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList(mockPedidos);
        TableView<Map<String, Object>> table = tableOf(display,
            col("ID","id"), col("Cliente","clienteNome"), col("Restaurante","restNome"),
            col("Estado","estado"), col("Total (€)","total"),
            col("Pagamento","pagamento"), col("Entregador","entregador")
        );

        javafx.scene.control.TextArea details = new javafx.scene.control.TextArea();
        details.setEditable(false); details.setPrefRowCount(4);
        details.setPromptText("Selecione um pedido para ver detalhes.");
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) details.setText(
                "ID: " + n.get("id") + "\nCliente: " + n.get("clienteNome") +
                "\nRestaurante: " + n.get("restNome") + "\nEstado: " + n.get("estado") +
                "\nTotal: " + n.get("total") + "€\nPagamento: " + n.get("pagamento") +
                "\nEntregador: " + n.get("entregador"));
        });

        ComboBox<String> estadoBox = combo("Todos","CRIADO","PAGO","PREPARACAO","PRONTO_PARA_ENTREGA","ENTREGUE","CANCELADO");
        TextField clienteF = field("Filtrar por cliente...", 160);
        Button filtrar = new Button("Filtrar");
        filtrar.setOnAction(e -> {
            String st = estadoBox.getValue(); String cl = clienteF.getText().toLowerCase();
            display.setAll(mockPedidos.stream()
                .filter(p -> "Todos".equals(st) || st.equals(p.get("estado")))
                .filter(p -> cl.isBlank() || p.get("clienteNome").toString().toLowerCase().contains(cl))
                .toList());
        });

        // J – Criar Pedido
        Button criarBtn   = colorBtn("+ Criar Pedido",           "#4caf50");
        // K – Pagamento
        Button pagarBtn   = colorBtn("💳 Pagar",                  "#1976d2");
        // L – Estado
        Button prepBtn    = colorBtn("⌛ Iniciar Prep.",           "#e65100");
        Button prontoBtn  = colorBtn("✓ Marcar Pronto",           "#558b2f");
        // M – Entregador
        Button atribBtn   = colorBtn("🚗 Atribuir Entregador",    "#6a1b9a");
        Button entregBtn  = colorBtn("📦 Registar Entrega",       "#00838f");
        // N – Cancelar
        Button cancelBtn  = colorBtn("✕ Cancelar Pedido",        "#c62828");

        criarBtn.setOnAction(e -> {
            Map<String, Object> novo = criarPedidoDialog();
            if (novo != null) { novo.put("id", nextPedidoId++); mockPedidos.add(novo); display.setAll(mockPedidos); info("Pedido #" + novo.get("id") + " criado."); }
        });

        pagarBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"CRIADO".equals(sel.get("estado"))) { warn("Só pedidos CRIADO podem ser pagos."); return; }
            ChoiceDialog<String> cd = new ChoiceDialog<>("MBWAY","DINHEIRO","MBWAY","MULTIBANCO");
            cd.setTitle("Processar Pagamento"); cd.setHeaderText("Método de pagamento:"); cd.initOwner(mainStage);
            cd.showAndWait().ifPresent(m -> {
                sel.put("estado","PAGO"); sel.put("pagamento",m);
                display.setAll(mockPedidos); table.refresh();
                info("Pagamento via " + m + " processado. Estado → PAGO.");
            });
        });

        prepBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"PAGO".equals(sel.get("estado"))) { warn("Só pedidos PAGO podem iniciar preparação."); return; }
            sel.put("estado","PREPARACAO"); display.setAll(mockPedidos); table.refresh();
            info("Preparação iniciada. Estado → PREPARACAO.");
        });

        prontoBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"PREPARACAO".equals(sel.get("estado"))) { warn("Só pedidos em PREPARACAO podem ser marcados prontos."); return; }
            sel.put("estado","PRONTO_PARA_ENTREGA"); display.setAll(mockPedidos); table.refresh();
            info("Estado → PRONTO_PARA_ENTREGA.");
        });

        atribBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"PRONTO_PARA_ENTREGA".equals(sel.get("estado"))) { warn("Pedido deve estar PRONTO_PARA_ENTREGA."); return; }
            List<String> couriers = mockUsers.stream()
                .filter(u -> "COURIER".equals(u.get("papel")) && "Sim".equals(u.get("ativo")))
                .map(u -> u.get("nome").toString()).toList();
            if (couriers.isEmpty()) { warn("Nenhum entregador disponível."); return; }
            ChoiceDialog<String> cd = new ChoiceDialog<>(couriers.get(0), couriers);
            cd.setTitle("Atribuir Entregador"); cd.setHeaderText("Escolha o entregador:"); cd.initOwner(mainStage);
            cd.showAndWait().ifPresent(c -> {
                sel.put("entregador", c); display.setAll(mockPedidos); table.refresh();
                info("Entregador " + c + " atribuído.");
            });
        });

        entregBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            if (!"PRONTO_PARA_ENTREGA".equals(sel.get("estado"))) { warn("Pedido deve estar PRONTO_PARA_ENTREGA."); return; }
            if ("-".equals(sel.get("entregador")) || s(sel,"entregador").isBlank()) { warn("Atribua um entregador antes de registar a entrega."); return; }
            sel.put("estado","ENTREGUE"); display.setAll(mockPedidos); table.refresh();
            info("Entrega registada. Estado → ENTREGUE.");
        });

        cancelBtn.setOnAction(e -> {
            Map<String, Object> sel = selected(table); if (sel == null) return;
            String st = s(sel,"estado");
            if (!"CRIADO".equals(st) && !"PAGO".equals(st)) { warn("Só pedidos CRIADO ou PAGO podem ser cancelados."); return; }
            if (confirm("Cancelar pedido #" + sel.get("id") + "?")) {
                sel.put("estado","CANCELADO"); display.setAll(mockPedidos); table.refresh();
            }
        });

        return pane(sectionTitle("J/K/L/M/N – Gestão de Pedidos e Entregas"),
            hbar(lbl("Estado:"), estadoBox, lbl("Cliente:"), clienteF, filtrar),
            hbar(criarBtn, pagarBtn, prepBtn, prontoBtn, atribBtn, entregBtn, cancelBtn),
            table,
            new Label("Detalhes do pedido selecionado:"), details
        );
    }

    private Map<String, Object> criarPedidoDialog() {
        Dialog<Map<String, Object>> dlg = dialog("Criar Novo Pedido (J)");

        List<Map<String, Object>> clientes = mockUsers.stream()
            .filter(u -> "CLIENT".equals(u.get("papel"))).toList();
        ComboBox<String> clienteBox = new ComboBox<>(FXCollections.observableArrayList(
            clientes.stream().map(u -> u.get("nome").toString()).toList()));
        clienteBox.getSelectionModel().selectFirst();

        List<Map<String, Object>> abertos = mockRestaurantes.stream()
            .filter(r -> "ABERTO".equals(r.get("estado"))).toList();
        ComboBox<String> restBox = new ComboBox<>(FXCollections.observableArrayList(
            abertos.stream().map(r -> r.get("nome").toString()).toList()));
        restBox.getSelectionModel().selectFirst();

        ListView<String> prodList = new ListView<>();
        prodList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        prodList.setPrefHeight(130);

        Runnable refreshProds = () -> {
            String rn = restBox.getValue();
            prodList.setItems(FXCollections.observableArrayList(
                mockProdutos.stream()
                    .filter(p -> rn != null && rn.equals(p.get("restNome")) && "Sim".equals(p.get("disponivel")))
                    .map(p -> p.get("nome") + " — " + p.get("preco") + "€")
                    .toList()));
        };
        restBox.setOnAction(e -> refreshProds.run());
        refreshProds.run();

        TextField moradaF = new TextField("Rua Exemplo, 1, Lisboa");
        Label prodHint = new Label("(Ctrl+Click para selecionar vários)");
        prodHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

        GridPane g = formGrid();
        g.add(lbl("Cliente:"),     0,0); g.add(clienteBox, 1,0);
        g.add(lbl("Restaurante:"), 0,1); g.add(restBox,    1,1);
        g.add(lbl("Produtos:"),    0,2); g.add(prodList,   1,2);
        g.add(new Label(),         0,3); g.add(prodHint,   1,3);
        g.add(lbl("Morada:"),      0,4); g.add(moradaF,    1,4);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().setPrefWidth(480);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(b -> {
            if (b != ButtonType.OK) return null;
            List<String> selecionados = prodList.getSelectionModel().getSelectedItems();
            double total = selecionados.stream().mapToDouble(it -> {
                try { return Double.parseDouble(it.split(" — ")[1].replace("€","")); }
                catch (Exception ex) { return 0.0; }
            }).sum();
            String cNome = clienteBox.getValue();
            int cId = clientes.stream().filter(u -> u.get("nome").equals(cNome))
                .findFirst().map(u -> (Integer)u.get("id")).orElse(-1);
            String rNome = restBox.getValue();
            Object rId = abertos.stream().filter(r -> r.get("nome").equals(rNome))
                .findFirst().map(r -> r.get("id")).orElse(-1);
            return row("clienteNome", cNome, "clienteId", cId,
                "restNome", rNome, "restId", rId,
                "estado", "CRIADO", "total", String.format("%.2f", total),
                "pagamento", "-", "entregador", "-", "morada", moradaF.getText());
        });

        Map<String, Object> result = dlg.showAndWait().orElse(null);
        if (result != null && "0.00".equals(result.get("total"))) {
            warn("Selecione pelo menos um produto."); return null;
        }
        return result;
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

    private Button primaryButton(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-font-size: 13px; -fx-padding: 10 28; -fx-background-color: #ff5722; " +
                   "-fx-text-fill: white; -fx-background-radius: 6;");
        return b;
    }

    private Button colorBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-padding: 6 10; -fx-background-color: " + color + "; " +
                   "-fx-text-fill: white; -fx-font-size: 11px;");
        return b;
    }

    @SafeVarargs
    private <T> TableView<T> tableOf(ObservableList<T> items, TableColumn<T,String>... cols) {
        TableView<T> t = new TableView<>(items);
        t.getColumns().addAll(cols);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Sem dados para mostrar."));
        VBox.setVgrow(t, Priority.ALWAYS);
        return t;
    }

    private TableColumn<Map<String, Object>, String> col(String title, String key) {
        TableColumn<Map<String, Object>, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getOrDefault(key,"").toString()));
        return c;
    }

    private GridPane formGrid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        g.setPadding(new Insets(16));
        g.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                   "-fx-border-color: #ddd; -fx-border-radius: 8;");
        ColumnConstraints c1 = new ColumnConstraints(110), c2 = new ColumnConstraints(230);
        g.getColumnConstraints().addAll(c1, c2);
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
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
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
        return m == null ? "" : m.getOrDefault(k,"").toString();
    }

    private Map<String, Object> row(Object... pairs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) m.put(pairs[i].toString(), pairs[i+1]);
        return m;
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

    private void ok(Label l, String msg) { l.setStyle("-fx-text-fill: #388e3c; -fx-font-size: 13px;"); l.setText(msg); }
    private void err(Label l, String msg) { l.setStyle("-fx-text-fill: #c62828; -fx-font-size: 13px;"); l.setText(msg); }

    /**
     * Converte o papel GUI (CLIENT, ADMIN, COURIER) para o tipo de utilizador da API (Cliente, Admin, Entregador).
     */
    private String mapPapelToTipo(String papel) {
        if (papel == null) return "Cliente";
        return switch (papel) {
            case "CLIENT" -> "Cliente";
            case "ADMIN" -> "Admin";
            case "COURIER" -> "Entregador";
            default -> "Cliente";
        };
    }

    /**
     * Converte o tipo de utilizador da API (Cliente, Admin, Entregador) para papel GUI (CLIENT, ADMIN, COURIER).
     */
    private String mapTipoToRole(String tipo) {
        if (tipo == null) return "CLIENT";
        return switch (tipo) {
            case "Cliente" -> "CLIENT";
            case "Admin" -> "ADMIN";
            case "Entregador" -> "COURIER";
            default -> "CLIENT";
        };
    }

    // ─────────────────────────────────────────────────────────────────────
    // Legacy inner class kept for compatibility
    // ─────────────────────────────────────────────────────────────────────
    public static class UseCaseRow {
        public String id, descricao, web, native_;
        public UseCaseRow(String id, String descricao, String web, String native_) {
            this.id=id; this.descricao=descricao; this.web=web; this.native_=native_;
        }
    }
}
