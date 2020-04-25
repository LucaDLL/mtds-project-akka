package projectMTDS.messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class GetMsg implements JsonSerializable {

    private final String key;

    @JsonCreator
    public GetMsg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "GetMsg [key=" + key + "]";
    }
    
}