package actors;

import messages.*;
import resources.NodePointer;

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

import java.util.Iterator;
import java.util.TreeSet;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class SupervisorActor extends AbstractActor {

    private final LoggingAdapter log;
    private final Cluster cluster;
    private TreeSet<NodePointer> nodes;
    
	private SupervisorActor() {
		log = Logging.getLogger(getContext().getSystem(), this);
		cluster = Cluster.get(getContext().getSystem());
		this.nodes = new TreeSet<>();
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
	}

	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		log.info("MEMBER {} DETECTED AS UNREACHABLE", mUnreachable.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		log.info("MEMBER {} IS REMOVED", mRemoved.member());
	}
	
	private final void onMemberEvent(MemberEvent mEvent) { }

	private final void onRegistrationMsg(RegistrationMsg registrationMsg) {
		log.info("REGISTERING {}", registrationMsg.getMemberAddress());
		nodes.add(new NodePointer(registrationMsg.getMemberAddress(), registrationMsg.getMemberId()));
	}

	private final void onPutMsg(PutMsg putMsg) {	
		NodePointer target = TargetSelection(putMsg.getKey());
		ActorSelection a = getContext().getSystem().actorSelection(target.getAddress());
		a.tell(putMsg, ActorRef.noSender());
	}

	private final void onGetMsg(GetMsg getMsg) {
		NodePointer target = TargetSelection(getMsg.getKey());
		ActorSelection a = getContext().getSystem().actorSelection(target.getAddress());
		final Future<Object> reply = Patterns.ask(a, getMsg, 1000);
		try {
			GetReplyMsg getReplyMsg = (GetReplyMsg) Await.result(reply, Duration.Inf());
			sender().tell(getReplyMsg, self());
		} catch (final Exception e) {
			log.info("FAILED GET");
		}
	}

	private final void onDebugMsg(DebugMsg debugMsg) {
		log.warning("DEBUG!");
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(UnreachableMember.class, this::onUnreachableMember)
			.match(MemberRemoved.class, this::onMemberRemoved)
			.match(MemberEvent.class, this::onMemberEvent)
			.match(RegistrationMsg.class, this::onRegistrationMsg)
		    .match(PutMsg.class, this::onPutMsg)
			.match(GetMsg.class, this::onGetMsg)
			.match(DebugMsg.class, this::onDebugMsg)
		    .build();
	}

	public static final Props props() {
		return Props.create(SupervisorActor.class);
	}

	private NodePointer TargetSelection(Integer value) {
		NodePointer candidate = nodes.higher(new NodePointer("", value));
		if(candidate == null) candidate = nodes.first();
		return candidate;
	}
}