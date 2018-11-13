package com.popoaichuiniu.intentGen;

import com.popoaichuiniu.intentGen.MyCallGraph;
import soot.Pack;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

import java.util.*;

public class OverallIntentFlowAnalysis {//一个targetAPI就有一个MyCallgraph，就会有一个OverallIntentFlowAnalysis,但是其中存在相同计算的
    //


    private MyCallGraph myCallGraph = null;

    private boolean isOverallAnalysis = true;

    public static Map<Unit, SingleSootMethodIntentFlowAnalysis> unitSingleSootMethodIntentFlowAnalysisMap = new HashMap<>();//key:目标unit

    public static Map<SootMethod, BriefUnitGraph> sootMethodBriefUnitGraphMap = new HashMap<>();


    public OverallIntentFlowAnalysis(MyCallGraph myCallGraph, boolean isOverallAnalysis) {

        this.myCallGraph = myCallGraph;
        this.isOverallAnalysis = isOverallAnalysis;
        process();


    }

    private void process() {

        SootMethod rootSootMethod = myCallGraph.dummyMainMethod;

        Queue<UnitToSootMethod> queue = new LinkedList<>();


        BriefUnitGraph rootBriefUnitGraph = sootMethodBriefUnitGraphMap.get(rootSootMethod);
        if (rootBriefUnitGraph == null) {
            rootBriefUnitGraph = new BriefUnitGraph(rootSootMethod.getActiveBody());

            sootMethodBriefUnitGraphMap.put(rootSootMethod, rootBriefUnitGraph);
        }


        Set<Value> rootParameterDataSet = new HashSet<>();

        Unit rootUnit = myCallGraph.dummyMainMethodUnit;

        queue.add(new UnitToSootMethod(rootUnit, rootSootMethod));


        unitSingleSootMethodIntentFlowAnalysisMap.put(rootUnit, new SingleSootMethodIntentFlowAnalysis(rootBriefUnitGraph, rootParameterDataSet));


        Set<UnitToSootMethod> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            UnitToSootMethod first = queue.poll();

            visited.add(first);

            Map<Unit, Set<Value>> singleSootMethodIntentFlowAnalysis = unitSingleSootMethodIntentFlowAnalysisMap.get(first.unit).getUnitCallMethodIntentRelativeMap();


            for (Edge oneEdge : myCallGraph.outEdgesOfThisMethod.get(first.sootMethod)) {
                SootMethod oneSootMethod = oneEdge.tgt();


                BriefUnitGraph briefUnitGraph = sootMethodBriefUnitGraphMap.get(oneSootMethod);

                if (briefUnitGraph == null) {
                    briefUnitGraph = new BriefUnitGraph(oneSootMethod.getActiveBody());

                    sootMethodBriefUnitGraphMap.put(oneSootMethod, briefUnitGraph);
                }


                Set<Value> parameterDataSet = singleSootMethodIntentFlowAnalysis.get(oneEdge.srcUnit());

                if(parameterDataSet==null)
                {
                    throw  new RuntimeException("singleSootMethodIntentFlowAnalysis出现错误！");
                }

                if (!isOverallAnalysis) {
                    parameterDataSet = new HashSet<>();//如果不采取全局分析，则让parameterDataSet一直为空
                }

                SingleSootMethodIntentFlowAnalysis oneSootMethodIntentFlowAnalysis = new SingleSootMethodIntentFlowAnalysis(briefUnitGraph, parameterDataSet);

                unitSingleSootMethodIntentFlowAnalysisMap.put(oneEdge.srcUnit(), oneSootMethodIntentFlowAnalysis);

                UnitToSootMethod unitToSootMethod = new UnitToSootMethod(oneEdge.srcUnit(), oneSootMethod);

                if (!visited.contains(unitToSootMethod)) {
                    queue.add(unitToSootMethod);
                }


            }
        }

    }



}
class UnitToSootMethod {//unit调用的sootmethod
    Unit unit;
    SootMethod sootMethod;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitToSootMethod that = (UnitToSootMethod) o;
        return Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    public UnitToSootMethod(Unit unit, SootMethod sootMethod) {
        this.unit = unit;
        this.sootMethod = sootMethod;
    }
}
