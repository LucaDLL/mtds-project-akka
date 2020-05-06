package store;

import java.io.File;
import java.util.Collections;

import akka.actor.ActorSystem;
import akka.http.javadsl.*;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import grpc.*;

import java.util.concurrent.CompletionStage;

class Store {

  final static Config config = ConfigFactory.parseFile(new File("conf/application.conf"));

  public static void main(String[] args) throws Exception {

    try {

      if (args[0].equals("init") && args.length == 1) {

        /*
          Start system and seed nodes
        */
        ActorSystem sys = startSeedNodes();

        /*
          Parse replication factor from config
        */
        Integer r = Integer.parseInt(config.getString("replication.factor"));
        
        /*
          To have R replicas, at least R cluster nodes are needed.
          Two seed nodes have been already created, so R-2 more nodes are created.
        */
        for (int i = 0; i < r-3; i++) startNode(0);
        
        /*
          gRPC server binding
        */
        run(sys).thenAccept(binding -> {
          System.out.println("gRPC server bound to: " + binding.localAddress());
        });

      } else if(args[0].equals("add") && args.length == 2) {
  
        try {
          Integer nodesNum = Integer.parseInt(args[1]);
          for (int i = 0; i < nodesNum; i++) startNode(0);
        } catch (NumberFormatException e) {
          throw new Exception();
        }

      } else throw new Exception();

    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  private static CompletionStage<ServerBinding> run(ActorSystem sys) throws Exception {
    Materializer mat = ActorMaterializer.create(sys);

    // Instantiate implementation
    StoreService impl = new StoreServiceImpl(sys);

    return Http.get(sys).bindAndHandleAsync(
      StoreServiceHandlerFactory.create(impl, sys),
      ConnectHttp.toHost("127.0.0.1", 9090),
      mat);
  }

  private static ActorSystem startSeedNodes() {

      startNode(25251);
      return startNode(25252);
  
  }

	private static ActorSystem startNode(int port) {
    
    // Override the configuration of the port
    final Config nodeConfig = config
      .withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(port))
      .withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(Collections.singletonList("storeNode")));

		// Create an Akka system
		final ActorSystem system = ActorSystem.create("StoreSystem", nodeConfig);

		// Create an actor
    system.actorOf(StoreNode.props(), "storeNode");
    
    return system;
  }

}