// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################


package pt.ul.fc.css.tascaeats.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Objects;


public final class UiUtils {

    private UiUtils() {}

    // ── Labels ────────────────────────────────────────────────────────────

    public static Label lbl(String t) { return new Label(t); }

    public static Label sectionTitle(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222;");
        return l;
    }

    public static Label statusLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        return l;
    }

    public static void setStatus(Label l, String text) {
        if (l != null) javafx.application.Platform.runLater(() -> l.setText(text));
    }

    public static void ok(Label l, String msg) {
        l.setStyle("-fx-text-fill: #388e3c; -fx-font-size: 13px;"); l.setText(msg);
    }

    public static void err(Label l, String msg) {
        l.setStyle("-fx-text-fill: #c62828; -fx-font-size: 13px;"); l.setText(msg);
    }

    // ── Buttons ───────────────────────────────────────────────────────────

    public static Button primaryButton(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-font-size: 13px; -fx-padding: 10 28; -fx-background-color: #ff5722;" +
                   "-fx-text-fill: white; -fx-background-radius: 6;");
        return b;
    }

    public static Button colorBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-padding: 6 10; -fx-background-color: " + color + ";" +
                   "-fx-text-fill: white; -fx-font-size: 11px;");
        return b;
    }

    public static Button navBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size: 11px; -fx-padding: 6 11; -fx-background-color: transparent;" +
                   "-fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 4;");
        b.setOnAction(e -> action.run());
        return b;
    }

    // ── Form helpers ──────────────────────────────────────────────────────

    public static GridPane formGrid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        g.setPadding(new Insets(16));
        g.setStyle("-fx-background-color: white; -fx-background-radius: 8;" +
                   "-fx-border-color: #ddd; -fx-border-radius: 8;");
        g.getColumnConstraints().addAll(new ColumnConstraints(110), new ColumnConstraints(230));
        return g;
    }

    public static TextField field(String prompt, double w) {
        TextField f = new TextField(); f.setPromptText(prompt); f.setPrefWidth(w); return f;
    }

    @SuppressWarnings("unchecked")
    public static <T> ComboBox<T> combo(T... items) {
        ComboBox<T> c = new ComboBox<>(FXCollections.observableArrayList(items));
        c.getSelectionModel().selectFirst(); return c;
    }

    public static Dialog<Map<String, Object>> dialog(String title, Stage owner) {
        Dialog<Map<String, Object>> d = new Dialog<>();
        d.setTitle(title); d.initOwner(owner); return d;
    }

    // ── Layout helpers ────────────────────────────────────────────────────

    public static VBox centeredCard(javafx.scene.Node... nodes) {
        VBox box = new VBox(12, nodes);
        box.setPadding(new Insets(24));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                     "-fx-border-color: #ddd; -fx-border-radius: 10;");
        return box;
    }

    public static VBox pane(javafx.scene.Node... nodes) {
        VBox v = new VBox(10, nodes);
        v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color: #f8f9fa;");
        VBox.setVgrow(v, Priority.ALWAYS);
        return v;
    }

    public static HBox hbar(javafx.scene.Node... nodes) {
        HBox h = new HBox(8, nodes);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ── Table helpers ─────────────────────────────────────────────────────

    @SafeVarargs
    public static <T> TableView<T> tableOf(ObservableList<T> items, TableColumn<T, String>... cols) {
        TableView<T> t = new TableView<>(items);
        t.getColumns().addAll(cols);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Sem dados para mostrar."));
        VBox.setVgrow(t, Priority.ALWAYS);
        return t;
    }

    public static TableColumn<Map<String, Object>, String> col(String title, String key) {
        TableColumn<Map<String, Object>, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(
            Objects.toString(cd.getValue().get(key), "")));
        return c;
    }

    public static Map<String, Object> selected(TableView<Map<String, Object>> t, Stage owner) {
        Map<String, Object> sel = t.getSelectionModel().getSelectedItem();
        if (sel == null) warn("Selecione um item da tabela.", owner);
        return sel;
    }

    // ── Dialogs ───────────────────────────────────────────────────────────

    public static void info(String msg, Stage owner) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(owner); a.setTitle("Sucesso"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public static void warn(String msg, Stage owner) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.initOwner(owner); a.setTitle("Aviso"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public static boolean confirm(String msg, Stage owner) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.initOwner(owner); a.setTitle("Confirmar"); a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait().map(b -> b == ButtonType.OK).orElse(false);
    }

    // ── Data helpers ──────────────────────────────────────────────────────

    public static String s(Map<String, Object> m, String k) {
        return m == null ? "" : Objects.toString(m.getOrDefault(k, ""), "");
    }

    public static Map<String, Object> row(Object... pairs) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) m.put(pairs[i].toString(), pairs[i + 1]);
        return m;
    }

    public static Long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(Objects.toString(v, "-1")); }
        catch (NumberFormatException e) { return -1L; }
    }

    public static double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(Objects.toString(v, "0")); }
        catch (NumberFormatException e) { return 0.0; }
    }
}
