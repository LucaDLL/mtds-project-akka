package projectMTDS.messages;

public class PutMsg implements JsonSerializable {

    private final String key;
    private final String val;

    public PutMsg(String key, String val){
        this.key = key;
        this.val = val;
    }

    public String getKey() {
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