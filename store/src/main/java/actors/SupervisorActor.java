package actors;

import messages.*;
import resources.Consts;
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

	private final void onRegistrationMsg(RegistrationMsg rMsg) {
		log.info("REGISTERING {}", rMsg.getMemberAddress());
		NodePointer newNode = new NodePointer(rMsg.getMemberAddress(), rMsg.getMemberId());
		
		if(!nodes.isEmpty()){
			NodePointer reqNodes[] = new NodePointer[2*Consts.REPLICATION_FACTOR + 1];
			reqNodes[Consts.REPLICATION_FACTOR] = newNode;
			Iterator<NodePointer> predIt = (nodes.headSet(newNode, false).isEmpty()) ? 
												nodes.descendingIterator() : 
												nodes.headSet(newNode, false).descendingIterator();
			Iterator<NodePointer> succIt = (nodes.tailSet(newNode, false).isEmpty()) ?
												nodes.iterator() : 
												nodes.tailSet(newNode, false).iterator();

			for(int i = 1; i < Consts.REPLICATION_FACTOR + 1; i++){
				if(!succIt.hasNext())
					succIt = nodes.iterator();
				if(!predIt.hasNext())
					predIt = nodes.descendingIterator();
				reqNodes[Consts.REPLICATION_FACTOR - i] = predIt.next();
				reqNodes[Consts.REPLICATION_FACTOR + i] = succIt.next();
			}
			/*
				Data from predecessor
			*/
			PredecessorMsg pMsg = new PredecessorMsg(
										reqNodes[Consts.REPLICATION_FACTOR - 1].getAddress(), 
										reqNodes[0].getId()
									);
			sender().tell(pMsg, ActorRef.noSender());
			/*
				Master keys from successor
			*/
			SuccessorMsg sMsg = new SuccessorMsg(
										reqNodes[Consts.REPLICATION_FACTOR + 1].getAddress()
									);
			sender().tell(sMsg, ActorRef.noSender());
			/*
				Clean old keys in replicas 
			*/
			for(int i = 0; i < Consts.REPLICATION_FACTOR; i++){
				CleanOldKeysMsg cMsg = new CleanOldKeysMsg(
											reqNodes[Consts.REPLICATION_FACTOR + 1 + i].getAddress(),
											reqNodes[1 + i].getId()
										);
				sender().tell(cMsg, ActorRef.noSender());
			}
		}
		nodes.add(newNode);
	}

	private final void onPutMsg(PutMsg putMsg) {
		Iterator<NodePointer> it = nodes.tailSet(TargetSelection(putMsg.getKey()), true).iterator();
		
		for(int i = 0; i < Consts.REPLICATION_FACTOR; i++) {
			if(!it.hasNext()){
				it = nodes.iterator();
			}
			ActorSelection a = getContext().getSystem().actorSelection(it.next().getAddress());
			a.tell(putMsg, ActorRef.noSender());
		}
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
		for(NodePointer np : nodes){
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
		return (nodes.higher(np) != null) ? nodes.higher(np) : nodes.first();
	}
}