package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.InventoryItem;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class InventoryController {

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableColumn<InventoryItem, String> itemNameCol;

    @FXML
    private TableColumn<InventoryItem, String> stockCol;

    @FXML
    private Label totalItemsLabel;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private static final int MAX_VISIBLE_ROWS = 12;
    private static final double ROW_HEIGHT = 36;

    @FXML
    public void initialize() {
        inventoryTable.getStylesheets().add(
                getClass().getResource("/css/inventory.css").toExternalForm()
        );

        itemNameCol.setCellValueFactory(
                new PropertyValueFactory<>("itemName")
        );

        stockCol.setCellValueFactory(cell -> {

            InventoryItem item = cell.getValue();

            double stock = item.getStock();

            String formatted =
                    (stock == Math.floor(stock))
                            ? String.valueOf((int) stock)
                            : String.format("%.2f", stock);

            return new SimpleStringProperty(
                    formatted + " " + item.getUnit()
            );
        });

        loadInventory();

        inventoryTable.setRowFactory(tv -> {
            TableRow<InventoryItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    InventoryItem item = row.getItem();
                    openAvailableInventory(item.getItemName());
                }
            });
            return row;
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) inventoryTable.getScene().getWindow();
            stage.sizeToScene();
        });
    }

    private void loadInventory() {

        List<InventoryItem> list = transactionDAO.getInventory();

        inventoryTable.setItems(
                FXCollections.observableArrayList(list)
        );

        totalItemsLabel.setText(String.valueOf(list.size()));

        int visibleRows = Math.min(list.size(), MAX_VISIBLE_ROWS);
        double tableHeight = (visibleRows + 1) * ROW_HEIGHT;

        inventoryTable.setPrefHeight(tableHeight);
        inventoryTable.setMinHeight(tableHeight);
        inventoryTable.setMaxHeight(tableHeight);

        Platform.runLater(() -> {
            Stage stage = (Stage) inventoryTable.getScene().getWindow();
            stage.sizeToScene();
        });
    }

    private void openAvailableInventory(String itemName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/available-inventory.fxml"));
            Parent root = loader.load();

            AvailableInventoryController controller =
                    loader.getController();

            controller.loadData(itemName);

            Stage stage = new Stage();
            stage.setTitle(itemName + " Inventory");
            stage.setScene(new Scene(root, 700, 550));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}