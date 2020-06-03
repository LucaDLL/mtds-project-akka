package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class FixFingersMsg implements JsonSerializable {
    private final String msg;

    @JsonCreator
    public FixFingersMsg() {
        this.msg = "FixFingersMsg";
    }

    @Override
    public String toString () {
        return msg;
    }
}