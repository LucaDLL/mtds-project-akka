package messages;

import com.google.common.primitives.UnsignedInteger;

public class CleanOldKeysMsg implements JsonSerializable {
    
    private final String replicaAddress;
    private final UnsignedInteger cleaningId;

    public CleanOldKeysMsg(String replicaAddress, UnsignedInteger cleaningId) {
        this.replicaAddress = replicaAddress;
        this.cleaningId = cleaningId;
    }

    public String getReplicaAddress() {
        return replicaAddress;
    }

    public UnsignedInteger getCleaningId() {
        return cleaningId;
    }

    @Override
    public String toString() {
        return "CleanOldKeysMsg [cleaningId=" + cleaningId + ", replicaAddress=" + replicaAddress + "]";
    }

}
