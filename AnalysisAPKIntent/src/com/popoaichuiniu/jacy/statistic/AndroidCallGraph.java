package com.popoaichuiniu.jacy.statistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import com.popoaichuiniu.intentGen.ICCEntryPointCreator;
import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.Util;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.xmlpull.v1.XmlPullParserException;

import soot.*;
import soot.jimple.*;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class AndroidCallGraph {


    private org.apache.log4j.Logger appLogger = null;// 应用日志


    public AndroidCallGraph(String appPath, String androidPlatformPath) {

        this.appPath = appPath;
        appLogger = org.apache.log4j.Logger.getLogger(appPath);
        String logfilePath = "app-exceptionLogger/androidCallgraph/" + appPath.replaceAll("/|\\.", "_") + "_callgraph.log";

        File temp = new File(logfilePath);
        if (temp.exists()) {
            temp.delete();
        }


        try {
            appLogger.addAppender(new FileAppender(new PatternLayout("%d %p [%t] %C.%M(%L) | %m%n"), logfilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.androidPlatformPath = androidPlatformPath;

    }

    public CallGraph getCg() {
        return cg;
    }

    public SetupApplication getApp() {
        return app;
    }

    private String appPath = null;
    private String androidPlatformPath = null;

    private CallGraph cg = null;

    private CGExporter cge = new CGExporter();

    private SootMethod entryPoint = null;

    private MySetupApplication app = null;

    public SootMethod getEntryPoint() {
        return entryPoint;
    }

    public void caculateAndroidCallGraph() throws IOException, XmlPullParserException {
        app = new MySetupApplication(androidPlatformPath, appPath);

        if (app == null)
            return;

        // 计算source,sink和入口点,这里修改为添加dummyMain
        long beforeCallgraph = System.nanoTime();
        app.calculateMyEntrypoints("SourcesAndSinks.txt");
        appLogger.info("Callgraph construction took " + (System.nanoTime() - beforeCallgraph) / 1E9 + " seconds");


        constructCallgraph();


    }

    private void constructCallgraph() {


        if (Scene.v().getEntryPoints().size() == 1) {
            entryPoint = Scene.v().getEntryPoints().get(0);
            cg = Scene.v().getCallGraph();

        } else {
            throw new RuntimeException("call graph 构建出现问题！");
        }

        long beforeAddICCMethods = System.nanoTime();
        addICCMethods();
        appLogger.info("AddICCMethods took " + (System.nanoTime() - beforeAddICCMethods) / 1E9 + " seconds");

        //validateAddICC();


        // 可视化函数调用图
        //visit();

        // 导出函数调用图


//		cge.exportMIG("./AnalysisAPKIntent/cg_gexf_output/"+appPath.replaceAll("/|\\.", "_"), ".");
//		System.out.println("导出函数调用图为:" + appPath.replaceAll("/|\\.", "_") + ".gexf");


    }

    private void validateAddICC() {

        Set<SootMethod> sootMethodSet = new HashSet<>();
        for (Iterator<soot.jimple.toolkits.callgraph.Edge> iterator = cg.iterator(); iterator.hasNext(); ) {

            Edge edge = iterator.next();
            if (!edge.src().getDeclaringClass().getName().startsWith("android.")) {
                sootMethodSet.add(edge.src());
            }

            if (!edge.tgt().getDeclaringClass().getName().startsWith("android.")) {
                sootMethodSet.add(edge.tgt());
            }


        }

        for (SootMethod sootMethod : sootMethodSet) {
            if (sootMethod.hasActiveBody()) {
                Body body = sootMethod.getActiveBody();
                int count = 0;
                for (Unit unit : body.getUnits()) {
                    SootMethod invokeSootMethod = Util.getCalleeSootMethodAt(unit);
                    if (invokeSootMethod != null) {
                        if (invokeSootMethod.getBytecodeSignature().contains("iccMain")) {
                            count++;
                        }
                    }

                }

                if (count > 5) {
                    System.out.println(sootMethod);
                }
            }
        }


    }

    class UnitPair {
        Unit point;
        Unit insertUnit;
        String className;

        public UnitPair(Unit point, Unit insertUnit, String className) {
            this.point = point;
            this.insertUnit = insertUnit;
            this.className = className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnitPair unitPair = (UnitPair) o;
            return Objects.equals(point, unitPair.point) &&
                    Objects.equals(insertUnit, unitPair.insertUnit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(point, insertUnit);
        }
    }

    private void addICCMethods() {


        IntentImplicitUse intentImplicitUse = new IntentImplicitUse(appPath, true);
        Map<SootMethod,String> sootMethodColor = new HashMap<>();//存储节点颜色
        Queue<SootMethod> sootMethodQueue = new LinkedList<>();
        sootMethodColor.put(entryPoint,"gray");//节点颜色为null的表示还未发现，为gray表示在队列里，节点为黑色，表示所有孩子都被发现了
        sootMethodQueue.offer(entryPoint);
        int count = 0;
        //不需要使用isChanged, dummymain可以到达所有组件，这些组件包含了app的所有代码

        while (!sootMethodQueue.isEmpty()) {

            SootMethod firstSootMethod = sootMethodQueue.poll();

            if (!firstSootMethod.getDeclaringClass().getName().startsWith("android.")) {//不分析android开头的
                if (Util.isApplicationMethod(firstSootMethod)) {

                    if (firstSootMethod.hasActiveBody()) {
                        Body body = firstSootMethod.getActiveBody();
                        List<UnitPair> insertUnitList = new ArrayList<>();
                        for (Unit eachUnit : body.getUnits()) {
                            Stmt iccStmt = (Stmt) eachUnit;
                            SootMethod calleeSootMethod = Util.getCalleeSootMethodAt(eachUnit);
                            if (calleeSootMethod != null) {
                                if (IntentImplicitUse.iccMethods.contains(calleeSootMethod.getName())) {
                                    InvokeExpr invokeExpr = iccStmt.getInvokeExpr();
                                    for (Value valueIntentArg : invokeExpr.getArgs()) {
                                        if (valueIntentArg.getType().toString().equals("android.content.Intent")) {
                                            count++;
                                            long beforeGetTargetComponent = System.nanoTime();
                                            TargetComponent targetComponent = intentImplicitUse.doAnalysisIntentGetTargetComponent(valueIntentArg, iccStmt, iccStmt, firstSootMethod);
                                            long afterGetTargetComponent = System.nanoTime();
                                            appLogger.info("getGetTargetComponent时间:" + ((afterGetTargetComponent - beforeGetTargetComponent) / 1E9) + " seconds");
                                            if (targetComponent != null && targetComponent.isHasInfo()) {

                                                addTargetComponent(intentImplicitUse, insertUnitList, iccStmt, targetComponent);


                                            }

                                        }
                                    }
                                }
                            }


                        }


                        for (UnitPair unitPair : insertUnitList) {
                            body.getUnits().insertAfter(unitPair.insertUnit, unitPair.point);
                            body.validate();

                            appLogger.info(firstSootMethod + "的" + unitPair.point + "解析成功！" + unitPair.className);
                        }
                    }


                }
                for (Iterator<Edge> edgeIterator = cg.edgesOutOf(firstSootMethod); edgeIterator.hasNext(); ) {
                    Edge outEdge = edgeIterator.next();
                    SootMethod outSootMethod = outEdge.tgt();
                    if (sootMethodColor.get(outSootMethod)==null) {

                        sootMethodColor.put(outSootMethod,"gray");

                        sootMethodQueue.offer(outSootMethod);
                    }

                }

                sootMethodColor.put(firstSootMethod,"black");
            }


        }



        PackManager.v().getPack("wjpp").apply();//预处理，参考setupApplication
        PackManager.v().getPack("cg").apply();//重新构建

        cg = Scene.v().getCallGraph();//需要重新赋值

        appLogger.info(appPath + "icc method ulCount" + count);

    }

    private boolean addTargetComponent(IntentImplicitUse intentImplicitUse, List<UnitPair> insertUnitList, Unit iccUnit, TargetComponent targetComponent) {
        if (targetComponent.type == TargetComponent.HYBIRD) {

            if (addTargetFromComponentName(insertUnitList, iccUnit, targetComponent.hybirdValues[TargetComponent.COMPONENT_NAME])) {
                return true;
            }


            return addTargetFromAction(intentImplicitUse, insertUnitList, iccUnit, targetComponent.hybirdValues[TargetComponent.ACTION]);

        }


        if (targetComponent.type == TargetComponent.COMPONENT_NAME) {
            if (addTargetFromComponentName(insertUnitList, iccUnit, targetComponent.hybirdValues[TargetComponent.COMPONENT_NAME])) {
                return true;
            }
        }

        if (targetComponent.type == TargetComponent.ACTION) {
            return addTargetFromAction(intentImplicitUse, insertUnitList, iccUnit, targetComponent.hybirdValues[TargetComponent.ACTION]);
        }

        return false;
    }

    private boolean addTargetFromAction(IntentImplicitUse intentImplicitUse, List<UnitPair> insertUnitList, Unit iccUnit, String actionStr) {
        if (actionStr != null) {
            String actionValues[] = actionStr.split("#");
            Set<String> actionSet = new HashSet<>();
            for (String action : actionValues) {
                actionSet.add(action);

            }

            if (actionSet.size() > 0) {
                if (actionSet.size() > 1) {
                    appLogger.warn("多个action" + Util.getPrintCollectionStr(actionSet));
                }

                for (String oneAction : actionSet) {
                    Set<String> componentSet = intentImplicitUse.actionComponentName.get(oneAction);
                    if (componentSet != null && componentSet.size() > 0) {
                        if (componentSet.size() > 1) {
                            appLogger.warn("action对应多个组件" + oneAction);
                        }

                        for (String componentNameStr : componentSet)//默认选第一个
                        {
                            if (addTargetFromComponentName(insertUnitList, iccUnit, componentNameStr)) {
                                return true;
                            }
                        }


                    }

                }
            }


        }

        return false;

    }

    private boolean addTargetFromComponentName(List<UnitPair> insertUnitList, Unit iccUnit, String componentNameStr) {

        if (componentNameStr != null) {
            String[] componentName = componentNameStr.split("#");
            Set<String> componentNameSet = new HashSet<>();
            for (String one : componentName) {
                componentNameSet.add(one.replaceAll("/", "."));
            }

            if (componentNameSet.size() > 0) {
                if (componentNameSet.size() > 1) {
                    appLogger.warn("多个targetComponent" + Util.getPrintCollectionStr(componentNameSet));
                }
                for (String className : componentNameSet) {
                    boolean flag = addMethodEdge(insertUnitList, iccUnit, className);
                    if (flag) {//默认选第一个
                        return true;
                    }
                }


            }


        }
        return false;
    }

    private Set<SootMethod> createICCMethodSet = new HashSet<>();

    private boolean addMethodEdge(List<UnitPair> insertUnitList, Unit iccUnit, String className) {

        SootClass sootClass = SootResolver.v().resolveClass(className, SootClass.BODIES);
        if (sootClass == null) {
            appLogger.warn("加载组件类出错！" + className);
        } else {
            //添加边


            ICCEntryPointCreator iccEntryPointCreator = new ICCEntryPointCreator(Collections.singletonList(className), className.replaceAll("\\.", ""));
            SootMethod iccDummyMethod = iccEntryPointCreator.createDummyMain();
            createICCMethodSet.add(iccDummyMethod);
            List<Value> args = new ArrayList<>();
            args.add(NullConstant.v());
            InvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(iccDummyMethod.makeRef(), args);
            Stmt staticInvokeStmt = Jimple.v().newInvokeStmt(staticInvokeExpr);
            UnitPair insertUnitPair = new UnitPair(iccUnit, staticInvokeStmt, className);
            insertUnitList.add(insertUnitPair);
            return true;


        }

        return false;
    }

//	private void initializeSoot() {
//
//		soot.G.reset();// 标准的soot操作，清空soot之前所有操作遗留下的缓存值
//
//		Options.v().set_src_prec(Options.src_prec_apk);// 设置优先处理的文件格式
//
//		Options.v().set_process_dir(Collections.singletonList(appPath));// 处理文件夹中所有的class
//		// singletonList(T) 方法用于返回一个只包含指定对象的不可变列表
//
//		Options.v().set_android_jars(androidPlatformPath);// 在该路径下找android.jar
//
//		Options.v().set_app(true);//
//
//		List<String> excludeList = new LinkedList<String>();
//		excludeList.add("java.*");
//		excludeList.add("sun.misc.*");
//		excludeList.add("android.*");
//		excludeList.add("org.apache.*");
//		excludeList.add("soot.*");
//		excludeList.add("javax.servlet.*");
//		Options.v().set_exclude(excludeList);
//		Options.v().set_no_bodies_for_excluded(true);//去除的类不加载body
//
//		Options.v().set_whole_program(true);// 以全局模式运行，这个默认是关闭的，否则不能构建cg(cg是全局的pack)
//
//		Options.v().set_allow_phantom_refs(true);// 允许未被解析的类，可能导致错误
//
//		//Phantom classes are classes that are neither in the process directory nor on
////		the Soot classpath, but that are referenced by some class / method body that
////		Soot loads. If phantom classes are enabled, Soot will not just abort and
////		fail on such an unresolvable reference, but create an empty stub called a
////		phantom class which in turn contains phanom methods to make up for the
////		missing bits and pieces.
//
//
//		//设置cg pack的选项
//
//		Options.v().setPhaseOption("cg.cha", "on");//不用设置的默认就为true
//
//		Options.v().setPhaseOption("cg.cha", "verbose:true");
//
//		Options.v().setPhaseOption("cg.cha", "apponly:true");
//
//		Options.v().setPhaseOption("cg.spark", "off");// 构建cg的选项，spark是一个指向性分析框架 这个打开的会可能会消除一些 节点
//
//		Scene.v().loadNecessaryClasses();// 加载soot需要的classes，包括命令行指定的
//										// 使用soot反编译dex文件，并将反编译后的文件加载到内存中
//
//	}

    // 可视化函数调用图的函数
    private void visit() {

        HashSet<String> visited = new HashSet<String>();
        BufferedWriter cgOutputWriter = null;//cg

        try {

            cgOutputWriter = new BufferedWriter(new FileWriter(new File("./AnalysisAPKIntent/CallGraphOutput/" + appPath.replaceAll("/|\\.", "_") + "_cfg" + ".txt")));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            appLogger.error(appPath, e);
        }

        // *******************************************************//
        System.out.println("*******CallGraph*******" + cg.size());
        for (Iterator<soot.jimple.toolkits.callgraph.Edge> iterator = cg.iterator(); iterator.hasNext(); ) {
            soot.jimple.toolkits.callgraph.Edge edge = iterator.next();
            System.out.println(
                    edge.getSrc().method().getBytecodeSignature() + "——>" + edge.getTgt().method().getBytecodeSignature());
            try {
                cgOutputWriter.write(edge.getSrc().method().getBytecodeSignature() + "——>" + edge.getTgt().method().getSignature() + "\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                appLogger.error(appPath, e);
            }
            if (!visited.contains(edge.getSrc().method().getSignature())) {
                cge.createNode(edge.getSrc().method().getSignature());
                visited.add(edge.getSrc().method().getSignature());
            }
            if (!visited.contains(edge.getTgt().method().getSignature())) {
                cge.createNode(edge.getTgt().method().getSignature());
                visited.add(edge.getTgt().method().getSignature());
            }
            cge.linkNodeByID(edge.getSrc().method().getSignature(), edge.getTgt().method().getSignature());
            System.out.println("**************");


        }
        try {
            cgOutputWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            appLogger.error(appPath, e);
        }

        // *******************************************************//
    }
}
