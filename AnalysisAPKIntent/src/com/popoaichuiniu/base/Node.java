package com.popoaichuiniu.base;


import soot.Unit;

import java.util.ArrayList;
import java.util.List;

public class Node {


    private Unit unit=null;


    private List<Node> children=null;

    private List<Node> parents=null;



    public Node(Unit unit)
    {
        this.unit=unit;
        children=new ArrayList<>();
        parents=new ArrayList<>();
    }

    public List<Node> getChildren() {
        return children;
    }

    public List<Node> getParents() {
        return parents;
    }

    @Override
    public int hashCode() {
        return unit.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj)
        {
            return true;
        }
        if(obj==null)
        {
            return false;
        }
        if(obj instanceof Node)
        {
            Node nodeObj= (Node) obj;

            return nodeObj.unit.equals(this.unit);

        }

        return false;

    }

    public Unit getUnit() {
        return unit;
    }
}
