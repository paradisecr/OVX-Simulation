package net.fnl.ovx;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cr on 2017/3/8.
 * 代表一个虚网的映射情况
 */
public class ResourceRequest {
    /**
     * 属性：虚拟网络拓扑、节点需求、链路需求、
     *     节点映射、链路映射、映射是否成功
     */
    UndirectedGraph virtualGraph;
    Map<String, Long> vCapabilityRequestMap;
    Map<Edge, Long> eCapabilityRequestMap;

    public ResourceRequest(NetCfg netCfg) {
        this.virtualGraph = new SimpleGraph(Edge.class);
        this.vCapabilityRequestMap = new HashMap<String, Long>();
        this.eCapabilityRequestMap = new HashMap<Edge, Long>();
        // vertex
        List<NodeCfg> nodes = netCfg.getNodes();
        for (NodeCfg node : nodes) {
            this.virtualGraph.addVertex(node.getName());
            this.vCapabilityRequestMap.put(node.getName(), node.getResource());
        }
        // links
        List<LinkCfg> links = netCfg.getLinks();
        for (LinkCfg link : links) {
            this.virtualGraph.addEdge(link.getSrc(), link.getDst());
            Edge edge = (Edge) this.virtualGraph.getEdge(link.getSrc(), link.getDst());
            this.eCapabilityRequestMap.put(edge, link.getResource());
        }
    }

    public ResourceRequest(UndirectedGraph virtualGraph, Map<String, Long> vCapabilityRequestMap, Map<Edge, Long> eCapabilityRequestMap) {
        this.virtualGraph = virtualGraph;
        this.vCapabilityRequestMap = vCapabilityRequestMap;
        this.eCapabilityRequestMap = eCapabilityRequestMap;
    }

    public UndirectedGraph getVirtualGraph() {
        return virtualGraph;
    }

    public void setVirtualGraph(UndirectedGraph virtualGraph) {
        this.virtualGraph = virtualGraph;
    }

    public long getVertexRequest(String vertex) {
        return vCapabilityRequestMap.get(vertex);
    }

    public long getEdgeRequest(Edge edge) {
        return eCapabilityRequestMap.get(edge);
    }

    public Map<String, Long> getvCapabilityRequestMap() {
        return vCapabilityRequestMap;
    }

    public Map<Edge, Long> geteCapabilityRequestMap() {
        return eCapabilityRequestMap;
    }
}
