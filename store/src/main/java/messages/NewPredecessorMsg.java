package messages;

import com.google.common.primitives.UnsignedInteger;

public class NewPredecessorMsg implements JsonSerializable {
    
    private final String predAddress;
    private final UnsignedInteger predId;
    private final UnsignedInteger oldPredId;

    public NewPredecessorMsg (String predAddress, UnsignedInteger predId, UnsignedInteger oldPredId){
        this.predAddress = predAddress;
        this.predId = predId;
        this.oldPredId = oldPredId;
    }

    public String getPredAddress() {
        return predAddress;
    }

    public UnsignedInteger getPredId() {
        return predId;
    }

    public UnsignedInteger getOldPredId() {
        return oldPredId;
    }

    @Override
    public String toString() {
        return "NewPredecessorMsg [oldPredId=" + oldPredId + ", predAddress=" + predAddress + ", predId=" + predId + "]";
    }
    
}