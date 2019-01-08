package com.popoaichuiniu.intentGen;

import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.infoflow.android.axml.AXmlNode;

import java.io.*;
import java.util.*;

public class IntentConditionTransformOnlyEA
        extends SceneTransformer {




    private String appPath=null;

    private String platforms=null;

    private static Logger logger=new MyLogger("AnalysisAPKIntent/IntentConditionTransformOnlyEA","exceptionLogger").getLogger();

    public IntentConditionTransformOnlyEA
            (String apkFilePath,String platforms) {
        this.appPath = apkFilePath;
        this.platforms = platforms;


    }

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {


        //AndroidCallGraph androidCallGraphHelper = new AndroidCallGraph(testAppPath,platforms);
        AndroidInfo androidInfo = new AndroidInfo(appPath,logger);
        String packageName= androidInfo.getPackageName(appPath);

        Map<String,AXmlNode> eas= androidInfo.getEAs();


        System.out.println("(((((((((((((((((((((((((((((");
        System.out.println(appPath);


        List<IntentInfo> intentInfoList=new ArrayList<>();
        for(Map.Entry<String,AXmlNode> eaComponent:eas.entrySet())
        {
            System.out.println(eaComponent.getKey());
            IntentInfo intentInfo=new IntentInfo();
            intentInfo.appPath=appPath;
            intentInfo.appPackageName=packageName;
            intentInfo.comPonentName=eaComponent.getKey();
            intentInfo.comPonentType=eaComponent.getValue().getTag();
            System.out.println(intentInfo.comPonentType);
            intentInfoList.add(intentInfo);

        }

        IntentInfoFileGenerate.generateIntentInfoFile(appPath,intentInfoList);
        System.out.println("))))))))))))))))))))))))))))))");





    }



    public void run() {
        Config.setSootOptions(appPath);
        PackManager.v().getPack("wjtp")
                .add(new Transform("wjtp.intentGenOnlyEA", this));

        PackManager.v().getPack("wjtp").apply();
    }


    public static void main(String[] args) {
        String APKDir="/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/sootOutput";

        for(File apkFile:new File(APKDir).listFiles())
        {

            if(apkFile.getName().endsWith("_signed_zipalign.apk"))
            {
                IntentConditionTransformOnlyEA
                        intentConditionTransform = new IntentConditionTransformOnlyEA
                        (apkFile.getAbsolutePath(),Config.androidJar);
                intentConditionTransform.run();
            }

        }




    }


}
