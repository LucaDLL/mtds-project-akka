package grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.29.0)",
    comments = "Source: store_proto.proto")
public final class StoreServiceGrpc {

  private StoreServiceGrpc() {}

  public static final String SERVICE_NAME = "store.StoreService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<grpc.PutRequest,
      grpc.PutReply> getPutMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Put",
      requestType = grpc.PutRequest.class,
      responseType = grpc.PutReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.PutRequest,
      grpc.PutReply> getPutMethod() {
    io.grpc.MethodDescriptor<grpc.PutRequest, grpc.PutReply> getPutMethod;
    if ((getPutMethod = StoreServiceGrpc.getPutMethod) == null) {
      synchronized (StoreServiceGrpc.class) {
        if ((getPutMethod = StoreServiceGrpc.getPutMethod) == null) {
          StoreServiceGrpc.getPutMethod = getPutMethod =
              io.grpc.MethodDescriptor.<grpc.PutRequest, grpc.PutReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Put"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.PutRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.PutReply.getDefaultInstance()))
              .setSchemaDescriptor(new StoreServiceMethodDescriptorSupplier("Put"))
              .build();
        }
      }
    }
    return getPutMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.GetRequest,
      grpc.GetReply> getGetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Get",
      requestType = grpc.GetRequest.class,
      responseType = grpc.GetReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.GetRequest,
      grpc.GetReply> getGetMethod() {
    io.grpc.MethodDescriptor<grpc.GetRequest, grpc.GetReply> getGetMethod;
    if ((getGetMethod = StoreServiceGrpc.getGetMethod) == null) {
      synchronized (StoreServiceGrpc.class) {
        if ((getGetMethod = StoreServiceGrpc.getGetMethod) == null) {
          StoreServiceGrpc.getGetMethod = getGetMethod =
              io.grpc.MethodDescriptor.<grpc.GetRequest, grpc.GetReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Get"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.GetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.GetReply.getDefaultInstance()))
              .setSchemaDescriptor(new StoreServiceMethodDescriptorSupplier("Get"))
              .build();
        }
      }
    }
    return getGetMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static StoreServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StoreServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StoreServiceStub>() {
        @java.lang.Override
        public StoreServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StoreServiceStub(channel, callOptions);
        }
      };
    return StoreServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static StoreServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StoreServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StoreServiceBlockingStub>() {
        @java.lang.Override
        public StoreServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StoreServiceBlockingStub(channel, callOptions);
        }
      };
    return StoreServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static StoreServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StoreServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StoreServiceFutureStub>() {
        @java.lang.Override
        public StoreServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StoreServiceFutureStub(channel, callOptions);
        }
      };
    return StoreServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class StoreServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void put(grpc.PutRequest request,
        io.grpc.stub.StreamObserver<grpc.PutReply> responseObserver) {
      asyncUnimplementedUnaryCall(getPutMethod(), responseObserver);
    }

    /**
     */
    public void get(grpc.GetRequest request,
        io.grpc.stub.StreamObserver<grpc.GetReply> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPutMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                grpc.PutRequest,
                grpc.PutReply>(
                  this, METHODID_PUT)))
          .addMethod(
            getGetMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                grpc.GetRequest,
                grpc.GetReply>(
                  this, METHODID_GET)))
          .build();
    }
  }

  /**
   */
  public static final class StoreServiceStub extends io.grpc.stub.AbstractAsyncStub<StoreServiceStub> {
    private StoreServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StoreServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StoreServiceStub(channel, callOptions);
    }

    /**
     */
    public void put(grpc.PutRequest request,
        io.grpc.stub.StreamObserver<grpc.PutReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void get(grpc.GetRequest request,
        io.grpc.stub.StreamObserver<grpc.GetReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class StoreServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<StoreServiceBlockingStub> {
    private StoreServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StoreServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StoreServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public grpc.PutReply put(grpc.PutRequest request) {
      return blockingUnaryCall(
          getChannel(), getPutMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.GetReply get(grpc.GetRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class StoreServiceFutureStub extends io.grpc.stub.AbstractFutureStub<StoreServiceFutureStub> {
    private StoreServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StoreServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StoreServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.PutReply> put(
        grpc.PutRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.GetReply> get(
        grpc.GetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PUT = 0;
  private static final int METHODID_GET = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final StoreServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(StoreServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PUT:
          serviceImpl.put((grpc.PutRequest) request,
              (io.grpc.stub.StreamObserver<grpc.PutReply>) responseObserver);
          break;
        case METHODID_GET:
          serviceImpl.get((grpc.GetRequest) request,
              (io.grpc.stub.StreamObserver<grpc.GetReply>) responseObserver);
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

  private static abstract class StoreServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    StoreServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return grpc.StoreProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("StoreService");
    }
  }

  private static final class StoreServiceFileDescriptorSupplier
      extends StoreServiceBaseDescriptorSupplier {
    StoreServiceFileDescriptorSupplier() {}
  }

  private static final class StoreServiceMethodDescriptorSupplier
      extends StoreServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    StoreServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (StoreServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new StoreServiceFileDescriptorSupplier())
              .addMethod(getPutMethod())
              .addMethod(getGetMethod())
              .build();
        }
      }
    }
    return result;
  }
}
