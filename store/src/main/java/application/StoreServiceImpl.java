package application;

import grpc.*;
import messages.*;
import static resources.Methods.*;

import akka.actor.ActorRef;
import akka.pattern.Patterns;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class StoreServiceImpl implements StoreService {
    /* 
        Receive gRPC requests and convert it to application specific requests.
    */
    ActorRef supervisor;

    public StoreServiceImpl(ActorRef supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public CompletionStage<PutReply> put(PutRequest incomingPutRequest){
        /*
            Create a PutMsg using data from the gRPC request, and tell the supervisor.
        */
        PutMsg putMsg = new PutMsg(hash(incomingPutRequest.getKey()), incomingPutRequest.getValue());
        
        supervisor.tell(putMsg,ActorRef.noSender()); 

        PutReply putReply = PutReply.newBuilder().setMessage(
            "Received put(key: " + incomingPutRequest.getKey() + 
            ", value: " + incomingPutRequest.getValue() + ")"
        ).build();

        return CompletableFuture.completedFuture(putReply);
    }

    @Override
    public CompletionStage<GetReply> get(GetRequest incomingGetRequest) {
        /*
            Create a GetMsg using data from the gRPC request, and ask the supervisor.
            The ask pattern is used: if a reply from supervisor is received within 1 second, use its content to send the get result to the client.
        */
        GetMsg msg = new GetMsg(hash(incomingGetRequest.getKey()));
        GetReply getReply;

        final Future<Object> reply = Patterns.ask(supervisor, msg, 1000);
        try {
            GetReplyMsg getReplyMsg = (GetReplyMsg) Await.result(reply, Duration.Inf());
            getReply = GetReply.newBuilder().setMessage(
                "Get result: " + getReplyMsg.getVal()
            ).build();
        } catch (Exception e) {
            getReply = GetReply.newBuilder().setMessage("Failed").build();
        }
        
        return CompletableFuture.completedFuture(getReply);
    }

    @Override
    public CompletionStage<DebugReply> debug(DebugRequest incomingDebugRequest) {
        /*
            Create a DebugMsg, and tell the supervisor.
            The DebugMsg is used to display the number of keys in the system.
        */
        supervisor.tell(new DebugMsg(),ActorRef.noSender()); 
        DebugReply DebugMsg = DebugReply.newBuilder().setMessage("Debug").build();
        return CompletableFuture.completedFuture(DebugMsg);
    }

}