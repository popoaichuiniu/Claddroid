package com.popoaichuiniu.intentGen;

import soot.SootMethod;

import soot.Unit;
import soot.Value;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;

import java.util.*;

//注意：OverallIntentFlowAnalysis static map 只会在一个apk个重用，所以换一个apk分析时，需要清除。否则outofmemory
public class OverallIntentFlowAnalysis {//一个targetAPI就有一个MyCallgraph，就会有一个OverallIntentFlowAnalysis,但是其中存在相同计算的
    //


    private MyCallGraph myCallGraph = null;

    private boolean isOverallAnalysis = false;

    private static  String appPath=null;


    //全局数据流分析使用
    public static Map<Edge, SingleSootMethodIntentFlowAnalysis> edgeSingleSootMethodIntentFlowAnalysisMap = new HashMap<>();//key: Edge 调用的sootMethod的SingleSootMethodIntentFlowAnalysis


    public static Map<SootMethod,SingleSootMethodIntentFlowAnalysis>  localSingleSootMethodIntentFlowAnalysisMap=new HashMap<>();

    public static Map<SootMethod, BriefUnitGraph> sootMethodBriefUnitGraphMap = new HashMap<>();


    public OverallIntentFlowAnalysis(String appPath,MyCallGraph myCallGraph, boolean isOverallAnalysis) {

        if(OverallIntentFlowAnalysis.appPath==null)
        {
            OverallIntentFlowAnalysis.appPath=appPath;
        }
        else
        {
            if(!OverallIntentFlowAnalysis.appPath.equals(appPath))//开始新的apk分析
            {
                OverallIntentFlowAnalysis.appPath=appPath;
                edgeSingleSootMethodIntentFlowAnalysisMap.clear();
                localSingleSootMethodIntentFlowAnalysisMap.clear();
                sootMethodBriefUnitGraphMap.clear();
            }

        }

        this.myCallGraph = myCallGraph;
        this.isOverallAnalysis = isOverallAnalysis;

        if(isOverallAnalysis)
        {   //没有使用,
            myCallGraph.constructTargetUnitInCallEdge();
            process();
        }
        else
        {
            for(Map.Entry<SootMethod, Set<MyPairUnitToEdge>> entry: myCallGraph.targetUnitInSootMethod.entrySet())

            {
                BriefUnitGraph briefUnitGraph = sootMethodBriefUnitGraphMap.get(entry.getKey());
                if (briefUnitGraph == null) {
                    briefUnitGraph = new BriefUnitGraph(entry.getKey().getActiveBody());

                    sootMethodBriefUnitGraphMap.put(entry.getKey(), briefUnitGraph);
                }
                SingleSootMethodIntentFlowAnalysis singleSootMethodIntentFlowAnalysis=new SingleSootMethodIntentFlowAnalysis(entry.getKey(),briefUnitGraph,null,isOverallAnalysis);

                localSingleSootMethodIntentFlowAnalysisMap.put(entry.getKey(),singleSootMethodIntentFlowAnalysis);

            }

        }



    }

    private void process() {

        SootMethod rootSootMethod = myCallGraph.dummyMainMethod;
        Edge rootEdge = myCallGraph.dummyMainMethodUnitOfEdge;
        EdgeToSootMethod rootEdgeToSootMethod=new EdgeToSootMethod(rootEdge, rootSootMethod);

        Queue<EdgeToSootMethod> queue = new LinkedList<>();
        Map<EdgeToSootMethod,String> nodeColor=new HashMap<>();


        nodeColor.put(rootEdgeToSootMethod,"gray");
        queue.add(rootEdgeToSootMethod);


        BriefUnitGraph rootBriefUnitGraph = sootMethodBriefUnitGraphMap.get(rootSootMethod);
        if (rootBriefUnitGraph == null) {
            rootBriefUnitGraph = new BriefUnitGraph(rootSootMethod.getActiveBody());
            sootMethodBriefUnitGraphMap.put(rootSootMethod, rootBriefUnitGraph);
        }



        Set<Value> rootParameterDataSet = new HashSet<>();
        edgeSingleSootMethodIntentFlowAnalysisMap.put(rootEdge, new SingleSootMethodIntentFlowAnalysis(rootSootMethod,rootBriefUnitGraph, rootParameterDataSet,isOverallAnalysis));




        while (!queue.isEmpty()) {
            EdgeToSootMethod first = queue.poll();



            Map<Unit, Set<Value>> singleSootMethodIntentFlowAnalysis = edgeSingleSootMethodIntentFlowAnalysisMap.get(first.edge).getUnitCallMethodIntentRelativeMap();


            for (Edge oneEdge : myCallGraph.outEdgesOfThisMethod.get(first.sootMethod)) {

                if(oneEdge.srcUnit()==null)
                {
                    continue;
                }

                SootMethod oneSootMethod = oneEdge.tgt();

                BriefUnitGraph briefUnitGraph = sootMethodBriefUnitGraphMap.get(oneSootMethod);

                if (briefUnitGraph == null) {
                    briefUnitGraph = new BriefUnitGraph(oneSootMethod.getActiveBody());

                    sootMethodBriefUnitGraphMap.put(oneSootMethod, briefUnitGraph);
                }


                Set<Value> parameterDataSet = singleSootMethodIntentFlowAnalysis.get(oneEdge.srcUnit());

                if (parameterDataSet == null) {
                    throw new RuntimeException("singleSootMethodIntentFlowAnalysis出现错误！"+oneEdge.srcUnit());
                }


                SingleSootMethodIntentFlowAnalysis oneSootMethodIntentFlowAnalysis = new SingleSootMethodIntentFlowAnalysis(oneSootMethod,briefUnitGraph, parameterDataSet,isOverallAnalysis);

                edgeSingleSootMethodIntentFlowAnalysisMap.put(oneEdge, oneSootMethodIntentFlowAnalysis);

                EdgeToSootMethod edgeToSootMethod = new EdgeToSootMethod(oneEdge, oneSootMethod);

                if (nodeColor.get(edgeToSootMethod)==null) {
                    nodeColor.put(edgeToSootMethod,"gray");
                    queue.add(edgeToSootMethod);
                }


            }

            nodeColor.put(first ,"black");
        }

    }


}

class EdgeToSootMethod {//edge 中的srcUnit调用了sootmethod
    Edge edge;
    SootMethod sootMethod;

    public EdgeToSootMethod(Edge edge, SootMethod sootMethod) {
        this.edge = edge;
        this.sootMethod = sootMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        EdgeToSootMethod that = (EdgeToSootMethod) o;
        return Objects.equals(edge, that.edge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge);
    }
}
