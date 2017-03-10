package net.fnl.ovx;

import java.util.List;

/**
 * Created by cr on 2017/3/9.
 */
public class NetCfg {
    private List<NodeCfg> nodes;
    private List<LinkCfg> links;

    public List<NodeCfg> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeCfg> nodes) {
        this.nodes = nodes;
    }

    public List<LinkCfg> getLinks() {
        return links;
    }

    public void setLinks(List<LinkCfg> links) {
        this.links = links;
    }

    @Override
    public java.lang.String toString() {
        return "NetCfg{" +
                "nodes=" + nodes +
                ", links=" + links +
                '}';
    }
}
