package resources;

import akka.cluster.Member;

public class Methods {

    public static String GetMemberAddress(Member member, String suffix) {
        return new String(member.address().toString() + suffix);
    }

    public static String GetMemberUniqueAddress(Member member) {
        /*
            For hashing
        */
        return new String(member.uniqueAddress().toString());
    }
}