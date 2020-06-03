package actors;

import messages.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.time.Duration;

public class DebugActor extends AbstractActor {
    
    private final ActorRef nodeActor;

	private DebugActor(ActorRef nodeActor) {

		this.nodeActor = nodeActor;

		getContext().getSystem().scheduler() .scheduleWithFixedDelay(
			Duration.ofMillis(2000),
			Duration.ofMillis(2000), 
			nodeActor, 
			new DebugMsg(), 
			getContext().getSystem().dispatcher(), 
			ActorRef.noSender());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
		    .build();
	}

	public static final Props props(ActorRef nodeActor) {
		return Props.create(DebugActor.class, nodeActor);
	}
    
}