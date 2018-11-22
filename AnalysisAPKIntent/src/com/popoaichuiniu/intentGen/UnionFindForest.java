package com.popoaichuiniu.intentGen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UnionFindForest<T>{

    public Map<T, unionFindNode> unionFindNodeMap =null;

    public static class unionFindNode<T> {
        int rank;
        T value;
        unionFindNode parent;
        Set<unionFindNode> children = new HashSet<>();

        public unionFindNode(int rank, T value) {
            this.rank = rank;
            this.value = value;
        }
    }


    public UnionFindForest(Set<T> valueSet) {
        this.unionFindNodeMap = make_set(valueSet);


    }

    private Map<T, unionFindNode> make_set(Set<T> valueSet) {
        Map<T, unionFindNode> map = new HashMap<>();
        for (T value : valueSet) {
            unionFindNode unionFindNode = new unionFindNode(0, value);
            unionFindNode.parent = unionFindNode;
            map.put(value, unionFindNode);
        }

        return map;
    }

    public static void unionTwoNode(unionFindNode a, unionFindNode b) {
        unionFindNode x = find_set(a);
        unionFindNode y = find_set(b);
        if (x == y) {
            return;
        }

        link(x, y);

    }

    public static unionFindNode find_set(unionFindNode x) {

        if (x != x.parent) {

            unionFindNode root = find_set(x.parent);
            x.parent.children.remove(x);
            x.parent = root;
            root.children.add(x);

        }
        return x.parent;
    }

    public static void link(unionFindNode x, unionFindNode y) {
        if (x.rank > y.rank) {
            y.parent = x;
            x.children.add(y);
        } else {
            x.parent = y;
            y.children.add(x);
            if (x.rank == y.rank) {
                y.rank = y.rank + 1;
            }
        }
    }

}