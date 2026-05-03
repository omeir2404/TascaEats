package pt.ul.fc.css.tascaeats.ui;

import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP client for all Utilizador-related backend calls.
 * Every method has a clear fallback so the GUI can handle failures gracefully.
 */
public class UtilizadorApiClient {

    private final RestTemplate restTemplate;
    private final String backendUrl;

    // ── Records ───────────────────────────────────────────────────────────

    public record RegistoResult(Long id, String email, String type, boolean fallbackUsado, String mensagem) {}
    public record LoginResult(boolean sucesso, boolean fallbackUsado, String mensagem) {}
    // ── Constructor ───────────────────────────────────────────────────────

    public UtilizadorApiClient(RestTemplate restTemplate, String backendUrl) {
        this.restTemplate = restTemplate;
        this.backendUrl   = normalizeUrl(backendUrl);
    }

    // ── A: Login ──────────────────────────────────────────────────────────

    /**
     * POST /api/login — mock login (any password accepted if user exists).
     */
    public LoginResult login(String email, String password) {
        try {
            Map<String, Object> payload = Map.of(
                "email",    email    != null ? email.trim()    : "",
                "password", password != null ? password.trim() : ""
            );
            ResponseEntity<Map> resp = restTemplate.postForEntity(backendUrl + "/api/login", payload, Map.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                String msg = msgFrom(resp.getBody(), "Login bem-sucedido");
                return new LoginResult(true, false, msg);
            }
            if (resp.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return new LoginResult(false, false, "Email ou password incorretos.");
            }
            return new LoginResult(false, false, "Erro: " + resp.getStatusCode());
        } catch (HttpClientErrorException.Unauthorized e) {
            return new LoginResult(false, false, "Email ou password incorretos.");
        } catch (RestClientException e) {
            return new LoginResult(false, true, "Backend indisponível — modo demo.");
        }
    }

    /**
     * Registar um utilizador no backend.
     *
     * @param nome Nome completo do utilizador
     * @param email Email (único)
     * @param password Password
     * @param tipo Tipo: "Cliente" ou "Entregador"
     * @return RegistoResult com id do utilizador criado
     */
    public RegistoResult registarUtilizador(String nome, String email, String password, String tipo) {
        try {
            // Montar o payload do registo
            Map<String, Object> payload = Map.of(
                "name", nome != null ? nome.trim() : "",
                "email", email != null ? email.trim() : "",
                "password", password != null ? password.trim() : "",
                "type", tipo != null ? tipo.trim() : "Cliente"
            );

            // Fazer o POST para /api/register
            ResponseEntity<?> response = restTemplate.postForEntity(
                backendUrl + "/api/register",
                payload,
                Map.class
            );

            // Se sucesso (201 Created ou 200 OK)
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                if (body != null) {
                    Long id = extractLongFromMap(body, "id");
                    String respEmail = Objects.toString(body.get("email"), email);
                    String respType = Objects.toString(body.get("type"), tipo);
                    String mensagem = Objects.toString(body.get("message"), "Utilizador registado com sucesso");
                    return new RegistoResult(id, respEmail, respType, false, mensagem);
                }
            }

            // Se não conseguiu processar
            return new RegistoResult(null, email, tipo, false,
                "Resposta inesperada do backend: " + response.getStatusCode());

        } catch (RestClientException ex) {
            // Backend indisponível - usar fallback mock
            Long mockId = System.nanoTime() % 1000000; // ID mock aleatório
            String mensagem = "Backend indisponível — registo em modo demo.";
            return new RegistoResult(mockId, email, tipo, true, mensagem);
        }
    }

    // ── D: Listar, editar e remover utilizadores ──────────────────────────

    /**
     * GET /api/Utilizadors — returns list normalized to GUI keys
     * (id, nome, email, papel, ativo, password="")
     */
    public LoginResult login(String email, String password) {
        try {
            // Montar o payload de login
            Map<String, Object> payload = Map.of(
                "email", email != null ? email.trim() : "",
                "password", password != null ? password.trim() : ""
            );

            // Fazer o POST para /api/login
            ResponseEntity<?> response = restTemplate.postForEntity(
                backendUrl + "/api/login",
                payload,
                Map.class
            );

            // Se sucesso (200 OK)
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                String mensagem = body != null ? Objects.toString(body.get("message"), "Login bem-sucedido") : "Login bem-sucedido";
                return new LoginResult(true, false, mensagem);
            }

            // Se falhou (4xx ou 5xx)
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return new LoginResult(false, false, "Email ou password incorretos.");
            }

    /**
     * PUT /api/Utilizadors/{id}
     */
    public SimpleResult atualizarUtilizador(Object id, String nome, String email, String password) {
        try {
            var payload = new java.util.LinkedHashMap<String, Object>();
            if (nome     != null && !nome.isBlank())     payload.put("name",     nome.trim());
            if (email    != null && !email.isBlank())    payload.put("email",    email.trim());
            if (password != null && !password.isBlank()) payload.put("password", password.trim());

        } catch (RestClientException ex) {
            // Backend indisponível - não usar fallback para login (segurança)
            // Deixar que o fluxo main use os dados mock como últimas verificações
            String mensagem = "Backend indisponível — tentando modo demo.";
            return new LoginResult(false, true, mensagem);
        }
    }

    private Long extractLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        } else if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String normalizeBackendUrl(String rawUrl) {
        String value = rawUrl == null || rawUrl.isBlank() ? "http://localhost:8080" : rawUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String normalizeUrl(String raw) {
        String v = (raw == null || raw.isBlank()) ? "http://localhost:8080" : raw.trim();
        while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
        return v;
    }
}
