package store;

import grpc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class StoreServiceImpl implements StoreService {
    
    @Override
    public CompletionStage<PutReply> put(PutRequest in){
        System.out.println("key " + in.getKey());
        System.out.println("value " + in.getValue());

        PutReply reply = PutReply.newBuilder().setMessage("Received put(key: " + in.getKey() + ", value: " + in.getValue() + ")").build();
        return CompletableFuture.completedFuture(reply);
    }

    @Override
    public CompletionStage<GetReply> get(GetRequest in){
        System.out.println("key " + in.getKey());

        GetReply reply = GetReply.newBuilder().setMessage("Received get(key: " + in.getKey() + ")").build();

        return CompletableFuture.completedFuture(reply);
    }

}