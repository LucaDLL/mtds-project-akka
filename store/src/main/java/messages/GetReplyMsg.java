package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class GetReplyMsg implements JsonSerializable{

    private final String val;

    @JsonCreator
    public GetReplyMsg(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "ReplyMsg [val=" + val + "]";
    }
    
}