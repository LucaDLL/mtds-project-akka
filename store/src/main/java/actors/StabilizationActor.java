package actors;

import messages.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.time.Duration;

public class StabilizationActor extends AbstractActor {
    
    private final ActorRef nodeActor;

	private StabilizationActor(ActorRef nodeActor) {

		this.nodeActor = nodeActor;

		getContext().getSystem().scheduler() .scheduleWithFixedDelay(
			Duration.ofMillis(1000), 
			Duration.ofMillis(400), 
			nodeActor, 
			new StabilizationMsg(), 
			getContext().getSystem().dispatcher(), 
			ActorRef.noSender());
	}


	@Override
	public Receive createReceive() {
		return receiveBuilder()
		    .build();
	}

	public static final Props props(ActorRef nodeActor) {
		return Props.create(StabilizationActor.class, nodeActor);
	}
    
}