package com.popoaichuiniu.jacy;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.ExceptionStackMessageUtil;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.IOException;

public class AndroidCallGraphProxy implements CGOperation{

    public  AndroidCallGraph androidCallGraph=null;
    public AndroidCallGraphProxy(String appPath, String androidPlatformPath,Logger logger) {
        androidCallGraph=new AndroidCallGraph(appPath,androidPlatformPath);


        try {
            androidCallGraph.caculateAndroidCallGraph();

        } catch (IOException e) {




            logger.error(appPath + "&&" + "IOException" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e));



        } catch (XmlPullParserException e) {




            logger.error(appPath + "&&" + "XmlPullParserException" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e));
        }


    }

    @Override
    public CallGraph getCG() {
        return androidCallGraph.getCg();
    }


}
