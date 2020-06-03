package messages;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;

public class FindSuccessorMsg implements JsonSerializable{

    private final BigInteger key;

    @JsonCreator
    public FindSuccessorMsg(BigInteger key) {
        this.key = key;
    }

    public BigInteger getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "FindSuccessorMsg [key=" + key + "]";
    }
}