package pt.ul.fc.css.tascaeats.grpc;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class NativeGrpcServer {

    private final NativeGrpcService service;
    private final int port;
    private Server server;

    public NativeGrpcServer(NativeGrpcService service,
                            @Value("${native.grpc.port:9090}") int port) {
        this.service = service;
        this.port = port;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(service)
                .build()
                .start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
