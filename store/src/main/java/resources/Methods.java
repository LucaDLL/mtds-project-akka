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
        /*
            For hashing
        */
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

        if (candidate.equals(member))
            return new NodePointer(null, null);
        else
            return new NodePointer(candidate);
    }

    public static NodePointer ImplNode(Cluster cluster) {
        /*
            To be used in StoreServiceImpl
        */
        Iterator<Member> iterator =  (cluster.state().getMembers()).iterator();
        Member candidate = iterator.next();

        return new NodePointer(candidate);
    }

    public static boolean idBelongsToIntervalCircle(boolean leftIncluded, boolean rightIncluded, BigInteger id, BigInteger nodeId, BigInteger successorId) {
        /*
            Context: nodes in ring.
        */
        BigInteger successorIdTemp;

        if(successorId.compareTo(nodeId) == 0)
            return true;
        else if(successorId.compareTo(nodeId) == -1)
            successorIdTemp = successorId.add(Consts.RING_SIZE);
        else
            successorIdTemp = successorId;

        if(!leftIncluded & !rightIncluded){
            return (id.compareTo(nodeId) == 1 && id.compareTo(successorIdTemp) == -1);
        } else if (!leftIncluded & rightIncluded) {
            return (id.compareTo(nodeId) == 1 && id.compareTo(successorIdTemp) != 1);
        } else if (leftIncluded & !rightIncluded) {
            return (id.compareTo(nodeId) != -1 && id.compareTo(successorIdTemp) == -1);
        } else { // both true
            return (id.compareTo(nodeId) != -1 && id.compareTo(successorIdTemp) != 1);
        }
        
    }
    
}