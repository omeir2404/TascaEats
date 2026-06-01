package pt.ul.fc.css.tascaeats.ui;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ul.fc.css.tascaeats.grpc.NativeAppRequest;
import pt.ul.fc.css.tascaeats.grpc.NativeAppResponse;
import pt.ul.fc.css.tascaeats.grpc.NativeAppServiceGrpc;

public class NativeGrpcClient implements AutoCloseable {

    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper mapper = new ObjectMapper();
    private final ManagedChannel channel;
    private final NativeAppServiceGrpc.NativeAppServiceBlockingStub stub;

    public NativeGrpcClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = NativeAppServiceGrpc.newBlockingStub(channel);
    }

    public GrpcResult invoke(String operation, Map<String, Object> payload) {
        try {
            NativeAppRequest request = NativeAppRequest.newBuilder()
                    .setOperation(operation)
                    .setPayloadJson(mapper.writeValueAsString(payload == null ? Map.of() : payload))
                    .build();
            NativeAppResponse response = stub.invoke(request);
            return new GrpcResult(response.getSuccess(), response.getMessage(), response.getBodyJson());
        } catch (Exception e) {
            return new GrpcResult(false, e.getMessage(), "{}");
        }
    }

    public List<Map<String, Object>> invokeList(String operation, Map<String, Object> payload) {
        GrpcResult result = invoke(operation, payload);
        if (!result.success()) {
            return List.of();
        }
        return readList(result.bodyJson());
    }

    public Map<String, Object> invokeMap(String operation, Map<String, Object> payload) {
        GrpcResult result = invoke(operation, payload);
        if (!result.success()) {
            return Map.of("error", result.message());
        }
        return readMap(result.bodyJson());
    }

    public List<Map<String, Object>> readList(String bodyJson) {
        try {
            return mapper.readValue(bodyJson, LIST_TYPE);
        } catch (Exception e) {
            return List.of();
        }
    }

    public Map<String, Object> readMap(String bodyJson) {
        try {
            return mapper.readValue(bodyJson, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    @Override
    public void close() {
        channel.shutdown();
        try {
            channel.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public record GrpcResult(boolean success, String message, String bodyJson) {}
}
