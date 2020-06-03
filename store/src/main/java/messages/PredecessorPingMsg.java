package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PredecessorPingMsg implements JsonSerializable {
    private final String msg;

    @JsonCreator
    public PredecessorPingMsg() {
        this.msg = "PredecessorPingMsg";
    }

    @Override
    public String toString () {
        return msg;
    }
}