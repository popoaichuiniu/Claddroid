package com.popoaichuiniu.util;

import org.apache.log4j.Logger;

public class UnHandleWriter {


    private static Logger logger=new MyLogger("AnalysisAPKIntent/UnHandleWriter","exceptionLogger").getLogger();
    private static WriteFile writeFile=new WriteFile(Config.intentConditionSymbolicExcutationResults+"/"+"unhandleSituation.txt",true,logger);
    public static void write(String message) {
        writeFile.writeStr(message);
        writeFile.flush();
    }


}
