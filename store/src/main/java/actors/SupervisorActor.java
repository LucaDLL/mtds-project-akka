package actors;

import messages.*;
import resources.NodePointer;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.TreeSet;

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
	}

	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		log.info("MEMBER {} DETECTED AS UNREACHABLE", mUnreachable.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		log.info("MEMBER {} IS REMOVED", mRemoved.member());
	}
	
	private final void onMemberEvent(MemberEvent mEvent) { }

	private final void onRegistrationMsg(RegistrationMsg registrationMsg) {
		log.warning("REGISTERING {}", registrationMsg.getMemberAddress());
		nodes.add(new NodePointer(registrationMsg.getMemberAddress(), registrationMsg.getMemberId()));
	}

	private final void onPutMsg(PutMsg putMsg) {
		sender().tell(new GetReplyMsg("Received put!"), self());
	}

	private final void onGetMsg(GetMsg getMsg) {
		log.warning("{}", nodes.first().toString());
		sender().tell(new GetReplyMsg("Received get!"), self());
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
		    .build();
	}

	public static final Props props() {
		return Props.create(SupervisorActor.class);
	}
}