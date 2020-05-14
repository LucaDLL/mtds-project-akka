package store;

import grpc.*;
import messages.*;
import resources.Consts;
import resources.NodePointer;
import static resources.Methods.*;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.event.LookupClassification;
import akka.pattern.Patterns;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import org.apache.commons.codec.digest.DigestUtils;

public class StoreServiceImpl implements StoreService {
    
    ActorSystem system;
    Cluster cluster;

    StoreServiceImpl(ActorSystem system) {
        this.system = system;
        this.cluster = Cluster.get(system);
    }

    private ActorSelection lookup(String key) throws Exception {

        Member member = (cluster.state().getMembers()).iterator().next();
        ActorSelection node = system.actorSelection(GetMemberAddress(member));
        LookupMsg lookupMsg = new LookupMsg(Sha1(key));
        Boolean continueLookup = true;

        while (continueLookup){
            final Future<Object> reply = Patterns.ask(node, lookupMsg, 1000);
            LookupReplyMsg replyMsg = (LookupReplyMsg) Await.result(reply, Duration.Inf());
            node = system.actorSelection(replyMsg.getAddress());
            continueLookup = replyMsg.getContinueLookup();
        }

        return node;
    }

    @Override
    public CompletionStage<PutReply> put(PutRequest incomingPutRequest){
        
        ActorSelection node;
        PutReply clientReply;

        try {
            node = lookup(incomingPutRequest.getKey());
            PutMsg msg = new PutMsg(incomingPutRequest.getKey(), incomingPutRequest.getValue());
            node.tell(msg, ActorRef.noSender());
            clientReply = PutReply.newBuilder().setMessage("Received put(key: " + incomingPutRequest.getKey() + ", value: " + incomingPutRequest.getValue() + ")").build();
        } catch (final Exception e) {
            e.printStackTrace();
            clientReply = PutReply.newBuilder().setMessage("Failed").build();
        }
        return CompletableFuture.completedFuture(clientReply);
    }

    @Override
    public CompletionStage<GetReply> get(GetRequest incomingGetRequest){

        ActorSelection node;
        GetReply clientReply;

        try {
            node = lookup(incomingGetRequest.getKey());
            GetMsg msg = new GetMsg(incomingGetRequest.getKey());
            final Future<Object> nodeReply = Patterns.ask(node, msg, 1000);
            GetReplyMsg getReplyMsg = (GetReplyMsg) Await.result(nodeReply, Duration.Inf());
            clientReply = GetReply.newBuilder().setMessage("Get result: " + getReplyMsg.getContent()).build();
        } catch (Exception e) {
            e.printStackTrace();
            clientReply = GetReply.newBuilder().setMessage("Failed").build();
        }
        
        return CompletableFuture.completedFuture(clientReply);
    }

}