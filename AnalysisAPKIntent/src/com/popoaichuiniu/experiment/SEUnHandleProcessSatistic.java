package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;





public class SEUnHandleProcessSatistic {


    private String appPath="";

    private long allCount=0;

    private long unHandleCount=0;


    public static Logger exceptionLogger=new MyLogger("AnalysisAPKIntent/SEUnHandleProcessSatistic","exceptionLogger").getLogger();
    public SEUnHandleProcessSatistic(String appPath) {
        this.appPath = appPath;
    }

    public void addAllCount()
    {
        allCount++;
    }


    public  void addUnHandleCount()
    {
        unHandleCount++;
    }

    public void saveData()
    {
        WriteFile writeFileResult=new WriteFile("AnalysisAPKIntent/SEUnHandleProcessSatistic/result.txt",true,exceptionLogger);
        if(allCount!=0&&allCount>=unHandleCount)
        {
            writeFileResult.writeStr(appPath+","+allCount+","+unHandleCount+","+((double)unHandleCount)/allCount+"\n");

        }
        else
        {
            writeFileResult.writeStr(appPath+","+"计算失败！"+allCount+","+unHandleCount+"\n");
        }

        writeFileResult.close();

    }


}
