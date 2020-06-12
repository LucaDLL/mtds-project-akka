package messages;

import com.google.common.primitives.UnsignedInteger;

public class SuccessorRequestMsg implements JsonSerializable {
    
	private final UnsignedInteger newPredecessorId;
	private final UnsignedInteger cleaningId;
	
	public SuccessorRequestMsg(UnsignedInteger newPredecessorId, UnsignedInteger cleaningId) {
		this.newPredecessorId = newPredecessorId;
		this.cleaningId = cleaningId;
	}

	public UnsignedInteger getNewPredecessorId() {
		return newPredecessorId;
	}

	public UnsignedInteger getCleaningId() {
		return cleaningId;
	}

	@Override
	public String toString() {
		return "SuccessorRequestMsg [cleaningId=" + cleaningId + ", newPredecessorId=" + newPredecessorId + "]";
	}

}