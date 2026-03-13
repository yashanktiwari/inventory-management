package com.inventory.ui.controller;

import com.inventory.cache.MasterCache;
import com.inventory.dao.MasterDAO;
import com.inventory.model.master.CategoryMaster;
import com.inventory.model.master.EmployeeMaster;
import com.inventory.model.master.ItemMaster;
import com.inventory.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

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
                "Category"
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

                TableColumn<ItemMaster, String> makeCol =
                        new TableColumn<>("Make");
                makeCol.setCellValueFactory(data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getItemMake()));

                TableColumn<ItemMaster, String> modelCol =
                        new TableColumn<>("Model");
                modelCol.setCellValueFactory(data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getItemModel()));

                masterTable.getColumns().addAll(codeCol, nameCol, makeCol, modelCol);

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
                        makeField.getText().trim().toUpperCase(),
                        modelField.getText().trim().toUpperCase(),
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
                String category = selected.toString();
                masterDAO.deleteCategory(category);
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
            case "Employee Code" -> {
                codeBox.setVisible(true);
                codeBox.setManaged(true);

                nameBox.setVisible(true);
                nameBox.setManaged(true);

                makeBox.setVisible(false);
                makeBox.setManaged(false);

                modelBox.setVisible(false);
                modelBox.setManaged(false);
            }

            case "Item Code" -> {
                codeBox.setVisible(true);
                codeBox.setManaged(true);

                nameBox.setVisible(true);
                nameBox.setManaged(true);

                makeBox.setVisible(true);
                makeBox.setManaged(true);

                modelBox.setVisible(true);
                modelBox.setManaged(true);
            }

            case "Category" -> {
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
                makeField.setPromptText("Item Make");
                modelField.setPromptText("Item Model");
            }

            case "Category" -> {
                codeField.setPromptText("Category Name");
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

                if (make.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Item make cannot be empty.");
                    return false;
                }

                if (model.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Item model cannot be empty.");
                    return false;
                }
            }

            case "Category" -> {
                if (code.isEmpty()) {
                    AlertUtil.showError("Validation Error", "Category name cannot be empty.");
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

}
