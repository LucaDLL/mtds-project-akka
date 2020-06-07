package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DebugMsg implements JsonSerializable {
    
    private final String msg;

    @JsonCreator
    public DebugMsg() {
        this.msg = "DebugMsg";
    }

    @Override
    public String toString() {
        return msg;
    }
    
}