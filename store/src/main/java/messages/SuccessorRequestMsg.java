package messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.primitives.UnsignedInteger;

public class SuccessorRequestMsg implements JsonSerializable {
    
    private final UnsignedInteger newPredecessorId;
	
	@JsonCreator
	public SuccessorRequestMsg(UnsignedInteger newPredecessorId) {
		this.newPredecessorId = newPredecessorId;
	}

	public UnsignedInteger getNewPredecessorId() {
		return newPredecessorId;
	}

	@Override
	public String toString() {
		return "SuccessorRequestMsg [newPredecessorId=" + newPredecessorId + "]";
	}

}