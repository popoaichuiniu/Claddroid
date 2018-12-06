package com.popoaichuiniu.jacy.statistic;

import com.popoaichuiniu.intentGen.IntentPropagateAnalysis;
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
    public static int HYBIRD = 2;
    int type = -1;

    String[] hybirdValues = new String[2];

    public TargetComponent(int type, String value) {
        this.type = type;
        hybirdValues[type] = value;
    }

    public TargetComponent() {

    }

    public int isInternalIntent(Set<String> actions, Set<String> components)//actions 自定义action值集合
    {
        if (type == -1) {
            return -1;
        }

        if (type == TargetComponent.HYBIRD || type == TargetComponent.COMPONENT_NAME) {

            String componentStringSet = hybirdValues[TargetComponent.COMPONENT_NAME].replaceAll("/", ".");

            Set<String> componentSet = new HashSet<>();

            for (String str : componentStringSet.split("#")) {
                componentSet.add(str);
            }


            for (String oneComponent : componentSet) {
                if (components.contains(oneComponent)) {
                    return 1;
                }
            }


        }


        if (type == TargetComponent.ACTION) {
            Set<String> actionSet = new HashSet<>();
            for (String str : hybirdValues[TargetComponent.ACTION].split("#")) {
                actionSet.add(str);
            }

            for (String act : actionSet) {

                if (actions.contains(act)) {
                    return 1;
                }

            }
        }


        return 0;


    }

    public void mixTargetComponent(TargetComponent targetComponent) {
        if (targetComponent != null && targetComponent.type != -1) {

            if (targetComponent.type != TargetComponent.HYBIRD) {
                if (this.hybirdValues[targetComponent.type] == null) {
                    this.hybirdValues[targetComponent.type] = targetComponent.hybirdValues[targetComponent.type];
                } else {
                    if (targetComponent.hybirdValues[targetComponent.type] != null) {
                        this.hybirdValues[targetComponent.type] = this.hybirdValues[targetComponent.type] + "#" + targetComponent.hybirdValues[targetComponent.type];
                    }
                }

            } else {
                for (int i = 0; i < targetComponent.type; i++) {
                    if (this.hybirdValues[i] == null) {
                        this.hybirdValues[i] = targetComponent.hybirdValues[i];
                    } else {
                        if (targetComponent.hybirdValues[i] != null) {
                            this.hybirdValues[i] = this.hybirdValues[i] + "#" + targetComponent.hybirdValues[i];
                        }
                    }
                }
            }

            if (hybirdValues[0] == null && hybirdValues[1] == null) {
                this.type = -1;
            }

            if (hybirdValues[0] != null && hybirdValues[1] == null) {
                this.type = 0;
            }

            if (hybirdValues[0] == null && hybirdValues[1] != null) {
                this.type = 1;
            }
            if (hybirdValues[0] != null && hybirdValues[1] != null) {
                this.type = 2;
            }


        }

    }

    public void updateTargetComponent(TargetComponent targetComponent) {
        if (targetComponent != null && targetComponent.type != -1) {

            if (targetComponent.type != TargetComponent.HYBIRD) {

                this.hybirdValues[targetComponent.type] = targetComponent.hybirdValues[targetComponent.type];


            } else {
                for (int i = 0; i < targetComponent.type; i++) {

                    this.hybirdValues[i] = targetComponent.hybirdValues[i];


                }
            }

            if (hybirdValues[0] == null && hybirdValues[1] == null) {
                this.type = -1;
            }

            if (hybirdValues[0] != null && hybirdValues[1] == null) {
                this.type = 0;
            }

            if (hybirdValues[0] == null && hybirdValues[1] != null) {
                this.type = 1;
            }
            if (hybirdValues[0] != null && hybirdValues[1] != null) {
                this.type = 2;
            }


        }

    }

    @Override
    public String toString() {

        String result = "";
        if (type == 2) {
            result = "TargetComponent{" +
                    "action=" + hybirdValues[0] +
                    ",componentName=" + hybirdValues[1] +
                    "}";

        } else if (type == 0) {
            result = "TargetComponent{" +
                    "action=" + hybirdValues[0] +
                    "}";
        } else if (type == 1) {
            result = "TargetComponent{" +
                    "componentName=" + hybirdValues[1] +
                    "}";
        } else {
            result = "null";
        }

        return result;


    }
}

class IntentStatus {
    int isExplicit = -1;
    int isStartInternalComponet = -1;


    public IntentStatus() {

    }

    public int isExplicitInternal() {
        if (isExplicit == -1 || isStartInternalComponet == -1) {
            return -1;
        }

        if (isExplicit == 1 && isStartInternalComponet == 1) {
            return 1;
        } else {
            return 0;
        }
    }


}

public class IntentImplicitUse {


    private static Logger logger = new MyLogger("EANotProper/IntentImplicitUse", "appException").getLogger();
    private static Logger infoLogger = new MyLogger("EANotProper/IntentImplicitUse", "info").getLogger();
    private static Logger targetComponentLogger = new MyLogger("EANotProper/IntentImplicitUse", "targetComponentLogger").getLogger();

    private static WriteFile startIntent = new WriteFile("EANotProper/IntentImplicitUse/startIntentUseStatus.txt", false, logger);

    private static Set<String> iccMethods = new HashSet<>();

    private static Set<String> intentSetTargetMethods = new HashSet<>();

    private static Set<String> systemActions = new HashSet<>();

    private String appPath = null;

    private int allCount = 0;

    private int explicitCount = 0;

    private static long resultAllCount = 0;

    private static long resultExplicitCount = 0;


    private static WriteFile results = new WriteFile("EANotProper/IntentImplicitUse/result.txt", false, logger);

    public IntentImplicitUse(String appPath) {
        this.appPath = appPath;
    }

    static {
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


        Set<String> systemActionsContent = new ReadFileOrInputStream("EANotProper/system_actions.txt", logger).getAllContentLinSet();

        for (String str : systemActionsContent) {
            systemActions.add(str.trim());
        }
    }

    public static void main(String[] args) {


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


    private Set<String> actions = new HashSet<>();//自定义actions

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

    private void getComponentInfo(AndroidInfo androidInfo) {


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

        actions.removeAll(systemActions);


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

            //doAnalysisOnNewIntentPropagate(sootMethod,unit);
            doAnalysisOnICC(sootMethod, unit);
        }
    }

    private void doAnalysisOnNewIntentPropagate(SootMethod sootMethod, Unit unit) {
        Stmt stmt = (Stmt) unit;
        if (stmt instanceof DefinitionStmt) {
            DefinitionStmt definitionStmt = (DefinitionStmt) stmt;
            Value value = definitionStmt.getRightOp();
            if (value instanceof NewExpr) {
                NewExpr newExpr = (NewExpr) value;
                if (newExpr.getType().toString().equals("android.content.Intent")) {
                    doAnalysisOnNewInstanceIntent(definitionStmt, sootMethod);
                }
            }
        }


    }

    private void doAnalysisOnNewInstanceIntent(DefinitionStmt definitionStmt, SootMethod sootMethod) {

        BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
        IntentPropagateAnalysis intentPropagateAnalysis = new IntentPropagateAnalysis(sootMethod, briefUnitGraph, definitionStmt);
    }

    private void doAnalysisOnICC(SootMethod sootMethod, Unit unit) {
        Stmt stmt = (Stmt) unit;
        SootMethod calleeSootMethod = Util.getCalleeSootMethodAt(unit);
        if (calleeSootMethod != null) {
            if (iccMethods.contains(calleeSootMethod.getName())) {
                InvokeExpr invokeExpr = stmt.getInvokeExpr();
                for (Value value : invokeExpr.getArgs()) {
                    if (value.getType().toString().equals("android.content.Intent")) {


                        IntentStatus intentStatus = new IntentStatus();
                        startIntent.writeStr("Intent:\n");
                        int flag = doAnalysisIntentIsExplicit(value, stmt, sootMethod);

                        intentStatus.isExplicit = flag;

                        if (flag != -1) {

                            if (flag == 1) {
                                startIntent.writeStr("vvvv" + "\n");
                            } else {
                                startIntent.writeStr("xxxx" + "\n");
                            }
                            startIntent.writeStr("\n\n");
                            startIntent.flush();
                        }


                        TargetComponent targetComponent = doAnalysisIntentIsStartInternalComponent(value, stmt, stmt, sootMethod);
                        if (targetComponent != null) {

                            targetComponentLogger.info(targetComponent.toString());

                            int result = targetComponent.isInternalIntent(actions, componentsName);

                            intentStatus.isStartInternalComponet = result;


                        }

                        if (intentStatus.isExplicitInternal() != -1) {
                            allCount++;

                            if (intentStatus.isExplicitInternal() == 1) {
                                explicitCount++;
                            }
                        }


                    }
                }
            }
        }
    }


    class MethodIntentUnit {
        Unit unit;
        SootMethod sootMethod;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodIntentUnit that = (MethodIntentUnit) o;
            return Objects.equals(unit, that.unit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unit);
        }

        public MethodIntentUnit(Unit unit, SootMethod sootMethod) {
            this.unit = unit;
            this.sootMethod = sootMethod;
        }
    }

    private TargetComponent doAnalysisIntentIsStartInternalComponent(Value value, Unit useIntentUnit, Unit iccUnit, SootMethod sootMethod) {


        List<TargetComponent> targetComponentList = new ArrayList<>();
        if (!(value instanceof Local)) {
            throw new RuntimeException("!(value instanceof Local)");
        }
        Local local = (Local) value;

        Set<SootMethod> intentPropagatePathSootMethodSet = new HashSet<>();//intent传递的所有方法
        Set<MethodIntentUnit> entryIntentSootMethodSet = new HashSet<>();
        Set<Local> visited = new HashSet<>();
        getIntentPropagatePathSootMethodSet(visited, intentPropagatePathSootMethodSet, entryIntentSootMethodSet, local, useIntentUnit, sootMethod);

        TargetComponent targetComponentUl = new TargetComponent();

        for (MethodIntentUnit methodIntentUnit : entryIntentSootMethodSet) {

            Unit startUnit = methodIntentUnit.unit;
            SootMethod startSootMethod = methodIntentUnit.sootMethod;

            TargetComponent targetComponentAll=new TargetComponent();


            getIntentPathUnitSet(targetComponentAll,startUnit, startSootMethod,-1, iccUnit, intentPropagatePathSootMethodSet);
            targetComponentUl.mixTargetComponent(targetComponentAll);

        }

        return targetComponentUl;
    }

    private void getIntentPathUnitSet(TargetComponent targetComponentAll,Unit startUnit, SootMethod sootMethod,  int parameterIndex,Unit iccUnit, Set<SootMethod> intentPropagatePathSootMethodSet) {

        if (sootMethod.hasActiveBody()) {
            BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
            SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);
            IntentPropagateAnalysis intentPropagateAnalysis = new IntentPropagateAnalysis(sootMethod, briefUnitGraph, startUnit,parameterIndex);
            Set<Unit> useIntentUnitSet = intentPropagateAnalysis.useIntentUnitSet;

            for (Unit useIntentUnit : useIntentUnitSet) {
                Stmt useIntentStmt = (Stmt) useIntentUnit;
                if (useIntentUnit == iccUnit) {
                    return ;
                }
                if (useIntentStmt.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = useIntentStmt.getInvokeExpr();
                    if (invokeExpr instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
                        if (instanceInvokeExpr.getBase().getType().toString().equals("android.content.Intent")) {

                            TargetComponent targetComponent = getTargetInfoFromOneUnit(sootMethod, briefUnitGraph, simpleLocalDefs, useIntentUnit, iccUnit, invokeExpr);
                            targetComponentAll.updateTargetComponent(targetComponent);
                        }

                    }

                    if(!invokeExpr.getMethod().getDeclaringClass().equals("android.content.Intent"))//不是Intent方法
                    {
                        if(intentPropagatePathSootMethodSet.contains(invokeExpr.getMethod()))//注意可能死循环，怎么排除
                        {

                            int index=-1;
                            for(int i=0;i<invokeExpr.getArgs().size();i++)
                            {
                                if(invokeExpr.getArg(i).getType().toString().equals("android.content.Intent"))
                                {
                                    index=i;
                                    break;
                                }
                            }

                            getIntentPathUnitSet(targetComponentAll,null,invokeExpr.getMethod(),index,iccUnit,intentPropagatePathSootMethodSet);

                        }
                    }

                }

                if(useIntentStmt instanceof ReturnStmt)//返回值
                {

                    CallGraph cg=Scene.v().getCallGraph();

                    for (Iterator<Edge> iterator = cg.edgesInto(sootMethod); iterator.hasNext(); ) {//多个都能到iccUnit怎么办？
                        Edge edge = iterator.next();
                        Unit scrUnit = edge.srcUnit();
                        if (scrUnit != null) {
                            SootMethod srcMethod = edge.src();
                            if(intentPropagatePathSootMethodSet.contains(sootMethod))
                            {
                                getIntentPathUnitSet(targetComponentAll,scrUnit,srcMethod,-1,iccUnit,intentPropagatePathSootMethodSet);
                            }

                        }
                    }



                }
            }


        }


    }

    private void getIntentPropagatePathSootMethodSet(Set<Local> visited, Set<SootMethod> intentPropagatePathSootMethodSet, Set<MethodIntentUnit> entryIntentSootMethodSet, Local local, Unit useLocalUnit, SootMethod sootMethod) {

        if (!Util.isApplicationMethod(sootMethod)) {
            return;
        }

        if (!sootMethod.hasActiveBody()) {
            return;
        }

        if (visited.contains(local)) {
            return;
        }

        visited.add(local);
        intentPropagatePathSootMethodSet.add(sootMethod);

        BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
        SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);
        for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, useLocalUnit)) {
            DefinitionStmt defStmt = (DefinitionStmt) defUnit;
            if (defStmt.getRightOp() instanceof NewExpr)// $r2=new android.content.Intent
            {
                MethodIntentUnit methodIntentUnit = new MethodIntentUnit(defUnit, sootMethod);
                entryIntentSootMethodSet.add(methodIntentUnit);

            } else if (defStmt.getRightOp() instanceof Local)//$r6 = $r13
            {
                Local assignLocal = (Local) defStmt.getRightOp();
                getIntentPropagatePathSootMethodSet(visited, intentPropagatePathSootMethodSet, entryIntentSootMethodSet, assignLocal, defUnit, sootMethod);
            } else if (defStmt.getRightOp() instanceof ParameterRef)//$r1 := @parameter0: android.content.Intent
            {
                ParameterRef parameterRef = (ParameterRef) defStmt.getRightOp();
                CallGraph cg = Scene.v().getCallGraph();
                for (Iterator<Edge> iterator = cg.edgesInto(sootMethod); iterator.hasNext(); ) {
                    Edge edge = iterator.next();
                    Unit scrUnit = edge.srcUnit();
                    if (scrUnit != null) {
                        SootMethod srcMethod = edge.src();
                        Stmt stmt = (Stmt) scrUnit;
                        if (stmt.containsInvokeExpr()) {
                            int index = parameterRef.getIndex();
                            Value intentValue = stmt.getInvokeExpr().getArg(index);
                            if (intentValue.getType().toString().equals("android.content.Intent")) {
                                if (intentValue instanceof Local) {
                                    Local intentLocal = (Local) intentValue;
                                    getIntentPropagatePathSootMethodSet(visited, intentPropagatePathSootMethodSet, entryIntentSootMethodSet, intentLocal, scrUnit, srcMethod);
                                } else {
                                    logger.error("未预料的错误！intentValue instanceof Local ");

                                }

                            } else {

                                logger.error("未预料的错误！intentValue.getType().toString().equals(\"android.content.Intent\")");

                            }
                        } else {
                            logger.error("未预料的错误！stmt.containsInvokeExpr()");
                        }

                    }

                }

            } else if (defStmt.getRightOp() instanceof InvokeExpr) {
                InvokeExpr invokeExpr = (InvokeExpr) defStmt.getRightOp();
                if (invokeExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent") && invokeExpr.getMethod().getName().equals("<init>")) {
                    if (invokeExpr.getArgs().size() == 1 && invokeExpr.getArg(0).getType().toString().equals("android.content.Intent")) {
                        Local intentLocal = (Local) invokeExpr.getArg(0);
                        getIntentPropagatePathSootMethodSet(visited, intentPropagatePathSootMethodSet, entryIntentSootMethodSet, intentLocal, defUnit, sootMethod);
                    }
                } else {
                    SootMethod returnIntentMethod = invokeExpr.getMethod();
                    if (returnIntentMethod.hasActiveBody()) {
                        Body body = returnIntentMethod.getActiveBody();
                        for (Unit unit : body.getUnits()) {
                            if (unit instanceof ReturnStmt) {
                                ReturnStmt returnStmt = (ReturnStmt) unit;
                                Value intentValue = returnStmt.getOp();
                                if (intentValue instanceof Local) {//可能返回null
                                    if (intentValue.getType().toString().equals("android.content.Intent")) {
                                        getIntentPropagatePathSootMethodSet(visited, intentPropagatePathSootMethodSet, entryIntentSootMethodSet, (Local) intentValue, returnStmt, returnIntentMethod);
                                    } else {

                                        logger.error("未预料的错误！intentValue instanceof Local");
                                    }

                                }

                            }
                        }
                    }
                }
            } else {

                infoLogger.info(useLocalUnit);

            }


        }

    }


    private void forEveryUnit(SootMethod sootMethod, List<TargetComponent> targetComponentList, BriefUnitGraph briefUnitGraph, SimpleLocalDefs simpleLocalDefs, SimpleLocalUses simpleLocalUses, Unit defUnit) {
        TargetComponent targetComponentAll = new TargetComponent();
        if (!(defUnit instanceof DefinitionStmt)) {
            throw new RuntimeException("!(defUnit instanceof DefinitionStmt)");
        }
        DefinitionStmt defStmt = (DefinitionStmt) defUnit;

        if (!(defStmt.getRightOp() instanceof NewExpr)) {

            infoLogger.warn("! defStmt.getRightOp() instanceof NewExpr intent " + defStmt);


        }
        List<UnitValueBoxPair> listUnitUse = simpleLocalUses.getUsesOf(defUnit);


        for (UnitValueBoxPair unitValueBoxPair : listUnitUse) {
            Unit useUnit = unitValueBoxPair.unit;
            Stmt useStmt = (Stmt) useUnit;
            if (!useStmt.containsInvokeExpr()) {
                continue;
            }
            InvokeExpr invokeExpr = useStmt.getInvokeExpr();
            getTargetInfo(sootMethod, briefUnitGraph, simpleLocalDefs, targetComponentAll, defStmt, useUnit, useStmt, invokeExpr);


        }


        targetComponentList.add(targetComponentAll);
    }

    private void getTargetInfo(SootMethod sootMethod, BriefUnitGraph briefUnitGraph, SimpleLocalDefs simpleLocalDefs, TargetComponent targetComponentAll, DefinitionStmt defStmt, Unit useUnit, Stmt useStmt, InvokeExpr invokeExpr) {
        if (invokeExpr instanceof InstanceInvokeExpr) {

            SootMethod calleeSootMethod = invokeExpr.getMethod();
            InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;

            if (calleeSootMethod.getDeclaringClass().getName().equals("android.content.Intent") && instanceInvokeExpr.getBase() == defStmt.getLeftOp()) {

                startIntent.writeStr(useStmt + "\n");
                if (calleeSootMethod.getName().equals("<init>")) {

                    if (instanceInvokeExpr.getArgs().size() == 1) {
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("android.content.Intent")) {//Intent(Intent o)

                            TargetComponent targetComponent = doAnalysisIntentIsStartInternalComponent(arg, useUnit, sootMethod);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                        if (arg.getType().toString().contains("java.lang.String")) {//Intent(String action)
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                    }

                    if (instanceInvokeExpr.getArgs().size() == 2) {
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("java.lang.String")) {//Intent(String action, Uri uri)
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                        Value arg1 = instanceInvokeExpr.getArg(1);

                        if (arg1.getType().toString().contains("java.lang.Class")) {//Intent(Context packageContext, Class<?> cls)

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg1, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }


                    }

                    if (instanceInvokeExpr.getArgs().size() == 4) {//Intent(String action, Uri uri, Context packageContext, Class<?> cls)
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("java.lang.String")) {
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                        Value arg3 = instanceInvokeExpr.getArg(3);
                        if (arg3.getType().toString().contains("java.lang.Class")) {

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg3, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                    }

                }

                if (calleeSootMethod.getName().equals("setClass")) {//	setClass(Context packageContext, Class<?> cls)
                    for (Value arg : instanceInvokeExpr.getArgs()) {
                        if (arg.getType().toString().contains("java.lang.Class")) {

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }


                    }
                }

                if (calleeSootMethod.getName().equals("setClassName")) {//1.	setClassName(String packageName, String className)
                    //2.  setClassName(Context packageContext, String className)


                    TargetComponent targetComponent = computeTwoStringPackageCLassName(sootMethod, simpleLocalDefs, useUnit, instanceInvokeExpr);
                    targetComponentAll.updateTargetComponent(targetComponent);


                }

                if (calleeSootMethod.getName().equals("setComponent")) {//	setComponent(ComponentName component)

                    TargetComponent targetComponent = doAnalysisOnComponentTypeValue(instanceInvokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs, briefUnitGraph);
                    targetComponentAll.updateTargetComponent(targetComponent);


                }

            }


        }
    }


    private TargetComponent getTargetInfoFromOneUnit(SootMethod sootMethod, BriefUnitGraph briefUnitGraph, SimpleLocalDefs simpleLocalDefs, Unit useUnit, Unit iccUnit, InvokeExpr invokeExpr) {

        TargetComponent targetComponentAll = new TargetComponent();

        if (invokeExpr instanceof InstanceInvokeExpr) {

            SootMethod calleeSootMethod = invokeExpr.getMethod();
            InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;

            if (instanceInvokeExpr.getBase().getType().toString().equals("android.content.Intent")) {

                if (calleeSootMethod.getName().equals("<init>")) {

                    if (instanceInvokeExpr.getArgs().size() == 1) {
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("android.content.Intent")) {//Intent(Intent o)

                            TargetComponent targetComponent = doAnalysisIntentIsStartInternalComponent(arg, useUnit, iccUnit, sootMethod);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                        if (arg.getType().toString().contains("java.lang.String")) {//Intent(String action)
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                    }

                    if (instanceInvokeExpr.getArgs().size() == 2) {
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("java.lang.String")) {//Intent(String action, Uri uri)
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                        Value arg1 = instanceInvokeExpr.getArg(1);

                        if (arg1.getType().toString().contains("java.lang.Class")) {//Intent(Context packageContext, Class<?> cls)

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg1, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }


                    }

                    if (instanceInvokeExpr.getArgs().size() == 4) {//Intent(String action, Uri uri, Context packageContext, Class<?> cls)
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("java.lang.String")) {
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                        Value arg3 = instanceInvokeExpr.getArg(3);
                        if (arg3.getType().toString().contains("java.lang.Class")) {

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg3, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                    }

                }

                if (calleeSootMethod.getName().equals("setClass")) {//	setClass(Context packageContext, Class<?> cls)
                    for (Value arg : instanceInvokeExpr.getArgs()) {
                        if (arg.getType().toString().contains("java.lang.Class")) {

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }


                    }
                }

                if (calleeSootMethod.getName().equals("setClassName")) {//1.	setClassName(String packageName, String className)
                    //2.  setClassName(Context packageContext, String className)


                    TargetComponent targetComponent = computeTwoStringPackageCLassName(sootMethod, simpleLocalDefs, useUnit, instanceInvokeExpr);
                    targetComponentAll.updateTargetComponent(targetComponent);


                }

                if (calleeSootMethod.getName().equals("setComponent")) {//	setComponent(ComponentName component)

                    TargetComponent targetComponent = doAnalysisOnComponentTypeValue(instanceInvokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs, briefUnitGraph);
                    targetComponentAll.updateTargetComponent(targetComponent);


                }

            }


        }

        return targetComponentAll;
    }

    private TargetComponent computeTwoStringPackageCLassName(SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs, Unit useUnit, InvokeExpr invokeExpr) {
        String packageName = "";
        String className = "";
        if (invokeExpr.getArg(0).getType().toString().equals("java.lang.String")) {
            TargetComponent targetComponentPackage = doAnalysisOnStringTypeValue(TargetComponent.COMPONENT_NAME, invokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs);


            if (targetComponentPackage != null && targetComponentPackage.type != -1) {
                packageName = targetComponentPackage.hybirdValues[targetComponentPackage.type];
            }


        }

        if (invokeExpr.getArg(1).getType().toString().equals("java.lang.String")) {
            TargetComponent targetComponentClassName = doAnalysisOnStringTypeValue(TargetComponent.COMPONENT_NAME, invokeExpr.getArg(1), useUnit, sootMethod, simpleLocalDefs);
            if (targetComponentClassName != null && targetComponentClassName.type != -1) {
                className = targetComponentClassName.hybirdValues[targetComponentClassName.type];
            }
        }


        String result = null;
        if (className.length() != 0) {
            result = className;
        } else {
            if (packageName.length() != 0) {
                result = packageName;
            }
        }

        if (result != null) {
            TargetComponent targetComponent = new TargetComponent(TargetComponent.COMPONENT_NAME, result);
            return targetComponent;
        }

        return null;
    }

    private TargetComponent doAnalysisOnComponentTypeValue(Value arg, Unit useUnit, SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs, BriefUnitGraph briefUnitGraph) {


        List<TargetComponent> targetComponentList = new ArrayList<>();
        if (arg instanceof Local) {

            Local local = (Local) arg;

            for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, useUnit)) {
                TargetComponent targetComponentAll = new TargetComponent();
                DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;

                if (!(definitionStmt.getRightOp() instanceof NewExpr)) {

                    infoLogger.warn("!(definitionStmt.getRightOp() instanceof NewExpr) component " + definitionStmt);
                }

                SimpleLocalUses simpleLocalUses = new SimpleLocalUses(briefUnitGraph, simpleLocalDefs);

                List<UnitValueBoxPair> listUnitUse = simpleLocalUses.getUsesOf(defUnit);


                for (UnitValueBoxPair unitValueBoxPair : listUnitUse) {
                    Unit useComponentLocalUnit = unitValueBoxPair.unit;
                    Stmt useComponentLocalStmt = (Stmt) useComponentLocalUnit;
                    if (!useComponentLocalStmt.containsInvokeExpr()) {
                        continue;
                    }
                    InvokeExpr invokeExpr = useComponentLocalStmt.getInvokeExpr();
                    SootMethod calleeSootMethod = invokeExpr.getMethod();
                    if (invokeExpr instanceof InstanceInvokeExpr) {


                        InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
                        if (instanceInvokeExpr.getBase() == definitionStmt.getLeftOp() && calleeSootMethod.getDeclaringClass().getName().equals("android.content.ComponentName")) {
                            if (calleeSootMethod.getName().equals("<init>")) {


                                if (instanceInvokeExpr.getArgs().size() == 2) {

                                    if (instanceInvokeExpr.getArg(1).getType().toString().equals("java.lang.Class")) {//ComponentName(Context pkg, Class<?> cls)
                                        TargetComponent targetComponent = doAnalysisOnClassTypeValue(instanceInvokeExpr.getArg(1), useComponentLocalUnit, sootMethod, simpleLocalDefs);
                                        targetComponentAll.updateTargetComponent(targetComponent);
                                    } else {//1. ComponentName(String pkg, String cls)   2. ComponentName(Context pkg, String cls)
                                        TargetComponent targetComponent = computeTwoStringPackageCLassName(sootMethod, simpleLocalDefs, useComponentLocalUnit, instanceInvokeExpr);
                                        targetComponentAll.updateTargetComponent(targetComponent);

                                    }


                                }
                            }
                        }


                    }

                    if (invokeExpr instanceof StaticInvokeExpr && calleeSootMethod.getName().equals("createRelative")) {//	createRelative(String pkg, String cls)
                        //	createRelative(Context pkg, String cls)

                        TargetComponent targetComponent = computeTwoStringPackageCLassName(sootMethod, simpleLocalDefs, useComponentLocalUnit, invokeExpr);
                        targetComponentAll.updateTargetComponent(targetComponent);

                    }


                }

                targetComponentList.add(targetComponentAll);


            }


        }


        TargetComponent targetComponentUl = new TargetComponent();
        for (TargetComponent targetComponent : targetComponentList) {
            targetComponentUl.mixTargetComponent(targetComponent);
        }

        return targetComponentUl;
    }


    private TargetComponent doAnalysisOnStringTypeValue(int valueKind, Value arg, Unit useUnit, SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs) {

        if (arg instanceof StringConstant) {
            StringConstant stringConstant = (StringConstant) arg;
            TargetComponent targetComponent = new TargetComponent(valueKind, stringConstant.value);
            return targetComponent;
        } else if (arg instanceof Local) {
            Local local = (Local) arg;
            TargetComponent targetComponentUl = new TargetComponent();
            for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, useUnit)) {
                DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;
                Value value = definitionStmt.getRightOp();
                TargetComponent targetComponent = doAnalysisOnStringTypeValue(valueKind, value, defUnit, sootMethod, simpleLocalDefs);
                targetComponentUl.mixTargetComponent(targetComponent);
            }

            return targetComponentUl;


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
            TargetComponent targetComponentUl = new TargetComponent();
            for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, useUnit)) {
                DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;
                Value value = definitionStmt.getRightOp();
                TargetComponent targetComponent = doAnalysisOnClassTypeValue(value, defUnit, sootMethod, simpleLocalDefs);
                targetComponentUl.mixTargetComponent(targetComponent);
            }

            return targetComponentUl;


        }

        return null;
    }

    private int doAnalysisIntentIsExplicit(Value value, Unit unit, SootMethod sootMethod) {//只在一个方法里使用


        if (!(value instanceof Local)) {
            throw new RuntimeException("!(value instanceof Local)");
        }
        Local local = (Local) value;
        BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
        SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);

        SimpleLocalUses simpleLocalUses = new SimpleLocalUses(briefUnitGraph, simpleLocalDefs);

        for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, unit)) {
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
                                    return 1;
                                }


                            }
                            for (Value arg : instanceInvokeExpr.getArgs()) {//看是否存在其的一个intent来自其他intent
                                if (arg.getType().toString().contains("android.content.Intent")) {
                                    return doAnalysisIntentIsExplicit(arg, useUnit, sootMethod);

                                }


                            }


                        }

                        if (calleeSootMethod.getName().equals("setClass") || calleeSootMethod.getName().equals("setClassName") || calleeSootMethod.getName().equals("setComponent")) {
                            return 1;
                        }

                    }


                }


            }
        }

        return 0;
    }
}
