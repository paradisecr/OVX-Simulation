package net.fnl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import net.fnl.ovx.LinkCfg;
import net.fnl.ovx.NetCfg;
import net.fnl.ovx.NodeCfg;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import java.util.*;

/**
 * Created by cr on 2017/3/7.
 */
public class GraphTest {
    @Test
    public void kPathTest() {
        final UndirectedGraph<java.lang.String, DefaultEdge> graph = new SimpleGraph(DefaultEdge.class);
        final java.lang.String v1 = "v1";
        final java.lang.String v2 = "v2";
        java.lang.String v3 = "v3";
        java.lang.String v4 = "v4";
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);
        graph.addEdge(v1, v2);
        graph.addEdge(v1, v3);
        graph.addEdge(v2, v4);
        graph.addEdge(v3, v4);
        KShortestPaths<java.lang.String, DefaultEdge> pathService = new KShortestPaths<java.lang.String, DefaultEdge>(graph, 4);
        List<GraphPath<java.lang.String, DefaultEdge>> pathList = pathService.getPaths(v1, v4);
        System.out.println(pathList.size());
        for (GraphPath<java.lang.String,DefaultEdge> path : pathList) {
            System.out.println(path);
        }
        Collection collection = Collections2.filter(graph.edgesOf(v1), new Predicate() {
            public boolean apply(Object o) {
                return graph.getEdge( v1, v2).equals(o);
            }
        });
        System.out.println(collection.size());
        System.out.println(collection);
    }

//    class UndirectedPaths<V,E> {
//        Graph<V,E> graph;
//
//        public UndirectedPaths(Graph<V,E> graph) {
//            this.graph = graph;
//            graph.getAllEdges()
//        }
//        List<GraphPath<V,E>> getPaths(V v1, V v2) {
//            List<GraphPath<V,E>> pathList = new ArrayList<GraphPath<V, E>>();
//
//            return null;
//        }
//    }

    @Test
    public void filterTest(){
        List<Integer> list = new ArrayList();
        list.add(3);
        list.add(2);
        list.add(1);
        list.add(4);
        Collection<Integer> collection = Collections2.filter(list, new Predicate<Integer>() {
            public boolean apply(Integer input) {
                return input > 1;
            }
        });
        System.out.println(collection);
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        System.out.println(list);
    }

    @Test
    public void bean2jsonTest() throws Exception{
        NodeCfg nodeCfg1 = new NodeCfg();
        NodeCfg nodeCfg2 = new NodeCfg();
        LinkCfg linkCfg = new LinkCfg();
        NetCfg netCfg = new NetCfg();
        nodeCfg1.setName("s1");
        nodeCfg1.setResource(10L);
        nodeCfg1.setCost(10L);
        nodeCfg2.setName("s2");
        nodeCfg2.setResource(10L);
        nodeCfg2.setCost(10L);
        linkCfg.setSrc(nodeCfg1.getName());
        linkCfg.setDst(nodeCfg2.getName());
        netCfg.setLinks(Arrays.asList(linkCfg));
        netCfg.setNodes(Arrays.asList(nodeCfg1,nodeCfg2));
        ObjectMapper mapper = new ObjectMapper();
        java.lang.String jsonStr = mapper.writeValueAsString(netCfg);
        System.out.println(jsonStr);
    }

    public void json2BeanTest() throws Exception {
        java.lang.String jsonStr = "{\"nodes\":[{\"name\":\"s1\",\"resource\":10,\"cost\":10},{\"name\":\"s2\",\"resource\":10,\"cost\":10}],\"links\":[{\"src\":{\"name\":\"s1\",\"resource\":10,\"cost\":10},\"dst\":{\"name\":\"s2\",\"resource\":10,\"cost\":10},\"resource\":0,\"cost\":0}]}";
        ObjectMapper mapper = new ObjectMapper();
        NetCfg netCfg = mapper.readValue(jsonStr, NetCfg.class);
        System.out.println(netCfg);
    }

    public void listSort() {
        List<String> strList = new ArrayList<String>();
//        strList.sort();
    }
}
