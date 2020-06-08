package messages;

public class SuccessorRequestMsg implements JsonSerializable {
    
    private final Integer newPredecessorId;
    private final Integer oldPredecessorId;
    
	public SuccessorRequestMsg(Integer newPredecessorId, Integer oldPredecessorId) {
		this.newPredecessorId = newPredecessorId;
		this.oldPredecessorId = oldPredecessorId;
	}

	public Integer getNewPredecessorId() {
		return newPredecessorId;
	}

	public Integer getOldPredecessorId() {
		return oldPredecessorId;
	}

	@Override
	public String toString() {
		return "SuccessorRequestMsg [newPredecessorId=" + newPredecessorId + ", oldPredecessorId=" + oldPredecessorId
				+ "]";
	}
    
}