package com.cheng;

import java.util.Iterator;

import lu.uni.snt.droidra.booster.Alteration;
import soot.Body;
import soot.IntType;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;

public abstract class DefaultInstrumentation {
	protected Element element;
	
	public DefaultInstrumentation(Element element){
		this.element = element;
		Alteration.v().init();
	}
	
	public abstract void instrument();

	public Stmt getNextStmt(Body body, Stmt stmt)
	{
		PatchingChain<Unit> units = body.getUnits();
		
		for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
		{
			Stmt s = (Stmt) iterU.next();
			
			if (s.equals(stmt))
			{
				if (iterU.hasNext())
					return (Stmt) iterU.next();
			}
			
		}
		
		return null;
	}
	
	public void injectedStmtWrapper(Body body, LocalGenerator localGenerator, Stmt stmt, Stmt nextStmt){
		Local opaqueLocal = localGenerator.generateLocal(IntType.v());
		Unit assignU = Jimple.v().newAssignStmt(opaqueLocal, Jimple.v().newStaticInvokeExpr(Alteration.v().getTryMethod().makeRef(), IntConstant.v(0)));
		Unit ifU = Jimple.v().newIfStmt(Jimple.v().newEqExpr(IntConstant.v(1), opaqueLocal), nextStmt);

		body.getUnits().insertAfter(ifU, stmt);
		body.getUnits().insertAfter(assignU, stmt);
	}
}
