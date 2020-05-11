package resources;

import java.math.BigInteger;

import org.apache.commons.codec.digest.DigestUtils;

import akka.cluster.Member;

public class Methods {

    public static String GetMemberAddress(Member member) {
        return new String(member.address().toString()+ "/user/storeNode");
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
    
}