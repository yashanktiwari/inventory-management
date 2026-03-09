package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.TransactionHistory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AvailableInventoryController {

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<TransactionHistory> table;

    @FXML
    private TableColumn<TransactionHistory, Void> rowNumberCol;

    @FXML
    private TableColumn<TransactionHistory, String> serialCol;

    @FXML
    private TableColumn<TransactionHistory, String> makeCol;

    @FXML
    private TableColumn<TransactionHistory, String> modelCol;

    @FXML
    private TableColumn<TransactionHistory, Double> countCol;

    @FXML
    private TableColumn<TransactionHistory, String> unitCol;

    @FXML
    private TableColumn<TransactionHistory, LocalDateTime> issuedDateCol;

    private final TransactionDAO dao = new TransactionDAO();

    public void loadData(String itemName) {

        List<TransactionHistory> list =
                dao.getAvailableSerialItems(itemName);

        table.setItems(FXCollections.observableArrayList(list));

        if (!list.isEmpty()) {
            double count = 0;
            for(TransactionHistory item : list) {
                count += item.getItemCount();
            }
            titleLabel.setText(itemName + " Inventory (" + count + ")");
        } else {
            titleLabel.setText(itemName + " Inventory");
        }

        rowNumberCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        serialCol.setCellValueFactory(
                new PropertyValueFactory<>("itemSerial"));

        makeCol.setCellValueFactory(
                new PropertyValueFactory<>("itemMake"));

        modelCol.setCellValueFactory(
                new PropertyValueFactory<>("itemModel"));

        serialCol.setCellValueFactory(
                new PropertyValueFactory<>("itemSerial"));

        makeCol.setCellValueFactory(
                new PropertyValueFactory<>("itemMake"));

        modelCol.setCellValueFactory(
                new PropertyValueFactory<>("itemModel"));

        countCol.setCellValueFactory(
                new PropertyValueFactory<>("itemCount"));

        unitCol.setCellValueFactory(
                new PropertyValueFactory<>("unit"));

        issuedDateCol.setCellValueFactory(
                new PropertyValueFactory<>("issuedDateTime"));
        issuedDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(
                            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")
                    ));
                }
            }
        });

        table.setItems(
                FXCollections.observableArrayList(list));
    }
}