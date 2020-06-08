package messages;

public class RegistrationMsg implements JsonSerializable {
    
    private final String memberAddress;
    private final Integer memberId;

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