package projectMTDS.messages;

public class GetMsg implements JsonSerializable{

    private final String key;

    public GetMsg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "GetMsg [key=" + key + "]";
    }

    
    
}