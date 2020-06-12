package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DebugReplyMsg implements JsonSerializable {
    
    private final Integer size;

    @JsonCreator
    public DebugReplyMsg(Integer size) {
        this.size = size;
    }

    public Integer getSize() {
        return size;
    }

    @Override
    public String toString() {
        return size.toString();
    }
    
}