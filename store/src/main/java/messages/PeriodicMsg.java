package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PeriodicMsg implements JsonSerializable {
    
    private final String msg;

    @JsonCreator
    public PeriodicMsg() {
        this.msg = "PeriodicMsg";
    }

    @Override
    public String toString() {
        return msg;
    }
    
}