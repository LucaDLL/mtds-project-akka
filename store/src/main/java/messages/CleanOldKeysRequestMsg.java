package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CleanOldKeysRequestMsg implements JsonSerializable {
    
    private final Integer cleaningId;

    @JsonCreator
    public CleanOldKeysRequestMsg(Integer cleaningId) {
        this.cleaningId = cleaningId;
    }

    public Integer getCleaningId() {
        return cleaningId;
    }


    @Override
    public String toString() {
        return "CleanOldKeysMsg cleaningId = [" + cleaningId + "]";
    }
}
