package projectMTDS.store;

import java.io.File;
import java.util.Collections;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Store {
	public static void main(String[] args) throws InterruptedException {
		startup(25251);
		startup(25252);
		startup(0);
	}

	public static ActorRef startup(int port) {
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