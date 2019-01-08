package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SelectAPP {

    private static String appDir = Config.xiaomiApp;

    private static Logger logger=new MyLogger("AnalysisAPKIntent/SelectAPP","exceptionLogger").getLogger();

    public static void main(String[] args) {
        ReadFileOrInputStream readFileOrInputStream = new ReadFileOrInputStream("AnalysisAPKIntent"+"/"+"think_dangerousPermission.txt",logger);
        Set<String> dangerousPermissions= readFileOrInputStream.getAllContentLinSet();
        for(Iterator<String> dangerousPermissionsIterator=dangerousPermissions.iterator();((Iterator) dangerousPermissionsIterator).hasNext();)
        {
            String dangerousPermission=dangerousPermissionsIterator.next();
            if(dangerousPermission.startsWith("#"))
            {
                dangerousPermissionsIterator.remove();
            }
        }
        WriteFile writeFile = new WriteFile(Config.unitNeedAnalysisGenerate+"/"+new File(appDir).getName()+"_dangerousAPP.txt",false,logger);
        File xmlFile = new File(Config.unitNeedAnalysisGenerate+"/" + new File(appDir).getName() + "_DIR_permissionUse.xml");
        Set<String> apps=new HashSet<>();


        try {
            Document document = new SAXReader().read(xmlFile).getDocument();
            Element rootElement = document.getRootElement();
            for(Element apkElement:rootElement.elements("APK"))
            {
                for(Element permissionElement:apkElement.elements("permission"))
                {
                    String permissionValue=permissionElement.getStringValue();
                    if(dangerousPermissions.contains(permissionValue))
                    {

                        apps.add(apkElement.attributeValue("name"));
                    }
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        for(String app:apps)
        {
            System.out.println(app);
            try {
                FileOutputStream fileOutputStream=new FileOutputStream("/home/zms/xiaomiAPPSelect/"+new File(app).getName());
                FileInputStream fileInputStream=new FileInputStream(new File(app));
                byte [] buffer= new byte[1024*512];
                int length=-1;
                while ((length=fileInputStream.read(buffer))!=-1)
                {
                    fileOutputStream.write(buffer,0,length);


                }
                writeFile.writeStr(app+"\n");

                fileInputStream.close();
                fileOutputStream.close();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        writeFile.close();
        System.out.println("over!");


    }
}
