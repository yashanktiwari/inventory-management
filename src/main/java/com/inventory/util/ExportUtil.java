package com.inventory.util;

import com.inventory.model.AuditEntry;
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

            // ================= TRANSACTION SHEET =================
            Sheet sheet = workbook.createSheet("Transactions");

            Row header = sheet.createRow(0);

            String[] columns = {
                    "Transaction ID",
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
                    "Item Condition",
                    "Item Location",
                    "Item Category",
                    "IMEI No",
                    "SIM No",
                    "PO No",
                    "Party Name",
                    "Status",
                    "Issued Date",
                    "Returned Date",
                    "Remarks",
                    "Item Count",
                    "Unit"
            };

            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;

            for (TransactionHistory t : data) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(t.getTransactionId());
                row.createCell(1).setCellValue(nullSafe(t.getBuySell()));
                row.createCell(2).setCellValue(nullSafe(t.getPlant()));
                row.createCell(3).setCellValue(nullSafe(t.getDepartment()));
                row.createCell(4).setCellValue(nullSafe(t.getLocation()));
                row.createCell(5).setCellValue(nullSafe(t.getEmployeeCode()));
                row.createCell(6).setCellValue(nullSafe(t.getEmployeeName()));
                row.createCell(7).setCellValue(nullSafe(t.getIpAddress()));
                row.createCell(8).setCellValue(nullSafe(t.getItemCode()));
                row.createCell(9).setCellValue(nullSafe(t.getItemName()));
                row.createCell(10).setCellValue(nullSafe(t.getItemMake()));
                row.createCell(11).setCellValue(nullSafe(t.getItemModel()));
                row.createCell(12).setCellValue(nullSafe(t.getItemSerial()));
                row.createCell(13).setCellValue(nullSafe(t.getItemCondition()));
                row.createCell(14).setCellValue(nullSafe(t.getItemLocation()));
                row.createCell(15).setCellValue(nullSafe(t.getItemCategory()));
                row.createCell(16).setCellValue(nullSafe(t.getImeiNo()));
                row.createCell(17).setCellValue(nullSafe(t.getSimNo()));
                row.createCell(18).setCellValue(nullSafe(t.getPoNo()));
                row.createCell(19).setCellValue(nullSafe(t.getPartyName()));
                row.createCell(20).setCellValue(nullSafe(t.getStatus()));

                if (t.getIssuedDateTime() != null) {
                    row.createCell(21)
                            .setCellValue(t.getIssuedDateTime().format(formatter));
                }

                if (t.getReturnedDateTime() != null) {
                    row.createCell(22)
                            .setCellValue(t.getReturnedDateTime().format(formatter));
                } else {
                    row.createCell(22).setCellValue("Not Returned");
                }

                row.createCell(23).setCellValue(nullSafe(t.getRemarks()));

                if (t.getItemCount() != null) {
                    row.createCell(24).setCellValue(t.getItemCount());
                }

                row.createCell(24).setCellValue(nullSafe(t.getUnit()));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ================= AUDIT SHEET =================

            Sheet auditSheet = workbook.createSheet("Audit History");

            Row auditHeader = auditSheet.createRow(0);

            String[] auditColumns = {
                    "Transaction ID",
                    "Modified By",
                    "Modified At",
                    "Field",
                    "Old Value",
                    "New Value"
            };

            for (int i = 0; i < auditColumns.length; i++) {
                auditHeader.createCell(i).setCellValue(auditColumns[i]);
            }

            int auditRow = 1;

            for (TransactionHistory t : data) {

                if (t.getAuditEntries() == null) continue;

                for (AuditEntry audit : t.getAuditEntries()) {

                    Row row = auditSheet.createRow(auditRow++);

                    row.createCell(0).setCellValue(t.getTransactionId());
                    row.createCell(1).setCellValue(nullSafe(audit.getModifiedBy()));

                    if (audit.getModifiedAt() != null) {
                        row.createCell(2)
                                .setCellValue(audit.getModifiedAt().format(formatter));
                    }

                    row.createCell(3).setCellValue(nullSafe(audit.getFieldName()));
                    row.createCell(4).setCellValue(nullSafe(audit.getOldValue()));
                    row.createCell(5).setCellValue(nullSafe(audit.getNewValue()));
                }
            }

            for (int i = 0; i < auditColumns.length; i++) {
                auditSheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.showError(
                    "Export Failed",
                    "Excel export failed:\n" + e.getMessage()
            );
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
                    "Item Condition",
                    "Item Location",
                    "Item Category",
                    "IMEI No",
                    "SIM No",
                    "PO No",
                    "Party Name",
                    "Status",
                    "Issued Date",
                    "Returned Date",
                    "Remarks",
                    "Item Count",
                    "Unit"
            };

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

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
                table.addCell(nullSafe(t.getItemCondition()));
                table.addCell(nullSafe(t.getItemLocation()));
                table.addCell(nullSafe(t.getItemCategory()));
                table.addCell(nullSafe(t.getImeiNo()));
                table.addCell(nullSafe(t.getSimNo()));
                table.addCell(nullSafe(t.getPoNo()));
                table.addCell(nullSafe(t.getPartyName()));
                table.addCell(nullSafe(t.getStatus()));

                if (t.getIssuedDateTime() != null) {
                    table.addCell(t.getIssuedDateTime().format(formatter));
                } else {
                    table.addCell("");
                }

                if (t.getReturnedDateTime() != null) {
                    table.addCell(t.getReturnedDateTime().format(formatter));
                } else {
                    table.addCell("Not Returned");
                }

                table.addCell(nullSafe(t.getRemarks()));

                if (t.getItemCount() != null) {
                    table.addCell(String.valueOf(t.getItemCount()));
                } else {
                    table.addCell("");
                }

                table.addCell(nullSafe(t.getUnit()));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.showError(
                    "Export Failed",
                    "PDF export failed:\n" + e.getMessage()
            );
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}