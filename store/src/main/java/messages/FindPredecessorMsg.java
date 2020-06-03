package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class FindPredecessorMsg implements JsonSerializable {
    private final String msg;

    @JsonCreator
    public FindPredecessorMsg() {
        this.msg = "FindPredecessorMsg";
    }

    @Override
    public String toString () {
        return msg;
    }
}