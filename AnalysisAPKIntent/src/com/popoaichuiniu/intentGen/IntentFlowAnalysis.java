package com.popoaichuiniu.intentGen;

import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;

import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.DirectedGraph;

import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.*;


public class IntentFlowAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Value>> {




    private Set<Unit> visited = new HashSet<>();

    private Set<Value> intentSetFromGetIntent =new HashSet<>();
    private Set<Value> intentSetFromParameter =new HashSet<>();
    private Set<Value> intentAttr=new HashSet<>();

    private WriteFile writeFileIntentAttr;
    private WriteFile writeFileIntentGetIntent;
    private WriteFile writeFileIntentFromParameter;
    public IntentFlowAnalysis(DirectedGraph<Unit> graph, Logger logger) {

        super(graph);
        writeFileIntentAttr=new WriteFile("AnalysisAPKIntent/intentPass/"+"intentAttr.txt",true,logger);
        writeFileIntentGetIntent=new WriteFile("AnalysisAPKIntent/intentPass/"+"intentGetIntent.txt",true,logger);
        writeFileIntentFromParameter=new WriteFile("AnalysisAPKIntent/intentPass/"+"intentFromParameter.txt",true,logger);

        doAnalysis();
        writeFileIntentAttr.close();
        writeFileIntentGetIntent.close();
        writeFileIntentFromParameter.close();


    }
    //终止是不再变化.

    @Override
    protected void flowThrough(FlowSet<Value> in, Unit d, FlowSet<Value> out) {//1.从方法中传入的intent属性数据多不多统计一下
                                                                                //2.intent属性数据从方法的返回值多不多
                                                                                //3.intent属性数据从类属性中取。（多个方法数据流分析分析组合）
                                                                                //4.传入的intent可能是new Intent的。（这个到无所谓，多考虑了呗）

        System.out.println(d);
//        if (visited.contains(d)) {
//            return;
//        }
        visited.add(d);//去除循环
        in.copy(out);

        //System.out.println("%%%%%%%"+d);

        if (d instanceof DefinitionStmt) {
            DefinitionStmt definitionStmt = (DefinitionStmt) d;


            if (definitionStmt.getRightOp().getType().toString().equals("android.content.Intent")) {//Intent 从参数和域
                if ( definitionStmt.getRightOp() instanceof ParameterRef || definitionStmt.getRightOp() instanceof FieldRef) {


                    out.add(definitionStmt.getRightOp());


                    out.add(definitionStmt.getLeftOp());

                    intentSetFromParameter.add(definitionStmt.getLeftOp());
                    intentSetFromParameter.add(definitionStmt.getRightOp());




                } else if (definitionStmt.getRightOp() instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) definitionStmt.getRightOp();
                    if (jVirtualInvokeExpr.getMethod().getName().equals("getIntent")) {//intent从getIntent方法中来

                        out.add(definitionStmt.getLeftOp());
                        intentSetFromGetIntent.add(definitionStmt.getLeftOp());


                    }

                }
                else if(definitionStmt.getRightOp() instanceof CastExpr)//intent从强制转换而来
                {
                    out.add(definitionStmt.getLeftOp());
                }

            } else if (definitionStmt.containsInvokeExpr()) {// intent attribute加入
                if (definitionStmt.getInvokeExpr() instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) definitionStmt.getInvokeExpr();
                    if (invokeExpr.getBase().getType().toString().equals("android.content.Intent")) {

                        //if (invokeExpr.getMethod().getName().startsWith("get") || invokeExpr.getMethod().getName().startsWith("has")) {

                        if (in.contains(invokeExpr.getBase()))//intent from in
                        {
                            out.add(definitionStmt.getLeftOp());
                            intentAttr.add(definitionStmt.getLeftOp());
                        }


                        //}


                    }

                }
            }

            //in value dataflow to others

//            if (definitionStmt.getRightOp() instanceof Local) {
//
//                if (in.contains(definitionStmt.getRightOp())) {
//
//                    out.add(definitionStmt.getLeftOp());
//
//
//                }
//
//
//            } else if (definitionStmt.containsInvokeExpr()) {
//                InvokeExpr invokeExpr = definitionStmt.getInvokeExpr();
//
//                List<Value> args = invokeExpr.getArgs();
//                for (Value arg : args) {
//                    if (in.contains(arg)) {
//
//                        out.add(definitionStmt.getLeftOp());
//
//
//                        break;
//                    }
//                }
//                if (invokeExpr instanceof InstanceInvokeExpr) {
//                    InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
//                    if (in.contains(instanceInvokeExpr.getBase())) {
//                        out.add(definitionStmt.getLeftOp());
//
//                    }
//                }
//            }
//            else {
//
//            }

            if(d instanceof DefinitionStmt)//直接赋值, x1=x2
            {
                DefinitionStmt temp= (DefinitionStmt) d;
                if(intentSetFromGetIntent.contains(temp.getRightOp()))
                {
                    intentSetFromGetIntent.add(temp.getLeftOp());
                }

                if(intentSetFromParameter.contains(temp.getRightOp()))
                {
                    intentSetFromParameter.add(temp.getLeftOp());
                }
                if(intentAttr.contains(temp.getRightOp()))
                {
                    intentAttr.add(temp.getLeftOp());
                }
            }


            //是否打印当前语句
            Stmt stmt= (Stmt) d;
            if(stmt.containsInvokeExpr())
            {
                InvokeExpr invokeExpr=stmt.getInvokeExpr();
                if((!invokeExpr.getMethod().getBytecodeSignature().contains("<android."))&&(!invokeExpr.getMethod().getBytecodeSignature().contains("<java.")))
                {
                    for(Value arg:invokeExpr.getArgs())
                    {

                        if(intentSetFromGetIntent.contains(arg))
                        {
                            writeFileIntentGetIntent.writeStr(d+"\n");
                        }
                        if(intentSetFromParameter.contains(arg))
                        {
                            writeFileIntentFromParameter.writeStr(d+"\n");
                        }
                        if(intentAttr.contains(arg))
                        {
                            writeFileIntentAttr.writeStr(d+"\n");
                        }

                    }
                }

            }




            List<ValueBox> usedUnitBox=definitionStmt.getRightOp().getUseBoxes();


            for(ValueBox valueBox:usedUnitBox)//只要等式右边有一个值被污染了，就认为这个等式左边的值被污染
            {
                Value value=valueBox.getValue();
                if(in.contains(value))
                {
                    out.add(definitionStmt.getLeftOp());

                    break;
                }
            }


            //kill

            if (in.contains(definitionStmt.getLeftOp())) {


                for(ValueBox valueBox:usedUnitBox)
                {
                    Value value=valueBox.getValue();
                    if(in.contains(value))
                    {
                        return;
                    }
                }



                if (definitionStmt.getRightOp().getType().toString().equals("android.content.Intent")) {
                    if ( definitionStmt.getRightOp() instanceof ParameterRef || definitionStmt.getRightOp() instanceof FieldRef) {

                        return;


                    } else if (definitionStmt.getRightOp() instanceof JVirtualInvokeExpr) {
                        JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) definitionStmt.getRightOp();
                        if (jVirtualInvokeExpr.getMethod().getName().equals("getIntent")) {

                            return;


                        }
                    }
                    else if(definitionStmt.getRightOp() instanceof CastExpr)
                    {
                        return;
                    }
                }



                //this unit is not about intent,so kill

                out.remove(definitionStmt.getLeftOp());

                intentSetFromGetIntent.remove(definitionStmt.getLeftOp());
                intentSetFromParameter.remove(definitionStmt. getLeftOp());
                intentAttr.remove(definitionStmt.getLeftOp());

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
        return new MyArraySparseSet<>();
    }
}


