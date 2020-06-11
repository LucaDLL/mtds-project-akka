package messages;

import com.google.common.primitives.UnsignedInteger;

public class RegistrationMsg implements JsonSerializable {
    
    private final String memberAddress;
    private final UnsignedInteger memberId;

    public RegistrationMsg(String memberAddress, UnsignedInteger memberId) {
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

    public UnsignedInteger getMemberId() {
        return memberId;
    }
}