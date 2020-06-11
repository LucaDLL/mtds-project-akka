package messages;

import com.google.common.primitives.UnsignedInteger;

public class PutMsg implements JsonSerializable {

    private final UnsignedInteger key;
    private final String val;

    public PutMsg(UnsignedInteger key, String val){
        this.key = key;
        this.val = val;
    }

    public UnsignedInteger getKey() {
        return key;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "PutMsg [key=" + key + ", val=" + val + "]";
    }

}