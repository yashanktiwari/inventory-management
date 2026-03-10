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
                    "Buy/Sell",
                    "Plant",
                    "Department",
                    "Location",
                    "Employee ID",
                    "Employee Name",
                    "IP Address",
                    "Item Code",
                    "Item Name",
                    "Item Make",
                    "Item Model",
                    "Item Serial",
                    "IMEI No",
                    "SIM No",
                    "PO No",
                    "Party Name",
                    "Status",
                    "Issued Date",
                    "Returned Date",
                    "Remarks"
            };

            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;

            for (TransactionHistory t : data) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(nullSafe(t.getBuySell()));
                row.createCell(1).setCellValue(nullSafe(t.getPlant()));
                row.createCell(2).setCellValue(nullSafe(t.getDepartment()));
                row.createCell(3).setCellValue(nullSafe(t.getLocation()));
                row.createCell(4).setCellValue(nullSafe(t.getEmployeeCode()));
                row.createCell(5).setCellValue(nullSafe(t.getEmployeeName()));
                row.createCell(6).setCellValue(nullSafe(t.getIpAddress()));
                row.createCell(7).setCellValue(nullSafe(t.getItemCode()));
                row.createCell(8).setCellValue(nullSafe(t.getItemName()));
                row.createCell(9).setCellValue(nullSafe(t.getItemMake()));
                row.createCell(10).setCellValue(nullSafe(t.getItemModel()));
                row.createCell(11).setCellValue(nullSafe(t.getItemSerial()));
                row.createCell(12).setCellValue(nullSafe(t.getImeiNo()));
                row.createCell(13).setCellValue(nullSafe(t.getSimNo()));
                row.createCell(14).setCellValue(nullSafe(t.getPoNo()));
                row.createCell(15).setCellValue(nullSafe(t.getPartyName()));
                row.createCell(16).setCellValue(nullSafe(t.getStatus()));

                if (t.getIssuedDateTime() != null) {
                    LocalDateTime issued = java.sql.Timestamp
                            .valueOf(t.getIssuedDateTime())
                            .toLocalDateTime();
                    row.createCell(17).setCellValue(issued.format(formatter));
                }

                if (t.getReturnedDateTime() != null) {
                    LocalDateTime returned =
                            java.sql.Timestamp
                                    .valueOf(t.getReturnedDateTime())
                                    .toLocalDateTime();
                    row.createCell(18).setCellValue(returned.format(formatter));
                } else {
                    row.createCell(18).setCellValue("Not Returned");
                }

                row.createCell(19).setCellValue(nullSafe(t.getRemarks()));
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

            PdfPTable table = new PdfPTable(20);
            table.setWidthPercentage(100);

            String[] headers = {
                    "Buy/Sell",
                    "Plant",
                    "Department",
                    "Location",
                    "Employee ID",
                    "Employee Name",
                    "IP Address",
                    "Item Code",
                    "Item Name",
                    "Item Make",
                    "Item Model",
                    "Item Serial",
                    "IMEI No",
                    "SIM No",
                    "PO No",
                    "Party Name",
                    "Status",
                    "Issued Date",
                    "Returned Date",
                    "Remarks"
            };

            for (String header : headers) {
                table.addCell(new Phrase(header));
            }

            for (TransactionHistory t : data) {

                table.addCell(nullSafe(t.getBuySell()));
                table.addCell(nullSafe(t.getPlant()));
                table.addCell(nullSafe(t.getDepartment()));
                table.addCell(nullSafe(t.getLocation()));
                table.addCell(nullSafe(t.getEmployeeCode()));
                table.addCell(nullSafe(t.getEmployeeName()));
                table.addCell(nullSafe(t.getIpAddress()));
                table.addCell(nullSafe(t.getItemCode()));
                table.addCell(nullSafe(t.getItemName()));
                table.addCell(nullSafe(t.getItemMake()));
                table.addCell(nullSafe(t.getItemModel()));
                table.addCell(nullSafe(t.getItemSerial()));
                table.addCell(nullSafe(t.getImeiNo()));
                table.addCell(nullSafe(t.getSimNo()));
                table.addCell(nullSafe(t.getPoNo()));
                table.addCell(nullSafe(t.getPartyName()));
                table.addCell(nullSafe(t.getStatus()));

                if (t.getIssuedDateTime() != null) {
                    LocalDateTime issued = java.sql.Timestamp
                            .valueOf(t.getIssuedDateTime())
                            .toLocalDateTime();
                    table.addCell(issued.format(formatter));
                } else {
                    table.addCell("");
                }

                if (t.getReturnedDateTime() != null) {
                    LocalDateTime returned =
                            java.sql.Timestamp
                                    .valueOf(t.getReturnedDateTime())
                                    .toLocalDateTime();
                    table.addCell(returned.format(formatter));
                } else {
                    table.addCell("Not Returned");
                }

                table.addCell(nullSafe(t.getRemarks()));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}