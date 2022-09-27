package EcdarProtoBuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.41.0)",
    comments = "Source: services.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class EcdarBackendGrpc {

  private EcdarBackendGrpc() {}

  public static final String SERVICE_NAME = "EcdarProtoBuf.EcdarBackend";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest,
      com.google.protobuf.Empty> getUpdateComponentsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateComponents",
      requestType = EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest,
      com.google.protobuf.Empty> getUpdateComponentsMethod() {
    io.grpc.MethodDescriptor<EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest, com.google.protobuf.Empty> getUpdateComponentsMethod;
    if ((getUpdateComponentsMethod = EcdarBackendGrpc.getUpdateComponentsMethod) == null) {
      synchronized (EcdarBackendGrpc.class) {
        if ((getUpdateComponentsMethod = EcdarBackendGrpc.getUpdateComponentsMethod) == null) {
          EcdarBackendGrpc.getUpdateComponentsMethod = getUpdateComponentsMethod =
              io.grpc.MethodDescriptor.<EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateComponents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new EcdarBackendMethodDescriptorSupplier("UpdateComponents"))
              .build();
        }
      }
    }
    return getUpdateComponentsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<EcdarProtoBuf.QueryProtos.Query,
      EcdarProtoBuf.QueryProtos.QueryResponse> getSendQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendQuery",
      requestType = EcdarProtoBuf.QueryProtos.Query.class,
      responseType = EcdarProtoBuf.QueryProtos.QueryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<EcdarProtoBuf.QueryProtos.Query,
      EcdarProtoBuf.QueryProtos.QueryResponse> getSendQueryMethod() {
    io.grpc.MethodDescriptor<EcdarProtoBuf.QueryProtos.Query, EcdarProtoBuf.QueryProtos.QueryResponse> getSendQueryMethod;
    if ((getSendQueryMethod = EcdarBackendGrpc.getSendQueryMethod) == null) {
      synchronized (EcdarBackendGrpc.class) {
        if ((getSendQueryMethod = EcdarBackendGrpc.getSendQueryMethod) == null) {
          EcdarBackendGrpc.getSendQueryMethod = getSendQueryMethod =
              io.grpc.MethodDescriptor.<EcdarProtoBuf.QueryProtos.Query, EcdarProtoBuf.QueryProtos.QueryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendQuery"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  EcdarProtoBuf.QueryProtos.Query.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  EcdarProtoBuf.QueryProtos.QueryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EcdarBackendMethodDescriptorSupplier("SendQuery"))
              .build();
        }
      }
    }
    return getSendQueryMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EcdarBackendStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EcdarBackendStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EcdarBackendStub>() {
        @java.lang.Override
        public EcdarBackendStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EcdarBackendStub(channel, callOptions);
        }
      };
    return EcdarBackendStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EcdarBackendBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EcdarBackendBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EcdarBackendBlockingStub>() {
        @java.lang.Override
        public EcdarBackendBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EcdarBackendBlockingStub(channel, callOptions);
        }
      };
    return EcdarBackendBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EcdarBackendFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EcdarBackendFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EcdarBackendFutureStub>() {
        @java.lang.Override
        public EcdarBackendFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EcdarBackendFutureStub(channel, callOptions);
        }
      };
    return EcdarBackendFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class EcdarBackendImplBase implements io.grpc.BindableService {

    /**
     */
    public void updateComponents(EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateComponentsMethod(), responseObserver);
    }

    /**
     */
    public void sendQuery(EcdarProtoBuf.QueryProtos.Query request,
        io.grpc.stub.StreamObserver<EcdarProtoBuf.QueryProtos.QueryResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendQueryMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getUpdateComponentsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_UPDATE_COMPONENTS)))
          .addMethod(
            getSendQueryMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                EcdarProtoBuf.QueryProtos.Query,
                EcdarProtoBuf.QueryProtos.QueryResponse>(
                  this, METHODID_SEND_QUERY)))
          .build();
    }
  }

  /**
   */
  public static final class EcdarBackendStub extends io.grpc.stub.AbstractAsyncStub<EcdarBackendStub> {
    private EcdarBackendStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EcdarBackendStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EcdarBackendStub(channel, callOptions);
    }

    /**
     */
    public void updateComponents(EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateComponentsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sendQuery(EcdarProtoBuf.QueryProtos.Query request,
        io.grpc.stub.StreamObserver<EcdarProtoBuf.QueryProtos.QueryResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendQueryMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class EcdarBackendBlockingStub extends io.grpc.stub.AbstractBlockingStub<EcdarBackendBlockingStub> {
    private EcdarBackendBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EcdarBackendBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EcdarBackendBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.google.protobuf.Empty updateComponents(EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateComponentsMethod(), getCallOptions(), request);
    }

    /**
     */
    public EcdarProtoBuf.QueryProtos.QueryResponse sendQuery(EcdarProtoBuf.QueryProtos.Query request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendQueryMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class EcdarBackendFutureStub extends io.grpc.stub.AbstractFutureStub<EcdarBackendFutureStub> {
    private EcdarBackendFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EcdarBackendFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EcdarBackendFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> updateComponents(
        EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateComponentsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<EcdarProtoBuf.QueryProtos.QueryResponse> sendQuery(
        EcdarProtoBuf.QueryProtos.Query request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendQueryMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_UPDATE_COMPONENTS = 0;
  private static final int METHODID_SEND_QUERY = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EcdarBackendImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(EcdarBackendImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPDATE_COMPONENTS:
          serviceImpl.updateComponents((EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_SEND_QUERY:
          serviceImpl.sendQuery((EcdarProtoBuf.QueryProtos.Query) request,
              (io.grpc.stub.StreamObserver<EcdarProtoBuf.QueryProtos.QueryResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class EcdarBackendBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EcdarBackendBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return EcdarProtoBuf.ServiceProtos.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EcdarBackend");
    }
  }

  private static final class EcdarBackendFileDescriptorSupplier
      extends EcdarBackendBaseDescriptorSupplier {
    EcdarBackendFileDescriptorSupplier() {}
  }

  private static final class EcdarBackendMethodDescriptorSupplier
      extends EcdarBackendBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    EcdarBackendMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (EcdarBackendGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EcdarBackendFileDescriptorSupplier())
              .addMethod(getUpdateComponentsMethod())
              .addMethod(getSendQueryMethod())
              .build();
        }
      }
    }
    return result;
  }
}
