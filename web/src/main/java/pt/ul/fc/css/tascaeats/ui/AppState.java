// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################


package pt.ul.fc.css.tascaeats.ui;

import java.util.Map;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
public class AppState {

    public static final String GRPC_HOST =
        System.getenv().getOrDefault("NATIVE_GRPC_HOST", "localhost");
    public static final int GRPC_PORT =
        Integer.parseInt(System.getenv().getOrDefault("NATIVE_GRPC_PORT", "8082"));

    // ── gRPC clients ─────────────────────────────────────────────────────
    public final NativeGrpcClient     grpc             = new NativeGrpcClient(GRPC_HOST, GRPC_PORT);
    public final UtilizadorApiClient  utilizadorClient = new UtilizadorApiClient(grpc);
    public final RestauranteApiClient restauranteClient = new RestauranteApiClient(grpc);

    // ── Live data ────────────────────────────────
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
