package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PredecessorRequestMsg implements JsonSerializable {
    
    private final Integer keysId;

    @JsonCreator
    public PredecessorRequestMsg(Integer keysId) {
        this.keysId = keysId;
    }

    public Integer getKeysId() {
        return keysId;
    }

    @Override
    public String toString() {
        return "PredecessorRequestMsg [keysId=" + keysId + "]";
    }
    
}