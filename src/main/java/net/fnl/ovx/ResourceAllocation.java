package net.fnl.ovx;

import org.jgrapht.GraphPath;

import java.util.Map;

/**
 * Created by cr on 2017/3/8.
 */
public class ResourceAllocation {
    ResourceRequest resourceRequest;
    Map<String, String> vAllocationMap;
    Map<Edge, GraphPath<String, Edge>> eAllocationMap;
    long totalCost = 0L;
    boolean isAllocationSuccess;

    public ResourceAllocation(ResourceRequest resourceRequest, Map<String, String> vAllocationMap, Map<Edge, GraphPath<String, Edge>> eAllocationMap, boolean isAllocationSuccess) {
        this.resourceRequest = resourceRequest;
        this.vAllocationMap = vAllocationMap;
        this.eAllocationMap = eAllocationMap;
        this.isAllocationSuccess = isAllocationSuccess;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }

    public boolean isAllocationSuccess() {
        return isAllocationSuccess;
    }

    @Override
    public String toString() {
        return "ResourceAllocation{\r\n" +
                "vAllocationMap=" + vAllocationMap +
                ",\r\neAllocationMap=" + eAllocationMap +
                ",\r\nisAllocationSuccess=" + isAllocationSuccess +
                '}';
    }
}
