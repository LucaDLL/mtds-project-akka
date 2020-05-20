package resources;

import java.math.BigInteger;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;

import akka.cluster.Cluster;
import akka.cluster.Member;

public class Methods {

    public static String GetMemberAddress(Member member) {
        return new String(member.address().toString()+ Consts.NODE_ACTOR_SUFFIX);
    }

    public static String GetMemberUniqueAddress(Member member) {
        return new String(member.uniqueAddress().toString());
    }

    public static BigInteger Mod(BigInteger value, BigInteger mod) {
        return (mod.add(value.remainder(mod))).remainder(mod);
    }

    public static BigInteger Sha1(String value) {
        return new BigInteger(DigestUtils.sha1Hex(value), 16);
    }

    public static NodePointer ClusterFirstElementNodePointer(Cluster cluster, Member member) {
        /*
            Returns the first element of the cluster which is different from member
        */
        Iterator<Member> iterator =  (cluster.state().getMembers()).iterator();
        Member candidate = member;

        while(candidate.equals(member) && iterator.hasNext())
            candidate = iterator.next();

        return new NodePointer(candidate);
    }

    public static boolean idBelongsToIntervalCircle(boolean leftIncluded, boolean rightIncluded, BigInteger id, BigInteger nodeId, BigInteger successorId) {
        /*
            Context: nodes in ring.
            TODO add explanation, left and right included
        */
        BigInteger successorIdTemp;

        if(successorId.compareTo(nodeId) == -1)
            successorIdTemp = successorId.add(Consts.RING_SIZE);
        else
            successorIdTemp = successorId;

        if(nodeId.compareTo(successorIdTemp) == 0 || (id.compareTo(nodeId) == 1 && id.compareTo(successorIdTemp) == -1)) 
            return true;
        else
            return false;
    }


    //public static NodePointer FindSuccessor(NodePointer n, BigInteger id) throws Exception {
        /*
            n.find_successor(id)
                if( id \in (n, successor])
                    return successor;
                else
                    n' = closest_preceding_node(id);
                    return n'.find_successor(id);
        */

        



    //}

    /*
    private ActorSelection lookup(String key) throws Exception {

        Member member = (cluster.state().getMembers()).iterator().next();
        ActorSelection node = system.actorSelection(GetMemberAddress(member));
        LookupMsg lookupMsg = new LookupMsg(Sha1(key));
        Boolean continueLookup = true;

        while (continueLookup){
            final Future<Object> reply = Patterns.ask(node, lookupMsg, 1000);
            LookupReplyMsg replyMsg = (LookupReplyMsg) Await.result(reply, Duration.Inf());
            node = system.actorSelection(replyMsg.getAddress());
            continueLookup = replyMsg.getContinueLookup();
        }

        return node;
    }
    */


    
}