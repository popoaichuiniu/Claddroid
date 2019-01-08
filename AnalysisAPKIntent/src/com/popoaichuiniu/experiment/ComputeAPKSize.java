package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Set;

public class ComputeAPKSize {
    private static Logger logger = new MyLogger("AnalysisAPKIntent/ComputeAPKSize", "exceptionLogger").getLogger();

    public static void main(String[] args) {


        //getMethodFromAppDir();
        computeApkSizeFromTxt();


    }

    private static void getMethodFromAppDir() {
        File appDir = new File(Config.big_app_set);
        String result_file = "AnalysisAPKIntent/ComputeAPKSize/" + appDir.getName() + ".csv";
        System.out.println(result_file);
        WriteFile writeFileApkSize = new WriteFile(result_file, false, logger);
        for (File file : appDir.listFiles()) {
            if (file.getName().endsWith(".apk")) {
                System.out.println(file.getAbsolutePath());
                writeFileApkSize.writeStr(file.getAbsolutePath() + "," + (((double) file.length()) / 1024 / 1024) + "\n");
            }
        }
        writeFileApkSize.close();
    }

    private static void computeApkSizeFromTxt() {
        String appTxt = "/media/mobile/myExperiment/idea_ApkIntentAnalysis/testAPP/wandoujia_2019_1_1/successTest_apk_list";

        ReadFileOrInputStream readFileOrInputStream = new ReadFileOrInputStream(appTxt, logger);

        long minSize = Long.MAX_VALUE;

        String minAPP = "";
        long maxSize = Long.MIN_VALUE;

        String maxAPP = "";

        long allSize = 0;
        Set<String> contentSet = readFileOrInputStream.getAllContentLinSet();
        String result_file = "AnalysisAPKIntent/ComputeAPKSize/" + new File(appTxt).getName() + ".csv";
        System.out.println(result_file);
        WriteFile writeFileApkSize = new WriteFile(result_file, false, logger);


        for (String app : contentSet) {
            File apkFile = new File(app);

            long fileSize = apkFile.length();

            allSize = allSize + fileSize;

            if (fileSize < minSize) {
                minSize = fileSize;
                minAPP = app;
            }

            if (fileSize > maxSize) {
                maxSize = fileSize;
                maxAPP = app;
            }



            if (apkFile.getName().endsWith(".apk")) {
                System.out.println(apkFile.getAbsolutePath());
                writeFileApkSize.writeStr(apkFile.getAbsolutePath() + "," + (((double) apkFile.length()) / 1024 / 1024) + "\n");
            }




        }

        System.out.println("****************************************");

        System.out.println("minSize:" + minSize + " " + minAPP);
        System.out.println("maxSize:" + maxSize + " " + maxAPP);
        System.out.println("allSize:" + ((double) allSize) / contentSet.size());

        writeFileApkSize.close();
    }
}
