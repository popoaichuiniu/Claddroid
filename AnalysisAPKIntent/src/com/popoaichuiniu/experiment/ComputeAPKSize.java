package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Set;

public class ComputeAPKSize {
    private static Logger logger=new MyLogger("AnalysisAPKIntent/ComputeAPKSize","exceptionLogger").getLogger();

    public static void main(String[] args) {

        String appTxt="/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/testAPP/successTest_apk_list";

        ReadFileOrInputStream readFileOrInputStream=new ReadFileOrInputStream(appTxt,logger);

        long minSize=Long.MAX_VALUE;

        String minAPP="";
        long maxSize=Long.MIN_VALUE;

        String maxAPP="";

        long allSize=0;
        Set<String> contentSet=readFileOrInputStream.getAllContentLinSet();

        for(String app:contentSet)
        {
            File apkFile=new File(app);

            long fileSize=apkFile.length();

            allSize=allSize+fileSize;

            if(fileSize<minSize)
            {
                minSize=fileSize;
                minAPP=app;
            }

            if(fileSize>maxSize)

            {
                maxSize=fileSize;
                maxAPP=app;
            }
        }

        System.out.println("minSize:"+minSize+" "+minAPP);
        System.out.println("maxSize:"+maxSize+" "+maxAPP);
        System.out.println("allSize:"+((double)allSize)/contentSet.size());


    }
}
