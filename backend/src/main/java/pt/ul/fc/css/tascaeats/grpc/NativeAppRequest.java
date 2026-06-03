// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.grpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.grpc.MethodDescriptor;

public final class NativeAppRequest {

    private static final MethodDescriptor.Marshaller<NativeAppRequest> MARSHALLER = new MarshallerImpl();

    private final String operation;
    private final String payloadJson;

    private NativeAppRequest(Builder builder) {
        this.operation = builder.operation;
        this.payloadJson = builder.payloadJson;
    }

    public String getOperation() {
        return operation;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static MethodDescriptor.Marshaller<NativeAppRequest> marshaller() {
        return MARSHALLER;
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0) {
            throw new IOException("Comprimento invalido.");
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static final class Builder {
        private String operation = "";
        private String payloadJson = "";

        private Builder() {
        }

        public Builder setOperation(String operation) {
            this.operation = operation == null ? "" : operation;
            return this;
        }

        public Builder setPayloadJson(String payloadJson) {
            this.payloadJson = payloadJson == null ? "" : payloadJson;
            return this;
        }

        public NativeAppRequest build() {
            return new NativeAppRequest(this);
        }
    }

    private static final class MarshallerImpl implements MethodDescriptor.Marshaller<NativeAppRequest> {
        @Override
        public InputStream stream(NativeAppRequest value) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (DataOutputStream out = new DataOutputStream(buffer)) {
                    writeString(out, value.getOperation());
                    writeString(out, value.getPayloadJson());
                }
                return new ByteArrayInputStream(buffer.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException("Nao foi possivel serializar o pedido gRPC.", e);
            }
        }

        @Override
        public NativeAppRequest parse(InputStream stream) {
            try (DataInputStream in = new DataInputStream(stream)) {
                return NativeAppRequest.newBuilder()
                        .setOperation(readString(in))
                        .setPayloadJson(readString(in))
                        .build();
            } catch (IOException e) {
                throw new IllegalStateException("Nao foi possivel desserializar o pedido gRPC.", e);
            }
        }
    }
}
