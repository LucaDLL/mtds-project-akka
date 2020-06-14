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
			supervisor = getContext().getSystem().actorSelection(GetMemberAddress(mUp.member(), Consts.SUPERVISOR_ACTOR_SUFFIX));
			RegistrationMsg msg = new RegistrationMsg(selfPointer.getAddress(), selfPointer.getId());
			supervisor.tell(msg, self());
		}
	}

	private final void onSuccessorMsg(SuccessorMsg sMsg) {
		ActorSelection successor = getContext().getSystem().actorSelection(sMsg.getSuccessorAddress());
		SuccessorRequestMsg srMsg = new SuccessorRequestMsg(selfPointer.getId(), sMsg.getCleaningId());
		successor.tell(srMsg, self());
	}
	
	private final void onSuccessorRequestMsg(SuccessorRequestMsg srMsg){
		final Map<UnsignedInteger, String> newMap = new HashMap<>();
		List<UnsignedInteger> toRemove = new ArrayList<UnsignedInteger>();

		for(Map.Entry<UnsignedInteger,String> entry : map.entrySet()){
			if(!idBelongsToInterval(entry.getKey(), srMsg.getNewPredecessorId(), selfPointer.getId()))
				newMap.put(entry.getKey(), entry.getValue());
			if(!idBelongsToInterval(entry.getKey(), srMsg.getCleaningId(), selfPointer.getId()))
				toRemove.add(entry.getKey());
		}

		for(UnsignedInteger key: toRemove){
			map.remove(key);
		}

		MapTransferMsg mtMsg = new MapTransferMsg(newMap);
		sender().tell(mtMsg, ActorRef.noSender());
	}

	private final void onCleanOldKeysMsg(CleanOldKeysMsg cMsg){
		ActorSelection replica = getContext().getSystem().actorSelection(cMsg.getReplicaAddress());
		CleanOldKeysRequestMsg crMsg = new CleanOldKeysRequestMsg(cMsg.getCleaningId());
		replica.tell(crMsg, ActorRef.noSender());
	}

	private final void onCleanOldKeysRequestMsg(CleanOldKeysRequestMsg crMsg){
		List<UnsignedInteger> toRemove = new ArrayList<UnsignedInteger>();

		for(Map.Entry<UnsignedInteger,String> entry : map.entrySet()){
			if(!idBelongsToInterval(entry.getKey(), crMsg.getCleaningId(), selfPointer.getId()))
				toRemove.add(entry.getKey());
		}
		
		for(UnsignedInteger key: toRemove){
			map.remove(key);
		}
	}

	private final void onNodeRemovedMsg(NodeRemovedMsg nrMsg) {
		ActorSelection a = getContext().getSystem().actorSelection(nrMsg.getAddress());
		NodeRemovedRequestMsg nrrMsg = new NodeRemovedRequestMsg(nrMsg.getKeysId());
		a.tell(nrrMsg, self());
	}

	private final void onNodeRemovedRequestMsg(NodeRemovedRequestMsg nrrMsg) {
		final Map<UnsignedInteger, String> newMap = new HashMap<>();

		for(Map.Entry<UnsignedInteger,String> entry : map.entrySet()){
			if(!idBelongsToInterval(entry.getKey(), nrrMsg.getKey(), selfPointer.getId()))
				newMap.put(entry.getKey(), entry.getValue());
		}

		MapTransferMsg mtMsg = new MapTransferMsg(newMap);
		sender().tell(mtMsg, ActorRef.noSender());
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
			.match(SuccessorMsg.class, this::onSuccessorMsg)
			.match(SuccessorRequestMsg.class, this::onSuccessorRequestMsg)
			.match(CleanOldKeysMsg.class, this::onCleanOldKeysMsg)
			.match(CleanOldKeysRequestMsg.class, this::onCleanOldKeysRequestMsg)
			.match(NodeRemovedMsg.class, this::onNodeRemovedMsg)
			.match(NodeRemovedRequestMsg.class, this::onNodeRemovedRequestMsg)
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