package com.popoaichuiniu.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelWrite {

    private String fileName = null;
    XSSFWorkbook workbook = null;
    XSSFSheet sheet = null;
    int rowRank = 0;

    public ExcelWrite(String path) {
        fileName = path;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("sheet1");
    }

    public void addRow(Object []value) {
        Row rowTemp = sheet.createRow(rowRank++);


        for(int i=0;i<value.length;i++)
        {
            Cell cell=rowTemp.createCell(i);

            if(value[i] instanceof String)
            {
                cell.setCellValue((String)value[i]);
            }
            else if(value[i] instanceof Double)
            {
                cell.setCellValue((Double) value[i]);
            }
            else if(value[i] instanceof Integer)
            {
                cell.setCellValue((Integer) value[i]);
            }
            else
            {
                throw  new RuntimeException("can't handle type");
            }

        }



    }


    public void closeFile()
    {

        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
