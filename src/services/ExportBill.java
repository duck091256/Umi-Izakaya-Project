package services;

import data_access_object.BillDAO;
import models.Bill;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;

public class ExportBill {
    public static void exportBill() {
        ArrayList<Bill> listBill = BillDAO.loadBill();
        if (listBill == null) return;

        try (Workbook workbook = new XSSFWorkbook();
            FileOutputStream fileOutputStream = new FileOutputStream("D:/Java Final Project To Store Export File/Receipt File For Umi Izakaya.xls")) {
            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

            Sheet sheet = workbook.createSheet("bill");

            Row headerRow = sheet.createRow(0);
            String[] header = {"ID", "Trang thai thanh toan", "Thoi gian", "Tong tien"};

            for (int i = 0; i < header.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(header[i]);
            }

            for (int i = 0; i < listBill.size(); i++) {
                Bill curBill = listBill.get(i);
                Row curRow = sheet.createRow(i + 1);
                curRow.createCell(0).setCellValue(curBill.getBillID());

                curRow.createCell(1).setCellValue("Da thanh toan");

                Cell dateCell = curRow.createCell(2);
                dateCell.setCellStyle(dateCellStyle);
                dateCell.setCellValue(curBill.getTime());

                curRow.createCell(3).setCellValue(curBill.getPayment());
                
                System.out.println("Supercalifragilisticexpialidocious");
            }

            workbook.write(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
