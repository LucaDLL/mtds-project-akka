package resources;

import akka.cluster.Member;

import java.math.BigInteger;

import static resources.Methods.*;

public class NodePointer {
    
    private String address;
    private BigInteger id;
    
    public NodePointer(String address, BigInteger id) {        
        this.address = address;
        this.id = id;
    }

    public NodePointer(Member member) {
        this.address =  GetMemberAddress(member);
        this.id = Sha1(GetMemberUniqueAddress(member));
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