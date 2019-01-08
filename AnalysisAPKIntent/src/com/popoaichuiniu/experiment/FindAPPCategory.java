package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FindAPPCategory {

    private static  String  apkCategoryDir="/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/apks_wandoujia/apks";

    private static Logger logger=new MyLogger("AnalysisAPKIntent/FindAPPCategory","exceptionLogger").getLogger();

    public static void main(String[] args) {

        String appDir="/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/testAPP/successTest_apk_list";

        ReadFileOrInputStream readFileOrInputStream=new ReadFileOrInputStream(appDir,logger);

        Set<String> contentSet=readFileOrInputStream.getAllContentLinSet();

        Map<String,String> appType=new HashMap<>();

        for(String app:contentSet)
        {
            File file=new File(app);

            ///media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/apks_wandoujia/apks/all_app/instrumented/HIIT间歇训练_signed_zipalign.apk
            String category= findAPPType(file.getName().replaceAll("_signed_zipalign",""));

            appType.put(app,category);





        }
    }

    public static String findAPPType(String name) {


        File apkCategoryDirFile=new File(apkCategoryDir);

        for(File dirFile:apkCategoryDirFile.listFiles())
        {
            if(dirFile.isDirectory()&&(!dirFile.getName().equals("all_app")))
            {
                for(File apkFile:dirFile.listFiles())
                {
                    if(apkFile.getName().equals(name))
                    {
                        return dirFile.getName();
                    }
                }
            }
        }

        return "zms";




    }
}
