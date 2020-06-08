package messages;

public class SuccessorMsg implements JsonSerializable {
    
    private final String memberAddress;
    private final Integer memberId;

    public SuccessorMsg(String memberAddress, Integer memberId) {
        this.memberAddress = memberAddress;
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return "SuccessorMsg [memberAddress=" + memberAddress + ", memberId=" + memberId + "]";
    }

    public String getMemberAddress() {
        return memberAddress;
    }

    public Integer getMemberId() {
        return memberId;
    }

}