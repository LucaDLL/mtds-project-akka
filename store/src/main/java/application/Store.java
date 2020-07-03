package application;

import actors.*;
import resources.Consts;
import static resources.Methods.*;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

class Store {

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
        		run(sys, supervisor, Consts.RPC_ADDRESS, Consts.RPC_PORT).thenAccept(binding -> {});
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
}