package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class StabilizationMsg implements JsonSerializable {
    private final String msg;

    @JsonCreator
    public StabilizationMsg() {
        this.msg = "StabilizationMsg";
    }

    @Override
    public String toString () {
        return msg;
    }
}