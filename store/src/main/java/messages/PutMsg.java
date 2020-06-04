package messages;

public class PutMsg implements JsonSerializable {

    private final Integer key;
    private final String val;

    public PutMsg(Integer key, String val){
        this.key = key;
        this.val = val;
    }

    public Integer getKey() {
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