package messages;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.primitives.UnsignedInteger;

public class MapTransferMsg implements JsonSerializable {
    
    private final Map<UnsignedInteger, String> map;

    @JsonCreator
    public MapTransferMsg(Map<UnsignedInteger, String> map) {
        this.map = map;
    }

    public Map<UnsignedInteger, String> getMap() {
        return map;
    }

    @Override
    public String toString() {
        return "MapTransferMsg [key=" + map + "]";
    }
    
}