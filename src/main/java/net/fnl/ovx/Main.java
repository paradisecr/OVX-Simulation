package net.fnl.ovx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by cr on 2017/3/8.
 */
public class Main {

    public static void main(String args[]) throws Exception {
        // 输出提示
        System.out.print("Please input physical topology param description file path(e.g, ~/params/phy.json): ");
        String phyNetCfgPath = readInputString();
//        String phyNetCfgPath = "D:\\workspace\\OVX\\src\\main\\resources\\phy.json";
//        String virNetCfgPath = "D:\\workspace\\OVX\\src\\main\\resources\\req1.json";
        // 1.读取物理链路信息
        NetCfg phyNetCfg = readCfg(phyNetCfgPath);
        OVXApplication ovxApplication = new OVXApplication(phyNetCfg);
        System.out.println("**************************************************");
        while(true) {
            System.out.print("Please input resource request param description file path:");
            // 2.读取虚拟网络请求信息
            String virNetCfgPath = readInputString();
            NetCfg virNetCfg = readCfg(virNetCfgPath);
            ResourceRequest resourceRequest = new ResourceRequest(virNetCfg);
            // 3.计算映射
            long startTime=System.currentTimeMillis();
            ResourceAllocation resourceAllocation = ovxApplication.processResourceRequest(resourceRequest);
            long endTime=System.currentTimeMillis();
            System.out.println("Request:");
            System.out.println(resourceRequest.getvCapabilityRequestMap());
            System.out.println(resourceRequest.geteCapabilityRequestMap());
            System.out.println(resourceAllocation);
            System.out.println("Total Cost:" + resourceAllocation.getTotalCost());
            System.out.println("Run time：" + (endTime - startTime) + "ms");
            System.out.println("*******");
            System.out.println("Remain Resource:");
            System.out.println("Nodes:" + ovxApplication.getvAvailableResourceMap().toString());
            System.out.println("Links:" + ovxApplication.geteAvailableResourceMap().toString());
            System.out.println("**************************************************");
        }

    }

    public static String readInputString() {
        Scanner scanner = new Scanner(System.in);
        String inpuStr = scanner.nextLine().trim();
        return inpuStr;
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
        UndirectedGraph<String, Edge> phyGraph = new SimpleGraph(Edge.class);
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
        Map<Edge, Long> eCapabilityMap = new HashMap<Edge, Long>();
        Map<String, Long> vCostMap = new HashMap<String, Long>();
        Map<Edge, Long> eCostMap = new HashMap<Edge, Long>();
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
        UndirectedGraph<String, Edge> virtualGraph = new SimpleGraph(Edge.class);
        String v1 = "v1";
        String v2 = "v2";
        virtualGraph.addVertex(v1);
        virtualGraph.addVertex(v2);
        virtualGraph.addEdge(v1, v2);
        Map<String, Long> vCapabilityRequestMap = new HashMap<String, Long>();
        Map<Edge, Long> eCapabilityRequestMap = new HashMap<Edge, Long>();
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
