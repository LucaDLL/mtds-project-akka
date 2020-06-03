package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

import resources.NodePointer;

public class NotifyMsg implements JsonSerializable{

    private final NodePointer nodePointer;

    @JsonCreator
    public NotifyMsg(NodePointer nodePointer) {
        this.nodePointer = nodePointer;
    }

    public NodePointer getNodePointer() {
        return nodePointer;
    }

    @Override
    public String toString() {
        return "NotifyMsg [node=" + nodePointer + "]";
    }
}