package com.inventory.util;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.TransactionHistory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ExcelImportUtil {

    public static void importTransactions(File file) throws Exception {

        TransactionDAO dao = new TransactionDAO();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);

            Map<String, Integer> headerMap = new HashMap<>();

            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue()
                        .trim()
                        .toUpperCase()
                        .replace(" ", "_");
                headerMap.put(header, cell.getColumnIndex());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                TransactionHistory t = new TransactionHistory();

                String buySell = ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("BUYSELL", -1))
                );

                t.setBuySell(buySell);

                t.setPlant(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("PLANT", -1))
                ));

                t.setDepartment(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("DEPARTMENT", -1))
                ));

                t.setLocation(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("LOCATION", -1))
                ));

                t.setEmployeeCode(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("EMPLOYEE_ID", -1))
                ));

                t.setEmployeeName(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("EMPLOYEE_NAME", -1))
                ));

                t.setIpAddress(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("IP_ADDRESS", -1))
                ));

                t.setItemCode(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("ITEM_CODE", -1))
                ));

                t.setItemName(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("ITEM_NAME", -1))
                ));

                t.setItemMake(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("ITEM_MAKE", -1))
                ));

                t.setItemModel(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("ITEM_MODEL", -1))
                ));

                t.setItemSerial(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("ITEM_SERIAL", -1))
                ));

                t.setItemCondition(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("ITEM_CONDITION", -1))
                ));

                t.setItemLocation(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("ITEM_LOCATION", -1))
                ));

                t.setItemCategory(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("ITEM_CATEGORY", -1))
                ));

                t.setImeiNo(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("IMEI_NO", -1))
                ));

                t.setSimNo(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("SIM_NO", -1))
                ));

                t.setPoNo(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("PO_NO", -1))
                ));

                t.setPartyName(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("PARTY_NAME", -1))
                ));

                t.setRemarks(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("REMARKS", -1))
                ));

                Integer countIndex = headerMap.get("ITEM_COUNT");
                if (countIndex != null) {
                    Cell countCell = row.getCell(countIndex);

                    if (countCell != null) {
                        try {
                            t.setItemCount(ExcelHelper.getDouble(countCell));
                        } catch (Exception e) {
                            t.setItemCount(0.0);
                        }
                    }
                }

                t.setUnit(ExcelHelper.getString(
                        row.getCell(headerMap.getOrDefault("UNIT", -1))
                ));

                t.setIssuedDateTime(LocalDateTime.now());
                t.setReturnedDateTime(null);

                if ("BUY".equalsIgnoreCase(buySell)) {

                    t.setStatus("IN STOCK");
                    t.setAvailable(true);
                    t.setParentTransactionId(null);

                } else if ("SELL".equalsIgnoreCase(buySell)) {

                    Integer parentId = dao.findAvailableParent(
                            t.getItemCode(),
                            t.getItemSerial()
                    );

                    if (parentId == null) {
                        System.out.println("Row " + i + " skipped. No stock available.");
                        continue;
                    }

                    t.setParentTransactionId(parentId);
                    t.setStatus("ISSUED");
                    t.setAvailable(false);
                }

                dao.insertTransactionFromExcel(t);
            }
        }
    }

    private static Map<String, Integer> readHeaderMap(Row headerRow) {

        Map<String, Integer> headerMap = new HashMap<>();

        for (Cell cell : headerRow) {

            String header = cell.getStringCellValue()
                    .trim()
                    .toUpperCase();

            headerMap.put(header, cell.getColumnIndex());
        }

        return headerMap;
    }

    private static String getValue(Row row, Map<String,Integer> map, String column) {

        Integer index = map.get(column);

        if (index == null) return "";

        return ExcelHelper.getString(row.getCell(index));
    }

    private static Double getDouble(Row row, Map<String,Integer> map, String column) {

        Integer index = map.get(column);

        if (index == null) return 0.0;

        return ExcelHelper.getDouble(row.getCell(index));
    }

    public static void importAuditSheet(Workbook workbook) {

        Sheet auditSheet = workbook.getSheet("Audit History");

        if (auditSheet == null) return;

        TransactionDAO dao = new TransactionDAO();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

        for (int i = 1; i <= auditSheet.getLastRowNum(); i++) {

            Row row = auditSheet.getRow(i);
            if (row == null) continue;

            int transactionId = (int) row.getCell(0).getNumericCellValue();

            String user = ExcelHelper.getString(row.getCell(1));
            String field = ExcelHelper.getString(row.getCell(3));
            String oldVal = ExcelHelper.getString(row.getCell(4));
            String newVal = ExcelHelper.getString(row.getCell(5));

            dao.insertAudit(transactionId, user, field, oldVal, newVal);
        }
    }
}