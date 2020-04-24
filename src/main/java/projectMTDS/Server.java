package projectMTDS;

import java.io.File;
import java.util.Arrays;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import akka.actor.ActorSystem;

public class Server {
	public static void main(String[] args) throws InterruptedException {
		if (args.length == 0) {
			startup(25251);
			startup(25252);
			startup(0);
		  } else {
			Arrays.stream(args).map(Integer::parseInt).forEach(Server::startup);
		}
	}

	public static void startup(int port) {
		// Override the configuration of the port
		final Config config = ConfigFactory //
			.parseFile(new File("conf/cluster.conf")) //
			.withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(port));

		// Create an Akka system
		final ActorSystem system = ActorSystem.create("ClusterSystem", config);

		// Create an actor that handles cluster domain events
		system.actorOf(ServerActor.props(), "clusterListener");
	}

}
