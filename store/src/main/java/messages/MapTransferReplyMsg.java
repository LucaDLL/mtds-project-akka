package messages;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MapTransferReplyMsg implements JsonSerializable {

    private final Map<Integer, String> map;

    @JsonCreator
    public MapTransferReplyMsg(Map<Integer, String> map) {
        this.map = map;
    }

    public Map<Integer, String> getMap() {
        return map;
    }

    @Override
    public String toString() {
        return "MapTransferReplyMsg [map=" + map + "]";
    }
}