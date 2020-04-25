package projectMTDS;

import java.io.File;
import java.util.Collections;
import java.util.Scanner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import projectMTDS.messages.*;

public class Client {
	public static void main(String[] args) throws InterruptedException {
		final Scanner scanner = new Scanner(System.in);
		
		ActorRef client = startup(0);

		while (true) {
            final String line = scanner.nextLine();
            final String[] words = line.split(" ");
			if (words[0].equalsIgnoreCase("put")) {
				final String key = words[1];
				final String val = words[2];
				final PutMsg msg = new PutMsg(key, val);
				client.tell(msg, ActorRef.noSender());
			} else if (words[0].equalsIgnoreCase("get")) {
				final String key = words[1];
				final GetMsg msg = new GetMsg(key);
				client.tell(msg, ActorRef.noSender());
			} else if (words[0].equalsIgnoreCase("quit")) {
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
			.withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(Collections.singletonList("client")));

		// Create an Akka system
		final ActorSystem system = ActorSystem.create("ClusterSystem", config);

		// Create an actor that handles cluster domain events
		return system.actorOf(ClientActor.props(), "client");
	}
}