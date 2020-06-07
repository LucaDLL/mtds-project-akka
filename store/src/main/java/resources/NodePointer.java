package resources;

import static resources.Methods.*;

import akka.cluster.Member;

public class NodePointer implements Comparable<NodePointer> {
    
    private String address;
    private Integer id;
    
    public NodePointer(String address, Integer id) {
        this.address = address;
        this.id = id;
    }

    public NodePointer(Member member) {
        this.address =  GetMemberAddress(member, Consts.NODE_ACTOR_SUFFIX);
        this.id = Hash(GetMemberUniqueAddress(member));
    }

    public String getAddress() {
        return address;
    }

    public Integer getId() {
        return id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setId(Integer id) {
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
        return Integer.compareUnsigned(this.getId(), otherPointer.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((address == null) ? 0 : address.hashCode());
        result = prime * result + id;
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