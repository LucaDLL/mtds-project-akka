package messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.primitives.UnsignedInteger;

public class GetMsg implements JsonSerializable {
    
    private final UnsignedInteger key;

    @JsonCreator
    public GetMsg(UnsignedInteger key) {
        this.key = key;
    }

    public UnsignedInteger getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "GetMsg [key=" + key + "]";
    }
    
}