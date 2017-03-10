package net.fnl.ovx;

/**
 * Created by cr on 2017/3/9.
 */
public class LinkCfg {
    private String src;
    private String dst;
    private long  resource;
    private long cost;


    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
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
        return "LinkCfg{" +
                "src=" + src +
                ", dst=" + dst +
                ", resource=" + resource +
                ", cost=" + cost +
                '}';
    }
}
