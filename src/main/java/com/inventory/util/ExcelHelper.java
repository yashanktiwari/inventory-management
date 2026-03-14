package com.inventory.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExcelHelper {

    public static String getString(Cell cell) {

        if (cell == null) return "";

        return switch (cell.getCellType()) {

            case STRING ->
                    cell.getStringCellValue().trim().toUpperCase();

            case NUMERIC ->
                    String.valueOf((long) cell.getNumericCellValue()).toUpperCase();

            case BOOLEAN ->
                    String.valueOf(cell.getBooleanCellValue()).toUpperCase();

            default -> "";
        };
    }

    public static Double getDouble(Cell cell) {

        if (cell == null) return 0.0;

        try {

            switch (cell.getCellType()) {

                case NUMERIC:
                    return cell.getNumericCellValue();

                case STRING:
                    String value = cell.getStringCellValue().trim();

                    if (value.isEmpty()) return 0.0;

                    return Double.parseDouble(value);

                default:
                    return 0.0;
            }

        } catch (Exception e) {
            return 0.0;
        }
    }

    public static LocalDateTime getDateTime(Cell cell) {

        if (cell == null) return null;

        try {

            if (cell.getCellType() == CellType.NUMERIC &&
                    DateUtil.isCellDateFormatted(cell)) {

                return cell.getLocalDateTimeCellValue();
            }

            if (cell.getCellType() == CellType.STRING) {

                String value = cell.getStringCellValue();

                if (value == null || value.isBlank()) return null;

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

                return LocalDateTime.parse(value, formatter);
            }

        } catch (Exception ignored) {}

        return null;
    }
}