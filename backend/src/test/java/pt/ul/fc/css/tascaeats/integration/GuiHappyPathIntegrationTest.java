// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end HTTP integration test for the main JavaFX staff happy path.
 *
 * This class intentionally uses TestRestTemplate and dynamic Map payloads to
 * exercise the same backend endpoints consumed by the native GUI, without
 * importing application DTOs or changing production code.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=true"
})
class GuiHappyPathIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@tascaeats.pt";
    private static final String ADMIN_PASSWORD = "password123";
    private static final String CLIENT_EMAIL = "cliente.gui@tascaeats.pt";
    private static final String DRIVER_EMAIL = "driver99@tascaeats.pt";

    private static Long clienteId;
    private static Long restauranteId;
    private static Long produtoId;
    private static Long pedidoId;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeAll
    void seedPrerequisiteUsers() {
        registerUser("Admin Tasca Eats", ADMIN_EMAIL, ADMIN_PASSWORD, "RestauranteAdmin", null);
        registerUser("Cliente GUI", CLIENT_EMAIL, "cliente123", "Cliente", mapOf(
                "rua", "Rua do Cliente GUI",
                "numero", 5,
                "codigoPostal", "1000-200",
                "cidade", "Lisboa",
                "distrito", "Lisboa",
                "pais", "Portugal"));
    }

    @Test
    @Order(1)
    void staffLogin() {
        Map<String, Object> payload = mapOf(
                "email", ADMIN_EMAIL,
                "password", ADMIN_PASSWORD);

        ResponseEntity<Map<String, Object>> response = postMap("/api/login", payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Login successful (mock)");
    }

    @Test
    @Order(2)
    void userManagementCreatesAndListsEntregador() {
        Map<String, Object> payload = mapOf(
                "name", "Novo Entregador",
                "email", DRIVER_EMAIL,
                "password", "123",
                "type", "Entregador");

        ResponseEntity<Map<String, Object>> createResponse = postMap("/api/register", payload);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody())
                .containsEntry("email", DRIVER_EMAIL)
                .containsEntry("type", "Entregador");

        List<Map<String, Object>> utilizadores = getList("/api/Utilizadors");

        assertThat(utilizadores)
                .anySatisfy(user -> {
                    assertThat(user).containsEntry("email", DRIVER_EMAIL);
                    assertThat(String.valueOf(user.get("type"))).isEqualTo("Entregador");
                });

        clienteId = utilizadores.stream()
                .filter(user -> "Cliente".equals(String.valueOf(user.get("type"))))
                .map(user -> toLong(user.get("id")))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new AssertionError("The test setup must provide at least one Cliente."));
    }

    @Test
    @Order(3)
    void restaurantManagementCreatesAndOpensRestaurant() {
        Map<String, Object> morada = mapOf(
                "rua", "Rua A",
                "numero", 1,
                "codigoPostal", "1000-000",
                "cidade", "Lisboa",
                "distrito", "Lisboa",
                "pais", "Portugal");
        Map<String, Object> payload = mapOf(
                "nome", "Tasca Nova",
                "nif", "999888777",
                "morada", morada);

        ResponseEntity<Map<String, Object>> createResponse =
                postMap("/api/restaurantes?adminEmail=" + ADMIN_EMAIL, payload);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        restauranteId = toLong(required(createResponse.getBody(), "id"));
        assertThat(restauranteId).isNotNull();

        ResponseEntity<Map<String, Object>> openResponse = patchMap(
                "/api/restaurantes/" + restauranteId + "/estado",
                mapOf("estado", "ABERTO"));

        assertThat(openResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openResponse.getBody()).containsEntry("estado", "ABERTO");
    }

    @Test
    @Order(4)
    void catalogManagementCreatesAndEnablesProduct() {
        assertThat(restauranteId).as("restauranteId created in order 3").isNotNull();

        Map<String, Object> payload = mapOf(
                "nome", "Bitoque",
                "preco", 10.50,
                "descricao", "Bitoque com ovo");

        ResponseEntity<Map<String, Object>> createResponse =
                postMap("/api/restaurantes/" + restauranteId + "/produtos", payload);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        produtoId = toLong(required(createResponse.getBody(), "id"));
        assertThat(produtoId).isNotNull();

        ResponseEntity<Map<String, Object>> unavailableResponse = patchMap(
                "/api/produtos/" + produtoId + "/disponibilidade",
                mapOf("disponivel", false));

        assertThat(unavailableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(unavailableResponse.getBody()).containsEntry("disponivel", false);

        ResponseEntity<Map<String, Object>> availabilityResponse = patchMap(
                "/api/produtos/" + produtoId + "/disponibilidade",
                mapOf("disponivel", true));

        assertThat(availabilityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availabilityResponse.getBody()).containsEntry("disponivel", true);
    }

    @Test
    @Order(5)
    void orderProcessingLifecyclePaysPreparesDispatchesAndDeliversOrder() {
        assertThat(clienteId).as("clienteId discovered in order 2").isNotNull();
        assertThat(restauranteId).as("restauranteId created in order 3").isNotNull();
        assertThat(produtoId).as("produtoId created in order 4").isNotNull();

        Map<String, Object> moradaNova = mapOf(
                "rua", "Rua do Cliente E2E",
                "numero", 42,
                "codigoPostal", "1000-042",
                "cidade", "Lisboa",
                "distrito", "Lisboa",
                "pais", "Portugal");
        Map<String, Object> item = mapOf(
                "produtoId", produtoId,
                "restauranteId", restauranteId,
                "quantidade", 1);
        Map<String, Object> pedidoPayload = mapOf(
                "clienteId", clienteId,
                "itens", List.of(item),
                "moradaNova", moradaNova);

        ResponseEntity<String> createOrderResponse =
                restTemplate.postForEntity("/pedidos", jsonEntity(pedidoPayload), String.class);

        assertThat(createOrderResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        pedidoId = findCreatedPedidoId();
        assertThat(pedidoId).isNotNull();

        Map<String, Object> pagamentoPayload = mapOf(
                "pedidoId", pedidoId,
                "tipoPagamento", "DINHEIRO",
                // The API factory requires valorRecebido for DINHEIRO.
                "detalhes", mapOf("valorRecebido", 10.50, "valorTroco", 0));
        ResponseEntity<String> pagamentoResponse =
                restTemplate.postForEntity("/pagamentos", jsonEntity(pagamentoPayload), String.class);
        assertThat(pagamentoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertPatchOk("/entregas/pedido/" + pedidoId + "/iniciar-preparacao");
        assertPatchOk("/entregas/pedido/" + pedidoId + "/marcar-pronto");
        assertPatchOk("/entregas/pedido/" + pedidoId + "/recolha");
        assertPatchOk("/entregas/pedido/" + pedidoId + "/entrega-final");
    }

    private ResponseEntity<Map<String, Object>> postMap(String path, Map<String, Object> payload) {
        return restTemplate.exchange(
                path,
                HttpMethod.POST,
                jsonEntity(payload),
                new ParameterizedTypeReference<>() {
                });
    }

    private ResponseEntity<Map<String, Object>> patchMap(String path, Map<String, Object> payload) {
        HttpResponse<String> response = sendPatch(path, payload);
        Map<String, Object> body = parseMapBody(response.body());
        return new ResponseEntity<>(body, HttpStatus.valueOf(response.statusCode()));
    }

    private void assertPatchOk(String path) {
        HttpResponse<String> response = sendPatch(path, null);

        assertThat(HttpStatus.valueOf(response.statusCode()))
                .as("PATCH %s response body: %s", path, response.body())
                .isEqualTo(HttpStatus.OK);
    }

    private HttpResponse<String> sendPatch(String path, Map<String, Object> payload) {
        try {
            String body = payload == null ? "" : objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new AssertionError("PATCH " + path + " failed", e);
        }
    }

    private Map<String, Object> parseMapBody(String body) {
        try {
            if (body == null || body.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(body, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new AssertionError("Could not parse JSON object response: " + body, e);
        }
    }

    private List<Map<String, Object>> getList(String path) {
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private Long findCreatedPedidoId() {
        List<Map<String, Object>> pedidos = getList("/pedidos?clienteId=" + clienteId);

        Optional<Map<String, Object>> pedido = pedidos.stream()
                .filter(item -> containsLong(item.get("produtosIds"), produtoId))
                .filter(item -> containsLong(item.get("restaurantesIds"), restauranteId))
                .max(Comparator.comparing(item -> toLong(item.get("id"))));

        return pedido.map(item -> toLong(item.get("id")))
                .orElseThrow(() -> new AssertionError("Created pedido was not returned by GET /pedidos."));
    }

    private void registerUser(String name, String email, String password, String type, Map<String, Object> address) {
        Map<String, Object> payload = mapOf(
                "name", name,
                "email", email,
                "password", password,
                "type", type);
        if (address != null) {
            payload.put("address", address);
        }

        ResponseEntity<Map<String, Object>> response = postMap("/api/register", payload);
        assertThat(response.getStatusCode())
                .as("seed user %s response body: %s", email, response.getBody())
                .isIn(HttpStatus.CREATED, HttpStatus.CONFLICT);
    }

    private boolean containsLong(Object maybeList, Long expected) {
        if (!(maybeList instanceof List<?> list)) {
            return false;
        }
        return list.stream()
                .map(this::toLong)
                .anyMatch(expected::equals);
    }

    private HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> payload) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    private Map<String, Object> mapOf(Object... keyValues) {
        assertThat(keyValues.length % 2).as("mapOf requires key/value pairs").isZero();
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }

    private Object required(Map<String, Object> body, String key) {
        assertThat(body).as("response body").isNotNull();
        assertThat(body).containsKey(key);
        return body.get(key);
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
