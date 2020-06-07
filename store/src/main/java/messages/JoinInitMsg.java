package messages;

public class JoinInitMsg implements JsonSerializable {
    
    private final String memberAddress;
    private final Integer memberId;

    public JoinInitMsg(String memberAddress, Integer memberId) {
        this.memberAddress = memberAddress;
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return "JoinInitMsg [memberAddress=" + memberAddress + ", memberId=" + memberId + "]";
    }

    public String getMemberAddress() {
        return memberAddress;
    }

    public Integer getMemberId() {
        return memberId;
    }

}