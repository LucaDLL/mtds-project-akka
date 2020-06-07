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
import akka.pattern.Patterns;

import java.util.HashMap;
import java.util.Map;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

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

	private final void onJoinInitMsg(JoinInitMsg joinInitMsg) {
		ActorSelection a = getContext().getSystem().actorSelection(joinInitMsg.getMemberAddress());
		MapTransferMsg msg = new MapTransferMsg(selfPointer.getId());

		final Future<Object> reply = Patterns.ask(a, msg, 1000);
		try {
			MapTransferReplyMsg mapTransferReplyMsg = (MapTransferReplyMsg) Await.result(reply, Duration.Inf());
			map.putAll(mapTransferReplyMsg.getMap());
		} catch (final Exception e) {
			log.info("FAILED TRANSFER");
		}
	}

	private final void onMapTransferMsg(MapTransferMsg mapTransferMsg) {
		final Map<Integer, String> joinMap = new HashMap<>();

		for(Map.Entry<Integer,String> entry : map.entrySet()){
			if(entry.getKey().compareTo(mapTransferMsg.getId()) == -1){
				joinMap.put(entry.getKey(), entry.getValue());
			}
		}
		MapTransferReplyMsg msg = new MapTransferReplyMsg(joinMap); 
		sender().tell(msg, ActorRef.noSender());
		
		for(Map.Entry<Integer,String> entry : joinMap.entrySet()){
			map.remove(entry.getKey());
		}

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
		    .match(PutMsg.class, this::onPutMsg)
			.match(GetMsg.class, this::onGetMsg)
			.match(JoinInitMsg.class, this::onJoinInitMsg)
			.match(MapTransferMsg.class, this::onMapTransferMsg)
			.match(DebugMsg.class, this::onDebugMsg)
		    .build();
	}

	public static final Props props() {
		return Props.create(NodeActor.class);
	}
}