package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CheckPredecessorMsg implements JsonSerializable {
    private final String msg;

    @JsonCreator
    public CheckPredecessorMsg() {
        this.msg = "CheckPredecessorMsg";
    }

    @Override
    public String toString () {
        return msg;
    }
}