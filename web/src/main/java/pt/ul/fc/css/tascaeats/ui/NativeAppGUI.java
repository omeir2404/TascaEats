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

/**
 * Aplicação JavaFX nativa para TascaEats.
 * Implementa todos os casos de uso A–N com dados em memória (mock) e
 * tentativa de sincronização opcional com o backend REST.
 */
public class NativeAppGUI extends Application {

    // ── Mock data (in-memory) ─────────────────────────────────────────────
    private final ObservableList<Map<String, Object>> mockUsers        = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> mockRestaurantes = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> mockProdutos     = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> mockPedidos      = FXCollections.observableArrayList();

    private int nextUserId = 5, nextRestId = 5, nextProdId = 6, nextPedidoId = 4;

    // ── Session ───────────────────────────────────────────────────────────
    private Map<String, Object> currentUser = null;

    // ── Backend client (optional, for restaurant sync) ────────────────────
    private RestauranteApiClient apiClient;

    // ── UI state ──────────────────────────────────────────────────────────
    private Stage mainStage;
    private VBox  contentArea;
    private Label headerUserLabel;
    private Label restStatusLabel;
    private Button navRestaurantesBtn, navProductsBtn, navOrdersBtn;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        initMockData();
        apiClient = new RestauranteApiClient(
            new RestTemplate(),
            System.getenv().getOrDefault("BACKEND_API_URL", "http://localhost:8080")
        );

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
    // Init mock data
    // ─────────────────────────────────────────────────────────────────────
    private void initMockData() {
        mockUsers.addAll(
            row("id",1,"nome","João Silva",   "email","joao@email.com",   "password","123","papel","CLIENT", "ativo","Sim"),
            row("id",2,"nome","Maria Santos", "email","maria@email.com",  "password","123","papel","ADMIN",  "ativo","Sim"),
            row("id",3,"nome","Carlos Ferreira","email","carlos@email.com","password","123","papel","COURIER","ativo","Sim"),
            row("id",4,"nome","Ana Costa",    "email","ana@email.com",    "password","123","papel","CLIENT", "ativo","Não")
        );
        mockRestaurantes.addAll(
            row("id",1,"nome","Tasca do Zé",  "nif","123456789","estado","ABERTO",   "cidade","Lisboa"),
            row("id",2,"nome","Bella Napoli", "nif","987654321","estado","FECHADO",  "cidade","Porto"),
            row("id",3,"nome","Sushi Zen",    "nif","456789123","estado","ABERTO",   "cidade","Lisboa"),
            row("id",4,"nome","Café Central", "nif","321654987","estado","RESERVADO","cidade","Coimbra")
        );
        mockProdutos.addAll(
            row("id",1,"restId",1,"restNome","Tasca do Zé","nome","Bacalhau à Brás",       "preco","12.50","categoria","PRATO",    "disponivel","Sim"),
            row("id",2,"restId",1,"restNome","Tasca do Zé","nome","Super Bock 33cl",        "preco","2.00", "categoria","BEBIDA",   "disponivel","Sim"),
            row("id",3,"restId",1,"restNome","Tasca do Zé","nome","Pastel de Nata",         "preco","1.50", "categoria","SOBREMESA","disponivel","Não"),
            row("id",4,"restId",3,"restNome","Sushi Zen",  "nome","Combinado Salmon 15 pcs","preco","22.00","categoria","PRATO",    "disponivel","Sim"),
            row("id",5,"restId",3,"restNome","Sushi Zen",  "nome","Chá Verde",              "preco","3.00", "categoria","BEBIDA",   "disponivel","Sim")
        );
        mockPedidos.addAll(
            row("id",1,"clienteNome","João Silva","clienteId",1,"restNome","Tasca do Zé","restId",1,
                "estado","CRIADO","total","14.50","pagamento","-","entregador","-"),
            row("id",2,"clienteNome","Ana Costa", "clienteId",4,"restNome","Sushi Zen",  "restId",3,
                "estado","PAGO",  "total","25.00","pagamento","MBWAY","entregador","-"),
            row("id",3,"clienteNome","João Silva","clienteId",1,"restNome","Tasca do Zé","restId",1,
                "estado","ENTREGUE","total","32.75","pagamento","DINHEIRO","entregador","Carlos Ferreira")
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // Navigation header
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

        Button exitBtn = new Button("Sair");
        exitBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6 14; -fx-background-color: #c62828; -fx-text-fill: white;");
        exitBtn.setOnAction(e -> Platform.exit());

        Button loginBtn = navBtn("A - Login", this::showLoginPane);
        Button usersBtn = navBtn("B/C/D - Utilizadores", this::showUsersPane);
        navRestaurantesBtn = navBtn("E/F - Restaurantes", this::showRestaurantesPane);
        navProductsBtn = navBtn("G/H/I – Produtos",     () -> show(buildProductsPane(null)));
        navOrdersBtn   = navBtn("J/K/L/M/N – Pedidos",  this::showOrdersPane);

        // Desabilitar botões de navegação até login bem-sucedido
        updateNavButtonsState();

        header.getChildren().addAll(
            title,
            loginBtn,
            usersBtn,
            navRestaurantesBtn,
            navProductsBtn,
            navOrdersBtn,
            spacer, headerUserLabel, exitBtn
        );
        return header;
    }

    private Button navBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size: 11px; -fx-padding: 6 11; -fx-background-color: transparent; " +
                   "-fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 4;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private void updateNavButtonsState() {
        // Apenas "C/D - Utilizadores" funciona sem login
        // Depois de login bem-sucedido, todos os botões ficam ativos
        navRestaurantesBtn.setDisable(currentUser == null);
        navProductsBtn.setDisable(currentUser == null);
        navOrdersBtn.setDisable(currentUser == null);
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

        TextField    emailF = new TextField();  emailF.setPromptText("email@exemplo.com");
        PasswordField passF = new PasswordField(); passF.setPromptText("password");

        GridPane form = formGrid();
        form.add(lbl("Email:"),    0, 0); form.add(emailF, 1, 0);
        form.add(lbl("Password:"), 0, 1); form.add(passF,  1, 1);

        Label feedback = new Label();
        Button loginBtn = primaryButton("Entrar");
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> {
            String email = emailF.getText().trim();
            Optional<Map<String, Object>> found = mockUsers.stream()
                .filter(u -> u.get("email").toString().equalsIgnoreCase(email))
                .findFirst();
            if (found.isPresent()) {
                currentUser = found.get();
                updateNavButtonsState();
                headerUserLabel.setText("Olá, " + currentUser.get("nome") + " (" + currentUser.get("papel") + ")");
                ok(feedback, "✓ Bem-vindo, " + currentUser.get("nome") + "!");
            } else {
                err(feedback, "✗ Utilizador não encontrado. Experimente: joao@email.com");
            }
        });

        Button regBtn = new Button("Ainda não tens conta? Registar");
        regBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1565c0; -fx-border-color: transparent;");
        regBtn.setOnAction(e -> showUsersPane());

        Label hint = new Label("Demo: joao@email.com | maria@email.com | carlos@email.com | ana@email.com");
        hint.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");

        VBox box = centeredCard(title, form, loginBtn, feedback, regBtn, hint);
        box.setMaxWidth(420);
        VBox wrapper = new VBox(box);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(40));
        wrapper.setStyle("-fx-background-color: #f8f9fa;");
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────
    // B / C / D – Utilizadores
    // ─────────────────────────────────────────────────────────────────────
    private VBox buildUsersPane() {
        ObservableList<Map<String, Object>> display = FXCollections.observableArrayList(mockUsers);
        TableView<Map<String, Object>> table = tableOf(display,
            col("ID","id"), col("Nome","nome"), col("Email","email"),
            col("Papel","papel"), col("Ativo","ativo")
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
                novo.put("id", nextUserId++);
                mockUsers.add(novo);
                display.setAll(mockUsers);
                info("Utilizador registado.");
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

        return pane(sectionTitle("B/C/D – Gestão de Utilizadores"),
            hbar(lbl("Nome:"), searchF, lbl("Papel:"), papelBox, filtrar),
            hbar(criarBtn, editarBtn, removerBtn),
            table
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

        form.add(new Label("Email:"), 0, 0);
        form.add(emailField, 1, 0);
        form.add(new Label("Palavra-passe:"), 0, 1);
        form.add(passwordField, 1, 1);
        form.add(loginBtn, 1, 2);

        container.getChildren().addAll(title, form, resultLabel);
        return container;
    }

    // ========== Caso C/D: Utilizadores ==========
    private VBox createUsersPane() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("C/D - Gestão de Utilizadores");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222;");

        HBox toolbar = new HBox(8);
        Button criarBtn = new Button("+ Criar");
        Button editarBtn = new Button("✎ Editar");
        Button removerBtn = new Button("✕ Remover");
        Button listarBtn = new Button("⟳ Listar");
        criarBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #4caf50; -fx-text-fill: white;");
        editarBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #2196f3; -fx-text-fill: white;");
        removerBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #f44336; -fx-text-fill: white;");
        listarBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #ff9800; -fx-text-fill: white;");
        toolbar.getChildren().addAll(criarBtn, editarBtn, removerBtn, listarBtn);

        TableView<Map<String, String>> table = new TableView<>();
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
        data.addAll(
                Map.of("ID", "1", "Nome", "Admin 1", "Email", "admin@example.com", "Tipo", "ADMIN"),
                Map.of("ID", "2", "Nome", "Cliente 1", "Email", "cliente@example.com", "Tipo", "CLIENT"),
                Map.of("ID", "3", "Nome", "Entregador 1", "Email", "driver@example.com", "Tipo", "DRIVER")
        );
        table.setItems(data);
        table.setPlaceholder(new Label("Nenhum utilizador encontrado."));

        TableColumn<Map<String, String>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("ID")));
        TableColumn<Map<String, String>, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Nome")));
        TableColumn<Map<String, String>, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Email")));
        TableColumn<Map<String, String>, String> tipoCol = new TableColumn<>("Tipo");
        tipoCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Tipo")));

        table.getColumns().addAll(idCol, nomeCol, emailCol, tipoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label statusLabel = new Label("Dados demo — não implementado ainda.");
        statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10px;");
        container.getChildren().addAll(title, toolbar, table, statusLabel);
        return container;
    }

    // ========== Caso E/F: Restaurantes ==========
    private VBox createRestaurantesPane() {
        VBox filters = createFilterPanel();
        HBox.setHgrow(filters, Priority.NEVER);

        VBox tableArea = new VBox(5, createTable(), createDetailsArea());
        HBox.setHgrow(tableArea, Priority.ALWAYS);
        VBox.setVgrow(tableArea, Priority.ALWAYS);

        HBox content = new HBox(16, filters, tableArea);
        content.setPadding(new Insets(16));
        HBox.setHgrow(tableArea, Priority.ALWAYS);
        HBox.setHgrow(content, Priority.ALWAYS);

        statusLabel = new Label("Pronto para pesquisar restaurantes.");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox();
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().addAll(content, statusBar);

        // Carregar restaurantes quando a aba é criada
        Platform.runLater(this::carregarRestaurantes);

        return root;
    }

    // ========== Caso G/H: Produtos ==========
    private VBox createProductsPane() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("G/H - Gestão de Produtos");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222;");

        HBox toolbar = new HBox(8);
        Button criarBtn = new Button("+ Criar Produto");
        Button editarBtn = new Button("✎ Editar");
        Button indisponBtn = new Button("↓ Indisponível");
        Button apagarBtn = new Button("✕ Apagar");
        criarBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #4caf50; -fx-text-fill: white;");
        editarBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #2196f3; -fx-text-fill: white;");
        indisponBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #ff9800; -fx-text-fill: white;");
        apagarBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #f44336; -fx-text-fill: white;");
        toolbar.getChildren().addAll(criarBtn, editarBtn, indisponBtn, apagarBtn);

        TableView<Map<String, String>> table = new TableView<>();
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
        data.addAll(
                Map.of("ID", "1", "Nome", "Francesinha", "Preço", "8.50 €", "Disponível", "Sim"),
                Map.of("ID", "2", "Nome", "Arroz de Marisco", "Preço", "14.00 €", "Disponível", "Sim"),
                Map.of("ID", "3", "Nome", "Caldo Verde", "Preço", "3.50 €", "Disponível", "Não")
        );
        table.setItems(data);
        table.setPlaceholder(new Label("Nenhum produto encontrado."));

        TableColumn<Map<String, String>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("ID")));
        TableColumn<Map<String, String>, String> nomeCol = new TableColumn<>("Nome");
        nomeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Nome")));
        TableColumn<Map<String, String>, String> precoCol = new TableColumn<>("Preço");
        precoCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Preço")));
        TableColumn<Map<String, String>, String> dispCol = new TableColumn<>("Disponível");
        dispCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Disponível")));

        table.getColumns().addAll(idCol, nomeCol, precoCol, dispCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label statusLabel = new Label("Dados demo — não implementado ainda.");
        statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10px;");
        container.getChildren().addAll(title, toolbar, table, statusLabel);
        return container;
    }

    // ========== Caso K/L/M/N: Pedidos e Entregas ==========
    private VBox createOrdersPane() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f8f9fa;");

        Label title = new Label("K/L/M/N - Gestão de Pedidos e Entregas");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222;");

        HBox toolbar = new HBox(8);
        Button criarBtn = new Button("+ Criar Pedido");
        Button pagarBtn = new Button("💳 Processar Pagamento");
        Button prepBtn = new Button("⌛ Iniciar Preparação");
        Button prontoBtn = new Button("✓ Marcar Pronto");
        Button atribuirBtn = new Button("🚗 Atribuir Entregador");
        Button entrBtn = new Button("✓ Registar Entrega");
        Button cancelBtn = new Button("✕ Cancelar");
        criarBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #4caf50; -fx-text-fill: white;");
        pagarBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #2196f3; -fx-text-fill: white;");
        prepBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #ff9800; -fx-text-fill: white;");
        prontoBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #8bc34a; -fx-text-fill: white;");
        atribuirBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #9c27b0; -fx-text-fill: white;");
        entrBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #00bcd4; -fx-text-fill: white;");
        cancelBtn.setStyle("-fx-padding: 6 12; -fx-background-color: #f44336; -fx-text-fill: white;");
        toolbar.getChildren().addAll(criarBtn, pagarBtn, prepBtn, prontoBtn, atribuirBtn, entrBtn, cancelBtn);

        TableView<Map<String, String>> table = new TableView<>();
        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
        data.addAll(
                Map.of("ID", "1", "Cliente", "João Silva", "Estado", "CRIADO", "Total", "25.50 €"),
                Map.of("ID", "2", "Cliente", "Maria Santos", "Estado", "PAGO", "Total", "18.00 €"),
                Map.of("ID", "3", "Cliente", "Pedro Costa", "Estado", "ENTREGUE", "Total", "32.75 €")
        );
        table.setItems(data);
        table.setPlaceholder(new Label("Nenhum pedido encontrado."));

        TableColumn<Map<String, String>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("ID")));
        TableColumn<Map<String, String>, String> clienteCol = new TableColumn<>("Cliente");
        clienteCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Cliente")));
        TableColumn<Map<String, String>, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Estado")));
        TableColumn<Map<String, String>, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Total")));

        table.getColumns().addAll(idCol, clienteCol, estadoCol, totalCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label statusLabel = new Label("Dados demo — não implementado ainda.");
        statusLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10px;");
        container.getChildren().addAll(title, toolbar, table, statusLabel);
        return container;
    }

//    private VBox createStatusContent() {
//        VBox container = new VBox(12);
//        container.setPadding(new Insets(20));
//        container.setStyle("-fx-background-color: #f8f9fa;");
//        VBox.setVgrow(container, Priority.ALWAYS);
//
//        // Cabeçalho
//        VBox header = new VBox(8);
//        Label title = new Label("Comparação de Casos de Uso");
//        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #222;");
//        Label subtitle = new Label("Estado de implementação por interface");
//        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
//        header.getChildren().addAll(title, subtitle);
//
//        // Legenda de cores
//        HBox legend = createLegend();
//
//        // Tabela de status
//        TableView<UseCaseRow> table = createStatusTable();
//        VBox.setVgrow(table, Priority.ALWAYS);
//
//        container.getChildren().addAll(header, legend, table);
//        return container;
//    }

    private HBox createLegend() {
        HBox legend = new HBox(16);
        legend.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-radius: 8; -fx-background-radius: 8;");
        legend.setPadding(new Insets(10));

        legend.getChildren().addAll(
                createLegendItemNew("■ Implementado", "#4caf50"),
                createLegendItemNew("■ Parcial", "#ff9800"),
                createLegendItemNew("■ Pendente", "#f44336"),
                createLegendItemNew("■ N/A", "#9e9e9e")
        );
        return legend;
    }

    private HBox createLegendItemNew(String label, String color) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        Label text = new Label(label);
        text.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        item.getChildren().add(text);
        return item;
    }

    private TableView<UseCaseRow> createStatusTable() {
        ObservableList<UseCaseRow> data = FXCollections.observableArrayList();

        // Dados dos casos de uso
        String[][] useCases = {
                {"A", "Login com autenticação (mock)", "Implementado", "Pendente"},
                {"B", "Registo de Clientes", "Parcial", "N/A"},
                {"C", "Registo de Administradores/Entregadores", "N/A", "Pendente"},
                {"D", "Verificar, remover e atualizar utilizadores", "N/A", "Pendente"},
                {"E", "Buscar por restaurantes", "Parcial", "Implementado"},
                {"F", "Gerir Restaurantes", "N/A", "Pendente"},
                {"G", "Gerir Menus", "N/A", "Pendente"},
                {"H", "Gerir Produtos", "N/A", "Pendente"},
                {"I", "Buscar produtos de um restaurante", "Pendente", "N/A"},
                {"J", "Criação de um pedido", "Pendente", "N/A"},
                {"K", "Registo e processamento de pagamento", "N/A", "Pendente"},
                {"L", "Atualização do estado do pedido", "N/A", "Pendente"},
                {"M", "Atribuição de um entregador a um pedido", "N/A", "Pendente"},
                {"N", "Cancelamento de um pedido", "Pendente", "Pendente"}
        };

        for (String[] row : useCases) {
            data.add(new UseCaseRow(row[0], row[1], row[2], row[3]));
        }

        TableView<UseCaseRow> table = new TableView<>(data);
        table.setStyle("-fx-font-size: 11px;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UseCaseRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().id));
        idCol.setMaxWidth(1f * Integer.MAX_VALUE * 5);

        TableColumn<UseCaseRow, String> descCol = new TableColumn<>("Descrição");
        descCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().descricao));
        descCol.setMaxWidth(1f * Integer.MAX_VALUE * 40);

        TableColumn<UseCaseRow, String> webCol = new TableColumn<>("Web Browser");
        webCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().web));
        webCol.setMaxWidth(1f * Integer.MAX_VALUE * 25);

        TableColumn<UseCaseRow, String> nativeCol = new TableColumn<>("Native JavaFX");
        nativeCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().native_));
        nativeCol.setMaxWidth(1f * Integer.MAX_VALUE * 25);

        table.getColumns().addAll(idCol, descCol, webCol, nativeCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private VBox createFilterPanel() {
        nomeField = new TextField();
        nomeField.setPromptText("Ex.: Tasca do Zé");
        nomeField.setMaxWidth(Double.MAX_VALUE);

        estadoComboBox = new ComboBox<>(FXCollections.observableArrayList("Todos", "ABERTO", "FECHADO", "RESERVADO"));
        estadoComboBox.getSelectionModel().selectFirst();
        estadoComboBox.setMaxWidth(Double.MAX_VALUE);

        Button pesquisarButton = new Button("Pesquisar");
        pesquisarButton.setMaxWidth(Double.MAX_VALUE);
        pesquisarButton.setDefaultButton(true);
        pesquisarButton.setOnAction(event -> carregarRestaurantes());

        Button limparButton = new Button("Limpar");
        limparButton.setMaxWidth(Double.MAX_VALUE);
        limparButton.setOnAction(event -> {
            nomeField.clear();
            estadoComboBox.getSelectionModel().selectFirst();
            carregarRestaurantes();
        });

        Button atualizarButton = new Button("Atualizar");
        atualizarButton.setMaxWidth(Double.MAX_VALUE);
        atualizarButton.setOnAction(event -> carregarRestaurantes());

        Button sairButton = new Button("Sair");
        sairButton.setMaxWidth(Double.MAX_VALUE);
        sairButton.setOnAction(event -> Platform.exit());

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(10);
        form.setMaxWidth(Double.MAX_VALUE);
        form.add(new Label("Nome"), 0, 0);
        form.add(nomeField, 1, 0);
        form.add(new Label("Estado"), 0, 1);
        form.add(estadoComboBox, 1, 1);
        GridPane.setHgrow(nomeField, Priority.ALWAYS);
        GridPane.setHgrow(estadoComboBox, Priority.ALWAYS);

        VBox panel = new VBox(12,
                new Label("Filtros"),
                form,
                pesquisarButton,
                atualizarButton,
                limparButton,
                sairButton);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(280);
        panel.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dddddd;");
        return panel;
    }

    private TableView<RestauranteResumo> createTable() {
        tableView = new TableView<>(restaurantes);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RestauranteResumo, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().id() == null ? "-" : String.valueOf(cell.getValue().id())));

        TableColumn<RestauranteResumo, String> nomeColumn = new TableColumn<>("Nome");
        nomeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().nome()));

        TableColumn<RestauranteResumo, String> estadoColumn = new TableColumn<>("Estado");
        estadoColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().estado()));

        tableView.getColumns().addAll(idColumn, nomeColumn, estadoColumn);
        tableView.setPlaceholder(new Label("Sem restaurantes para mostrar."));
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateDetails(newValue));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return tableView;
    }

    private VBox createDetailsArea() {
        detalhesArea = new TextArea();
        detalhesArea.setEditable(false);
        detalhesArea.setWrapText(true);
        detalhesArea.setPromptText("Seleciona um restaurante para ver os detalhes.");
        VBox.setVgrow(detalhesArea, Priority.ALWAYS);

        VBox details = new VBox(6, new Label("Detalhes"), detalhesArea);
        VBox.setVgrow(details, Priority.ALWAYS);
        return details;
    }

    private HBox createStatusBar() {
        statusLabel = new Label("Pronto para pesquisar restaurantes.");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(12, 0, 0, 0));
        return statusBar;
    }

    private void carregarRestaurantes() {
        String nome = nomeField.getText();
        String selectedEstado = estadoComboBox.getValue();
        String estado = selectedEstado != null && selectedEstado.equalsIgnoreCase("Todos") ? null : selectedEstado;

        statusLabel.setText("A carregar restaurantes...");

        Task<RestauranteApiClient.RestaurantSearchResult> task = new Task<>() {
            @Override
            protected RestauranteApiClient.RestaurantSearchResult call() {
                return apiClient.listarRestaurantesComFallback(nome, estado);
            }
        };

        task.setOnSucceeded(event -> {
            RestauranteApiClient.RestaurantSearchResult resultado = task.getValue();
            restaurantes.setAll(resultado.restaurantes());
            if (resultado.fallbackUsado()) {
                statusLabel.setText(resultado.mensagem());
            } else if (resultado.restaurantes().isEmpty()) {
                statusLabel.setText("Nenhum restaurante encontrado com os filtros atuais.");
                detalhesArea.clear();
            } else {
                statusLabel.setText(resultado.mensagem());
            }
        });

        task.setOnFailed(event -> {
            restaurantes.clear();
            detalhesArea.clear();
            Throwable error = task.getException();
            String message = error == null ? "Erro desconhecido." : error.getMessage();
            statusLabel.setText("Falha ao contactar o backend: " + message);
        });

        Thread worker = new Thread(task, "tascaeats-restaurantes-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void updateDetails(RestauranteResumo restaurante) {
        if (restaurante == null) {
            detalhesArea.clear();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(restaurante.id() == null ? "-" : restaurante.id()).append('\n');
        details.append("Nome: ").append(restaurante.nome()).append('\n');
        details.append("Estado: ").append(restaurante.estado()).append('\n');
        details.append("Fonte: ").append(apiClient.getBackendUrl()).append("/api/restaurantes");

        detalhesArea.setText(details.toString());
    }


    /**
     * Classe simples para representar uma linha na tabela de casos de uso.
     */
    public static class UseCaseRow {
        public String id;
        public String descricao;
        public String web;
        public String native_;

        public UseCaseRow(String id, String descricao, String web, String native_) {
            this.id = id;
            this.descricao = descricao;
            this.web = web;
            this.native_ = native_;
        }
    }
}



