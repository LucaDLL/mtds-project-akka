package messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.primitives.UnsignedInteger;

public class CleanOldKeysRequestMsg implements JsonSerializable {
    
    private final UnsignedInteger cleaningId;

    @JsonCreator
    public CleanOldKeysRequestMsg(UnsignedInteger cleaningId) {
        this.cleaningId = cleaningId;
    }

    public UnsignedInteger getCleaningId() {
        return cleaningId;
    }

    @Override
    public String toString() {
        return "CleanOldKeysMsg cleaningId = [" + cleaningId + "]";
    }
}
