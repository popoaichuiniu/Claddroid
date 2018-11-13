package com.popoaichuiniu.base;



public class Edge {
    Node src;

    Node target;

    public Edge(Node src, Node target) {
        this.src = src;
        this.target = target;
    }

    public Node getSrc() {
        return src;
    }

    public void setSrc(Node src) {
        this.src = src;
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return this.src.equals(edge.src)&&this.target.equals(edge.target);
    }

    @Override
    public int hashCode() {

        return 2*src.hashCode()+target.hashCode();
    }
}
