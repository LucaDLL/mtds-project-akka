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

import java.util.Iterator;
import java.util.TreeSet;

import com.google.common.primitives.UnsignedInteger;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class SupervisorActor extends AbstractActor {

    private final LoggingAdapter log;
    private final Cluster cluster;
	private TreeSet<NodePointer> nodes;
	private ActorRef schedulerActor;

	private SupervisorActor() {
		log = Logging.getLogger(getContext().getSystem(), this);
		cluster = Cluster.get(getContext().getSystem());
		nodes = new TreeSet<>();
		schedulerActor = getContext().actorOf(SchedulerActor.props(self()), "schedulerActor");
	}

	// subscribe to cluster changes
	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class, ReachableMember.class);
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
		NodePointer unreachableNode = new NodePointer(mUnreachable.member());
		log.warning("MEMBER {} IS UNREACHABLE", unreachableNode);
		//nodes.remove(unreachableNode);
	}
	
	private final void onReachableMember(ReachableMember rm) {
		log.warning("MEMBER {} IS REACHABLE", rm.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		NodePointer removedNode = new NodePointer(mRemoved.member());
		log.warning("MEMBER {} IS REMOVED", removedNode);
		nodes.remove(removedNode);
	}
	
	private final void onMemberEvent(MemberEvent mEvent) {

	}

	private final void onRegistrationMsg(RegistrationMsg rMsg) {

		getContext().become(schedulerOff());

		log.info("REGISTERING {}", rMsg.getMemberAddress());
		NodePointer newNode = new NodePointer(rMsg.getMemberAddress(), rMsg.getMemberId());
		nodes.add(newNode);

		NodePointer succ = newNode.equals(nodes.last()) ? nodes.first() : nodes.higher(newNode);
		UnsignedInteger oldPredId = newNode.equals(nodes.first()) ? nodes.last().getId() : nodes.lower(newNode).getId();

		ActorSelection a = SelectActor(getContext(), succ.getAddress());
		a.tell(new NewPredecessorMsg(newNode.getAddress(), newNode.getId(),oldPredId), ActorRef.noSender());

		getContext().become(schedulerOn());
	}

	private final void onPeriodicMsg(PeriodicMsg pMsg){
		log.info("PERIODIC");
		if(!nodes.isEmpty()) {
			for (NodePointer np : nodes) {
				ActorSelection a = SelectActor(getContext(), np.getAddress());
				a.tell(new CleanKeysMsg(GetCleaningId(nodes, np)), ActorRef.noSender());
				if(Consts.REPLICATION_FACTOR > 1) 
					a.tell(new UpdateSuccessorsMsg(GetSuccAddresses(nodes, np), GetPredId(nodes,np)), ActorRef.noSender());
			}
		}
	}

	private final void onPutMsg(PutMsg putMsg) {
		NodePointer target = TargetSelection(nodes, putMsg.getKey());
		ActorSelection a = SelectActor(getContext(), target.getAddress());
		a.tell(putMsg, ActorRef.noSender());
	}

	private final void onGetMsg(GetMsg getMsg) {
		Iterator<NodePointer> it = nodes.tailSet(TargetSelection(nodes, getMsg.getKey())).iterator();
		int tries = Consts.REPLICATION_FACTOR;

		while(tries > 0) {
			if(!it.hasNext()){
				it = nodes.iterator();
			}

			ActorSelection a = SelectActor(getContext(), it.next().getAddress());
			final Future<Object> reply = Patterns.ask(a, getMsg, 1000);
			try {
				GetReplyMsg getReplyMsg = (GetReplyMsg) Await.result(reply, Duration.Inf());
				sender().tell(getReplyMsg, self());
				tries = 0;
			} catch (final Exception e) {
				log.info("FAILED GET");
				tries--;
			}
		}
	}

	private final void onDebugMsg(DebugMsg debugMsg) {
		Integer tot = 0;
		for(NodePointer np : nodes){
			ActorSelection a = SelectActor(getContext(), np.getAddress());
			final Future<Object> reply = Patterns.ask(a, debugMsg, 1000);
			try {
				DebugReplyMsg debugReplyMsg = (DebugReplyMsg) Await.result(reply, Duration.Inf());
				tot += debugReplyMsg.getSize();
			} catch (final Exception e) {
				log.info("FAILED DEBUG");
			}
		}
		log.warning("TOTAL SIZE {}", tot);
		sender().tell(new DebugReplyMsg(tot), self());
	}

	@Override
	public Receive createReceive() {
		return schedulerOff();
	}

	private final Receive schedulerOff() {
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(UnreachableMember.class, this::onUnreachableMember)
			.match(ReachableMember.class, this::onReachableMember)
			.match(MemberRemoved.class, this::onMemberRemoved)
			.match(MemberEvent.class, this::onMemberEvent)
			.match(RegistrationMsg.class, this::onRegistrationMsg)
			.match(DebugMsg.class, this::onDebugMsg)
		    .build();
	}

	private final Receive schedulerOn() {
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(UnreachableMember.class, this::onUnreachableMember)
			.match(ReachableMember.class, this::onReachableMember)
			.match(MemberRemoved.class, this::onMemberRemoved)
			.match(MemberEvent.class, this::onMemberEvent)
			.match(RegistrationMsg.class, this::onRegistrationMsg)
			.match(PeriodicMsg.class, this::onPeriodicMsg)
		    .match(PutMsg.class, this::onPutMsg)
			.match(GetMsg.class, this::onGetMsg)
			.match(DebugMsg.class, this::onDebugMsg)
		    .build();
	}

	public static final Props props() {
		return Props.create(SupervisorActor.class);
	}
}