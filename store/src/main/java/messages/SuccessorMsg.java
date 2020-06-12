package messages;

import com.google.common.primitives.UnsignedInteger;

public class SuccessorMsg implements JsonSerializable {
    
    private final String successorAddress;
    private final UnsignedInteger cleaningId;

    public SuccessorMsg(String successorAddress, UnsignedInteger cleaningId) {
        this.successorAddress = successorAddress;
        this.cleaningId = cleaningId;
    }

    public UnsignedInteger getCleaningId() {
        return cleaningId;
    }

    public String getSuccessorAddress() {
        return successorAddress;
    }

    @Override
    public String toString() {
        return "SuccessorMsg [cleaningId=" + cleaningId + ", successorAddress=" + successorAddress + "]";
    }
 
}