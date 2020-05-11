package store;

import grpc.*;
import messages.*;
import resources.Consts;
import resources.NodePointer;
import static resources.Methods.*;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.Member;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.commons.codec.digest.DigestUtils;

public class StoreServiceImpl implements StoreService {
    
    ActorSystem system;
    Cluster cluster;

    StoreServiceImpl(ActorSystem system) {
        this.system = system;
        this.cluster = Cluster.get(system);
    }

    @Override
    public CompletionStage<PutReply> put(PutRequest in){
        
        Member member = (cluster.state().getMembers()).iterator().next();
        String lookupAddress = GetMemberAddress(member);
        LookupMsg lookupMsg = new LookupMsg(Sha1(in.getKey()));
        
        //TODO THIS IS THE ADDRESS TO HASH
        //String string = member.uniqueAddress().toString();

        //TODO: ask instead of tell
        //system.actorSelection(lookupAddress).tell(lookupMsg, ActorRef.noSender());

        //TODO: putMsg
        //PutMsg msg = new PutMsg(in.getKey(), in.getValue());
        //system.actorSelection(address).tell(msg, ActorRef.noSender());
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