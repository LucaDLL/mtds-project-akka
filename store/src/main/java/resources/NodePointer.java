package resources;

import static resources.Methods.*;

import com.google.common.primitives.UnsignedInteger;

import akka.cluster.Member;

public class NodePointer implements Comparable<NodePointer> {
    /*
        Each NodePointer identifies a NodeActor.
        It contains the address of the actor, and its 32 bit ID.
    */

    private String address;
    private UnsignedInteger id;
    
    public NodePointer(String address, UnsignedInteger id) {
        this.address = address;
        this.id = id;
    }

    public NodePointer(Member member) {
        this.address = getMemberAddress(member, Consts.NODE_ACTOR_SUFFIX);
        this.id = hash(getMemberUniqueAddress(member));
    }

    public String getAddress() {
        return address;
    }

    public UnsignedInteger getId() {
        return id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setId(UnsignedInteger id) {
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
    public int compareTo(NodePointer otherPointer){
        return this.getId().compareTo(otherPointer.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((address == null) ? 0 : address.hashCode());
        result = prime * result + id.intValue();
        return result;
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