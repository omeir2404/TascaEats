package pt.ul.fc.css.tascaeats.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Cliente HTTP simples para consumir a API REST do backend.
 */
public class RestauranteApiClient {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_TYPE = new ParameterizedTypeReference<>() {
    };

    private final RestTemplate restTemplate;
    private final String backendUrl;

    public RestauranteApiClient(RestTemplate restTemplate, String backendUrl) {
        this.restTemplate = restTemplate;
        this.backendUrl = normalizeBackendUrl(backendUrl);
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public List<RestauranteResumo> listarRestaurantes(String nome, String estado) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(backendUrl).path("/api/restaurantes");

        if (nome != null && !nome.isBlank()) {
            builder.queryParam("nome", nome.trim());
        } else if (estado != null && !estado.isBlank()) {
            builder.queryParam("estado", estado.trim());
        }

        List<Map<String, Object>> resposta = restTemplate
                .exchange(builder.toUriString(), HttpMethod.GET, null, LIST_TYPE)
                .getBody();

        if (resposta == null || resposta.isEmpty()) {
            return List.of();
        }

        List<RestauranteResumo> restaurantes = new ArrayList<>(resposta.size());
        for (Map<String, Object> item : resposta) {
            restaurantes.add(toResumo(item));
        }
        return restaurantes;
    }

    private RestauranteResumo toResumo(Map<String, Object> item) {
        Long id = null;
        Object rawId = item.get("id");
        if (rawId instanceof Number number) {
            id = number.longValue();
        } else if (rawId != null) {
            try {
                id = Long.parseLong(rawId.toString());
            } catch (NumberFormatException ignored) {
                id = null;
            }
        }

        String nome = Objects.toString(item.get("nome"), "");
        String estado = Objects.toString(item.get("estado"), "");
        return new RestauranteResumo(id, nome, estado);
    }

    private String normalizeBackendUrl(String rawUrl) {
        String value = rawUrl == null || rawUrl.isBlank() ? "http://localhost:8080" : rawUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}

