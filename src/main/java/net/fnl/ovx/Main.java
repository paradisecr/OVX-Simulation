package net.fnl.ovx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cr on 2017/3/8.
 */
public class Main {

    public static void main(String args[]) throws Exception {
//        if (null == args || args.length < 2) return;
//        String phyNetCfgPath = args[0];
//        String virNetCfgPath = args[1];
        String phyNetCfgPath = "D:\\workspace\\OVX\\src\\main\\resources\\phy.json";
        String virNetCfgPath = "D:\\workspace\\OVX\\src\\main\\resources\\req1.json";
        // 1.读取物理链路信息
        NetCfg phyNetCfg = readCfg(phyNetCfgPath);
        // 2.读取虚拟网络请求信息
        NetCfg virNetCfg = readCfg(virNetCfgPath);
        ResourceRequest resourceRequest = new ResourceRequest(virNetCfg);
        // 3.计算映射
        OVXApplication ovxApplication = new OVXApplication(phyNetCfg);
        long startTime=System.currentTimeMillis();
        ResourceAllocation resourceAllocation = ovxApplication.processResourceRequest(resourceRequest);
        long endTime=System.currentTimeMillis();
        System.out.println(resourceAllocation);
        System.out.println("Total Cost:" + resourceAllocation.getTotalCost());
        System.out.println("Run time：" + (endTime - startTime) + "ms");
    }


    public static NetCfg readCfg(String filePath) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        NetCfg netCfg = mapper.readValue(new File(filePath), NetCfg.class);
        return netCfg;
    }

    public static String readFile2String(String filePath) {

        StringBuilder stringBuilder = new StringBuilder();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String tmpStr;
            while((tmpStr = reader.readLine()) != null) {
                stringBuilder.append(tmpStr);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            System.out.println(e);
        }
        return null;
    }

    public static void main2(String args[]) {
        // 1.读取物理链路信息
        UndirectedGraph<String, DefaultEdge> phyGraph = new SimpleGraph(DefaultEdge.class);
        String s1 = "s1";
        String s2 = "s2";
        String s3 = "s3";
        String s4 = "s4";
        phyGraph.addVertex(s1);
        phyGraph.addVertex(s2);
        phyGraph.addVertex(s3);
        phyGraph.addVertex(s4);
        phyGraph.addEdge(s1, s2);
        phyGraph.addEdge(s1, s3);
        phyGraph.addEdge(s2, s4);
        phyGraph.addEdge(s3, s4);
        Map<String, Long> vCapabilityMap = new HashMap<String, Long>();
        Map<DefaultEdge, Long> eCapabilityMap = new HashMap<DefaultEdge, Long>();
        Map<String, Long> vCostMap = new HashMap<String, Long>();
        Map<DefaultEdge, Long> eCostMap = new HashMap<DefaultEdge, Long>();
        vCapabilityMap.put(s1, 10L);
        vCapabilityMap.put(s2, 10L);
        vCapabilityMap.put(s3, 10L);
        vCapabilityMap.put(s4, 10L);
        eCapabilityMap.put(phyGraph.getEdge(s1,s2), 1000L);
        eCapabilityMap.put(phyGraph.getEdge(s1,s3), 1000L);
        eCapabilityMap.put(phyGraph.getEdge(s2,s4), 1000L);
        eCapabilityMap.put(phyGraph.getEdge(s3,s4), 1000L);
        vCostMap.put(s1, 10L);
        vCostMap.put(s2, 10L);
        vCostMap.put(s3, 10L);
        vCostMap.put(s4, 10L);
        eCostMap.put(phyGraph.getEdge(s1,s2), 10L);
        eCostMap.put(phyGraph.getEdge(s1,s3), 1000L);
        eCostMap.put(phyGraph.getEdge(s2,s4), 10L);
        eCostMap.put(phyGraph.getEdge(s3,s4), 10L);

        // 2.读取虚拟网络请求信息
        UndirectedGraph<String, DefaultEdge> virtualGraph = new SimpleGraph(DefaultEdge.class);
        String v1 = "v1";
        String v2 = "v2";
        virtualGraph.addVertex(v1);
        virtualGraph.addVertex(v2);
        virtualGraph.addEdge(v1, v2);
        Map<String, Long> vCapabilityRequestMap = new HashMap<String, Long>();
        Map<DefaultEdge, Long> eCapabilityRequestMap = new HashMap<DefaultEdge, Long>();
        vCapabilityRequestMap.put(v1, 5L);
        vCapabilityRequestMap.put(v2, 5L);
        eCapabilityRequestMap.put(virtualGraph.getEdge(v1, v2), 100L);
        ResourceRequest resourceRequest = new ResourceRequest(virtualGraph, vCapabilityRequestMap, eCapabilityRequestMap);
        // 3.计算映射
        OVXApplication ovxApplication = new OVXApplication(phyGraph,vCapabilityMap, eCapabilityMap,
                vCostMap, eCostMap);
        ResourceAllocation resourceAllocation = ovxApplication.processResourceRequest(resourceRequest);
        System.out.println(resourceAllocation.isAllocationSuccess());
        System.out.println(resourceAllocation);
    }
}
