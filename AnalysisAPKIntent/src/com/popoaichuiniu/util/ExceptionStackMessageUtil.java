package com.popoaichuiniu.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionStackMessageUtil {

    public static  String getStackTrace(Throwable throwable)
    {
        StringWriter sw=new StringWriter();
        PrintWriter pw=new PrintWriter(sw);

        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
