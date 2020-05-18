package store;

import messages.*;
import resources.Consts;
import resources.NodePointer;
import static resources.Methods.*;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.ClusterEvent.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class StoreNode extends AbstractActor {

	private boolean updatable;
	private NodePointer predecessor;
	private List<NodePointer> fingerTable;
	private Map<String, String> map;
	private final LoggingAdapter log;
	private final Cluster cluster;

	private StoreNode() {
		this.updatable = false;
		this.predecessor = new NodePointer(null, null);
		this.fingerTable = new ArrayList<>();
		this.map = new HashMap<>();
		this.log = Logging.getLogger(getContext().getSystem(), this);
		this.cluster = Cluster.get(getContext().getSystem());
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
		log.info("Member is Up: {}", mUp.member());

		if(mUp.member().upNumber() == Consts.MIN_NR_OF_MEMBERS && !updatable){
			Initialize();
			this.updatable = true;

			for(Member m: cluster.state().getMembers()) {
				log.warning("{}", m);
			}
		}

		if(updatable) {
			//TODO new node join
		}

		/*
		Member selfMember = cluster.selfMember();
		Member upMember = mUp.member();
		
		if(selfMember.equals(upMember)){
			
		}	
		*/	
	}

	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		log.info("Member detected as unreachable: {}", mUnreachable.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		log.info("Member is Removed: {}", mRemoved.member());
	}
	
	private final void onMemberEvent(MemberEvent mEvent) { }

	private final void onLookupMsg (LookupMsg lookupMsg) {
		BigInteger myId = Sha1(GetMemberUniqueAddress(cluster.selfMember()));
		BigInteger keyId = lookupMsg.getKey();
		NodePointer candidate = fingerTable.get(0);

		for(NodePointer np: fingerTable) {
			BigInteger n = np.getId();
			if(n.compareTo(myId) == 1 && n.compareTo(keyId) == 1 && n.compareTo(candidate.getId()) == 1){
				candidate = np;
			}
		}

		if (candidate.equals(fingerTable.get(0)))
			sender().tell(new LookupReplyMsg(false, candidate.getAddress()), self());
		else
			sender().tell(new LookupReplyMsg(true, candidate.getAddress()), self());

	}

	private final void onPutMsg(PutMsg putMsg) {
		log.warning("{} received {}", cluster.selfMember().address(), putMsg);
		map.put(putMsg.getKey(), putMsg.getVal());
	}

	private final void onGetMsg(GetMsg getMsg) {

		log.warning("{} received {}", cluster.selfMember().address(), getMsg);

		final String val = map.get(getMsg.getKey());
		final GetReplyMsg reply = new GetReplyMsg(val);
		sender().tell(reply, self());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(MemberUp.class, this::onMemberUp)
			.match(UnreachableMember.class, this::onUnreachableMember)
			.match(MemberRemoved.class, this::onMemberRemoved)
			.match(MemberEvent.class, this::onMemberEvent)
			.match(LookupMsg.class, this::onLookupMsg)
		    .match(PutMsg.class, this::onPutMsg)
			.match(GetMsg.class, this::onGetMsg)
		    .build();
	}

	public static final Props props() {
		return Props.create(StoreNode.class);
	}

	private NodePointer InitPredecessorNode(BigInteger id) throws Exception {
		Iterator<Member> members = (cluster.state().getMembers()).iterator();
		Member curr;
		Member candidatePredecessor = null;
		BigInteger distance = Consts.RING_SIZE;
		BigInteger candidateDistance;

		while(members.hasNext()){
			curr = members.next();
			candidateDistance = Mod(id.subtract(Sha1(GetMemberUniqueAddress(curr))),Consts.RING_SIZE);

			if(candidateDistance.compareTo(distance) == -1){ // if Mod < distance 
				distance = candidateDistance;
				candidatePredecessor = curr;
			}
		}

		if (candidatePredecessor == null) 
			throw new Exception("No predecessor");
		else	
			return new NodePointer(GetMemberAddress(candidatePredecessor), Sha1(GetMemberUniqueAddress(candidatePredecessor)));
	}

	private NodePointer InitSuccessorNode(BigInteger id) throws Exception {
		Iterator<Member> members = (cluster.state().getMembers()).iterator();
		Member curr;
		Member candidateSuccessor = null;
		BigInteger distance = Consts.RING_SIZE;
		BigInteger candidateDistance;

		while(members.hasNext()){
			curr = members.next();
			candidateDistance = Mod(Sha1(GetMemberUniqueAddress(curr)).subtract(id), Consts.RING_SIZE);

			if(candidateDistance.compareTo(distance) == -1){ // if Mod < distance 
				distance = candidateDistance;
				candidateSuccessor = curr;
			}
		}
		
		if (candidateSuccessor == null) 
			throw new Exception("No successor");
		else	
			return new NodePointer(GetMemberAddress(candidateSuccessor), Sha1(GetMemberUniqueAddress(candidateSuccessor)));
	}

	private void Initialize() {
		Iterator<Member> members = (cluster.state().getMembers()).iterator();
		BigInteger n = Sha1(GetMemberUniqueAddress(cluster.selfMember()));
		int i = 0;

		try {
			this.predecessor = InitPredecessorNode(n);
			while(members.hasNext() && i < Consts.ID_LENGTH) {
				members.next();
				this.fingerTable.add(InitSuccessorNode(n.add(Consts.TWO_BIG_INTEGER.pow(i)))); //n + 2^(i-1)
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}