package com.popoaichuiniu.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReadFileOrInputStream {

    private String fileName=null;

    private BufferedReader bufferedReader=null;

    private List<String>  contentList=null;

    private Set<String > contentSet=null;

    private Logger logger=null;




    public ReadFileOrInputStream(String fileName,Logger logger) {
        this.fileName = fileName;
        this.logger=logger;
        try {
            bufferedReader=new BufferedReader(new FileReader(fileName));
        }

        catch (IOException e)
        {
            logger.error(e.getMessage()+"##"+ExceptionStackMessageUtil.getStackTrace(e));
        }

    }

    public ReadFileOrInputStream(InputStream inputStream,Logger logger) {
        this.logger=logger;
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public String getContent()
    {
        String line = null;
        String content = "";

        try {
            while ((line = bufferedReader.readLine()) != null) {
                content=content+line+"\n";

            }
            bufferedReader.close();
        }
        catch (IOException e)
        {

            logger.error(e.getMessage()+"##"+ExceptionStackMessageUtil.getStackTrace(e));
        }

        return content;
    }


    public List<String> getAllContentList()
    {
        contentList=new ArrayList<>();
        String line = null;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                contentList.add(line);

            }
            bufferedReader.close();
        }
        catch (IOException e)
        {
            contentList=null;
            logger.error(e.getMessage()+"##"+ExceptionStackMessageUtil.getStackTrace(e));
        }

        return contentList;
    }

    public Set<String> getAllContentLinSet()
    {
        contentSet=new LinkedHashSet<>();
        String line = null;

        try {
            while ((line = bufferedReader.readLine()) != null) {
               contentSet.add(line);

            }
            bufferedReader.close();
        }
        catch (IOException e)
        {
            contentSet=null;
            logger.error(e.getMessage()+"##"+ExceptionStackMessageUtil.getStackTrace(e));
        }

        return contentSet;
    }
}
