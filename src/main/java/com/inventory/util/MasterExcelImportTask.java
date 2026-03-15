package com.inventory.util;

import com.inventory.dao.MasterDAO;
import com.inventory.model.master.*;
import javafx.concurrent.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MasterExcelImportTask extends Task<Integer> {

    private final File file;
    private final String masterType;

    public MasterExcelImportTask(File file, String masterType) {
        this.file = file;
        this.masterType = masterType;
    }

    @Override
    protected Integer call() throws Exception {

        MasterDAO dao = new MasterDAO();
        int importedCount = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            validateHeader(headerRow);
            int totalRows = sheet.getLastRowNum();

            if (totalRows == 0) {
                updateMessage("No data found in Excel file");
                return 0;
            }

            switch (masterType) {

                case "Item Code" -> {
                    List<ItemMaster> items = new ArrayList<>();

                    for (int i = 1; i <= totalRows; i++) {
                        if (isCancelled()) break;

                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String itemCode = ExcelHelper.getString(row.getCell(0));
                        String itemName = ExcelHelper.getString(row.getCell(1));

                        if (itemCode.isEmpty()) continue;

                        items.add(new ItemMaster(itemCode, itemName, null));

                        updateProgress(i, totalRows);
                        updateMessage("Processing row " + i + " of " + totalRows);
                    }

                    importedCount = dao.bulkInsertItems(items);
                }

                case "Employee Code" -> {
                    List<EmployeeMaster> employees = new ArrayList<>();

                    for (int i = 1; i <= totalRows; i++) {
                        if (isCancelled()) break;

                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String empCode = ExcelHelper.getString(row.getCell(0));
                        String empName = ExcelHelper.getString(row.getCell(1));

                        if (empCode.isEmpty()) continue;

                        employees.add(new EmployeeMaster(empCode, empName));

                        updateProgress(i, totalRows);
                        updateMessage("Processing row " + i + " of " + totalRows);
                    }

                    importedCount = dao.bulkInsertEmployees(employees);
                }

                case "Category" -> {
                    List<String> categories = new ArrayList<>();

                    for (int i = 1; i <= totalRows; i++) {
                        if (isCancelled()) break;

                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String category = ExcelHelper.getString(row.getCell(0));

                        if (category.isEmpty()) continue;

                        categories.add(category);

                        updateProgress(i, totalRows);
                        updateMessage("Processing row " + i + " of " + totalRows);
                    }

                    importedCount = dao.bulkInsertCategories(categories);
                }

                case "Plant" -> {
                    List<String> plants = new ArrayList<>();

                    for (int i = 1; i <= totalRows; i++) {
                        if (isCancelled()) break;

                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String plant = ExcelHelper.getString(row.getCell(0));

                        if (plant.isEmpty()) continue;

                        plants.add(plant);

                        updateProgress(i, totalRows);
                        updateMessage("Processing row " + i + " of " + totalRows);
                    }

                    importedCount = dao.bulkInsertPlants(plants);
                }

                case "Department" -> {
                    List<String> departments = new ArrayList<>();

                    for (int i = 1; i <= totalRows; i++) {
                        if (isCancelled()) break;

                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String department = ExcelHelper.getString(row.getCell(0));

                        if (department.isEmpty()) continue;

                        departments.add(department);

                        updateProgress(i, totalRows);
                        updateMessage("Processing row " + i + " of " + totalRows);
                    }

                    importedCount = dao.bulkInsertDepartments(departments);
                }

                case "Party" -> {
                    List<String> parties = new ArrayList<>();

                    for (int i = 1; i <= totalRows; i++) {
                        if (isCancelled()) break;

                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String party = ExcelHelper.getString(row.getCell(0));

                        if (party.isEmpty()) continue;

                        parties.add(party);

                        updateProgress(i, totalRows);
                        updateMessage("Processing row " + i + " of " + totalRows);
                    }

                    importedCount = dao.bulkInsertParties(parties);
                }
            }
        }

        return importedCount;
    }

    private void validateHeader(Row headerRow) {

        if (headerRow == null) {
            throw new RuntimeException("Excel file is missing header row.");
        }

        String col1 = ExcelHelper.getString(headerRow.getCell(0));
        String col2 = ExcelHelper.getString(headerRow.getCell(1));

        System.out.println("Validating Excel header for master type: " + masterType);
        switch (masterType) {

            case "Item Code" -> {
                if (!"ITEM CODE".equalsIgnoreCase(col1) ||
                        !"ITEM NAME".equalsIgnoreCase(col2)) {
                    throw new IllegalArgumentException(
                            "Invalid Excel format. Expected columns: Item Code, Item Name"
                    );
                }
            }

            case "Employee Code" -> {
                if (!"EMPLOYEE CODE".equalsIgnoreCase(col1) ||
                        !"EMPLOYEE NAME".equalsIgnoreCase(col2)) {
                    throw new IllegalArgumentException(
                            "Invalid Excel format. Expected columns: Employee Code, Employee Name"
                    );
                }
            }

            case "Category" -> {
                if (!"CATEGORY NAME".equalsIgnoreCase(col1)) {
                    System.out.println("Header validation failed. Throwing exception.");
                    throw new IllegalArgumentException(
                            "Invalid Excel format. Expected column: Category Name"
                    );
                }
            }

            case "Plant" -> {
                if (!"PLANT NAME".equalsIgnoreCase(col1)) {
                    throw new IllegalArgumentException(
                            "Invalid Excel format. Expected column: Plant Name"
                    );
                }
            }

            case "Department" -> {
                if (!"DEPARTMENT NAME".equalsIgnoreCase(col1)) {
                    throw new IllegalArgumentException(
                            "Invalid Excel format. Expected column: Department Name"
                    );
                }
            }

            case "Party" -> {
                if (!"PARTY NAME".equalsIgnoreCase(col1)) {
                    throw new IllegalArgumentException(
                            "Invalid Excel format. Expected column: Party Name"
                    );
                }
            }
        }
    }
}
