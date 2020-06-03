package actors;

import messages.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.time.Duration;

public class CheckPredecessorActor extends AbstractActor {
    
    private final ActorRef nodeActor;

	private CheckPredecessorActor(ActorRef nodeActor) {

		this.nodeActor = nodeActor;
		
		getContext().getSystem().scheduler() .scheduleWithFixedDelay(
			Duration.ofMillis(1000),
			Duration.ofMillis(1000), 
			nodeActor, 
			new CheckPredecessorMsg(), 
			getContext().getSystem().dispatcher(), 
			ActorRef.noSender());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
		    .build();
	}

	public static final Props props(ActorRef nodeActor) {
		return Props.create(CheckPredecessorActor.class, nodeActor);
	}
    
}