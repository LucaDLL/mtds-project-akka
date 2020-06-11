package messages;

public class CleanOldKeysMsg implements JsonSerializable {
    
    private final String replicaAddress;
    private final Integer cleaningId;

    public CleanOldKeysMsg(String replicaAddress, Integer cleaningId) {
        this.replicaAddress = replicaAddress;
        this.cleaningId = cleaningId;
    }

    public String getReplicaAddress() {
        return replicaAddress;
    }

    public Integer getCleaningId() {
        return cleaningId;
    }

    @Override
    public String toString() {
        return "CleanOldKeysMsg [cleaningId=" + cleaningId + ", replicaAddress=" + replicaAddress + "]";
    }

}
