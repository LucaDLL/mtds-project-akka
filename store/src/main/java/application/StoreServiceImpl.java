package application;

import grpc.*;
import messages.*;
import static resources.Methods.*;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.pattern.Patterns;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class StoreServiceImpl implements StoreService {
    
    ActorSystem system;
    Cluster cluster;

    StoreServiceImpl(ActorSystem system) {
        this.system = system;
        this.cluster = Cluster.get(system);
    }

    
    @Override
    public CompletionStage<PutReply> put(PutRequest incomingPutRequest){
        
        PutMsg msg = new PutMsg(incomingPutRequest.getKey(), incomingPutRequest.getValue());
        BigInteger id = Sha1(incomingPutRequest.getKey());
        ActorSelection lookupNode = system.actorSelection(ImplNode(cluster).getAddress());
        PutReply clientReply;

		final Future<Object> reply = Patterns.ask(lookupNode, new FindSuccessorMsg(id), 1000000000);
        try {
            FindSuccessorReplyMsg replyMsg = (FindSuccessorReplyMsg) Await.result(reply, Duration.Inf());
            ActorSelection storageNode = system.actorSelection(replyMsg.getNodePointer().getAddress());
            storageNode.tell(msg, ActorRef.noSender());
            clientReply = PutReply.newBuilder().setMessage("Received put(key: " + incomingPutRequest.getKey() + ", value: " + incomingPutRequest.getValue() + ")").build();
        } catch (final Exception e) {
            e.printStackTrace();
            clientReply = PutReply.newBuilder().setMessage("Failed").build();
        }
        return CompletableFuture.completedFuture(clientReply);
    }

    @Override
    public CompletionStage<GetReply> get(GetRequest incomingGetRequest) {
        
        GetMsg msg = new GetMsg(incomingGetRequest.getKey());
        BigInteger id = Sha1(incomingGetRequest.getKey());
        ActorSelection lookupNode = system.actorSelection(ImplNode(cluster).getAddress());
        GetReply clientReply;

        final Future<Object> reply = Patterns.ask(lookupNode, new FindSuccessorMsg(id), 1000);
        try {
            FindSuccessorReplyMsg replyMsg = (FindSuccessorReplyMsg) Await.result(reply, Duration.Inf());
            ActorSelection storageNode = system.actorSelection(replyMsg.getNodePointer().getAddress());
            final Future<Object> nodeReply = Patterns.ask(storageNode, msg, 1000);
            GetReplyMsg getReplyMsg = (GetReplyMsg) Await.result(nodeReply, Duration.Inf());
            clientReply = GetReply.newBuilder().setMessage("Get result: " + getReplyMsg.getContent()).build();
        } catch (Exception e) {
            e.printStackTrace();
            clientReply = GetReply.newBuilder().setMessage("Failed").build();
        }
        
        return CompletableFuture.completedFuture(clientReply);
    }

}