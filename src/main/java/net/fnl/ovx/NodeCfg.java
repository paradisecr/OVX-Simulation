package net.fnl.ovx;

/**
 * Created by cr on 2017/3/9.
 */
public class NodeCfg {
    private String name;
    private long resource;
    private long cost;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getResource() {
        return resource;
    }

    public void setResource(long resource) {
        this.resource = resource;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    @Override
    public java.lang.String toString() {
        return "NodeCfg{" +
                "name='" + name + '\'' +
                ", resource=" + resource +
                ", cost=" + cost +
                '}';
    }
}
