package store;

import java.io.File;
import java.util.Collections;

import akka.actor.ActorRef;
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
  public static void main(String[] args) throws Exception {

    try {
      if(args[0].equals("init") && args.length == 1) {
        //Enable HTTP/2 in ActorSystem's config
        Config conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
        .withFallback(ConfigFactory.defaultApplication());
  
        // Akka ActorSystem Boot
        ActorSystem sys = ActorSystem.create("StoreSystem", conf);
  
        //gRPC server binding
        run(sys).thenAccept(binding -> {
        System.out.println("gRPC server bound to: " + binding.localAddress());
        });
  
        startNode(25251);
        startNode(25252);
      }
      else if(args[0].equals("add") && args.length == 2) {
  
        try {
          Integer nodesNum = Integer.parseInt(args[1]);
          for (int i = 0; i < nodesNum; i++) startNode(0);
        } catch (NumberFormatException e) {
          throw new Exception();
        }
      }
      else throw new Exception();
    } catch (Exception e) {
        System.out.println("Bad input");
    }
  }

  public static CompletionStage<ServerBinding> run(ActorSystem sys) throws Exception {
    Materializer mat = ActorMaterializer.create(sys);

    // Instantiate implementation
    StoreService impl = new StoreServiceImpl();

    return Http.get(sys).bindAndHandleAsync(
      StoreServiceHandlerFactory.create(impl, sys),
      ConnectHttp.toHost("127.0.0.1", 9090),
      mat);
  }

	public static ActorRef startNode(int port) {
		// Override the configuration of the port
		final Config config = ConfigFactory //
			.parseFile(new File("conf/application.conf")) //
			.withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(port))
			.withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(Collections.singletonList("storeNode")));

		// Create an Akka system
		final ActorSystem system = ActorSystem.create("StoreSystem", config);

		// Create an actor
		return system.actorOf(StoreNode.props(), "storeNode");
	}

}