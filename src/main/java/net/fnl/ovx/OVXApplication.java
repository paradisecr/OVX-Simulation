package net.fnl.ovx;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultEdge;
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
    UndirectedGraph physicalGraph;
    Map<String, Long> vCapabilityMap;
    Map<DefaultEdge, Long> eCapabilityMap;
    Map<String, Long> vAvailableResourceMap;
    Map<DefaultEdge, Long> eAvailableResourceMap;
    Map<String, Long> vCostMap;
    Map<DefaultEdge, Long> eCostMap;
    Map<String, String> vAllocationMap = new HashMap<String, String>();
    Map<DefaultEdge, GraphPath<String, DefaultEdge>> eAllocationMap = new HashMap<DefaultEdge, GraphPath<String, DefaultEdge>>();

    /**
     * 虚拟网络信息：Map(虚拟网络)--->映射详情
     */
    Map<UndirectedGraph, ResourceRequest> resourceAllocationMap = new HashMap<UndirectedGraph, ResourceRequest>();

    public OVXApplication(NetCfg netCfg) {
        this.physicalGraph = new SimpleGraph(DefaultEdge.class);
        this.vCapabilityMap = new HashMap<String, Long>();
        this.eCapabilityMap = new HashMap<DefaultEdge, Long>();
        this.vCostMap = new HashMap<String, Long>();
        this.eCostMap = new HashMap<DefaultEdge, Long>();
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
            DefaultEdge edge = (DefaultEdge) this.physicalGraph.getEdge(link.getSrc(), link.getDst());
            this.eCapabilityMap.put(edge, link.getResource());
            this.eCostMap.put(edge, link.getCost());
        }
        this.vAvailableResourceMap = Maps.newHashMap(vCapabilityMap);
        this.eAvailableResourceMap = Maps.newHashMap(eCapabilityMap);
    }

    public OVXApplication(UndirectedGraph physicalGraph, Map<String, Long> vCapabilityMap, Map<DefaultEdge, Long> eCapabilityMap, Map<String, Long> vCostMap, Map<DefaultEdge, Long> eCostMap) {
        this.physicalGraph = physicalGraph;
        this.vCapabilityMap = vCapabilityMap;
        this.eCapabilityMap = eCapabilityMap;
        this.vCostMap = vCostMap;
        this.eCostMap = eCostMap;
        this.vAvailableResourceMap = Maps.newHashMap(vCapabilityMap);
        this.eAvailableResourceMap = Maps.newHashMap(eCapabilityMap);
    }

    public ResourceAllocation processResourceRequest(ResourceRequest resourceRequest) {
        ResourceAllocator resourceAllocator = new ResourceAllocator(resourceRequest);
        resourceAllocator.allocate();
        return resourceAllocator.getResourceAllocation();
    }

    public long getvertexCapability(String vertex) {
        return vCapabilityMap.get(vertex) * vCostMap.get(vertex);
    }

    /**
     * 资源分配器，用于分配资源
     */
    class ResourceAllocator {
        ResourceRequest resourceRequest;
        ResourceAllocation resourceAllocation;

        UndirectedGraph<String, DefaultEdge> virtualGraph;
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
//        Map<DefaultEdge, GraphPath<String, DefaultEdge>> eAllocationMap;

        Map<String, String> vAllocationMapCache;
        Map<DefaultEdge, GraphPath<String, DefaultEdge>> eAllocationMapCache;

        boolean isAllocateSuccess = false;

        public ResourceAllocator(ResourceRequest resourceRequest) {
            this.resourceRequest = resourceRequest;
            this.handledVirtualVertexSet = new HashSet<String>();
            this.virtualGraph = resourceRequest.getVirtualGraph();
            this.unhandledVirtualVertexSet = Sets.newHashSet(this.virtualGraph.vertexSet());
            this.vAllocationMapCache = Maps.newHashMap(vAllocationMap);
            this.eAllocationMapCache = Maps.newHashMap(eAllocationMap);
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
                resourceAllocation = new ResourceAllocation(resourceRequest, vAllocationMap, eAllocationMap, isAllocateSuccess);
                return isAllocateSuccess;
            }
            /** 映射成功的情况
             *  1. 记录映射
             *  2. 扣减资源
             *  3. 成本计算
             */
            vAllocationMap = vAllocationMapCache;
            eAllocationMap = eAllocationMapCache;
            resourceAllocation = new ResourceAllocation(resourceRequest, vAllocationMap, eAllocationMap, isAllocateSuccess);
            long totalCost = 0L;
            for (String virtualVertex : vAllocationMap.keySet()) {
                long request = resourceRequest.getVertexRequest(virtualVertex);
                long left = vAvailableResourceMap.get(vAllocationMap.get(virtualVertex));
                left -= request;
                totalCost += request * vCostMap.get(vAllocationMap.get(virtualVertex));
                vAvailableResourceMap.put(vAllocationMap.get(virtualVertex), left);
            }
            for (DefaultEdge virtualEdge : eAllocationMap.keySet()) {
                long request = resourceRequest.getEdgeRequest(virtualEdge);
                List<DefaultEdge> phyEdgeList = eAllocationMap.get(virtualEdge).getEdgeList();
                for (DefaultEdge physicalEdge : phyEdgeList) {
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
            List<DefaultEdge> relatedVirtualEdgeList = getRelatedVirtualEdge(vVertex);
            // 2. 取待处理虚拟节点所有可映射的物理节点集合
            List<String> availablePhysicalvertexList = getAvailablePhysicalvertexList(resourceRequest.getVertexRequest(vVertex));
            // 判断可用物理节点是否为空
            if (availablePhysicalvertexList.isEmpty()) {
                return false;
            }
            //  2.1 对每个物理节点做如下处理，选出符合映射条件的物理节点
            boolean findAvailableVertex = false;
            String allocatedPhysicalVertex = null;
            Map<DefaultEdge, GraphPath<String, DefaultEdge>> allocatedPhysicalPathMap = null;
            // check是否有符合需求的物理节点
            // 存储所有物理节点结果
            Map<String, Map<DefaultEdge, GraphPath<String, DefaultEdge>>> nodesResult = new HashMap<String, Map<DefaultEdge, GraphPath<String, DefaultEdge>>>();
            Map<String, Long> resultCosts = new HashMap<String, Long>();
            for (String physicalvertex : availablePhysicalvertexList) {
                Map<DefaultEdge, GraphPath<String, DefaultEdge>> edgeMap = new HashMap<DefaultEdge, GraphPath<String, DefaultEdge>>();
                boolean isPhysicalVertexAvailable = false;
                //  对每个虚拟链路尝试最短路映射
                for (final DefaultEdge virtualEdge : relatedVirtualEdgeList) {
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
                    KShortestPaths<String, DefaultEdge> pathService = new KShortestPaths<String, DefaultEdge>(physicalGraph, 4);
                    List<GraphPath<String, DefaultEdge>> pathList = pathService.getPaths(phySrcVertex, phyDstVertex);
                    Collection<GraphPath<String, DefaultEdge>> filteredPathList = Collections2.filter(pathList, new Predicate<GraphPath<String, DefaultEdge>>() {
                        public boolean apply(GraphPath<String, DefaultEdge> stringDefaultEdgeGraphPath) {
                            List<DefaultEdge> edgeList = stringDefaultEdgeGraphPath.getEdgeList();
                            for (DefaultEdge edge : edgeList) {
                                if (eCapabilityMap.get(edge) < resourceRequest.getEdgeRequest(virtualEdge)) return false;
                            }
                            return true;
                        }
                    });
                    pathList = new ArrayList<GraphPath<String, DefaultEdge>>(filteredPathList);
                    Collections.sort(pathList,new Comparator<GraphPath<String, DefaultEdge>>() {
                        public int compare(GraphPath<String, DefaultEdge> o1, GraphPath<String, DefaultEdge> o2) {
                            return o1.getEdgeList().size() - o2.getEdgeList().size();
                        }
                    });
                    if (!pathList.isEmpty()) {
                        edgeMap.put(virtualEdge, pathList.iterator().next());
                        isPhysicalVertexAvailable = true;
                    } else {
                        isPhysicalVertexAvailable = false;
                        break;
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
                return true;
            }
            return false;
        }


        private long caculateCost(String phyVertex, long vRequest, Map<DefaultEdge, GraphPath<String, DefaultEdge>> edgeMap) {
            long result = vCostMap.get(phyVertex) * vRequest;
            for (DefaultEdge vEdge : edgeMap.keySet()) {
                GraphPath<String, DefaultEdge> path = edgeMap.get(vEdge);
                for (DefaultEdge edge : path.getEdgeList()){
                    result += eCostMap.get(edge) * resourceRequest.getEdgeRequest(vEdge);
                }
            }
            return result;
        }
        /**
         * 取所有与已映射的虚拟节点关联的虚拟链路
         * @param vertex
         * @return
         */
        private List<DefaultEdge> getRelatedVirtualEdge(final String vertex) {
            Collection<DefaultEdge> collection = Collections2.filter(virtualGraph.edgesOf(vertex), new Predicate<DefaultEdge>() {
                public boolean apply(DefaultEdge defaultEdge) {
                    return handledVirtualVertexSet.contains(virtualGraph.getEdgeTarget(defaultEdge)) || handledVirtualVertexSet.contains(virtualGraph.getEdgeSource(defaultEdge));
                }
            });
            return new ArrayList<DefaultEdge>(collection);
        }

        /**
         * 获取所有可用的物理节点，按资源匹配筛选
         * @return
         */
        private List<String> getAvailablePhysicalvertexList(final long request) {
            Set<String> vertexSet = physicalGraph.vertexSet();
            Collection<String>  availablevertexs = Collections2.filter(vertexSet, new Predicate<String>() {
                public boolean apply(String vertex) {
                    return !vAllocationMapCache.values().contains(vertex) && request <= vCostMap.get(vertex);
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
            Set<DefaultEdge> edgeSet =  graph.edgesOf(vertex);
            Set<String> relatedvertexSet = new HashSet<String>();
            for (DefaultEdge edge : edgeSet) {
                relatedvertexSet.add((String) graph.getEdgeTarget(edge));
            }
            return relatedvertexSet;
        }
    }
}
