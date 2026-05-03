package pt.ul.fc.css.tascaeats.ui;

import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * Cliente HTTP para registar utilizadores no backend REST com fallback para modo mock.
 */
public class UtilizadorApiClient {

    private final String baseUrl = System.getenv().getOrDefault("BACKEND_API_URL","http://localhost:8080");

    /**
     * Resultado de registo de utilizador, com indicação se foi necessário usar fallback local.
     */
    public record RegistoResult(Long id, String email, String type, boolean fallbackUsado, String mensagem) {
    }

    /**
     * Resultado de login, com indicação se foi necessário usar fallback local.
     */
    public record LoginResult(boolean sucesso, boolean fallbackUsado, String mensagem) {
    }

    private final RestTemplate restTemplate;
    private final String backendUrl;

    public UtilizadorApiClient(RestTemplate restTemplate, String backendUrl) {
        this.restTemplate = restTemplate;
        this.backendUrl = normalizeBackendUrl(backendUrl);
    }

    public String getBackendUrl() {
        return backendUrl;
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

    /**
     * Fazer login de um utilizador.
     *
     * @param email Email do utilizador
     * @param password Password do utilizador
     * @return LoginResult indicando sucesso ou falha
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

            return new LoginResult(false, false, "Erro ao fazer login: " + response.getStatusCode());

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

    public List<Map<String, Object>> listarUtilizadores() {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    baseUrl + "/api/Utilizadors", List.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (List<Map<String, Object>>) response.getBody();
            }
        } catch (Exception e) {
            // backend unavailable, fallback to mock
        }
        return List.of();
    }

}





