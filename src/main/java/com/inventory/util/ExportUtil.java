package com.inventory.util;

import com.inventory.model.TransactionHistory;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportUtil {

    // ===================== EXCEL =====================
    public static void exportToExcel(List<TransactionHistory> data, String filePath) {

        try (Workbook workbook = new XSSFWorkbook()) {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

            Sheet sheet = workbook.createSheet("Transactions");

            Row header = sheet.createRow(0);

            String[] columns = {
                    "Item ID", "Item Name", "Employee ID",
                    "Person Name", "Issued Date",
                    "Returned Date", "Remarks"
            };

            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;

            for (TransactionHistory t : data) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(t.getItemId());
                row.createCell(1).setCellValue(t.getItemName());
                row.createCell(2).setCellValue(t.getEmployeeId());
                row.createCell(3).setCellValue(t.getPersonName());

                LocalDateTime issued = LocalDateTime.parse(t.getIssuedDateTime());
                row.createCell(4).setCellValue(issued.format(formatter));

                row.createCell(5).setCellValue(
                        t.getReturnedDateTime() == null ?
                                "Not Returned" :
                                LocalDateTime.parse(t.getReturnedDateTime()).format(formatter)
                );
                row.createCell(6).setCellValue(t.getRemarks());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== PDF =====================
    public static void exportToPDF(List<TransactionHistory> data, String filePath) {

        try {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            document.open();

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);

            String[] headers = {
                    "Item ID", "Item Name", "Employee ID",
                    "Person Name", "Issued Date",
                    "Returned Date", "Remarks"
            };

            for (String header : headers) {
                table.addCell(new Phrase(header));
            }

            for (TransactionHistory t : data) {

                table.addCell(t.getItemId());
                table.addCell(t.getItemName());
                table.addCell(t.getEmployeeId());
                table.addCell(t.getPersonName());

                LocalDateTime issued = LocalDateTime.parse(t.getIssuedDateTime());
                table.addCell(issued.format(formatter));

                table.addCell(
                        t.getReturnedDateTime() == null ?
                                "Not Returned" :
                                LocalDateTime.parse(t.getReturnedDateTime()).format(formatter)
                );
                table.addCell(t.getRemarks());
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}