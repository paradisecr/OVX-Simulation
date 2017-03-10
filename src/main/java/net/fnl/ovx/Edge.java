package net.fnl.ovx;

import org.jgrapht.graph.DefaultEdge;

/**
 * Created by cr on 2017/3/10.
 */
public class Edge  extends DefaultEdge {
    @Override
    public int hashCode() {
        Object src = getSource();
        Object tgt = getTarget();
        int result = src != null ? src.hashCode() : 0;
        result = 31 * result + (tgt != null ? tgt.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof  Edge)) {
            return false;
        }
        Edge anotherObj = (Edge) obj;
        return getSource().equals(anotherObj.getSource()) && getTarget().equals(anotherObj.getTarget());
    }
}
