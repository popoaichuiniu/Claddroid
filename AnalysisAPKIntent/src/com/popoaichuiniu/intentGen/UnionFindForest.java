package com.popoaichuiniu.intentGen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UnionFindForest<T>{

    public Map<T, UnionFindNode> unionFindNodeMap =null;

    public static class UnionFindNode<T> {
        int rank;
        T value;
        UnionFindNode parent;
        Set<UnionFindNode> children = new HashSet<>();

        public UnionFindNode(int rank, T value) {
            this.rank = rank;
            this.value = value;
        }
    }


    public UnionFindForest(Set<T> valueSet) {
        this.unionFindNodeMap = make_set(valueSet);


    }

    private Map<T, UnionFindNode> make_set(Set<T> valueSet) {
        Map<T, UnionFindNode> map = new HashMap<>();
        for (T value : valueSet) {
            UnionFindNode unionFindNode = new UnionFindNode(0, value);
            unionFindNode.parent = unionFindNode;
            map.put(value, unionFindNode);
        }

        return map;
    }

    public static void UnionTwoNode(UnionFindNode a, UnionFindNode b) {
        UnionFindNode x = find_set(a);
        UnionFindNode y = find_set(b);
        if (x == y) {
            return;
        }

        link(x, y);

    }

    public static UnionFindNode find_set(UnionFindNode x) {

        if (x != x.parent) {

            UnionFindNode root = find_set(x.parent);
            x.parent.children.remove(x);
            x.parent = root;
            root.children.add(x);

        }
        return x.parent;
    }

    public static void link(UnionFindNode x, UnionFindNode y) {
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