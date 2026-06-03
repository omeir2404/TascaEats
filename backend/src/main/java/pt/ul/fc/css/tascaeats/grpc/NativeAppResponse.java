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

public final class NativeAppResponse {

    private static final MethodDescriptor.Marshaller<NativeAppResponse> MARSHALLER = new MarshallerImpl();

    private final boolean success;
    private final String message;
    private final String bodyJson;

    private NativeAppResponse(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.bodyJson = builder.bodyJson;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getBodyJson() {
        return bodyJson;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static MethodDescriptor.Marshaller<NativeAppResponse> marshaller() {
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
        private boolean success;
        private String message = "";
        private String bodyJson = "";

        private Builder() {
        }

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message == null ? "" : message;
            return this;
        }

        public Builder setBodyJson(String bodyJson) {
            this.bodyJson = bodyJson == null ? "" : bodyJson;
            return this;
        }

        public NativeAppResponse build() {
            return new NativeAppResponse(this);
        }
    }

    private static final class MarshallerImpl implements MethodDescriptor.Marshaller<NativeAppResponse> {
        @Override
        public InputStream stream(NativeAppResponse value) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (DataOutputStream out = new DataOutputStream(buffer)) {
                    out.writeBoolean(value.getSuccess());
                    writeString(out, value.getMessage());
                    writeString(out, value.getBodyJson());
                }
                return new ByteArrayInputStream(buffer.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException("Nao foi possivel serializar a resposta gRPC.", e);
            }
        }

        @Override
        public NativeAppResponse parse(InputStream stream) {
            try (DataInputStream in = new DataInputStream(stream)) {
                return NativeAppResponse.newBuilder()
                        .setSuccess(in.readBoolean())
                        .setMessage(readString(in))
                        .setBodyJson(readString(in))
                        .build();
            } catch (IOException e) {
                throw new IllegalStateException("Nao foi possivel desserializar a resposta gRPC.", e);
            }
        }
    }
}
