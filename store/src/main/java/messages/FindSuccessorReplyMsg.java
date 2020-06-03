package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

import resources.NodePointer;

public class FindSuccessorReplyMsg implements JsonSerializable{

    private final NodePointer nodePointer;

    @JsonCreator
    public FindSuccessorReplyMsg(NodePointer nodePointer) {
        this.nodePointer = nodePointer;
    }

    public NodePointer getNodePointer() {
        return nodePointer;
    }

    @Override
    public String toString() {
        return "FindSuccessorReplyMsg [node=" + nodePointer + "]";
    }
}