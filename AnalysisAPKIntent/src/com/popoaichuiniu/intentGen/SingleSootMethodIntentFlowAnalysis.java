package com.popoaichuiniu.intentGen;

import soot.*;
import soot.jimple.*;

import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.DirectedGraph;

import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.*;


public class SingleSootMethodIntentFlowAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Value>> {//终极ok


    private Set<Value> parameterDataSet = null;

    private Map<Unit, Set<Value>> unitCallMethodIntentRelativeMap = null;

    private boolean isOveralIntentFlowAnalysis = false;

    public SingleSootMethodIntentFlowAnalysis(SootMethod sootMethod, DirectedGraph<Unit> graph, Set<Value> parameterDataSet, boolean isOveralIntentFlowAnalysis) {


        super(graph);

        this.isOveralIntentFlowAnalysis = isOveralIntentFlowAnalysis;

        this.parameterDataSet = parameterDataSet;

        if (isOveralIntentFlowAnalysis) {

            unitCallMethodIntentRelativeMap = new HashMap<>();
        } else {
            this.parameterDataSet = null;
        }


        doAnalysis();


    }

    public Map<Unit, Set<Value>> getUnitCallMethodIntentRelativeMap() {
        return unitCallMethodIntentRelativeMap;
    }


//终止是不再变化.

    @Override
    protected void flowThrough(FlowSet<Value> in, Unit d, FlowSet<Value> out) {//1.从方法中传入的intent属性数据多不多统计一下
        //2.intent属性数据和intent从方法的返回值多不多
        //3.intent属性数据从类属性中取。（未考虑，考虑了intent来自field）


        in.copy(out);

        System.out.println("%%%%%%%" + d);

        Stmt stmt = (Stmt) d;


        if (isOveralIntentFlowAnalysis) {

            HashSet<Value> intentDataValueSet = new HashSet<>();
            if (stmt.containsInvokeExpr()) {
                InvokeExpr invokeExpr = stmt.getInvokeExpr();


                for (int i = 0; i < invokeExpr.getArgCount(); i++) {

                    if (in.contains(invokeExpr.getArg(i))) {
                        intentDataValueSet.add(invokeExpr.getArg(i));
                    }


                }


            }
            unitCallMethodIntentRelativeMap.put(d, intentDataValueSet);//参数和intentdata有关，存储起来

        }


        if (d instanceof DefinitionStmt) {
            DefinitionStmt definitionStmt = (DefinitionStmt) d;


            if (definitionStmt.getRightOp().getType().toString().equals("android.content.Intent")) {
                if (definitionStmt.getRightOp() instanceof FieldRef || definitionStmt.getRightOp() instanceof ParameterRef) {//Intent 来自域 和 参数


                    out.add(definitionStmt.getRightOp());


                    out.add(definitionStmt.getLeftOp());


                } else if (definitionStmt.getRightOp() instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) definitionStmt.getRightOp();
                    if (jVirtualInvokeExpr.getMethod().getName().equals("getIntent")) {//intent从getIntent方法中来

                        out.add(definitionStmt.getLeftOp());


                    }

                } else if (definitionStmt.getRightOp() instanceof CastExpr)//intent从强制转换而来
                {
                    out.add(definitionStmt.getLeftOp());
                }

            } else if (definitionStmt.containsInvokeExpr()) {// intent attribute加入
                if (definitionStmt.getInvokeExpr() instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) definitionStmt.getInvokeExpr();
                    if (invokeExpr.getBase().getType().toString().equals("android.content.Intent")) {


                        if (in.contains(invokeExpr.getBase()))//intent from in
                        {
                            out.add(definitionStmt.getLeftOp());

                        }


                    }

                }
            }


            List<ValueBox> usedUnitBox = definitionStmt.getRightOp().getUseBoxes();


            for (ValueBox valueBox : usedUnitBox)//只要等式右边有一个值被污染了，就认为这个等式左边的值被污染
            {
                Value value = valueBox.getValue();
                if (in.contains(value)) {
                    out.add(definitionStmt.getLeftOp());

                    break;
                }
            }


            //kill

            if (in.contains(definitionStmt.getLeftOp())) {//in中的值被重新赋值，所以可能他就不是intent相关的呢


                for (ValueBox valueBox : usedUnitBox) {
                    Value value = valueBox.getValue();
                    if (in.contains(value)) {//使用了in中的值
                        return;
                    }
                }


                if (definitionStmt.getRightOp().getType().toString().equals("android.content.Intent")) {
                    if (definitionStmt.getRightOp() instanceof FieldRef || definitionStmt.getRightOp() instanceof ParameterRef) {

                        return;


                    } else if (definitionStmt.getRightOp() instanceof JVirtualInvokeExpr) {
                        JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) definitionStmt.getRightOp();
                        if (jVirtualInvokeExpr.getMethod().getName().equals("getIntent")) {

                            return;


                        }
                    } else if (definitionStmt.getRightOp() instanceof CastExpr) {
                        return;
                    }
                }


                //this unit is not about intent,so kill

                out.remove(definitionStmt.getLeftOp());


            }


        }


    }

    @Override
    protected void merge(FlowSet<Value> in1, FlowSet<Value> in2, FlowSet<Value> out) {

        in1.union(in2, out);

    }

    @Override
    protected void copy(FlowSet<Value> source, FlowSet<Value> dest) {

        source.copy(dest);

    }

    @Override
    protected FlowSet<Value> newInitialFlow() {
        return new MyArraySparseSet<>();
    }

    @Override
    protected FlowSet<Value> entryInitialFlow() {

        MyArraySparseSet myArraySparseSet = new MyArraySparseSet<>();

        if (isOveralIntentFlowAnalysis && parameterDataSet != null) {
            for (Value value : parameterDataSet) {
                myArraySparseSet.add(value);
            }
        }

        return myArraySparseSet;
    }
}


