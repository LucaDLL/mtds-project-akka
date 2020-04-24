package projectMTDS;

import projectMTDS.messages.*;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ClientActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Cluster cluster = Cluster.get(getContext().getSystem());

    // subscribe to cluster changes
	@Override
	public void preStart() {
		// #subscribe
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
	}

	// re-subscribe when restart
	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
    }
    
    private final void onMemberUp(MemberUp mUp) {
		log.info("Member is Up: {}", mUp.member());
	}

	private final void onUnreachableMember(UnreachableMember mUnreachable) {
		log.info("Member detected as unreachable: {}", mUnreachable.member());
	}

	private final void onMemberRemoved(MemberRemoved mRemoved) {
		log.info("Member is Removed: {}", mRemoved.member());
	}

	private final void onMemberEvent(MemberEvent mEvent) {

	}
    
    private final void onPutMsg(PutMsg putMsg){
        log.info("Client puts {}", putMsg); //System.out.println("Client puts: " + putMsg);
        //TODO server tell
        //server.tell(putMsg, self());
    }

    private final void onGetMsg(GetMsg getMsg){
        log.info("Client wants to get {}", getMsg); //System.out.println("Client wants to get: key " + getMsg);
        //TODO server tell
        //server.tell(getMsg, self()); 
    }

    private final void onReplyMsg(ReplyMsg replyMsg) {
        log.info("Client wants to get {}", replyMsg); //System.out.println("Client received: " + replyMsg);
    }
    
    @Override
    public Receive createReceive(){
        return receiveBuilder() 
            .match(MemberUp.class, this::onMemberUp)
            .match(UnreachableMember.class, this::onUnreachableMember)
            .match(MemberRemoved.class, this::onMemberRemoved)
            .match(MemberEvent.class, this::onMemberEvent)
            .match(PutMsg.class, this::onPutMsg) //
            .match(GetMsg.class, this::onGetMsg) //
            .match(ReplyMsg.class, this::onReplyMsg) //
            .build();
    }

	public static final Props props() {
		return Props.create(ClientActor.class);
	}
}
