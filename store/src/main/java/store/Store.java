package store;

import java.io.File;
import java.util.Collections;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.Member;
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
  final static int replicationFactor = Integer.parseInt(config.getString("replication.factor"));

  public static void main(String[] args) throws Exception {

    try {

      if (args[0].equals("init") && args.length == 1) {

        /*
          Start system and seed node
        */
        ActorSystem sys = startNode(25251);
        
        /*
          gRPC server binding
        */
        run(sys).thenAccept(binding -> {
          System.out.println("gRPC server bound to: " + binding.localAddress() + "\n");
        });

      } else if (args[0].equals("add") && args.length == 1) {
      
        startNode(0);
      
      } else if (args[0].equals("add") && args.length == 2) {
  
        try {
          Integer port = Integer.parseInt(args[1]);
          startNode(port);
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