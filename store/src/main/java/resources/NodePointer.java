package resources;

import java.math.BigInteger;

public class NodePointer {
    
    private String address;
    private BigInteger id;
    
    public NodePointer(String address, BigInteger id) {        
        this.address = address;
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "NodePointer [identifier=" + id + "address=" + address + "]";
    }

}