package com.popoaichuiniu.jacy.statistic;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;

import java.util.Set;

public class PreProcess {

    private static Logger logger=new MyLogger("EANotProper/IntentImplicitUse/wandoujia_2018_11_25","preProcess").getLogger();
    public static void main(String[] args) {

        Set<String>  contentSet=new ReadFileOrInputStream("EANotProper/IntentImplicitUse/wandoujia_2018_11_25/result (copy).txt",logger).getAllContentLinSet();
        WriteFile writeFile=new WriteFile("EANotProper/IntentImplicitUse/wandoujia_2018_11_25/"+"result.csv",false,logger);

        for(String str:contentSet)
        {
            String newStr=str.replaceAll(" ",",");
            writeFile.writeStr(newStr+"\n");
        }
        writeFile.close();

    }

}