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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class StoreNode extends AbstractActor {

	private final NodePointer precedessor;
	private final NodePointer[] fingerTable;
	private Boolean updatable;

	//TODO key to biginteger 
	private final Map<String, String> map;

	private final LoggingAdapter log;
	private final Cluster cluster;
	
	public NodePointer SuccessorNode(BigInteger id) throws Exception {

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

	public void CreateFingerTable() {
		Iterator<Member> members = (cluster.state().getMembers()).iterator();
		BigInteger n = Sha1(GetMemberUniqueAddress(cluster.selfMember()));
		int i = 0;

		try {
			while(members.hasNext() && i < Consts.ID_LENGTH) {
				this.fingerTable[i] = SuccessorNode(n.add(Consts.TWO_BIG_INTEGER.pow(i))); //n + 2^(i-1)
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private StoreNode() {
		this.precedessor = new NodePointer(null, null);
		this.fingerTable = new NodePointer[Consts.ID_LENGTH];
		this.map = new HashMap<>();
		this.log = Logging.getLogger(getContext().getSystem(), this);
		this.cluster = Cluster.get(getContext().getSystem());
		this.updatable = false;
	}

	// subscribe to cluster changes
	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
		
		//TODO: finger table initialization
		
	}

	// re-subscribe when restart
	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}

	private final void onMemberUp(MemberUp mUp) {
		log.info("Member is Up: {}", mUp.member());
		
		Member selfMember = cluster.selfMember();
		Member upMember = mUp.member();
		
		if(selfMember.equals(upMember)){
			updatable = true;
		}

		if(updatable.equals(true)) {
			//TODO update finger table when new member is up
		}

	}

	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		log.info("Member detected as unreachable: {}", mUnreachable.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		log.info("Member is Removed: {}", mRemoved.member());
	}
	
	private final void onMemberEvent(MemberEvent mEvent) { }

	private final void onLookupMsg (LookupMsg lookupMsg) {

	}

	private final void onPutMsg(PutMsg putMsg) {
		log.info(getSelf().path().name() + "#" + getSelf().path().uid() + " received {}", putMsg);	
		map.put(putMsg.getKey(), putMsg.getVal());
	}

	private final void onGetMsg(GetMsg getMsg) {
		log.info("Server received {}", getMsg);
		final String val = map.get(getMsg.getKey());
		final ReplyMsg reply = new ReplyMsg(val);
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

}