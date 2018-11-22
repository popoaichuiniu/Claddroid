package com.popoaichuiniu.jacy.statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.popoaichuiniu.util.Util;
import soot.BooleanType;
import soot.ByteType;
import soot.Local;
import soot.NullType;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class IntentConditionGenerate extends SceneTransformer {

	List<SootMethod> callPath = null;

	public IntentConditionGenerate(List<SootMethod> callPath) {
		super();
		this.callPath = callPath;

	}



	

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {

		System.out.println("**********************************产生Intent条件啊***************************************");

		// JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();

		Map<SootMethod, List<Unit>> useMethodUnit = new HashMap<>();
		for (int i = callPath.size() - 2; i >= 0; i--) {// 假如一个方法在某个方法中被多次调用，则callgraph只有一个。context-insensitive
			List<Unit> callUnitList = new ArrayList<>();
			System.out.println("cccccccccccccccccccccc" + callPath.get(i) + "ccccccccccccccccccccccccc");
			Util.getAllUnitofCallPath(callPath.get(i), callPath.get(i + 1), callUnitList);
			System.out.println("cccccccccccccccccccccc" + callPath.get(i) + "ccccccccccccccccccccccccc");

			useMethodUnit.put(callPath.get(i), callUnitList);

		}
		File file_string=new File("string.txt");
		if(file_string.exists())
		{
			file_string.delete();
		}
		File file_sootMethod=new File("sootMethod.txt");
		
		if(file_sootMethod.exists())
		{
			file_sootMethod.delete();
		}
		
		File file_otherUnits=new File("otherUnits.txt");
		if(file_otherUnits.exists())
		{
			file_otherUnits.delete();
		}
		BufferedWriter bufferedWriterString = null;
		try {
			bufferedWriterString = new BufferedWriter(new FileWriter("string.txt", true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedWriter bufferedWriterSootMethod = null;
		try {
			bufferedWriterSootMethod= new BufferedWriter(new FileWriter("sootMethod.txt", true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedWriter bufferedWriterOtherUnit = null;
		try {
			bufferedWriterOtherUnit= new BufferedWriter(new FileWriter("otherUnits.txt", true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Map.Entry<SootMethod, List<Unit>> entry : useMethodUnit.entrySet()) {
			List<Unit> callUnitList = entry.getValue();
			SootMethod sootMethod = entry.getKey();
			try {
				bufferedWriterSootMethod.write(sootMethod.getActiveBody().toString()+"\n");
				
				bufferedWriterSootMethod.write("****************************************\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SimpleLocalDefs defs = new SimpleLocalDefs(new BriefUnitGraph(sootMethod.getActiveBody()));
			for (Unit unit : callUnitList) {
				
				if(unit instanceof DefinitionStmt)
				{
					DefinitionStmt uStmt=(DefinitionStmt) unit;
					
					Value rValue=uStmt.getRightOp();
					
					if(rValue instanceof StringConstant)
					{
						try {
							bufferedWriterString.write(unit.toString()+"************"+rValue.toString()+"\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (rValue instanceof InvokeExpr) {
						
						InvokeExpr invokeExpr=(InvokeExpr) rValue;
						
						for (Value value:invokeExpr.getArgs())
						{
							if(value instanceof StringConstant)
							{
								try {
									bufferedWriterString.write(unit.toString()+"************"+rValue.toString()+"\n");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						
					}
				}
				else if(unit instanceof InvokeStmt)
				{
					InvokeStmt invokeStmt=(InvokeStmt) unit;
					
					InvokeExpr invokeExpr=invokeStmt.getInvokeExpr();
					
					for (Value value:invokeExpr.getArgs())
					{
						if(value instanceof StringConstant)
						{
							try {
								bufferedWriterString.write(unit.toString()+"************"+value.toString()+"\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				else {
					
					try {
						bufferedWriterOtherUnit.write(unit.toString()+"******"+unit.getClass().getSimpleName()+"\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				
				
				
//				else if(unit instanceof SwitchStmt)//不可能有常量
//				{
//					try {
//						bufferedWriterString.write(unit.toString()+"\n");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
				
				
				
				
				
				///-----------------------------------路径上if条件----------------------------------------------
				if (unit instanceof IfStmt) {

					IfStmt sIfStmt = (IfStmt) unit;

					ConditionExpr condition = (ConditionExpr) sIfStmt.getCondition();

					Value opVal1 = condition.getOp1();
					Value opVal2 = condition.getOp2();

					findConditionOpValue(opVal1, sIfStmt, sootMethod, defs);

				}
			}

		}
		try {
			bufferedWriterString.close();
			bufferedWriterSootMethod.close();
			bufferedWriterOtherUnit.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("**********************************产生Intent条件啊***************************************");

		// TODO Auto-generated method stub

	}

	private void findConditionOpValue(Value opVal1, IfStmt sIfStmt, SootMethod sootMethod, SimpleLocalDefs defs) {
		// TODO Auto-generated method stub
		BufferedWriter bufferedWriter_boolean = null;
		try {
			bufferedWriter_boolean = new BufferedWriter(new FileWriter("if_boolean.txt", true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedWriter bufferedWriter_bundle = null;
		try {
			bufferedWriter_bundle = new BufferedWriter(new FileWriter("if_bundle.txt", true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (opVal1.getType() instanceof BooleanType) {
			if (opVal1 instanceof Local) {
				Local local = (Local) opVal1;
				List<Unit> units = defs.getDefsOfAt(local, sIfStmt);
				assert units.size() == 1;

				Unit unit = units.get(0);
				if (unit instanceof DefinitionStmt) {
					DefinitionStmt defStmt = (DefinitionStmt) unit;
					if (defStmt.getRightOp() instanceof JVirtualInvokeExpr) {
						JVirtualInvokeExpr jviExpr = (JVirtualInvokeExpr) defStmt.getRightOp();
						
						
						
						if (jviExpr.getMethod().getName().equals("equals")
								&& jviExpr.getMethod().getDeclaringClass().getName().equals("java.lang.String")) {

							// findOriginalVal(sootMethod, defs,defStmt,jviExpr.getBase());
							// findOriginalVal(sootMethod, defs,defStmt,jviExpr.getArg(0));

							try {
								bufferedWriter_boolean.write(defStmt.toString() + "**********" + sIfStmt.toString() + "\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (Pattern.matches("hasFileDescriptors", jviExpr.getMethod().getName())) {

							try {
								bufferedWriter_boolean.write(defStmt.toString() + "**********" + sIfStmt.toString() + "\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						if (Pattern.matches("getBooleanExtra", jviExpr.getMethod().getName())) {

							try {
								bufferedWriter_boolean.write(defStmt.toString() + "**********" + sIfStmt.toString() + "\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						if (Pattern.matches("hasExtra", jviExpr.getMethod().getName())) {

							// findOriginalVal(sootMethod, defs,defStmt,jviExpr.getBase());
							// findOriginalVal(sootMethod, defs,defStmt,jviExpr.getArg(0));

							try {
								bufferedWriter_boolean.write(defStmt.toString() + "**********" + sIfStmt.toString() + "\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

						if (Pattern.matches("hasCategory", jviExpr.getMethod().getName())) {

							// findOriginalVal(sootMethod, defs,defStmt,jviExpr.getBase());
							// findOriginalVal(sootMethod, defs,defStmt,jviExpr.getArg(0));

							try {
								bufferedWriter_boolean.write(defStmt.toString() + "**********" + sIfStmt.toString() + "\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}

			}
		} else if (opVal1.getType() instanceof NullType) {

		}

		else if (opVal1.getType() instanceof ByteType) {

		}

		try {
			bufferedWriter_boolean.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Object findOriginalVal(SootMethod sootMethod, SimpleLocalDefs defs, DefinitionStmt defStmt, Value base) {
		// TODO Auto-generated method stub
		return null;
	}

	public void directCall() {
		internalTransform(null, null);
	}
//	public Set<String> handleIfStmt(int tabs, IfStmt currIfStmt, Unit succUnit, SootMethod method, SimpleLocalDefs defs, Set<String> decls, List<Unit> currPath) {
//		//exceptionLogger.debug("currUnit: " + currIfStmt);
//		String returnExpr = "";
//		String opVal1Assert = null;
//		String opVal2Assert = null;
//
//		Unit opVal1DefUnit = null;
//		Unit opVal2DefUnit = null;
//
//		ConditionExpr condition = (ConditionExpr) currIfStmt.getCondition();
////		exceptionLogger.debug(Utils.createTabsStr(tabs) + "Handling if stmt: " + currIfStmt);
////		exceptionLogger.debug(Utils.createTabsStr(tabs) + "\thandling symbol: " + condition.getSymbol());
//		Value opVal1 = condition.getOp1();
//		Value opVal2 = condition.getOp2();
//
//		Value opVal1Org = opVal1;
//		Value opVal2Org = opVal2;
//
//		boolean generateCondExpr = true;
//		if (opVal1.getType() instanceof ByteType) {//ByteType bundle
//			exceptionLogger.debug("opVal1.getType() instanceof ByteType");
//			Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> condValuesPair = findLeftAndRightValuesOfByteVal(method, defs, currIfStmt, opVal1, currPath);
//			Quartet<Value, String, String, Unit> left = condValuesPair.getValue0();
//			Quartet<Value, String, String, Unit> right = condValuesPair.getValue1();
//			opVal1 = left.getValue0();
//			opVal2 = right.getValue0();
//			if (left.getValue1() != null)
//				decls.add(left.getValue1());
//			if (right.getValue1() != null)
//				decls.add(right.getValue1());
//			if (left.getValue2() != null) {
//				opVal1Assert = left.getValue2();
//			}
//			if (right.getValue2() != null) {
//				opVal2Assert = right.getValue2();
//			}
//			opVal1DefUnit = left.getValue3();
//			opVal2DefUnit = right.getValue3();
//		} else if (opVal1.getType() instanceof BooleanType) {
//			//exceptionLogger.debug("opVal1.getType() instanceof BooleanType");
//			Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> condValuesPair = findStringValuesOfBoolType(method, defs, currIfStmt, opVal1, currPath);
//			Quartet<Value, String, String, Unit> left = condValuesPair.getValue0();
//			Quartet<Value, String, String, Unit> right = condValuesPair.getValue1();
//
//			if (left == null) {
//				condValuesPair = findBundleValues(method, defs, currIfStmt, opVal1, currPath);
//				left = condValuesPair.getValue0();
//				right = condValuesPair.getValue1();
//
//				if (left!= null || right!=null) {
//					generateCondExpr = false;
//				}
//
//				if (left!=null) {
//					opVal1 = left.getValue0();
//				}
//				if (right!=null) {
//					opVal2 = right.getValue0();
//				}
//				AssignOpVals assignOpVals = new AssignOpVals(decls, opVal1Assert, opVal2Assert, opVal1, opVal2, left, right).invoke();
//				opVal1DefUnit = assignOpVals.getOpVal1DefUnit();
//				opVal2DefUnit = assignOpVals.getOpVal2DefUnit();
//				opVal1Assert = assignOpVals.getOpVal1Assert();
//				opVal2Assert = assignOpVals.getOpVal2Assert();
//			}
//
//			if (left != null && right != null) {
//				if (left.getValue0() == null && right.getValue0() == null) {
//					findKeysForLeftAndRightValues(currIfStmt, opVal1, opVal2, defs, currPath);
//				} else {
//					opVal1 = left.getValue0();
//					opVal2 = right.getValue0();
//					AssignOpVals assignOpVals = new AssignOpVals(decls, opVal1Assert, opVal2Assert, opVal1, opVal2, left, right).invoke();
//					opVal1DefUnit = assignOpVals.getOpVal1DefUnit();
//					opVal2DefUnit = assignOpVals.getOpVal2DefUnit();
//					opVal1Assert = assignOpVals.getOpVal1Assert();
//					opVal2Assert = assignOpVals.getOpVal2Assert();
//				}
//			}
//
//			if (left == null && right == null) {
//				condValuesPair = findCategories(method, defs, currIfStmt, opVal1, currPath);
//				left = condValuesPair.getValue0();
//				right = condValuesPair.getValue1();
//
//				if (left!= null || right!=null) {
//					generateCondExpr = false;
//				}
//
//				if (left!=null) {
//					opVal1 = left.getValue0();
//				}
//				if (right!=null) {
//					opVal2 = right.getValue0();
//				}
//				AssignOpVals assignOpVals = new AssignOpVals(decls, opVal1Assert, opVal2Assert, opVal1, opVal2, left, right).invoke();
//				opVal1DefUnit = assignOpVals.getOpVal1DefUnit();
//				opVal2DefUnit = assignOpVals.getOpVal2DefUnit();
//				opVal1Assert = assignOpVals.getOpVal1Assert();
//				opVal2Assert = assignOpVals.getOpVal2Assert();
//			}
//		} else {
//			exceptionLogger.debug("else branch, simply invoking findKeysForLeftAndRightValues(...)");
//			findKeysForLeftAndRightValues(currIfStmt, opVal1, opVal2, defs, currPath);
//			opVal1DefUnit = getDefOfValInPath(opVal1, currIfStmt, currPath, defs);
//			opVal2DefUnit = getDefOfValInPath(opVal2, currIfStmt, currPath, defs);
//		}
//
//		Set<String> returnExprs = new LinkedHashSet<String>();
//		if (opVal1DefUnit == null && opVal2DefUnit == null && opVal1Assert == null && opVal2Assert == null) {
//			exceptionLogger.debug("No new information from this if stmt, so returning empty set of expressions");
//			return returnExprs;
//		}
//
//		String opExpr1 = null;
//		String opExpr2 = null;
//		try {
//			if (opVal1 == null) {
//				exceptionLogger.debug("Could not resolve opVal1, so setting it to true");
//				opExpr1 = "";
//			} else {
//				opExpr1 = createZ3Expr(opVal1, currIfStmt, opVal1DefUnit, method, decls, tabs);
//			}
//
//			if (opVal2 == null) {
//				exceptionLogger.debug("Could not resolve opVal2, so setting it to true");
//				opExpr2 = "";
//			} else {
//				opExpr2 = createZ3Expr(opVal2, currIfStmt, opVal2DefUnit, method, decls, tabs);
//			}
//		} catch (RuntimeException e) {
//			exceptionLogger.warn("caught exceptionLogger: ", e);
//			return null;
//		}
//
//		if (opExpr1 == opExpr2 && opExpr1 == null) {
//			exceptionLogger.debug("op1 and op2 are both null, so just returning true expression");
//			return Collections.singleton(returnExpr);
//		}
//
//		// if the curr unit to convert is an ifStmt ensure the symbol is not negated
//		boolean isFallThrough = isFallThrough(currIfStmt, succUnit);
//
//		String branchSensitiveSymbol = null;
//		if (isFallThrough) {
//			if (opVal1Org.getType() instanceof BooleanType) {
//				branchSensitiveSymbol = condition.getSymbol();
//			} else {
//				branchSensitiveSymbol = negateSymbol(condition.getSymbol());
//			}
//		} else {
//			if (opVal1Org.getType() instanceof BooleanType) {
//				branchSensitiveSymbol = negateSymbol(condition.getSymbol());
//			} else {
//				branchSensitiveSymbol = condition.getSymbol();
//			}
//		}
//
//		if (opVal1Assert != null) {
//			if (opVal1Assert.contains("select keys index") && opVal2Assert == null) { // handling a hasExtra statement, so do not create additional expressions
//				generateCondExpr = false;
//			}
//		}
//
//
//		if (generateCondExpr) {
//			returnExpr = buildZ3CondExpr(tabs, opExpr1, opExpr2, branchSensitiveSymbol);
//			returnExprs.add(returnExpr);
//		}
//		if (opVal1Assert != null) {
//			returnExprs.add(opVal1Assert);
//		}
//		if (opVal2Assert != null) {
//			returnExprs.add(opVal2Assert);
//		}
//		return returnExprs;
//	}
//	public Pair<Quartet<Value,String,String,Unit>,Quartet<Value,String,String,Unit>> findLeftAndRightValuesOfByteVal(SootMethod method, SimpleLocalDefs defs, Unit inUnit, Value value, List<Unit> currPath) {
//		Quartet<Value,String,String,Unit> leftVal = null;
//		Quartet<Value,String,String,Unit> rightVal = null;
//		if (value instanceof Local) {
//			Local local = (Local)value;
//			if (local.getType() instanceof ByteType) {
//				List<Unit> potentialCmpUnits = defs.getDefsOfAt(local,inUnit);
//				for (Unit potentialCmpUnit : potentialCmpUnits) {
//					/*if (!currPath.contains(potentialCmpUnit)) {
//						continue;
//					}*/
//					if (!isDefInPathAndLatest(currPath,potentialCmpUnit,local,inUnit,defs)) {
//						continue;
//					}
//					if (potentialCmpUnit.toString().contains("cmp")) {
//						exceptionLogger.debug("Found potential cmp* statement: " + potentialCmpUnit);
//						if (potentialCmpUnit instanceof DefinitionStmt) {
//							DefinitionStmt defStmt = (DefinitionStmt)potentialCmpUnit;
//							Value rightOp = defStmt.getRightOp();
//							if (rightOp instanceof AbstractJimpleIntBinopExpr) {
//								AbstractJimpleIntBinopExpr cmpExpr = (AbstractJimpleIntBinopExpr)rightOp;
//								leftVal = findOriginalVal(method, defs, potentialCmpUnit, cmpExpr.getOp1(), currPath);
//								rightVal = findOriginalVal(method, defs, potentialCmpUnit, cmpExpr.getOp2(), currPath);
//							}
//						}
//					}
//				}
//			}
//		}
//		return new Pair<Quartet<Value,String,String,Unit>,Quartet<Value,String,String,Unit>>(leftVal,rightVal);
//	}

}
