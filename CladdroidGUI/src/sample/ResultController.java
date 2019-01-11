package sample;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.ExceptionStackMessageUtil;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.apache.log4j.Logger;


import java.util.*;

public class ResultController {

    private Logger exceptionLogger=new MyLogger("CladdroidGUI/log","exception").getLogger();

    @FXML

    private TextArea resultTextArea;


    @FXML
    private void initialize()
    {

        String analysisResultFile=Config.logDir+"/"+"testlog"+"/"+"ZMSInstrumentResult.txt";

        ReadFileOrInputStream readFileOrInputStream=new ReadFileOrInputStream(analysisResultFile,exceptionLogger);
        List<String> listString=readFileOrInputStream.getAllContentList();

        String result="";
        for(String str:listString)
        {
            result=result+str+"\n";
        }

        resultTextArea.setText(result);
    }


}
