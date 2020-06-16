package actors;

import messages.*;
import resources.Consts;
import resources.NodePointer;

import static resources.Methods.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.UnsignedInteger;

public class NodeActor extends AbstractActor {

	private final Cluster cluster;
	private final LoggingAdapter log;
	private final Map<UnsignedInteger, String> map;
	private final NodePointer selfPointer;

	private ActorSelection supervisor;
	
	private NodeActor() {
		cluster = Cluster.get(getContext().getSystem());
		log = Logging.getLogger(getContext().getSystem(), this);
		map = new HashMap<>();
		selfPointer = new NodePointer(cluster.selfMember());
	}

	// subscribe to cluster changes
	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
	}

	// re-subscribe when restart
	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}

	private final void onMemberUp(MemberUp mUp) {
		if(mUp.member().equals(cluster.selfMember())){
			log.warning("MEMBER {} IS UP", selfPointer);
		}

		if(mUp.member().hasRole(Consts.SUPERVISOR_ACTOR_NAME)){
			supervisor = SelectActor(getContext(),GetMemberAddress(mUp.member(), Consts.SUPERVISOR_ACTOR_SUFFIX));
			RegistrationMsg msg = new RegistrationMsg(selfPointer.getAddress(), selfPointer.getId());
			supervisor.tell(msg, self());
		}
	}

	private final void onNewPredecessorMsg (NewPredecessorMsg npMsg) {
		if(!map.isEmpty()) {
			MapTransferMsg mtMsg = new MapTransferMsg(MapSelector(map, npMsg.getOldPredId(), npMsg.getPredId())); 
			ActorSelection a = SelectActor(getContext(), npMsg.getPredAddress());
			a.tell(mtMsg, ActorRef.noSender());
		}
	}

	private final void onCleanKeysMsg(CleanKeysMsg cMsg) { 
		if(!map.isEmpty()){
			log.info("CLEANING OLD KEYS");

			List<UnsignedInteger> toRemove = new ArrayList<UnsignedInteger>();

			for(Map.Entry<UnsignedInteger,String> entry : map.entrySet()){
				if(!IdBelongsToInterval(entry.getKey(), cMsg.getCleaningId(), selfPointer.getId()))
					toRemove.add(entry.getKey());
			}
			
			log.warning("{} REMOVES {}", selfPointer.getId(), toRemove);

			for(UnsignedInteger key: toRemove){
				map.remove(key);
			}
		}
	}

	private final void onUpdateSuccessorsMsg(UpdateSuccessorsMsg usMsg) {
		log.info("UPDATING SUCCESSORS");
		if(!map.isEmpty()) {
			MapTransferMsg mtMsg = new MapTransferMsg(MapSelector(map, usMsg.getKeysId(), selfPointer.getId()));
			for(String succAddress: usMsg.getSuccAddresses()) {
				ActorSelection a = SelectActor(getContext(), succAddress);
				a.tell(mtMsg, ActorRef.noSender());
			}
		}
	}

	private final void onMapTransferMsg(MapTransferMsg mtMsg) {
		log.info("UPDATING LOCAL MAP");
		map.putAll(mtMsg.getMap());
	}

	private final void onPutMsg(PutMsg putMsg) {
		log.warning("{} RECEIVED {}", self().path(), putMsg);
		map.put(putMsg.getKey(), putMsg.getVal());
	}

	private final void onGetMsg(GetMsg getMsg) {
		log.warning("{} received {}", self().path(), getMsg);
		final String val = map.get(getMsg.getKey());
		final GetReplyMsg reply = new GetReplyMsg(val);
		sender().tell(reply, self());
	}

	private final void onDebugMsg(DebugMsg debugMsg) {
		log.warning("{} {}", selfPointer.getId(), map.size());
		final DebugReplyMsg reply = new DebugReplyMsg(map.size());
		sender().tell(reply, self());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(NewPredecessorMsg.class, this::onNewPredecessorMsg)
			.match(CleanKeysMsg.class, this::onCleanKeysMsg)
			.match(UpdateSuccessorsMsg.class, this::onUpdateSuccessorsMsg)
			.match(MapTransferMsg.class, this::onMapTransferMsg)
		    .match(PutMsg.class, this::onPutMsg)
			.match(GetMsg.class, this::onGetMsg)
			.match(DebugMsg.class, this::onDebugMsg)
		    .build();
	}

	public static final Props props() {
		return Props.create(NodeActor.class);
	}
}