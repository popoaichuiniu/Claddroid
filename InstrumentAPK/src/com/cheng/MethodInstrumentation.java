package com.cheng;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lu.uni.snt.droidra.booster.InstrumentationUtils;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;

public class MethodInstrumentation extends DefaultInstrumentation{

	public MethodInstrumentation(Element element) {
		super(element);
	}

	@Override
	public void instrument() {
		String targetSignature = element.getSootSignature();
		SootMethod targetMethod = null;
		try{
			targetMethod = Scene.v().getMethod(targetSignature);
		}catch(Exception e){
			return;
		}
		String callerSignature = element.getStackElements().get(2).getSootSignature();
		int callLine = element.getStackElements().get(2).getLineNumber();
		SootMethod callerMethod = Scene.v().getMethod(callerSignature);
		Body body = callerMethod.retrieveActiveBody();
		System.out.println(body.toString());
		Stmt stmt = null;
		int count = 0;
		PatchingChain<Unit> units = body.getUnits();
		for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			Stmt tmpStmt = (Stmt) iter.next();
			int line = tmpStmt.getJavaSourceStartLineNumber();
			if(tmpStmt.getJavaSourceStartLineNumber() == callLine
			   && tmpStmt.containsInvokeExpr()
			   && tmpStmt.toString().contains(GlobalValue.METHOD_INVOKE_SIGNATURE)){//插桩位置
				stmt = tmpStmt;
				break;    						
			}
			count++;
		}
		if(stmt == null){//未找到反射调用语句
			return;
		}
		
		//nextStmt一定要在body被改变之前进行获取，否则可能改变程序结构
		Stmt nextStmt = getNextStmt(body,stmt);
		
		List<Value> args = new LinkedList<>();
		LocalGenerator localGenerator = new LocalGenerator(body);
		List<Unit> injectedUnits = new LinkedList<>();
		List<Type> paramTypes = targetMethod.getParameterTypes();
		Value arg1 = stmt.getInvokeExpr().getArg(1);
		
		//arg1为null时，目标函数的参数为空
		if (null == arg1 || "null".equals(arg1.toString())){//目标函数参数为null
			//args.add(NullConstant.v());
		}else{
			for(int i = 0;i < paramTypes.size();i++){
				Type type = paramTypes.get(i);
				//构造局部变量，存储数组中第i个元素
				Local local = localGenerator.generateLocal(RefType.v("java.lang.Object"));
				//赋值语句，将第i个元素赋给local
				Unit assignU = Jimple.v().newAssignStmt(local, Jimple.v().newArrayRef(arg1, IntConstant.v(i)));
				injectedUnits.add(assignU);
				type = InstrumentationUtils.toPrimitiveWrapperType(type);
				//局部变量，第i个元素的真正类型
				Local local2 = localGenerator.generateLocal(type);
				//强制类型转换
				Unit assignU2 = Jimple.v().newAssignStmt(local2, Jimple.v().newCastExpr(local, type));
				injectedUnits.add(assignU2);
				args.add(local2);
			}
		}
		boolean isStatic = InstrumentationUtils.isStaticReflectiveInvocation(stmt);;
		InvokeExpr invokeExpr = null;
		if(isStatic || targetMethod.isStatic()){
			invokeExpr = Jimple.v().newStaticInvokeExpr(targetMethod.makeRef(),args);
		}else{
			Value arg0 = stmt.getInvokeExpr().getArg(0);
			Local arg0Local = localGenerator.generateLocal(arg0.getType());
			
			Unit assignU = Jimple.v().newAssignStmt(arg0Local, arg0);
			body.getUnits().insertBefore(assignU, stmt);
			if (isInterfaceType(arg0Local)){
				invokeExpr = Jimple.v().newInterfaceInvokeExpr(arg0Local, targetMethod.makeRef(), args);
			}else{
				System.out.println(targetMethod.isStatic());
				invokeExpr = Jimple.v().newVirtualInvokeExpr(arg0Local, targetMethod.makeRef(), args);
			}
		}
		
		System.out.println(body.toString());
		
		if(stmt instanceof AssignStmt){
			Value leftOp = ((AssignStmt) stmt).getLeftOp();
			Unit invokeU = Jimple.v().newAssignStmt(leftOp, invokeExpr);
			injectedUnits.add(invokeU);
		}else{
			Unit invokeU = Jimple.v().newInvokeStmt(invokeExpr);
			injectedUnits.add(invokeU);
		}
		
		for(int i = injectedUnits.size() - 1;i >= 0;i--){
			body.getUnits().insertAfter(injectedUnits.get(i), stmt);
		}
		
		System.out.println(body.toString());
		
		injectedStmtWrapper(body, localGenerator, stmt, nextStmt);
		
		System.out.println(body);
		body.validate();
		
		int finalSize = Scene.v().getCallGraph().size();
		System.out.println("final size:"+finalSize);
	}
	
	private boolean isInterfaceType(Value value){
		try{
			String type = value.getType().toString();
			SootClass sc = Scene.v().getSootClass(type);
			
			if (sc.isInterface()){
				return true;
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}

		return false;
	}
	
}
