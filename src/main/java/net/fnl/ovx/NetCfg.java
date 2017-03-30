package net.fnl.ovx;

import java.util.List;

/**
 * Created by cr on 2017/3/9.
 */
public class NetCfg {
    private List<NodeCfg> nodes;
    private List<LinkCfg> links;
    private String controllerAddress;
    private String net;
    private String netMask;
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<NodeCfg> getNodes() {
        return nodes;
    }

    public String getControllerAddress() {
        return controllerAddress;
    }

    public void setControllerAddress(String controllerAddress) {
        this.controllerAddress = controllerAddress;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getNetMask() {
        return netMask;
    }

    public void setNetMask(String netMask) {
        this.netMask = netMask;
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
