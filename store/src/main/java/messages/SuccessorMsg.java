package messages;

public class SuccessorMsg implements JsonSerializable {
    
    private final String successorAddress;
    private final Integer predecessorId;

    public SuccessorMsg(String successsorAddress, Integer predecessorId) {
        this.successorAddress = successsorAddress;
        this.predecessorId = predecessorId;
    }

    public String getSuccesssorAddress() {
        return successorAddress;
    }

    public Integer getPredecessorId() {
        return predecessorId;
    }

    @Override
    public String toString() {
        return "SuccessorMsg [predecessorId=" + predecessorId + ", successorAddress=" + successorAddress + "]";
    }
}