package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class GetMsg implements JsonSerializable {
    
    private final Integer key;

    @JsonCreator
    public GetMsg(Integer key) {
        this.key = key;
    }

    public Integer getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "GetMsg [key=" + key + "]";
    }
    
}