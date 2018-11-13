package com.cheng;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

public class NewInstanceInstrumentation extends DefaultInstrumentation {

	public NewInstanceInstrumentation(Element element) {
		super(element);
	}

	@Override
	public void instrument() {
		String targetSignature = element.getSootSignature();
		SootMethod targetMethod = Scene.v().getMethod(targetSignature);
		String callerSignature = element.getStackElements().get(2).getSootSignature();
		int callLine = element.getStackElements().get(2).getLineNumber();
		SootMethod callerMethod = Scene.v().getMethod(callerSignature);
		Body body = callerMethod.retrieveActiveBody();
		
		Stmt stmt = null;
		int count = 0;
		
		PatchingChain<Unit> units = body.getUnits();
		for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			Stmt tmpStmt = (Stmt) iter.next();
			int line = tmpStmt.getJavaSourceStartLineNumber();
			if(tmpStmt.getJavaSourceStartLineNumber() == callLine
			   && tmpStmt.containsInvokeExpr()
			   && (tmpStmt.toString().contains(GlobalValue.CLASS_NEW_INSTANCE_SIGNATURE)
				  || tmpStmt.toString().contains(GlobalValue.CONSTRUCTOR_NEW_INSTANCE_SIGNATURE))){//插桩位置
				stmt = tmpStmt;
				break;    						
			}
			count++;
		}
		if(stmt == null){//未找到newInstance语句
			return;
		}
		
		Stmt nextStmt = getNextStmt(body,stmt); 
		
		List<Value> args = new LinkedList<>();
		LocalGenerator localGenerator = new LocalGenerator(body);
		List<Unit> injectedUnits = new LinkedList<>();
		
		String targetClassName = element.getClassName();
		SootClass targetClass = Scene.v().getSootClass(targetClassName);
		
		Local local = localGenerator.generateLocal(RefType.v(element.getClassName()));
		Unit newU = Jimple.v().newAssignStmt(local, Jimple.v().newNewExpr(targetClass.getType()));
		injectedUnits.add(newU);
		
		//处理静态字段和静态代码块
		try{//如果包含静态字段及静态代码块就处理
			SootMethod cinit = targetClass.getMethod("<clinit>");
			Unit cinitCallU = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(cinit.makeRef()));
			injectedUnits.add(cinitCallU);
		}catch(Exception e){
			
		}
		
		SootMethod throughMethod = stmt.getInvokeExpr().getMethod();
		String throughClass = throughMethod.getDeclaringClass().getName();
		
		if(throughClass.equals("java.lang.Class")){//Class.newInstance()
			InvokeExpr invokeExpr = Jimple.v().newVirtualInvokeExpr(local, targetMethod.makeRef());
			Unit initU = Jimple.v().newInvokeStmt(invokeExpr);
			injectedUnits.add(initU);
		}else if(throughClass.equals("java.lang.reflect.Constructor")){//Constructor.newInstance()
			List<Type> paramTypes = targetMethod.getParameterTypes();
			
			Value arg = stmt.getInvokeExpr().getArg(0);
			/*if(null == arg || "null".equals(arg.toString())){//构造函数的参数为null
				args.add(NullConstant.v());
			}else{*/
				for(int i = 0;i < paramTypes.size();i++){
					Type type = paramTypes.get(i);
					Local local1 = localGenerator.generateLocal(RefType.v("java.lang.Object"));
					Unit assignU = Jimple.v().newAssignStmt(local1, Jimple.v().newArrayRef(arg, IntConstant.v(i)));
					injectedUnits.add(assignU);
					
					Local local2 = localGenerator.generateLocal(type);
					Unit assignU2 = Jimple.v().newAssignStmt(local2, Jimple.v().newCastExpr(local1, type));
					injectedUnits.add(assignU2);
					
					args.add(local2);
				/*}*/
			}
			
			InvokeExpr invokeExpr = Jimple.v().newVirtualInvokeExpr(local, targetMethod.makeRef(), args);
			Unit initU = Jimple.v().newInvokeStmt(invokeExpr);
			injectedUnits.add(initU);
			
			
		}
		
		//大多数情况下，对象实例化之后都会赋值给对应的引用，不排除特殊情况
		if(stmt instanceof AssignStmt){
			AssignStmt assignStmt = (AssignStmt) stmt;
			Unit invokeU = Jimple.v().newAssignStmt(assignStmt.getLeftOp(), local);
			injectedUnits.add(invokeU);
		}
		
		for (int i = injectedUnits.size()-1; i >= 0; i--)
		{
			body.getUnits().insertAfter(injectedUnits.get(i), stmt);
		}
		
		
		injectedStmtWrapper(body, localGenerator, stmt, nextStmt);
		
		System.out.println(body);
		body.validate();
	}

}
