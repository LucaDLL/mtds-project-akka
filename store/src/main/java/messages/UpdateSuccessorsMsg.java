package messages;

import java.util.List;

import com.google.common.primitives.UnsignedInteger;

public class UpdateSuccessorsMsg implements JsonSerializable {

    private final List<String> succAddresses;
    private final UnsignedInteger keysId;

    public UpdateSuccessorsMsg(List<String> succAddresses, UnsignedInteger keysId) {
        this.succAddresses = succAddresses;
        this.keysId = keysId;
    }

    public List<String> getSuccAddresses() {
        return succAddresses;
    }

    public UnsignedInteger getKeysId() {
        return keysId;
    }

    @Override
    public String toString() {
        return "UpdateSuccessorsMsg [keysId=" + keysId + ", succAddresses=" + succAddresses + "]";
    }

}
