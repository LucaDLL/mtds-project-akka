package messages;

public class PredecessorMsg implements JsonSerializable {
    
    private final String memberAddress;
    private final Integer memberId;

    public PredecessorMsg(String memberAddress, Integer memberId) {
        this.memberAddress = memberAddress;
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return "PredecessorMsg [memberAddress=" + memberAddress + ", memberId=" + memberId + "]";
    }

    public String getMemberAddress() {
        return memberAddress;
    }

    public Integer getMemberId() {
        return memberId;
    }

}