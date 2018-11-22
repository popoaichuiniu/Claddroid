package com.popoaichuiniu.jacy.statistic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

public class FindDataSource  extends SceneTransformer{
	
	List<SootMethod>  callPath=null;
	

public FindDataSource(List<SootMethod> callPath) {
		super();
		this.callPath = callPath;
		
	}

private boolean isTargetAPIInvoke(SootMethod target,InvokeExpr invokeExpr)
{
	if(invokeExpr.getMethod().getBytecodeSignature().equals(target.getBytecodeSignature()))
			{
		return true;
			}
	return false;
}
@Override
protected void internalTransform(String phaseName, Map<String, String> options) {
	
	
	System.out.println("**********************************我在数据流分析啊***************************************");
	CallGraph callGraph=Scene.v().getCallGraph();
	
	for(int i=callPath.size()-2;i>=0;i--)//不分析倒数第一个
	{
		SootMethod sootMethod=callPath.get(i);
		
		if(sootMethod.getActiveBody()!=null)
		{
			UnitGraph unitGraph=new BriefUnitGraph(sootMethod.getActiveBody()); 
			
			Set<Object> initialSet=new HashSet<>();
			
			Unit sUnit=null;
			if(i==callPath.size()-2)
			{
				for(Iterator<Unit>iterator= unitGraph.iterator();iterator.hasNext();)
				{
					Unit unit=iterator.next();
					sUnit=unit;
				
					System.out.println(unit);
					if(unit instanceof DefinitionStmt)
					{	Value value=((DefinitionStmt) unit).getRightOp();
						if(value instanceof InvokeExpr)
						{   InvokeExpr invokeExpr=(InvokeExpr)value;
							if(isTargetAPIInvoke(callPath.get(callPath.size()-1),invokeExpr))
							{
								initialSet.addAll(invokeExpr.getArgs());//将目标方法的参数全部加入到initialSet中
								
								break;
							}
						}
						
					}
					else if(unit instanceof InvokeStmt)
					{
						InvokeExpr invokeExpr=((InvokeStmt) unit).getInvokeExpr();
						if(isTargetAPIInvoke(callPath.get(callPath.size()-1),invokeExpr))
						{       
							initialSet.addAll(invokeExpr.getArgs());//将目标方法的参数全部加入到initialSet中
							
							break;
						}
					}
					
				}
			}
			MyFlowAnalysis myFlowAnalysis=new MyFlowAnalysis(unitGraph, initialSet);
			FlowSet<Object> flowSet=myFlowAnalysis.getFlowBefore(sUnit); 
			
			System.out.println("数据流分析结果：×××××××××××××××××××××××××××××××××××××××");
			System.out.println(flowSet.toList());
			System.out.println("数据流分析结果：×××××××××××××××××××××××××××××××××××××××");

			
			
		}
		break;
		
		
		
	}
	System.out.println("**********************************我在数据流分析啊***************************************");
	
	
	
	
	  
	
	
	
	
	
	
	
	
	// TODO Auto-generated method stub
	
}
public static void main(String[] args) {
	
	


	
	
	


}




}


