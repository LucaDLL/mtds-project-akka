package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class GetReplyMsg implements JsonSerializable{

    private final String content;

    @JsonCreator
    public GetReplyMsg(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "ReplyMsg [content=" + content + "]";
    }
}