package net.fnl.ovx;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

/**
 * Created by cr on 2017/3/8.
 */
public class OVXApplication {
    /**
     * 物理网络信息：点集合、链路集合、
     *     点计算能力、点剩余可分配能力
     *     链路最大可分配带宽、链路剩余可分配带宽
     */
    private UndirectedGraph physicalGraph;
    private  Map<String, Long> vCapabilityMap;
    private Map<Edge, Long> eCapabilityMap;
    private Map<String, Long> vAvailableResourceMap;
    private Map<Edge, Long> eAvailableResourceMap;
    private Map<String, Long> vCostMap;
    private Map<Edge, Long> eCostMap;
    private Map<String, String> vAllocationMap = new HashMap<String, String>();
    private Map<Edge, GraphPath<String, Edge>> eAllocationMap = new HashMap<Edge, GraphPath<String, Edge>>();

    /**
     * 虚拟网络信息：Map(虚拟网络)--->映射详情
     */
    Map<UndirectedGraph, ResourceRequest> resourceAllocationMap = new HashMap<UndirectedGraph, ResourceRequest>();

    public OVXApplication(NetCfg netCfg) {
        this.physicalGraph = new SimpleGraph(Edge.class);
        this.vCapabilityMap = new HashMap<String, Long>();
        this.eCapabilityMap = new HashMap<Edge, Long>();
        this.vCostMap = new HashMap<String, Long>();
        this.eCostMap = new HashMap<Edge, Long>();
        // vertex
        List<NodeCfg> nodes = netCfg.getNodes();
        for (NodeCfg node : nodes) {
            this.physicalGraph.addVertex(node.getName());
            this.vCapabilityMap.put(node.getName(), node.getResource());
            this.vCostMap.put(node.getName(), node.getCost());
        }
        // links
        List<LinkCfg> links = netCfg.getLinks();
        for (LinkCfg link : links) {
            this.physicalGraph.addEdge(link.getSrc(), link.getDst());
            Edge edge = (Edge) this.physicalGraph.getEdge(link.getSrc(), link.getDst());
            this.eCapabilityMap.put(edge, link.getResource());
            this.eCostMap.put(edge, link.getCost());
        }
        this.vAvailableResourceMap = cloneResource(vCapabilityMap);
        this.eAvailableResourceMap = cloneResource(eCapabilityMap);
    }

    public OVXApplication(UndirectedGraph physicalGraph, Map<String, Long> vCapabilityMap, Map<Edge, Long> eCapabilityMap, Map<String, Long> vCostMap, Map<Edge, Long> eCostMap) {
        this.physicalGraph = physicalGraph;
        this.vCapabilityMap = vCapabilityMap;
        this.eCapabilityMap = eCapabilityMap;
        this.vCostMap = vCostMap;
        this.eCostMap = eCostMap;
        this.vAvailableResourceMap = cloneResource(vCapabilityMap);
        this.eAvailableResourceMap = cloneResource(eCapabilityMap);
    }

    public ResourceAllocation processResourceRequest(ResourceRequest resourceRequest) {
        ResourceAllocator resourceAllocator = new ResourceAllocator(resourceRequest);
        resourceAllocator.allocate();
        return resourceAllocator.getResourceAllocation();
    }

    public Map<String, Long> getvAvailableResourceMap() {
        return vAvailableResourceMap;
    }

    public Map<Edge, Long> geteAvailableResourceMap() {
        return eAvailableResourceMap;
    }

    public long getvertexCapability(String vertex) {
        return vCapabilityMap.get(vertex) * vCostMap.get(vertex);
    }

    public <T> Map<T, Long> cloneResource(Map<T,Long> srcMap) {
        Map<T, Long> dstMap = new HashMap<T, Long>();
        for (T key : srcMap.keySet()) {
            dstMap.put(key, Long.valueOf(srcMap.get(key)));
        }
        return dstMap;
    }

    /**
     * 资源分配器，用于分配资源
     */
    class ResourceAllocator {
        ResourceRequest resourceRequest;
        ResourceAllocation resourceAllocation;

        UndirectedGraph<String, Edge> virtualGraph;
        /**
         * 未映射点集合
         * 已映射点集合
         * 当前所处理节点的所有可映射集合
         *
         */
        Set<String> unhandledVirtualVertexSet;
        Set<String> handledVirtualVertexSet;
//
//        Map<String, String> vAllocationMap;
//        Map<Edge, GraphPath<String, Edge>> eAllocationMap;

        private Map<String, Long> vAvailableResourceMapCache;
        private Map<Edge, Long> eAvailableResourceMapCache;

        Map<String, String> vAllocationMapCache;
        Map<Edge, GraphPath<String, Edge>> eAllocationMapCache;

        boolean isAllocateSuccess = false;

        public ResourceAllocator(ResourceRequest resourceRequest) {
            this.resourceRequest = resourceRequest;
            this.handledVirtualVertexSet = new HashSet<String>();
            this.virtualGraph = resourceRequest.getVirtualGraph();
            this.unhandledVirtualVertexSet = Sets.newHashSet(this.virtualGraph.vertexSet());
            this.vAvailableResourceMapCache = cloneResource(vAvailableResourceMap);
            this.eAvailableResourceMapCache = cloneResource(eAvailableResourceMap);
            this.vAllocationMapCache = Maps.newHashMap();
            this.eAllocationMapCache = Maps.newHashMap();
        }

        /**
         * ！！！！！算法核心处理函数！！！！
         * @return
         */
        public boolean allocate(){
            while (!unhandledVirtualVertexSet.isEmpty() && (isAllocateSuccess = allocateNextVirtualVertex())) {
            }
            // 映射失败的情况
            if (!isAllocateSuccess) {
                resourceAllocation = new ResourceAllocation(resourceRequest, new HashMap<String, String>(), new HashMap<Edge, GraphPath<String, Edge>>(), isAllocateSuccess);
                return isAllocateSuccess;
            }
            /** 映射成功的情况
             *  1. 记录映射
             *  2. 扣减资源
             *  3. 成本计算
             */
            vAllocationMap.putAll(vAllocationMapCache);
            eAllocationMap.putAll(eAllocationMapCache);
            resourceAllocation = new ResourceAllocation(resourceRequest, vAllocationMap, eAllocationMap, isAllocateSuccess);
            long totalCost = 0L;
            for (String virtualVertex : vAllocationMap.keySet()) {
                long request = resourceRequest.getVertexRequest(virtualVertex);
                long left = vAvailableResourceMap.get(vAllocationMap.get(virtualVertex));
                left -= request;
                totalCost += request * vCostMap.get(vAllocationMap.get(virtualVertex));
                vAvailableResourceMap.put(vAllocationMap.get(virtualVertex), left);
            }
            for (Edge virtualEdge : eAllocationMap.keySet()) {
                  long request = resourceRequest.getEdgeRequest(virtualEdge);
                List<Edge> phyEdgeList = eAllocationMap.get(virtualEdge).getEdgeList();
                for (Edge physicalEdge : phyEdgeList) {
                    long left = eAvailableResourceMap.get(physicalEdge);
                    left -= request;
                    eAvailableResourceMap.put(physicalEdge, left);
                    totalCost += request * eCostMap.get(physicalEdge);
                }
            }
            resourceAllocation.setTotalCost(totalCost);
            return isAllocateSuccess;
        }

        public ResourceAllocation getResourceAllocation() {
            return resourceAllocation;
        }

        private boolean allocateNextVirtualVertex() {
            // 1. 取一个未映射的虚拟节点
            String vVertex = getRelatedUnhandledvertex();
            //  取所有相关虚拟链路：
            List<Edge> relatedVirtualEdgeList = getRelatedVirtualEdge(vVertex);
            // 2. 取待处理虚拟节点所有可映射的物理节点集合
            List<String> availablePhysicalvertexList = getAvailablePhysicalvertexList(resourceRequest.getVertexRequest(vVertex));
            // 判断可用物理节点是否为空
            if (availablePhysicalvertexList.isEmpty()) {
                return false;
            }
            //  2.1 对每个物理节点做如下处理，选出符合映射条件的物理节点
            boolean findAvailableVertex = false;
            String allocatedPhysicalVertex = null;
            Map<Edge, GraphPath<String, Edge>> allocatedPhysicalPathMap = null;
            // check是否有符合需求的物理节点
            // 存储所有物理节点结果
            Map<String, Map<Edge, GraphPath<String, Edge>>> nodesResult = new HashMap<String, Map<Edge, GraphPath<String, Edge>>>();
            Map<String, Long> resultCosts = new HashMap<String, Long>();
            for (String physicalvertex : availablePhysicalvertexList) {
                Map<Edge, GraphPath<String, Edge>> edgeMap = new HashMap<Edge, GraphPath<String, Edge>>();
                boolean isPhysicalVertexAvailable = false;
                final Map<String, Long> vAvailableResourceTmp = cloneResource(vAvailableResourceMapCache);
                final Map<Edge, Long> eAvailableResourceTmp = cloneResource(eAvailableResourceMapCache);
                //  对每个虚拟链路尝试最短路映射
                for (final Edge virtualEdge : relatedVirtualEdgeList) {
                    String virtualSrcVertex = virtualGraph.getEdgeSource(virtualEdge);
                    String virtualDstVertex = virtualGraph.getEdgeTarget(virtualEdge);
                    String phySrcVertex = null;;
                    String phyDstVertex = null;
                    if((phyDstVertex = vAllocationMapCache.get(virtualDstVertex))!= null) {
                        phySrcVertex = physicalvertex;
                    } else {
                        phySrcVertex =  vAllocationMapCache.get(virtualSrcVertex);
                        phyDstVertex = physicalvertex;
                    }
                    KShortestPaths<String, Edge> pathService = new KShortestPaths<String, Edge>(physicalGraph, 4);
                    List<GraphPath<String, Edge>> pathList = pathService.getPaths(phySrcVertex, phyDstVertex);
                    Collection<GraphPath<String, Edge>> filteredPathList = Collections2.filter(pathList, new Predicate<GraphPath<String, Edge>>() {
                        public boolean apply(GraphPath<String, Edge> stringEdgeGraphPath) {
                            List<Edge> edgeList = stringEdgeGraphPath.getEdgeList();
                            for (Edge edge : edgeList) {
                                if (eAvailableResourceTmp.get(edge) < resourceRequest.getEdgeRequest(virtualEdge)) return false;
                            }
                            return true;
                        }
                    });
                    pathList = new ArrayList<GraphPath<String, Edge>>(filteredPathList);
                    Collections.sort(pathList,new Comparator<GraphPath<String, Edge>>() {
                        public int compare(GraphPath<String, Edge> o1, GraphPath<String, Edge> o2) {
                            return o1.getEdgeList().size() - o2.getEdgeList().size();
                        }
                    });
                    if (!pathList.isEmpty()) {
                        edgeMap.put(virtualEdge, pathList.iterator().next());
                        isPhysicalVertexAvailable = true;
                    } else {
                        isPhysicalVertexAvailable = false;
                        continue;
                    }
                    // 扣减临时路径上链路资源
                    GraphPath<String, Edge> path = pathList.iterator().next();
                    long request = resourceRequest.getEdgeRequest(virtualEdge);
                    for (Edge edge : path.getEdgeList()) {
                        long remain = eAvailableResourceTmp.get(edge);
                        remain -= request;
                        eAvailableResourceTmp.put(edge, remain);
                    }
                }
                if (isPhysicalVertexAvailable || relatedVirtualEdgeList.isEmpty()) {
                    findAvailableVertex = true;
                    nodesResult.put(physicalvertex, edgeMap);
                    allocatedPhysicalVertex = physicalvertex;
                    long cost = caculateCost(physicalvertex, resourceRequest.getVertexRequest(vVertex),
                            edgeMap);
                    resultCosts.put(physicalvertex, cost);
                }
            }
            if (findAvailableVertex) {
                // noderesult排序，选出最优物理节点
                String bestPhyVertex = resultCosts.keySet().iterator().next();
                Long minCost = resultCosts.get(bestPhyVertex);
                for (String vertex : resultCosts.keySet()) {
                    if (resultCosts.get(vertex) < minCost) bestPhyVertex = vertex;
                }

                vAllocationMapCache.put(vVertex, bestPhyVertex);
                eAllocationMapCache.putAll(nodesResult.get(bestPhyVertex));
                unhandledVirtualVertexSet.remove(vVertex);
                handledVirtualVertexSet.add(vVertex);
                //扣除cache中的资源
                Map<Edge, GraphPath<String, Edge>> bestPathMap = nodesResult.get(bestPhyVertex);;
                for (Edge vEdge : bestPathMap.keySet()) {
                    GraphPath<String, Edge> path = bestPathMap.get(vEdge);
                    long request = resourceRequest.getEdgeRequest(vEdge);
                    for (Edge edge : path.getEdgeList()) {
                        long remain = eAvailableResourceMapCache.get(edge);
                        remain -= request;
                        eAvailableResourceMapCache.put(edge, remain);
                    }
                }
                long request = resourceRequest.getVertexRequest(vVertex);
                long remain = vAvailableResourceMapCache.get(bestPhyVertex);
                remain -= request;
                vAvailableResourceMapCache.put(bestPhyVertex, remain);
                return true;
            }
            return false;
        }


        private long caculateCost(String phyVertex, long vRequest, Map<Edge, GraphPath<String, Edge>> edgeMap) {
            long result = vCostMap.get(phyVertex) * vRequest;
            for (Edge vEdge : edgeMap.keySet()) {
                GraphPath<String, Edge> path = edgeMap.get(vEdge);
                for (Edge edge : path.getEdgeList()){
                    result += eCostMap.get(edge) * resourceRequest.getEdgeRequest(vEdge);
                }
            }
            return result;
        }

        private long caculatePathCost(GraphPath<String, Edge> path, Edge vEdge) {
            long result = 0L;
            for (Edge edge : path.getEdgeList()){
                result += eCostMap.get(edge) * resourceRequest.getEdgeRequest(vEdge);
            }
            return result;
        }
        /**
         * 取所有与已映射的虚拟节点关联的虚拟链路
         * @param vertex
         * @return
         */
        private List<Edge> getRelatedVirtualEdge(final String vertex) {
            Collection<Edge> collection = Collections2.filter(virtualGraph.edgesOf(vertex), new Predicate<Edge>() {
                public boolean apply(Edge defaultEdge) {
                    return handledVirtualVertexSet.contains(virtualGraph.getEdgeTarget(defaultEdge)) || handledVirtualVertexSet.contains(virtualGraph.getEdgeSource(defaultEdge));
                }
            });
            return new ArrayList<Edge>(collection);
        }

        /**
         * 获取所有可用的物理节点，按资源匹配筛选
         * @return
         */
        private List<String> getAvailablePhysicalvertexList(final long request) {
            Set<String> vertexSet = physicalGraph.vertexSet();
            Collection<String>  availablevertexs = Collections2.filter(vertexSet, new Predicate<String>() {
                public boolean apply(String vertex) {
                    return !vAllocationMapCache.values().contains(vertex) && request <= vAvailableResourceMap.get(vertex);
                }
            });
            List<String> availablevertexList = new ArrayList<String>(availablevertexs);
//            Collections.sort(availablevertexList, new Comparator<String>() {
//                public int compare(String v1, String v2) {
//                    // TODO : 计算成本
//                    long v1Capability = getvertexCapability(v1);
//                    long v2Capability = getvertexCapability(v2);
//                    if (v1Capability == v2Capability) return 0;
//                    return  v1Capability < v2Capability ? -1 : 1;
//                }
//            });
            return availablevertexList;
        }

        private String getRelatedUnhandledvertex() {
            if (handledVirtualVertexSet.isEmpty()) {
                return virtualGraph.vertexSet().iterator().next();
            }
            Set<String> temSet = new HashSet<String>();
            for (String vertex : handledVirtualVertexSet) {
                temSet.addAll(getRelatedVertexSet(virtualGraph, vertex));
            }
            Collection<String> relatedvertex = Collections2.filter(temSet, new Predicate<String>() {
                public boolean apply(String s) {
                    return !handledVirtualVertexSet.contains(s);
                }
            });
            if (!relatedvertex.isEmpty()) {
                return relatedvertex.iterator().next();
            }
            if (!unhandledVirtualVertexSet.isEmpty()) {
                return unhandledVirtualVertexSet.iterator().next();
            }
            return null;
        }

        private Set<String> getRelatedVertexSet(UndirectedGraph graph, String vertex) {
            Set<Edge> edgeSet =  graph.edgesOf(vertex);
            Set<String> relatedVertexSet = new HashSet<String>();
            for (Edge edge : edgeSet) {
                relatedVertexSet.add((String) graph.getEdgeTarget(edge));
            }
            return relatedVertexSet;
        }
    }
}
