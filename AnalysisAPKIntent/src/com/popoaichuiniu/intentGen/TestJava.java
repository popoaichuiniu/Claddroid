package com.popoaichuiniu.intentGen;

import java.util.HashSet;
import java.util.Set;

public class TestJava {

    public static void main(String [] args)
    {
        Set<IntentExtraKey> intentExtraKeySet1=new HashSet<>();
        intentExtraKeySet1.add(new IntentExtraKey("xxx","int","5"));
        Set<IntentExtraKey> intentExtraKeySet2=new HashSet<>();
        intentExtraKeySet2.add(new IntentExtraKey("xxx","int","8"));

        Set<Set<IntentExtraKey>> hashSet=new HashSet<>();
        hashSet.add(intentExtraKeySet1);
        hashSet.add(intentExtraKeySet2);
        System.out.println(hashSet);

        Set<Integer> set1=new HashSet<>();
        set1.add(1);
        Set<Integer>  set2=new HashSet<>();
        set2.add(1);
        Set<Set<Integer>> xxx=new HashSet<>();
        xxx.add(set1);
        xxx.add(set2);
        System.out.println(set1.equals(set2));

    }
}
