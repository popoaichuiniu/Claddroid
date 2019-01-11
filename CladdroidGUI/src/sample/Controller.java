package sample;

import com.popoaichuiniu.intentGen.GenerateUnitNeedToAnalysis;
import com.popoaichuiniu.intentGen.IntentConditionTransformSymbolicExcutation;
import com.popoaichuiniu.util.*;
import com.zhou.ApkSigner;
import com.zhou.InstrumentAPPBeforePermissionInvoke;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.List;

public class Controller {

    @FXML
    private Label filePathLabel;

    @FXML
    private TextArea progressTextArea;

    @FXML
    private TextArea logTextArea;


    private String chooseFilePath = null;

    private boolean isEvironmentCorrect = false;

    private boolean isStart = false;


    private static Logger exceptionLogger = new MyLogger(Config.cladDroidLogDir, "exception").getLogger();

    private static Logger infoLogger = new MyLogger(Config.cladDroidLogDir, "info").getLogger();


    @FXML
    private void initialize()
    {

        logTextArea.setWrapText(true);
    }

    @FXML
    private void detection() {
        isEvironmentCorrect = true;
        JOptionPane.showMessageDialog(null, "Your execution environment is correct!");
    }

    @FXML
    private void openApkOrDir() {
        String filePath = getFilePath();
        if (filePath != null) {
            chooseFilePath = filePath;
            filePathLabel.setText("File Path:  " + filePath);

        }

    }

    private String getFilePath() {
        String filePath = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileFilter filter = new FileFilter() {

            public boolean accept(File file) {

                if (file.isDirectory()) {
                    return true;
                } else {
                    if (file.getName().endsWith(".apk")) {
                        return true;
                    }
                }


                return false;
            }

            @Override
            public String getDescription() {
                return "apk & apk dir";
            }
        };

        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: " +
                    chooser.getSelectedFile().getName());
            filePath = chooser.getSelectedFile().getAbsolutePath();
        }


        if (filePath == null || filePath.trim().equals("")) {
            return null;
        }


        System.out.println(filePath);
        return filePath;
    }

    @FXML
    private void startAnalyse() {
        if (chooseFilePath == null) {
            JOptionPane.showMessageDialog(null, "You must choose file firstly!", "warning", JOptionPane.WARNING_MESSAGE);
        } else {


            if (!isEvironmentCorrect) {
                JOptionPane.showMessageDialog(null, "You must detect your execution environment firstly!", "warning", JOptionPane.WARNING_MESSAGE);

            } else {

                if (!isStart) {
                    isStart = true;
                    doAnalysis();


                } else {
                    JOptionPane.showMessageDialog(null, "You must wait for current analysis to complete!", "warning", JOptionPane.WARNING_MESSAGE);
                }


            }


        }

    }

    private void doAnalysis() {

        Config.defaultAppDirPath = chooseFilePath;

        Thread childThreadAnalysis = new Thread(new Runnable() {
            @Override
            public void run() {

                String logPaths []={Config.unitNeedAnalysisGenerate+"/"+"GenerateUnitNeedToAnalysisInfo.log",
                        Config.logDir+"/"+"com.popoaichuiniu.intentGen.IntentConditionTransformSymbolicExcutation.log",
                        Config.instrument_logDir+"/"+"InstrumentInfo.log",
                        Config.apkSignerLog+"/"+"info.log",
                        Config.cladDroidLogDir+"/"+"info.log"
                };

                for(String path:logPaths)
                {
                    File unitNeedAnalysisGenerateLog=new File(path);
                    if(unitNeedAnalysisGenerateLog.exists())
                    {
                        unitNeedAnalysisGenerateLog.delete();
                    }
                }

                ReadLogThread readLogThread=new ReadLogThread(logPaths);
                readLogThread.start();

                progressTextArea.appendText("1. Start construct CG and CFG,\n");
                progressTextArea.appendText("\tfind potential privilege leak units\n");
                GenerateUnitNeedToAnalysis.main(null);
                progressTextArea.appendText("\tPart 1 complete!\n\n");




                progressTextArea.appendText("2. Start find paths and get intent conditions\n");
                IntentConditionTransformSymbolicExcutation.main(null);
                progressTextArea.appendText("\tPart 2 complete!\n\n");


                progressTextArea.appendText("3. Start instrument and sign app\n");
                InstrumentAPPBeforePermissionInvoke.main(null);
                ApkSigner.main(null);
                progressTextArea.appendText("\tPart 3 complete!\n\n");

                progressTextArea.appendText("4. Start test app\n");
                try {
                    int status = exeCmd(new File("testAPP"), "/home/lab418/anaconda3/bin/python", "testAPP.py", chooseFilePath);
                    if (status != 0) {
                        exceptionLogger.error(status + " exeCmd error");
                    }
                } catch (IOException e) {
                    exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
                } catch (InterruptedException e) {
                    exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
                }
                progressTextArea.appendText("\tPart 4 complete!\n\n");
                isStart = false;

                generateResult();

                JOptionPane.showMessageDialog(null, "Analyse completely!");

            }
        });
        childThreadAnalysis.start();


    }

    private void generateResult() {

        WriteFile writeFile = new WriteFile(Config.logDir + "/" + "testlog" + "/" + "ZMSInstrumentResult.txt", false, exceptionLogger);
        String result = getResult();
        writeFile.writeStr(result);
        writeFile.close();
    }

    private String getResult() {

        String logPath = Config.logDir + "/" + "testlog" + "/" + "ZMSInstrument.log";

        ReadFileOrInputStream readFileOrInputStream = new ReadFileOrInputStream(logPath, exceptionLogger);
        List<String> listString = readFileOrInputStream.getAllContentList();

        String result = "";
        for (String str : listString) {
            try {
                String[] strArray = str.split("#");
                String permissionStr = strArray[strArray.length - 1].substring(1, strArray[strArray.length - 1].length());
                String appName = strArray[2];
                result = result + appName + "," + permissionStr + "\n";
            } catch (Exception e) {
                exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e) + "##" + "analyse ZMSInstrument.log error!");
            }


        }

        return result;

    }


    @FXML
    private void viewResult() {

        try {

            String analysisResultFile = Config.logDir + "/" + "testlog" + "/" + "ZMSInstrumentResult.txt";
            exeCmd(new File("."), "gedit", analysisResultFile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }


    }

    @FXML
    private void viewLog() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    exeCmd(new File("."), "nautilus", Config.logDir);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();


    }

    private int exeCmd(File workdir, String... command) throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workdir);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        Thread childThread = new Thread(new Runnable() {//must start thread to read process output
            @Override
            public void run() {

                ReadFileOrInputStream readFileOrInputStreamReturnString = new ReadFileOrInputStream(process.getInputStream(), exceptionLogger);
                infoLogger.info(readFileOrInputStreamReturnString.getContent() + "&&&");

            }
        });

        childThread.start();
        int status = process.waitFor();//

        return status;


    }


    private void testCmd() {
        try {
            int status = exeCmd(new File("/media/mobile/myExperiment/idea_ApkIntentAnalysis/testAPP"), "/home/lab418/anaconda3/bin/python", "/media/mobile/myExperiment/idea_ApkIntentAnalysis/testAPP/testAPP.py", "/home/zms/test_cl/app-debug.apk");
            if (status != 0) {
                exceptionLogger.error(status + " exeCmd error");
            }
        } catch (IOException e) {
            exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
        } catch (InterruptedException e) {
            exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
        }
    }

    class ReadLogThread extends Thread {


        private boolean isStop = false;

        private String [] logPaths = null;

        private int index=0;

        public ReadLogThread(String [] logPaths) {
            this.logPaths = logPaths;
        }

        @Override
        public void run() {

            long lastTimeFileSize = 0;
            int count=0;

            while (!isStop) {

                while (true)
                {
                    File logFile=new File(logPaths[index]);
                    if(logFile.exists())
                    {
                        break;
                    }
                }

                System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                try {

                    RandomAccessFile randomFile = new RandomAccessFile(logPaths[index], "r");
                    randomFile.seek(lastTimeFileSize);

                    String oneLine = randomFile.readLine();
                    if(oneLine!=null)
                    {
                        byte [] lineBytes=oneLine.getBytes("ISO-8859-1");
                        if(logTextArea.getLength()>5000)
                        {
                            logTextArea.clear();
                        }
                        logTextArea.appendText(new String(lineBytes,"utf-8"));
                        lastTimeFileSize = lastTimeFileSize+lineBytes.length+1;//注意需要加1，跳过\n
                        count=0;
                    }
                    else
                    {
                        System.out.println("------------------------------");
                        count++;
                    }

                    randomFile.close();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        exceptionLogger.error("ReadLogThread" + e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e) + "##");
                    }

                    if(count>150)//15秒内不变化，终止读！换个文件读
                    {
                        index++;
                        if(index>=logPaths.length)
                        {
                            isStop=true;
                        }
                    }


                } catch (IOException e) {
                    exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e) + "##" + logPaths);
                }

            }


        }


    }




}


