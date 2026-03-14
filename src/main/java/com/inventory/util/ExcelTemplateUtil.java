package com.inventory.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;

public class ExcelTemplateUtil {

    public static void generateTransactionTemplate(String path) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");

        List<String> headers = List.of(
                "TRANSACTION_ID",
                "BUYSELL",
                "PLANT",
                "DEPARTMENT",
                "LOCATION",
                "EMPLOYEE_ID",
                "EMPLOYEE_NAME",
                "IP_ADDRESS",
                "ITEM_CODE",
                "ITEM_NAME",
                "ITEM_MAKE",
                "ITEM_MODEL",
                "ITEM_SERIAL",
                "ITEM_CONDITION",
                "ITEM_LOCATION",
                "ITEM_CATEGORY",
                "IMEI_NO",
                "SIM_NO",
                "PO_NO",
                "PARTY_NAME",
                "STATUS",
                "ISSUED_DATE",
                "RETURNED_DATE",
                "REMARKS",
                "ITEM_COUNT",
                "UNIT",
                "LAST_MODIFIED_BY"
        );

        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < headers.size(); i++) {

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);

            sheet.autoSizeColumn(i);
        }

        // Example row to guide users
        Row example = sheet.createRow(1);

        example.createCell(0).setCellValue(1); // Transaction ID
        example.createCell(1).setCellValue("BUY");
        example.createCell(2).setCellValue("BLR");
        example.createCell(3).setCellValue("IT");
        example.createCell(4).setCellValue("SERVER ROOM");
        example.createCell(5).setCellValue("EMP001");
        example.createCell(6).setCellValue("JOHN");
        example.createCell(7).setCellValue("192.168.1.10");
        example.createCell(8).setCellValue("LAP001");
        example.createCell(9).setCellValue("LAPTOP");
        example.createCell(10).setCellValue("DELL");
        example.createCell(11).setCellValue("5430");
        example.createCell(12).setCellValue("SN12345");
        example.createCell(13).setCellValue("NEW");
        example.createCell(14).setCellValue("RACK1");
        example.createCell(15).setCellValue("ELECTRONICS");
        example.createCell(16).setCellValue("IMEI123456");
        example.createCell(17).setCellValue("SIM123456");
        example.createCell(18).setCellValue("PO7788");
        example.createCell(19).setCellValue("ABC SUPPLIERS");
        example.createCell(20).setCellValue("IN STOCK");
        example.createCell(21).setCellValue("2026-03-14 10:30");
        example.createCell(22).setCellValue("");
        example.createCell(23).setCellValue("INITIAL STOCK");
        example.createCell(24).setCellValue(1);
        example.createCell(25).setCellValue("PCS");
        example.createCell(26).setCellValue("ADMIN");

        try (FileOutputStream fos = new FileOutputStream(path)) {
            workbook.write(fos);
        }

        workbook.close();
    }
}