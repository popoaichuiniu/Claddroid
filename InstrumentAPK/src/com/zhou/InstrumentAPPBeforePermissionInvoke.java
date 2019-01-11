package com.zhou;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import org.javatuples.Pair;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.tagkit.BytecodeOffsetTag;


class InstrumentUnit {
    Body body = null;
    Unit point = null;
    String message = null;

    public InstrumentUnit(Body body, Unit unit, String appPath) {
        super();
        this.body = body;
        this.point = unit;
        this.message = appPath;
    }


}

public class InstrumentAPPBeforePermissionInvoke extends BodyTransformer {


    private static boolean isTest = Config.isTest;

    private static Map<String, Set<String>> apiPermissionMap = AndroidInfo.getPermissionAndroguardMethods();

    private static Logger exceptionLogger= new MyLogger(Config.instrument_logDir,"exception").getLogger();

    private static Logger infoLogger=new MyLogger(Config.instrument_logDir,"InstrumentInfo").getLogger();

    @Override
    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {

        //System.out.println("xxxxxx");
        if (Util.isLibraryClass(b.getMethod().getBytecodeSignature())) {
            return;
        }

        // TODO Auto-generated method stub
//		System.out.println("************************************************");
//		System.out.println("phaseName=" + phaseName);
//
//
//		options.forEach((key, value)->{
//			System.out.println("key="+key+"--value="+value);
//		});


        PatchingChain<Unit> units = b.getUnits();


        List<InstrumentUnit> instrumentUnits = new ArrayList<InstrumentUnit>();
        for (Unit unit : units) {


//			for(Tag tag:unit.getTags())
//			{
//				System.out.println(tag.getName()+"zzzzzzzzzzz");
//			}


            if (unitNeedAnalysis(unit, b.getMethod())) {
                instrumentUnits.add(new InstrumentUnit(b, unit, appPath));
            }


        }

        try {

            for (InstrumentUnit instrumentUnit : instrumentUnits) {
                addInstrumentBeforeStatement(instrumentUnit.body, instrumentUnit.point, instrumentUnit.message);
            }

        } catch (RuntimeException e) {
            writeFileAppInstrumentException.writeStr(e.getMessage() + " " + appPath + "\n");
            writeFileAppInstrumentException.flush();
        }


    }

    /**
     * appPath是应用的绝对路径
     * platforms是SDK中platforms的路径
     */
    //static String testAppPath = "/home/lab418/AndroidStudioProjects/TestIntrument3/app/build/outputs/apk/debug/app-debug.apk";
    private String appPath = null;
    //private String platforms = "/home/zms/platforms";//设置最低版本为android5.0，app就插桩失败  签名的jarsigner不行了？soot太老了？

    private Set<Pair<Integer, String>> targets = null;

    private static BufferedWriter bufferedWriter_instrument_content = null;
    private static BufferedWriter bufferedWriter_app_has_Instrumented = null;

    private static WriteFile writeFileAppInstrumentException = null;

    private static volatile int inStrumentCount = 0;

    private static int targetCount = 0;


    public InstrumentAPPBeforePermissionInvoke(String appPath, String targetsFile) {
        this.appPath = appPath;

        targets = new LinkedHashSet<Pair<Integer, String>>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(targetsFile));

            String content = null;
            while ((content = bufferedReader.readLine()) != null) {

                String[] str = content.split("#");
                String methodString = str[0];
                String byteTag = str[1];

                targets.add(new Pair<>(Integer.valueOf(byteTag), methodString));

            }

            targetCount = targets.size();
            inStrumentCount = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private boolean unitNeedAnalysis(Unit unit, SootMethod sootMethod) {
        BytecodeOffsetTag tag = Util.extractByteCodeOffset(unit);
        if (tag == null) {
            return false;
        }

        for (Pair<Integer, String> item : targets) {
            if (item.getValue0() == tag.getBytecodeOffset() && item.getValue1().equals(sootMethod.getBytecodeSignature())) {
                return true;
            }

        }
        return false;
    }

    public static void main(String[] args) {

        String[] instrumentArgs = new String[3];


        String appDir = null;
        if (isTest) {
            appDir = Config.testAppPath;
        } else {
            appDir = Config.defaultAppDirPath;
        }


        File appDirFile = new File(appDir);

        if (appDirFile.isDirectory()) {
            File logFileDir = new File("InstrumentAPK/instrument_log/" + appDirFile.getName());
            if (!logFileDir.exists()) {
                logFileDir.mkdir();

            }
            if (logFileDir.exists()) {
                writeFileAppInstrumentException = new WriteFile("InstrumentAPK/instrument_log/" + appDirFile.getName() + "/" + "appInstrumentException.log", true,exceptionLogger);

            } else {
                throw new RuntimeException("创建logFileDir失败！");
            }
        } else {
            writeFileAppInstrumentException = new WriteFile("InstrumentAPK/instrument_log/" + appDirFile.getName() + "_instrumentException.log", true,exceptionLogger);
        }


        Set<String> hasInstrumentAPP = new ReadFileOrInputStream("InstrumentAPK/instrument_log/app_has_Instrumented.txt",exceptionLogger).getAllContentLinSet();

        if (hasInstrumentAPP == null) {
            throw new RuntimeException("读取app_has_Instrumented.txt失败！");
        }

        try {
            bufferedWriter_app_has_Instrumented = new BufferedWriter(new FileWriter("InstrumentAPK/instrument_log/app_has_Instrumented.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (appDirFile.isDirectory()) {
            for (File file : appDirFile.listFiles()) {
                if (file.getName().endsWith(".apk")) {
                    if (hasInstrumentAPP.contains(file.getAbsolutePath())) {
                        continue;
                    }
                    File unitedAnalysis = new File(file.getAbsolutePath() + "_UnitsNeedAnalysis.txt");
                    if (unitedAnalysis.exists()) {

                        Thread childThread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub

                                Long startTime = System.nanoTime();
                                instrumentArgs[0] = file.getAbsolutePath();
                                instrumentArgs[1] = "/home/zms/platforms";
                                instrumentArgs[2] = unitedAnalysis.getAbsolutePath();

                                soot.G.reset();
                                try {
                                    singleAPPAnalysis(instrumentArgs);
                                } catch (RuntimeException e) {
                                    writeFileAppInstrumentException.writeStr(e.getMessage() + " " + instrumentArgs[0] + "\n");
                                    writeFileAppInstrumentException.flush();
                                }

                                try {
                                    bufferedWriter_app_has_Instrumented.write(instrumentArgs[0] + "\n");
                                    bufferedWriter_app_has_Instrumented.flush();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }


                                Long stopTime = System.nanoTime();
                                System.out.println("运行时间:" + ((stopTime - startTime) / 1000 / 1000 / 1000 / 60) + "分钟");
                            }
                        });
                        childThread.start();

                        try {
                            childThread.join();
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }


                    }


                }
            }

            writeFileAppInstrumentException.close();
            try {

                bufferedWriter_app_has_Instrumented.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File unitedAnalysis = new File(appDirFile.getAbsolutePath() + "_UnitsNeedAnalysis.txt");
            instrumentArgs[0] = appDirFile.getAbsolutePath();
            instrumentArgs[1] = "/home/zms/platforms";
            instrumentArgs[2] = unitedAnalysis.getAbsolutePath();

            soot.G.reset();
            singleAPPAnalysis(instrumentArgs);
        }


    }

    private static void singleAPPAnalysis(String[] args) {

        infoLogger.info(args[0]+" startInstrument");
        String outputDir = new File(args[0]).getParentFile().getAbsolutePath() + "/" + "instrumented";
        File outputDirFile = new File(outputDir);

        if (!outputDirFile.exists()) {
            outputDirFile.mkdir();

        }
        if (!outputDirFile.exists()) {
            throw new RuntimeException("instrumented目录创建失败！");
        }
        String sootArgs[] = {
                "-process-dir", args[0],
                "-android-jars", args[1],
                "-allow-phantom-refs",
                "-output-dir", outputDir,


        };


        //prefer Android APK files// -src-prec apk
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_keep_line_number(true);
        Options.v().set_coffi(true);
        Options.v().set_keep_offset(true);

        //output as APK, too//-f J
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().set_force_overwrite(true);

        Scene.v().addBasicClass("android.util.Log", SootClass.SIGNATURES);


        try {
            bufferedWriter_instrument_content = new BufferedWriter(new FileWriter(args[0] + "_instrument.log"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        InstrumentAPPBeforePermissionInvoke instrumentAPPBeforePermissionInvoke = new InstrumentAPPBeforePermissionInvoke(args[0], args[2]);

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", instrumentAPPBeforePermissionInvoke));


        soot.Main.main(sootArgs);
        try {
            bufferedWriter_instrument_content.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        if (targetCount != inStrumentCount) {

            exceptionLogger.error(args[0]+"##"+"插桩数量和需要插桩的数量不匹配！");
            //throw new RuntimeException("插桩数量和需要插桩的数量不匹配！");
        }

        infoLogger.info(args[0]+" instrument over!");

    }

    public void addInstrumentAfterStatement(Body b, Unit point, String message) {


        //insert a log.i instrument statement

        Scene.v().forceResolve("android.util.Log", SootClass.SIGNATURES);

        SootMethod log = Scene.v().getMethod("<android.util.Log: int i(java.lang.String,java.lang.String)>");
        Value logMessage = StringConstant.v("#Instrument#" + message +
                "#method#" + b.getMethod().getSignature() + "#unitPoint_after#" + point.toString() + "#lineNumber#" + point.getJavaSourceStartLineNumber());
        Value logType = StringConstant.v("ZMSInstrument");
        Value logMsg = logMessage;
        //make new static invokement
        StaticInvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(log.makeRef(), logType, logMsg);
        // turn it into an invoke statement

        InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(newInvokeExpr);

        b.getUnits().insertAfter(invokeStmt, point);
        //check that we did not mess up the Jimple
        b.validate();
        inStrumentCount = inStrumentCount + 1;
        try {
            bufferedWriter_instrument_content.write(logMessage.toString() + "\n");
            bufferedWriter_instrument_content.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void addInstrumentBeforeStatement(Body b, Unit point, String message) {


        //insert a log.i instrument statement

        Scene.v().forceResolve("android.util.Log", SootClass.SIGNATURES);

        SootMethod log = Scene.v().getMethod("<android.util.Log: int i(java.lang.String,java.lang.String)>");
        String permission =getPermissionString(point);
        Value logMessage = StringConstant.v("#Instrument#" + message +
                "#method#" + b.getMethod().getSignature() + "#unitPoint_before#" + point.toString() + "#lineNumber#" + point.getJavaSourceStartLineNumber() + "#unitPoint_permission#" + permission);
        Value logType = StringConstant.v("ZMSInstrument");
        Value logMsg = logMessage;
        //make new static invokement
        StaticInvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(log.makeRef(), logType, logMsg);
        // turn it into an invoke statement

        InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(newInvokeExpr);

        b.getUnits().insertBefore(invokeStmt, point);
        //check that we did not mess up the Jimple
        b.validate();
        infoLogger.info("insert before "+point);
        inStrumentCount = inStrumentCount + 1;
        try {
            bufferedWriter_instrument_content.write(logMessage.toString() + "\n");
            bufferedWriter_instrument_content.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private String getPermissionString(Unit point) {

        if(point==null)
        {
            throw new RuntimeException("point unit is null");
        }
        else
        {
            Stmt stmt= (Stmt) point;
            InvokeExpr invokeExpr=stmt.getInvokeExpr();
            if(invokeExpr==null)
            {
                throw new RuntimeException("point unit don't have invokeExpr");
            }
            else
            {
                Set<String> permissionSet=apiPermissionMap.get(invokeExpr.getMethod().getBytecodeSignature());

                if(permissionSet==null)
                {
                    throw new RuntimeException("point unit isn't protected by permission");
                }
                else
                {
                    String allPermission="";
                    for(String permission:permissionSet)
                    {
                        allPermission=allPermission+","+permission;
                    }

                    return allPermission;

                }
            }
        }
    }

}
