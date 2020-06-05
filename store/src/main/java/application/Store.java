package application;

import actors.*;
import grpc.*;
import resources.Consts;
import static resources.Methods.*;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.*;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;


import java.io.File;
import java.util.Collections;
import java.util.concurrent.CompletionStage;

import java.net.InetAddress;
import java.net.UnknownHostException;

class Store {
	final static Config config = ConfigFactory.parseFile(new File("conf/application.conf"));
	final static int replicationFactor = Integer.parseInt(config.getString("replication.factor"));
	final static String rpcAddress = config.getString("rpc.address");
	final static int rpcPort = Integer.parseInt(config.getString("rpc.port"));
	final static int seedPort = Integer.parseInt(config.getString("seed.port"));

  	public static void main(String[] args) throws Exception {
    	try {
			if (args[0].equals("init") && args.length == 1) { 
				/*
					Start system and supervisor
				*/
				ActorSystem sys = startSystem();
				ActorRef supervisor = sys.actorOf(SupervisorActor.props(), Consts.SUPERVISOR_ACTOR_NAME);
				/*
					gRPC server binding
				*/
        		run(sys, supervisor, rpcAddress, rpcPort).thenAccept(binding -> {});
      		} else if (args[0].equals("add") && args.length == 1) {
				/*
					Add one node, random port
				*/
				startNode(0);

      		} else if (args[0].equals("add") && args.length == 2) {
				/*
					Add some nodes, random ports
				*/
				try {
					Integer size = Integer.parseInt(args[1]);
					for(int i = 0; i < size; i++) startNode(0);
				} catch (NumberFormatException e) {
					throw new Exception();
				}
			} else
				/*
					Invalid input
				*/
				throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ActorSystem startSystem() throws UnknownHostException {
		/*
			Override the configuration of the port
		*/
		final Config supervisorConfig = config
			.withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(getLocalHostLANAddress().getHostAddress()))
			.withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(seedPort))
			.withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(Collections.singletonList(Consts.SUPERVISOR_ACTOR_NAME)));
		/*
			Create an Akka system
		*/
		return ActorSystem.create(Consts.SYSTEM_NAME, supervisorConfig);
	}


	private static CompletionStage<ServerBinding> run(ActorSystem sys, ActorRef supervisor, String rpcAddress, int rpcPort) throws Exception {
		Materializer mat = ActorMaterializer.create(sys);
		/*
			Instantiate RPC handler
		*/
		StoreService impl = new StoreServiceImpl(supervisor);
		/*
			Start listening on port
		*/
		return Http.get(sys).bindAndHandleAsync(
			StoreServiceHandlerFactory.create(impl, sys),
			ConnectHttp.toHost(rpcAddress, rpcPort),
			mat
		);
	}

	private static void startNode(int port) throws UnknownHostException {
		/*
			Override the configuration of the port
		*/
		final Config nodeConfig = config
			.withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(getLocalHostLANAddress().getHostAddress()))
			.withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(port))
			.withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(Collections.singletonList(Consts.NODE_ACTOR_NAME)));
		/*
			Create an Akka system
		*/
		final ActorSystem system = ActorSystem.create(Consts.SYSTEM_NAME, nodeConfig);
		/*
			Create an actor
		*/
		system.actorOf(NodeActor.props(), Consts.NODE_ACTOR_NAME);
	}

}