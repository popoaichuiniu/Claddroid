package com.zhou;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import com.popoaichuiniu.util.WriteFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ApkSigner {

    private static boolean isTest = Config.isTest;

    public static void main(String[] args) {


        String appDir = null;
        if (isTest) {
            appDir = new File(Config.defaultAppPath).getParentFile().getAbsolutePath() + "/" + "instrumented/" + new File(Config.defaultAppPath).getName();
        } else {
            appDir = Config.wandoijiaAPP + "/" + "instrumented";
        }

        File appDirFile = new File(appDir);
        if (appDirFile.isDirectory()) {

            Set<String> hasSignedApps = new ReadFileOrInputStream("InstrumentAPK/apkSignerLog/hasSignedApps.txt").getAllContentLinSet();
            WriteFile writeFile = new WriteFile("InstrumentAPK/apkSignerLog/hasSignedApps.txt", true);
            WriteFile writeFileException = new WriteFile("InstrumentAPK/apkSignerLog/exceptionSignedApps.txt", true);
            for (File apkFile : appDirFile.listFiles()) {
                if ((apkFile.getName().endsWith(".apk")) && (!apkFile.getName().endsWith("_signed_zipalign.apk"))) {
                    if (hasSignedApps.contains(apkFile.getAbsolutePath())) {
                        continue;
                    }

                    try {
                        singleAppAnalysis(apkFile);
                    } catch (Exception e) {

                        writeFileException.writeStr(apkFile.getAbsolutePath() + "\n");
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
        ProcessBuilder processBuilder = new ProcessBuilder("InstrumentAPK/src/com/zhou/apkSigner.sh", appDirFile.getAbsolutePath());
        // ProcessBuilder processBuilder=new ProcessBuilder();


        processBuilder.redirectErrorStream(true);


        System.out.println("InstrumentAPK/src/com/zhou/apkSigner.sh" + "  " + appDirFile.getAbsolutePath());

        Process process = processBuilder.start();


        Thread childThread = new Thread(new Runnable() {//must start thread to read process output
            @Override
            public void run() {

                ReadFileOrInputStream readFileOrInputStreamReturnString = new ReadFileOrInputStream(process.getInputStream());
                System.out.println(readFileOrInputStreamReturnString.getContent() + "&&&");

            }
        });

        childThread.start();
        int status = process.waitFor();//


        if (status != 0) {

            throw new RuntimeException("sign apk failed!\n");
        }


    }
}
