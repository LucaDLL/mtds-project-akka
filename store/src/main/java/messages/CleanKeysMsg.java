package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

import com.google.common.primitives.UnsignedInteger;

public class CleanKeysMsg implements JsonSerializable {
    
    private final UnsignedInteger cleaningId;

    @JsonCreator
    public CleanKeysMsg() {
        this.cleaningId = UnsignedInteger.ZERO;
    }

    @JsonCreator
    public CleanKeysMsg(UnsignedInteger cleaningId) {
        this.cleaningId = cleaningId;
    }

    public UnsignedInteger getCleaningId() {
        return cleaningId;
    }

    @Override
    public String toString() {
        return "CleanKeysMsg [cleaningId=" + cleaningId + "]";
    }
}
