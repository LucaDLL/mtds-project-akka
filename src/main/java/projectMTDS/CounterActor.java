package projectMTDS;

import akka.actor.AbstractActor;
import akka.actor.Props;
import projectMTDS.messages.DecMessage;
import projectMTDS.messages.IncMessage;

public class CounterActor extends AbstractActor {
	private int counter;

	private CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder() //
		    .match(IncMessage.class, msg -> msg.getAmount() < 5, this::onIncMessage) //
		    .match(DecMessage.class, msg -> msg.getAmount() < 5, this::onDecMessage) //
		    .build();
	}

	private final void onIncMessage(IncMessage message) {
		++counter;
		System.out.println("Counter increased to: " + counter);
	}

	private final void onDecMessage(DecMessage message) {
		--counter;
		System.out.println("Counter increased to: " + counter);
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}