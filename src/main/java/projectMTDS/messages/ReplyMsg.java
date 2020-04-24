package projectMTDS.messages;

public class ReplyMsg implements JsonSerializable{

    private final String content;

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