package com.popoaichuiniu.jacy.statistic;

import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.*;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;

import java.io.File;
import java.util.*;

class TargetComponent {
    public static int ACTION = 0;
    public static int COMPONENT_NAME = 1;
    int type;
    String value;

    public TargetComponent(int type, String value) {
        this.type = type;
        this.value = value;
    }
}

class IntentStatus {
    boolean isExplicit = false;
    boolean isStartInternalComponet = true;

    public IntentStatus(boolean isExplicit, boolean isStartInternalComponet) {
        this.isExplicit = isExplicit;
        this.isStartInternalComponet = isStartInternalComponet;
    }


}

public class IntentImplicitUse {


    private static Logger logger = new MyLogger("EANotProper/IntentImplicitUse", "appException").getLogger();

    private static WriteFile startIntent = new WriteFile("EANotProper/IntentImplicitUse/startIntentUseStatus.txt", false, logger);

    private static Set<String> iccMethods = new HashSet<>();

    private static Set<String> intentSetTargetMethods = new HashSet<>();

    private String appPath = null;

    private int allCount = 0;

    private int explicitCount = 0;

    private static long resultAllCount = 0;

    private static long resultExplicitCount = 0;


    private static WriteFile results = new WriteFile("EANotProper/IntentImplicitUse/result.txt", false, logger);

    public IntentImplicitUse(String appPath) {
        this.appPath = appPath;
    }

    public static void main(String[] args) {

        Set<String> iccContent = new ReadFileOrInputStream("EANotProper/ICC.txt", logger).getAllContentLinSet();

        for (String str : iccContent) {
            String[] temp = str.split("\\(");
            iccMethods.add(temp[0].trim());
        }

        Set<String> intentMethodAboutTargetDataContent = new ReadFileOrInputStream("EANotProper/intentMethodAboutTargetData.txt", logger).getAllContentLinSet();

        for (String str : intentMethodAboutTargetDataContent) {
            String[] temp = str.split("\\(");
            intentSetTargetMethods.add(temp[0].trim());
        }


        String appDirPath = Config.wandoijiaAPP;
        File appDir = new File(appDirPath);
        for (File file : appDir.listFiles()) {
            if (file.getName().endsWith(".apk")) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {


                        try {
                            IntentImplicitUse intentImplicitUse = new IntentImplicitUse(file.getAbsolutePath());
                            startIntent.writeStr("************************" + file.getAbsolutePath() + "\n");
                            intentImplicitUse.doAnalysisOneApk(file);
                            startIntent.writeStr("******************************************************");
                        } catch (Exception e) {
                            logger.error(file.getAbsolutePath() + "##" + e.getMessage() + "&&" + ExceptionStackMessageUtil.getStackTrace(e));
                        }


                    }
                });

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {

                }


            }
        }

        results.writeStr("result**********************************************************" + "\n");
        results.writeStr(resultExplicitCount + " " + resultAllCount + "\n");
        results.close();
        startIntent.close();


    }


    private Set<String> actions = new HashSet<>();

    private Set<String> componentsName = new HashSet<>();

    private void doAnalysisOneApk(File file) {
        AndroidCallGraphProxy androidCallGraphProxy = new AndroidCallGraphProxy(file.getAbsolutePath(), Config.androidJar, logger);
        CallGraph callGraph = androidCallGraphProxy.androidCallGraph.getCg();

        AndroidInfo androidInfo = new AndroidInfo(file.getAbsolutePath(), logger);
        getComponentInfo(androidInfo);
        Set<SootMethod> sootMethodSet = new HashSet<>();
        for (Iterator<Edge> iterator = callGraph.iterator(); iterator.hasNext(); ) {
            Edge edge = iterator.next();
            System.out.println(
                    edge.getSrc().method().getBytecodeSignature() + "——>" + edge.getTgt().method().getBytecodeSignature());
            sootMethodSet.add(edge.getSrc().method());
            sootMethodSet.add(edge.tgt().method());


        }

        for (SootMethod sootMethod : sootMethodSet) {
            doAnalysisOneSootMethod(sootMethod);
        }

        resultAllCount = resultAllCount + allCount;
        resultExplicitCount = resultExplicitCount + explicitCount;

        results.writeStr(explicitCount + " " + allCount + " " + file.getAbsolutePath() + "\n");
        results.flush();


    }

    private Set<String> getComponentInfo(AndroidInfo androidInfo) {


        Map<String, AXmlNode> components = androidInfo.getComponents();

        for (Map.Entry<String, AXmlNode> entry : components.entrySet()) {
            AXmlNode aXmlNode = entry.getValue();
            AXmlAttribute<?> componentName = aXmlNode.getAttribute("name");
            if (componentName != null) {
                componentsName.add(((String) componentName.getValue()).trim());
            }
            List<AXmlNode> intentFilterList = aXmlNode.getChildrenWithTag("intent-filter");
            for (AXmlNode aXmlNodeIntentFilter : intentFilterList) {
                List<AXmlNode> actionIntentFilterList = aXmlNodeIntentFilter.getChildrenWithTag("action");

                for (AXmlNode actionIntentFilter : actionIntentFilterList) {
                    AXmlAttribute<?> actionName = actionIntentFilter.getAttribute("name");
                    if (actionName != null) {
                        actions.add(((String) actionName.getValue()).trim());
                    }


                }


            }


        }

        return actions;


    }


    private void doAnalysisOneSootMethod(SootMethod sootMethod) {


        if (!Util.isApplicationMethod(sootMethod)) {
            return;
        }
        Body body = null;
        try {
            body = sootMethod.getActiveBody();
        } catch (RuntimeException e) {
            //logger.error(appPath + "##" + e.getMessage() + "&&" + ExceptionStackMessageUtil.getStackTrace(e));
            return;
        }

        for (Unit unit : body.getUnits()) {
            Stmt stmt = (Stmt) unit;
            SootMethod calleeSootMethod = Util.getCalleeSootMethodAt(unit);
            if (calleeSootMethod != null) {
                if (iccMethods.contains(calleeSootMethod.getName())) {
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    for (Value value : invokeExpr.getArgs()) {
                        if (value.getType().toString().equals("android.content.Intent")) {
                            allCount++;
                            startIntent.writeStr("Intent:\n");
                            boolean flag = doAnalysisIntentIsExplicit(value, stmt, sootMethod);
                            if (flag) {
                                explicitCount++;
                                startIntent.writeStr("vvvv" + "\n");
                            } else {
                                startIntent.writeStr("xxxx" + "\n");
                            }
                            startIntent.writeStr("\n\n");
                            startIntent.flush();

                            doAnalysisIntentIsStartInternalComponent(value, stmt, sootMethod);
                        }
                    }
                }
            }
        }
    }

    private TargetComponent doAnalysisIntentIsStartInternalComponent(Value value, Unit unit, SootMethod sootMethod) {
        if (!(value instanceof Local)) {
            throw new RuntimeException("!(value instanceof Local)");
        }
        Local local = (Local) value;
        BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
        SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);
        List<Unit> defUnitList = simpleLocalDefs.getDefsOfAt(local, unit);

        SimpleLocalUses simpleLocalUses = new SimpleLocalUses(briefUnitGraph, simpleLocalDefs);

        Unit defUnit = getDefUnit(local, unit, simpleLocalDefs);

        if (!(defUnit instanceof DefinitionStmt)) {
            throw new RuntimeException("!(defUnit instanceof DefinitionStmt)");
        }
        DefinitionStmt defStmt = (DefinitionStmt) defUnit;
        List<UnitValueBoxPair> listUnitUse = simpleLocalUses.getUsesOf(defUnit);


        for (UnitValueBoxPair unitValueBoxPair : listUnitUse) {
            Unit useUnit = unitValueBoxPair.unit;
            Stmt useStmt = (Stmt) useUnit;
            if (!useStmt.containsInvokeExpr()) {
                continue;
            }
            InvokeExpr invokeExpr = useStmt.getInvokeExpr();
            if (invokeExpr instanceof InstanceInvokeExpr) {

                SootMethod calleeSootMethod = invokeExpr.getMethod();
                InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;

                if (calleeSootMethod.getDeclaringClass().getName().equals("android.content.Intent") && instanceInvokeExpr.getBase() == defStmt.getLeftOp()) {

                    startIntent.writeStr(useStmt + "\n");
                    if (calleeSootMethod.getName().equals("<init>")) {

                        for (Value arg : instanceInvokeExpr.getArgs()) {
                            if (arg.getType().toString().contains("java.lang.Class")) {

                                return doAnalysisOnClassTypeValue(arg, useUnit, sootMethod, simpleLocalDefs);

                            }


                        }

                        for (Value arg : instanceInvokeExpr.getArgs()) {
                            if (arg.getType().toString().contains("java.lang.String")) {//action
                                return doAnalysisOnStringTypeValue(arg, useUnit, sootMethod, simpleLocalDefs);

                            }


                        }
                        for (Value arg : instanceInvokeExpr.getArgs()) {//看是否存在其的一个intent来自其他intent
                            if (arg.getType().toString().contains("android.content.Intent")) {
                                return doAnalysisIntentIsStartInternalComponent(arg, useUnit, sootMethod);

                            }


                        }


                    }

                    if (calleeSootMethod.getName().equals("setClass")) {
                        for (Value arg : instanceInvokeExpr.getArgs()) {
                            if (arg.getType().toString().contains("java.lang.Class")) {

                                return doAnalysisOnClassTypeValue(arg, useUnit, sootMethod, simpleLocalDefs);

                            }


                        }
                    }

                    if (calleeSootMethod.getName().equals("setClassName")) {
                        TargetComponent targetComponentPackage = doAnalysisOnStringTypeValue(instanceInvokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs);
                        TargetComponent targetComponentClassName = doAnalysisOnStringTypeValue(instanceInvokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs);
                        String packageName = "";
                        String className = "";
                        if (targetComponentPackage != null) {
                            packageName = targetComponentPackage.value;
                        }

                        if (className != null) {
                            className = targetComponentClassName.value;
                        }

                        String result = null;
                        if (packageName.length() != 0) {
                            result = packageName + "." + className;
                        } else {
                            result = className;
                        }

                        if (className.length() != 0) {
                            TargetComponent targetComponent = new TargetComponent(TargetComponent.COMPONENT_NAME, result);
                            return targetComponent;
                        }
                        return null;


                    }

                    if (calleeSootMethod.getName().equals("setComponent")) {

                        return doAnalysisOnComponentTypeValue(instanceInvokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs, briefUnitGraph);

                    }

                }


            }


        }


        return null;
    }

    private TargetComponent doAnalysisOnComponentTypeValue(Value arg, Unit useUnit, SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs, BriefUnitGraph briefUnitGraph) {

        if (arg instanceof Local) {

            Local local = (Local) arg;

            Unit defUnit = getDefUnit(local, useUnit, simpleLocalDefs);

            //----------------------------


        }

        return null;
    }

    private Unit getDefUnit(Local local, Unit useUnit, SimpleLocalDefs simpleLocalDefs) {

        List<Unit> defUnitList = simpleLocalDefs.getDefsOfAt(local, useUnit);
        Unit defUnit = null;
        int lineNumer = -1;
        for (Unit unit : defUnitList) {
            if (unit.getJavaSourceStartLineNumber() != -1) {

                if (unit.getJavaSourceStartLineNumber() > lineNumer) {
                    lineNumer = unit.getJavaSourceStartLineNumber();
                    defUnit = unit;
                }

            } else {
                throw new RuntimeException("unit.getJavaSourceStartLineNumber() = -1");
            }

        }

        return defUnit;
    }

    private TargetComponent doAnalysisOnStringTypeValue(Value arg, Unit useUnit, SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs) {

        if (arg instanceof StringConstant) {
            StringConstant stringConstant = (StringConstant) arg;
            TargetComponent targetComponent = new TargetComponent(TargetComponent.ACTION, stringConstant.value);
            return targetComponent;
        } else if (arg instanceof Local) {
            Local local = (Local) arg;
            Unit defUnit = getDefUnit(local, useUnit, simpleLocalDefs);

            DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;
            Value value = definitionStmt.getRightOp();
            return doAnalysisOnStringTypeValue(value, defUnit, sootMethod, simpleLocalDefs);
        }

        return null;
    }

    private TargetComponent doAnalysisOnClassTypeValue(Value arg, Unit useUnit, SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs) {

        if (arg instanceof ClassConstant) {

            ClassConstant classConstant = (ClassConstant) arg;
            TargetComponent targetComponent = new TargetComponent(TargetComponent.COMPONENT_NAME, classConstant.getValue());

            return targetComponent;


        } else if (arg instanceof Local) {
            Local local = (Local) arg;
            Unit defUnit = getDefUnit(local, useUnit, simpleLocalDefs);
            DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;
            Value value = definitionStmt.getRightOp();
            return doAnalysisOnClassTypeValue(value, defUnit, sootMethod, simpleLocalDefs);

        }

        return null;
    }

    private boolean doAnalysisIntentIsExplicit(Value value, Unit unit, SootMethod sootMethod) {


        if (!(value instanceof Local)) {
            throw new RuntimeException("!(value instanceof Local)");
        }
        Local local = (Local) value;
        BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
        SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);
        List<Unit> defUnitList = simpleLocalDefs.getDefsOfAt(local, unit);

        SimpleLocalUses simpleLocalUses = new SimpleLocalUses(briefUnitGraph, simpleLocalDefs);

        for (Unit defUnit : defUnitList) {
            if (!(defUnit instanceof DefinitionStmt)) {
                throw new RuntimeException("!(defUnit instanceof DefinitionStmt)");
            }
            DefinitionStmt defStmt = (DefinitionStmt) defUnit;
            List<UnitValueBoxPair> listUnitUse = simpleLocalUses.getUsesOf(defUnit);


            for (UnitValueBoxPair unitValueBoxPair : listUnitUse) {
                Unit useUnit = unitValueBoxPair.unit;
                Stmt useStmt = (Stmt) useUnit;
                if (!useStmt.containsInvokeExpr()) {
                    continue;
                }
                InvokeExpr invokeExpr = useStmt.getInvokeExpr();
                if (invokeExpr instanceof InstanceInvokeExpr) {

                    SootMethod calleeSootMethod = invokeExpr.getMethod();
                    InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;

                    if (calleeSootMethod.getDeclaringClass().getName().equals("android.content.Intent") && instanceInvokeExpr.getBase() == defStmt.getLeftOp()) {

                        startIntent.writeStr(useStmt + "\n");
                        if (calleeSootMethod.getName().equals("<init>")) {

                            for (Value arg : instanceInvokeExpr.getArgs()) {
                                if (arg.getType().toString().contains("java.lang.Class")) {
                                    return true;
                                }


                            }
                            for (Value arg : instanceInvokeExpr.getArgs()) {//看是否存在其的一个intent来自其他intent
                                if (arg.getType().toString().contains("android.content.Intent")) {
                                    return doAnalysisIntentIsExplicit(arg, useUnit, sootMethod);

                                }


                            }


                        }

                        if (calleeSootMethod.getName().equals("setClass") || calleeSootMethod.getName().equals("setClassName") || calleeSootMethod.getName().equals("setComponent")) {
                            return true;
                        }

                    }


                }


            }

        }

        return false;
    }
}
