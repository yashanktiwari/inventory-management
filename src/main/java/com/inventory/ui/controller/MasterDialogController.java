package com.inventory.ui.controller;

import com.inventory.cache.MasterCache;
import com.inventory.dao.MasterDAO;
import com.inventory.model.master.*;
import com.inventory.util.AlertUtil;
import com.inventory.util.MasterExcelImportTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Optional;

public class MasterDialogController {

    @FXML private ComboBox<String> typeComboBox;
    @FXML private TableView masterTable;

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField makeField;
    @FXML private TextField modelField;

    @FXML private HBox codeBox;
    @FXML private HBox nameBox;
    @FXML private HBox makeBox;
    @FXML private HBox modelBox;

    @FXML private Button deleteButton;

    private final MasterDAO masterDAO = new MasterDAO();

    @FXML
    public void initialize() {

        typeComboBox.setItems(FXCollections.observableArrayList(
                "Item Code",
                "Employee Code",
                "Category",
                "Plant",
                "Department"
        ));

        typeComboBox.setValue("Item Code");

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null) return;

            updateFieldVisibility(newVal);
            updatePromptText(newVal);
            loadTable();
        });

        masterTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        deleteButton.disableProperty().bind(
                masterTable.getSelectionModel().selectedItemProperty().isNull()
        );

        enforceUpperCase(codeField);
        enforceUpperCase(nameField);
        enforceUpperCase(makeField);
        enforceUpperCase(modelField);

        // 🔹 IMPORTANT: Initialize first view
        updateFieldVisibility("Item Code");
        updatePromptText("Item Code");
        loadTable();
    }


    private void loadTable() {

        if (typeComboBox.getValue() == null) return;

        masterTable.getColumns().clear();

        switch (typeComboBox.getValue()) {

            case "Employee Code" -> {

                TableColumn<EmployeeMaster, String> codeCol =
                        new TableColumn<>("Employee Code");
                codeCol.setCellValueFactory(data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getEmployeeCode()));

                TableColumn<EmployeeMaster, String> nameCol =
                        new TableColumn<>("Employee Name");
                nameCol.setCellValueFactory(data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getEmployeeName()));

                masterTable.getColumns().addAll(codeCol, nameCol);

                masterTable.setItems(
                        FXCollections.observableArrayList(masterDAO.getAllEmployees())
                );
            }

            case "Item Code" -> {

                TableColumn<ItemMaster, String> codeCol =
                        new TableColumn<>("Item Code");
                codeCol.setCellValueFactory(data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getItemCode()));

                TableColumn<ItemMaster, String> nameCol =
                        new TableColumn<>("Item Name");
                nameCol.setCellValueFactory(data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getItemName()));

                masterTable.getColumns().addAll(codeCol, nameCol);

                masterTable.setItems(
                        FXCollections.observableArrayList(masterDAO.getAllItems())
                );
            }

            case "Category" -> {

                TableColumn<CategoryMaster, String> categoryCol = new TableColumn("Category");

                categoryCol.setCellValueFactory(data ->
                        new SimpleStringProperty(
                                ((CategoryMaster) data.getValue()).getCategoryName()
                        ));

                masterTable.getColumns().add(categoryCol);

                masterTable.setItems(
                        FXCollections.observableArrayList(masterDAO.getAllCategories())
                );
            }

            case "Plant" -> {

                TableColumn<PlantMaster, String> plantCol =
                        new TableColumn<>("Plant");

                plantCol.setCellValueFactory(data ->
                        new SimpleStringProperty(
                                data.getValue().getPlantName()
                        ));

                masterTable.getColumns().add(plantCol);

                masterTable.setItems(
                        FXCollections.observableArrayList(masterDAO.getAllPlants())
                );
            }

            case "Department" -> {

                TableColumn<DepartmentMaster, String> depCol =
                        new TableColumn<>("Department");

                depCol.setCellValueFactory(data ->
                        new SimpleStringProperty(
                                data.getValue().getDepartmentName()
                        ));

                masterTable.getColumns().add(depCol);

                masterTable.setItems(
                        FXCollections.observableArrayList(masterDAO.getAllDepartments())
                );
            }
        }
    }


    @FXML
    private void handleAdd() {
        String type = typeComboBox.getValue();

        if (type == null) {
            AlertUtil.showError("Validation Error", "Please select a master type.");
            return;
        }

        if (!validateFields(type)) {
            return;
        }

        switch(type) {
            case "Item Code" -> {
                ItemMaster item = new ItemMaster(
                        codeField.getText().trim().toUpperCase(),
                        nameField.getText().trim().toUpperCase(),
                        null
                );
                masterDAO.addItem(item);
            }
            case "Employee Code" -> {

                EmployeeMaster emp = new EmployeeMaster(
                        codeField.getText().trim().toUpperCase(),
                        nameField.getText().trim().toUpperCase()
                );
                masterDAO.addEmployee(emp);
            }

            case "Category" -> {
                masterDAO.addCategory(codeField.getText().trim().toUpperCase());
            }

            case "Plant" -> {
                masterDAO.addPlant(codeField.getText().trim().toUpperCase());
            }

            case "Department" -> {
                masterDAO.addDepartment(codeField.getText().trim().toUpperCase());
            }
        }
        MasterCache.loadCache();
        loadTable();
        clearFields();
    }


    @FXML
    private void handleDelete() {
        Object selected = masterTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError(
                    "Delete Error",
                    "Please select a row to delete."
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete selected record?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        switch (typeComboBox.getValue()) {
            case "Item Code" -> {
                ItemMaster item = (ItemMaster) selected;
                masterDAO.deleteItem(item.getItemCode());
            }

            case "Employee Code" -> {
                EmployeeMaster emp = (EmployeeMaster) selected;
                masterDAO.deleteEmployee(emp.getEmployeeCode());
            }

            case "Category" -> {
                CategoryMaster category = (CategoryMaster) selected;
                masterDAO.deleteCategory(category.getCategoryName());
            }

            case "Plant" -> {
                PlantMaster plant = (PlantMaster) selected;
                masterDAO.deletePlant(plant.getPlantName());
            }

            case "Department" -> {
                DepartmentMaster dep = (DepartmentMaster) selected;
                masterDAO.deleteDepartment(dep.getDepartmentName());
            }
        }
        // Reload cache
        MasterCache.loadCache();
        // Refresh table
        loadTable();
    }


    private void clearFields() {
        codeField.clear();
        nameField.clear();
        makeField.clear();
        modelField.clear();
    }

    private void updateFieldVisibility(String type) {
        switch (type) {
            case "Employee Code", "Item Code" -> {
                codeBox.setVisible(true);
                codeBox.setManaged(true);

                nameBox.setVisible(true);
                nameBox.setManaged(true);

                makeBox.setVisible(false);
                makeBox.setManaged(false);

                modelBox.setVisible(false);
                modelBox.setManaged(false);
            }

            case "Category", "Plant", "Department" -> {
                codeBox.setVisible(true);
                codeBox.setManaged(true);

                nameBox.setVisible(false);
                nameBox.setManaged(false);

                makeBox.setVisible(false);
                makeBox.setManaged(false);

                modelBox.setVisible(false);
                modelBox.setManaged(false);
            }

        }
    }


    private void updatePromptText(String type) {
        switch (type) {

            case "Employee Code" -> {
                codeField.setPromptText("Employee Code");
                nameField.setPromptText("Employee Name");
            }

            case "Item Code" -> {
                codeField.setPromptText("Item Code");
                nameField.setPromptText("Item Name");
//                makeField.setPromptText("Item Make");
//                modelField.setPromptText("Item Model");
            }

            case "Category" -> {
                codeField.setPromptText("Category Name");
            }

            case "Plant" -> {
                codeField.setPromptText("Plant Name");
            }

            case "Department" -> {
                codeField.setPromptText("Department Name");
            }
        }
    }

    private boolean validateFields(String type) {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String make = makeField.getText().trim();
        String model = modelField.getText().trim();

        switch (type) {

            case "Employee Code" -> {
                if (code.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Employee code cannot be empty.");
                    return false;
                }

                if (name.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Employee name cannot be empty.");
                    return false;
                }
            }

            case "Item Code" -> {
                if (code.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Item code cannot be empty.");
                    return false;
                }

                if (name.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Item name cannot be empty.");
                    return false;
                }
//
//                if (make.isEmpty()) {
//                    AlertUtil.showError("Validation Error", "Item make cannot be empty.");
//                    return false;
//                }
//
//                if (model.isEmpty()) {
//                    AlertUtil.showError("Validation Error", "Item model cannot be empty.");
//                    return false;
//                }
            }

            case "Category" -> {
                if (code.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Category name cannot be empty.");
                    return false;
                }
            }

            case "Plant" -> {
                if (code.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Plant name cannot be empty.");
                    return false;
                }
            }

            case "Department" -> {
                if (code.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Department name cannot be empty.");
                    return false;
                }
            }
        }

        return true;
    }

    private void enforceUpperCase(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String upper = newVal.toUpperCase();
            if (!upper.equals(newVal)) {
                field.setText(upper);
            }
        });
    }

//    @FXML
//    private void handleImportExcel() {
//
//        String type = typeComboBox.getValue();
//
//        if (type == null) {
//            AlertUtil.showError("Validation Error", "Please select a master type first.");
//            return;
//        }
//
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Select Excel File to Import");
//        fileChooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("Excel Files", ".xlsx", ".xls")
//        );
//
//        File file = fileChooser.showOpenDialog(typeComboBox.getScene().getWindow());
//
//        if (file == null) return;
//
//        String expectedColumns = getExpectedColumnsForType(type);
//
//        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
//        confirmAlert.setTitle("Import " + type);
//        confirmAlert.setHeaderText("Import " + type + " from Excel");
//        confirmAlert.setContentText(
//                "Expected Excel format:\n" +
//                        "First row: Column headers\n" +
//                        "Columns: " + expectedColumns + "\n\n" +
//                        "Duplicate entries will be ignored.\n\n" +
//                        "Continue with import?"
//        );
//
//        Optional<ButtonType> result = confirmAlert.showAndWait();
//
//        if (result.isEmpty() || result.get() != ButtonType.OK) {
//            return;
//        }
//
//        MasterExcelImportTask task = new MasterExcelImportTask(file, type);
//
//        ProgressDialog progressDialog = new ProgressDialog();
//        progressDialog.setTitle("Importing " + type);
//        progressDialog.setHeaderText("Importing data from Excel...");
//        progressDialog.progressProperty().bind(task.progressProperty());
//        progressDialog.messageProperty().bind(task.messageProperty());
//
//        task.setOnSucceeded(event -> {
//            progressDialog.close();
//
//            Integer count = task.getValue();
//
//            javafx.application.Platform.runLater(() -> {
//                MasterCache.loadCache();
//                loadTable();
//
//                if (count == 0) {
//                    Alert warningAlert = new Alert(Alert.AlertType.WARNING);
//                    warningAlert.setTitle("Import Complete");
//                    warningAlert.setHeaderText("No Records Imported");
//                    warningAlert.setContentText(
//                            "No records were imported. Possible reasons:\n" +
//                                    "- Excel file is empty or has no data rows\n" +
//                                    "- All records already exist in the database\n" +
//                                    "- Data format doesn't match expected columns"
//                    );
//                    warningAlert.getButtonTypes().setAll(ButtonType.OK);
//                    warningAlert.showAndWait();
//                } else {
//                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
//                    successAlert.setTitle("Import Complete");
//                    successAlert.setHeaderText("Import Successful");
//                    successAlert.setContentText(
//                            "Successfully imported " + count + " " + type + " records."
//                    );
//                    successAlert.getButtonTypes().setAll(ButtonType.OK);
//                    successAlert.showAndWait();
//                }
//            });
//        });
//
//        task.setOnFailed(event -> {
//            progressDialog.close();
//
//            Throwable ex = task.getException();
//
//            javafx.application.Platform.runLater(() -> {
//                AlertUtil.showError(
//                        "Import Failed",
//                        "Failed to import data: " + ex.getMessage()
//                );
//            });
//
//            ex.printStackTrace();
//        });
//
//        task.setOnCancelled(event -> {
//            progressDialog.close();
//
//            javafx.application.Platform.runLater(() -> {
//                AlertUtil.showError("Import Cancelled", "Import was cancelled by user.");
//            });
//        });
//
//        Thread thread = new Thread(task);
//        thread.setDaemon(true);
//        thread.start();
//
//        progressDialog.showAndWait();
//    }

    @FXML
    private void handleImportExcel() {

        String type = typeComboBox.getValue();

        if (type == null) {
            AlertUtil.showError("Validation Error", "Please select a master type first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel File to Import");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
        );

        File file = fileChooser.showOpenDialog(typeComboBox.getScene().getWindow());

        if (file == null) return;

        String expectedColumns = getExpectedColumnsForType(type);

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Import " + type);
        confirmAlert.setHeaderText("Import " + type + " from Excel");
        confirmAlert.setContentText(
                "Expected Excel format:\n" +
                "First row: Column headers\n" +
                "Columns: " + expectedColumns + "\n\n" +
                "Duplicate entries will be ignored.\n\n" +
                "Continue with import?"
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        MasterExcelImportTask task = new MasterExcelImportTask(file, type);

        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.setTitle("Importing " + type);
        progressDialog.setHeaderText("Importing data from Excel...");
        progressDialog.progressProperty().bind(task.progressProperty());
        progressDialog.messageProperty().bind(task.messageProperty());

        task.setOnSucceeded(event -> {
            progressDialog.close();

            Integer count = task.getValue();

            javafx.application.Platform.runLater(() -> {
                MasterCache.loadCache();
                loadTable();

                if (count == 0) {
                    Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                    warningAlert.setTitle("Import Complete");
                    warningAlert.setHeaderText("No Records Imported");
                    warningAlert.setContentText(
                            "No records were imported. Possible reasons:\n" +
                            "- Excel file is empty or has no data rows\n" +
                            "- All records already exist in the database\n" +
                            "- Data format doesn't match expected columns"
                    );
                    warningAlert.getButtonTypes().setAll(ButtonType.OK);
                    warningAlert.showAndWait();
                } else {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Import Complete");
                    successAlert.setHeaderText("Import Successful");
                    successAlert.setContentText(
                            "Successfully imported " + count + " " + type + " records."
                    );
                    successAlert.getButtonTypes().setAll(ButtonType.OK);
                    successAlert.showAndWait();
                }
            });
        });

        task.setOnFailed(event -> {
            progressDialog.close();

            Throwable ex = task.getException();

            javafx.application.Platform.runLater(() -> {
                AlertUtil.showError(
                        "Import Failed",
                        "Failed to import data: " + ex.getMessage()
                );
            });

            ex.printStackTrace();
        });

        task.setOnCancelled(event -> {
            progressDialog.close();

            javafx.application.Platform.runLater(() -> {
                AlertUtil.showError("Import Cancelled", "Import was cancelled by user.");
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        progressDialog.showAndWait();
    }

    private String getExpectedColumnsForType(String type) {
        return switch (type) {
            case "Item Code" -> "Item Code, Item Name";
            case "Employee Code" -> "Employee Code, Employee Name";
            case "Category" -> "Category Name";
            case "Plant" -> "Plant Name";
            case "Department" -> "Department Name";
            default -> "";
        };
    }

    private static class ProgressDialog extends Dialog<Void> {

        private final ProgressBar progressBar;
        private final Label messageLabel;

        public ProgressDialog() {
            setTitle("Progress");
            setHeaderText("Processing...");

            progressBar = new ProgressBar();
            progressBar.setPrefWidth(400);

            messageLabel = new Label();

            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
            content.getChildren().addAll(progressBar, messageLabel);
            content.setPadding(new javafx.geometry.Insets(20));

            getDialogPane().setContent(content);
            getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setOnAction(e -> close());
        }

        public javafx.beans.property.DoubleProperty progressProperty() {
            return progressBar.progressProperty();
        }

        public javafx.beans.property.StringProperty messageProperty() {
            return messageLabel.textProperty();
        }
    }
}
