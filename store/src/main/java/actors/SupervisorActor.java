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
	/*
		SupervisorActor contains the routing information of the multiple NodeActor that compose the system.
		It routes the put and get requests to them, and it also generates the messages required to keep the store consistent.
	*/
    private final LoggingAdapter log;
    private final Cluster cluster;
	private TreeSet<NodePointer> nodes;
	private ActorRef schedulerActor;
	private Boolean hasEntries;

	private SupervisorActor() {
		log = Logging.getLogger(getContext().getSystem(), this);
		cluster = Cluster.get(getContext().getSystem());
		nodes = new TreeSet<>();
		schedulerActor = getContext().actorOf(SchedulerActor.props(self()), "schedulerActor");
		hasEntries = false;
	}

	@Override
	public void preStart() {
		/*
			Subscribe to cluster changes.
		*/
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsSnapshot(), MemberEvent.class, UnreachableMember.class, ReachableMember.class);
	}

	@Override
	public void postStop() {
		/*
			Re-subscribe when restart.
		*/
		cluster.unsubscribe(getSelf());
	}

	private final void onMemberUp(MemberUp mUp) {
		if(mUp.member().equals(cluster.selfMember())){
			log.warning("MEMBER {} IS UP", cluster.selfMember());
		}
	}
	
	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		/*
			If a NodeActor is unreachable, change its status to Down, which will lead to its removal.
		*/
		NodePointer unreachableNode = new NodePointer(mUnreachable.member());
		log.warning("MEMBER {} IS UNREACHABLE", unreachableNode);
		cluster.down(mUnreachable.member().address());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		/*
			When a NodeActor is removed from the cluster, remove it from the local routing information.
		*/
		NodePointer removedNode = new NodePointer(mRemoved.member());
		log.warning("MEMBER {} IS REMOVED", removedNode);
		nodes.remove(removedNode);
	}
	
	private final void onMemberEvent(MemberEvent mEvent) {}

	private final void onRegistrationMsg(RegistrationMsg rMsg) {
		/*
			If a new NodeActor is up:
				1) Set the schedulerOff behavior.
				2) Add the new NodeActor to the routing information.
				3) Send a NewPredecessorMsg to its successor.
				4) Set the schedulerOn behavior.
		*/
		getContext().become(schedulerOff());

		log.info("REGISTERING {}", rMsg.getMemberAddress());
		NodePointer newNode = new NodePointer(rMsg.getMemberAddress(), rMsg.getMemberId());
		nodes.add(newNode);

		NodePointer succ = newNode.equals(nodes.last()) ? nodes.first() : nodes.higher(newNode);
		UnsignedInteger oldPredId = newNode.equals(nodes.first()) ? nodes.last().getId() : nodes.lower(newNode).getId();

		ActorSelection a = selectActor(getContext(), succ.getAddress());
		a.tell(new NewPredecessorMsg(newNode.getAddress(), newNode.getId(), oldPredId), ActorRef.noSender());

		getContext().become(schedulerOn());
	}

	private final void onPeriodicMsg(PeriodicMsg pMsg){
		/*
			This message is sent periodically from the SchedulerActor.
			Send to each NodeActor a CleanKeysMsg and an UpdateSuccessorMsg.
		*/
		log.info("PERIODIC");
		if(!nodes.isEmpty() && hasEntries) {
			for (NodePointer np : nodes) {
				ActorSelection a = selectActor(getContext(), np.getAddress());
				a.tell(new CleanKeysMsg(getCleaningId(nodes, np)), ActorRef.noSender());
				if(Consts.REPLICATION_FACTOR > 1) 
					a.tell(new UpdateSuccessorsMsg(getSuccAddresses(nodes, np), getPredId(nodes,np)), ActorRef.noSender());
			}
		}
	}

	private final void onPutMsg(PutMsg putMsg) {
		/*
			Find the NodeActor responsible for the new key, and send the putMsg to it.
		*/
		if(!hasEntries) hasEntries = true;
		NodePointer target = targetSelection(nodes, putMsg.getKey());
		ActorSelection a = selectActor(getContext(), target.getAddress());
		a.tell(putMsg, ActorRef.noSender());
	}

	private final void onGetMsg(GetMsg getMsg) {
		/*
			Find the NodeActor responsible for the requested key, and ask it and the next R-1 successor to find the key.
		*/
		Iterator<NodePointer> it = nodes.tailSet(targetSelection(nodes, getMsg.getKey())).iterator();
		int tries = Consts.REPLICATION_FACTOR;

		while(tries > 0) {
			if(!it.hasNext()){
				it = nodes.iterator();
			}

			ActorSelection a = selectActor(getContext(), it.next().getAddress());
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
		/*
			Log how many keys are present in the system.
		*/
		Integer tot = 0;
		for(NodePointer np : nodes){
			ActorSelection a = selectActor(getContext(), np.getAddress());
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
		/*
			This method defines what messages NodeActor is able to process.
			Initially, SupervisorActor's behavior is defined by schedulerOff().
		*/
		return schedulerOff();
	}

	private final Receive schedulerOff() {
		/*
			When SupervisorActor is in this behavior, it cannot process PeriodicMsg, PutMsg and GetMsg.
			This means that the periodic maintenance operations of the store are not performed, and no new keys can be added.
		*/
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(UnreachableMember.class, this::onUnreachableMember)
			.match(MemberRemoved.class, this::onMemberRemoved)
			.match(MemberEvent.class, this::onMemberEvent)
			.match(RegistrationMsg.class, this::onRegistrationMsg)
			.match(DebugMsg.class, this::onDebugMsg)
		    .build();
	}

	private final Receive schedulerOn() {
		/*
			When SupervisorActor is in this behavior, the store is fully functioning.
			The periodic maintenance operations of the store are performed, and the put/get operations are supported.
		*/
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(UnreachableMember.class, this::onUnreachableMember)
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