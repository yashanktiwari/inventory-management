package com.inventory.util;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.TransactionHistory;
import javafx.concurrent.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ExcelImportTask extends Task<Integer> {

    private final File file;

    public ExcelImportTask(File file) {
        this.file = file;
    }

    @Override
    protected Integer call() throws Exception {

        TransactionDAO dao = new TransactionDAO();

        int importedCount = 0;

        Map<Integer, Integer> idMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Transactions");

            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            int totalRows = sheet.getLastRowNum();

            for (int i = 1; i <= totalRows; i++) {

                if (isCancelled()) break;

                Row row = sheet.getRow(i);
                if (row == null) continue;

                int excelTransactionId =
                        ExcelHelper.getDouble(row.getCell(0)).intValue();

                TransactionHistory t = new TransactionHistory();

                String buySell = ExcelHelper.getString(row.getCell(1));

                t.setBuySell(buySell);
                t.setPlant(ExcelHelper.getString(row.getCell(2)));
                t.setDepartment(ExcelHelper.getString(row.getCell(3)));
                t.setLocation(ExcelHelper.getString(row.getCell(4)));

                t.setEmployeeCode(ExcelHelper.getString(row.getCell(5)));
                t.setEmployeeName(ExcelHelper.getString(row.getCell(6)));

                t.setItemCode(ExcelHelper.getString(row.getCell(8)));
                t.setItemName(ExcelHelper.getString(row.getCell(9)));

                t.setItemMake(ExcelHelper.getString(row.getCell(10)));
                t.setItemModel(ExcelHelper.getString(row.getCell(11)));

                t.setItemSerial(ExcelHelper.getString(row.getCell(12)));
                t.setItemCondition(ExcelHelper.getString(row.getCell(13)));
                t.setItemLocation(ExcelHelper.getString(row.getCell(14)));
                t.setItemCategory(ExcelHelper.getString(row.getCell(15)));

                t.setImeiNo(ExcelHelper.getString(row.getCell(16)));
                t.setSimNo(ExcelHelper.getString(row.getCell(17)));

                t.setPoNo(ExcelHelper.getString(row.getCell(18)));
                t.setPartyName(ExcelHelper.getString(row.getCell(19)));

                String status = ExcelHelper.getString(row.getCell(20));

                t.setStatus(normalizeStatus(status));

                LocalDateTime issued =
                        ExcelHelper.getDateTime(row.getCell(21));

                if (issued == null)
                    issued = LocalDateTime.now();

                t.setIssuedDateTime(issued);

                t.setReturnedDateTime(
                        ExcelHelper.getDateTime(row.getCell(22)));

                t.setRemarks(ExcelHelper.getString(row.getCell(23)));
                t.setItemCount(ExcelHelper.getDouble(row.getCell(24)));
                t.setUnit(ExcelHelper.getString(row.getCell(25)));
                t.setLastModifiedBy(ExcelHelper.getString(row.getCell(26)));

                t.setAvailable(
                        "IN STOCK".equalsIgnoreCase(t.getStatus())
                                || "RETURNED".equalsIgnoreCase(t.getStatus())
                );

                int dbId = dao.insertTransactionFromExcel(t);

                if (dbId > 0) {
                    idMap.put(excelTransactionId, dbId);
                }

                importedCount++;

                updateProgress(i, totalRows);
                updateMessage("Importing row " + i + " of " + totalRows);
            }

            Sheet auditSheet = workbook.getSheet("Audit History");

            if (auditSheet != null) {

                int auditRows = auditSheet.getLastRowNum();

                for (int i = 1; i <= auditRows; i++) {

                    Row row = auditSheet.getRow(i);
                    if (row == null) continue;

                    int excelTransactionId =
                            ExcelHelper.getDouble(row.getCell(0)).intValue();

                    Integer newTransactionId =
                            idMap.get(excelTransactionId);

                    if (newTransactionId == null) continue;

                    String user =
                            ExcelHelper.getString(row.getCell(1));

                    LocalDateTime modifiedAt =
                            ExcelHelper.getDateTime(row.getCell(2));

                    String field =
                            ExcelHelper.getString(row.getCell(3));

                    String oldVal =
                            ExcelHelper.getString(row.getCell(4));

                    String newVal =
                            ExcelHelper.getString(row.getCell(5));

                    dao.insertAuditWithTime(
                            newTransactionId,
                            user,
                            modifiedAt,
                            field,
                            oldVal,
                            newVal
                    );
                }
            }
        }

        return importedCount;
    }

    private String normalizeStatus(String status) {

        if (status == null) return "";

        status = status.trim().toUpperCase();

        switch (status) {

            case "IN STOCK":
            case "IN_STOCK":
                return "IN STOCK";

            case "ISSUED":
                return "ISSUED";

            case "RETURNED":
                return "RETURNED";

            case "SCRAP":
            case "SCRAPPED":
                return "SCRAPPED";

            default:
                return status;
        }
    }
}