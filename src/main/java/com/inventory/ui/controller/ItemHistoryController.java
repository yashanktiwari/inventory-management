package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.TransactionHistory;
import com.inventory.util.ExportUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ItemHistoryController {

    @FXML private Label titleLabel;

    @FXML private TableView<TransactionHistory> itemHistoryTable;

    @FXML private TableColumn<TransactionHistory, Integer> serialColumn;

    @FXML private TableColumn<TransactionHistory, String> issuedColumn;
    @FXML private TableColumn<TransactionHistory, String> returnedColumn;
    @FXML private TableColumn<TransactionHistory, String> remarksColumn;
    @FXML private TableColumn<TransactionHistory, String> buySellColumn;
    @FXML private TableColumn<TransactionHistory, String> plantColumn;
    @FXML private TableColumn<TransactionHistory, String> departmentColumn;
    @FXML private TableColumn<TransactionHistory, String> locationColumn;

    @FXML private TableColumn<TransactionHistory, String> employeeIdColumn;
    @FXML private TableColumn<TransactionHistory, String> employeeNameColumn;

    @FXML private TableColumn<TransactionHistory, String> ipColumn;

    @FXML private TableColumn<TransactionHistory, String> itemCodeColumn;
    @FXML private TableColumn<TransactionHistory, String> itemNameColumn;
    @FXML private TableColumn<TransactionHistory, String> itemMakeColumn;
    @FXML private TableColumn<TransactionHistory, String> itemModelColumn;
    @FXML private TableColumn<TransactionHistory, String> itemSerialColumn;

    @FXML private TableColumn<TransactionHistory, String> imeiColumn;
    @FXML private TableColumn<TransactionHistory, String> simColumn;

    @FXML private TableColumn<TransactionHistory, String> poColumn;
    @FXML private TableColumn<TransactionHistory, String> partyColumn;

    @FXML private TableColumn<TransactionHistory, String> statusColumn;

    @FXML
    private void handleExportExcel() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(itemHistoryTable.getScene().getWindow());

        if (file != null) {
            ExportUtil.exportToExcel(
                    itemHistoryTable.getItems(),
                    file.getAbsolutePath()
            );
        }
    }

    @FXML
    private void handleExportPDF() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(itemHistoryTable.getScene().getWindow());

        if (file != null) {
            ExportUtil.exportToPDF(
                    itemHistoryTable.getItems(),
                    file.getAbsolutePath()
            );
        }
    }

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public void loadItemHistory(String itemId, String itemName) {

        titleLabel.setText("History for Item: " + itemId + " (" + itemName + ")");

        serialColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        itemHistoryTable.getItems().indexOf(cellData.getValue()) + 1
                ).asObject()
        );

        buySellColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getBuySell()));

        plantColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPlant()));

        departmentColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDepartment()));

        locationColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getLocation()));

        employeeIdColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEmployeeId()));

        employeeNameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEmployeeName()));

        ipColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getIpAddress()));

        itemCodeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemCode()));

        itemNameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemName()));

        itemMakeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemMake()));

        itemModelColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemModel()));

        itemSerialColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemSerial()));

        imeiColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getImeiNo()));

        simColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSimNo()));

        poColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPoNo()));

        partyColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPartyName()));

        statusColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus()));

        issuedColumn.setCellValueFactory(cellData -> {

            String raw = cellData.getValue().getIssuedDateTime();

            if (raw == null) return new SimpleStringProperty("");

            LocalDateTime dateTime = LocalDateTime.parse(raw);

            return new SimpleStringProperty(
                    dateTime.format(formatter)
            );
        });

        returnedColumn.setCellValueFactory(cellData -> {

            String raw = cellData.getValue().getReturnedDateTime();

            if (raw == null)
                return new SimpleStringProperty("Not Returned");

            LocalDateTime dateTime = LocalDateTime.parse(raw);

            return new SimpleStringProperty(
                    dateTime.format(formatter)
            );
        });

        remarksColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRemarks())
        );

        itemHistoryTable.setItems(
                FXCollections.observableArrayList(
                        transactionDAO.getTransactionsByItemCode(itemId)
                )
        );
    }
}