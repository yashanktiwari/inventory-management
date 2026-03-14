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
                "REMARKS",
                "ITEM_COUNT",
                "UNIT"
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

        example.createCell(0).setCellValue("BUY");
        example.createCell(1).setCellValue("BLR");
        example.createCell(2).setCellValue("IT");
        example.createCell(3).setCellValue("SERVER ROOM");
        example.createCell(4).setCellValue("EMP001");
        example.createCell(5).setCellValue("JOHN");
        example.createCell(6).setCellValue("192.168.1.10");
        example.createCell(7).setCellValue("LAP001");
        example.createCell(8).setCellValue("LAPTOP");
        example.createCell(9).setCellValue("DELL");
        example.createCell(10).setCellValue("5430");
        example.createCell(11).setCellValue("SN12345");
        example.createCell(12).setCellValue("NEW");
        example.createCell(13).setCellValue("RACK1");
        example.createCell(14).setCellValue("ELECTRONICS");
        example.createCell(15).setCellValue("IMEI123456");
        example.createCell(16).setCellValue("SIM123456");
        example.createCell(17).setCellValue("PO7788");
        example.createCell(18).setCellValue("ABC SUPPLIERS");
        example.createCell(19).setCellValue("INITIAL STOCK");
        example.createCell(20).setCellValue(1);
        example.createCell(21).setCellValue("PCS");

        try (FileOutputStream fos = new FileOutputStream(path)) {
            workbook.write(fos);
        }

        workbook.close();
    }
}