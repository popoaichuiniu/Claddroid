package com.popoaichuiniu.intentGen;

import com.popoaichuiniu.jacy.statistic.AndroidCallGraph;
import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.Util;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.io.File;
import java.util.*;

import java.util.regex.Pattern;

public class UseIPCCheckPermssion extends SceneTransformer {

    private String appPath = null;

    private String platforms = null;

    private static int EA_count = 0;

    private static int permissionEA_count = 0;

    private static int checkPermissionEA_count = 0;

    private static int allPermissionEA_count = 0;

    private static Logger logger=new MyLogger("AnalysisAPKIntent/UseIPCCheckPermission","exceptionLogger").getLogger();

    private static WriteFile writeFile=new WriteFile("AnalysisAPKIntent/checkPermissionCount/permissionCheckSituation.txt",false,logger);

    public UseIPCCheckPermssion
            (String apkFilePath, String platforms) {
        this.appPath = apkFilePath;
        this.platforms = platforms;


    }

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {


        AndroidInfo androidInfo = new AndroidInfo(appPath,logger);
        List<String> string_EAs = androidInfo.getString_EAs();


        AndroidCallGraph androidCallGraph = new AndroidCallGraph(appPath, platforms);

        CallGraph cGraph = androidCallGraph.getCg();

        SootMethod entryPoint = androidCallGraph.getEntryPoint();


        Map<String, Set<SootMethod>> eaSootMethodsSetMap = new HashMap<>();//ea --> ea entryPoints methods

        for (Iterator<Edge> iterator = cGraph.edgesOutOf(entryPoint); iterator.hasNext(); )// DummyMain方法调用的所有方法（各个组件的回调方法和生命周期）
        {
            Edge edge = iterator.next();
            SootMethod method = edge.getTgt().method();


            if (string_EAs.contains(method.getDeclaringClass().getName()))// 这个方法是不是属于EA的方法
            {

                Set<SootMethod> sootMethodsSet = eaSootMethodsSetMap.get(method.getDeclaringClass().getName());

                if (sootMethodsSet == null) {
                    sootMethodsSet = new HashSet<>();

                }

                sootMethodsSet.add(method);

                eaSootMethodsSetMap.put(method.getDeclaringClass().getName(), sootMethodsSet);
            }


        }


        Hierarchy h = Scene.v().getActiveHierarchy();

        Pattern pattern = Pattern.compile("checkCallingOrSelfPermission|checkCallingOrSelfUriPermission|checkCallingPermission|checkCallingUriPermission|enforceCallingOrSelfPermission|enforceCallingOrSelfUriPermission|enforceCallingPermission|enforceCallingUriPermission");


        Set<String> eaHasCheckIPCPermission = new HashSet<>();
        for (Map.Entry<String, Set<SootMethod>> entry : eaSootMethodsSetMap.entrySet()) {

            List<SootMethod> ea_entryPoints = new ArrayList<>(entry.getValue());
            List<SootMethod> roMethods = Util.getMethodsInReverseTopologicalOrder(ea_entryPoints, androidCallGraph.getCg());//get all method we can reach from these ea entrypoints

            oneEA:
            for (SootMethod sootMethod : roMethods) {
                for (Unit unit : sootMethod.getActiveBody().getUnits()) {
                    InvokeExpr invokeExpr = Util.getInvokeOfUnit(unit);

                    if (invokeExpr != null) {
                        SootMethod invokeMethod = invokeExpr.getMethod();

                        if (isContextChildClass(invokeMethod, h)) {
                            String name = invokeMethod.getName();

                            if (pattern.matcher(name).matches())//is checkPermission?
                            {
                                eaHasCheckIPCPermission.add(entry.getKey());
                                break oneEA;
                            }
                        }


                    }
                }
            }

        }




        Map<String, Map<AXmlNode, String>> permission_protectedEA = androidInfo.getPermissionProtectedEAs();


        System.out.println(((float) permission_protectedEA.size()) / string_EAs.size());


        System.out.println(((float) eaHasCheckIPCPermission.size()) / string_EAs.size());


        Set<String> allPermissionEA = new HashSet<>();

        allPermissionEA.addAll(eaHasCheckIPCPermission);

        allPermissionEA.addAll(permission_protectedEA.keySet());


        System.out.println(((float) allPermissionEA.size()) / string_EAs.size());


        EA_count = EA_count + string_EAs.size();

        permissionEA_count = permissionEA_count + permission_protectedEA.keySet().size();

        checkPermissionEA_count = checkPermissionEA_count + eaHasCheckIPCPermission.size();

        allPermissionEA_count = allPermissionEA_count+allPermissionEA.size();


        writeFile.writeStr("1111111111111111111111111111111111111111111111"+"\n");
        writeFile.writeStr(permissionEA_count +"&&&&&&&"+checkPermissionEA_count+"&&&&&&&&"+allPermissionEA_count+"#######"+EA_count+"\n");

        writeFile.writeStr(((float) permission_protectedEA.size()) / string_EAs.size()+"\n");

        writeFile.writeStr(((float) eaHasCheckIPCPermission.size()) / string_EAs.size()+"\n");

        writeFile.writeStr(((float) allPermissionEA.size()) / string_EAs.size()+"\n");

        writeFile.writeStr("222222222222222222222222222222222222222222222222"+"\n");
        writeFile.flush();


    }


    public boolean isContextChildClass(SootMethod sootMethod, Hierarchy h) {


        if(!sootMethod.getDeclaringClass().isInterface())

        {
            List<SootClass> superClasses = h.getSuperclassesOfIncluding(sootMethod.getDeclaringClass());


            for (SootClass sootClass : superClasses) {
                if (sootClass.getName().equals("android.content.Context")) {
                    return true;
                }
            }
        }
        else

        {
            List<SootClass> superClasses = h.getSuperinterfacesOfIncluding(sootMethod.getDeclaringClass());


            for (SootClass sootClass : superClasses) {
                if (sootClass.getName().equals("android.content.Context")) {
                    return true;
                }
            }
        }



        return false;

    }


    public void run() {
        Config.setSootOptions(appPath);
        PackManager.v().getPack("wjtp")
                .add(new Transform("wjtp.useIPCCheckPermission", this));

        PackManager.v().getPack("wjtp").apply();
    }


    public static void main(String[] args) {
        String APKDir = Config.wandoijiaAPP;


        for (File apkFile : new File(APKDir).listFiles()) {


            if (apkFile.getName().endsWith(".apk")) {


                Thread childThread = new Thread(new Runnable() {
                    @Override
                    public void run() {


                        UseIPCCheckPermssion
                                useIPCCheckPermssion = new UseIPCCheckPermssion
                                (apkFile.getAbsolutePath(), Config.androidJar);
                        useIPCCheckPermssion.run();

                    }
                });


                childThread.start();

                try {
                    childThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                System.out.println(apkFile.getAbsolutePath());

            }

        }



        System.out.println("permissionEA:");
        System.out.println(((float)permissionEA_count)/EA_count);
        System.out.println("checkPermissionEA:");
        System.out.println(((float)checkPermissionEA_count)/EA_count);
        System.out.println("allPermissionEA:");
        System.out.println(((float)allPermissionEA_count)/EA_count);

        writeFile.writeStr("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");


        writeFile.writeStr(((float)permissionEA_count)/EA_count+"\n");

        writeFile.writeStr(((float)checkPermissionEA_count)/EA_count+"\n");

        writeFile.writeStr(((float)allPermissionEA_count)/EA_count+"\n");



        writeFile.writeStr("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");


        writeFile.close();
    }


}
