package actors;

import messages.*;
import resources.Consts;
import resources.NodePointer;

import static resources.Methods.*;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
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
	/*
		Each NodeActor stores a part of the keys.
	*/
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

	@Override
	public void preStart() {
		/*
			subscribe to cluster changes
		*/
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class);
	}
	
	@Override
	public void postStop() {
		/*
			re-subscribe when restart
		*/
		cluster.unsubscribe(getSelf());
	}

	private final void onMemberUp(MemberUp mUp) {
		/*
			If SupervisorActor is up, then notify it that a new NodeActor is up with a RegistrationMsg.
		*/
		if(mUp.member().equals(cluster.selfMember())){
			log.warning("MEMBER {} IS UP", selfPointer);
		}

		if(mUp.member().hasRole(Consts.SUPERVISOR_ACTOR_NAME)){
			supervisor = selectActor(getContext(),getMemberAddress(mUp.member(), Consts.SUPERVISOR_ACTOR_SUFFIX));
			RegistrationMsg msg = new RegistrationMsg(selfPointer.getAddress(), selfPointer.getId());
			supervisor.tell(msg, self());
		}
	}

	private final void onNewPredecessorMsg(NewPredecessorMsg npMsg) {
		/*
			Send the new predecessor the keys for which it is now responsible of. 
		*/
		if(!map.isEmpty()) {
			MapTransferMsg mtMsg = new MapTransferMsg(mapSelector(map, npMsg.getOldPredId(), npMsg.getPredId())); 
			ActorSelection a = selectActor(getContext(), npMsg.getPredAddress());
			a.tell(mtMsg, ActorRef.noSender());
		}
	}

	private final void onCleanKeysMsg(CleanKeysMsg cMsg) { 
		/*
			Delete the keys that you don't have to store anymore.
		*/
		if(!map.isEmpty()){
			log.info("CLEANING OLD KEYS");

			List<UnsignedInteger> toRemove = new ArrayList<UnsignedInteger>();

			for(Map.Entry<UnsignedInteger,String> entry : map.entrySet()){
				if(!idBelongsToInterval(entry.getKey(), cMsg.getCleaningId(), selfPointer.getId()))
					toRemove.add(entry.getKey());
			}

			for(UnsignedInteger key: toRemove){
				map.remove(key);
			}
		}
	}

	private final void onUpdateSuccessorsMsg(UpdateSuccessorsMsg usMsg) {
		/*
			Send the keys for which you are responsible for to your successors.
		*/
		log.info("UPDATING SUCCESSORS");
		if(!map.isEmpty()) {
			MapTransferMsg mtMsg = new MapTransferMsg(mapSelector(map, usMsg.getKeysId(), selfPointer.getId()));
			for(String succAddress: usMsg.getSuccAddresses()) {
				ActorSelection a = selectActor(getContext(), succAddress);
				a.tell(mtMsg, ActorRef.noSender());
			}
		}
	}

	private final void onMapTransferMsg(MapTransferMsg mtMsg) {
		/*
			Add the entries to your local map.
		*/
		log.info("UPDATING LOCAL MAP");
		map.putAll(mtMsg.getMap());
	}

	private final void onPutMsg(PutMsg putMsg) {
		/*
			Add the new entry to your local map.
		*/
		log.warning("{} RECEIVED {}", self().path(), putMsg);
		map.put(putMsg.getKey(), putMsg.getVal());
	}

	private final void onGetMsg(GetMsg getMsg) {
		/*
			Check if you have the requested key and reply to the sender.
		*/
		log.warning("{} RECEIVED {}", self().path(), getMsg);
		final String val = map.get(getMsg.getKey());
		final GetReplyMsg reply = new GetReplyMsg(val);
		sender().tell(reply, self());
	}

	private final void onDebugMsg(DebugMsg debugMsg) {
		/*
			Log the size of your current map.
		*/
		log.warning("{} {}", selfPointer.getId(), map.size());
		final DebugReplyMsg reply = new DebugReplyMsg(map.size());
		sender().tell(reply, self());
	}

	@Override
	public Receive createReceive() {
		/*
			This method defines what messages NodeActor is able to process.
		*/
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