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
    @FXML private TableColumn<TransactionHistory, String> employeeColumn;
    @FXML private TableColumn<TransactionHistory, String> personColumn;
    @FXML private TableColumn<TransactionHistory, String> issuedColumn;
    @FXML private TableColumn<TransactionHistory, String> returnedColumn;
    @FXML private TableColumn<TransactionHistory, String> remarksColumn;

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

        employeeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmployeeId())
        );

        personColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPersonName())
        );

        issuedColumn.setCellValueFactory(cellData -> {
            String raw = cellData.getValue().getIssuedDateTime();
            LocalDateTime dateTime = LocalDateTime.parse(raw);
            return new SimpleStringProperty(dateTime.format(formatter));
        });

        returnedColumn.setCellValueFactory(cellData -> {
            String raw = cellData.getValue().getReturnedDateTime();
            if (raw == null) return new SimpleStringProperty("Not Returned");

            LocalDateTime dateTime = LocalDateTime.parse(raw);
            return new SimpleStringProperty(dateTime.format(formatter));
        });

        remarksColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRemarks())
        );

        itemHistoryTable.setItems(
                FXCollections.observableArrayList(
                        transactionDAO.getTransactionsByItemId(itemId)
                )
        );
    }
}