package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ComputeSETimeUse {

    private static Logger logger=new MyLogger("AnalysisAPKIntent/ComputeSETimeUse","exceptionLogger").getLogger();

    public static void main(String[] args) {

        ReadFileOrInputStream readFileOrInputStream=new ReadFileOrInputStream(Config.intentConditionSymbolicExcutationResults +"/"+"timeUse.txt",logger);
        List<String> listTime=readFileOrInputStream.getAllContentList();
        double sum=0;
        double min=Integer.MAX_VALUE;
        String minApp="";
        double max=Integer.MIN_VALUE;
        String maxApp="";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet1");

        Row row=sheet.createRow(0);
        Cell cell1=row.createCell(0);
        cell1.setCellValue("APPName");
        Cell cell2=row.createCell(1);
        cell2.setCellValue("time");

        int rowRank=1;

        for(String line:listTime)
        {
            Row rowTemp=sheet.createRow(rowRank++);
            Cell cellTemp1=rowTemp.createCell(0);
            Cell cellTemp2=rowTemp.createCell(1);
            String [] str=line.split(",");
            String time=str[0];
            String apkPath=str[1];
            double timeDouble=Double.parseDouble(time);

            cellTemp1.setCellValue(apkPath);
            cellTemp2.setCellValue(timeDouble);

            sum=sum+timeDouble;
            if(timeDouble<min)
            {
                min=timeDouble;
                minApp=apkPath;
            }

            if(timeDouble>max)
            {
                max=timeDouble;
                maxApp=apkPath;
            }


        }

        try {
            FileOutputStream outputStream = new FileOutputStream(Config.intentConditionSymbolicExcutationResults+"/" +"timeUse.xlsx");
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double average=sum/listTime.size();

        System.out.println("average time:"+average);
        System.out.println("min time"+ min);
        System.out.println("min app"+ minApp);

        System.out.println("max time"+ max);
        System.out.println("max app"+ maxApp);

    }
}
