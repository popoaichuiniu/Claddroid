package com.cheng;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lu.uni.snt.droidra.booster.InstrumentationUtils;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;

public class FieldInstrumentation extends DefaultInstrumentation{

	public FieldInstrumentation(Element element) {
		super(element);
	}

	@Override
	public void instrument() {
		System.out.println("**********************************************");
		SootClass targetClass = Scene.v().getSootClass(element.getClassName());
		SootField targetField = getSootField(targetClass,element.getMethodName());
		String callerSignature = element.getStackElements().get(2).getSootSignature();
		int callerLine = element.getStackElements().get(2).getLineNumber();
		SootMethod callerMethod = Scene.v().getMethod(callerSignature);
		Body body = callerMethod.retrieveActiveBody();
		Stmt stmt = null;
		PatchingChain<Unit> units = body.getUnits();
		for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			Stmt tmpStmt = (Stmt) iter.next();
			if(tmpStmt.getJavaSourceStartLineNumber() == callerLine
			   && tmpStmt.containsInvokeExpr()
			   && GlobalValue.FIELD_CALL_SET.contains(tmpStmt.getInvokeExpr().getMethod().getSignature())){
					stmt = tmpStmt;
					break;
			}
		}
		
		if(stmt == null){
			return;
		}
		
		Stmt nextStmt = getNextStmt(body, stmt);
		
		SootMethod throughMethod = stmt.getInvokeExpr().getMethod();
		boolean isStatic = InstrumentationUtils.isStaticReflectiveInvocation(stmt) || targetField.isStatic();
		
		LocalGenerator localGenerator = new LocalGenerator(body);
		List<Unit> injectedUnits = new LinkedList<>();
		
		if(throughMethod.getName().startsWith("get")){//获取字段值
			if(isStatic){//静态字段
				if(stmt instanceof AssignStmt){//只考虑向变量赋值
					AssignStmt assignStmt = (AssignStmt) stmt;
					Value leftOp = assignStmt.getLeftOp();
					
					Unit callU = Jimple.v().newAssignStmt(leftOp, Jimple.v().newStaticFieldRef(targetField.makeRef()));
					injectedUnits.add(callU);
				}
			}else{//非静态字段
				Value arg0 = stmt.getInvokeExpr().getArg(0);
				Local arg0Local = localGenerator.generateLocal(arg0.getType());
				//构造局部变量，引用字段所属对象
				Unit assignU = Jimple.v().newAssignStmt(arg0Local, arg0);
				injectedUnits.add(assignU);
				
				if(stmt instanceof AssignStmt){
					AssignStmt assignStmt = (AssignStmt) stmt;
					Value leftOp = assignStmt.getLeftOp();
					
					Unit callU = Jimple.v().newAssignStmt(leftOp, Jimple.v().newInstanceFieldRef(arg0Local, targetField.makeRef()));
					injectedUnits.add(callU);
				}
			}
		}else if(throughMethod.getName().startsWith("set")){//设置字段值
			if(isStatic){//静态字段
				Value arg1 = stmt.getInvokeExpr().getArg(1);
				Unit callU = Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(targetField.makeRef()), arg1);
				injectedUnits.add(callU);
			}else{//非静态字段
				Value arg0 = stmt.getInvokeExpr().getArg(0);
				
				Value arg1 = stmt.getInvokeExpr().getArg(1);
				Unit callU = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(arg0, targetField.makeRef()), arg1);
				injectedUnits.add(callU);
			}
		}
		
		for(int i = injectedUnits.size() - 1;i >= 0;i--){
			body.getUnits().insertAfter(injectedUnits.get(i), stmt);
		}
		
		
		injectedStmtWrapper(body, localGenerator, stmt, nextStmt);
		
		//System.out.println(body);
		body.validate();
	}
	
	private SootField getSootField(SootClass sootClass, String fieldName){
		try{
			return sootClass.getFieldByName(fieldName);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
