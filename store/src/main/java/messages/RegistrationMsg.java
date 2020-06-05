package messages;

import akka.cluster.Member;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RegistrationMsg implements JsonSerializable{

    private final String memberAddress;
    private final Integer memberId;

    @JsonCreator
    public RegistrationMsg(String memberAddress, Integer memberId) {
        this.memberAddress = memberAddress;
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return "RegistrationMsg [memberAddress=" + memberAddress + ", memberId=" + memberId + "]";
    }

    public String getMemberAddress() {
        return memberAddress;
    }

    public Integer getMemberId() {
        return memberId;
    }
    
}