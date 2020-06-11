package messages;

public class PredecessorMsg implements JsonSerializable {
    
    private final String predecessorAddress;
    private final Integer keysId;

    public PredecessorMsg(String predecessorAddress, Integer keysId) {
        this.predecessorAddress = predecessorAddress;
        this.keysId = keysId;
    }

    public String getPredecessorAddress() {
        return predecessorAddress;
    }

    public Integer getKeysId() {
        return keysId;
    }

    @Override
    public String toString() {
        return "PredecessorMsg [keysId=" + keysId + ", predecessorAddress=" + predecessorAddress + "]";
    }
    
}