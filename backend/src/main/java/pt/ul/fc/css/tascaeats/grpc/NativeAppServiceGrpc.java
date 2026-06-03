// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.grpc;

import io.grpc.BindableService;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

public final class NativeAppServiceGrpc {

    private NativeAppServiceGrpc() {
    }

    public static final String SERVICE_NAME = "pt.ul.fc.css.tascaeats.grpc.NativeAppService";
    private static final int METHODID_INVOKE = 0;
    private static volatile MethodDescriptor<NativeAppRequest, NativeAppResponse> invokeMethod;
    private static volatile ServiceDescriptor serviceDescriptor;

    public static MethodDescriptor<NativeAppRequest, NativeAppResponse> getInvokeMethod() {
        MethodDescriptor<NativeAppRequest, NativeAppResponse> result = invokeMethod;
        if (result == null) {
            synchronized (NativeAppServiceGrpc.class) {
                result = invokeMethod;
                if (result == null) {
                    invokeMethod = result = MethodDescriptor.<NativeAppRequest, NativeAppResponse>newBuilder()
                            .setType(MethodDescriptor.MethodType.UNARY)
                            .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, "Invoke"))
                            .setSampledToLocalTracing(true)
                            .setRequestMarshaller(NativeAppRequest.marshaller())
                            .setResponseMarshaller(NativeAppResponse.marshaller())
                            .build();
                }
            }
        }
        return result;
    }

    public static ServiceDescriptor getServiceDescriptor() {
        ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (NativeAppServiceGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .addMethod(getInvokeMethod())
                            .build();
                }
            }
        }
        return result;
    }

    public static NativeAppServiceBlockingStub newBlockingStub(Channel channel) {
        return new NativeAppServiceBlockingStub(channel, CallOptions.DEFAULT);
    }

    public abstract static class NativeAppServiceImplBase implements BindableService {

        public void invoke(NativeAppRequest request, StreamObserver<NativeAppResponse> responseObserver) {
            ServerCalls.asyncUnimplementedUnaryCall(getInvokeMethod(), responseObserver);
        }

        @Override
        public final ServerServiceDefinition bindService() {
            return ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(getInvokeMethod(), ServerCalls.asyncUnaryCall(new MethodHandlers<>(this, METHODID_INVOKE)))
                    .build();
        }
    }

    public static final class NativeAppServiceBlockingStub extends AbstractBlockingStub<NativeAppServiceBlockingStub> {

        private NativeAppServiceBlockingStub(Channel channel, CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected NativeAppServiceBlockingStub build(Channel channel, CallOptions callOptions) {
            return new NativeAppServiceBlockingStub(channel, callOptions);
        }

        public NativeAppResponse invoke(NativeAppRequest request) {
            return ClientCalls.blockingUnaryCall(getChannel(), getInvokeMethod(), getCallOptions(), request);
        }
    }

    private static final class MethodHandlers<Req, Resp> implements ServerCalls.UnaryMethod<Req, Resp> {

        private final NativeAppServiceImplBase serviceImpl;
        private final int methodId;

        private MethodHandlers(NativeAppServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request, StreamObserver<Resp> responseObserver) {
            if (methodId == METHODID_INVOKE) {
                serviceImpl.invoke((NativeAppRequest) request, (StreamObserver<NativeAppResponse>) responseObserver);
                return;
            }
            throw new AssertionError();
        }
    }
}
