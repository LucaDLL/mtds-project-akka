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

    public Boolean isEmpty() {
        return (this.address == null) && (this.id == null);
    }

    @Override
    public String toString() {
        return "NodePointer [Identifier=" + id + ", Address=" + address + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        NodePointer np = (NodePointer) obj;
        return (this.address).equals(np.getAddress()) && (this.id).equals(np.getId());
    }

}