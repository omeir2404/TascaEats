package pt.ul.fc.css.tascaeats.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Map;

import static pt.ul.fc.css.tascaeats.ui.UiUtils.*;

/**
 * Application entry point.
 * Responsible only for navigation, the top bar, and wiring the panes together.
 * All business logic lives in the individual pane classes.
 */
public class NativeAppGUI extends Application {

    private final AppState state = new AppState();

    // ── UI ────────────────────────────────────────────────────────────────
    private Stage  mainStage;
    private VBox   contentArea;
    private Label  headerUserLabel;
    private Button loginBtn, usersBtn, navRestBtn, navProdBtn, navOrderBtn, exitBtn;

    // ── Panes (created once, reused) ──────────────────────────────────────
    private UtilizadoresPane utilizadoresPane;
    private RestaurantesPane restaurantesPane;
    private ProdutosPane     produtosPane;
    private PedidosPane      pedidosPane;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        mainStage = stage;

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

    // ── Navigation header ─────────────────────────────────────────────────

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

        loginBtn  = navBtn("A – Login",          this::showLoginPane);
        usersBtn  = navBtn("C/D – Utilizadores", this::showUsersPane);
        navRestBtn  = navBtn("E/F – Restaurantes",  this::showRestaurantesPane);
        navProdBtn  = navBtn("G/H – Produtos",       () -> showProdutosPane(null));
        navOrderBtn = navBtn("K/L/M/N – Pedidos",    this::showOrdersPane);

        updateNavState();

        header.getChildren().addAll(title, loginBtn, usersBtn,
            navRestBtn, navProdBtn, navOrderBtn, spacer, headerUserLabel, exitBtn);
        return header;
    }

    private void updateNavState() {
        boolean loggedIn = state.isLoggedIn();
        loginBtn.setVisible(!loggedIn);
        loginBtn.setManaged(!loggedIn);
        usersBtn.setVisible(loggedIn);
        usersBtn.setManaged(loggedIn);
        usersBtn.setDisable(!loggedIn);
        navRestBtn.setVisible(loggedIn);
        navRestBtn.setManaged(loggedIn);
        navRestBtn.setDisable(!loggedIn);
        navProdBtn.setVisible(loggedIn);
        navProdBtn.setManaged(loggedIn);
        navProdBtn.setDisable(!loggedIn);
        navOrderBtn.setVisible(loggedIn);
        navOrderBtn.setManaged(loggedIn);
        navOrderBtn.setDisable(!loggedIn);
        exitBtn.setText(loggedIn ? "Logout" : "Sair");
        exitBtn.setOnAction(e -> {
            if (loggedIn) {
                state.currentUser = null;
                headerUserLabel.setText("Não autenticado");
                updateNavState();
                showLoginPane();
            } else {
                Platform.exit();
            }
        });
    }

    // ── Navigation methods ────────────────────────────────────────────────

    private void show(javafx.scene.Node node) { contentArea.getChildren().setAll(node); }

    private boolean requireLogin() {
        if (state.isLoggedIn()) return true;
        headerUserLabel.setText("Não autenticado");
        updateNavState();
        showLoginPane();
        return false;
    }

    private void showLoginPane() {
        LoginPane pane = new LoginPane(state, mainStage,
            this::onLoginSuccess,
            this::showRegisterPane);
        show(pane.build());
    }

    private void showRegisterPane() {
        RegisterPane pane = new RegisterPane(state, mainStage, this::showLoginPane);
        show(pane.build());
    }

    private void showUsersPane() {
        if (!requireLogin()) return;
        utilizadoresPane = new UtilizadoresPane(state, mainStage);
        show(utilizadoresPane.build());
    }

    private void showRestaurantesPane() {
        if (!requireLogin()) return;
        restaurantesPane = new RestaurantesPane(state, mainStage, this::showProdutosPane);
        show(restaurantesPane.build());
    }

    private void showProdutosPane(Map<String, Object> restFilter) {
        if (!requireLogin()) return;
        // Ensure restaurantes are loaded before building the produtos pane
        if (state.restaurantes.isEmpty() && restaurantesPane != null) {
            restaurantesPane.syncRestaurantes(() -> buildAndShowProdutos(restFilter));
        } else {
            buildAndShowProdutos(restFilter);
        }
    }

    private void buildAndShowProdutos(Map<String, Object> restFilter) {
        if (!requireLogin()) return;
        produtosPane = new ProdutosPane(state, mainStage, this::showRestaurantesPane);
        show(produtosPane.build(restFilter));
    }

    private void showOrdersPane() {
        if (!requireLogin()) return;
        pedidosPane = new PedidosPane(state, mainStage);
        show(pedidosPane.build());
    }

    // ── Login callback ────────────────────────────────────────────────────

    private void onLoginSuccess() {
        String nome  = UiUtils.s(state.currentUser, "nome");
        String papel = UiUtils.s(state.currentUser, "papel");
        headerUserLabel.setText("Olá, " + nome + " (" + papel + ")");
        updateNavState();
        showRestaurantesPane();
    }

    // ── Legacy inner class kept for compatibility ─────────────────────────
    public static class UseCaseRow {
        public String id, descricao, web, native_;
        public UseCaseRow(String id, String descricao, String web, String native_) {
            this.id = id; this.descricao = descricao; this.web = web; this.native_ = native_;
        }
    }
}
