package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.ExcelWrite;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class ComputePlPermssions {


    private static String appDir="/home/zms/logger_file/testlog/wandoujia_1_2_needInstrument";
    private static Logger logger=new MyLogger("AnalysisAPKIntent/ComputePlPermssions","exceptionLogger").getLogger();

    public static void main(String[] args) {
        ReadFileOrInputStream readFileOrInputStream=new ReadFileOrInputStream(appDir+"/ZMSInstrument.log",logger);
        List<String> listString=readFileOrInputStream.getAllContentList();
        Map<String,Integer> permissionCount=new HashMap<>();
        Map<String,Set<String>> permissionAPPCount=new HashMap<>();
        Map<String,Set<String>> permissionAPPTypeCount=new HashMap<>();
        Map<String,Set<String>> appTypePermissionCount=new HashMap<>();
        HashSet<String> apps=new HashSet<>();
        for(String str:listString)
        {
            String [] strArray=str.split("#");
            String permissionStr=strArray[strArray.length-1].substring(1,strArray[strArray.length-1].length());
            String appName=strArray[2];

            apps.add(appName);

           // String appType= FindAPPCategory.findAPPType(new File(appName).getName());

//            if(appType.equals("zms"))//adb log has problem
//            {
//                System.out.println(appName);
//
//
//            }

            String [] permissionArray=permissionStr.split(",");

            for(String permission:permissionArray)
            {
                if(!permission.startsWith("android"))
                {
                    continue;
                }

                //
                Integer integer=permissionCount.get(permission);
                if(integer==null)
                {
                    permissionCount.put(permission,1);
                }
                else
                {
                    permissionCount.put(permission,integer+1);
                }


                //

                Set<String> appSet=permissionAPPCount.get(permission);
                if(appSet==null)
                {
                    appSet=new HashSet<>();

                }

                appSet.add(appName);


                permissionAPPCount.put(permission,appSet);



//                //appType permission
//
//                Set<String> permissionSet=appTypePermissionCount.get(appType);
//
//                if(permissionSet==null)
//                {
//                    permissionSet=new TreeSet<>();
//                }
//
//                permissionSet.add(permission);
//
//                appTypePermissionCount.put(appType,permissionSet);
//
//
//
//                //permission  appType
//
//                Set<String> appTypeSet=permissionAPPTypeCount.get(permission);
//                if(appTypeSet==null)
//                {
//                    appTypeSet=new TreeSet<>();
//
//                }
//
//                appTypeSet.add(appType);
//
//
//                permissionAPPTypeCount.put(permission,appTypeSet);






            }

        }


        ExcelWrite excelWritePermissionCount=new ExcelWrite(appDir+"/permissionAllCount.xlsx");

        excelWritePermissionCount.addRow(new Object[]{"permission","allCount"});
        for(Map.Entry<String,Integer> entry:permissionCount.entrySet())
        {
            excelWritePermissionCount.addRow(new Object[]{entry.getKey(),entry.getValue()});
        }

        excelWritePermissionCount.closeFile();




        ExcelWrite appExcelWritePermissionCount=new ExcelWrite(appDir+"/permissionAPPUseCount.xlsx");

        appExcelWritePermissionCount.addRow(new Object[]{"permission","appUseCount"});
        for(Map.Entry<String,Set<String>> entry:permissionAPPCount.entrySet())
        {
            appExcelWritePermissionCount.addRow(new Object[]{entry.getKey(),entry.getValue().size()});
        }

        appExcelWritePermissionCount.closeFile();




        ExcelWrite excelWriteAPPTypePermissionCount=new ExcelWrite(appDir+"/appTypePermissionSet.xlsx");

        excelWriteAPPTypePermissionCount.addRow(new Object[]{"appType","count","permissionSet"});

        for(Map.Entry<String,Set<String>> entry:appTypePermissionCount.entrySet())
        {
            excelWriteAPPTypePermissionCount.addRow(new Object[]{entry.getKey(),entry.getValue().size(),entry.getValue().toString()});
        }
        excelWriteAPPTypePermissionCount.closeFile();



        ExcelWrite appExcelWritePermissionAPPTypeCount=new ExcelWrite(appDir+"/permissionAPPTypeUseCount.xlsx");

        appExcelWritePermissionAPPTypeCount.addRow(new Object[]{"permission","appTypeCount","appType"});
        for(Map.Entry<String,Set<String>> entry:permissionAPPTypeCount.entrySet())
        {
            appExcelWritePermissionAPPTypeCount.addRow(new Object[]{entry.getKey(),entry.getValue().size(),entry.getValue().toString()});
        }

        appExcelWritePermissionAPPTypeCount.closeFile();



        System.out.println("over");

        System.out.println(apps.size());
    }
}
