package com.popoaichuiniu.intentGen;

import com.popoaichuiniu.base.ExitJStmt;
import com.popoaichuiniu.base.Graph;
import com.popoaichuiniu.base.Node;
import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.SimpleLocalDefs;

import java.io.File;
import java.util.*;

public class MyUnitGraph extends BriefUnitGraph {

    private Unit targetUnit = null;

    private SootMethod sootMethod = null;

    private String appPath = null;


    private BodyMapping bodyMapping=null;


    public BodyMapping getBodyMapping() {
        return bodyMapping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyUnitGraph units = (MyUnitGraph) o;
        return Objects.equals(targetUnit, units.targetUnit) &&
                Objects.equals(sootMethod, units.sootMethod) &&
                Objects.equals(appPath, units.appPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetUnit, sootMethod, appPath);
    }

    public Set<Unit> allUnitFromStartToTargetUnitInpath = null;//包括Intent不相关的


    public Set<Unit> allIntentUnitFromStartToTargetUnitInpath = null;//只包括Intent相关的

    private Logger logger=null;

    public MyUnitGraph(Body body, Unit targetUnit, String appPath,Logger logger,UnitGraph ug, SingleSootMethodIntentFlowAnalysis intentFlowAnalysis, SimpleLocalDefs defs) {
        super(body);

        this.targetUnit = targetUnit;
        this.sootMethod = body.getMethod();
        this.appPath = appPath;
        this.logger=logger;

        for (Unit unit : body.getUnits()) {
            if (unitToPreds.get(unit) == null) {
                unitToPreds.put(unit, new ArrayList<>());
            }
            if (unitToSuccs.get(unit) == null) {
                unitToSuccs.put(unit, new ArrayList<>());
            }
        }

        reducedCFG(ug,intentFlowAnalysis,defs);


    }

    private void reducedCFG(UnitGraph ug, SingleSootMethodIntentFlowAnalysis intentFlowAnalysis, SimpleLocalDefs defs) {


        JimpleBody jimpleBody = (JimpleBody) sootMethod.getActiveBody();


        bodyMapping=CloneJimpleBodyFactory.cloneJimpleBody(jimpleBody);



        Graph allUnitInPathGraphPrevious = new Graph(sootMethod, targetUnit);

        getAllBranchUnit(ug, new HashSet<>(jimpleBody.getUnits()), allUnitInPathGraphPrevious);

        // allUnitInPathGraphPrevious.exportGexf(sootMethod.getName() + "_previous");


        Set<Unit> needToRemove = new HashSet<>();//需要删除的语句

        Set<Unit> unitIntentRelativeAndStartStop = new HashSet<>();//和intent相关语句还有起点和终点

        findUnRelativeNode(targetUnit, sootMethod, needToRemove, intentFlowAnalysis, ug, unitIntentRelativeAndStartStop);//计算得到上面两个集合


        allUnitFromStartToTargetUnitInpath = new HashSet<>();
        findAllUnitInToTargetUnitPath(targetUnit, ug, allUnitFromStartToTargetUnitInpath);//计算得到allUnitFromStartToTargetUnitInpath集合


        Set<Unit> relativeUnitDefUnitSet = new HashSet<>(unitIntentRelativeAndStartStop);//复制unitIntentRelativeAndStartStop到relativeUnitDefUnitSet

        addUseLocalDefUnit(defs, needToRemove, relativeUnitDefUnitSet);//添加intent data相关的语句中使用的变量的定义语句。如：x1.equals(x2) ,x1和intent相关，需要找到x2的定义语句，x2定义语句可能在删除之列。


        System.out.println(relativeUnitDefUnitSet);


        Set<Unit> allUnitsOfGraph = new HashSet<>(this.getAllUnit());
        deleteUnitAndSimilarEdge(needToRemove, allUnitsOfGraph, allUnitInPathGraphPrevious.allBranchUnitSet);//删除不相关的点


        for (Unit unit : needToRemove) {
            bodyMapping.jimpleBodyA.getUnits().remove(bodyMapping.bindings.get(unit));
        }




        allIntentUnitFromStartToTargetUnitInpath = new HashSet<>();
        findAllUnitInToTargetUnitPath(targetUnit, this, allIntentUnitFromStartToTargetUnitInpath);


        Graph allUnitInPathGraphReduced = new Graph(sootMethod, targetUnit);

        getAllBranchUnit(this, this.getAllUnit(), allUnitInPathGraphReduced);


        WriteFile writeFile = new WriteFile(Config.intentConditionSymbolicExcutationResults+"/" + "unitGraphSize.txt", true,logger);
        writeFile.writeStr(allUnitInPathGraphPrevious.getUnits().size() + " " + this.getAllUnit().size() + "#" + new File(appPath).getName() + "#" + sootMethod.getBytecodeSignature() + "#" + targetUnit + "\n");
        writeFile.close();


        //allUnitInPathGraphReduced.exportGexf(sootMethod.getName() + "_reduced");
        WriteFile writeFileJimpleBodyReduced=new WriteFile("AnalysisAPKIntent/cfgReduced.txt",true,logger);
        writeFileJimpleBodyReduced.writeStr("\n\n"+bodyMapping.jimpleBodyP.toString()+"\n\n");


    }

    private void addUseLocalDefUnit(SimpleLocalDefs defs, Set<Unit> needToRemove, Set<Unit> relativeUnitDefUnitSet) {//ok

        List<Unit> processQueue = new ArrayList<>(relativeUnitDefUnitSet);

        while (!processQueue.isEmpty()) {
            Unit unit = processQueue.remove(0);
            if (!judgeThisUnitUseLocal(unit)) {
                continue;
            }

            if (!allUnitFromStartToTargetUnitInpath.contains(unit)) {
                continue;
            }


            for (ValueBox valuebox :unit.getUseBoxes()) {
                if (valuebox.getValue() instanceof Local) {
                    Local local = (Local) (valuebox.getValue());
                    int count = 0;
                    removeUseLocalDefUnit(defs, needToRemove, local, unit, relativeUnitDefUnitSet, count, processQueue);
                }
            }


        }

    }

    public static boolean judgeThisUnitUseLocal(Unit relativeUnit) {

        Stmt stmt = (Stmt) relativeUnit;
        if (stmt.containsInvokeExpr()) {
            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            if (invokeExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {
                return true;
            } else if (invokeExpr.getMethod().getName().equals("equals") && invokeExpr.getMethod().getDeclaringClass().getName().equals("java.lang.String")) {//
                return true;
            } else if (invokeExpr.getMethod().getDeclaringClass().getName().equals("android.os.Bundle") || invokeExpr.getMethod().getDeclaringClass().getName().equals("android.os.BaseBundle")) {//
                return true;
            }

        }
        return false;

    }

    public static void getAllBranchUnit(UnitGraph ug, Set<Unit> allUnits, Graph allUnitInPathGraph) {

        for (Unit unit : allUnits) {
            allUnitInPathGraph.addNode(new Node(unit));

            if (ug.getSuccsOf(unit).size() >= 2) {
                allUnitInPathGraph.allBranchUnitSet.add(unit);
            }

            for (Unit p : ug.getPredsOf(unit)) {
                allUnitInPathGraph.addEdge(new Node(p), new Node(unit));
            }

            for (Unit s : ug.getSuccsOf(unit)) {
                allUnitInPathGraph.addEdge(new Node(unit), new Node(s));
            }
        }


    }

    public static void findUnRelativeNode(Unit targetUnit, SootMethod sootMethod, Set<Unit> unitNeedToRemove, SingleSootMethodIntentFlowAnalysis intentFlowAnalysis, UnitGraph ug, Set<Unit> unitIntentRelativeAndStartStop) {

        for (Unit oneUnit : sootMethod.getActiveBody().getUnits())

        {
            if ((!isAboutIntentOrTargetAPI(oneUnit, intentFlowAnalysis, targetUnit)) && ug.getPredsOf(oneUnit).size() != 0 && ug.getSuccsOf(oneUnit).size() != 0) {

                unitNeedToRemove.add(oneUnit);


            } else {
                unitIntentRelativeAndStartStop.add(oneUnit);
            }

        }
    }

    public static void removeUseLocalDefUnit(SimpleLocalDefs defs, Set<Unit> needToRemove, Local local, Unit useLocalUnit, Set<Unit> relativeUnitDefUnitSet, int count, List<Unit> processQueue) {


        for (Unit defUnit : defs.getDefsOfAt(local, useLocalUnit)) {
            if (!relativeUnitDefUnitSet.contains(defUnit)) {
                needToRemove.remove(defUnit);
                relativeUnitDefUnitSet.add(defUnit);
                processQueue.add(defUnit);
                System.out.println(defUnit + "******" + count);

                if (defUnit instanceof DefinitionStmt) {
                    DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;
                    if (definitionStmt.getRightOp() instanceof Local) {

                        removeUseLocalDefUnit(defs, needToRemove, (Local) definitionStmt.getRightOp(), defUnit, relativeUnitDefUnitSet, count++, processQueue);

                    }

                }

            }

        }

    }

    public void deleteUnitAndSimilarEdge(Set<Unit> needToRemove, Set<Unit> allUnit, Set<Unit> branchUnitSet) {


        HashSet<Unit> removeSet = new HashSet<>(needToRemove);

        while (!removeSet.isEmpty()) {

            for (Unit oneUnit : allUnit) {

                deleteRepeatEdge(removeSet, oneUnit, needToRemove, branchUnitSet);


            }

            while (!removeSet.isEmpty()) {

                Iterator<Unit> iterator = removeSet.iterator();

                Unit toBeRemovedUnit = iterator.next();//第一个元素
                this.deleteUnit(toBeRemovedUnit);
                allUnit.remove(toBeRemovedUnit);
                iterator.remove();

                HashSet<Unit> parents = new HashSet<>(this.getPredsOf(toBeRemovedUnit));

                for (Unit p : parents) {
                    deleteRepeatEdge(removeSet, p, needToRemove, branchUnitSet);
                }

                HashSet<Unit> successors = new HashSet<>(this.getSuccsOf(toBeRemovedUnit));

                for (Unit s : successors) {
                    deleteRepeatEdge(removeSet, s, needToRemove, branchUnitSet);
                }


            }

            for (Unit oneUnit : allUnit) {


                deleteRepeatEdge(removeSet, oneUnit, needToRemove, branchUnitSet);


            }
        }


    }

    public void deleteRepeatEdge(HashSet<Unit> removeSet, Unit oneUnit, Set<Unit> needToRemove, Set<Unit> branchUnitSet) {

        List<Unit> allParentsList = this.getPredsOf(oneUnit);

        HashSet<Unit> allParentsSet = new HashSet<>(allParentsList);//去除重复的父亲

        allParentsSet.remove(oneUnit);//去除自循环 约减循环

        List<Unit> newParentsList = new ArrayList<>(allParentsSet);

        this.getUnitToPreds().put(oneUnit, newParentsList);


        List<Unit> allChildList = this.getSuccsOf(oneUnit);

        HashSet<Unit> allChildListSet = new HashSet<>(allChildList);//去除重复的孩子

        allChildListSet.remove(oneUnit);//去除自循环  约减循环

        if (allChildListSet.size() == 1 && (branchUnitSet.contains(oneUnit))) {
            removeSet.add(oneUnit);
            needToRemove.add(oneUnit);
        }

        List<Unit> newChildList = new ArrayList<>(allChildListSet);

        this.getUnitToSuccs().put(oneUnit, newChildList);
    }

    public static void findAllUnitInToTargetUnitPath(Unit oneUnit, UnitGraph unitGraph, Set<Unit> visited) {

        visited.add(oneUnit);

        for (Unit unit : unitGraph.getPredsOf(oneUnit)) {
            if (!visited.contains(unit)) {
                findAllUnitInToTargetUnitPath(unit, unitGraph, visited);
            }

        }

    }

    public static boolean isAboutIntentOrTargetAPI(Unit unit,SingleSootMethodIntentFlowAnalysis intentFlowAnalysis, Unit targetUnit) {

        if (unit == targetUnit) {
            return true;
        }
        FlowSet<Value> intentRelativeValue = intentFlowAnalysis.getFlowBefore(unit);

        FlowSet<Value> intentAfter = intentFlowAnalysis.getFlowAfter(unit);


        for (Value value : intentAfter) {
            for (ValueBox valueBox : unit.getDefBoxes()) {
                if (value.equivTo(valueBox.getValue())) {
                    return true;
                }
            }
        }

        for (Value value : intentRelativeValue) {
            for (ValueBox valueBox : unit.getUseBoxes()) {
                if (value.equivTo(valueBox.getValue())) {
                    return true;
                }

            }
        }


        return false;
    }

    public Map<Unit, List<Unit>> getUnitToPreds() {
        return unitToPreds;
    }

    public Map<Unit, List<Unit>> getUnitToSuccs() {
        return unitToSuccs;
    }

    public Set<Unit> getAllUnit() {
//        if (!unitToPreds.keySet().containsAll(unitToSuccs.keySet()) && unitToSuccs.keySet().containsAll(unitToPreds.keySet())) {
//            throw new RuntimeException("冲突");
//        }

        return unitToPreds.keySet();
    }

    public void deleteUnit(Unit unit) {

        List<Unit> parents = unitToPreds.get(unit);

        List<Unit> successors = unitToSuccs.get(unit);


        if (parents != null && successors != null) {


            for (Unit p : parents) {

                HashSet<Unit> pSuccessorsSet = new HashSet<>(unitToSuccs.get(p));//本来的后继

                pSuccessorsSet.addAll(successors);

                pSuccessorsSet.remove(unit);

                unitToSuccs.put(p, new ArrayList<>(pSuccessorsSet));
            }


            for (Unit s : successors) {

                HashSet<Unit> sPredecessors = new HashSet<>(unitToPreds.get(s));//本来的前继

                sPredecessors.addAll(parents);
                sPredecessors.remove(unit);
                unitToPreds.put(s, new ArrayList<>(sPredecessors));

            }

//            for(Unit p:parents)
//            {
//                List<Unit> pSuccessorList=unitToSuccs.get(p);
//                pSuccessorList.addAll(successors);
//                for(Iterator<Unit> iterator=pSuccessorList.iterator();iterator.hasNext();)
//                {
//                    Unit pSuccessor=iterator.next();
//                    if(pSuccessor==unit)
//                    {
//                        iterator.remove();
//                    }
//                }
//
//
//                unitToSuccs.put(p,pSuccessorList);
//            }
//
//            for(Unit s:successors)
//            {
//                List<Unit> sParentsList=unitToPreds.get(s);
//                sParentsList.addAll(parents);
//                for(Iterator<Unit> iterator=sParentsList.iterator();iterator.hasNext();)
//                {
//                    Unit sParent=iterator.next();
//                    if(sParent==unit)
//                    {
//                        iterator.remove();
//                    }
//                }
//                unitToPreds.put(s,sParentsList);
//            }

            unitToSuccs.remove(unit);
            unitToPreds.remove(unit);


        } else {
            throw new RuntimeException("算法异常！");
        }


    }

    public void addEndReturnNode(Unit exitUnit) {

        for (Unit targetUnit : this.getBody().getUnits()) {
            if ((targetUnit instanceof ReturnVoidStmt || targetUnit instanceof ReturnStmt) && (exitUnit instanceof ExitJStmt)) {
                List<Unit> successorsOfReturn = unitToSuccs.get(targetUnit);
                assert successorsOfReturn == null;

                successorsOfReturn = new ArrayList<>();

                successorsOfReturn.add(exitUnit);

                unitToSuccs.put(targetUnit, successorsOfReturn);


                List<Unit> parentsOfExit = unitToPreds.get(exitUnit);

                if (parentsOfExit == null) {
                    parentsOfExit = new ArrayList<>();
                }

                parentsOfExit.add(targetUnit);

                unitToPreds.put(exitUnit, parentsOfExit);


            }

        }


    }

    public void changeInherit(Unit ifUnit, Unit joinUnit)//(去除If语句块)
    {

        List<Unit> ifParents = unitToPreds.get(ifUnit);

        List<Unit> joinParents = new ArrayList<>();


        if (ifParents != null) {
            for (Unit ifFather : ifParents) {
                List<Unit> ifFatherChilds = unitToSuccs.get(ifFather);
                if (ifFatherChilds != null) {
                    ifFatherChilds.remove(ifUnit);//去除原来的if的后继
                    ifFatherChilds.add(joinUnit);//直接加入joinUnit作为后继
                }

                joinParents.add(ifFather);


            }

        }

        if (joinParents != null) {
            unitToPreds.put(joinUnit, joinParents);//join的前驱为ifUnit的前驱
        }

        unitToPreds.remove(ifUnit);
        unitToSuccs.remove(ifUnit);


    }

    public void removeExitJstmt(Unit exitUnit) {
        unitToSuccs.remove(exitUnit);
        unitToPreds.remove(exitUnit);
        for (Unit targetUnit : this.getBody().getUnits()) {
            if ((targetUnit instanceof ReturnVoidStmt || targetUnit instanceof ReturnStmt) && (exitUnit instanceof ExitJStmt)) {
                unitToSuccs.remove(targetUnit);


            }

        }
    }

    public boolean equivTo(MyUnitGraph otherUnitGraph) {
        if (otherUnitGraph == null) {
            return false;
        }
        if (this.getAllUnit().size() != otherUnitGraph.getAllUnit().size()) {
            return false;
        }

        for (Unit unit : this.getAllUnit())//targetUnit不在考察范围之内
        {
            Unit unit1 = null;
            Unit unit2 = null;
            if (unit == targetUnit) {
                unit1 = targetUnit;
                unit2 = otherUnitGraph.targetUnit;
            } else {
                unit1 = unit;
                unit2 = unit;
            }

            Set<Unit> parents = new HashSet<>(getPredsOf(unit1));
            parents.remove(targetUnit);
            Set<Unit> otherParents = new HashSet<>(otherUnitGraph.getPredsOf(unit2));
            otherParents.remove(otherUnitGraph.targetUnit);
            if (parents.size() != otherParents.size()) {
                return false;
            }

            if (!parents.containsAll(otherParents)) {
                return false;
            }

            Set<Unit> children = new HashSet<>(getSuccsOf(unit1));
            children.remove(targetUnit);
            Set<Unit> otherChildren = new HashSet<>(otherUnitGraph.getSuccsOf(unit2));
            otherChildren.remove(otherUnitGraph.targetUnit);
            if (children.size() != otherChildren.size()) {
                return false;
            }

            if (!children.containsAll(otherChildren)) {
                return false;
            }
        }


        return true;


    }
}
