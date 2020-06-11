package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SuccessorMsg implements JsonSerializable {
    
    private final String successorAddress;

    @JsonCreator
    public SuccessorMsg(String successorAddress) {
        this.successorAddress = successorAddress;
    }

    public String getSuccessorAddress() {
        return successorAddress;
    }

    @Override
    public String toString() {
        return "SuccessorMsg [successorAddress=" + successorAddress + "]";
    }
}