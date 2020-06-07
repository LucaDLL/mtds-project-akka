package messages;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MapTransferMsg implements JsonSerializable {
    
    private final Integer id;

    @JsonCreator
	public MapTransferMsg(Integer id) {
		this.id = id;
	}

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "MapTransferMsg [id=" + id + "]";
    }
       
}