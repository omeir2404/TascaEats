package pt.ul.fc.css.tascaeats.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
/**
 * Shared application state: live data lists, current user, and HTTP clients.
 * All panes read from and write to this single instance.
 */
public class AppState {

    public static final String BACKEND =
        System.getenv().getOrDefault("BACKEND_API_URL", "http://localhost:8080");

    // ── HTTP clients ──────────────────────────────────────────────────────
    // HttpComponentsClientHttpRequestFactory is required to support PATCH
    public final RestTemplate         rest             = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    public final UtilizadorApiClient  utilizadorClient = new UtilizadorApiClient(rest, BACKEND);
    public final RestauranteApiClient restauranteClient = new RestauranteApiClient(rest, BACKEND);

    // ── Live data (populated from backend) ────────────────────────────────
    public final ObservableList<Map<String, Object>> users        = FXCollections.observableArrayList();
    public final ObservableList<Map<String, Object>> restaurantes = FXCollections.observableArrayList();
    public final ObservableList<Map<String, Object>> produtos     = FXCollections.observableArrayList();
    public final ObservableList<Map<String, Object>> pedidos      = FXCollections.observableArrayList();

    // ── Session ───────────────────────────────────────────────────────────
    public Map<String, Object> currentUser = null;

    public boolean isLoggedIn()  { return currentUser != null; }
    public boolean isAdmin()     { return currentUser != null && "ADMIN".equals(currentUser.getOrDefault("papel", "")); }
    public boolean isCourier()   { return currentUser != null && "COURIER".equals(currentUser.getOrDefault("papel", "")); }

    // ── Background task runner ────────────────────────────────────────────
    public <T> void bg(java.util.concurrent.Callable<T> work, Consumer<T> onDone) {
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return work.call(); }
        };
        task.setOnSucceeded(ev -> onDone.accept(task.getValue()));
        task.setOnFailed(ev -> System.err.println("BG task failed: " + task.getException()));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}
