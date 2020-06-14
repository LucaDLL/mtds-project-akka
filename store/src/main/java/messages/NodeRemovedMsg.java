package messages;

import com.google.common.primitives.UnsignedInteger;

public class NodeRemovedMsg implements JsonSerializable {
    
    private final String address;
    private final UnsignedInteger keysId;

    public NodeRemovedMsg(String address, UnsignedInteger keysId) {
        this.address = address;
        this.keysId = keysId;
    }

    public UnsignedInteger getKeysId() {
        return keysId;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "SuccessorMsg [keysId=" + keysId + ", address=" + address + "]";
    }
 
}