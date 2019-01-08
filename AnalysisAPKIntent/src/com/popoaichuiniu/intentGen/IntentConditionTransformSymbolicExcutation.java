package com.popoaichuiniu.intentGen;

import com.microsoft.z3.Z3Exception;
import com.popoaichuiniu.experiment.SEUnHandleProcessSatistic;
import com.popoaichuiniu.jacy.statistic.AndroidCallGraph;

import com.popoaichuiniu.jacy.statistic.AndroidCallGraphProxy;
import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import org.javatuples.Pair;
import org.javatuples.Quartet;

import org.javatuples.Triplet;

import soot.*;
import soot.jimple.*;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.internal.AbstractJimpleIntBinopExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IntentConditionTransformSymbolicExcutation extends SceneTransformer {

    private static boolean exeModelTest = Config.isTest;


    private boolean pathLimitEnabled = true;

    private int enterBranchLimit = 10;//default=13

    private boolean hasReachBranchLimit = false;


    private boolean callGraphLimitEnabled = true;

    private int callgraphEnterBranchLimit = Integer.MAX_VALUE;//

    private boolean hasReachCallGraphBranchLimit = false;


    public String appPath = null;

    Set<Triplet<Integer, String, String>> targets = null;


    private static WriteFile ifReducedWriter = null;


    private static WriteFile writeFileCallGraphSize = null;

    private static WriteFile appUnitGraphPathReducedReachLimit = null;


    private HashSet<Intent> allIntentConditionOfOneApp = new HashSet<>();


    private Set<IntentUnit> ultiIntentSet = new HashSet<>();


    String packageName = null;
    Map<String, AXmlNode> eas = null;


    /**
     * key:extraDataSymbol,value:key  ///   xxx=ie.ge.*("ttt“),ie有可能是bundle或者intent
     */

    private Map<String, String> extraDataMapKey = new HashMap<>();

    /**
     * key: a symbol used to represent a Local, value: the Local represented by the symbol
     */
    private Map<String, Local> symbolLocalMap = new HashMap<String, Local>();


    /**
     * key: a symbol used to represent a Local, value: the defunit of the local
     */
    private Map<String, Unit> symbolDefUnitMap = new HashMap<String, Unit>();

    /**
     * key: a symbol used to represent a Local, value: the Local represented by the symbol
     */
    private Map<Local, String> localSymbolMap = new HashMap<Local, String>();


    private Map<String, String> extraFromMap = new HashMap<>();//key:extra   value:extra 从哪里得到, intent还是bundle


    Set<Pair<Unit, SootMethod>> infeasibleTargets = new LinkedHashSet<Pair<Unit, SootMethod>>();


    Map<List<Unit>, Intent> pathIntents = new LinkedHashMap<List<Unit>, Intent>();


    protected Map<SootMethod, SimpleLocalDefs> sootMethodSimpleLocalDefsMap = new HashMap<>();


    private boolean isAnalysis = false;

    private SEUnHandleProcessSatistic seUnHandleProcessSatistic = null;


    public IntentConditionTransformSymbolicExcutation(String apkFilePath) {


        appPath = apkFilePath;
        seUnHandleProcessSatistic = new SEUnHandleProcessSatistic(appPath);

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(apkFilePath + "_" + "UnitsNeedAnalysis.txt")));

            String line = null;
            targets = new LinkedHashSet<Triplet<Integer, String, String>>();
            while ((line = bufferedReader.readLine()) != null) {
                String[] strs = line.split("#");
                String sootMethodString = strs[0];
                String byteCode = strs[1];
                int intByteCode = Integer.parseInt(byteCode);
                String unitString = strs[2];
                targets.add(new Triplet<>(intByteCode, sootMethodString, unitString));

            }

            bufferedReader.close();
        } catch (IOException e) {


            exceptionLogger.error(appPath + "&&" + "UnitsNeedAnalysis.txt出错");


        }


        if (targets == null || targets.size() == 0) {//没有待分析的目标API
            isAnalysis = false;
        } else {
            isAnalysis = true;
        }


    }

    protected static Logger exceptionLogger = new MyLogger(Config.intentConditionSymbolicExcutationResults, "exceptionLogger").getLogger();

    protected static Logger infoLogger = new MyLogger(Config.intentConditionSymbolicExcutationResults, "info").getLogger();

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        if (!isAnalysis) {//要分析吗？
            return;
        }

        AndroidCallGraph androidCallGraph = new AndroidCallGraphProxy(appPath, Config.androidJar, exceptionLogger).androidCallGraph;
        AndroidInfo androidInfo = new AndroidInfo(appPath, exceptionLogger);
        packageName = androidInfo.getPackageName(appPath);
        eas = androidInfo.getEAs();
        List<SootMethod> ea_entryPoints = Util.getEA_EntryPoints(androidCallGraph, androidInfo);
        List<SootMethod> roMethods = Util.getMethodsInReverseTopologicalOrder(ea_entryPoints, androidCallGraph.getCg());
        roMethods.add(androidCallGraph.getEntryPoint());
//        for(SootMethod sootMethod:roMethods)
//        {
////            if(sootMethod.getBytecodeSignature().contains("iccMain"))
////            {
////                System.out.println(sootMethod);//ok
////            }
//
//              if(sootMethod.getBytecodeSignature().contains("zms1250"))
//              {
//                  System.out.println(sootMethod);//ok
//              }
//        }


        //testInitial(ea_entryPoints, roMethods, Scene.v().getApplicationClasses(), appPath,exceptionLogger);//ok


        try {


            for (SootMethod sootMethod : roMethods) {


                analysisSootMethod(sootMethod, androidCallGraph.getCg(), ea_entryPoints, roMethods);


            }

        } catch (Exception e) {

            exceptionLogger.error(appPath + "&&" + "exception" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e));


        }

        saveIntent(allIntentConditionOfOneApp, appPath);//所有intent结果（startPoint到tgtAPI）

        //最终intent结果
        WriteFile writeFile_intent_ulti = new WriteFile(Config.intent_ulti_path + "/" + new File(appPath).getName() + ".txt", false, exceptionLogger);

        ultiIntentSet = preProcess(ultiIntentSet);//将intent的num值和string处理

        List<IntentInfo> intentInfoList = new ArrayList<>();
        for (IntentUnit intentUnit : ultiIntentSet) {
            Stmt stmt = (Stmt) intentUnit.unit;
            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            if (invokeExpr == null) {
                throw new RuntimeException("intent unit do not contain invoke!");
            } else {

                intentInfoList.add(getIntentInfo(intentUnit));
                writeFile_intent_ulti.writeStr(intentUnit.intent + "%%%" + intentUnit.unit + "&*" + intentUnit.unit.getTag("BytecodeOffsetTag") + "%%%" + invokeExpr.getMethod().getBytecodeSignature() + "\n");
            }

        }

        writeFile_intent_ulti.close();

        IntentInfoFileGenerate.generateIntentInfoFile(appPath, intentInfoList);//产生test-app读取的测试用例文件

        seUnHandleProcessSatistic.saveData();


    }

    public static Set<IntentUnit> preProcess(Set<IntentUnit> ultiIntentSet) {
        Set<IntentUnit> newIntentUnitSet = new HashSet<>();
        for (IntentUnit intentUnit : ultiIntentSet) {
            //extra
            Set<IntentExtraKey> intentExtraKeySet = intentUnit.intent.myExtras;
            List<Set<IntentExtraKey>> intentExtraList = new ArrayList<>();
            if (intentExtraKeySet != null && intentExtraKeySet.size() != 0) {
                Map<IntentExtraKey, Set<IntentExtraValue>> map = new HashMap<>();
                for (IntentExtraKey intentExtraKey : intentExtraKeySet) {

                    if (isNumberType(intentExtraKey)) {
                        Set<IntentExtraValue> numValueSet = new HashSet<>();
                        String numbers[] = intentExtraKey.value.split("##");
                        for (String num : numbers) {
                            numValueSet.add(new IntentExtraValue(intentExtraKey.key, intentExtraKey.type, num));
                        }
                        map.put(intentExtraKey, numValueSet);
                    } else if (intentExtraKey.type.equals("java.lang.String")) {
                        Set<IntentExtraValue> stringValueSet = new HashSet<>();
                        IntentExtraValue intentExtraValueStr = new IntentExtraValue(intentExtraKey);
                        intentExtraValueStr.value = getStringValueOfIntent(intentExtraValueStr.value);
                        stringValueSet.add(intentExtraValueStr);
                        map.put(intentExtraKey, stringValueSet);
                    } else {
                        Set<IntentExtraValue> intentExtraValueSetOther = new HashSet<>();
                        intentExtraValueSetOther.add(new IntentExtraValue(intentExtraKey));
                        map.put(intentExtraKey, intentExtraValueSetOther);
                    }


                }

                List<Set<IntentExtraValue>> listIntentExtraValueSet = new ArrayList<>();
                for (Map.Entry<IntentExtraKey, Set<IntentExtraValue>> entry : map.entrySet()) {

                    listIntentExtraValueSet.add(entry.getValue());
                }

                selectIntentExtraKeyNew(intentExtraList, new HashSet<>(), listIntentExtraValueSet);
            }


            //action

            String action = getStringValueOfIntent(intentUnit.intent.action);


            //categories

            Set<String> categories = intentUnit.intent.categories;


            if (intentExtraList.size() == 0) {
                Intent newIntent = new Intent(intentUnit.intent);
                newIntent.action = action;
                newIntent.categories = categories;

                IntentUnit newIntentUnit = new IntentUnit(intentUnit);
                newIntentUnit.intent = newIntent;
                newIntentUnitSet.add(newIntentUnit);
            } else {
                for (Set<IntentExtraKey> oneIntentExtra : intentExtraList) {
                    Intent newIntent = new Intent(intentUnit.intent);
                    newIntent.action = action;
                    newIntent.categories = categories;
                    newIntent.myExtras = oneIntentExtra;

                    IntentUnit newIntentUnit = new IntentUnit(intentUnit);
                    newIntentUnit.intent = newIntent;
                    newIntentUnitSet.add(newIntentUnit);


                }
            }


        }

        return newIntentUnitSet;

    }

    private static void selectIntentExtraKeyNew(List<Set<IntentExtraKey>> intentExtraList, Set<IntentExtraKey> oneIntentExtra, List<Set<IntentExtraValue>> list) {


        if (oneIntentExtra.size() == list.size()) {
            intentExtraList.add(oneIntentExtra);
            return;
        }

        for (IntentExtraValue intentExtraValue : list.get(oneIntentExtra.size())) {

            Set<IntentExtraKey> oneIntentExtraCopy = new HashSet<>(oneIntentExtra);

            oneIntentExtraCopy.add(new IntentExtraKey(intentExtraValue));

            selectIntentExtraKeyNew(intentExtraList, oneIntentExtraCopy, list);
        }
    }

    private IntentInfo getIntentInfo(IntentUnit intentUnit) {

        IntentInfo intentInfo = new IntentInfo();
        intentInfo.appPath = appPath;
        intentInfo.appPackageName = packageName;
        intentInfo.comPonentType = intentUnit.comPonentType;
        intentInfo.comPonentName = intentUnit.comPonentName;

        intentInfo.comPonentAction = intentUnit.intent.action;
        intentInfo.comPonentCategory.addAll(intentUnit.intent.categories);
        intentInfo.comPonentExtraData.addAll(intentUnit.intent.myExtras);


        return intentInfo;


    }

    public static String getStringValueOfIntent(String strValue) {
        if (strValue != null) {
            if (strValue.contains("!0!")) {
                strValue = "!0!";
            } else if (strValue.contains("##")) {
                String[] acs = strValue.split("##");
                String temp = null;
                for (String str : acs) {
                    if (!str.startsWith("ZMS!")) {
                        temp = str;
                        break;
                    }
                }
                if (temp == null) {
                    strValue = acs[0];
                } else {
                    strValue = temp;
                }
            }
        }
        return strValue;
    }


    private void analysisSootMethod(SootMethod sootMethod, CallGraph cg, List<SootMethod> ea_entryPoints, List<SootMethod> roMethods) {


        if (exeModelTest) {//测试


            //test
            //××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××
//            if (sootMethod.getBytecodeSignature().contains("onCreate")) {
//                HashSet<Unit> unitsSet = new HashSet<>(sootMethod.getActiveBody().getUnits());
//                assert unitsSet.size() == sootMethod.getActiveBody().getUnits().size();//
//
//                HashSet<Unit> xxx=new HashSet<>();
//                HashSet<Value> yyy=new HashSet<>();
//                HashSet<Value> zzz=new HashSet<>();
//                for(Unit unit:sootMethod.getActiveBody().getUnits())//每个unit都是唯一的,都是不同实例，即使unit.toString()相同
//                {
//                    if(unit.toString().contains("specialinvoke $r0.<com.example.lab418.testwebview2.TestUnUsedIFBlockAlgorithm: void test6(android.content.Intent)>($r2)"))
//                    {
//                        xxx.add(unit);
//                    }
//
//                    Stmt stmt= (Stmt) unit;
//                    if(stmt.containsInvokeExpr())
//                    {
//
//                        InvokeExpr invokeExpr=stmt.getInvokeExpr();
//
//                        if(invokeExpr instanceof InstanceInvokeExpr)
//                        {
//                            InstanceInvokeExpr instanceInvokeExpr= (InstanceInvokeExpr) invokeExpr;
//                            yyy.add(instanceInvokeExpr.getBase());//每个变量在整个方法内是同一个一实例，
//                            // 但是表达式每一个都是唯一的。即使其toString()相同的。
//                        }
//
//                    }
//                    if(stmt instanceof DefinitionStmt)
//                    {
//                        zzz.add(((DefinitionStmt) stmt).getRightOp());
//                    }
//
//
//                }
//
//                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(xxx);
//                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(yyy);
//                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(zzz);
//
//
//
//            }

            //××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××


        }


        Body body = sootMethod.getActiveBody();

        if (body != null) {
            PatchingChain<Unit> units = body.getUnits();

            if (!exeModelTest) {


                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(sootMethod.getBytecodeSignature());

                for (Unit unit : units) {


                    boolean isNeedAnalysis = Util.unitNeedAnalysis(unit, sootMethod, targets);


                    if (isNeedAnalysis) {

                        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(unit.toString());
                        findEAToTargetUnit(sootMethod, cg, unit, ea_entryPoints, roMethods);


                    }
                }
            } else //测试
            {


//                if(!sootMethod.getBytecodeSignature().contains("onCreate"))
//                {
//                    return;
//                }
//                for(Iterator<Edge> iterator=cg.edgesOutOf(sootMethod);iterator.hasNext();)
//
//                {
//                    Edge edge=iterator.next();
//
//                    SootMethod tgtSootMethod=edge.tgt();
//
//
//                    System.out.println(tgtSootMethod.getBytecodeSignature());
//                }


                for (Unit unit : units) {


                    if (unit.toString().contains("sendTextMessage")) {

                        findEAToTargetUnit(sootMethod, cg, unit, ea_entryPoints, roMethods);
                    }


                }


            }


        }


    }

    private void findEAToTargetUnit(SootMethod sootMethod, CallGraph cg, Unit unit, List<SootMethod> ea_entryPoints, List<SootMethod> roMethods) {

        Set<SootMethod> aboutUnitNeedMethods = new HashSet<SootMethod>();

        Util.getAllMethodsToThisSootMethod(sootMethod, cg, aboutUnitNeedMethods);


        HashSet<SootMethod> allSootMethodsInPathOfEAToTarget = new HashSet<>(roMethods);


        allSootMethodsInPathOfEAToTarget.retainAll(aboutUnitNeedMethods);//ok  sootMethod中unit到startPoint的所有连通点


        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("MyCallGraph初始化开始！");
        MyCallGraph myCallGraph = new MyCallGraph(allSootMethodsInPathOfEAToTarget, cg, sootMethod, unit, this);
        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("MyCallGraph初始化完成！");
        writeFileCallGraphSize.writeStr("myCallGraph_P:" + "方法数：" + myCallGraph.allMethods.size() + " " + "边数：" + myCallGraph.allEdges.size() + " " + new File(appPath).getName() + "\n");
        writeFileCallGraphSize.flush();
        //myCallGraph.exportGexf(new File(appPath).getName());

        writeFileCallGraphSize.writeStr("开始求callgraph所有路径" + " " + new File(appPath).getName() + "\n");
        writeFileCallGraphSize.flush();
        myCallGraphReducedAndAllPathGet(myCallGraph);
        writeFileCallGraphSize.writeStr("callgraph所有路径求解完毕！" + " " + new File(appPath).getName() + "\n");
        writeFileCallGraphSize.flush();


    }

    Set<SootMethod> methodsActionConflict;

    Map<String, Set<SootMethod>> methodsCategoryConflict = new HashMap<>();
    Map<IntentExtraKey, Set<SootMethod>> methodsExtraConflict = new HashMap<>();

    private void myCallGraphReducedAndAllPathGet(MyCallGraph myCallGraph) {
//        myCallGraph.reduced(allSootMethodsAllUnitsTargetUnitInMethodInfo);
//        myCallGraph.exportGexf("reduced_" + new File(appPath).getName());


//        writeFileCallGraphSize.writeStr("myCallGraph_R:" + myCallGraph.allMethods.size() + " " + myCallGraph.allEdges.size() + " " + new File(appPath).getName() + "\n");
//        writeFileCallGraphSize.flush();

        Map<SootMethod, Map<Unit, Set<Intent>>> sootMethodSetIntentCondition = new HashMap<>();

        HashSet<IntentUnitSootMethod> intentUnitSootMethodHashSet = new HashSet<>();

        for (Map.Entry<SootMethod, Set<MyPairUnitToEdge>> entry : myCallGraph.targetUnitInSootMethod.entrySet()) {
            SootMethod oneSootMethod = entry.getKey();
            Set<MyPairUnitToEdge> myPairUnitToEdgeSet = entry.getValue();


            HashMap<Unit, Set<Intent>> singleUnitIntentSetMap = new HashMap<>();
            for (MyPairUnitToEdge myPairUnitToEdge : myPairUnitToEdgeSet) {
                HashSet<Intent> hashSet = new HashSet<>();
                Map<Unit, TargetUnitInMethodInfo> sootMethodInfo = allSootMethodsAllUnitsTargetUnitInMethodInfo.get(oneSootMethod);
                if (sootMethodInfo == null) {
                    continue;
                    //throw new RuntimeException("找不到"+oneSootMethod+"allSootMethodsAllUnitsTargetUnitInMethodInfo");
                }

                if (myPairUnitToEdge.srcUnit == null) {
                    continue;
                    //throw new RuntimeException("找不到"+oneSootMethod+"中边的调用语句！");
                }

                TargetUnitInMethodInfo targetUnitInMethodInfo = sootMethodInfo.get(myPairUnitToEdge.srcUnit);
                if (targetUnitInMethodInfo == null) {

                    throw new RuntimeException("找不到" + oneSootMethod + "中边的调用语句的targetUnitInMethodInfo！");
                }

                if (targetUnitInMethodInfo.unitPaths == null || targetUnitInMethodInfo.unitPaths.size() == 0) {
                    throw new RuntimeException(oneSootMethod + "中到调用语句的路径为空");
                }

                for (IntentConditionTransformSymbolicExcutation.UnitPath unitPath : targetUnitInMethodInfo.unitPaths) {
                    if (unitPath.intentSoln.isFeasible) {//可行的路径的intent

                        hashSet.add(unitPath.intentSoln.intent);

                        intentUnitSootMethodHashSet.add(new IntentUnitSootMethod(unitPath.intentSoln.intent, myPairUnitToEdge.srcUnit, oneSootMethod));

                    }
                }
                if (hashSet.size() == 0) {//说明没有一条路径可行的


                }
                singleUnitIntentSetMap.put(myPairUnitToEdge.srcUnit, hashSet);

            }

            sootMethodSetIntentCondition.put(oneSootMethod, singleUnitIntentSetMap);//the intent in this map is not null


        }

        //获得冲突intent//////////////////////////////////////////////////////////////////////////////////////////////////////////////

//        long startTime=System.nanoTime();
//        Map<String, Set<IntentUnitSootMethod>> actionMap = new HashMap<>();//action=null 表示action没有要求
//
//        Map<String, Set<IntentUnitSootMethod>> categoryMap = new HashMap<>();
//
//        Map<IntentExtraKey, Set<IntentUnitSootMethod>> extraMap = new HashMap<>();
//
//
//        for (IntentUnitSootMethod intentUnitSootMethod : intentUnitSootMethodHashSet) {
//
//            Set<IntentUnitSootMethod> actionSet = actionMap.get(intentUnitSootMethod.intent.action);
//            if (actionSet == null) {
//                actionSet = new HashSet<>();
//
//            }
//            actionSet.add(intentUnitSootMethod);
//
//            actionMap.put(intentUnitSootMethod.intent.action, actionSet);
//
//
//            for (String cate : intentUnitSootMethod.intent.categories) {
//
//                Set<IntentUnitSootMethod> categorySet = categoryMap.get(cate.replace("ZMS!", ""));
//
//                if (categorySet == null) {
//                    categorySet = new HashSet<>();
//                }
//                categorySet.add(intentUnitSootMethod);
//
//                categoryMap.put(cate.replace("ZMS!", ""), categorySet);
//            }
//
//
//            for (IntentExtraKey intentExtraKey : intentUnitSootMethod.intent.myExtras) {
//
//                Set<IntentUnitSootMethod> extraSet = extraMap.get(intentExtraKey);
//
//                if (extraSet == null) {
//                    extraSet = new HashSet<>();
//                }
//
//                extraSet.add(intentUnitSootMethod);
//
//                extraMap.put(intentExtraKey, extraSet);
//            }
//
//        }
//
//        Set<IntentUnitSootMethod> actionConflictIntentSet = new HashSet<>();
//
//        for (Map.Entry<String, Set<IntentUnitSootMethod>> entry : actionMap.entrySet()) {
//
//            if (entry.getKey() != null) {
//                actionConflictIntentSet.addAll(entry.getValue());
//            }
//        }
//
//
//        Map<String, Set<IntentUnitSootMethod>> categoryConflictIntentMap = new HashMap<>();
//        for (Map.Entry<String, Set<IntentUnitSootMethod>> entry : categoryMap.entrySet())//如果存在一个intent含有"ZMS！entry.getKey()",则存在冲突
//        {
//            Set<IntentUnitSootMethod> categoryConflictIntentSet = new HashSet<>();
//            for (IntentUnitSootMethod intentUnitSootMethod : entry.getValue()) {
//                for (String cate : intentUnitSootMethod.intent.categories) {
//                    if (cate.contains(entry.getKey()) && cate.startsWith("ZMS!")) {
//                        categoryConflictIntentSet.addAll(entry.getValue());
//                        categoryConflictIntentMap.put(entry.getKey(), categoryConflictIntentSet);
//                    }
//                }
//            }
//        }
//
//
//        Map<IntentExtraKey, Set<IntentUnitSootMethod>> extraConflictIntentMap = new HashMap<>();
//
//
//        for (Map.Entry<IntentExtraKey, Set<IntentUnitSootMethod>> entry : extraMap.entrySet()) {
//
//            if (entry.getValue().size() >= 2) {
//                if (isNumberType(entry.getKey())) {
//                    continue;//都是直接合并，不会产生冲突
//                }
//
//                extraConflictIntentMap.put(entry.getKey(), entry.getValue());
//
//            }
//        }
//
//
//        methodsActionConflict = getAllIntentUnitSootMethodReachSet(actionConflictIntentSet, myCallGraph);
//
//        for (Map.Entry<String, Set<IntentUnitSootMethod>> entry : categoryConflictIntentMap.entrySet()) {
//            methodsCategoryConflict.put(entry.getKey(), getAllIntentUnitSootMethodReachSet(entry.getValue(), myCallGraph));
//        }
//
//        for (Map.Entry<IntentExtraKey, Set<IntentUnitSootMethod>> entry : extraConflictIntentMap.entrySet()) {
//            methodsExtraConflict.put(entry.getKey(), getAllIntentUnitSootMethodReachSet(entry.getValue(), myCallGraph));
//        }
//
//        long endTime=System.nanoTime();
//
//        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info((((double)(endTime-startTime))/1E9)+"##"+"查找冲突路径"+"##"+myCallGraph.targetSootMethod+"##"+myCallGraph.targetUnit);


        /////////////////////////////////////////////////////////////////////////////////////////////////


        Map<SootMethod, Set<Intent>> sootMethodIntentConditionSummary = new HashMap<>();//这是结果！！！
        Set<List<SootMethod>> sootMethodCallFinalPaths = new HashSet<>();
        //正向路径

        if (!callGraphLimitEnabled) {
            callgraphEnterBranchLimit = Integer.MAX_VALUE;
        }

        hasReachCallGraphBranchLimit = false;
        long startTime = System.nanoTime();
        getCallPathSootMethod(myCallGraph.dummyMainMethod, new ArrayList<SootMethod>(), myCallGraph, null, new HashSet<Edge>(), sootMethodCallFinalPaths, sootMethodSetIntentCondition, sootMethodIntentConditionSummary, 1);
        long endTime = System.nanoTime();
        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info((((double) (endTime - startTime)) / 1E9) + "##" + "DFS融合Intent" + "##" + myCallGraph.targetSootMethod + "##" + myCallGraph.targetUnit);
        if (hasReachCallGraphBranchLimit) {
            infoLogger.warn(appPath);

            File callgraphLimitFile = new File(Config.intentConditionSymbolicExcutationResults + "/" + "callgraphLimit.txt");
            if (callgraphLimitFile.exists()) {

                ReadFileOrInputStream readFileOrInputStream = new ReadFileOrInputStream(Config.intentConditionSymbolicExcutationResults + "/" + "callgraphLimit.txt", exceptionLogger);
                Set<String> contentSet = readFileOrInputStream.getAllContentLinSet();
                if (!contentSet.contains(appPath)) {
                    WriteFile writeFileCallGraphReachLimit = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "callgraphLimit.txt", true, exceptionLogger);
                    writeFileCallGraphReachLimit.writeStr(appPath + "\n");
                    writeFileCallGraphReachLimit.close();
                }
            } else {
                WriteFile writeFileCallGraphReachLimit = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "callgraphLimit.txt", true, exceptionLogger);
                writeFileCallGraphReachLimit.writeStr(appPath + "\n");
                writeFileCallGraphReachLimit.close();
            }


            WriteFile writeFileCallGraphReachLimitRepeat = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "callgraphLimitRepeat.txt", true, exceptionLogger);
            writeFileCallGraphReachLimitRepeat.writeStr(appPath + "##" + myCallGraph.targetSootMethod + "##" + myCallGraph.targetUnit + "##" + "callgraphLimit" + "\n");
            writeFileCallGraphReachLimitRepeat.close();
            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).warn(appPath + "##" + myCallGraph.targetSootMethod + "##" + myCallGraph.targetUnit + "##" + "callgraphLimit");


            return;
        }

        for (Iterator<Edge> edgeIterator = myCallGraph.edgesOutOf(myCallGraph.dummyMainMethod); edgeIterator.hasNext(); ) {

            Edge outEdge = edgeIterator.next();

            SootMethod sootMethodTgt = outEdge.tgt();

            Set<Intent> intentSet = sootMethodIntentConditionSummary.get(sootMethodTgt);

            AXmlNode aXmlNode = eas.get(sootMethodTgt.getDeclaringClass().getName());
            if (aXmlNode == null) {
                continue;

            }

            String componentName = sootMethodTgt.getDeclaringClass().getName();
            String componentType = aXmlNode.getTag();

            if (intentSet != null) {
                for (Intent intent : intentSet) {
                    intent.targetComponent = sootMethodTgt.getBytecodeSignature() + "##" + sootMethodTgt.getDeclaringClass().getName();
                    ultiIntentSet.add(new IntentUnit(intent, myCallGraph.targetUnit, componentType, componentName));

                }


            }


        }


    }


    class IntentUnitSootMethod {//sootMethod中的unit到sootMethod起始点的Intent condition
        Intent intent;//key
        Unit unit;
        SootMethod sootMethod;

        public IntentUnitSootMethod(Intent intent, Unit unit, SootMethod sootMethod) {
            this.intent = intent;
            this.unit = unit;
            this.sootMethod = sootMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntentUnitSootMethod that = (IntentUnitSootMethod) o;
            return Objects.equals(intent, that.intent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(intent);
        }
    }

    public static class IntentUnit {

        Intent intent;//key
        Unit unit;
        String comPonentType;
        String comPonentName;


        public IntentUnit(Intent intent, Unit unit, String comPonentType, String comPonentName) {
            this.intent = intent;
            this.unit = unit;
            this.comPonentType = comPonentType;
            this.comPonentName = comPonentName;
        }

        public IntentUnit(IntentUnit intentUnit) {
            this.intent = intentUnit.intent;
            this.unit = intentUnit.unit;
            this.comPonentType = intentUnit.comPonentType;
            this.comPonentName = intentUnit.comPonentName;
        }

        public IntentUnit() {

        }

        public IntentUnit(Intent intent, Unit unit) {
            this.intent = intent;
            this.unit = unit;
        }

        @Override
        public String toString() {
            return "IntentUnit{" +
                    "intent=" + intent +
                    ", unit=" + unit +
                    ", comPonentType='" + comPonentType + '\'' +
                    ", comPonentName='" + comPonentName + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntentUnit that = (IntentUnit) o;
            return Objects.equals(intent, that.intent);
        }

        @Override
        public int hashCode() {

            return Objects.hash(intent);
        }
    }


    public Map<SootMethod, Map<Unit, TargetUnitInMethodInfo>> allSootMethodsAllUnitsTargetUnitInMethodInfo = new HashMap<>();


    public boolean doAnalysisOnUnit(MyPairUnitToEdge myPairUnitToEdge, SootMethod sootMethod, SingleSootMethodIntentFlowAnalysis intentFlowAnalysis, SimpleLocalDefs defs, UnitGraph ug) {


        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(myPairUnitToEdge.srcUnit.toString() + "doAnalysisOnUnit");

        if (!pathLimitEnabled) {
            enterBranchLimit = Integer.MAX_VALUE;
        }


        int finalPathsPreviousCount=-1;


        //experiment*******************************

        List<List<Unit>> finalPaths = new ArrayList<>();

        hasReachBranchLimit = false;

        getAllPathInMethod(myPairUnitToEdge.srcUnit, null, ug, finalPaths, new ArrayList<Unit>(), new HashSet<UnitEdge>(),0);//欧拉路径



        if (hasReachBranchLimit) {

            //finalPathsPreviousCount=Integer.MAX_VALUE;
            finalPathsPreviousCount=finalPaths.size();
            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("finalPaths数量达到限制");


        }
        else
        {
            finalPathsPreviousCount=finalPaths.size();
        }

        //experiment*******************************



        int finalPathsReducedAfterCount=-1;

        List<List<Unit>> finalPathsReduced = new ArrayList<>();


        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("开始约简unitgraph" + sootMethod.getBytecodeSignature() + "#" + myPairUnitToEdge.srcUnit);

        MyUnitGraph myUnitGraph = MyUnitGraphFactory.getMyUnitGraph(appPath, sootMethod.getActiveBody(), myPairUnitToEdge.srcUnit, exceptionLogger, ug, intentFlowAnalysis, defs);

        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("约简结束！");


        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("unitgraph节点数" + ":" + myUnitGraph.getAllUnit().size());

        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("unitgraph！寻找所有路径" + sootMethod.getBytecodeSignature() + "#" + myPairUnitToEdge.srcUnit);
        hasReachBranchLimit = false;
        getAllPathInMethod(myPairUnitToEdge.srcUnit, null, myUnitGraph, finalPathsReduced, new ArrayList<Unit>(), new HashSet<UnitEdge>(), 0);
        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("unitgraph！寻找路径完毕");


        if (hasReachBranchLimit) {

            //finalPathsReducedAfterCount=Integer.MAX_VALUE;
            finalPathsReducedAfterCount=finalPathsReduced.size();
            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("化简CFG的finalPathsReduced达到限制！");

            appUnitGraphPathReducedReachLimit.writeStr(appPath + "#" + sootMethod + "#" + myPairUnitToEdge.srcUnit + "\n");
            appUnitGraphPathReducedReachLimit.flush();


        }
        else
        {
            finalPathsReducedAfterCount=finalPathsReduced.size();
        }


        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(finalPaths.size() + "*******************************" + finalPathsReduced.size() + "************************" + (finalPaths.size() - finalPathsReduced.size()) + " " + myPairUnitToEdge.srcUnit);


        ifReducedWriter.writeStr(appPath+","+sootMethod.getBytecodeSignature()+","+finalPathsPreviousCount + "," + finalPathsReducedAfterCount + "," + (finalPathsPreviousCount - finalPathsReducedAfterCount)+"\n");
        ifReducedWriter.flush();



        if (hasReachBranchLimit) {
            return false;
        }


        Map<Unit, TargetUnitInMethodInfo> allUnitsTargetUnitInMethodInfo = allSootMethodsAllUnitsTargetUnitInMethodInfo.get(sootMethod);

        if (allUnitsTargetUnitInMethodInfo == null) {
            allUnitsTargetUnitInMethodInfo = new HashMap<>();
        }


        HashSet<UnitPath> unitPaths = new HashSet<>();


        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("开始分析每一条路径");
        Pair<Boolean, HashSet> intentSetBetweenTwoPoints = analysePaths(sootMethod, defs, finalPathsReduced, unitPaths);
        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("分析所有路径完成");

        if (intentSetBetweenTwoPoints.getValue0()) {
            allIntentConditionOfOneApp.addAll(intentSetBetweenTwoPoints.getValue1());//targetUnit到方法起始点的Intent condition
        }


        allUnitsTargetUnitInMethodInfo.put(myPairUnitToEdge.srcUnit, new TargetUnitInMethodInfo(myPairUnitToEdge, sootMethod, myUnitGraph, unitPaths));

        allSootMethodsAllUnitsTargetUnitInMethodInfo.put(sootMethod, allUnitsTargetUnitInMethodInfo);


        return true;


    }

    private Pair<Boolean, HashSet> analysePaths(SootMethod sootMethod, SimpleLocalDefs defs, List<List<Unit>> finalPaths, HashSet<UnitPath> unitPaths) {

        List<UnitPath> intraUnitPaths = new ArrayList<UnitPath>();
        Map<Unit, List<UnitPath>> unitSum = null;
        Map<List<Unit>, UnitPath> pathMapUnitPathConds = new LinkedHashMap<List<Unit>, UnitPath>();//key：path ----------------value：路径的条件
        //MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("%%%%%%%%%%%%%%%%%%%%%%%%%" + sootMethod.getBytecodeSignature() + "%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        for (List<Unit> path : finalPaths) {
            Set<String> currPathCond = new LinkedHashSet<String>();
            Set<String> currDecls = new LinkedHashSet<String>();

            analysisPathIntraMethod(path, sootMethod, defs, currPathCond, currDecls);

            //MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("33333333333333333333333333333333333");
            //currDecls.stream().forEach(System.out::println);

            //currPathCond.stream().forEach(System.out::println);

            //MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("44444444444444444444444444444444444");

            UnitPath up = new UnitPath(currPathCond, currDecls, path);
            intraUnitPaths.add(up);

            pathMapUnitPathConds.put(path, up);


        }

        //MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        //--------------------------------


        //对路径中的方法处理，其可能有约束(如果不解决的话，可能中间崩溃)


        //--------------------------------

        boolean isFeasibleUnit = false;


        HashSet<Intent> intentConditionSet = new HashSet<>();
        for (Map.Entry<List<Unit>, UnitPath> pathMapPathCondEntry : pathMapUnitPathConds.entrySet()) {
            List<Unit> onePath = pathMapPathCondEntry.getKey();


            Unit unit = onePath.get(0);

            UnitPath oneUnitPath = pathMapPathCondEntry.getValue();

            Pair<Intent, Boolean> soln = runSolvingPhase(sootMethod, onePath, oneUnitPath.getPathCond(), oneUnitPath.getDecl());

            oneUnitPath.intentSoln = new IntentSoln(soln.getValue1(), soln.getValue0());

            unitPaths.add(oneUnitPath);

            if (!soln.getValue1()) {
                infeasibleTargets.add(new Pair<Unit, SootMethod>(unit, sootMethod));
            } else {
                isFeasibleUnit = true;
                intentConditionSet.add(soln.getValue0());
            }


        }


        return new Pair<Boolean, HashSet>(isFeasibleUnit, intentConditionSet);
    }


    private Set<Intent> getCallPathSootMethod(SootMethod sootMethod, List<SootMethod> callSootMethodPath, MyCallGraph cg, Edge edge, Set<Edge> edgeSet, Set<List<SootMethod>> sootMethodCallFinalPaths, Map<SootMethod, Map<Unit, Set<Intent>>> sootMethodUnitIntentConditionMap, Map<SootMethod, Set<Intent>> sootMethodIntentConditionSummary, int branch) {

        if (branch > callgraphEnterBranchLimit || branch < 0) {
            hasReachCallGraphBranchLimit = true;
            return null;
        }

        List<SootMethod> callSootMethodPathCopy = new ArrayList<>(callSootMethodPath);

        callSootMethodPathCopy.add(sootMethod);

        Set<Edge> edgeSetCopy = new HashSet<>(edgeSet);

        edgeSetCopy.add(edge);


        if (!cg.edgesOutOf(sootMethod).hasNext()) {
            if (sootMethod == cg.targetSootMethod) {


                sootMethodCallFinalPaths.add(callSootMethodPathCopy);

                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("路径长度：" + callSootMethodPath.size());

                return sootMethodUnitIntentConditionMap.get(cg.targetSootMethod).get(cg.targetUnit);
            } else {
                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("非targetSootMethod出度为0");
                throw new RuntimeException("非targetSootMethod出度为0");
            }
        }


        Map<Unit, Set<Intent>> hashMapParentOfUnitToSetIntent = sootMethodUnitIntentConditionMap.get(sootMethod);

        Set<Intent> hashSetParentSummary = new HashSet<>();

        branch = branch * (cg.outEdgesOfThisMethod.get(sootMethod).size());

        for (Iterator<Edge> edgeIterator = cg.edgesOutOf(sootMethod); edgeIterator.hasNext(); ) {
            Edge outEdge = edgeIterator.next();


            if (!edgeSetCopy.contains(outEdge)) {


                SootMethod sootMethodTgt = outEdge.tgt();
                Unit srcUnit = outEdge.srcUnit();

                Set<Intent> hashSetChildrenSummary = null;
                if (!sootMethodIntentConditionSummary.containsKey(sootMethodTgt)) {
                    hashSetChildrenSummary = getCallPathSootMethod(sootMethodTgt, callSootMethodPathCopy, cg, outEdge, edgeSetCopy, sootMethodCallFinalPaths, sootMethodUnitIntentConditionMap, sootMethodIntentConditionSummary, branch);
                    if (hasReachCallGraphBranchLimit) {
                        return null;
                    }
                } else {

                    hashSetChildrenSummary = sootMethodIntentConditionSummary.get(sootMethodTgt);//取出sootMethodTgt到达targetSootMethod的所有intent条件


                }

                if (srcUnit == null) {//有边但是没有srcUnit,比如new xxx（）

                    hashSetParentSummary.addAll(hashSetChildrenSummary);

                } else {

                    Set<Intent> hashSetIntentParent = hashMapParentOfUnitToSetIntent.get(srcUnit);


                    for (Intent intentChildren : hashSetChildrenSummary) {
                        for (Intent intentParent : hashSetIntentParent) {
                            Intent newIntent = joinTwoIntent(intentParent, intentChildren);//融合intent

                            if (newIntent != null) {
                                hashSetParentSummary.add(newIntent);
                            }


                        }
                    }

                }


            }
        }

        //将不冲突的intent合并,减少intent数量
        int preSize = hashSetParentSummary.size();
        //hashSetParentSummary = joinIntentSet(hashSetParentSummary, sootMethod);
        int afterSize = hashSetParentSummary.size();
        infoLogger.info("单个方法Intent数量：" + "pre:" + preSize + " ^^ " + "after:" + afterSize + "##" + appPath);
        sootMethodIntentConditionSummary.put(sootMethod, hashSetParentSummary);

        return hashSetParentSummary;


    }


    public Set<Intent> joinIntentSet(Set<Intent> hashSetParentSummary, SootMethod curSootMethod) {//这些intent是或的关系，取出这些intent中冲突的地方，其他不冲突可以全部合并，让每一个intent都拥有


        Set<Intent> allReturnIntentSet = new HashSet<>();
        Map<String, Set<Intent>> similarActionKeyMap = new HashMap<>();
        for (Intent intent : hashSetParentSummary) {//根据action分类
            Set<Intent> similarActionIntentSet = similarActionKeyMap.get(intent.action);
            if (similarActionIntentSet == null) {
                similarActionIntentSet = new HashSet<>();
            }
            similarActionIntentSet.add(intent);
            similarActionKeyMap.put(intent.action, similarActionIntentSet);
        }


        for (Map.Entry<String, Set<Intent>> entryActionIntentSet : similarActionKeyMap.entrySet()) {


            Map<IntentExtraKey, Set<IntentExtraValue>> similarExtraKeyTypeMap = new HashMap<>();//根据intentExtra归类
            Map<String, Set<String>> categoryHashMap = new HashMap<>();//根据category归类

            Set<Intent> similarActionSet = entryActionIntentSet.getValue();

            for (Intent oneIntent : similarActionSet) {
                for (IntentExtraKey intentExtraKey : oneIntent.myExtras) {

                    Set<IntentExtraValue> similarIntentExtraKeyTypeSet = similarExtraKeyTypeMap.get(intentExtraKey);
                    if (similarIntentExtraKeyTypeSet == null) {
                        similarIntentExtraKeyTypeSet = new HashSet<>();
                    }

                    similarIntentExtraKeyTypeSet.add(new IntentExtraValue(intentExtraKey));
                    similarExtraKeyTypeMap.put(intentExtraKey, similarIntentExtraKeyTypeSet);


                }

                for (String cate : oneIntent.categories) {
                    Set<String> similarCategorySet = categoryHashMap.get(cate.replaceFirst("ZMS!", ""));
                    if (similarCategorySet == null) {
                        similarCategorySet = new HashSet<>();
                    }
                    similarCategorySet.add(cate);//包含ZMS！A  和A
                    categoryHashMap.put(cate.replaceFirst("ZMS!", ""), similarCategorySet);


                }

            }


            Set<IntentExtraKey> commonExtra = new HashSet<>();


            Set<String> commonCategory = new HashSet<>();


            for (Map.Entry<IntentExtraKey, Set<IntentExtraValue>> entryExtra : similarExtraKeyTypeMap.entrySet()) {
                if (entryExtra.getValue().size() <= 1) {

                    Set<SootMethod> sootMethodSet = methodsExtraConflict.get(entryExtra.getKey());
                    if (sootMethodSet != null && sootMethodSet.contains(curSootMethod)) {
                        //之后会有冲突。
                        //这个IntentExtraValue 会和之后的Intent冲突，如果其和其他融合（设为B）会生成一个新的Intent,则这个新的Intent之后会被丢弃掉。但是之后的条件和B可能是可满足的
                    } else {
                        commonExtra.add(new IntentExtraKey(entryExtra.getValue().iterator().next()));
                    }


                }
            }


            for (Map.Entry<String, Set<String>> entryCategory : categoryHashMap.entrySet()) {
                if (entryCategory.getValue().size() <= 1) {//entryCategory.getValue()大于1 说明这个entryCategory.getKey（）（设为A）, 存在ZMS！A
                    Set<SootMethod> sootMethodSet = methodsCategoryConflict.get(entryCategory.getKey());
                    if (sootMethodSet != null && sootMethodSet.contains(curSootMethod)) {

                    } else {
                        commonCategory.add(entryCategory.getValue().iterator().next());
                    }

                }
            }

            for (Intent intent : similarActionSet)//将一些公共的加到每一个相同action的intent上.
            {
                intent.categories.addAll(commonCategory);

                intent.myExtras.addAll(commonExtra);
            }


            Set<Intent> newIntentSet = new HashSet<>(similarActionSet);//去除相等intent


            //**********************如果一个intent包含一个intent去除子Intent****************

            List<Intent> intentList = new ArrayList<>(newIntentSet);
            Set<Intent> removeIntentSet = new HashSet<>();
            for (int i = 0; i < intentList.size(); i++) {

                for (int j = i + 1; j < intentList.size(); j++) {
                    if (intentList.get(i).contains(intentList.get(j))) {//1
                        removeIntentSet.add(intentList.get(j));
                    } else if (intentList.get(j).contains(intentList.get(i)))//2
                    {
                        removeIntentSet.add(intentList.get(i));
                    }

                    //1,2 总是将较小的intent加入removeIntentSet


                }
            }

            newIntentSet.removeAll(removeIntentSet);

            //*************************************************


            allReturnIntentSet.addAll(newIntentSet);


        }


        return allReturnIntentSet;


    }

    private static void validateJoin(Set<Intent> returnIntentSet, Set<IntentExtraValue> commonExtra, Set<String> commonCategory) {
        for (Intent intent : returnIntentSet) {


            if (!intent.categories.containsAll(commonCategory)) {
                throw new RuntimeException("joinIntent error!");
            }

            Set<IntentExtraValue> intentExtraValueSet = new HashSet<>();
            for (IntentExtraKey intentExtraKey : intent.myExtras) {
                intentExtraValueSet.add(new IntentExtraValue(intentExtraKey));
            }

            if (!intentExtraValueSet.containsAll(commonExtra)) {
                throw new RuntimeException("joinIntent error!");
            }

        }
    }


    public static Intent joinTwoIntent(Intent intentParent, Intent intentChildren) {//intent不会为null
        if (intentChildren == null && intentParent == null) {
            return null;
        }

        if (intentParent != null && intentChildren != null) {
            Intent intent = new Intent();

            if (intentChildren.action != null && intentParent.action != null) {
                intent.action = jointTwoStringValue(intentChildren.action, intentParent.action);//两个action不冲突，必然会产生至少一个新的action

                if (intent.action == null) {
                    return null;
                }

            }
            if (intentChildren.action == null && intentParent.action == null) {
                intent.action = null;
            }
            if (intentChildren.action != null && intentParent.action == null) {
                intent.action = intentChildren.action;
            }
            if (intentParent.action != null && intentChildren.action == null) {
                intent.action = intentParent.action;
            }

            HashSet<String> hashSet = new HashSet();
            hashSet.addAll(intentParent.categories);
            hashSet.addAll(intentChildren.categories);
            hashSet.remove(null);
            for (String cate : hashSet) {
                if (cate.startsWith("ZMS!")) {
                    String removePrefixCate = cate.substring(4, cate.length());
                    if (hashSet.contains(removePrefixCate)) {
                        return null;//冲突
                    }
                }
            }
            intent.categories = hashSet;


            Set<IntentExtraValue> intentExtraValueSet = new HashSet<>();
            for (IntentExtraKey intentExtraKeyChild : intentChildren.myExtras) {
                intentExtraValueSet.add(new IntentExtraValue(intentExtraKeyChild));
            }

            for (IntentExtraKey intentExtraKeyParent : intentParent.myExtras) {
                intentExtraValueSet.add(new IntentExtraValue(intentExtraKeyParent));
            }

            HashSet<IntentExtraKey> hashSetIntentExtraKey = new HashSet();//存储最后的结果

            for (IntentExtraValue oneExtra : intentExtraValueSet) {

                IntentExtraKey intentExtraKey = new IntentExtraKey(oneExtra);
                if (hashSetIntentExtraKey.contains(intentExtraKey)) {// key和type相同则true
                    for (IntentExtraKey oneIntentExtraKey : hashSetIntentExtraKey) {
                        if (oneIntentExtraKey.equals(intentExtraKey)) {

                            String value = null;
                            if (intentExtraKey.type.equals("java.lang.String")) {
                                value = jointTwoStringValue(intentExtraKey.value.trim(), oneIntentExtraKey.value.trim());
                            } else if (isNumberType(intentExtraKey)) {
                                value = intentExtraKey.value + "##" + oneIntentExtraKey.value;//比如： >5 >6 得到 6,7 其实7是满足两个要求的
                            } else//其他类型
                            {
                                if (intentExtraKey.value.equals(oneIntentExtraKey.value)) {
                                    value = intentExtraKey.value;
                                }
                            }


                            if (value == null)//存在两个值冲突
                            {
                                return null;
                            }

                            oneIntentExtraKey.value = value;// assign new value


                        }
                    }
                } else {
                    hashSetIntentExtraKey.add(intentExtraKey);
                }

            }


            intent.myExtras = hashSetIntentExtraKey;

            return intent;


        }
        if (intentChildren == null) {
            return new Intent(intentParent);
        }
        if (intentParent == null) {
            return new Intent(intentChildren);
        }
        return null;
    }

    private static boolean isNumberType(IntentExtraKey intentExtraKey) {
        return intentExtraKey.type.equals("long") || intentExtraKey.type.equals("short") || intentExtraKey.type.equals("int") || intentExtraKey.type.equals("float") || intentExtraKey.type.equals("double") || intentExtraKey.type.equals("byte");
    }

    private static String jointTwoStringValue(String str1, String str2) {//##表示且   ok

        Set<String> str1SetOfChildren = getAllCondition(str1);
        Set<String> str2SetOfParent = getAllCondition(str2);
        String result = "";
        boolean flag = false;
        for (String actionOfChildren : str1SetOfChildren) {
            for (String actionOfParent : str2SetOfParent) {
                if (actionOfChildren.contains("!0!") && (!actionOfParent.contains("!0!"))) {
                    result = actionOfParent + "##" + result;
                    flag = true;
                    continue;

                }
                if ((!actionOfChildren.contains("!0!")) && (actionOfParent.contains("!0!"))) {
                    result = actionOfChildren + "##" + result;
                    flag = true;
                    continue;

                }
                if ((actionOfChildren.contains("!0!")) && (actionOfParent.contains("!0!"))) {
                    result = result;
                    flag = true;
                    continue;

                }
                if (actionOfChildren.startsWith("ZMS!") || actionOfParent.startsWith("ZMS!")) {

                    if (actionOfChildren.startsWith("ZMS!") && actionOfParent.startsWith("ZMS!")) {
                        String removePrefixChildren = actionOfChildren.substring(4, actionOfChildren.length());
                        String removePrefixParent = actionOfParent.substring(4, actionOfParent.length());
                        if (removePrefixChildren.equals(removePrefixParent)) {
                            result = actionOfChildren + "##" + result;
                            flag = true;
                        } else {
                            result = actionOfChildren + "##" + actionOfParent + "##" + result;
                            flag = true;
                        }
                    } else if (actionOfChildren.startsWith("ZMS!") && (!actionOfParent.startsWith("ZMS!"))) {
                        String removePrefixChildren = actionOfChildren.substring(4, actionOfChildren.length());
                        if (removePrefixChildren.equals(actionOfParent)) {//两个action冲突
                            continue;
                        } else {
                            result = actionOfChildren + "##" + actionOfParent + "##" + result;
                            flag = true;
                        }
                    } else {
                        String removePrefixParent = actionOfParent.substring(4, actionOfParent.length());
                        if (removePrefixParent.equals(actionOfChildren)) {//两个action冲突
                            continue;
                        } else {
                            result = actionOfChildren + "##" + actionOfParent + "##" + result;
                            flag = true;
                        }
                    }


                } else if ((!actionOfChildren.trim().equals(actionOfParent.trim()))) {//两个action不同
                    continue;
                } else //两个action相同
                {
                    result = actionOfChildren + "##" + result;
                    flag = true;
                }

            }
        }
        if (!flag) {
            return null;
        }

        if (result.length() >= 2) {
            result = result.substring(0, result.length() - 2);//去掉##
        } else// two string value are "!0!"
        {
            result = "!0!";
        }


        return result;
    }

    private static Set<String> getAllCondition(String str) {
        HashSet<String> hashSet = new HashSet<>();
        if (str.contains("##")) {
            String[] conditionArray = str.split("##");
            for (String conditionStr : conditionArray) {
                hashSet.add(conditionStr);
            }

        } else {
            hashSet.add(str);
        }
        return hashSet;

    }


    protected Pair<Intent, Boolean> runSolvingPhase(SootMethod method, List<Unit> currPath, Set<String> interPathCond, Set<String> interDecls) {

        Set<String> interPathCondNew = new HashSet<>();

        Set<String> interDeclsNew = new HashSet<>();
        for (String oneInterPathCond : interPathCond) {
            String[] oneCondArray = oneInterPathCond.split("\n");
            for (String oneCond : oneCondArray) {
                interPathCondNew.add(oneCond);
            }
        }
        for (String oneInterDecl : interDecls) {
            String[] oneDeclArray = oneInterDecl.split("\n");
            for (String oneDecl : oneDeclArray) {
                interDeclsNew.add(oneDecl);
            }
        }


        Pair<Intent, Boolean> soln = findSolutionForPath(interPathCondNew, method, interDeclsNew, currPath);
        boolean feasible = soln.getValue1();
        Intent genIntent = soln.getValue0();

        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("!!!!!!!!!!!!!!!!!!!!!!!" + currPath.iterator().next() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (feasible) {
            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("路径可行" + genIntent);
        } else {
            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("路径不可行");
            String content = "";
            for (String d : interDeclsNew) {
                content += d + "\n";
            }
            for (String c : interPathCondNew) {
                content += c + "\n";
            }

            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(content);
        }
        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        return soln;
    }

    public Pair<Intent, Boolean> findSolutionForPath(Set<String> currPathCond,
                                                     SootMethod method,
                                                     Set<String> decls,
                                                     List<Unit> currPath) {
        Set<Quartet<String, String, String, String>> extraData = new LinkedHashSet<Quartet<String, String, String, String>>();
        String action = null;
        Set<String> categories = new LinkedHashSet<String>();
        boolean isPathFeasible = false;

        try {
            Pair<Map<String, String>, Boolean> ret = returnSatisfyingModel(decls, currPathCond);
            Map<String, String> model = ret.getValue0();
            Boolean isSat = ret.getValue1();
            if (!isSat) {
                //exceptionLogger.debug("path is infeasible");
                isPathFeasible = false;

            } else {
                //exceptionLogger.debug("path is feasible---here is a solution");
                isPathFeasible = true;

                Map<String, String> intentActionSymbols = new LinkedHashMap<String, String>();
                for (String expr : currPathCond) {
                    Pattern p = Pattern.compile("\\(assert \\(= \\(getAction (.+)\\) (.+)\\)\\)");
                    Matcher m = p.matcher(expr);
                    while (m.find()) {
                        String intentSymbol = m.group(1);
                        //exceptionLogger.info("intent symbol for action: " + intentSymbol);

                        String actionStrSymbol = m.group(2);
                        //exceptionLogger.info("action symbol: " + actionStrSymbol);

                        intentActionSymbols.put(intentSymbol, actionStrSymbol);//
                    }
                }

                Map<String, List<String>> intentOrBundleLocalKeys = new LinkedHashMap<String, List<String>>();
                for (String expr : currPathCond) {
                    Pattern p = Pattern.compile("\\(assert \\(= \\(containsKey (.+) \\\"(.+)\\\"\\) true\\)\\)");
                    Matcher m = p.matcher(expr);
                    while (m.find()) {
                        String intentOrBundleLocalSymbol = m.group(1);

                        String key = m.group(2);

                        List<String> keys = intentOrBundleLocalKeys.get(intentOrBundleLocalSymbol);


                        if (keys == null) {
                            keys = new ArrayList<String>();
                        }

                        keys.add(key);
                        intentOrBundleLocalKeys.put(intentOrBundleLocalSymbol, keys);

                    }
                }

                Map<String, String> extraDataFromIntent = new LinkedHashMap<>();
                for (String expr : currPathCond) {
                    //"(assert (= (fromIntent " + extraLocalSymbol + ") " + intentSymbol + "))"
                    Pattern p = Pattern.compile("\\(assert\\s+\\(=\\s+\\(fromIntent\\s+(\\S+)\\)\\s+(\\S+)\\)\\)");
                    Matcher m = p.matcher(expr);
                    while (m.find()) {

                        String all = m.group(0);
                        //MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(all);
                        String extraDataSymbol = m.group(1);

                        //MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(dataSymbol);
//
                        String intentSymbol = m.group(2);

                        //MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(intentSymbol);
                        extraDataFromIntent.put(extraDataSymbol, intentSymbol);

                    }

                }


                Set<Quartet<String, String, String, String>> tempIntentExtraData = new LinkedHashSet<Quartet<String, String, String, String>>();
                Map<String, Quartet<String, String, String, String>> tempBundleExtraDataMap = new HashMap<String, Quartet<String, String, String, String>>();
                for (Map.Entry<String, String> entry : model.entrySet()) {//model里的是一个变量和一个值 变量=值

                    String symbol = entry.getKey();
                    String generatedValue = entry.getValue();//  由z3推断出来的symbol=generatedValue

                    String key = extraDataMapKey.get(symbol);

                    if (key != null)//这里key！=null 则这个 symbol是extraData变量
                    {

                        String extraFrom = extraFromMap.get(symbol);

                        if (extraFrom != null) {
                            if (extraFrom.contains("android.os.BaseBundle") || extraFrom.contains("android.os.Bundle"))//extraData from bundle
                            {
                                Local local = symbolLocalMap.get(symbol);
                                if (local != null) {
                                    Quartet<String, String, String, String> quartet = new Quartet<>("BundleKey", local.getType().toString(), key, generatedValue);
                                    tempBundleExtraDataMap.put(extraFrom, quartet);
                                }

                            } else if (extraFrom.contains("android.content.Intent"))//extraData from Intent
                            {
                                Local local = symbolLocalMap.get(symbol);
                                if (local != null) {
                                    if (local.getType().toString().equals("android.os.Bundle"))//这个intent 的extra是bundle

                                    {
                                        Quartet<String, String, String, String> quartet = new Quartet<>("IntentKey", local.getType().toString(), key, symbol);
                                        tempIntentExtraData.add(quartet);
                                    } else {
                                        Quartet<String, String, String, String> quartet = new Quartet<>("IntentKey", local.getType().toString(), key, generatedValue);
                                        tempIntentExtraData.add(quartet);
                                    }

                                }


                            } else {
                                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("extraData is not from intent or bundle");
                                throw new RuntimeException("extraData is not from intent or bundle");
                            }
                        }

                    }


                    for (String actionSymbol : intentActionSymbols.values()) {//-----------action的值求解---------
                        if (actionSymbol.equals(symbol)) {
                            action = generatedValue.replaceAll("^\"|\"$", "");// ^ Matches the beginning of the line.
                            System.out.println(action);
                        }
                    }

                }
                for (Quartet<String, String, String, String> intentExtra : tempIntentExtraData) {
                    if (intentExtra.getValue1().equals("android.os.Bundle"))//得到bundle的具体值。而不是仅仅非空
                    {

                        String bundleExtraLocal = intentExtra.getValue3();
                        Quartet quartet = tempBundleExtraDataMap.get(bundleExtraLocal);
                        if (quartet != null) {
                            //intentExtra=intentExtra.setAt3(quartet.getValue0()+":"+quartet.getValue1()+":"+quartet.getValue2()+":"+quartet.getValue3());
                            intentExtra = intentExtra.setAt3(quartet.toString());
                        } else {
                            intentExtra = intentExtra.setAt3("NotNull");
                        }

                        extraData.add(intentExtra);

                    } else {
                        extraData.add(intentExtra);
                    }
                }


//                for (Map.Entry<String, String> entry : model.entrySet()) {//model里的是一个变量和一个值 变量=值
//                    String symbol = entry.getKey();
//                    String generatedValue = entry.getValue();
//                    //exceptionLogger.debug(symbol + ": " + generatedValue);
//
//                    Quartet<String, String, String, String> genDatum = generateDatum(symbol, generatedValue, extraLocalKeys);//获取extra的值
//					/*if (genDatum == null) {
//						exceptionLogger.warn("Skipping generation of extra datum for " + symbol);
//						continue;
//					}*/
//
//                    Quartet<String, String, String, String> extraDatum = genDatum;//这个key -value不一定是intent的可能是bundle的，而这个bundle是intent的
//                    if (extraDatum != null) {
//                        extraData.add(extraDatum);
//                    }
//
//                    for (String actionSymbol : intentActionSymbols.values()) {//-----------action的值求解---------
//                        if (actionSymbol.equals(symbol)) {
//                            action = generatedValue.replaceAll("^\"|\"$", "");// ^ Matches the beginning of the line.
//                        }
//                    }
//                }

                for (String expr : currPathCond) {
                    Pattern p = Pattern.compile("\\(assert \\(exists \\(\\(index Int\\)\\) \\(= \\(select cats index\\) \\\"(.+)\\\"\\)\\)\\)");
                    Matcher m = p.matcher(expr);
                    while (m.find()) {
                        String category = m.group(1);
                        //exceptionLogger.info("Found category: " + category);
                        categories.add(category);
                    }
                }

                // I cannot generate an Intent for an extra datum if I don't know it's type
				/*for (String expr : currPathCond) {
					Pattern p = Pattern.compile("\\(assert \\(exists \\(\\(index Int\\)\\) \\(= \\(select keys index\\) \\\"(.+)\\\"\\)\\)\\)");
					Matcher m = p.matcher(expr);
					while (m.find()) {
						String key = m.group(1);
						exceptionLogger.info(("Found extra key: " + key));
						Triplet<String,String,String> extraDatum = new Triplet(null,key,null);
						extraData.add(extraDatum);
					}
				}*/
                //exceptionLogger.debug("");
            }
        } catch (Z3Exception e) {
            e.printStackTrace();
        }

        Intent genIntent = new Intent();//每条路径的intent都不为null,不能执行的话，也有intent


        for (Quartet<String, String, String, String> oneExtra : extraData) {
            if (oneExtra.getValue0().equals("IntentKey")) {
                IntentExtraKey intentExtraKey = new IntentExtraKey(oneExtra.getValue2(), oneExtra.getValue1(), oneExtra.getValue3());
                genIntent.myExtras.add(intentExtraKey);
            } else {
                throw new RuntimeException("BundleKey do not handle!");
            }

        }
        genIntent.action = action;
        genIntent.categories = categories;


        if (pathIntents.containsKey(currPath)) {
            throw new RuntimeException("已经存在这个路径的intent");
        }
        pathIntents.put(currPath, genIntent);

        return new Pair<Intent, Boolean>(genIntent, isPathFeasible);
    }


    Pair<Map<String, String>, Boolean> returnSatisfyingModel(Set<String> decls, Set<String> pathCond) throws Z3Exception {
        return returnSatisfyingModelForZ3(decls, pathCond);
    }

    Pair<Map<String, String>, Boolean> returnSatisfyingModelForZ3(Set<String> decls, Set<String> pathCond) throws Z3Exception {
        String pathCondFileName = null;
        String outSpec = "";
        try {
            pathCondFileName = Config.Z3_RUNTIME_SPECS_DIR + File.separator + "z3_path_cond";
            PrintWriter out = new PrintWriter(pathCondFileName);

            outSpec += "(declare-datatypes () ((Object Null NotNull)))\n" +
                    "(declare-fun containsKey (Object String) Bool)\n" +
                    "(declare-fun containsKey (String String) Bool)\n" +
                    "(declare-fun containsKey (Int String) Bool)\n" +
                    "(declare-fun containsKey (Real String) Bool)\n" +
                    "(declare-fun containsKey (Bool String) Bool)\n" +

                    "(declare-fun getAction (Object) String)\n" +

                    "(declare-fun fromIntent (Object) Object)\n" +//extraData  FromIntent
                    "(declare-fun fromIntent (String) Object)\n" +
                    "(declare-fun fromIntent (Int) Object)\n" +
                    "(declare-fun fromIntent (Real) Object)\n" +
                    "(declare-fun fromIntent (Bool) Object)\n" +


                    "(declare-fun fromBundle (Object) Object)\n" +
                    "(declare-fun fromBundle (String) Object)\n" +
                    "(declare-fun fromBundle (Int) Object)\n" +
                    "(declare-fun fromBundle (Real) Object)\n" +
                    "(declare-fun fromBundle (Bool) Object)\n" +

                    "(declare-datatypes () ((ParamRef (mk-paramref (index Int) (type String) (method String)))))\n" +
                    "(declare-fun hasParamRef (Object) ParamRef)\n" +
                    "(declare-fun hasParamRef (String) ParamRef)\n" +
                    "(declare-fun hasParamRef (Int) ParamRef)\n" +
                    "(declare-fun hasParamRef (Real) ParamRef)\n" +
                    "(declare-fun hasParamRef (Bool) ParamRef)\n" +

                    "(declare-fun isNull (String) Bool)\n" +
                    "(declare-fun isNull (Object) Bool)\n" +
                    "(declare-fun oEquals (String Object) Bool)\n" +
                    "(declare-fun oEquals (Object String) Bool)\n" +
                    "(declare-const cats (Array Int String))\n" +
                    "(declare-const keys (Array Int String))\n";
            for (String d : decls) {
                outSpec += d + "\n";
            }
            for (String c : pathCond) {
                outSpec += c + "\n";
            }

            String addAssert = addAssertToBlockRandomStringValue(decls, pathCond);
            outSpec += addAssert;

            outSpec += "(check-sat-using (then qe smt))\n";
            outSpec += "(get-model)\n";
            //exceptionLogger.debug("z3 specification sent to solver:");
            //exceptionLogger.debug(outSpec);
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&finalSolve&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println(outSpec);


            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            out.print(outSpec);

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        ProcessBuilder pb = new ProcessBuilder("z3-4.7.1-x64-ubuntu-16.04/bin/z3", pathCondFileName);

        Process p = null;
        String returnedOutput = null;
        try {
            p = pb.start();
            int errCode = p.waitFor();

            returnedOutput = convertStreamToString(p.getInputStream());//returnedOutput represent result whether errcode equals 0
            System.out.println(returnedOutput);

            if (errCode != 0) {
                String errorOut = convertStreamToString(p.getErrorStream());
                System.out.println("errCode:" + errCode + "*" + errorOut + "*");
                WriteFile writeFile = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "errorSymbolicExcuation.txt", true, exceptionLogger);
                writeFile.writeStr(outSpec + "\n" + "errCode:" + errCode + "*" + returnedOutput + "*" + "\n");
                writeFile.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, String> model = new LinkedHashMap<String, String>();
        Pattern pat = Pattern.compile("\\s+\\(define-fun\\s+(\\S+)\\s+\\(\\)\\s+\\w+\\s+(.+)(?=\\))");//取出常数放入model
        //\\s+   多个空白（Matches the whitespace. Equivalent to [\t\n\r\f].）
        //(\S+) ：m.group(1)  函数名
        //  \\(\\)   （）
        //  \\w+  多个Matches the word characters. 返回类型
        //   (.+)  m.group(2)  .: Matches any single character except newline. Using m option allows it to match the newline as well.
        //   (?=\\)) 最后是一个括号,不占匹配的字符
        Matcher m = pat.matcher(returnedOutput);
        while (m.find()) {
            String symbol = m.group(1);
            String value = m.group(2);
            value = modifyValue(value);
            model.put(symbol, value);
        }

        String[] outLines = returnedOutput.split("\\n");
        Boolean isSat = false;
        for (String line : outLines) {
            if (line.trim().equals("sat"))
                isSat = true;
        }
        if (!isSat) {
            WriteFile writeFile = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "ifeasiblePath.txt", true, exceptionLogger);
            writeFile.writeStr("11111111111111111111111111111111111111111111111111111" + new File(appPath).getName() + "\n");
            writeFile.writeStr(outSpec + "\n");
            writeFile.writeStr("22222222222222222222222222222222222222222222222222222\n");
            writeFile.close();
        }
        return new Pair<Map<String, String>, Boolean>(model, isSat);

    }

    public static String modifyValue(String value) {
        if (value.contains("!0!")) {
            return "!0!";
        }
        int index = 0;
        int count = 0;
        while (index != -1) {//含有多个ZMS！
            index = value.indexOf("ZMS!", index);
            if (index != -1) {
                count++;
                index = index + 4;
            }
        }
        if (count >= 2) {
            return "!0!";
        }

        return value;


    }

    public static String addAssertToBlockRandomStringValue(Set<String> decls, Set<String> pathCond) {

        Set<String> addAssertSet = new HashSet<>();
        Pattern patStr = Pattern.compile("\\s*\\(\\s*declare-const\\s+(\\S+)\\s+String\\s*\\)\\s*");
        Set<String> strVarSet = new HashSet<>();

        for (String decl : decls) {
            Matcher m = patStr.matcher(decl);
            while (m.find()) {
                String strVar = m.group(1);
                strVarSet.add(strVar);
                Set<String> hashSet = new HashSet<>();
                hashSet.add(strVar);

            }

        }

        //condExpr = "(assert (not (= " + opExpr1 + " " + opExpr2 + ")))";

        Pattern strAssertPat = Pattern.compile("\\s*\\(\\s*assert\\s+\\(=\\s+(\\S+)\\s+(\\(str\\.\\+\\+\\s+\\\"ZMS!\\\"\\s+(\\S+)\\)|(\\S+))\\s*\\)\\s*\\)\\s*");
        Set<String> hasStringConstantSet = new HashSet<>();
        UnionFindForest<String> unionFindForest = new UnionFindForest(strVarSet);
        //(assert (= $r3_java.lang.String_onCreate_com.example.lab418.testwebview2.TestUnUsedIFBlockAlgorithm_13 (str.++ "ZMS!" "android.action.zms")))
        for (String cond : pathCond) {
            Matcher m = strAssertPat.matcher(cond);
            while (m.find()) {
                String strVar1 = m.group(1);
                String strVar2 = m.group(2);
                if (strVar2.contains("ZMS!")) {
                    strVar2 = m.group(3);
                }
                if ((strVarSet.contains(strVar1) || strVar1.contains("\"")) && (strVarSet.contains(strVar2) || strVar2.contains("\""))) {
                    if (!strVar1.contains("\"")) {


                        if (strVar2.contains("\"")) {
                            hasStringConstantSet.add(strVar1);
                        } else {
                            unionFindForest.unionTwoNode(unionFindForest.unionFindNodeMap.get(strVar1), unionFindForest.unionFindNodeMap.get(strVar2));
                        }

                    }

                    if (!strVar2.contains("\"")) {


                        if (strVar1.contains("\"")) {
                            hasStringConstantSet.add(strVar2);
                        } else {
                            unionFindForest.unionTwoNode(unionFindForest.unionFindNodeMap.get(strVar1), unionFindForest.unionFindNodeMap.get(strVar2));
                        }
                    }


                }
            }

        }

        HashSet<UnionFindForest.unionFindNode> rootSet = new HashSet<>();

        for (Map.Entry<String, UnionFindForest.unionFindNode> entry : unionFindForest.unionFindNodeMap.entrySet()) {
            UnionFindForest.unionFindNode unionFindNode = entry.getValue();
            if (unionFindNode.parent == unionFindNode) {
                rootSet.add(unionFindNode);
            }
        }

        for (String hasValueStrVar : hasStringConstantSet) {
            UnionFindForest.unionFindNode unionFindNode = unionFindForest.unionFindNodeMap.get(hasValueStrVar);
            UnionFindForest.unionFindNode hasValueRootNode = unionFindForest.find_set(unionFindNode);
            if (hasValueRootNode == null) {
                throw new RuntimeException("算法错误！");
            } else {
                rootSet.remove(hasValueRootNode);
            }
        }

        for (UnionFindForest.unionFindNode unionFindNode : rootSet) {
            addAssertSet.add("(assert (= " + unionFindNode.value + " " + "\"!0!\"" + "))");
        }


        String returnStr = "";
        for (String str : addAssertSet) {
            returnStr = str + "\n" + returnStr;
        }


        return returnStr;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");// \A表示字符串的开头 为了消除\的转义作用,多加一个\
        return s.hasNext() ? s.next() : "";
    }

    class TargetUnitInMethodInfo {

        Unit unit;

        Edge edge;

        SootMethod sootMethod;

        MyUnitGraph myUnitGraph;

        Set<UnitPath> unitPaths;

        public TargetUnitInMethodInfo(MyPairUnitToEdge myPairUnitToEdge, SootMethod sootMethod, MyUnitGraph myUnitGraph, Set<UnitPath> unitPaths) {
            this.unit = myPairUnitToEdge.srcUnit;
            this.edge = myPairUnitToEdge.outEdge;
            this.sootMethod = sootMethod;
            this.myUnitGraph = myUnitGraph;
            this.unitPaths = unitPaths;
        }
    }

    public class IntentSoln {

        public boolean isFeasible;

        public Intent intent;

        public IntentSoln(boolean isFeasible, Intent intent) {
            this.isFeasible = isFeasible;
            this.intent = intent;
        }
    }

    class UnitPath {
        Set<String> pathCond;
        Set<String> decl;
        List<Unit> path;
        IntentSoln intentSoln;


        public UnitPath(Set<String> currPathCond, Set<String> currDecls, List<Unit> currPath) {
            this.pathCond = currPathCond;
            this.decl = currDecls;
            this.path = currPath;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UnitPath unitPath = (UnitPath) o;

            if (!pathCond.equals(unitPath.pathCond)) return false;
            if (!decl.equals(unitPath.decl)) return false;
            return path.equals(unitPath.path);

        }

        @Override
        public int hashCode() {
            int result = pathCond.hashCode();
            result = 31 * result + decl.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }

        public Set<String> getPathCond() {
            return pathCond;
        }

        public void setPathCond(Set<String> pathCond) {
            this.pathCond = pathCond;
        }

        public Set<String> getDecl() {
            return decl;
        }

        public void setDecl(Set<String> decl) {
            this.decl = decl;
        }

        public List<Unit> getPath() {
            return path;
        }

        public void setPath(List<Unit> path) {
            this.path = path;
        }
    }

    private void analysisPathIntraMethod(List<Unit> path, SootMethod sootMethod, SimpleLocalDefs defs, Set<String> currPathCond, Set<String> currDecls) {


        for (Unit unit : path) {
            Stmt stmt = (Stmt) unit;
            if (stmt instanceof IfStmt) {
                IfStmt ifStmt = (IfStmt) stmt;
                ConditionExpr conditionExpr = (ConditionExpr) ifStmt.getCondition();
                Unit succUnit = null;
                List<Unit> currPathList = new ArrayList<Unit>(path);
                int indexOfUnit = currPathList.indexOf(unit);
                if (indexOfUnit == -1) {
                    MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error(unit + " is not in path");
                    throw new RuntimeException(unit + " is not in path");
                }
                Unit succ = currPathList.get(indexOfUnit - 1);

                seUnHandleProcessSatistic.addAllCount();
                Set<String> newExprs = handleIfStmt(ifStmt, path, sootMethod, defs, currDecls, succ);
                if (newExprs != null) {
                    currPathCond.addAll(newExprs);
                }


            }

            if (stmt.containsInvokeExpr() && stmt instanceof DefinitionStmt) {


                handleGetActionOfIntent(sootMethod, path, currPathCond, currDecls, defs, stmt);//对intent的action语句进行声明,例如：xxx=ie.getAction()  定义Intent ie ，约束 xxx来源于ie的getAction
                handleGetExtraOfIntent(sootMethod, path, currPathCond, currDecls, defs, stmt);//

                //category
                //data


            }


        }


    }


    private void handleGetExtraOfIntent(SootMethod method, List<Unit> currPath, Set<String> currPathCond, Set<String> currDecls, SimpleLocalDefs defs, Stmt currStmtInPath) {
        DefinitionStmt defStmt = (DefinitionStmt) currStmtInPath;
        if (defStmt.containsInvokeExpr() && defStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr ie = (InstanceInvokeExpr) defStmt.getInvokeExpr();
            if (Pattern.matches("get.*Extra", ie.getMethod().getName())) {
                if (ie.getMethod().getDeclaringClass().toString().equals("android.content.Intent")) {
                    seUnHandleProcessSatistic.addAllCount();
                    Pair<Set<String>, Set<String>> exprPair = buildGetExtraData(defStmt, defs, ie, method, currPath);
                    currDecls.addAll(exprPair.getValue0());
                    currPathCond.addAll(exprPair.getValue1());
                }
            }
            if (Pattern.matches("get.*", ie.getMethod().getName())) {//defStmt:xxx=ie.get.*()  ie:android.os.Bundle
                if (ie.getMethod().getDeclaringClass().toString().equals("android.os.Bundle") || ie.getMethod().getDeclaringClass().toString().equals("android.os.BaseBundle")) {
                    seUnHandleProcessSatistic.addAllCount();
                    Pair<Set<String>, Set<String>> exprPair = buildGetBundleData(defStmt, defs, ie, method, currPath);
                    currDecls.addAll(exprPair.getValue0());
                    currPathCond.addAll(exprPair.getValue1());
                }
            }
        }
    }

    private Pair<Set<String>, Set<String>> buildGetBundleData(Unit currUnit, SimpleLocalDefs defs, InstanceInvokeExpr ie, SootMethod method, List<Unit> currPath) {//ok
        Set<String> newDecls = new LinkedHashSet<String>();//currUnit:xxx=ie.get.*(arg1,arg2)   ie:android.os.Bundle
        Set<String> newAsserts = new LinkedHashSet<String>();
        Value arg1 = ie.getArg(0);
        Value arg2 = null;
        if (ie.getArgCount() > 1) {
            arg2 = ie.getArg(1);
        }

        String extraType = null;

        if (arg2 != null) {
            extraType = arg2.getType().toString();
        } else {
            extraType = ie.getMethod().getReturnType().toString();
        }

        String arg2Str = "unk";
        if (arg2 != null) {
            arg2Str = arg2.toString();
        }

        if (arg1 instanceof StringConstant) {
            StringConstant keyStrConst = (StringConstant) arg1;
            if (ie.getBase() instanceof Local) {
                Local bundleLocal = (Local) ie.getBase();
                for (Unit bundleDef : defs.getDefsOfAt(bundleLocal, currUnit)) {
					/*if (!currPath.contains(intentDef)) {
						continue;
					}*/
                    if (!isDefInPathAndLatest(currPath, bundleDef, bundleLocal, currUnit, defs)) {
                        continue;
                    }
                    DefinitionStmt bundleDefStmt = (DefinitionStmt) bundleDef;//bundleDefStmt:bundle对象的定义语句
                    if (bundleDefStmt.containsInvokeExpr()) {
                        if (bundleDefStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr iie = (InstanceInvokeExpr) bundleDefStmt.getInvokeExpr();
                            if (iie.getBase().getType().toString().equals("android.content.Intent")) {//iie:android.content.Intent
                                if (iie.getBase() instanceof Local) {
                                    Local intentLocal = (Local) iie.getBase();
                                    for (Unit intentDef : defs.getDefsOfAt(intentLocal, bundleDefStmt)) {
                                        if (!isDefInPathAndLatest(currPath, intentDef, intentLocal, bundleDefStmt, defs)) {
                                            continue;
                                        }

                                        if (currUnit instanceof DefinitionStmt) {
                                            DefinitionStmt defStmt = (DefinitionStmt) currUnit;////currUnit:xxx=ie.get.*(arg1,arg2)   ie:android.os.Bundle
                                            if (defStmt.getLeftOp() instanceof Local) {//
                                                Local extraLocal = (Local) defStmt.getLeftOp();//extraLocal 是xxx,保存extra值的变量
                                                String extraLocalSymbol = createSymbol(extraLocal, method, defStmt);
                                                symbolLocalMap.put(extraLocalSymbol, extraLocal);
                                                symbolDefUnitMap.put(extraLocalSymbol, defStmt);
                                                String intentSymbol = createSymbol(intentLocal, method, intentDef);//包括intent在哪个位置定义的（什么类，什么方法，方法中哪个位置）
                                                symbolLocalMap.put(intentSymbol, intentLocal);
                                                symbolDefUnitMap.put(intentSymbol, intentDef);
                                                String bundleLocalSymbol = createSymbol(bundleLocal, method, bundleDef);
                                                symbolLocalMap.put(bundleLocalSymbol, bundleLocal);
                                                symbolDefUnitMap.put(bundleLocalSymbol, bundleDef);
                                                String newExtraType = getZ3Type(extraLocal.getType());
                                                String newIntentType = getZ3Type(intentLocal.getType());
                                                newDecls.add("(declare-const " + extraLocalSymbol + " " + newExtraType + " )");
                                                newDecls.add("(declare-const " + intentSymbol + " " + newIntentType + " )");

                                                if (iie.getMethod().getName().equals("getExtras")) {
                                                    newAsserts.add("(assert (= (containsKey " + intentSymbol + " \"" + keyStrConst.value + "\") true))");
                                                    newAsserts.add("(assert (= (fromIntent " + extraLocalSymbol + ") " + intentSymbol + "))");
                                                    extraFromMap.put(extraLocalSymbol, intentSymbol);
                                                    extraDataMapKey.put(extraLocalSymbol, keyStrConst.value);
                                                } else if (iie.getMethod().getName().equals("getBundleExtra")) {//已经在intent get*Extra做过了bundeldata=intent.getBundleExtra()

                                                    newAsserts.add("(assert (= (containsKey " + bundleLocalSymbol + " \"" + keyStrConst.value + "\") true))");
                                                    extraFromMap.put(extraLocalSymbol, bundleLocalSymbol);
                                                    extraDataMapKey.put(extraLocalSymbol, keyStrConst.value);
                                                    newAsserts.add("(assert (= (fromBundle " + extraLocalSymbol + ") " + bundleLocalSymbol + "))");

//                                                    extraFromMap.put(bundleLocalSymbol, intentSymbol);//已经被做过了
//                                                    Value getBundleExtraArg=iie.getArg(0);
//                                                    if(getBundleExtraArg instanceof StringConstant)
//                                                    {
//
//                                                        extraDataMapKey.put(bundleLocalSymbol,((StringConstant) getBundleExtraArg).value);
//                                                    }
//                                                    newAsserts.add("(assert (= (fromIntent " + bundleLocalSymbol + ") " + intentSymbol + "))");

                                                }


                                                buildParamRefExpressions(method, currPath, newAsserts, newDecls, intentDef, intentSymbol);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        return new Pair<Set<String>, Set<String>>(newDecls, newAsserts);
    }

    private Pair<Set<String>, Set<String>> buildGetExtraData(Unit currUnit, SimpleLocalDefs defs, InstanceInvokeExpr ie, SootMethod method, List<Unit> currPath) {//ok
        Set<String> newDecls = new LinkedHashSet<String>();
        Set<String> newAsserts = new LinkedHashSet<String>();
        Value arg1 = ie.getArg(0);
        Value arg2 = null;
        if (ie.getArgCount() > 1) {
            arg2 = ie.getArg(1);
        }

        String extraType = null;

        if (arg2 != null) {
            extraType = arg2.getType().toString();//默认值
        } else {
            extraType = ie.getMethod().getReturnType().toString();
        }

        String arg2Str = "unk";
        if (arg2 != null) {
            arg2Str = arg2.toString();
        }

        if (arg1 instanceof StringConstant) {
            StringConstant keyStrConst = (StringConstant) arg1;
            if (ie.getBase() instanceof Local) {
                Local intentLocal = (Local) ie.getBase();
                for (Unit intentDef : defs.getDefsOfAt(intentLocal, currUnit)) {//currUnit  例如：xxx=ie.getStringExtra("zz","t")
					/*if (!currPath.contains(intentDef)) {
						continue;
					}*/
                    if (!isDefInPathAndLatest(currPath, intentDef, intentLocal, currUnit, defs)) {
                        continue;
                    }

                    if (currUnit instanceof DefinitionStmt) {
                        DefinitionStmt defStmt = (DefinitionStmt) currUnit;
                        if (defStmt.getLeftOp() instanceof Local) {//
                            Local extraLocal = (Local) defStmt.getLeftOp();
                            String extraLocalSymbol = createSymbol(extraLocal, method, defStmt);
                            symbolLocalMap.put(extraLocalSymbol, extraLocal);
                            symbolDefUnitMap.put(extraLocalSymbol, defStmt);
                            String intentSymbol = createSymbol(intentLocal, method, intentDef);
                            symbolLocalMap.put(intentSymbol, intentLocal);
                            symbolDefUnitMap.put(intentSymbol, intentDef);
                            String newExtraType = getZ3Type(extraLocal.getType());
                            String newIntentType = getZ3Type(intentLocal.getType());
                            newDecls.add("(declare-const " + extraLocalSymbol + " " + newExtraType + " )");
                            newDecls.add("(declare-const " + intentSymbol + " " + newIntentType + " )");

                            newAsserts.add("(assert (= (containsKey " + intentSymbol + " \"" + keyStrConst.value + "\") true))");
                            newAsserts.add("(assert (= (fromIntent " + extraLocalSymbol + ") " + intentSymbol + "))");
                            extraFromMap.put(extraLocalSymbol, intentSymbol);
                            extraDataMapKey.put(extraLocalSymbol, keyStrConst.value);


                            buildParamRefExpressions(method, currPath, newAsserts, newDecls, intentDef, intentSymbol);
                        }
                    }
                }
            }
        }
        return new Pair<Set<String>, Set<String>>(newDecls, newAsserts);
    }

    private String getZ3Type(Type type) {
        switch (type.toString()) {
            case "short":
                return "Int";
            case "int":
                return "Int";
            case "long":
                return "Int";
            case "float":
                return "Real";
            case "double":
                return "Real";
            case "boolean":
                return "Bool";
            case "byte":
                return "Int";
            case "java.lang.String":
                return "String";
            default:
                return "Object";
        }
    }

    private void handleGetActionOfIntent(SootMethod method, List<Unit> currPath, Set<String> currPathCond, Set<String> currDecls, SimpleLocalDefs defs, Stmt currStmtInPath) {
        DefinitionStmt currDefStmt = (DefinitionStmt) currStmtInPath;
        InvokeExpr ie = currStmtInPath.getInvokeExpr();
        if (ie.getMethod().getName().equals("getAction")) {
            if (ie.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {

                seUnHandleProcessSatistic.addAllCount();

                if (ie instanceof InstanceInvokeExpr) {
                    InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
                    String actionRefSymbol = null;
                    if (currDefStmt.getLeftOp() instanceof Local) {
                        Local leftLocal = (Local) currDefStmt.getLeftOp();
                        actionRefSymbol = createSymbol(currDefStmt.getLeftOp(), method, currStmtInPath);
                        //symbolLocalMap.put(actionRefSymbol,leftLocal);
                        if (iie.getBase() instanceof Local) {
                            Local intentLocal = (Local) iie.getBase();
                            for (Unit intentDef : defs.getDefsOfAt(intentLocal, currDefStmt)) {

                                if (!isDefInPathAndLatest(currPath, intentDef, intentLocal, currDefStmt, defs)) {
                                    continue;
                                }

                                if (!currPath.contains(intentDef)) {
                                    MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("isDefInPathAndLatest工作不正常");
                                    throw new RuntimeException("isDefInPathAndLatest工作不正常");
                                }

                                String getActionSymbol = createSymbol(leftLocal, method, intentDef);
                                symbolLocalMap.put(getActionSymbol, intentLocal);
                                String intentSymbol = createSymbol(intentLocal, method, intentDef);
                                symbolLocalMap.put(intentSymbol, intentLocal);

                                String intentDecl = "(declare-const " + intentSymbol + " Object )";
                                String actionRefDecl = "(declare-const " + actionRefSymbol + " String )";

                                currDecls.add(intentDecl);
                                currDecls.add(actionRefDecl);

                                String getActionAssert = "(assert (= (getAction " + intentSymbol + ") " + actionRefSymbol + "))";

                                currPathCond.add(getActionAssert);


                                buildParamRefExpressions(method, currPath, currPathCond, currDecls, intentDef, intentSymbol);//intent的来源参数的话，再加一些decl,assert

                                //intent来源于getIntent呢？不用
                                //对Field暂时不考虑，不知道哪里被赋值
                                //强制转化而来，更不考虑。情况少
                                // 来源于参数的话，那么这个Intent在调用者方法中可能还有约束


                            }
                        }
                    }
                } else {
                    MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("getAction竟然不是实例调用");

                    throw new RuntimeException("getAction竟然不是实例调用");
                }
            }
        }
    }

    private void buildParamRefExpressions(SootMethod method, List<Unit> currPath, Set<String> currPathCond, Set<String> currDecls, Unit intentDef, String intentSymbol) {
        if (intentDef instanceof DefinitionStmt) {
            DefinitionStmt defStmt = (DefinitionStmt) intentDef;
            if (!currPath.contains(defStmt)) {
                return;
            }
            if (defStmt.getRightOp() instanceof ParameterRef) {//判断intent是否来源于参数
                ParameterRef pr = (ParameterRef) defStmt.getRightOp();

                String prSymbol = createParamRefSymbol(defStmt.getLeftOp(), pr.getIndex(), method, defStmt);

                currDecls.add("(declare-const " + prSymbol + " ParamRef)");
                currPathCond.add("(assert ( = (index " + prSymbol + ") " + pr.getIndex() + "))");
                currPathCond.add("(assert ( = (type " + prSymbol + ") \"" + pr.getType() + "\"))");
                currPathCond.add("(assert ( = (method " + prSymbol + ") \"" + method.getDeclaringClass().getName() + "." + method.getName() + "\"))");
                currPathCond.add("(assert (= (hasParamRef " + intentSymbol + ") " + prSymbol + "))");
            }
        }
    }

    private static String createParamRefSymbol(Value opVal, int index, SootMethod method, Unit unit) {
        String valNameNoDollar = opVal.toString();
        BytecodeOffsetTag bcoTag = null;
        for (Tag tag : unit.getTags()) {
            if (tag instanceof BytecodeOffsetTag) {
                bcoTag = (BytecodeOffsetTag) tag;
            }
        }
        String symbol = null;
        if (bcoTag != null)
            symbol = "pr" + index + "_" + convertTypeNameForZ3(opVal.getType()) + "_" + method.getName() + "_" + method.getDeclaringClass().getName() + "_" + bcoTag.toString();
        else
            symbol = "pr" + index + "_" + convertTypeNameForZ3(opVal.getType()) + "_" + method.getName() + "_" + method.getDeclaringClass().getName();
        return symbol;
    }

    private static String createSymbol(Value opVal, SootMethod method, Unit unit) {
        String valNameNoDollar = opVal.toString();
        BytecodeOffsetTag bcoTag = null;
        for (Tag tag : unit.getTags()) {
            if (tag instanceof BytecodeOffsetTag) {
                bcoTag = (BytecodeOffsetTag) tag;
            }
        }
        String symbol = null;
        if (bcoTag != null)
            symbol = valNameNoDollar + "_" + convertTypeNameForZ3(opVal.getType()) + "_" + method.getName() + "_" + method.getDeclaringClass().getName() + "_" + bcoTag.toString();
        else
            symbol = valNameNoDollar + "_" + convertTypeNameForZ3(opVal.getType()) + "_" + method.getName() + "_" + method.getDeclaringClass().getName();
        return symbol;
    }

    private static String convertTypeNameForZ3(Type type) {
        String returnStr = type.toString();
        returnStr = returnStr.replace("[]", "-Arr");
        return returnStr;
    }

    private boolean isDefInPathAndLatest(List<Unit> path, Unit inDef, Local usedLocal, Unit usedUnit, SimpleLocalDefs defs) {//---------------------------------------
        if (!MyUnitGraphFactory.getCurMyUnitGraph().allIntentUnitFromStartToTargetUnitInpath.contains(usedUnit)) {
            WriteFile writeFile = new WriteFile("AnalysisAPKIntent/testFunctions/" + "Error.txt", true, exceptionLogger);
            writeFile.writeStr("allIntentUnitFromStartToTargetUnitInpath don't contains " + usedUnit + "&&" + appPath + "\n");
            writeFile.close();
            exceptionLogger.error("allIntentUnitFromStartToTargetUnitInpath don't contains " + usedUnit + "&&" + appPath + "\n");
        }
        if (path.contains(inDef)) { // does the path contain the definition
            for (Unit otherDef : defs.getDefsOfAt(usedLocal, usedUnit)) { // check other defs of usedLocal at usedUnit to determine if inDef is the latestDef in path
                if (inDef.equals(otherDef)) { // continue if inDef equals otherDef
                    continue;
                }
                if (!path.contains(otherDef)) { // if the otherDef is not in path, then continue
                    continue;
                }
                List<Unit> cipList = new ArrayList<Unit>(path);
                int inDefPos = cipList.indexOf(inDef);
                int argDef2Pos = cipList.indexOf(otherDef);//
                if (inDefPos > argDef2Pos) { // if inDef's position in the path is earlier then otherDef's position, then inDef is not the latest definition in the path, so return false

                    //-------------是可能的，可能的for循环中的i
                    WriteFile writeFile = new WriteFile("AnalysisAPKIntent/testFunctions/" + "isDefInPathAndLatest.txt", true, exceptionLogger);
                    writeFile.writeStr(usedLocal + " " + inDef + " " + otherDef + " #" + usedUnit + "# " + appPath + "\n");
                    writeFile.close();
                    return false;
                }
            }
            return true; // inDef is in the path and is the latest definition along that path
        } else { // inDef is not in the path, so return false
            //seUnHandleProcessSatistic.addUnHandleCount();
            if ((!MyUnitGraphFactory.getCurMyUnitGraph().allIntentUnitFromStartToTargetUnitInpath.contains(inDef)) && MyUnitGraphFactory.getCurMyUnitGraph().allIntentUnitFromStartToTargetUnitInpath.contains(usedUnit)) {
                WriteFile writeFile = new WriteFile("AnalysisAPKIntent/testFunctions/" + "isDefInPathAndLatest.txt", true, exceptionLogger);
                writeFile.writeStr(usedLocal + " " + inDef + " " + "not in path" + " #" + usedUnit + "# " + appPath + "\n");
                writeFile.close();
                //exceptionLogger.error(usedLocal + " " + inDef + " " + "not in path" + " #" + usedUnit + "# " + appPath + "\n");
            }

            return false;
        }
    }

    private Value opVal1Org = null;
    private Value opVal2Org = null;
    private String symbolOrg = null;

    private boolean ifBooleanTypeConditionValue() {
        if (opVal1Org.getType().toString().equals("boolean") && opVal2Org.getType().toString().equals("int")) {
            if (symbolOrg.trim().equals("==")) {

                if (opVal2Org.toString().equals("0")) {
                    return true;
                } else if (opVal2Org.toString().equals("1")) {
                    return false;
                }
                throw new RuntimeException("use this method error, not 0 or 1 ==");


            } else if (symbolOrg.trim().equals("!=")) {

                if (opVal2Org.toString().equals("0")) {
                    return false;
                } else if (opVal2Org.toString().equals("1")) {
                    return true;
                }
                throw new RuntimeException("use this method error, not 0 or 1  !=");


            }
        }

        throw new RuntimeException("use this method error");


    }

    private Set<String> handleIfStmt(IfStmt ifStmt, List<Unit> path, SootMethod sootMethod, SimpleLocalDefs defs, Set<String> decls, Unit succUnit) {

        //只用生成关于if语句的表达式，intent的表达式都在intent方法里生成了

        String returnExpr = "";


        String opVal1Assert = null;
        String opVal2Assert = null;

        Unit opVal1DefUnit = null;
        Unit opVal2DefUnit = null;


        ConditionExpr condition = (ConditionExpr) ifStmt.getCondition();

        Value conditionLeft = condition.getOp1();
        Value conditionRight = condition.getOp2();


        opVal1Org = conditionLeft;
        opVal2Org = conditionRight;
        symbolOrg = condition.getSymbol();


        Value opVal1 = conditionLeft;

        Value opVal2 = conditionRight;


        if (new File(Config.intentConditionSymbolicExcutationResults + "/" + "if.txt").length() < 104857600) {
            WriteFile writeFile = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "if.txt", true, exceptionLogger);
            writeFile.writeStr(conditionLeft.getType().toString() + "***" + condition + "$$$" + conditionRight.getType().toString() + "###" + ifStmt + "\n");
            writeFile.close();
        }


        boolean generateCondExpr = true;


        if (conditionLeft instanceof Local) {
            Local localConditionLeft = (Local) conditionLeft;
            List<Unit> defineLocalUnits = defs.getDefsOfAt(localConditionLeft, ifStmt);

            for (Unit defUnitConditionLeft : defineLocalUnits)//这个可能会有多个值，比如当前面有if语句,因为不确定不确定执行哪一个。
            {
                if (!isDefInPathAndLatest(path, defUnitConditionLeft, localConditionLeft, ifStmt, defs))//注意这个定义其的语句可能不在当前路径中
                {
                    continue;
                }

                if (defUnitConditionLeft instanceof DefinitionStmt) {//defUnitConditionLeft是定义if语句左边变量的语句  9484/9484
                    DefinitionStmt defDefintionStmtConditionLeft = (DefinitionStmt) defUnitConditionLeft;

                    if (opVal1.getType() instanceof ByteType) {//ByteType    $b0= 3>5   $b0就为ByteType

                        Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> condValuesPair = findLeftAndRightValuesOfByteVal(sootMethod, defs, ifStmt, opVal1, path);
                        Quartet<Value, String, String, Unit> left = condValuesPair.getValue0();
                        Quartet<Value, String, String, Unit> right = condValuesPair.getValue1();

                        if (left != null) {
                            opVal1 = left.getValue0();

                            if (left.getValue1() != null) {
                                decls.add(left.getValue1());
                            }


                            if (left.getValue2() != null) {
                                opVal1Assert = left.getValue2();
                            }

                            opVal1DefUnit = left.getValue3();
                        }

                        if (right != null) {
                            opVal2 = right.getValue0();

                            if (right.getValue1() != null) {
                                decls.add(right.getValue1());
                            }

                            if (right.getValue2() != null) {
                                opVal2Assert = right.getValue2();
                            }

                            opVal2DefUnit = right.getValue3();
                        }


                    } else

                        //基本类型
                        //if (conditionLeft.getType() instanceof PrimType) {
                        if (conditionLeft.getType().toString().equals("boolean")) {// 5196/7726

                            if (defDefintionStmtConditionLeft.getRightOp() instanceof JVirtualInvokeExpr) {//  7726/9484   只分析这个
                                JVirtualInvokeExpr jVInvokeExpr = (JVirtualInvokeExpr) defDefintionStmtConditionLeft.getRightOp();


                                Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> pair_quartet_left_and_right = findEqualsBoolean(ifStmt, path, sootMethod, defs, defDefintionStmtConditionLeft, jVInvokeExpr);
                                Quartet<Value, String, String, Unit> leftValue = pair_quartet_left_and_right.getValue0();
                                Quartet<Value, String, String, Unit> rightValue = pair_quartet_left_and_right.getValue1();//注意 equals fallthough 没有判定，ifStmt约束还没有生成，所以其generateCondExpr为true

                                if (leftValue == null && rightValue == null) {//不是equals方法
                                    pair_quartet_left_and_right = findIntentBooleanValues(sootMethod, defs, ifStmt, conditionLeft, path, jVInvokeExpr, defDefintionStmtConditionLeft);


                                    if (pair_quartet_left_and_right == null) {//不是IntentBoolean方法
                                        pair_quartet_left_and_right = findBundleValues(sootMethod, defs, ifStmt, conditionLeft, path);//if match this case  leftValue！=null  rightValue=null
                                        leftValue = pair_quartet_left_and_right.getValue0();
                                        rightValue = pair_quartet_left_and_right.getValue1();
                                        if (leftValue == null) {//都不满足

                                            UnHandleWriter.write(appPath + "%%1111111111" + jVInvokeExpr + "这种jVInvokeExpr boolean未考虑！\n");
//                                            seUnHandleProcessSatistic.addUnHandleCount();///111111111111
//                                            /media/mobile/myExperiment/apps/0_5_25_45_65_/铃声剪辑.apk%%1111111111virtualinvoke $r2.<java.lang.String: boolean startsWith(java.lang.String)>("|")这种jVInvokeExpr boolean未考虑！

                                            findKeysForLeftAndRightValues(ifStmt, opVal1, opVal2, defs, path);
                                            opVal1DefUnit = getDefOfValInPath(opVal1, ifStmt, path, defs);
                                            opVal2DefUnit = getDefOfValInPath(opVal2, ifStmt, path, defs);


                                        } else//BundleValue
                                        {
                                            generateCondExpr = false;

                                            if (leftValue != null) {
                                                if (leftValue.getValue1() != null) {
                                                    decls.add(leftValue.getValue1());
                                                }


                                                opVal1Assert = leftValue.getValue2();

                                                opVal1DefUnit = leftValue.getValue3();

                                            }

                                            if (rightValue != null) {
                                                if (rightValue.getValue1() != null) {
                                                    decls.add(rightValue.getValue1());
                                                }


                                                opVal2Assert = leftValue.getValue2();


                                                opVal2DefUnit = leftValue.getValue3();
                                            }

                                        }
                                    } else//IntentBooleanValues
                                    {
                                        generateCondExpr = false;

                                        leftValue = pair_quartet_left_and_right.getValue0();
                                        rightValue = pair_quartet_left_and_right.getValue1();

                                        if (leftValue != null) {
                                            if (leftValue.getValue1() != null) {
                                                decls.add(leftValue.getValue1());
                                            }


                                            opVal1Assert = leftValue.getValue2();

                                            opVal1DefUnit = leftValue.getValue3();

                                        }

                                        if (rightValue != null) {
                                            if (rightValue.getValue1() != null) {
                                                decls.add(rightValue.getValue1());
                                            }


                                            opVal2Assert = leftValue.getValue2();


                                            opVal2DefUnit = leftValue.getValue3();
                                        }

                                    }


                                } else //equals
                                {
                                    opVal1 = leftValue.getValue0();

                                    opVal2 = rightValue.getValue0();

                                    if (opVal1 == null && opVal2 == null) {

                                        UnHandleWriter.write(appPath + "%%" + "equals异常" + "\n");
                                        seUnHandleProcessSatistic.addUnHandleCount();

                                        MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("equals异常");

                                        throw new RuntimeException("equals异常");

                                    } else {


                                        if (leftValue != null) {
                                            if (leftValue.getValue1() != null) {
                                                decls.add(leftValue.getValue1());
                                            }


                                            opVal1Assert = leftValue.getValue2();

                                            opVal1DefUnit = leftValue.getValue3();

                                        }

                                        if (rightValue != null) {
                                            if (rightValue.getValue1() != null) {
                                                decls.add(rightValue.getValue1());
                                            }


                                            opVal2Assert = leftValue.getValue2();


                                            opVal2DefUnit = leftValue.getValue3();
                                        }


                                    }


                                }


//                                if (leftValue == null) {
//                                    pair_quartet_left_and_right = findBundleValues(sootMethod, defs, ifStmt, conditionLeft, path);//if match this case  leftValue！=null  rightValue=null
//                                    leftValue = pair_quartet_left_and_right.getValue0();
//                                    rightValue = pair_quartet_left_and_right.getValue1();
//
//                                    if (leftValue != null || rightValue != null) {
//                                        generateCondExpr = false;
//                                    }
//
//                                    if (leftValue != null) {
//                                        opVal1 = leftValue.getValue0();
//                                    }
//                                    if (rightValue != null) {
//                                        opVal2 = rightValue.getValue0();
//                                    }
//                                    AssignOpVals assignOpVals = new AssignOpVals(decls, opVal1Assert, opVal2Assert, opVal1, opVal2, leftValue, rightValue).invoke();
//                                    opVal1DefUnit = assignOpVals.getOpVal1DefUnit();
//                                    opVal2DefUnit = assignOpVals.getOpVal2DefUnit();
//                                    opVal1Assert = assignOpVals.getOpVal1Assert();
//                                    opVal2Assert = assignOpVals.getOpVal2Assert();
//                                }


//


//                            }//其他类型，比如float ，等
//                            else {
//
//                                if (jVInvokeExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {
//
//
//                                    MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(defDefintionStmtConditionLeft);//---------------------------------------------
//
//
//                                }
//
//
                            }

//
//
//                        }

                            //对象类型

//                        else if (conditionLeft.getType() instanceof RefLikeType) {
//
//                            if (conditionLeft.getType() instanceof ArrayType) {
//
//                                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(conditionLeft);//-----------------------------------
//
//                            }
//
//                            if (conditionLeft.getType() instanceof RefType) {
//                                if (conditionLeft.getType().toString().equals("java.lang.String")) {
//
//                                }
//                                if (conditionLeft.getType().toString().equals("android.os.Bundle")) {
//
//                                }
//                                if (conditionLeft.getType().toString().equals("android.content.Intent")) {
//
//                                }
//
//                            }
//
//                        }

                            else {
                                UnHandleWriter.write(appPath + "%%2222222222" + defDefintionStmtConditionLeft + "这种defDefintionStmtConditionLeft  boolean 未考虑！\n");
//                                seUnHandleProcessSatistic.addUnHandleCount();//////////2222222222222222222
///media/mobile/myExperiment/apps/0_5_25_45_65_/导航犬.apk%%2222222222$z0 = staticinvoke <android.text.TextUtils: boolean isEmpty(java.lang.CharSequence)>($r2)这种defDefintionStmtConditionLeft  boolean 未考虑！
                                findKeysForLeftAndRightValues(ifStmt, opVal1, opVal2, defs, path);
                                opVal1DefUnit = getDefOfValInPath(opVal1, ifStmt, path, defs);
                                opVal2DefUnit = getDefOfValInPath(opVal2, ifStmt, path, defs);

                            }


                        } else {

                            UnHandleWriter.write(appPath + "%%3333333333" + defDefintionStmtConditionLeft + "这种defDefintionStmtConditionLeft 非 boolean未考虑！\n");
//                            seUnHandleProcessSatistic.addUnHandleCount();///////333333333333333333333  在其他地方处理了
//                            media/mobile/myExperiment/apps/0_5_25_45_65_/城市旅游指南Guides.apk%%3333333333$r3 = virtualinvoke $r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>("referrer")这种defDefintionStmtConditionLeft 非 boolean未考虑！

                            findKeysForLeftAndRightValues(ifStmt, opVal1, opVal2, defs, path);
                            opVal1DefUnit = getDefOfValInPath(opVal1, ifStmt, path, defs);
                            opVal2DefUnit = getDefOfValInPath(opVal2, ifStmt, path, defs);

                        }


                }

            }

        }

        //生成if的表达式
        Set<String> returnExprs = new LinkedHashSet<String>();
        if (opVal1DefUnit == null && opVal2DefUnit == null && opVal1Assert == null && opVal2Assert == null) {
            //exceptionLogger.debug("No new information from this if stmt, so returning empty set of expressions");
            return returnExprs;
        }


        // if the curr unit to convert is an ifStmt ensure the symbol is not negated //如果要转换的curr单位是ifStmt，则确保该符号不是否定的
        boolean isFallThrough = isFallThrough(ifStmt, succUnit, (JimpleBody) sootMethod.getActiveBody());
        //boolean isFallThrough = isFallThrough(ifStmt, succUnit);
        String branchSensitiveSymbol = null;
        if (isFallThrough) {
            if (opVal1Org.getType() instanceof BooleanType) {

                if (ifBooleanTypeConditionValue()) {
                    branchSensitiveSymbol = "==";
                } else {
                    branchSensitiveSymbol = negateSymbol("==");
                }

            } else {

                branchSensitiveSymbol = negateSymbol(condition.getSymbol());//这里是有缺陷的
                //if ((getIntent().getFloatExtra("floatExtraGreatThan7", -1) > 7)==false)//这种情况会得出相反的结果
                //if (！(getIntent().getFloatExtra("floatExtraGreatThan7", -1) > 7))//这种情况会是对的，没办法得出有效的信息来区分这两种情况
                //if ((getIntent().getFloatExtra("floatExtraGreatThan7", -1) > 7)==true)//
                //if ((getIntent().getFloatExtra("floatExtraGreatThan7", -1) > 7))//四条语句对应的jimple语句无法区分


            }
        } else {
            if (opVal1Org.getType() instanceof BooleanType) {

                if (ifBooleanTypeConditionValue()) {
                    branchSensitiveSymbol = negateSymbol("==");
                } else {
                    branchSensitiveSymbol = "==";
                }

            } else {//byte type  do not need use ifBooleanTypeConditionValue()


                branchSensitiveSymbol = condition.getSymbol();
            }
        }


        if (generateCondExpr) {
            String opExpr1 = null;
            String opExpr2 = null;
            try {
                if (opVal1 == null) {
                    //exceptionLogger.debug("Could not resolve opVal1, so setting it to true");
                    opExpr1 = "";
                } else {
                    opExpr1 = createZ3Expr(opVal1, ifStmt, opVal1DefUnit, sootMethod, decls);
                }

                if (opVal2 == null) {
                    //exceptionLogger.debug("Could not resolve opVal2, so setting it to true");
                    opExpr2 = "";
                } else {
                    opExpr2 = createZ3Expr(opVal2, ifStmt, opVal2DefUnit, sootMethod, decls);
                }
            } catch (RuntimeException e) {
                //exceptionLogger.warn("caught exceptionLogger: ", e);
                return null;
            }

            if (opExpr1 == opExpr2 && opExpr1 == null) {//---------------------------------
                //exceptionLogger.debug("op1 and op2 are both null, so just returning true expression");
                return Collections.singleton(returnExpr);
            }
            returnExpr = buildZ3CondExpr(opExpr1, opExpr2, branchSensitiveSymbol);
            returnExprs.add(returnExpr);
        }
        if (opVal1Assert != null) {
            returnExprs.add(opVal1Assert);
        }
        if (opVal2Assert != null) {
            returnExprs.add(opVal2Assert);
        }


        return returnExprs;


    }

    private Unit getDefOfValInPath(Value opVal, Unit currUnit, List<Unit> currPath, SimpleLocalDefs defs) {
        Unit defUnit = null;
        if (opVal instanceof Local) {
            Local opLocal1 = (Local) opVal;
            for (Unit opLocalDefUnit : defs.getDefsOfAt(opLocal1, currUnit)) {
                if (currPath.contains(opLocalDefUnit)) {
                    defUnit = opLocalDefUnit;
                }
            }
        }
        return defUnit;
    }

    public void findKeysForLeftAndRightValues(Unit currUnit, Value opVal1, Value opVal2, SimpleLocalDefs defs, List<Unit> currPath) {
        findKeyForVal(currUnit, opVal1, defs, currPath);
        findKeyForVal(currUnit, opVal2, defs, currPath);
    }

    public Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> findLeftAndRightValuesOfByteVal(SootMethod method, SimpleLocalDefs defs, Unit inUnit, Value value, List<Unit> currPath) {
        Quartet<Value, String, String, Unit> leftVal = null;
        Quartet<Value, String, String, Unit> rightVal = null;
        if (value instanceof Local) {
            Local local = (Local) value;
            if (local.getType() instanceof ByteType) {
                List<Unit> potentialCmpUnits = defs.getDefsOfAt(local, inUnit);
                for (Unit potentialCmpUnit : potentialCmpUnits) {
					/*if (!currPath.contains(potentialCmpUnit)) {
						continue;
					}*/
                    if (!isDefInPathAndLatest(currPath, potentialCmpUnit, local, inUnit, defs)) {
                        continue;
                    }
                    if (potentialCmpUnit.toString().contains("cmp")) {
                        //exceptionLogger.debug("Found potential cmp* statement: " + potentialCmpUnit);
                        if (potentialCmpUnit instanceof DefinitionStmt) {
                            DefinitionStmt defStmt = (DefinitionStmt) potentialCmpUnit;
                            Value rightOp = defStmt.getRightOp();
                            if (rightOp instanceof AbstractJimpleIntBinopExpr) {
                                AbstractJimpleIntBinopExpr cmpExpr = (AbstractJimpleIntBinopExpr) rightOp;
                                leftVal = findOriginalVal(method, defs, potentialCmpUnit, cmpExpr.getOp1(), currPath);
                                rightVal = findOriginalVal(method, defs, potentialCmpUnit, cmpExpr.getOp2(), currPath);
                            }
                        }
                    }
                }
            }
        }
        return new Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>>(leftVal, rightVal);
    }

    public void findKeyForVal(Unit currUnit, Value opVal, SimpleLocalDefs defs, List<Unit> currPath) {
        if (opVal instanceof Local) {
            Local local = (Local) opVal;
            List<Unit> defUnits = defs.getDefsOfAt(local, currUnit);
            for (Unit defUnit : defUnits) {
				/*if (!currPath.contains(defUnit)) {
					continue;
				}*/
                if (!isDefInPathAndLatest(currPath, defUnit, local, currUnit, defs)) {
                    continue;
                }
                if (defUnit instanceof DefinitionStmt) {
                    DefinitionStmt defStmt = (DefinitionStmt) defUnit;
                    String key = extractKeyFromIntentExtra(defStmt, defs, currPath);

                }
            }
        }
    }

    private String negateSymbol(String symbol) {
        switch (symbol.trim()) {
            case "==":
                return "!=";
            case "!=":
                return "==";
            case ">":
                return "<=";
            case "<":
                return ">=";
            case ">=":
                return "<";
            case "<=":
                return ">";
            default:
                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("invalid symbol passed to negateSymbol(): " + symbol);
                throw new RuntimeException("invalid symbol passed to negateSymbol(): " + symbol);
        }
    }

    private String buildZ3CondExpr(String opExpr1, String opExpr2, String branchSensitiveSymbol) {//for string  if(== true false) error
        String returnExpr;
        String condExpr = null;

        switch (branchSensitiveSymbol.trim()) {
            case "==":
                if (opExpr2.equals("Null"))
                    condExpr = "(assert (= (isNull " + opExpr1 + ") true))";
                else if (isObjectEquals(opExpr1, opExpr2))
                    condExpr = "(assert (= (oEquals " + opExpr1 + " " + opExpr2 + ") true))";
                else if (opExpr1.contains("_boolean_")) {
                    if (opExpr2.equals("0")) {
                        condExpr = "(assert (= " + opExpr1 + " " + "true" + "))";
                    } else if (opExpr2.equals("1")) {
                        condExpr = "(assert (= " + opExpr1 + " " + "false" + "))";
                    } else {
                        condExpr = "(assert (= " + opExpr1 + " " + opExpr2 + "))";
                    }
                } else {
                    condExpr = "(assert (= " + opExpr1 + " " + opExpr2 + "))";
                }

                break;
            case "!=":
                if (opExpr2.equals("Null"))
                    condExpr = "(assert (= (isNull " + opExpr1 + ") false))";
                else if (isObjectEquals(opExpr1, opExpr2))
                    condExpr = "(assert (= (oEquals " + opExpr1 + " " + opExpr2 + ") false))";
                else {
                    if (opExpr1.contains("_java.lang.String_"))//string op1=string op2
                    {
                        String notString = "(str.++ " + "\"ZMS!\" " + opExpr2 + ")";
                        condExpr = "(assert (= " + opExpr1 + " " + notString + "))";


                        // condExpr = "(assert (not (= " + opExpr1 + " " + opExpr2 + ")))";

                    } else//!string op1  !string  op2
                    {
                        if (opExpr1.contains("_boolean_")) {
                            if (opExpr2.equals("0")) {
                                condExpr = "(assert (not (= " + opExpr1 + " " + "true" + ")))";
                            } else if (opExpr2.equals("1")) {
                                condExpr = "(assert (not (= " + opExpr1 + " " + "false" + ")))";
                            } else {
                                condExpr = "(assert (not (= " + opExpr1 + " " + opExpr2 + ")))";
                            }

                        } else {
                            condExpr = "(assert (not (= " + opExpr1 + " " + opExpr2 + ")))";
                        }

                    }

                }

                break;
            case ">":
                condExpr = "(assert (> " + opExpr1 + " " + opExpr2 + "))";
                break;
            case ">=":
                condExpr = "(assert (>= " + opExpr1 + " " + opExpr2 + "))";
                break;
            case "<":
                condExpr = "(assert (< " + opExpr1 + " " + opExpr2 + "))";
                break;
            case "<=":
                condExpr = "(assert (<= " + opExpr1 + " " + opExpr2 + "))";
                break;
        }
        //exceptionLogger.debug(Utils.createTabsStr(tabs) + "z3 conditional expr: " + condExpr);

        if (condExpr == null) {
            //exceptionLogger.error("currExpr should not be null");
            //exceptionLogger.debug("opExpr1: " + opExpr1);
            //exceptionLogger.debug("opExpr2: " + opExpr2);
            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("currExpr should not be null");
            throw new RuntimeException("currExpr should not be null");
        }
        returnExpr = condExpr;
        return returnExpr;
    }

    private boolean isObjectEquals(String opExpr1, String opExpr2) {
        if (opExpr1.contains("_java.lang.String_") && !opExpr2.contains("_java.lang.String_") && !opExpr2.contains("\""))//一个是String，另外一个不是String
            return true;
        else if (!opExpr1.contains("_java.lang.String_") && opExpr2.contains("_java.lang.String_") && !opExpr2.contains("\""))
            return true;
        else
            return false;//String  String  or   ！String   ！String
    }

    private String createZ3Expr(Value opVal, Unit currUnit, Unit defUnit, SootMethod method, Set<String> decls) {
        String opExpr = null;
        String newDecl = null;

        if (opVal instanceof IntConstant) {
            IntConstant intConst = (IntConstant) opVal;
            opExpr = Integer.toString(intConst.value);

        } else if (opVal instanceof LongConstant) {
            LongConstant longConst = (LongConstant) opVal;
            opExpr = Long.toString(longConst.value);
        } else if (opVal instanceof FloatConstant) {
            FloatConstant floatConst = (FloatConstant) opVal;
            opExpr = Float.toString(floatConst.value);
        } else if (opVal instanceof DoubleConstant) {
            DoubleConstant doubleConst = (DoubleConstant) opVal;
            opExpr = Double.toString(doubleConst.value);
        } else if (opVal instanceof NullConstant) {
            opExpr = "Null";
        } else if (opVal instanceof StringConstant) {
            StringConstant strConst = (StringConstant) opVal;
            opExpr = "\"" + strConst.value + "\"";
        } else if (opVal instanceof JimpleLocal) {
            JimpleLocal opLocal = (JimpleLocal) opVal;
            //exceptionLogger.debug(Utils.createTabsStr(tabs + 1) + "opLocal type: " + opLocal.getType());

            String symbol = null;

            DefinitionStmt defStmt = (DefinitionStmt) defUnit;
            if (defStmt.getLeftOp() == opVal) {
                symbol = createSymbol(opVal, method, defStmt);
                symbolLocalMap.put(symbol, opLocal);
                symbolDefUnitMap.put(symbol, defStmt);
                localSymbolMap.put(opLocal, symbol);
            }

            symbol = localSymbolMap.get(opLocal);
            if (symbol == null) {
                symbol = createSymbol(opVal, method, defUnit);
                symbolLocalMap.put(symbol, opLocal);
                symbolDefUnitMap.put(symbol, defStmt);
                localSymbolMap.put(opLocal, symbol);
            }


            switch (opLocal.getType().toString().trim()) {
                case "short":
                    newDecl = "(declare-const " + symbol + " Int )";
                    opExpr = symbol;
                    break;
                case "int":
                    newDecl = "(declare-const " + symbol + " Int )";
                    opExpr = symbol;
                    break;
                case "long":
                    newDecl = "(declare-const " + symbol + " Int )";
                    opExpr = symbol;
                    break;
                case "float":
                    newDecl = "(declare-const " + symbol + " Real )";
                    opExpr = symbol;
                    break;
                case "double":
                    newDecl = "(declare-const " + symbol + " Real )";
                    opExpr = symbol;
                    break;
                case "boolean":
                    newDecl = "(declare-const " + symbol + " Bool )";
                    opExpr = symbol;
                    break;
                case "byte":
                    newDecl = "(declare-const " + symbol + " Int )";
                    opExpr = symbol;
                    break;
                case "java.lang.String":
                    newDecl = "(declare-const " + symbol + " String )";
                    opExpr = symbol;
                    break;
                default:
                    // object is an arbitrary type so we'll mark it as null or not null
                    //exceptionLogger.debug("Creating object with symbol: " + symbol + " for Local " + opLocal + " in " + method);
                    newDecl = "(declare-const " + symbol + " Object )";
                    opExpr = symbol;
            }
            decls.add(newDecl);
        } else {
            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("I don't know what to do with this Value's type: " + opVal.getType());
            throw new RuntimeException("I don't know what to do with this Value's type: " + opVal.getType());
        }


        return opExpr;
    }

    private class AssignOpVals {
        private Set<String> decls;
        private String opVal1Assert;
        private String opVal2Assert;
        private Value opVal1;
        private Value opVal2;
        private Quartet<Value, String, String, Unit> left;
        private Quartet<Value, String, String, Unit> right;
        private Unit opVal1DefUnit;
        private Unit opVal2DefUnit;

        public AssignOpVals(Set<String> decls, String opVal1Assert, String opVal2Assert, Value opVal1, Value opVal2, Quartet<Value, String, String, Unit> left, Quartet<Value, String, String, Unit> right) {
            this.decls = decls;
            this.opVal1Assert = opVal1Assert;
            this.opVal2Assert = opVal2Assert;
            this.opVal1 = opVal1;
            this.opVal2 = opVal2;
            this.left = left;
            this.right = right;
        }

        public String getOpVal1Assert() {
            return opVal1Assert;
        }

        public String getOpVal2Assert() {
            return opVal2Assert;
        }

        public Unit getOpVal1DefUnit() {
            return opVal1DefUnit;
        }

        public Unit getOpVal2DefUnit() {
            return opVal2DefUnit;
        }

        public AssignOpVals invoke() {
            if (left != null) {
                if (left.getValue1() != null)
                    decls.add(left.getValue1());
                if (left.getValue2() != null)
                    opVal1Assert = left.getValue2();
                opVal1DefUnit = left.getValue3();
            }

            if (right != null) {
                if (right.getValue1() != null)
                    decls.add(right.getValue1());
                if (right.getValue2() != null)
                    opVal2Assert = right.getValue2();
                opVal2DefUnit = right.getValue3();
            }

            return this;
        }
    }

    private Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> findCategories(SootMethod method, SimpleLocalDefs defs, Unit inUnit, Value value, List<Unit> currPath) {
        Quartet<Value, String, String, Unit> leftVal = null;
        Quartet<Value, String, String, Unit> rightVal = null;
        if (value instanceof Local) {
            Local local = (Local) value;
            if (local.getType() instanceof BooleanType) {
                List<Unit> potentialStringEqualsUnits = defs.getDefsOfAt(local, inUnit);
                for (Unit pseUnit : potentialStringEqualsUnits) {
					/*if (!currPath.contains(pseUnit)) {
						continue;
					}*/
                    if (!isDefInPathAndLatest(currPath, pseUnit, local, inUnit, defs)) {
                        continue;
                    }

                    if (pseUnit instanceof DefinitionStmt) {
                        DefinitionStmt defStmt = (DefinitionStmt) pseUnit;
                        if (defStmt.getRightOp() instanceof JVirtualInvokeExpr) {
                            JVirtualInvokeExpr jviExpr = (JVirtualInvokeExpr) defStmt.getRightOp();
                            if (jviExpr.getMethod().getName().equals("hasCategory")) {
                                if (jviExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {
                                    if (jviExpr.getBase() instanceof Local) {
                                        Local intentLocal = (Local) jviExpr.getBase();
                                        for (Unit intentDef : defs.getDefsOfAt(intentLocal, defStmt)) {
											/*if (!currPath.contains(intentDef)) {
												continue;
											}*/
                                            if (!isDefInPathAndLatest(currPath, intentDef, intentLocal, defStmt, defs)) {
                                                continue;
                                            }
                                            String intentSymbol = createSymbol(intentLocal, method, intentDef);
                                            symbolLocalMap.put(intentSymbol, intentLocal);
                                            localSymbolMap.put(intentLocal, intentSymbol);
                                            symbolDefUnitMap.put(intentSymbol, intentDef);

                                            String category = null;
                                            if (jviExpr.getArg(0) instanceof StringConstant) {
                                                StringConstant catStrConst = (StringConstant) jviExpr.getArg(0);
                                                category = catStrConst.value;
                                            } else//--------------------------------------------------
                                            {
                                                UnHandleWriter.write(appPath + "%%" + "intent has category 的值来源于非 StringConstant！" + jviExpr + "\n");
                                                seUnHandleProcessSatistic.addUnHandleCount();
                                            }

                                            Body b = method.getActiveBody();
                                            UnitGraph ug = new BriefUnitGraph(b);

                                            List<Unit> currPathList = new ArrayList<Unit>(currPath);
                                            int indexOfUnit = currPathList.indexOf(inUnit);
                                            if (indexOfUnit == -1) {
                                                MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error(inUnit + " is not in path");
                                                throw new RuntimeException(inUnit + " is not in path");
                                            }
                                            Unit succ = currPathList.get(indexOfUnit - 1);

                                            //boolean isFallThrough = isFallThrough(inUnit, succ);
                                            boolean isFallThrough = isFallThrough(inUnit, succ, (JimpleBody) method.getActiveBody());

                                            String newAssert = null;
                                            if (isFallThrough) { // intent contains the category
                                                if (ifBooleanTypeConditionValue()) {
                                                    newAssert = "(assert (exists ((index Int)) (= (select cats index) \"" + category + "\")))";
                                                } else {
                                                    newAssert = "(assert (forall ((index Int)) (not(= (select cats index) \"" + category + "\"))))";
                                                }

                                                //addIntentCategoryForPath(currPath,category);
                                            } else { // intent does not contain the category
                                                if (ifBooleanTypeConditionValue()) {
                                                    newAssert = "(assert (forall ((index Int)) (not(= (select cats index) \"" + category + "\"))))";
                                                } else {
                                                    newAssert = "(assert (exists ((index Int)) (= (select cats index) \"" + category + "\")))";
                                                }
                                            }
                                            leftVal = new Quartet<Value, String, String, Unit>(defStmt.getLeftOp(), null, newAssert, defStmt);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>>(leftVal, rightVal);
    }

    Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> findIntentBooleanValues(SootMethod sootMethod, SimpleLocalDefs defs, IfStmt ifStmt, Value conditionLeft, List<Unit> path, JVirtualInvokeExpr jVInvokeExpr, DefinitionStmt definitionStmtConditionLeft) {


        if (jVInvokeExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {

            if (Pattern.matches("hasExtra", jVInvokeExpr.getMethod().getName())) {//ok

                return intentHasExtra(sootMethod, defs, ifStmt, conditionLeft, path, jVInvokeExpr, definitionStmtConditionLeft);

            } else if (Pattern.matches("hasCategory", jVInvokeExpr.getMethod().getName())) {//ok
                return findCategories(sootMethod, defs, ifStmt, conditionLeft, path);
            } else {
                UnHandleWriter.write(appPath + "%%" + "没有处理的返回值为boolean的intent方法:" + jVInvokeExpr.getMethod() + "\n");
                seUnHandleProcessSatistic.addUnHandleCount();
            }
        }


//     else if (Pattern.matches("hasFileDescriptors", jVInvokeExpr.getMethod().getName())) {
//
//            //------------------------------------------------------
//
//
//        } else if (Pattern.matches("getBooleanExtra", jVInvokeExpr.getMethod().getName())) {//这个有两个对应的方法
//
//
//
//
//
//        } else {
//
//            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(defDefintionStmtConditionLeft);//---------------------------------
//
//        }

        return null;

    }

    private Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> intentHasExtra(SootMethod sootMethod, SimpleLocalDefs defs, IfStmt ifStmt, Value conditionLeft, List<Unit> path, JVirtualInvokeExpr jVInvokeExpr, DefinitionStmt defDefintionStmtConditionLeft) {

        Quartet<Value, String, String, Unit> leftVal = null;
        Quartet<Value, String, String, Unit> rightVal = null;

        if (Pattern.matches("hasExtra", jVInvokeExpr.getMethod().getName())) {

            if (jVInvokeExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {


                Value origValue = defDefintionStmtConditionLeft.getLeftOp();
                Unit defOrigValueUnit = defDefintionStmtConditionLeft;

                String newDecl = null;
                String newAssert = null;

                Local intentLocal = (Local) jVInvokeExpr.getBase();
                int count = 0;

                Unit defIntentLocalUnit = null;
                for (Unit oneDefIntentLocalUnit : defs.getDefsOfAt(intentLocal, defDefintionStmtConditionLeft)) {
                    if (!isDefInPathAndLatest(path, oneDefIntentLocalUnit, intentLocal, defDefintionStmtConditionLeft, defs)) {
                        continue;
                    }

                    defIntentLocalUnit = oneDefIntentLocalUnit;
                    count = count + 1;


                }
                if (count > 1) {
                    throw new RuntimeException("intent 定义语句有多个！");
                }
                String intentLocalSymbol = createSymbol(intentLocal, sootMethod, defIntentLocalUnit);//intent 的定义语句
                symbolLocalMap.put(intentLocalSymbol, intentLocal);
                symbolDefUnitMap.put(intentLocalSymbol, defIntentLocalUnit);
                localSymbolMap.put(intentLocal, intentLocalSymbol);
                newDecl = "(declare-const " + intentLocalSymbol + " Object )";
                newAssert = "(assert (= " + intentLocalSymbol + " NotNull))";


                String keyString = null;
                if (jVInvokeExpr.getArg(0) instanceof StringConstant) {
                    keyString = ((StringConstant) (((StringConstant) jVInvokeExpr.getArg(0)))).value;


                } else if (jVInvokeExpr.getArg(0) instanceof Local) {

                    Local keyLocal = (Local) (jVInvokeExpr.getArg(0));//-----------------------------------

                    UnHandleWriter.write(appPath + "%%" + "intent has extra的值来源于变量！" + jVInvokeExpr + "\n");
                    seUnHandleProcessSatistic.addUnHandleCount();


                } else {
                    MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("unhandle case :" + jVInvokeExpr.toString());
                    UnHandleWriter.write(appPath + "%%" + "unhandle case :" + jVInvokeExpr.toString() + "\n");
                    seUnHandleProcessSatistic.addUnHandleCount();
                    throw new RuntimeException("unhandle case :" + jVInvokeExpr.toString());
                }

                if (keyString != null) {
                    Body b = sootMethod.getActiveBody();
                    UnitGraph ug = new BriefUnitGraph(b);
                    List<Unit> currPathList = new ArrayList<Unit>(path);
                    Unit succ = currPathList.get(currPathList.indexOf(ifStmt) - 1);

                    boolean isFallThrough = isFallThrough(ifStmt, succ, (JimpleBody) sootMethod.getActiveBody());
                    ConditionExpr conditionExpr = (ConditionExpr) ifStmt.getCondition();

                    if (isFallThrough) { // then intent contains the key

                        if (ifBooleanTypeConditionValue()) {
                            newAssert += "\n(assert (= (containsKey " + intentLocalSymbol + " \"" + keyString + "\") true))";
                        } else {
                            newAssert += "\n(assert (= (containsKey " + intentLocalSymbol + " \"" + keyString + "\") false))";
                        }


                        //addIntentExtraForPath(currPath,keyString,keyVal.getType().toString());
                    } else { // the intent does NOT contain the key
                        if (ifBooleanTypeConditionValue()) {
                            newAssert += "\n(assert (= (containsKey " + intentLocalSymbol + " \"" + keyString + "\") false))";
                        } else {
                            newAssert += "\n(assert (= (containsKey " + intentLocalSymbol + " \"" + keyString + "\") true))";
                        }

                    }
//                    if (isFallThrough) { // intent contains the extra
//                        newAssert += "\n"+"(assert (exists ((index Int)) (= (select keys index) \"" + keyString + "\")))";
//
//                        //addIntentExtraForPath(currPath, rightVal.getValue0().toString(), rightVal.getValue0().getType().toString());
//                    } else { // intent does not contain the extra
//                        newAssert += "\n"+"(assert (forall ((index Int)) (not(= (select keys index) \"" + keyString + "\"))))";
//                    }
                }

                leftVal = new Quartet<Value, String, String, Unit>(origValue, newDecl, newAssert, defOrigValueUnit);


            }
        }
        return new Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>>(leftVal, rightVal);
    }


    private Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> findBundleValues(SootMethod method, SimpleLocalDefs defs, IfStmt inUnit, Value value, List<Unit> currPath) {
        Quartet<Value, String, String, Unit> leftVal = null;
        Quartet<Value, String, String, Unit> rightVal = null;
        if (value instanceof Local) {
            Local local = (Local) value;
            if (local.getType() instanceof BooleanType) {
                for (Unit defUnit : defs.getDefsOfAt(local, inUnit)) {
					/*if (!currPath.contains(defUnit)) {
						continue;
					}*/
                    if (!isDefInPathAndLatest(currPath, defUnit, local, inUnit, defs)) {//ok
                        continue;
                    }

                    Stmt defStmt = (Stmt) defUnit;
                    if (defStmt.containsInvokeExpr()) {
                        if (defStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr ie = (InstanceInvokeExpr) defStmt.getInvokeExpr();
                            if (ie.getMethod().getDeclaringClass().getName().equals("android.os.Bundle") || ie.getMethod().getDeclaringClass().getName().equals("android.os.BaseBundle")) {
                                if (ie.getMethod().getName().equals("containsKey")) {
                                    Value keyVal = ie.getArg(0);
                                    if (keyVal instanceof StringConstant) {//直接没有考虑key是否是个变量的情况--------------------------------------
                                        StringConstant keyStringConst = (StringConstant) keyVal;
                                        String keyString = keyStringConst.value;


                                        if (ie.getBase() instanceof Local) {
                                            Local bundleLocal = (Local) ie.getBase();
                                            for (Unit bundleDef : defs.getDefsOfAt(bundleLocal, defUnit)) {
												/*if (!currPath.contains(bundleDef)) {
													continue;
												}*/
                                                if (!isDefInPathAndLatest(currPath, bundleDef, bundleLocal, defUnit, defs)) {
                                                    continue;
                                                }
                                                Stmt bundleStmt = (Stmt) bundleDef;
                                                if (bundleStmt.containsInvokeExpr()) {
                                                    if (bundleStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
                                                        InstanceInvokeExpr bundleInvoke = (InstanceInvokeExpr) bundleStmt.getInvokeExpr();
                                                        if (bundleInvoke.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {
                                                            if (bundleInvoke.getMethod().getName().equals("getExtras")) {//
                                                                if (bundleInvoke.getBase() instanceof Local) {
                                                                    Local intentLocal = (Local) bundleInvoke.getBase();
                                                                    for (Unit intentDef : defs.getDefsOfAt(intentLocal, bundleStmt)) {
																		/*if (!currPath.contains(intentDef)) {
																			continue;
																		}*/
                                                                        if (!isDefInPathAndLatest(currPath, intentDef, intentLocal, bundleStmt, defs)) {
                                                                            continue;
                                                                        }
                                                                        String intentLocalSymbol = createSymbol(intentLocal, method, intentDef);//intent 的定义语句

//                                                                        symbolLocalMap.put(intentLocalSymbol, intentLocal);//已经做了
//                                                                        localSymbolMap.put(intentLocal,intentLocalSymbol);
//                                                                        symbolDefUnitMap.put(intentLocalSymbol, intentDef);

                                                                        String newDecl = "(declare-const " + intentLocalSymbol + " Object )";
                                                                        String newAssert = "(assert (= " + intentLocalSymbol + " NotNull))";


                                                                        List<Unit> currPathList = new ArrayList<Unit>(currPath);
                                                                        Unit succ = currPathList.get(currPathList.indexOf(inUnit) - 1);

                                                                        //boolean isFallThrough = isFallThrough(inUnit, succ);
                                                                        boolean isFallThrough = isFallThrough(inUnit, succ, (JimpleBody) method.getActiveBody());
                                                                        ConditionExpr conditionExpr = (ConditionExpr) inUnit.getCondition();
                                                                        if (isFallThrough) { // then intent contains the key   //
                                                                            if (ifBooleanTypeConditionValue()) {
                                                                                newAssert += "\n(assert (= (containsKey " + intentLocalSymbol + " \"" + keyString + "\") true))";//getExtras是将intent的extra data converted into bundle ,so assert is about intent
                                                                            } else {
                                                                                newAssert += "\n(assert (= (containsKey " + intentLocalSymbol + " \"" + keyString + "\") false))";
                                                                            }


                                                                        } else { // the intent does NOT contain the key
                                                                            if (ifBooleanTypeConditionValue()) {
                                                                                newAssert += "\n(assert (= (containsKey " + intentLocalSymbol + " \"" + keyString + "\") false))";

                                                                            } else {
                                                                                newAssert += "\n(assert (= (containsKey " + intentLocalSymbol + " \"" + keyString + "\") true))";//getExtras是将intent的extra data converted into bundle ,so assert is about intent
                                                                            }
                                                                        }

                                                                        leftVal = new Quartet<Value, String, String, Unit>(local, newDecl, newAssert, defUnit);
                                                                    }
                                                                }
                                                            } else if (bundleInvoke.getMethod().getName().equals("getBundleExtra"))//43个------------------------这里表明bundle是从intent的getBundleExtra取出来，这里intent的getBundleExtra约束已经再处理
                                                            {

                                                                String bundleLocalSymbol = createSymbol(bundleLocal, method, bundleDef);//

//                                                                symbolLocalMap.put(bundleLocalSymbol, bundleLocal);
//                                                                symbolDefUnitMap.put(bundleLocalSymbol, bundleDef);已经做了

                                                                String newDecl = "(declare-const " + bundleLocalSymbol + " Object )";
                                                                String newAssert = "(assert (= " + bundleLocalSymbol + " NotNull))";

                                                                List<Unit> currPathList = new ArrayList<Unit>(currPath);
                                                                Unit succ = currPathList.get(currPathList.indexOf(inUnit) - 1);

                                                                //boolean isFallThrough = isFallThrough(inUnit, succ);
                                                                boolean isFallThrough = isFallThrough(inUnit, succ, (JimpleBody) method.getActiveBody());
                                                                ConditionExpr conditionExpr = (ConditionExpr) inUnit.getCondition();
                                                                if (isFallThrough) { // then intent contains the key
                                                                    if (ifBooleanTypeConditionValue()) {
                                                                        newAssert += "\n(assert (= (containsKey " + bundleLocalSymbol + " \"" + keyString + "\") true))";
                                                                    } else {
                                                                        newAssert += "\n(assert (= (containsKey " + bundleLocalSymbol + " \"" + keyString + "\") false))";
                                                                    }

                                                                    //addIntentExtraForPath(currPath,keyString,keyVal.getType().toString());
                                                                } else { // the intent does NOT contain the key

                                                                    if (ifBooleanTypeConditionValue()) {
                                                                        newAssert += "\n(assert (= (containsKey " + bundleLocalSymbol + " \"" + keyString + "\") false))";
                                                                    } else {

                                                                        newAssert += "\n(assert (= (containsKey " + bundleLocalSymbol + " \"" + keyString + "\") true))";

                                                                    }


                                                                }


                                                                leftVal = new Quartet<Value, String, String, Unit>(local, newDecl, newAssert, defUnit);


                                                            } else {
                                                                UnHandleWriter.write(appPath + "%%" + "unhandle type！" + bundleInvoke.getMethod() + "\n");
                                                                seUnHandleProcessSatistic.addUnHandleCount();
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (keyVal instanceof Local)//------------------------------------------------写一个专门抽取函数
                                    {
                                        UnHandleWriter.write(appPath + "%%" + "bundle contains key 是一个变量！\n");
                                        seUnHandleProcessSatistic.addUnHandleCount();

                                    }
                                } else if (ie.getMethod().getName().equals("getBoolean")) {
                                    String booleanValueSymbol = createSymbol(value, method, defStmt);
                                    List<Unit> currPathList = new ArrayList<Unit>(currPath);
                                    Unit succ = currPathList.get(currPathList.indexOf(inUnit) - 1);
                                    String newDecl = "(declare-const " + booleanValueSymbol + " Bool )";
                                    String newAssert = null;
                                    //boolean isFallThrough = isFallThrough(inUnit, succ);
                                    boolean isFallThrough = isFallThrough(inUnit, succ, (JimpleBody) method.getActiveBody());
                                    ConditionExpr conditionExpr = (ConditionExpr) inUnit.getCondition();
                                    if (isFallThrough) { // then intent contains the key

                                        if (ifBooleanTypeConditionValue()) {
                                            newAssert = "(assert (=" + booleanValueSymbol + " true))";
                                        } else {
                                            newAssert = "(assert (=" + booleanValueSymbol + " false))";
                                        }


                                        //addIntentExtraForPath(currPath,keyString,keyVal.getType().toString());
                                    } else { // the intent does NOT contain the key
                                        if (ifBooleanTypeConditionValue()) {
                                            newAssert = "(assert (=" + booleanValueSymbol + " false))";

                                        } else {
                                            newAssert = "(assert (=" + booleanValueSymbol + " true))";
                                        }
                                    }

                                    leftVal = new Quartet<Value, String, String, Unit>(local, newDecl, newAssert, defUnit);

                                } else {


                                    UnHandleWriter.write(appPath + "%%" + "unhandle type！" + ie.getMethod().getName() + "\n");
                                    seUnHandleProcessSatistic.addUnHandleCount();

                                }
                            }
                        }
                    }
                }
            }
        }
        return new Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>>(leftVal, rightVal);
    }

    private Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>> findEqualsBoolean(IfStmt ifStmt, List<Unit> path, SootMethod sootMethod, SimpleLocalDefs defs, DefinitionStmt defDefintionStmtConditionLeft, JVirtualInvokeExpr jVInvokeExpr) {
        Quartet<Value, String, String, Unit> leftVal = null;
        Quartet<Value, String, String, Unit> rightVal = null;

        if (jVInvokeExpr.getMethod().getName().equals("equals") && jVInvokeExpr.getMethod().getDeclaringClass().getName().equals("java.lang.String")) {

            leftVal = findEqualLeftAndRightVal(sootMethod, defs, defDefintionStmtConditionLeft, jVInvokeExpr.getBase(), path);
            rightVal = findEqualLeftAndRightVal(sootMethod, defs, defDefintionStmtConditionLeft, jVInvokeExpr.getArg(0), path);


        }

        return new Pair<Quartet<Value, String, String, Unit>, Quartet<Value, String, String, Unit>>(leftVal, rightVal);
    }


    private boolean isFallThrough(Unit inUnit, Unit succ, JimpleBody body) {
        if (succ == null && inUnit instanceof IfStmt) {
            return true;
        } else {
            //assert getSuccsOf(u).contains(succ);
            if (!inUnit.fallsThrough()) {
                return false;
            }


            MyUnitGraph curMyUnitGraph = MyUnitGraphFactory.getCurMyUnitGraph();

            Body modifiedBody = curMyUnitGraph.getBodyMapping().jimpleBodyA;

            Unit succMapping = (Unit) curMyUnitGraph.getBodyMapping().bindings.get(succ);

            Unit inUnitMapping = (Unit) curMyUnitGraph.getBodyMapping().bindings.get(inUnit);

//            if (modifiedBody.getUnits().getSuccOf(inUnitMap) != succMap) {
//                WriteFile writeFile = new WriteFile("AnalysisAPKIntent/testFunctions/" + "isFallThrough.txt", true);
//                writeFile.writeStr(body.getMethod().getBytecodeSignature() + " " + inUnit + " " + succ + " " + appPath + "\n");
//                writeFile.close();
//            }


            return modifiedBody.getUnits().getSuccOf(inUnitMapping) == succMapping;
        }


    }

    public Quartet<Value, String, String, Unit> findOriginalVal(SootMethod method, SimpleLocalDefs defs, Unit potentialCmpUnit, Value cmpOp, List<Unit> currPath) {
        Value origVal = null;
        String newDecl = null;
        String newAssert = null;
        Unit defUnit = null;
        if (cmpOp instanceof Local) {
            Value cmpVal = cmpOp;
            Quartet<Value, String, String, Unit> r = findOriginalValFromCmpVal(method, defs, potentialCmpUnit, cmpVal, currPath);
            origVal = r.getValue0();
            newDecl = r.getValue1();
            newAssert = r.getValue2();
            defUnit = r.getValue3();
        } else if (cmpOp instanceof Constant) {
            origVal = cmpOp;

        } else {
            UnHandleWriter.write(appPath + "%%" + "Unhandled cmpOp for: " + potentialCmpUnit + "\n");
            seUnHandleProcessSatistic.addUnHandleCount();

            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("Unhandled cmpOp for: " + potentialCmpUnit);
            throw new RuntimeException("Unhandled cmpOp for: " + potentialCmpUnit);

        }
        return new Quartet<Value, String, String, Unit>(origVal, newDecl, newAssert, defUnit);
    }

    public Quartet<Value, String, String, Unit> findOriginalValFromCmpVal(SootMethod method, SimpleLocalDefs defs, Unit potentialCmpUnit, Value cmpVal, List<Unit> currPath) {
        Value origVal = null;
        String key = null;
        Local cmpOp1 = (Local) cmpVal;
        List<Unit> castOrInvokeUnits = defs.getDefsOfAt(cmpOp1, potentialCmpUnit);
        String newDecl = null;
        String newAssert = null;
        Unit defUnit = null;
        for (Unit coiUnit : castOrInvokeUnits) {

            if (!isDefInPathAndLatest(currPath, coiUnit, cmpOp1, potentialCmpUnit, defs)) {
                continue;
            }

            if (coiUnit instanceof DefinitionStmt) {
                DefinitionStmt coiStmt = (DefinitionStmt) coiUnit;
                origVal = coiStmt.getLeftOp();
                defUnit = coiUnit;
                if (!currPath.contains(defUnit)) {
                    continue;
                }
                if (coiStmt.getRightOp() instanceof JCastExpr) {

                    JCastExpr expr = (JCastExpr) coiStmt.getRightOp();
                    if (expr.getOp() instanceof Local) {
                        Local localFromCast = (Local) expr.getOp();
                        List<Unit> defsOfLocalFromCast = defs.getDefsOfAt(localFromCast, coiUnit);
                        for (Unit defLocalAssignFromCastUnit : defsOfLocalFromCast) {
							/*if (!currPath.contains(defLocalAssignFromCastUnit)) {
								continue;
							}*/
                            if (!isDefInPathAndLatest(currPath, defLocalAssignFromCastUnit, localFromCast, coiUnit, defs)) {
                                continue;
                            }
                            if (defLocalAssignFromCastUnit instanceof DefinitionStmt) {
                                DefinitionStmt defLocalAssignFromCastStmt = (DefinitionStmt) defLocalAssignFromCastUnit;
                                origVal = defLocalAssignFromCastStmt.getLeftOp();
                                defUnit = defLocalAssignFromCastUnit;

                            }
                        }
                    }
                } else if (coiStmt.getRightOp() instanceof StringConstant) {
                    Local local = (Local) coiStmt.getLeftOp();
                    String symbol = createSymbol(local, method, coiStmt);
                    symbolLocalMap.put(symbol, local);
                    symbolDefUnitMap.put(symbol, coiStmt);
                    StringConstant stringConst = (StringConstant) coiStmt.getRightOp();
                    newDecl = "(declare-const " + symbol + " String )";
                    newAssert = "(assert (= " + symbol + " " + stringConst + " ))";
                } else if (coiStmt.getRightOp() instanceof ParameterRef) {

                    if (coiStmt.getLeftOp() instanceof Local) {
                        Local prLocal = (Local) coiStmt.getLeftOp();
                        String localSymbol = createSymbol(prLocal, method, coiStmt);

                        origVal = coiStmt.getLeftOp();
                        ParameterRef pr = (ParameterRef) coiStmt.getRightOp();
                        String prSymbol = createParamRefSymbol(prLocal, pr.getIndex(), method, coiStmt);

                        newDecl = "(declare-const " + prSymbol + " ParamRef)";
                        newAssert = "(assert ( = (index " + prSymbol + ") " + pr.getIndex() + "))\n";
                        newAssert += "(assert ( = (type " + prSymbol + ") \"" + pr.getType() + "\"))\n";
                        newAssert += "(assert ( = (method " + prSymbol + ") \"" + method.getDeclaringClass().getName() + "." + method.getName() + "\"))\n";
                        newAssert += "(assert (= (hasParamRef " + localSymbol + ") " + prSymbol + "))";
                        defUnit = coiStmt;
                    }


                    UnHandleWriter.write(appPath + "%%" + "Unhandled data source" + coiStmt.getRightOp() + "\n");
                    seUnHandleProcessSatistic.addUnHandleCount();//并没有对来源于参数的数据处理

                } else {
                    UnHandleWriter.write(appPath + "%%" + "Unhandled type" + coiStmt.getRightOp() + "\n");
                    seUnHandleProcessSatistic.addUnHandleCount();
                }


            }
        }

        return new Quartet<Value, String, String, Unit>(origVal, newDecl, newAssert, defUnit);
    }

    private Quartet<Value, String, String, Unit> findEqualLeftAndRightVal(SootMethod sootMethod, SimpleLocalDefs defs, Unit equalsUnit, Value value, List<Unit> path) {


        Value origVal = null;//左边变量
        String newDecl = null;
        String newAssert = null;
        Unit defOrigValUnit = null;


        if (value instanceof Local) {// value 是   x1 or x2   x1.euqals(x2)
            Local local = (Local) value;//如果这个equals其中一个是变量的话，去找这个变量的来源

            Quartet<Value, String, String, Unit> quartet = findLocalSource(local, equalsUnit, sootMethod, defs, path);

            origVal = quartet.getValue0();
            newDecl = quartet.getValue1();
            newAssert = quartet.getValue2();
            defOrigValUnit = quartet.getValue3();


        } else if (value instanceof StringConstant) {
            origVal = value;

        } else {

            UnHandleWriter.write(appPath + "%%" + "不能处理equals的两个参数的类型情况" + equalsUnit + "\n");
            seUnHandleProcessSatistic.addUnHandleCount();

            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("不能处理equals的两个参数的类型情况" + equalsUnit);
            throw new RuntimeException("不能处理equals的两个参数的类型情况" + equalsUnit);

        }

        return new Quartet<Value, String, String, Unit>(origVal, newDecl, newAssert, defOrigValUnit);

    }

    private Quartet<Value, String, String, Unit> findLocalSource(Local localVar, Unit curUnit, SootMethod sootMethod, SimpleLocalDefs defs, List<Unit> path) {


        String newDecl = null;
        String newAssert = null;

        Value origVal = null;//x1.euqals(x2)     //这里的x1就是origVal   保存origVal,defUnitOfOrigVal用来生成约束
        Unit defUnitOfOrigVal = null;


        List<Unit> defineLocalUnits = defs.getDefsOfAt(localVar, curUnit);//curUnit是equals语句  localVar就是x1    x1.euals(x2)

        for (Unit defUnit : defineLocalUnits) {
            if (!isDefInPathAndLatest(path, defUnit, localVar, curUnit, defs)) {
                continue;
            }

            if (defUnit instanceof DefinitionStmt) {
                DefinitionStmt defDefintionStmt = (DefinitionStmt) defUnit;//x1的定义语句 例如：x1=intent.getStringExtra("xxx")

                origVal = defDefintionStmt.getLeftOp();
                defUnitOfOrigVal = defDefintionStmt;

                if (defDefintionStmt.getRightOp() instanceof InstanceInvokeExpr) {


                    //如果这个x1来源于intent方法, 这个约束已经在handleIntentExtra那里处理了。不用生成约束


                } else if (defDefintionStmt.getRightOp() instanceof JCastExpr) {//x1=(String)3.5

                    JCastExpr jCastExpr = (JCastExpr) defDefintionStmt.getRightOp();
                    if (jCastExpr.getOp() instanceof Local) {
                        Local localFromCast = (Local) jCastExpr.getOp();
                        List<Unit> defsOfLocalFromCast = defs.getDefsOfAt(localFromCast, defUnit);
                        for (Unit defLocalAssignFromCastUnit : defsOfLocalFromCast) {
							/*if (!currPath.contains(defLocalAssignFromCastUnit)) {
								continue;
							}*/
                            if (!isDefInPathAndLatest(path, defLocalAssignFromCastUnit, localFromCast, defUnit, defs)) {
                                continue;
                            }
                            if (defLocalAssignFromCastUnit instanceof DefinitionStmt) {
                                DefinitionStmt defLocalAssignFromCastStmt = (DefinitionStmt) defLocalAssignFromCastUnit;

                                origVal = defLocalAssignFromCastStmt.getLeftOp();
                                defUnitOfOrigVal = defLocalAssignFromCastUnit;


                            }
                        }
                    }


                } else if (defDefintionStmt.getRightOp() instanceof StringConstant) {//定义是一个常量定义 例如：x1="ttt"
                    Local local = (Local) defDefintionStmt.getLeftOp();
                    String symbol = createSymbol(local, sootMethod, defDefintionStmt);
                    symbolLocalMap.put(symbol, local);
                    symbolDefUnitMap.put(symbol, defDefintionStmt);
                    StringConstant stringConst = (StringConstant) defDefintionStmt.getRightOp();
                    newDecl = "(declare-const " + symbol + " String )";
                    newAssert = "(assert (= " + symbol + " " + stringConst + " ))";
                } else if (defDefintionStmt.getRightOp() instanceof ParameterRef) {//来源于参数   x1=parameter0

                    if (defDefintionStmt.getLeftOp() instanceof Local) {
                        Local prLocal = (Local) defDefintionStmt.getLeftOp();
                        String localSymbol = createSymbol(prLocal, sootMethod, defDefintionStmt);


                        ParameterRef pr = (ParameterRef) defDefintionStmt.getRightOp();
                        String prSymbol = createParamRefSymbol(prLocal, pr.getIndex(), sootMethod, defDefintionStmt);

                        newDecl = "(declare-const " + prSymbol + " ParamRef)";
                        newAssert = "(assert ( = (index " + prSymbol + ") " + pr.getIndex() + "))\n";
                        newAssert += "(assert ( = (type " + prSymbol + ") \"" + pr.getType() + "\"))\n";
                        newAssert += "(assert ( = (method " + prSymbol + ") \"" + sootMethod.getDeclaringClass().getName() + "." + sootMethod.getName() + "\"))\n";
                        newAssert += "(assert (= (hasParamRef " + localSymbol + ") " + prSymbol + "))";


                        UnHandleWriter.write(appPath + "%%" + "equals: " + "x1 from parameter" + "\n");
                        seUnHandleProcessSatistic.addUnHandleCount();

                    }
                } else {// from  field or other sources  ???????可以参考原先letterbomb写的

                    UnHandleWriter.write(appPath + "%%" + "equals: " + "x1 from " + defDefintionStmt.getRightOp() + "\n");
                    seUnHandleProcessSatistic.addUnHandleCount();
                }

            }


        }


        return new Quartet<Value, String, String, Unit>(origVal, newDecl, newAssert, defUnitOfOrigVal);
    }

    public String extractKeyFromIntentExtra(DefinitionStmt defStmt, SimpleLocalDefs defs, List<Unit> currPath) {

        String key = null;
        if (defStmt.getRightOp() instanceof JVirtualInvokeExpr) {
            JVirtualInvokeExpr expr = (JVirtualInvokeExpr) defStmt.getRightOp();
            boolean keyExtractionEnabled = false;
            if (Pattern.matches("get.*Extra", expr.getMethod().getName())) {
                if (expr.getMethod().getDeclaringClass().toString().equals("android.content.Intent")) {
                    keyExtractionEnabled = true;
                }
            }
            if (Pattern.matches("has.*Extra", expr.getMethod().getName())) {
                if (expr.getMethod().getDeclaringClass().toString().equals("android.content.Intent")) {
                    keyExtractionEnabled = true;
                }
            }
            if (Globals.bundleExtraDataMethodsSet.contains(expr.getMethod().getName())) {
                if (expr.getMethod().getDeclaringClass().getName().equals("android.os.Bundle")) {
                    keyExtractionEnabled = true;
                }
                if (expr.getMethod().getDeclaringClass().getName().equals("android.os.BaseBundle")) {
                    keyExtractionEnabled = true;
                }
            }

            if (keyExtractionEnabled) {

                if (!(expr.getArg(0) instanceof StringConstant)) {
                    if (expr.getArg(0) instanceof Local) {
                        Local keyLocal = (Local) expr.getArg(0);
                        List<Unit> defUnits = defs.getDefsOfAt(keyLocal, defStmt);
                        for (Unit defUnit : defUnits) {
							/*if (!currPath.contains(defUnit)) {
								continue;
							}*/
                            if (!isDefInPathAndLatest(currPath, defUnit, keyLocal, defStmt, defs)) {
                                continue;
                            }
                            if (defUnit instanceof DefinitionStmt) {
                                DefinitionStmt keyLocalDefStmt = (DefinitionStmt) defUnit;
                                if (keyLocalDefStmt.getRightOp() instanceof VirtualInvokeExpr) {
                                    VirtualInvokeExpr invokeExpr = (VirtualInvokeExpr) keyLocalDefStmt.getRightOp();
                                    if (invokeExpr.getBase() instanceof Local) {
                                        if (invokeExpr.getMethod().getDeclaringClass().getType().toString().equals("java.lang.Enum")) {
                                            Local base = (Local) invokeExpr.getBase();
                                            List<Unit> baseDefs = defs.getDefsOfAt(base, keyLocalDefStmt);
                                            for (Unit baseDef : baseDefs) {
												/*if (!currPath.contains(baseDef)) {
													continue;
												}*/
                                                if (!isDefInPathAndLatest(currPath, baseDef, base, keyLocalDefStmt, defs)) {
                                                    continue;
                                                }
                                                if (baseDef instanceof DefinitionStmt) {
                                                    DefinitionStmt baseDefStmt = (DefinitionStmt) baseDef;
                                                    if (baseDefStmt.getRightOp() instanceof FieldRef) {
                                                        FieldRef fieldRef = (FieldRef) baseDefStmt.getRightOp();
                                                        if (fieldRef.getField().getDeclaringClass().toString().equals(invokeExpr.getBase().getType().toString())) {
                                                            key = fieldRef.getField().getName();
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                    continue;
                                } else if (keyLocalDefStmt.getRightOp() instanceof StaticFieldRef) {
                                    SootField keyField = ((StaticFieldRef) keyLocalDefStmt.getRightOp()).getField();
                                    SootMethod clinitMethod = keyField.getDeclaringClass().getMethodByName("<clinit>");
                                    if (clinitMethod.hasActiveBody()) {
                                        Body clinitBody = clinitMethod.getActiveBody();
                                        for (Unit clinitUnit : clinitBody.getUnits()) {
                                            if (clinitUnit instanceof DefinitionStmt) {
                                                DefinitionStmt clinitDefStmt = (DefinitionStmt) clinitUnit;
                                                if (clinitDefStmt.getLeftOp() instanceof StaticFieldRef) {
                                                    SootField clinitField = ((StaticFieldRef) clinitDefStmt.getLeftOp()).getField();
                                                    if (clinitField.equals(keyField)) {
                                                        if (clinitDefStmt.getRightOp() instanceof StringConstant) {
                                                            StringConstant clinitStringConst = (StringConstant) clinitDefStmt.getRightOp();
                                                            key = clinitStringConst.value;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("Unhandled case for: " + keyLocalDefStmt.getRightOp());
                                    UnHandleWriter.write("Unhandled case for: " + keyLocalDefStmt.getRightOp() + "\n");
                                    seUnHandleProcessSatistic.addUnHandleCount();

                                    //throw new RuntimeException("Unhandled case for: " + keyLocalDefStmt.getRightOp());
                                }

                            }
                        }
                    }
                } else {
                    key = expr.getArg(0).toString();
                }
            }
        }
        return key;
    }

//    private String getExtra(SimpleLocalDefs defs, List<Unit> path, DefinitionStmt defStmt) {//intent and bundle 的extra
//        String key = null;
//
//
//        if (defStmt.getRightOp() instanceof JVirtualInvokeExpr) {
//
//            JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) defStmt.getRightOp();
//            boolean keyExtractionEnabled = false;
//
//            if (Pattern.matches("get.*Extra", jVirtualInvokeExpr.getMethod().getName())) {//不需要判断返回值类型的，因为前面的equals已经限制了满足这个方法是getStringExtra等
//
//                if (jVirtualInvokeExpr.getMethod().getDeclaringClass().toString().equals("android.content.Intent")) {
//                    keyExtractionEnabled = true;
//
//                }
//            }
//            if (Pattern.matches("has.*Extra", jVirtualInvokeExpr.getMethod().getName())) {
//                if (jVirtualInvokeExpr.getMethod().getDeclaringClass().toString().equals("android.content.Intent")) {
//                    keyExtractionEnabled = true;
//
//                }
//            }
//            if (Globals.bundleExtraDataMethodsSet.contains(jVirtualInvokeExpr.getMethod().getName())) {
//                if (jVirtualInvokeExpr.getMethod().getDeclaringClass().getName().equals("android.os.Bundle")) {
//                    keyExtractionEnabled = true;
//
//                }
//                if (jVirtualInvokeExpr.getMethod().getDeclaringClass().getName().equals("android.os.BaseBundle")) {
//                    keyExtractionEnabled = true;
//
//                }
//            }
//
//            if (keyExtractionEnabled) {
//                if (!(jVirtualInvokeExpr.getArg(0) instanceof StringConstant)) { //参数不是一个字符串常量
//                    if (jVirtualInvokeExpr.getArg(0) instanceof Local) {//参数是一个变量
//                        Local keyLocal = (Local) jVirtualInvokeExpr.getArg(0);
//                        List<Unit> defUnits = defs.getDefsOfAt(keyLocal, defStmt);
//                        for (Unit defUnitOfKey : defUnits) {
//
//                            if (!isDefInPathAndLatest(path, defUnitOfKey, keyLocal, defStmt, defs)) {
//                                continue;
//                            }
//                            if (defUnitOfKey instanceof DefinitionStmt) {
//                                DefinitionStmt keyLocalDefStmt = (DefinitionStmt) defUnitOfKey;
//                                if (keyLocalDefStmt.getRightOp() instanceof StringConstant) {
//                                    key = ((StringConstant) keyLocalDefStmt.getRightOp()).value;
//                                } else if (keyLocalDefStmt.getRightOp() instanceof VirtualInvokeExpr) {//key的值来源于Enum
//
//
//                                    VirtualInvokeExpr invokeExpr = (VirtualInvokeExpr) keyLocalDefStmt.getRightOp();
//                                    if (invokeExpr.getBase() instanceof Local) {
//                                        if (invokeExpr.getMethod().getDeclaringClass().getType().toString().equals("java.lang.Enum")) {
//                                            Local base = (Local) invokeExpr.getBase();
//                                            List<Unit> baseDefs = defs.getDefsOfAt(base, keyLocalDefStmt);
//                                            for (Unit baseDef : baseDefs) {
//
//                                                if (!isDefInPathAndLatest(path, baseDef, base, keyLocalDefStmt, defs)) {
//                                                    continue;
//                                                }
//                                                if (baseDef instanceof DefinitionStmt) {
//                                                    DefinitionStmt baseDefStmt = (DefinitionStmt) baseDef;
//                                                    if (baseDefStmt.getRightOp() instanceof FieldRef) {
//                                                        FieldRef fieldRef = (FieldRef) baseDefStmt.getRightOp();
//                                                        if (fieldRef.getField().getDeclaringClass().toString().equals(invokeExpr.getBase().getType().toString())) {
//                                                            key = fieldRef.getField().getName();
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                    }
//                                    //continue;
//                                    //throw new RuntimeException("key的值来源于Enum");
//
//                                } else if (keyLocalDefStmt.getRightOp() instanceof StaticFieldRef) {//key的值来源于静态属性
//                                    //-----------------------------------------------------------------------
//                                    SootField keyField = ((StaticFieldRef) keyLocalDefStmt.getRightOp()).getField();
//                                    SootMethod clinitMethod = keyField.getDeclaringClass().getMethodByName("<clinit>");//静态初始化方法
//                                    if (clinitMethod.hasActiveBody()) {
//                                        Body clinitBody = clinitMethod.getActiveBody();
//                                        for (Unit clinitUnit : clinitBody.getUnits()) {
//                                            if (clinitUnit instanceof DefinitionStmt) {
//                                                DefinitionStmt clinitDefStmt = (DefinitionStmt) clinitUnit;
//                                                if (clinitDefStmt.getLeftOp() instanceof StaticFieldRef) {
//                                                    SootField clinitField = ((StaticFieldRef) clinitDefStmt.getLeftOp()).getField();
//                                                    if (clinitField.equals(keyField)) {
//                                                        if (clinitDefStmt.getRightOp() instanceof StringConstant) {
//                                                            StringConstant clinitStringConst = (StringConstant) clinitDefStmt.getRightOp();
//                                                            key = clinitStringConst.value;
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                    }
//                                    //throw new RuntimeException("key的值来源于静态属性");
//                                } else {
//
//                                    MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).error("Unhandled case for key来源于其他类型语句" + keyLocalDefStmt.getRightOp());
//                                    throw new RuntimeException("Unhandled case for key来源于其他类型语句" + keyLocalDefStmt.getRightOp());
//                                }
//
//                            }
//                        }
//                    }
//                } else {//参数是一个字符串常量
//                    key = jVirtualInvokeExpr.getArg(0).toString();
//                }
//            }
//
//        }
//
//
//        return key;
//    }

    class UnitEdge {
        Unit src;
        Unit tgt;

        public UnitEdge(Unit src, Unit tgt) {
            this.src = src;
            this.tgt = tgt;
        }

        public Unit getSrc() {
            return src;
        }

        public Unit getTgt() {
            return tgt;
        }


        @Override
        public int hashCode() {
            return src.hashCode() * 2 + tgt.hashCode() + 1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnitEdge unitEdge = (UnitEdge) o;
            return this.src.equals(unitEdge.src) && this.tgt.equals(unitEdge.tgt);
        }
    }

    private void getAllPathInMethod(Unit unit, UnitEdge unitEdge, UnitGraph ug, List<List<Unit>> finalPaths, List<Unit> curPath, Set<UnitEdge> visited, int branchCount) {


        if (branchCount >= enterBranchLimit) {
            hasReachBranchLimit = true;
            return;
        }

        List<Unit> curPathCopy = new ArrayList<>(curPath);
        curPathCopy.add(unit);


        Set<UnitEdge> visitedCopy = new HashSet<>(visited);

        visitedCopy.add(unitEdge);


        if (ug.getPredsOf(unit).size() == 0) {

            finalPaths.add(curPathCopy);

        } else {

            if (ug.getPredsOf(unit).size() >= 2) {
                branchCount++;
            }
            for (Unit parentUnit : ug.getPredsOf(unit)) {
                UnitEdge unitEdgeNew = new UnitEdge(unit, parentUnit);

                if (!visitedCopy.contains(unitEdgeNew)) {//这条边是不是已经走过了


                    getAllPathInMethod(parentUnit, unitEdgeNew, ug, finalPaths, curPathCopy, visitedCopy, branchCount);

                }

            }
        }

    }


    public void run() {
        Config.setSootOptions(appPath);
        PackManager.v().getPack("wjtp")
                .add(new Transform("wjtp.intentGen", this));

        PackManager.v().getPack("wjtp").apply();
    }


    public static void main(String[] args) {


        writeFileCallGraphSize = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "callGraphSize.txt", false, exceptionLogger);
        appUnitGraphPathReducedReachLimit = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "appUnitGraphReachLimit.txt", false, exceptionLogger);
        ifReducedWriter = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "if_reduced.csv", false, exceptionLogger);
        ifReducedWriter.writeStr("app,method,previousCount,afterReducedCount,previousCount-afterReducedCount"+"\n");


        String appDir = null;

        if (exeModelTest) {
            appDir = Config.testAppPath;
        } else {

            appDir = Config.defaultAppDirPath;
        }

        File appDirFile = new File(appDir);

        if (appDirFile.isDirectory()) {


            Set<String> hasAnalysisAPP = new ReadFileOrInputStream(Config.intentConditionSymbolicExcutationResults + "/" + "hasSatisticIfReducedAndPreviousIF.txt", exceptionLogger).getAllContentLinSet();


            for (File file : appDirFile.listFiles()) {
                if ((!file.getName().contains("_signed_zipalign")) && file.getName().endsWith(".apk")) {

                    if (hasAnalysisAPP.contains(file.getAbsolutePath())) {
                        continue;
                    }


                    Thread childThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(file.getAbsolutePath() + "开始分析" + "\n");


                            long startTime = System.nanoTime();
                            IntentConditionTransformSymbolicExcutation intentConditionTransform = new IntentConditionTransformSymbolicExcutation(file.getAbsolutePath());
                            intentConditionTransform.run();
                            long endTime = System.nanoTime();


                            WriteFile writeFileHasBeenProcessedApp = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "hasSatisticIfReducedAndPreviousIF.txt", true, exceptionLogger);
                            writeFileHasBeenProcessedApp.writeStr(file.getAbsolutePath() + "\n");
                            writeFileHasBeenProcessedApp.close();

                            WriteFile writeFileTimeUse = new WriteFile(Config.intentConditionSymbolicExcutationResults + "/" + "timeUse.txt", true, exceptionLogger);
                            writeFileTimeUse.writeStr(((((double) (endTime - startTime)) / 1E9) + "," + file.getAbsolutePath() + "\n"));
                            writeFileTimeUse.close();


                            MyLogger.getOverallLogger(IntentConditionTransformSymbolicExcutation.class).info(file.getAbsolutePath() + "分析结束" + "\n");


                        }

                    });

                    childThread.start();

                    try {
                        childThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }


            }


        } else {


            IntentConditionTransformSymbolicExcutation intentConditionTransform = new IntentConditionTransformSymbolicExcutation(appDir);
            intentConditionTransform.run();


        }


        ifReducedWriter.close();


        writeFileCallGraphSize.close();
        appUnitGraphPathReducedReachLimit.close();


    }

    private void saveIntent(HashSet<Intent> allIntentConditionOfOneApp, String appPath) {


        WriteFile writeFileSingleIntent = new WriteFile(Config.intent_file_path + "/" + new File(appPath).getName() + ".txt", false, exceptionLogger);
        for (Intent intent : allIntentConditionOfOneApp) {

            // if (Util.judgeIntentIsUseful(intent)) {
            writeFileSingleIntent.writeStr(intent.toString() + "\n");
            // }


        }
        writeFileSingleIntent.close();
    }


}