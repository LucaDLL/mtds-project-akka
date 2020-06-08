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

import java.util.HashMap;
import java.util.Map;

public class NodeActor extends AbstractActor {

	private final Cluster cluster;
	private final LoggingAdapter log;
	private final Map<Integer, String> map;
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
		log.info("MEMBER {} IS UP", mUp.member());

		if(mUp.member().equals(cluster.selfMember())){
			log.warning("MEMBER {} IS UP", cluster.selfMember());
		}

		if(mUp.member().hasRole(Consts.SUPERVISOR_ACTOR_NAME)){
			log.info("SENDING REGISTRATION MSG");
			supervisor = getContext().getSystem().actorSelection(GetMemberAddress(mUp.member(), Consts.SUPERVISOR_ACTOR_SUFFIX));
			RegistrationMsg msg = new RegistrationMsg(selfPointer.getAddress(), selfPointer.getId());
			supervisor.tell(msg, self());
		}
	}

	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		log.info("MEMBER {} DETECTED AS UNREACHABLE", mUnreachable.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		log.info("MEMBER {} IS REMOVED", mRemoved.member());
	}
	
	private final void onMemberEvent(MemberEvent mEvent) { }

	private final void onSuccessorMsg(SuccessorMsg sMsg) {
		ActorSelection successor = getContext().getSystem().actorSelection(sMsg.getSuccesssorAddress());
		SuccessorRequestMsg msg = new SuccessorRequestMsg(selfPointer.getId(), sMsg.getPredecessorId());
		successor.tell(msg, self());
	}

	private final void onSuccessorRequestMsg(SuccessorRequestMsg srMsg){
		final Map<Integer, String> newMap = new HashMap<>();

		for(Map.Entry<Integer,String> entry : map.entrySet()){
			if(
				entry.getKey().compareTo(srMsg.getNewPredecessorId()) == -1 && 
				entry.getKey().compareTo(srMsg.getOldPredecessorId()) == 1
			){
				newMap.put(entry.getKey(), entry.getValue());
			}
		}

		MapTransferMsg msg = new MapTransferMsg(newMap); 
		sender().tell(msg, ActorRef.noSender());

		for(Map.Entry<Integer,String> entry : newMap.entrySet()){
			map.remove(entry.getKey());
		}
	}

	private final void onPredecessorMsg(PredecessorMsg pMsg) {
		log.warning("PREDECESSOR {}", pMsg);
		//TODO
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

	private final void onMapTransferMsg(MapTransferMsg mtMsg) {
		log.info("UPDATING LOCAL MAP");
		map.putAll(mtMsg.getMap());
	}

	private final void onDebugMsg(DebugMsg debugMsg) {
		log.warning("NUMBER OF ENTRIES {}", map.size());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(UnreachableMember.class, this::onUnreachableMember)
			.match(MemberRemoved.class, this::onMemberRemoved)
			.match(MemberEvent.class, this::onMemberEvent)
			.match(SuccessorMsg.class, this::onSuccessorMsg)
			.match(SuccessorRequestMsg.class, this::onSuccessorRequestMsg)
			.match(PredecessorMsg.class, this::onPredecessorMsg)
		    .match(PutMsg.class, this::onPutMsg)
			.match(GetMsg.class, this::onGetMsg)
			.match(MapTransferMsg.class, this::onMapTransferMsg)
			.match(DebugMsg.class, this::onDebugMsg)
		    .build();
	}

	public static final Props props() {
		return Props.create(NodeActor.class);
	}
}