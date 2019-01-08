package com.popoaichuiniu.intentGen;

import com.popoaichuiniu.jacy.statistic.CGExporter;
import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.WriteFile;
import soot.Kind;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

import java.io.File;
import java.util.*;

public class MyCallGraph extends CallGraph {

    Set<SootMethod> allMethods = new HashSet<>();

    Set<Edge> allEdges = new HashSet<>();


    Map<SootMethod, Set<MyPairUnitToEdge>> targetUnitInSootMethod = new HashMap<SootMethod, Set<MyPairUnitToEdge>>();

    Map<EdgeToSootMethod, Set<MyPairUnitToEdge>> targetUnitInSootMethodOfThisEdgeCall = new HashMap<EdgeToSootMethod, Set<MyPairUnitToEdge>>();

    Map<SootMethod, Set<List<IntentConditionTransformSymbolicExcutation.TargetUnitInMethodInfo>>> identicalMyUnitGraphSetOfMethodMap = new HashMap<>();


    Map<SootMethod, Set<Edge>> inEdgesOfThisMethod = new HashMap<>();


    Map<SootMethod, Set<Edge>> outEdgesOfThisMethod = new HashMap<>();


    SootMethod targetSootMethod = null;

    Unit targetUnit = null;

    SootMethod dummyMainMethod = null;

    Unit dummyMainMethodUnit = null;

    Edge dummyMainMethodUnitOfEdge =null;


    static {

        File file = new File(Config.intentConditionSymbolicExcutationResults+"/" + "RepeatEdgeSituation.txt");
        if (file.exists()) {
            file.delete();
        }


    }


    private IntentConditionTransformSymbolicExcutation intentConditionTransformSymbolicExcutation = null;

    public MyCallGraph(Set<SootMethod> allMethodsInPathOfTarget, CallGraph cg, SootMethod targetSootMethod, Unit targetUnit, IntentConditionTransformSymbolicExcutation intentConditionTransformSymbolicExcutation) {

        //targetSootMethod中的targetUnit和entry method是连通的
        this.targetSootMethod = targetSootMethod;
        this.targetUnit = targetUnit;
        this.intentConditionTransformSymbolicExcutation = intentConditionTransformSymbolicExcutation;


        for (SootMethod sootMethod : allMethodsInPathOfTarget) {


            inEdgesOfThisMethod.put(sootMethod, new HashSet<>());
            outEdgesOfThisMethod.put(sootMethod, new HashSet<>());
            if (sootMethod.getBytecodeSignature().equals("<dummyMainClass: dummyMainMethod([Ljava/lang/String;)V>")) {
                dummyMainMethod = sootMethod;
                dummyMainMethodUnit = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(dummyMainMethod.makeRef()));
            }


        }

        MyPairUnitToEdge myPairUnitToEdge = new MyPairUnitToEdge(null, targetUnit);
        HashSet<MyPairUnitToEdge> hashSetTargetSootMethod = new HashSet<>();
        hashSetTargetSootMethod.add(myPairUnitToEdge);
        targetUnitInSootMethod.put(targetSootMethod, hashSetTargetSootMethod);//targetSootMethod拥有一个targetUnit

        constructMyCallGraph(allMethodsInPathOfTarget, cg, targetSootMethod, new HashSet<>());



        validateTargetUnitInSootMethod(targetUnitInSootMethod);
        validateCallGraph(targetSootMethod);


        long startTime=System.nanoTime();
        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("开始数据流分析");

        OverallIntentFlowAnalysis overallIntentFlowAnalysis = new OverallIntentFlowAnalysis(intentConditionTransformSymbolicExcutation.appPath,this, false);

        long endTime=System.nanoTime();

        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("数据流分析结束！"+(((double) (endTime - startTime)) / 1E9));


        analyseEverySootMethodToGetMyUnitGraph(intentConditionTransformSymbolicExcutation, overallIntentFlowAnalysis);


    }

    protected void constructTargetUnitInCallEdge() {




        Queue<EdgeToSootMethod> queue = new LinkedList<>();

        Map<EdgeToSootMethod,String> nodeColor=new HashMap<>();


        dummyMainMethodUnitOfEdge =new Edge(null,dummyMainMethodUnit,dummyMainMethod, Kind.ZMS_Set);

        EdgeToSootMethod rootEdgeToSootMethod=new EdgeToSootMethod(dummyMainMethodUnitOfEdge, dummyMainMethod);


        nodeColor.put(rootEdgeToSootMethod,"gray");

        queue.add(rootEdgeToSootMethod);



        while (!queue.isEmpty()) {

            EdgeToSootMethod first = queue.poll();


            SootMethod firstSootMethod = first.sootMethod;

            Set<MyPairUnitToEdge> myPairUnitToEdgeSet = targetUnitInSootMethod.get(firstSootMethod);

            if (myPairUnitToEdgeSet == null) {
                throw new RuntimeException("targetUnitInSootMethod构建出错" + intentConditionTransformSymbolicExcutation.appPath);
            }
            targetUnitInSootMethodOfThisEdgeCall.put(first, myPairUnitToEdgeSet);


            for (Edge oneEdge : outEdgesOfThisMethod.get(first.sootMethod)) {
                SootMethod oneSootMethod = oneEdge.tgt();
                EdgeToSootMethod oneEdgeToSootMethod = new EdgeToSootMethod(oneEdge, oneSootMethod);

                if(nodeColor.get(oneEdgeToSootMethod)==null)
                {
                    nodeColor.put(oneEdgeToSootMethod,"gray");
                    queue.offer(oneEdgeToSootMethod);
                }

            }


            nodeColor.put(first,"black");


        }


    }

    private void validateTargetUnitInSootMethod(Map<SootMethod, Set<MyPairUnitToEdge>> map) {
        for (Map.Entry<SootMethod, Set<MyPairUnitToEdge>> entry : map.entrySet()) {


            SootMethod sootMethod = entry.getKey();

            for (MyPairUnitToEdge myPairUnitToEdge : entry.getValue()) {
                if (sootMethod != targetSootMethod) {
                    if (myPairUnitToEdge.outEdge == null) {

                        throw new RuntimeException(sootMethod.getBytecodeSignature() + "\n" +
                                myPairUnitToEdge.srcUnit + "\n" +
                                "非目标方法myPairUnitToEdge.outEdge == null");
                    }
                }
            }
        }
    }

    private void constructMyCallGraph(Set<SootMethod> allMethodsInPathOfTarget, CallGraph cg, SootMethod targetSootMethod, HashSet<SootMethod> visited) {

        visited.add(targetSootMethod);
        for (Iterator<Edge> iteratorSootMethod = cg.edgesInto(targetSootMethod); iteratorSootMethod.hasNext(); ) {

            Edge edge = iteratorSootMethod.next();

            SootMethod sootMethodSrc = edge.getSrc().method();

            if (allMethodsInPathOfTarget.contains(sootMethodSrc)) {

                addEdge(edge);//允许环
                Set<MyPairUnitToEdge> targetMyPairUnitToEdges = targetUnitInSootMethod.get(sootMethodSrc);

                if (targetMyPairUnitToEdges == null) {
                    targetMyPairUnitToEdges = new HashSet<>();
                }
                //FINALIZE edge: null in <org.jivesoftware.smackx.muc.MultiUserChat: void <init>(org.jivesoftware.smack.Connection,java.lang.String)> ==> <org.jivesoftware.smackx.muc.MultiUserChat: void finalize()>


                targetMyPairUnitToEdges.add(new MyPairUnitToEdge(edge, edge.srcUnit()));//注意其srcUnit是可以为空的  这个条边是虚拟的


                targetUnitInSootMethod.put(sootMethodSrc, targetMyPairUnitToEdges);

                if (!visited.contains(sootMethodSrc)) {
                    constructMyCallGraph(allMethodsInPathOfTarget, cg, sootMethodSrc, visited);
                }

            }


        }


    }

    private void analyseEverySootMethodToGetMyUnitGraph(IntentConditionTransformSymbolicExcutation intentConditionTransformSymbolicExcutation, OverallIntentFlowAnalysis overallIntentFlowAnalysis) {


        for (Map.Entry<SootMethod, Set<MyPairUnitToEdge>> oneEntry : targetUnitInSootMethod.entrySet()) {


            SootMethod oneSootMethod = oneEntry.getKey();//


            Set<MyPairUnitToEdge> oneMyPairUnitToEdgeSet = oneEntry.getValue();


            for (MyPairUnitToEdge onePair : oneMyPairUnitToEdgeSet) {

                if (onePair.srcUnit == null)//如果有的边的srcUnit就不分析它.
                {
                    WriteFile writeFile = new WriteFile(Config.intentConditionSymbolicExcutationResults+"/"+"sootmethodEdgeNoSrcUnit.txt", true, intentConditionTransformSymbolicExcutation.exceptionLogger);
                    writeFile.writeStr(oneSootMethod + "&&" + new File(intentConditionTransformSymbolicExcutation.appPath).getName() + "\n");
                    writeFile.close();
                    continue;
                }


                boolean hasInfo = false;
                Map<Unit, IntentConditionTransformSymbolicExcutation.TargetUnitInMethodInfo> map = intentConditionTransformSymbolicExcutation.allSootMethodsAllUnitsTargetUnitInMethodInfo.get(oneSootMethod);
                if (map != null) {
                    IntentConditionTransformSymbolicExcutation.TargetUnitInMethodInfo targetUnitInMethodInfo = map.get(onePair.srcUnit);
                    if (targetUnitInMethodInfo != null) {
                        hasInfo = true;//已经分析过了，有信息了就不用再分析了。
                    }
                }

                if (!hasInfo) {

                    SingleSootMethodIntentFlowAnalysis singleSootMethodIntentFlowAnalysis = OverallIntentFlowAnalysis.localSingleSootMethodIntentFlowAnalysisMap.get(oneSootMethod);
                    if (singleSootMethodIntentFlowAnalysis == null) {
                        throw new RuntimeException("OverallIntentFlowAnalysis分析不完整！");
                    }
                    BriefUnitGraph ug = OverallIntentFlowAnalysis.sootMethodBriefUnitGraphMap.get(oneSootMethod);
                    if (ug == null) {
                        throw new RuntimeException("sootMethodBriefUnitGraphMap分析不完整！");
                    }
                    SimpleLocalDefs defs = intentConditionTransformSymbolicExcutation.sootMethodSimpleLocalDefsMap.get(oneSootMethod);

                    if (defs == null) {
                        defs = new SimpleLocalDefs(ug);
                        intentConditionTransformSymbolicExcutation.sootMethodSimpleLocalDefsMap.put(oneSootMethod, defs);
                    }

                    intentConditionTransformSymbolicExcutation.doAnalysisOnUnit(onePair, oneSootMethod, singleSootMethodIntentFlowAnalysis, defs, ug);

                }


            }


        }


    }

    public boolean judgeUnitPathHasCondition(IntentConditionTransformSymbolicExcutation.IntentSoln intentSoln) {

        if (!intentSoln.isFeasible) {
            return false;

        }
        Intent intent = intentSoln.intent;
        if (intent == null) {
            return false;
        }
        if ((intent.action != null) && (!intent.action.trim().equals(""))) {
            return true;
        }
        if (intent.myExtras != null && intent.myExtras.size() != 0) {
            return true;
        }
        if (intent.categories != null && intent.categories.size() != 0) {
            return true;
        }

        return false;

    }


    private void validateCallGraph(SootMethod targetSootMethod) {
        for (SootMethod sootMethod : allMethods) {
            Set<Edge> inEdges = inEdgesOfThisMethod.get(sootMethod);
            if (inEdges.size() == 0 && (!sootMethod.getBytecodeSignature().equals("<dummyMainClass: dummyMainMethod([Ljava/lang/String;)V>"))) {

                throw new RuntimeException(sootMethod.getBytecodeSignature() + "xxxxxxxx入度为0" + "\n"
                        + "validateCallGraph非主函数入度为0");
            }
            Set<Edge> outEdges = outEdgesOfThisMethod.get(sootMethod);

            if (outEdges.size() == 0 && sootMethod != targetSootMethod) {

                throw new RuntimeException(sootMethod.getBytecodeSignature() + "xxxxxxxx出度为0" + "\n"
                        + "validateCallGraph非targetSootMethod出度为0");
            }
        }
    }


    private Map<SootMethod, Set<MyPairUnitToEdge>> unitRemainInSootMethodMap = new HashMap<>();


    @Override
    public boolean addEdge(Edge e) {


        SootMethod src = e.src();
        SootMethod tgt = e.tgt();

        Set<Edge> inEdgesOfSootMethod = inEdgesOfThisMethod.get(tgt);
        if (inEdgesOfSootMethod == null) {
            inEdgesOfSootMethod = new HashSet<>();
        }


        boolean flag1 = inEdgesOfSootMethod.add(e);

        inEdgesOfThisMethod.put(tgt, inEdgesOfSootMethod);

        Set<Edge> outEdgesOfSootMethod = outEdgesOfThisMethod.get(src);
        if (outEdgesOfSootMethod == null) {
            outEdgesOfSootMethod = new HashSet<>();
        }

        boolean flag2 = outEdgesOfSootMethod.add(e);

        outEdgesOfThisMethod.put(src, outEdgesOfSootMethod);

        boolean flag = flag1 && flag2;
        allEdges.add(e);
        allMethods.add(src);
        allMethods.add(tgt);

        if (!flag) {
            intentConditionTransformSymbolicExcutation.exceptionLogger.error("存在相同边！");
        }


        return flag;


    }

    @Override
    public boolean removeEdge(Edge e) {

        SootMethod src = e.src();
        SootMethod tgt = e.tgt();
        Set<Edge> outEdges = outEdgesOfThisMethod.get(src);
        boolean flag1 = true;
        if (outEdges != null) {
            flag1 = outEdges.remove(e);
        } else {
            flag1 = false;
        }

        Set<Edge> inEdges = inEdgesOfThisMethod.get(tgt);
        boolean flag2 = true;
        if (inEdges != null) {
            flag2 = inEdges.remove(e);
        } else {

            flag2 = false;
        }

        boolean flag = flag1 && flag2;


        allEdges.remove(e);


        return flag;


    }


    @Override
    public Iterator<Edge> edgesOutOf(MethodOrMethodContext m) {

        Set<Edge> outEdges = outEdgesOfThisMethod.get(m.method());
        if (outEdges == null) {
            return null;
        }

        return outEdges.iterator();

    }

    @Override
    public Iterator<Edge> edgesInto(MethodOrMethodContext m) {
        Set<Edge> inEdges = inEdgesOfThisMethod.get(m.method());
        if (inEdges == null) {
            return null;
        } else {
            return inEdges.iterator();
        }
    }

    public void exportGexf(String fileName) {

        CGExporter cgExporter = new CGExporter();

        for (SootMethod sootMethod : allMethods) {
            cgExporter.createNode(sootMethod.getBytecodeSignature());
        }

        for (Edge edge : allEdges) {
            it.uniroma1.dis.wsngroup.gexf4j.core.Node node1 = cgExporter.createNode(edge.src().getBytecodeSignature());
            it.uniroma1.dis.wsngroup.gexf4j.core.Node node2 = cgExporter.createNode(edge.tgt().getBytecodeSignature());
            cgExporter.linkNodeByID(edge.src().getBytecodeSignature(), edge.tgt().getBytecodeSignature());

        }

        cgExporter.exportMIG(fileName, Config.intentConditionSymbolicExcutationResults);
    }

}

class MyPairUnitToEdge {//edge 相同保证：srcUnit,src,tgt,kind都相同 //srcUnit可能为空。一个方法里可能有多个edge的srcUnit为空空，因此使用edge作为key。

    Edge outEdge;//key  对于方法里有多个多个edge:srcUnit是null ,src,tgt,kind都相同，认为只有一条边(由于edge的equals定义)
    Unit srcUnit;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyPairUnitToEdge that = (MyPairUnitToEdge) o;
        return Objects.equals(outEdge, that.outEdge);
    }

    @Override
    public int hashCode() {

        return Objects.hash(outEdge);
    }

    public MyPairUnitToEdge(Edge outEdge, Unit srcUnit) {
        this.outEdge = outEdge;
        this.srcUnit = srcUnit;
    }
}
