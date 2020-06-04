package resources;

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
}