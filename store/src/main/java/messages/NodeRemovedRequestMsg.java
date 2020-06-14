package messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.primitives.UnsignedInteger;

public class NodeRemovedRequestMsg implements JsonSerializable {
    
    private final UnsignedInteger key;

    @JsonCreator
    public NodeRemovedRequestMsg(UnsignedInteger key) {
        this.key = key;
    }

    public UnsignedInteger getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "NodeRemovedRequestMsg [key=" + key + "]";
    }
}