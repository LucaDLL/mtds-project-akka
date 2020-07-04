package actors;

import messages.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.time.Duration;

public class SchedulerActor extends AbstractActor {
	/*
		SchedulerActor sends a message periodically to SupervisorActor. 
	*/
    private final ActorRef supervisorActor;

	private SchedulerActor(ActorRef supervisorActor) {
		this.supervisorActor = supervisorActor;

		getContext().getSystem().scheduler().scheduleWithFixedDelay(
			Duration.ofMillis(5000),
			Duration.ofMillis(200), 
			supervisorActor, 
			new PeriodicMsg(),
			getContext().getSystem().dispatcher(), 
			ActorRef.noSender()
		);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
		    .build();
	}

	public static final Props props(ActorRef supervisorActor) {
		return Props.create(SchedulerActor.class, supervisorActor);
	}
    
}