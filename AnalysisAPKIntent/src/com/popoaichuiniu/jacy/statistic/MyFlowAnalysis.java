package com.popoaichuiniu.jacy.statistic;

import java.util.List;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

public class MyFlowAnalysis extends BackwardFlowAnalysis<Unit, FlowSet<Object>> {

	private Set<Object> initialSet = null;

	public MyFlowAnalysis(DirectedGraph<Unit> graph, Set<Object> initialSet) {

		super(graph);

		this.initialSet = initialSet;

		doAnalysis();
	}

	@Override
	protected void flowThrough(FlowSet<Object> in, Unit d, FlowSet<Object> out) {
		// TODO Auto-generated method stub
		in.copy(out);

		if(d instanceof DefinitionStmt)//d是一个赋值语句
		{
			DefinitionStmt definitionStmt=(DefinitionStmt)d;			
			
			for(Object item: in)
			{
				MyFlowSetValue myFlowSetValue=(MyFlowSetValue)item;
				
				if(myFlowSetValue.local!= null)//存储的是local
				{
					if(definitionStmt.getLeftOp().equivTo(myFlowSetValue.local))//---------------equivTo是否存在问题？
					{
						
						myFlowSetValue.local=null;//去掉旧值
						
						
						Value value=definitionStmt.getRightOp();
						if(value instanceof Local)//item=value
						{
							myFlowSetValue.local=(Local) value;
						}
						else if(value instanceof InvokeExpr)
						{
							InvokeExpr invokeExpr=(InvokeExpr) value ;//  item=xxx(zzz)  用xxx(zzz)替换item 
							
							if(invokeExpr.getArgCount()>0)//有参数的调用 
							{
								myFlowSetValue.args=invokeExpr.getArgs();
								myFlowSetValue.invokeArgExpr=invokeExpr;
							}
							else//无参数的调用
							{
								myFlowSetValue.invokeNoArgExpr=invokeExpr;
							}
							
						}
						
						
					}
				}
				else if(myFlowSetValue.invokeArgExpr!=null)//看看当前语句是的左边的变量是不是这个invokeArgExpr的参数之一，是的话更新参数的值
				{
					List<Value> args=myFlowSetValue.args;
					for(Value arg:args)
					{
						assert arg instanceof Local;//--------由于三地址的原因,参数一定是local，更新的时候也只考虑local更新
						
						Local local=(Local) arg;
						if(definitionStmt.getLeftOp().equivTo(local))
						{
							if(definitionStmt.getRightOp() instanceof Local)
							{arg=definitionStmt.getRightOp();//更新参数，，，
							
							}
							else//不考虑更新其他形式的值了，太复杂了。  
							{
								
							}
						}
						
						
					}
					
					
				}
				
				else if(myFlowSetValue.invokeNoArgExpr!=null) {
					
				}
			
				
				
				
				
			
					
			}
			
			
		}
//		else if(d instanceof InvokeStmt)//intent产生的值除了Bundle其他几乎都是基本类型和String   省略---------------------
//		{
//			InvokeStmt invokeStmt=(InvokeStmt)d;
//			java.util.List<Value> args=invokeStmt.getInvokeExpr().getArgs();
//			for(Value item: in)			{
//				
//				for(Value arg:args)					
//				{
//					if(arg.equivExtraTo(item)&&)//调用语句调用了In里的参数,可能在调用的方法体中被修改了 xxx(item)   item可能是表达式吗？不能是，三地址结构导致。且item必须是引用才能被修改,这里
//					{
//						out.add(invokeStmt.getInvokeExpr());
//						out.remove(item);
//						break;
//					}
//					
//				}
//					
//			}
//		}
		
		
		
		
	}

	@Override
	protected void merge(FlowSet<Object> in1, FlowSet<Object> in2, FlowSet<Object> out) {
		// TODO Auto-generated method stub
		in1.union(in2, out);
	}

	@Override
	protected void copy(FlowSet<Object> source, FlowSet<Object> dest) {
		// TODO Auto-generated method stub

		source.copy(dest);

	}
	// Used to initialize the in and out sets for each node. In

	@Override
	protected FlowSet<Object> newInitialFlow() {
		// TODO Auto-generated method stub

		return new ArraySparseSet<>();

	}

	// Returns FlowSet representing the initial set of the entry
	// node.
	@Override
	protected FlowSet<Object> entryInitialFlow() {
		FlowSet<Object> flowSet = new ArraySparseSet<>();
		for (Object item : initialSet) {
			flowSet.add(item);
		}

		return flowSet;
	}

}
