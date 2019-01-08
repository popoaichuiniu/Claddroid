package com.popoaichuiniu.intentGen;

import com.popoaichuiniu.jacy.statistic.AndroidCallGraph;
import com.popoaichuiniu.jacy.statistic.AndroidCallGraphProxy;
import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.*;
import java.util.*;

public class GenerateDataFlowInUnitNeedToAnalysis {//未实现


    private static boolean isTest = Config.isTest;

    private static boolean isJustThinkDangerous = false;//是否只考虑危险API

    private static Logger logger=new MyLogger(Config.unitNeedAnalysisGenerate,"GenerateDataFlowInUnitNeedToAnalysis").getLogger();


    static Set<String> dangerousPermissions = null;
    static Map<String, Set<String>> apiPermissionMap = AndroidInfo.getPermissionAndroguardMethods();

    private static void generateUnitToAnalysis(List<SootMethod> ea_entryPoints, CallGraph cg, String appPath) throws IOException {


        Hierarchy h = Scene.v().getActiveHierarchy();


        List<SootMethod> roMethods = Util.getMethodsInReverseTopologicalOrder(ea_entryPoints, cg);


        BufferedWriter bufferedWriterUnitsNeedAnalysis = null;


        bufferedWriterUnitsNeedAnalysis = new BufferedWriter(new FileWriter(appPath + "_" + "UnitsNeedAnalysis.txt"));


        for (SootMethod sootMethod : roMethods) {

            List<Unit> unitsNeedToAnalysis = new ArrayList<>();

            Body body = sootMethod.getActiveBody();
            if (body != null) {
                PatchingChain<Unit> units = body.getUnits();
                for (Unit unit : units) {

                    SootMethod calleeSootMethod = Util.getCalleeSootMethodAt(unit);
                    if (calleeSootMethod == null) {
                        continue;
                    }
                    Set<String> permissionSet = apiPermissionMap.get(calleeSootMethod.getBytecodeSignature());
                    if (permissionSet != null && isExistSimilarItem(permissionSet, dangerousPermissions)) {
                        unitsNeedToAnalysis.add(unit);

                    }


                }

                for (Unit unit : unitsNeedToAnalysis) {

                    Stmt stmt = (Stmt) unit;
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    if (invokeExpr == null) {
                        throw new RuntimeException("illegal unitNeedAnalysis"+unit.toString());
                    } else {
                        bufferedWriterUnitsNeedAnalysis.write(sootMethod.getBytecodeSignature() + "#" + unit.getTag("BytecodeOffsetTag") + "#" + unit.toString() + "#" + invokeExpr.getMethod().getBytecodeSignature() + "\n");
                    }


                }

            }
        }

        bufferedWriterUnitsNeedAnalysis.close();


    }

    private static boolean isExistSimilarItem(Set<String> permissionSet, Set<String> dangerousPermissions) {
        if (isJustThinkDangerous) {
            for (String permission : permissionSet) {
                if (dangerousPermissions.contains(permission)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }

    }

    public static void main(String[] args) {

        //dangerousPermissions是考虑的自己设定的需要考虑的权限提升
        dangerousPermissions = new ReadFileOrInputStream("AnalysisAPKIntent"+"/"+"think_dangerousPermission.txt",logger).getAllContentLinSet();
        for (Iterator<String> dangerousPermissionsIterator = dangerousPermissions.iterator(); ((Iterator) dangerousPermissionsIterator).hasNext(); ) {
            String dangerousPermission = dangerousPermissionsIterator.next();
            if (dangerousPermission.startsWith("#")) {
                dangerousPermissionsIterator.remove();//
            }
        }
        //String appDirPath="/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/sootOutput";

        //String appDirPath=Config.wandoijiaAPP;
        String appDirPath = null;
        if (isTest) {
            appDirPath = Config.testAppPath;
        } else {

            appDirPath = Config.defaultAppDirPath;

        }


        File appDir = new File(appDirPath);




        if (appDir.isDirectory()) {


            File hasGeneratedAPPFile = new File(Config.unitNeedAnalysisGenerate+"/"+appDir.getName() + "_hasGeneratedAPP.txt");
            if (!hasGeneratedAPPFile.exists()) {
                try {
                    hasGeneratedAPPFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            Set<String> hasGenerateAppSet = new ReadFileOrInputStream(Config.unitNeedAnalysisGenerate+"/"+appDir.getName() + "_hasGeneratedAPP.txt",logger).getAllContentLinSet();
            WriteFile writeFileHasGenerateUnitNeedAnalysis = new WriteFile(Config.unitNeedAnalysisGenerate+"/"+appDir.getName() + "_hasGeneratedAPP.txt", true,logger);//分析一个目录中途断掉，可以继续重新分析



            for (File apkFile : appDir.listFiles()) {
                if (apkFile.getName().endsWith(".apk") && (!apkFile.getName().contains("_signed_zipalign"))) {
                    if (hasGenerateAppSet.contains(apkFile.getAbsolutePath())) {
                        continue;
                    }


                    Thread childThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                singleAPPAnalysis(apkFile.getAbsolutePath());//分析每一个app

                            } catch (Exception e) {
                                logger.error(apkFile.getAbsolutePath() + "&&" + "Exception" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e));
                            }


                        }
                    });

                    childThread.start();

                    try {
                        childThread.join();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    writeFileHasGenerateUnitNeedAnalysis.writeStr(apkFile.getAbsolutePath() + "\n");//分析完整成功才写进去。
                    writeFileHasGenerateUnitNeedAnalysis.flush();


                }

            }


            writeFileHasGenerateUnitNeedAnalysis.close();

        } else {

            try {
                singleAPPAnalysis(appDirPath);
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

        }


    }

    public static void singleAPPAnalysis(String appPath) throws IOException{


        AndroidInfo androidInfo = new AndroidInfo(appPath,logger);


        AndroidCallGraph androidCallGraph = new AndroidCallGraphProxy(appPath, Config.androidJar,logger).androidCallGraph;

        CallGraph cGraph=androidCallGraph.getCg();


        List<SootMethod> ea_entryPoints = Util.getEA_EntryPoints(androidCallGraph,androidInfo);


        generateUnitToAnalysis(ea_entryPoints, cGraph, appPath);
    }
}
