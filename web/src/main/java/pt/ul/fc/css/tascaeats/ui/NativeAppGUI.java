package pt.ul.fc.css.tascaeats.ui;

import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

/**
 * Aplicação JavaFX nativa para consultar restaurantes no backend TascaEats.
 */
public class NativeAppGUI extends Application {

    private final ObservableList<RestauranteResumo> restaurantes = FXCollections.observableArrayList();

    private RestauranteApiClient apiClient;
    private TableView<RestauranteResumo> tableView;
    private TextField nomeField;
    private ComboBox<String> estadoComboBox;
    private Label statusLabel;
    private TextArea detalhesArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        String backendUrl = System.getenv().getOrDefault("BACKEND_API_URL", "http://localhost:8080");
        apiClient = new RestauranteApiClient(new RestTemplate(), backendUrl);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setTop(createHeader());
        root.setCenter(createContent());
        root.setBottom(createStatusBar());

        Scene scene = new Scene(root, 980, 680);
        stage.setTitle("TascaEats - GUI Nativa");
        stage.setScene(scene);
        stage.show();

        carregarRestaurantes();
    }

    private VBox createHeader() {
        Label title = new Label("TascaEats - GUI Nativa");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label("Consulta o backend REST a partir de uma janela JavaFX.");
        Label backend = new Label("Backend: " + apiClient.getBackendUrl() + "/api/restaurantes");
        backend.setStyle("-fx-text-fill: #555555;");

        VBox header = new VBox(4, title, subtitle, backend);
        header.setPadding(new Insets(0, 0, 14, 0));
        return header;
    }

    private HBox createContent() {
        VBox filters = createFilterPanel();
        HBox.setHgrow(filters, Priority.NEVER);

        VBox tableArea = new VBox(10, createTable(), createDetailsArea());
        HBox.setHgrow(tableArea, Priority.ALWAYS);

        HBox content = new HBox(16, filters, tableArea);
        HBox.setHgrow(tableArea, Priority.ALWAYS);
        return content;
    }

    private VBox createFilterPanel() {
        nomeField = new TextField();
        nomeField.setPromptText("Ex.: Tasca do Zé");
        nomeField.setMinWidth(220);

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
        form.add(new Label("Nome"), 0, 0);
        form.add(nomeField, 1, 0);
        form.add(new Label("Estado"), 0, 1);
        form.add(estadoComboBox, 1, 1);

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
        detalhesArea.setPrefRowCount(6);
        detalhesArea.setPromptText("Seleciona um restaurante para ver os detalhes.");

        VBox details = new VBox(6, new Label("Detalhes"), detalhesArea);
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

        Task<List<RestauranteResumo>> task = new Task<>() {
            @Override
            protected List<RestauranteResumo> call() {
                return apiClient.listarRestaurantes(nome, estado);
            }
        };

        task.setOnSucceeded(event -> {
            List<RestauranteResumo> resultado = task.getValue();
            restaurantes.setAll(resultado);
            if (resultado.isEmpty()) {
                statusLabel.setText("Nenhum restaurante encontrado com os filtros atuais.");
                detalhesArea.clear();
            } else {
                statusLabel.setText(resultado.size() + " restaurante(s) carregado(s) com sucesso.");
            }
        });

        task.setOnFailed(event -> {
            restaurantes.clear();
            detalhesArea.clear();
            Throwable error = task.getException();
            String message = error == null ? "Erro desconhecido." : error.getMessage();
            statusLabel.setText("Falha ao contactar o backend: " + message);
            mostrarErro("Não foi possível carregar restaurantes", message);
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

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("TascaEats");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}


