package messages;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LookupMsg implements JsonSerializable {

    private final BigInteger key;

    @JsonCreator
    public LookupMsg(BigInteger key) {
        this.key = key;
    }

    public BigInteger getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "LookupMsg [key=" + key + "]";
    }
    
}