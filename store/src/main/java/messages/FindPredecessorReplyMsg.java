package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

import resources.NodePointer;

public class FindPredecessorReplyMsg implements JsonSerializable{

    private final NodePointer predecessorPointer;

    @JsonCreator
    public FindPredecessorReplyMsg(NodePointer predecessorPointer) {
        this.predecessorPointer = predecessorPointer;
    }

    public NodePointer getNodePointer() {
        return predecessorPointer;
    }

    @Override
    public String toString() {
        return "FindPredecessorReplyMsg [node=" + predecessorPointer + "]";
    }
}