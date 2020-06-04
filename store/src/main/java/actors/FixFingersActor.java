package actors;

import messages.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class FixFingersActor extends AbstractActor {
    
    private final ActorRef nodeActor;

	private FixFingersActor(ActorRef nodeActor) {

		this.nodeActor = nodeActor;
		
		getContext().getSystem().scheduler() .scheduleWithFixedDelay(
			Duration.ofMillis(ThreadLocalRandom.current().nextInt(500, 1500 + 1)),
			Duration.ofMillis(100), 
			nodeActor, 
			new FixFingersMsg(), 
			getContext().getSystem().dispatcher(), 
			ActorRef.noSender());
	}


	@Override
	public Receive createReceive() {
		return receiveBuilder()
		    .build();
	}

	public static final Props props(ActorRef nodeActor) {
		return Props.create(FixFingersActor.class, nodeActor);
	}
    
}