import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class computeCountOfNormalPermission {


    private static Logger logger = new MyLogger("NormalSelfPermission/computeCountOfNormalPermission/", "appException").getLogger();

    public static void main(String[] args) {

        Set<String> allPermissions = AndroidInfo.getAllPermissions();

        String appDirPath = Config.wandoijiaAPP;

        File appDir = new File(appDirPath);
        ReadFileOrInputStream readFileOrInputStream = new ReadFileOrInputStream("NormalSelfPermission/computeCountOfNormalPermission/hasAppsProcess.txt", logger);

        Set<String> hasAppsProcess = readFileOrInputStream.getAllContentLinSet();

        WriteFile writeFileSuccess = new WriteFile("NormalSelfPermission/computeCountOfNormalPermission/hasAppsProcess.txt", true, logger);

        WriteFile writeFileResult = new WriteFile("NormalSelfPermission/computeCountOfNormalPermission/" + "result.txt", true, logger);


        HashSet<String> allProblemApps = new HashSet<>();


        int count = 0;
        for (File file : appDir.listFiles()) {
            if (file.getName().endsWith(".apk")) {

                count++;

                if (hasAppsProcess.contains(file.getAbsolutePath())) {
                    continue;
                }

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            AndroidInfo androidInfo = new AndroidInfo(file.getAbsolutePath(), logger);
                            Map<String, Map<AXmlNode, String>> permissionProtectedEAs = androidInfo.getPermissionProtectedEAs();
                            for (Map.Entry<String, Map<AXmlNode, String>> entry : permissionProtectedEAs.entrySet())//key :EA name value: EA node
                            {
                                Map<AXmlNode, String> eaNodeAndPermission = entry.getValue();//map：权限保护的EA     key：EA  value:permission

                                for (Map.Entry<AXmlNode, String> permissionProtectedEAEntry : eaNodeAndPermission.entrySet()) {
                                    if (!allPermissions.contains(permissionProtectedEAEntry.getValue()))//android权限包括它吗？
                                    {
                                        System.out.println(permissionProtectedEAEntry.getKey());

                                        AXmlNode selfDefinePermissionAXmlNode = androidInfo.getSelfDefinePermissionsMap().get(permissionProtectedEAEntry.getValue());
                                        if (selfDefinePermissionAXmlNode != null) {
                                            AXmlAttribute<?> aXmlAttribute = selfDefinePermissionAXmlNode.getAttribute("protectionLevel");
                                            if (aXmlAttribute.getValue() instanceof Integer) {
                                                Integer protectionLevel = (Integer) aXmlAttribute.getValue();
                                                if (protectionLevel == 0 || protectionLevel == 1) {
                                                    writeFileResult.writeStr(file.getAbsolutePath() + "##" + permissionProtectedEAEntry.getKey() + "$$" + selfDefinePermissionAXmlNode + "\n");
                                                    allProblemApps.add(file.getAbsolutePath());
                                                } else {
                                                    logger.info(protectionLevel + "^^^^^^" + permissionProtectedEAEntry.getKey() + "$$" + selfDefinePermissionAXmlNode);
                                                }
                                            } else {
                                                logger.error("aXmlAttribute.getValue() not integer");
                                            }


                                        } else {

                                                logger.warn(file.getAbsolutePath() + "##" + permissionProtectedEAEntry.getKey() + "##" + "没有找到权限定义");


                                        }
                                    }
                                }
                            }
                            writeFileSuccess.writeStr(file.getAbsolutePath() + "\n");
                            writeFileSuccess.flush();

                        } catch (Exception exception) {
                            logger.error(file.getAbsolutePath() + "##" + exception.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(exception));
                        }

                    }
                });

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    logger.error(file.getAbsolutePath() + "##" + e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
                }


            }
        }
        writeFileSuccess.close();

        System.out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
        writeFileResult.writeStr("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ"+"\n");
        System.out.println(allProblemApps.size() + "tt" + count);
        writeFileResult.writeStr(allProblemApps.size() + "tt" + count+"\n");
        for(String app: allProblemApps)
        {
            writeFileResult.writeStr(app+"\n");
        }
        writeFileResult.close();



    }
}
