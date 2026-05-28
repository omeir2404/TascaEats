// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################


package pt.ul.fc.css.tascaeats.ui;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UtilizadorApiClient {

    private final RestTemplate restTemplate;
    private final String backendUrl;

    // ── Records ───────────────────────────────────────────────────────────

    public record RegistoResult(Long id, String email, String type, boolean fallbackUsado, String mensagem) {}
    public record LoginResult(boolean sucesso, boolean fallbackUsado, String mensagem) {}
    public record SimpleResult(boolean sucesso, boolean fallbackUsado, String mensagem) {}

    // ── Constructor ───────────────────────────────────────────────────────

    public UtilizadorApiClient(RestTemplate restTemplate, String backendUrl) {
        this.restTemplate = restTemplate;
        this.backendUrl   = normalizeUrl(backendUrl);
    }

    // ── A: Login ──────────────────────────────────────────────────────────

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

    // ── C: Registar utilizador ────────────────────────────────────────────

    public RegistoResult registarUtilizador(String nome, String email, String password, String tipo) {
        try {
            Map<String, Object> payload = Map.of(
                "name",     nome     != null ? nome.trim()     : "",
                "email",    email    != null ? email.trim()    : "",
                "password", password != null ? password.trim() : "",
                "type",     tipo     != null ? tipo.trim()     : "client"
            );
            ResponseEntity<Map> resp = restTemplate.postForEntity(backendUrl + "/api/register", payload, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<?,?> body = resp.getBody();
                Long id        = extractLong(body, "id");
                String respEmail = Objects.toString(body.get("email"), email);
                String respType  = Objects.toString(body.get("type"),  tipo);
                String msg       = msgFrom(body, "Utilizador registado com sucesso");
                return new RegistoResult(id, respEmail, respType, false, msg);
            }
            return new RegistoResult(null, email, tipo, false, "Resposta inesperada: " + resp.getStatusCode());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            return new RegistoResult(null, email, tipo, false, "Erro do Servidor: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            return new RegistoResult((long)(System.nanoTime() % 1_000_000), email, tipo, true,
                "Backend indisponível — registo em modo demo.");
        }
    }

    // ── D: Listar, editar e remover utilizadores ──────────────────────────


    public List<Map<String, Object>> listarUtilizadores() {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                backendUrl + "/api/Utilizadors",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody().stream()
                    .map(this::normalizeUser)
                    .toList();
            }
        } catch (RestClientException ignored) {}
        return List.of();
    }


    public SimpleResult atualizarUtilizador(Object id, String nome, String email, String password) {
        try {
            var payload = new java.util.LinkedHashMap<String, Object>();
            if (nome     != null && !nome.isBlank())     payload.put("name",     nome.trim());
            if (email    != null && !email.isBlank())    payload.put("email",    email.trim());
            if (password != null && !password.isBlank()) payload.put("password", password.trim());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload);
            ResponseEntity<Map> resp = restTemplate.exchange(
                backendUrl + "/api/Utilizadors/" + id, HttpMethod.PUT, entity, Map.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return new SimpleResult(true, false, "Utilizador atualizado.");
            }
            return new SimpleResult(false, false, "Erro: " + resp.getStatusCode());
        } catch (HttpClientErrorException e) {
            return new SimpleResult(false, false, "Erro " + e.getStatusCode());
        } catch (RestClientException e) {
            return new SimpleResult(false, true, "Backend indisponível.");
        }
    }


    public SimpleResult removerUtilizador(Object id) {
        try {
            restTemplate.delete(backendUrl + "/api/Utilizadors/" + id);
            return new SimpleResult(true, false, "Utilizador removido.");
        } catch (HttpClientErrorException.NotFound e) {
            return new SimpleResult(false, false, "Utilizador não encontrado.");
        } catch (RestClientException e) {
            return new SimpleResult(false, true, "Backend indisponível.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    public Map<String, Object> normalizeUser(Map<String, Object> u) {
        String type  = Objects.toString(u.get("type"), "Cliente");
        boolean ativo = true;
        // Drivers are "active" based on the available field
        if (type.equalsIgnoreCase("Entregador") || type.equalsIgnoreCase("driver")) {
            Object avail = u.get("available");
            ativo = Boolean.TRUE.equals(avail) || "true".equalsIgnoreCase(Objects.toString(avail, "true"));
        }
        var m = new java.util.LinkedHashMap<String, Object>();
        m.put("id",       u.getOrDefault("id", "?"));
        m.put("nome",     Objects.toString(u.get("name"),  ""));
        m.put("email",    Objects.toString(u.get("email"), ""));
        m.put("papel",    mapTypeToPapel(type));
        m.put("ativo",    ativo ? "Sim" : "Não");
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

    private String normalizeUrl(String raw) {
        String v = (raw == null || raw.isBlank()) ? "http://localhost:80" : raw.trim();
        while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
        return v;
    }
}
