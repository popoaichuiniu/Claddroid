package com.zhou;

import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ApkSigner {

    private static boolean isTest = Config.isTest;
    private static Logger exceptionLogger = new MyLogger(Config.apkSignerLog, "exception").getLogger();
    private static Logger infoLogger = new MyLogger(Config.apkSignerLog, "info").getLogger();


    public static void main(String[] args) {


        String appDir = null;
        if (isTest) {
            appDir = new File(Config.testAppPath).getParentFile().getAbsolutePath() + "/" + "instrumented/" + new File(Config.testAppPath).getName();
        } else {
            File dirFile = new File(Config.defaultAppDirPath);
            if (dirFile.isDirectory()) {
                appDir = Config.defaultAppDirPath + "/" + "instrumented";
            } else {
                appDir = dirFile.getParentFile().getAbsolutePath() + "/" + "instrumented/" + dirFile.getName();
            }

        }

        File appDirFile = new File(appDir);
        if (appDirFile.isDirectory()) {

            Set<String> hasSignedApps = new ReadFileOrInputStream("InstrumentAPK/apkSignerLog/hasSignedApps.txt", exceptionLogger).getAllContentLinSet();
            WriteFile writeFile = new WriteFile("InstrumentAPK/apkSignerLog/hasSignedApps.txt", true, exceptionLogger);
            WriteFile writeFileException = new WriteFile("InstrumentAPK/apkSignerLog/exceptionSignedApps.txt", true, exceptionLogger);
            for (File apkFile : appDirFile.listFiles()) {
                if ((apkFile.getName().endsWith(".apk")) && (!apkFile.getName().endsWith("_signed_zipalign.apk"))) {
                    if (hasSignedApps.contains(apkFile.getAbsolutePath())) {
                        continue;
                    }

                    try {
                        singleAppAnalysis(apkFile);
                    } catch (Exception e) {

                        writeFileException.writeStr(apkFile.getAbsolutePath() + "##" + e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e) + "\n");
                        writeFileException.flush();


                    }

                    writeFile.writeStr(apkFile.getAbsolutePath() + "\n");
                    writeFile.flush();


                }
            }

            writeFileException.close();
            writeFile.close();


        } else {

            try {
                singleAppAnalysis(appDirFile);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private static void singleAppAnalysis(File appDirFile) throws InterruptedException, IOException {

        infoLogger.info("start APK Sign:" + appDirFile.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder("InstrumentAPK/src/com/zhou/apkSigner.sh", appDirFile.getAbsolutePath());
        // ProcessBuilder processBuilder=new ProcessBuilder();


        processBuilder.redirectErrorStream(true);


        System.out.println("InstrumentAPK/src/com/zhou/apkSigner.sh" + "  " + appDirFile.getAbsolutePath());

        Process process = processBuilder.start();


        Thread childThread = new Thread(new Runnable() {//must start thread to read process output
            @Override
            public void run() {

                ReadFileOrInputStream readFileOrInputStreamReturnString = new ReadFileOrInputStream(process.getInputStream(), exceptionLogger);
                System.out.println(readFileOrInputStreamReturnString.getContent() + "&&&");
                infoLogger.info(readFileOrInputStreamReturnString.getContent() + "&&&");

            }
        });

        childThread.start();
        int status = process.waitFor();//


        if (status != 0) {

            throw new RuntimeException("sign apk failed!\n");
        }

        infoLogger.info("apk sign over:"+appDirFile.getAbsolutePath());
    }
}
