package com.popoaichuiniu.base;

import com.popoaichuiniu.jacy.statistic.CGExporter;
import soot.SootMethod;
import soot.Unit;

import java.util.*;

public class Graph {

    private Set<Node> nodes = null;
    private Set<Edge> edges = null;
    private Set<Unit> units = null;
    private Map<Unit,Node> unitNodeMap=null;

    private Node startNode = null;

    private SootMethod sootMethod = null;
    private Unit startUnit = null;

    public Set<Unit> allBranchUnitSet=null;

    public Graph(SootMethod sootMethod, Unit startUnit) {
        nodes = new HashSet<>();
        edges = new HashSet<>();
        units = new HashSet<>();
        unitNodeMap=new HashMap<>();
        this.startUnit = startUnit;
        this.sootMethod = sootMethod;

        this.startNode = new Node(startUnit);
        nodes.add(startNode);
        units.add(startUnit);
        unitNodeMap.put(startUnit,startNode);

        allBranchUnitSet=new HashSet<>();
    }

    public Map<Unit, Node> getUnitNodeMap() {
        return unitNodeMap;
    }

    public Node addNode(Node node) {


        if (nodes.contains(node)) {
            for (Node temp : nodes) {
                if (temp.equals(node)) {
                    return temp;
                }

            }
        } else {
            nodes.add(node);
            units.add(node.getUnit());
            unitNodeMap.put(node.getUnit(),node);
            return node;
        }

        return null;
    }

    public void addEdge(Node src, Node tgt) {


        Node nodeSrc = this.addNode(src);
        Node nodeTgt = this.addNode(tgt);
        if (nodeSrc == null || nodeTgt == null) {
            throw new RuntimeException("图的算法有误！");
        }

        nodeSrc.getChildren().add(nodeTgt);
        nodeTgt.getParents().add(nodeSrc);

        Edge edge = new Edge(nodeSrc, nodeTgt);

        edges.add(edge);


    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public SootMethod getSootMethod() {
        return sootMethod;
    }

    public Unit getStartUnit() {
        return startUnit;
    }

    public Set<Unit> getUnits() {
        return units;
    }

    public void exportGexf(String fileName) {

        CGExporter cgExporter = new CGExporter();

        for(Node node:nodes)
        {
            cgExporter.createNode(node.getUnit().toString());
        }

        for (Edge edge:edges) {
            it.uniroma1.dis.wsngroup.gexf4j.core.Node node1 = cgExporter.createNode(edge.src.getUnit().toString());
            it.uniroma1.dis.wsngroup.gexf4j.core.Node node2 = cgExporter.createNode(edge.target.getUnit().toString());
            cgExporter.linkNodeByID(edge.src.getUnit().toString(),edge.target.getUnit().toString());

        }

        cgExporter.exportMIG(fileName, "app_analysis_results/finalPathsGexf");
    }
}
