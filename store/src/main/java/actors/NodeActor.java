package actors;

import messages.*;
import resources.Consts;
import resources.NodePointer;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

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

import java.math.BigInteger;

public class NodeActor extends AbstractActor {

	private final LoggingAdapter log;
	private final Cluster cluster;
	private int next;

	private NodePointer predecessor;
	private final NodePointer selfPointer;	
	private NodePointer[] fingerTable;

	private ActorRef storageActor, stabilizationActor, checkPredecessorActor, fixFingersActor, debugActor;

	private NodeActor() {
		log = Logging.getLogger(getContext().getSystem(), this);
		cluster = Cluster.get(getContext().getSystem());
		selfPointer = new NodePointer(cluster.selfMember());
		fingerTable = new NodePointer[Consts.ID_LENGTH];
		next = -1;
	}

	// subscribe to cluster changes
	@Override
	public void preStart() {

		predecessor = new NodePointer(null, null);

		for (int i = 0; i < fingerTable.length; i++) {
			fingerTable[i] = new NodePointer(null, null);
		}

		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
	}

	// re-subscribe when restart
	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}

	private final void onMemberUp(MemberUp mUp) {
		log.info("MEMBER {} IS UP", mUp.member());

		if((mUp.member()).equals(cluster.selfMember())) {
			if (mUp.member().upNumber() == 1)
				CreateChordRing();
			else
				JoinChordRing();
		}
	}

	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		log.info("MEMBER {} DETECTED AS UNREACHABLE", mUnreachable.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		log.info("MEMBER {} IS REMOVED", mRemoved.member());
	}
	
	private final void onMemberEvent(MemberEvent mEvent) { }

	private final void onFindSuccessorMsg(FindSuccessorMsg findSuccessorMsg) {
		/*
			n.find_successor(id)
				if(id \in (n, successor])
					return successor;
				else
					nn = closest_preceding_node(id);
					return nn.find_successor(id);
		*/
		BigInteger id = findSuccessorMsg.getKey();
		NodePointer nn = selfPointer;
		int i = Consts.ID_LENGTH - 1;
		ActorSelection a;

		if(!fingerTable[0].isEmpty()) {
			if(fingerTable[0].equals(selfPointer) || idBelongsToIntervalCircle(false, true, id, selfPointer.getId(), fingerTable[0].getId())){
				FindSuccessorReplyMsg msg = new FindSuccessorReplyMsg(fingerTable[0]);
				sender().tell(msg, ActorRef.noSender());
			}
			else{
				while (i >= 0 && nn.equals(selfPointer)){				
					if(!fingerTable[i].isEmpty() && idBelongsToIntervalCircle(false, false, id, selfPointer.getId(), fingerTable[i].getId())){
						nn = fingerTable[i];
					}
					i--;
				}
				
				if(nn.equals(selfPointer))
					a = getContext().getSystem().actorSelection(fingerTable[0].getAddress());
				else
					a = getContext().getSystem().actorSelection(nn.getAddress());
				
				a.forward(findSuccessorMsg, getContext());
			}
		}
	}

	private final void onStabilizationMsg(StabilizationMsg sMsg) {
		/*
			n.stabilize()
				x=successor.predecessor;
				if(x \in (n,successor))
					successor=x;
				successor.notify(n)
		*/
		NodePointer x;
		NodePointer successPointer = fingerTable[0];

		if(successPointer.equals(selfPointer)){
			x = predecessor;
		} else {
			ActorSelection a1 = getContext().getSystem().actorSelection(successPointer.getAddress());
			final Future<Object> reply = Patterns.ask(a1, new FindPredecessorMsg(), 250);
			try {
				FindPredecessorReplyMsg replyMsg = (FindPredecessorReplyMsg) Await.result(reply, Duration.Inf());
				x = replyMsg.getNodePointer();
			} catch (final Exception e) {
				log.info("FAILED STABILIZATION");
				x = new NodePointer(null, null);
			}
		}

		if(!x.isEmpty()){
			if(idBelongsToIntervalCircle(false, false, x.getId(), selfPointer.getId(), fingerTable[0].getId())){
				fingerTable[0] = x;
			}
				
			ActorSelection a2 = getContext().getSystem().actorSelection(fingerTable[0].getAddress());
			a2.tell(new NotifyMsg(selfPointer), self());
		}
	}

	private final void onFindPredecessorMsg(FindPredecessorMsg fpMsg) {
		sender().tell(new FindPredecessorReplyMsg(predecessor), self());
	}

	private final void onNotifyMsg(NotifyMsg nMsg) {
		/*
			n.notify(nn)
				if(predecessor is nil or nn \in (predecessor, n))
					predecessor=nn;
		*/
		NodePointer nn = nMsg.getNodePointer(); 
		
		if ( predecessor.isEmpty() || !predecessor.equals(nn) && idBelongsToIntervalCircle(false, false, nn.getId(), predecessor.getId(), selfPointer.getId()) ) {
			log.info("NEW PREDECESSOR {}", nn.getAddress());
			predecessor = nn;
		}
	}

	private final void onFixFingersMsg(FixFingersMsg ffMsg) {
		/*
			n.fix_fingers()
				next=next+1;
				if (next>m) 
					next=1;
				finger[next]=find_successor(n+2^{next-1});
		*/
		NodePointer nn = selfPointer;
		int i = Consts.ID_LENGTH - 1;
		ActorSelection a;
		
		next = (next + 1) % Consts.ID_LENGTH;
		BigInteger id = Mod((selfPointer.getId()).add((Consts.TWO_BIG_INTEGER).pow(next)), Consts.RING_SIZE);

		if(idBelongsToIntervalCircle(false, true, id, selfPointer.getId(), fingerTable[0].getId())){
			fingerTable[next] = fingerTable[0]; 
		} else {
			while (i >= 0 && nn.equals(selfPointer)){				
				if(!fingerTable[i].isEmpty() && idBelongsToIntervalCircle(false, false, id, selfPointer.getId(), fingerTable[i].getId())){
					nn = fingerTable[i];
				}
				i--;
			}
			
			if(nn.equals(selfPointer))
				a = getContext().getSystem().actorSelection(fingerTable[0].getAddress());
			else
				a = getContext().getSystem().actorSelection(nn.getAddress());

			final Future<Object> reply = Patterns.ask(a, new FindSuccessorMsg(id), 1250);
			try {
				FindSuccessorReplyMsg replyMsg = (FindSuccessorReplyMsg) Await.result(reply, Duration.Inf());
				fingerTable[next] = replyMsg.getNodePointer();
			} catch (final Exception e) {
				log.info("FAILED FIX FINGERS");
			}
		}
		
		/*
		NodePointer np = predecessor; //TODO not sure

		if(!np.isEmpty() && !np.equals(selfPointer)){
			next = (next + 1) % Consts.ID_LENGTH;
			BigInteger value = (selfPointer.getId()).add((Consts.TWO_BIG_INTEGER).pow(next-1));
			BigInteger arg = Mod(value, Consts.RING_SIZE);
			String address = np.getAddress();
			ActorSelection a = getContext().getSystem().actorSelection(address);
			final Future<Object> reply = Patterns.ask(a, new FindSuccessorMsg(arg), 250);
			try {
				FindSuccessorReplyMsg replyMsg = (FindSuccessorReplyMsg) Await.result(reply, Duration.Inf());
				fingerTable[next] = replyMsg.getNodePointer();
			} catch (final Exception e) {
				log.warning("FAILED FIX FINGERS");
			}
		}
		*/
	}

	private final void onCheckPredecessorMsg(CheckPredecessorMsg cpMsg) {
		/*
			n.check_predecessor()
				if(predecessor has failed)
					predecessor = nil
		*/
		if(!predecessor.isEmpty() && !predecessor.equals(selfPointer)){
			ActorSelection a = getContext().getSystem().actorSelection(predecessor.getAddress());
			final Future<Object> reply = Patterns.ask(a, new PredecessorPingMsg(), 500);
			try {
				Await.result(reply, Duration.Inf());
			} catch (final Exception e) {
				log.warning("PREDECESSOR NOT ALIVE");
				predecessor = new NodePointer(null,null);
			}
		}
	}

	private final void onPredecessorPingMsg(PredecessorPingMsg ppMsg) {
		sender().tell(new PredecessorPingMsg(), self());
	}

	private final void onPutMsg(PutMsg putMsg) {
		storageActor.tell(putMsg, self());
	}

	private final void onGetMsg(GetMsg getMsg) {
		final Future<Object> reply = Patterns.ask(storageActor, getMsg, 250);
        try {
			GetReplyMsg replyMsg = (GetReplyMsg) Await.result(reply, Duration.Inf());
			sender().tell(replyMsg, self());
        } catch (final Exception e) {
			log.info("FAILED GET");
        }
	}

	private final void onDebugMsg(DebugMsg msg) {
		log.warning("{} SAYS: PREDECESSOR {}", selfPointer.getId(), predecessor.getId());
		log.warning("{} SAYS: SUCCESSOR {}", selfPointer.getId(), fingerTable[0].getId());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(UnreachableMember.class, this::onUnreachableMember)
			.match(MemberRemoved.class, this::onMemberRemoved)
			.match(MemberEvent.class, this::onMemberEvent)
			.match(FindSuccessorMsg.class, this::onFindSuccessorMsg)
			.match(StabilizationMsg.class, this::onStabilizationMsg)
			.match(FindPredecessorMsg.class, this::onFindPredecessorMsg)
			.match(NotifyMsg.class, this::onNotifyMsg)
			.match(FixFingersMsg.class, this::onFixFingersMsg)
			.match(CheckPredecessorMsg.class, this::onCheckPredecessorMsg)
			.match(PredecessorPingMsg.class, this::onPredecessorPingMsg)
		    .match(PutMsg.class, this::onPutMsg)
			.match(GetMsg.class, this::onGetMsg)
			.match(DebugMsg.class, this::onDebugMsg)
		    .build();
	}

	public static final Props props() {
		return Props.create(NodeActor.class);
	}

	private void CreateChordRing() {
		/*
			n.create() 
				predecessor = nil;
				successor = n;	
		*/
		predecessor = selfPointer;
		fingerTable[0] = selfPointer;
		StartActors();
	}

	private void JoinChordRing() {
		/*
			n.join(n') 
				predecessor = nil;
				successor = n'.find_successor(n);	
		*/
		NodePointer np = ClusterFirstElementNodePointer(cluster, cluster.selfMember());

		if(!np.isEmpty()){
			
			FindSuccessorMsg findSuccessorMsg = new FindSuccessorMsg(selfPointer.getId());
			ActorSelection nodeToJoin = getContext().getSystem().actorSelection(np.getAddress());

			final Future<Object> reply = Patterns.ask(nodeToJoin, findSuccessorMsg, 20000);
			try {
				FindSuccessorReplyMsg replyMsg = (FindSuccessorReplyMsg) Await.result(reply, Duration.Inf());
				
				predecessor = new NodePointer(null, null);
				fingerTable[0] = replyMsg.getNodePointer();
				StartActors();
			} catch (final Exception e) {
				log.warning("FAILED JOIN CHORD RING");
				cluster.unsubscribe(getSelf());
				context().stop(self());
				System.exit(1);
			}
		}
	}

	private void StartActors() {
		storageActor = getContext().actorOf(StorageActor.props(), "storageActor");
		stabilizationActor = getContext().actorOf(StabilizationActor.props(self()), "stabilizationActor");
		checkPredecessorActor = getContext().actorOf(CheckPredecessorActor.props(self()), "checkPredecessorActor");
		fixFingersActor = getContext().actorOf(FixFingersActor.props(self()), "fixFingersActor");
		debugActor = getContext().actorOf(DebugActor.props(self()), "debugActor");
	}
}