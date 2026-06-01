// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################


package pt.ul.fc.css.tascaeats.ui;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UtilizadorApiClient {

    private final NativeGrpcClient grpcClient;

    // ── Records ───────────────────────────────────────────────────────────

    public record RegistoResult(Long id, String email, String type, boolean fallbackUsado, String mensagem) {}
    public record LoginResult(boolean sucesso, boolean fallbackUsado, String mensagem) {}
    public record SimpleResult(boolean sucesso, boolean fallbackUsado, String mensagem) {}

    // ── Constructor ───────────────────────────────────────────────────────

    public UtilizadorApiClient(NativeGrpcClient grpcClient) {
        this.grpcClient = grpcClient;
    }

    // ── A: Login ──────────────────────────────────────────────────────────

    public LoginResult login(String email, String password) {
        Map<String, Object> payload = Map.of(
            "email",    email    != null ? email.trim()    : "",
            "password", password != null ? password.trim() : ""
        );
        NativeGrpcClient.GrpcResult resp = grpcClient.invoke("login", payload);
        if (resp.success()) {
            return new LoginResult(true, false, "Login bem-sucedido");
        }
        if (resp.message() != null && resp.message().contains("401")) {
            return new LoginResult(false, false, "Email ou password incorretos.");
        }
        return new LoginResult(false, true, "Backend gRPC indisponível ou erro: " + resp.message());
    }

    // ── C: Registar utilizador ────────────────────────────────────────────

    public RegistoResult registarUtilizador(String nome, String email, String password, String tipo) {
        Map<String, Object> payload = Map.of(
            "name",     nome     != null ? nome.trim()     : "",
            "email",    email    != null ? email.trim()    : "",
            "password", password != null ? password.trim() : "",
            "type",     tipo     != null ? tipo.trim()     : "client"
        );
        NativeGrpcClient.GrpcResult resp = grpcClient.invoke("registerUser", payload);
        if (!resp.success()) {
            return new RegistoResult(null, email, tipo, true, "Erro gRPC: " + resp.message());
        }
        Map<String, Object> body = grpcClient.readMap(resp.bodyJson());
        Long id        = extractLong(body, "id");
        String respEmail = Objects.toString(body.get("email"), email);
        String respType  = Objects.toString(body.get("type"),  tipo);
        String msg       = msgFrom(body, "Utilizador registado com sucesso");
        return new RegistoResult(id, respEmail, respType, false, msg);
    }

    // ── D: Listar, editar e remover utilizadores ──────────────────────────


    public List<Map<String, Object>> listarUtilizadores() {
        return grpcClient.invokeList("listUsers", Map.of()).stream()
            .map(this::normalizeUser)
            .toList();
    }


    public SimpleResult atualizarUtilizador(Object id, String nome, String email, String password) {
        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("id", id);
        if (nome     != null && !nome.isBlank())     payload.put("name",     nome.trim());
        if (email    != null && !email.isBlank())    payload.put("email",    email.trim());
        if (password != null && !password.isBlank()) payload.put("password", password.trim());

        NativeGrpcClient.GrpcResult resp = grpcClient.invoke("updateUser", payload);
        if (resp.success()) {
            return new SimpleResult(true, false, "Utilizador atualizado.");
        }
        return new SimpleResult(false, true, "Erro gRPC: " + resp.message());
    }


    public SimpleResult removerUtilizador(Object id) {
        NativeGrpcClient.GrpcResult resp = grpcClient.invoke("deleteUser", Map.of("id", id));
        if (resp.success()) {
            return new SimpleResult(true, false, "Utilizador removido.");
        }
        return new SimpleResult(false, true, "Erro gRPC: " + resp.message());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    public Map<String, Object> normalizeUser(Map<String, Object> u) {
        String type  = Objects.toString(u.get("type"), "Cliente");
        String papel = mapTypeToPapel(type);
        boolean ativo = true;
        String disponivel = "-";
        // Drivers are "active" based on the available field.
        if ("COURIER".equals(papel)) {
            Object avail = u.get("available");
            ativo = Boolean.TRUE.equals(avail) || "true".equalsIgnoreCase(Objects.toString(avail, "true"));
            disponivel = ativo ? "Sim" : "Não";
        }
        var m = new java.util.LinkedHashMap<String, Object>();
        m.put("id",       u.getOrDefault("id", "?"));
        m.put("nome",     Objects.toString(u.get("name"),  ""));
        m.put("email",    Objects.toString(u.get("email"), ""));
        m.put("papel",    papel);
        m.put("ativo",    ativo ? "Sim" : "Não");
        m.put("disponivel", disponivel);
        m.put("password", "");   // never returned by backend; kept so existing code doesn't NPE
        return m;
    }

    private String mapTypeToPapel(String type) {
        if (type == null) return "CLIENT";
        String t = type.trim().toLowerCase();
        if (t.contains("entregador") || t.contains("driver"))   return "COURIER";
        if (t.contains("admin") || t.contains("restaurante"))   return "ADMIN";
        return "CLIENT";
    }

    private Long extractLong(Map<?,?> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.longValue();
        if (v != null) { try { return Long.parseLong(v.toString()); } catch (NumberFormatException ignored) {} }
        return null;
    }

    private String msgFrom(Map<?,?> body, String fallback) {
        if (body == null) return fallback;
        Object m = body.get("message");
        return m != null ? m.toString() : fallback;
    }
}
