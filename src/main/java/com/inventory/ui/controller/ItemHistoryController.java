package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.TransactionHistory;
import com.inventory.util.ExportUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    @FXML private TableColumn<TransactionHistory, Double> itemCountColumn;
    @FXML private TableColumn<TransactionHistory, String> unitColumn;

    @FXML private TableColumn<TransactionHistory, String> imeiColumn;
    @FXML private TableColumn<TransactionHistory, String> simColumn;

    @FXML private TableColumn<TransactionHistory, String> poColumn;
    @FXML private TableColumn<TransactionHistory, String> partyColumn;

    @FXML private TableColumn<TransactionHistory, String> statusColumn;
    @FXML private TableColumn<TransactionHistory, String> auditColumn;

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

    @FXML
    public void initialize() {

        serialColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        itemHistoryTable.getItems().indexOf(cellData.getValue()) + 1
                ).asObject()
        );

        buySellColumn.setCellValueFactory(new PropertyValueFactory<>("buySell"));
        buySellColumn.setCellFactory(column -> new TableCell<TransactionHistory, String>() {

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {

                    setText(value);
                    setAlignment(javafx.geometry.Pos.CENTER);

                    if ("Buy".equalsIgnoreCase(value)) {
                        setStyle("-fx-background-color:#d4edda; -fx-text-fill:black;");
                    } else if ("Sell".equalsIgnoreCase(value)) {
                        setStyle("-fx-background-color:#f8d7da; -fx-text-fill:black;");
                    }
                }
            }
        });

        plantColumn.setCellValueFactory(new PropertyValueFactory<>("plant"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemMakeColumn.setCellValueFactory(new PropertyValueFactory<>("itemMake"));
        itemModelColumn.setCellValueFactory(new PropertyValueFactory<>("itemModel"));
        itemSerialColumn.setCellValueFactory(new PropertyValueFactory<>("itemSerial"));

        itemCountColumn.setCellValueFactory(new PropertyValueFactory<>("itemCount"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        imeiColumn.setCellValueFactory(new PropertyValueFactory<>("imeiNo"));
        simColumn.setCellValueFactory(new PropertyValueFactory<>("simNo"));

        poColumn.setCellValueFactory(new PropertyValueFactory<>("poNo"));
        partyColumn.setCellValueFactory(new PropertyValueFactory<>("partyName"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {

                    setText(value);
                    setAlignment(javafx.geometry.Pos.CENTER);

                    switch (value.toLowerCase()) {

                        case "issued" ->
                                setStyle("-fx-background-color:#d6eaff; -fx-text-fill:black;");

                        case "returned" ->
                                setStyle("-fx-background-color:#d4edda; -fx-text-fill:black;");

                        case "scrap" ->
                                setStyle("-fx-background-color:#e0e0e0; -fx-text-fill:black;");

                        case "in stock" ->
                                setStyle("-fx-background-color:#fff3cd; -fx-text-fill:black;");
                    }
                }
            }
        });

        issuedColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getIssuedDateTime().format(formatter))
        );
        returnedColumn.setCellValueFactory(cellData -> {

            LocalDateTime returned = cellData.getValue().getReturnedDateTime();

            if (returned == null) {
                return new SimpleStringProperty("Not Returned");
            }

            return new SimpleStringProperty(returned.format(formatter));
        });

        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        auditColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLastModifiedBy()
                ));
    }

    public void loadHistory(String field, String value, String title) {

        titleLabel.setText("History for: " + value);

        List<TransactionHistory> historyList =
                transactionDAO.getTransactionsByField(field, value);

        itemHistoryTable.setItems(
                FXCollections.observableArrayList(historyList)
        );
    }

    private void centerAllColumns(TableView<TransactionHistory> table) {

        for (TableColumn<?, ?> column : table.getColumns()) {

            column.setCellFactory(col -> new TableCell() {

                @Override
                protected void updateItem(Object item, boolean empty) {

                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }

                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            });
        }
    }
}