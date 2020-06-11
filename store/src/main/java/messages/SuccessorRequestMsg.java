package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SuccessorRequestMsg implements JsonSerializable {
    
    private final Integer newPredecessorId;
	
	@JsonCreator
	public SuccessorRequestMsg(Integer newPredecessorId) {
		this.newPredecessorId = newPredecessorId;
	}

	public Integer getNewPredecessorId() {
		return newPredecessorId;
	}

	@Override
	public String toString() {
		return "SuccessorRequestMsg [newPredecessorId=" + newPredecessorId + "]";
	}

}