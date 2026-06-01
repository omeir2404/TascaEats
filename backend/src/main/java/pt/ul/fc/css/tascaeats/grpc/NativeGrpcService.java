package pt.ul.fc.css.tascaeats.grpc;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.grpc.stub.StreamObserver;
import pt.ul.fc.css.tascaeats.controllers.PagamentoController;
import pt.ul.fc.css.tascaeats.controllers.PedidoController;
import pt.ul.fc.css.tascaeats.controllers.ProdutoController;
import pt.ul.fc.css.tascaeats.controllers.RestauranteController;
import pt.ul.fc.css.tascaeats.controllers.UtilizadorController;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.dto.pagamentoDTO.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.AtualizarEstadoPedidoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarDisponibilidadeProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.CriarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarEstadoRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.CriarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorLoginDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorRegistoDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorUpdateDTO;
import pt.ul.fc.css.tascaeats.publishers.DeliveryCommandPublisher;

@Service
public class NativeGrpcService extends NativeAppServiceGrpc.NativeAppServiceImplBase {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper mapper;
    private final UtilizadorController utilizadorController;
    private final RestauranteController restauranteController;
    private final ProdutoController produtoController;
    private final PedidoController pedidoController;
    private final PagamentoController pagamentoController;
    private final DeliveryCommandPublisher deliveryCommandPublisher;

    public NativeGrpcService(ObjectMapper mapper,
                             UtilizadorController utilizadorController,
                             RestauranteController restauranteController,
                             ProdutoController produtoController,
                             PedidoController pedidoController,
                             PagamentoController pagamentoController,
                             DeliveryCommandPublisher deliveryCommandPublisher) {
        this.mapper = mapper;
        this.utilizadorController = utilizadorController;
        this.restauranteController = restauranteController;
        this.produtoController = produtoController;
        this.pedidoController = pedidoController;
        this.pagamentoController = pagamentoController;
        this.deliveryCommandPublisher = deliveryCommandPublisher;
    }

    @Override
    public void invoke(NativeAppRequest request, StreamObserver<NativeAppResponse> responseObserver) {
        NativeAppResponse response;
        try {
            response = dispatch(request.getOperation(), payload(request.getPayloadJson()));
        } catch (Exception e) {
            response = NativeAppResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage())
                    .setBodyJson("{}")
                    .build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private NativeAppResponse dispatch(String operation, Map<String, Object> payload) throws Exception {
        return switch (operation) {
            case "login" -> respond(utilizadorController.login(map(payload, UtilizadorLoginDTO.class)));
            case "registerUser" -> respond(utilizadorController.register(map(payload, UtilizadorRegistoDTO.class)));
            case "listUsers" -> respond(utilizadorController.getUtilizadors());
            case "updateUser" -> respond(utilizadorController.updateUtilizador(longValue(payload.get("id")),
                    map(payload, UtilizadorUpdateDTO.class)));
            case "deleteUser" -> respond(utilizadorController.deleteUtilizador(longValue(payload.get("id"))));
            case "listRestaurants" -> respond(restauranteController.listarRestaurantes(
                    stringValue(payload.get("nome")),
                    stringValue(payload.get("cidade")),
                    enumValue(payload.get("estado"), EstadoRestaurante.class)));
            case "createRestaurant" -> respond(restauranteController.criarRestaurante(
                    map(requiredMap(payload.get("restaurante")), CriarRestauranteDTO.class),
                    stringValue(payload.get("adminEmail"))));
            case "updateRestaurant" -> respond(restauranteController.atualizarRestaurante(
                    longValue(payload.get("id")),
                    map(requiredMap(payload.get("restaurante")), AtualizarRestauranteDTO.class)));
            case "updateRestaurantStatus" -> respond(restauranteController.atualizarEstado(
                    longValue(payload.get("id")),
                    map(Map.of("estado", stringValue(payload.get("estado"))), AtualizarEstadoRestauranteDTO.class)));
            case "deleteRestaurant" -> respond(restauranteController.removerRestaurante(longValue(payload.get("id"))));
            case "listProducts" -> respond(produtoController.listarProdutos(
                    stringValue(payload.get("nome")),
                    optionalLong(payload.get("restauranteId")),
                    boolValue(payload.get("ativos")),
                    boolValue(payload.get("disponiveis"))));
            case "createProduct" -> respond(produtoController.criarProduto(
                    longValue(payload.get("restauranteId")),
                    map(requiredMap(payload.get("produto")), CriarProdutoDTO.class)));
            case "updateProduct" -> respond(produtoController.atualizarProduto(
                    longValue(payload.get("id")),
                    map(requiredMap(payload.get("produto")), AtualizarProdutoDTO.class)));
            case "updateProductAvailability" -> respond(produtoController.atualizarDisponibilidade(
                    longValue(payload.get("id")),
                    map(Map.of("disponivel", boolValue(payload.get("disponivel"))), AtualizarDisponibilidadeProdutoDTO.class)));
            case "deleteProduct" -> respond(produtoController.apagarProduto(longValue(payload.get("id"))));
            case "listOrders" -> respond(pedidoController.listarPedidos(
                    optionalLong(payload.get("clienteId")),
                    null));
            case "updateOrderStatus" -> respond(pedidoController.atualizarEstadoPedido(
                    longValue(payload.get("pedidoId")),
                    map(Map.of("novoEstado", stringValue(payload.get("novoEstado"))), AtualizarEstadoPedidoDTO.class)));
            case "cancelOrder" -> respond(pedidoController.cancelarPedidoPatch(longValue(payload.get("pedidoId"))));
            case "processPayment" -> respond(pagamentoController.pagarPedido(map(payload, ProcessarPagamentoDTO.class)));
            case "deliveryPatch" -> deliveryPatch(stringValue(payload.get("path")));
            default -> NativeAppResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Operacao gRPC desconhecida: " + operation)
                    .setBodyJson("{}")
                    .build();
        };
    }

    private NativeAppResponse deliveryPatch(String path) {
        try {
            DeliveryCommand command = parseDeliveryCommand(path);
            deliveryCommandPublisher.publicar(command.pedidoId(), command.action());
            return NativeAppResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("OK")
                    .setBodyJson(mapper.writeValueAsString(Map.of("message", "Comando de entrega publicado.")))
                    .build();
        } catch (Exception e) {
            return NativeAppResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage())
                    .setBodyJson("{}")
                    .build();
        }
    }

    private DeliveryCommand parseDeliveryCommand(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path de entrega invalido.");
        }
        String[] parts = path.split("/");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Path de entrega invalido: " + path);
        }
        Long pedidoId = Long.parseLong(parts[3]);
        String action = switch (parts[4]) {
            case "iniciar-preparacao" -> "INICIAR_PREPARACAO";
            case "marcar-pronto" -> "MARCAR_PRONTO";
            case "recolha" -> "REGISTAR_RECOLHA";
            case "entrega-final" -> "ENTREGA_FINAL";
            default -> throw new IllegalArgumentException("Comando de entrega invalido: " + parts[4]);
        };
        return new DeliveryCommand(pedidoId, action);
    }

    private record DeliveryCommand(Long pedidoId, String action) {}

    private NativeAppResponse respond(ResponseEntity<?> entity) throws Exception {
        HttpStatusCode status = entity.getStatusCode();
        Object body = entity.getBody();
        return NativeAppResponse.newBuilder()
                .setSuccess(status.is2xxSuccessful())
                .setMessage(status.toString())
                .setBodyJson(body == null ? "{}" : mapper.writeValueAsString(body))
                .build();
    }

    private NativeAppResponse ok(Object body) {
        try {
            return NativeAppResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("OK")
                    .setBodyJson(mapper.writeValueAsString(body))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Object> payload(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        return mapper.readValue(json, MAP_TYPE);
    }

    private <T> T map(Map<String, Object> payload, Class<T> type) {
        return mapper.convertValue(payload, type);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> requiredMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Payload invalido.");
    }

    private Long optionalLong(Object value) {
        return value == null || stringValue(value).isBlank() ? null : longValue(value);
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private boolean boolValue(Object value) {
        return value instanceof Boolean bool ? bool : Boolean.parseBoolean(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private <E extends Enum<E>> E enumValue(Object value, Class<E> enumType) {
        String text = stringValue(value);
        return text.isBlank() || "Todos".equalsIgnoreCase(text) ? null : Enum.valueOf(enumType, text);
    }

}
