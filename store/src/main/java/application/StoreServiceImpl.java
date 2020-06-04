package application;

import grpc.*;
import messages.*;

import akka.actor.ActorRef;
import akka.pattern.Patterns;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class StoreServiceImpl implements StoreService {
    
    ActorRef supervisor;

    StoreServiceImpl(ActorRef supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public CompletionStage<PutReply> put(PutRequest incomingPutRequest){
        
        PutMsg putMsg = new PutMsg(incomingPutRequest.getKey().hashCode(), incomingPutRequest.getValue());
        
        supervisor.tell(putMsg,ActorRef.noSender()); 

        PutReply putReply = PutReply.newBuilder().setMessage(
            "Received put(key: " + incomingPutRequest.getKey() + 
            ", value: " + incomingPutRequest.getValue() + ")"
        ).build();

        return CompletableFuture.completedFuture(putReply);
    }

    @Override
    public CompletionStage<GetReply> get(GetRequest incomingGetRequest) {
        
        GetMsg msg = new GetMsg(incomingGetRequest.getKey().hashCode());
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

}