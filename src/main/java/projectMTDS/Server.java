package projectMTDS;

import projectMTDS.messages.*;

import java.io.File;
import java.util.Collections;
import java.util.Scanner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Server {
	public static void main(String[] args) throws InterruptedException {
		final Scanner scanner = new Scanner(System.in);
		ActorRef server = startup(25251);

		while (true) {
            final String line = scanner.nextLine();
			if (line.equalsIgnoreCase("print")) {
				server.tell(new PrintMsg(), ActorRef.noSender());
			} else if (line.equalsIgnoreCase("quit")) {
				break;
			} else {
				System.out.println("Unknown command");
			}
		}
		scanner.close();
	}

	public static ActorRef startup(int port) {
		// Override the configuration of the port
		final Config config = ConfigFactory //
            .parseFile(new File("src/main/java/projectMTDS/conf/cluster.conf")) //
            .withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(port))
            .withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(Collections.singletonList("server")));

		// Create an Akka system
		final ActorSystem system = ActorSystem.create("ClusterSystem", config);

		// Create an actor
		return system.actorOf(ServerActor.props(), "server");
	}

}
