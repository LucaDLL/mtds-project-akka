package store;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.Member;

import messages.*;
import grpc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
        String address = member.address().toString() + "/user/storeNode";
        PutMsg msg = new PutMsg(in.getKey(), in.getValue());

        system.actorSelection(address).tell(msg, ActorRef.noSender());
        
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