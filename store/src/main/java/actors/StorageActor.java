package actors;

import messages.*;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;

public class StorageActor extends AbstractActor {
    
    private final LoggingAdapter log;
    
    private Map<String, String> map;
	
	private StorageActor() {
		this.map = new HashMap<>();
		this.log = Logging.getLogger(getContext().getSystem(), this);
	}

	private final void onPutMsg(PutMsg putMsg) {
		log.warning("{} received {}", self().path(), putMsg);
		map.put(putMsg.getKey(), putMsg.getVal());
	}

	private final void onGetMsg(GetMsg getMsg) {
		log.warning("{} received {}", self().path(), getMsg);
		final String val = map.get(getMsg.getKey());
		final GetReplyMsg reply = new GetReplyMsg(val);
		sender().tell(reply, self());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
		    .match(PutMsg.class, this::onPutMsg)
			.match(GetMsg.class, this::onGetMsg)
		    .build();
	}

	public static final Props props() {
		return Props.create(StorageActor.class);
	}
    
}