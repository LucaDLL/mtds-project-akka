package projectMTDS.messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ReplyMsg implements JsonSerializable{

    private final String content;

    @JsonCreator
    public ReplyMsg(String content) {
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