package com.popoaichuiniu.util;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.io.IOException;

public class MyLogger {


    private  Logger logger=null;

    public static Logger getOverallLogger(Class logger)
    {
        Logger runLogger=Logger.getLogger(logger);
        if(runLogger.getAllAppenders().hasMoreElements())
        {
            return runLogger;
        }
        String logfilePathRun=Config.logDir+ File.separator+logger.getName()+".log";
        try {
        runLogger.addAppender(new FileAppender(new PatternLayout("%d %p [%t] %C.%M(%L) | %m%n"), logfilePathRun));
        runLogger.addAppender(new ConsoleAppender(new PatternLayout("%d %p [%t] %C.%M(%L) | %m%n")));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return runLogger;
    }


    public MyLogger(String loggerName) {//默认目录
        this.logger=Logger.getLogger(loggerName);
        String logfilePath=Config.logDir+File.separator+loggerName+".log";
        try {
            this.logger.addAppender(new FileAppender(new PatternLayout("%d %p [%t] %C.%M(%L) | %m%n"), logfilePath));
            this.logger.addAppender(new ConsoleAppender(new PatternLayout("%d %p [%t] %C.%M(%L) | %m%n")));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public MyLogger(String dir,String loggerName) {
        this.logger=Logger.getLogger(loggerName);
        String logfilePath=dir+File.separator+loggerName+".log";
        try {
            this.logger.addAppender(new FileAppender(new PatternLayout("%d %p [%t] %C.%M(%L) | %m%n"), logfilePath));
            this.logger.addAppender(new ConsoleAppender(new PatternLayout("%d %p [%t] %C.%M(%L) | %m%n")));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }



    }

    public Logger getLogger() {
        return logger;
    }
}
