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

import java.util.TreeSet;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class SupervisorActor extends AbstractActor {

    private final LoggingAdapter log;
    private final Cluster cluster;
    private TreeSet<NodePointer> leaderNodes;
    
	private SupervisorActor() {
		log = Logging.getLogger(getContext().getSystem(), this);
		cluster = Cluster.get(getContext().getSystem());
		this.leaderNodes = new TreeSet<>();
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
			log.warning("MEMBER {} IS UP", cluster.selfMember());
		}
	}

	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		log.warning("MEMBER {} DETECTED AS UNREACHABLE", mUnreachable.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		log.warning("MEMBER {} IS REMOVED", mRemoved.member());
	}
	
	private final void onMemberEvent(MemberEvent mEvent) { }

	private final void onRegistrationMsg(RegistrationMsg registrationMsg) {
		log.info("REGISTERING {}", registrationMsg.getMemberAddress());
		
		NodePointer np = new NodePointer(registrationMsg.getMemberAddress(), registrationMsg.getMemberId());
		
		if(!leaderNodes.isEmpty()){
			log.info("SENDING JOIN INIT TO {}", sender());
			NodePointer successor = (leaderNodes.ceiling(np) != null) ? leaderNodes.ceiling(np) : leaderNodes.first();
			JoinInitMsg msg = new JoinInitMsg(successor.getAddress(), successor.getId());
			sender().tell(msg, ActorRef.noSender());
		}

		leaderNodes.add(np);
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
		for(NodePointer np : leaderNodes){
			ActorSelection a = getContext().getSystem().actorSelection(np.getAddress());
			a.tell(debugMsg, ActorRef.noSender());
		}
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
		NodePointer np = new NodePointer("", value);
		return (leaderNodes.higher(np) != null) ? leaderNodes.higher(np) : leaderNodes.first();
	}
}