package com.popoaichuiniu.intentGen;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.*;

public class IntentPropagateAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Value>> {


    private Unit startUnit = null;

    private int parameterIndex=-1;

    private boolean isReachStartUnitFlag = false;

    public  Set<Unit> useIntentUnitSet =new LinkedHashSet<>();


    private SootMethod sootMethod=null;

    public IntentPropagateAnalysis(SootMethod sootMethod, DirectedGraph<Unit> graph, Unit unit ,int index) {


        super(graph);

        this.sootMethod=sootMethod;

        this.startUnit = unit;

        this.parameterIndex=index;


        doAnalysis();


    }


//终止是不再变化.

    @Override
    protected void flowThrough(FlowSet<Value> in, Unit d, FlowSet<Value> out) {
        //1.从方法中传入的intent属性数据多不多统计一下
        //2.intent属性数据和intent从方法的返回值多不多
        //3.intent属性数据从类属性中取。（未考虑，考虑了intent来自field）

        in.copy(out);

        if (parameterIndex==-1) {


            if(!isReachStartUnitFlag)
            {

                if (startUnit != d) {
                    return;
                }
                else
                {
                    if (d instanceof DefinitionStmt) {
                        DefinitionStmt definitionStmt = (DefinitionStmt) d;
                        out.add(definitionStmt.getLeftOp());
                        isReachStartUnitFlag = true;
                    } else {
                       throw new RuntimeException("! start instanceof DefinitionStmt"+d);
                    }
                }



            }


        }
        else
        {
            if (d instanceof DefinitionStmt) {
                DefinitionStmt definitionStmt = (DefinitionStmt) d;
                if(definitionStmt.getRightOp() instanceof ParameterRef)
                {
                    ParameterRef parameterRef= (ParameterRef) definitionStmt.getRightOp();
                    if(parameterRef.getIndex()==parameterIndex)
                    {
                        out.add(definitionStmt.getLeftOp());
                    }
                }

            }

        }


        System.out.println("%%%%%%%" + d);


        if (d instanceof DefinitionStmt) {
            DefinitionStmt definitionStmt = (DefinitionStmt) d;

            //add
            if (in.contains(definitionStmt.getRightOp())) {//$r2=$r3
                out.add(definitionStmt.getLeftOp());
            }

            //add

            if(definitionStmt.containsInvokeExpr())
            {
                SootMethod calleeSootMethod=definitionStmt.getInvokeExpr().getMethod();
                if(calleeSootMethod.getDeclaringClass().getName().equals("android.content.Intent")&&calleeSootMethod.getName().equals("<init>"))// new intent(intent o)
                {
                    if(definitionStmt.getInvokeExpr().getArgs().size()==1&&definitionStmt.getInvokeExpr().getArg(0).getType().toString().equals("android.content.Intent"))
                    {
                        if(in.contains(definitionStmt.getInvokeExpr().getArg(0)))
                        {
                            out.add(definitionStmt.getLeftOp());
                        }
                    }
                }
            }



            //kill

            if (in.contains(definitionStmt.getLeftOp()) && (!definitionStmt.getRightOp().getType().toString().equals("android.content.Intent"))) {//in中的值被重新赋值，所以可能他就不是intent相关的呢


                out.remove(definitionStmt.getLeftOp());


            }


        }


        List<ValueBox> valueBoxList= d.getUseBoxes();

        for(ValueBox valueBox:valueBoxList)
        {
            if(in.contains(valueBox.getValue()))
            {
                useIntentUnitSet.add(d);//将使用intent相关变量的语句保存
                break;
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


        return myArraySparseSet;
    }
}
