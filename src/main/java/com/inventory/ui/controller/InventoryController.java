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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.util.List;

public class InventoryController {

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableColumn<InventoryItem, String> itemNameCol;

    @FXML
    private TableColumn<InventoryItem, String> stockCol;

    @FXML
    private TableColumn<InventoryItem, Double> minimumCol;

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

        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        inventoryTable.setRowFactory(tv -> {

            TableRow<InventoryItem> row = new TableRow<>() {

                @Override
                protected void updateItem(InventoryItem item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setStyle("");
                    }
                    else if (item.getMinimumStock() >= 0 &&
                            item.getStock() < item.getMinimumStock()) {

                        setStyle("-fx-background-color: #ffe6e6;");
                    }
                    else {
                        setStyle("");
                    }
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    InventoryItem item = row.getItem();
                    openAvailableInventory(item.getItemName());
                }
            });

            return row;
        });



        Label text = new Label("No inventory items available");
        text.setStyle(
                "-fx-text-fill:#6b7c93;" +
                        "-fx-font-size:14px;" +
                        "-fx-font-style:italic;"
        );

        VBox emptyBox = new VBox(text);
        emptyBox.setStyle(
                "-fx-alignment:center;" +
                        "-fx-padding:40;"
        );

        inventoryTable.setPlaceholder(emptyBox);

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

        minimumCol.setCellValueFactory(
                new PropertyValueFactory<>("minimumStock")
        );

        minimumCol.setCellFactory(col -> {

            TextFieldTableCell<InventoryItem, Double> cell =
                    new TextFieldTableCell<>(new DoubleStringConverter());

            cell.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && cell.isEditing()) {
                    cell.commitEdit(cell.getItem());
                }
            });

            return cell;
        });

        minimumCol.setOnEditCommit(event -> {

            InventoryItem item = event.getRowValue();

            double newValue = event.getNewValue();

            item.setMinimumStock(newValue);

            transactionDAO.updateMinimumStock(item.getItemName(), newValue);
        });

        inventoryTable.setEditable(true);

        loadInventory();

        Platform.runLater(() -> {
            Stage stage = (Stage) inventoryTable.getScene().getWindow();
            stage.sizeToScene();
            inventoryTable.refresh();
            inventoryTable.getColumns().forEach(col -> {
                col.setPrefWidth(col.getWidth());
            });

        });
    }

    private void loadInventory() {

        List<InventoryItem> list = transactionDAO.getInventory();

        inventoryTable.setItems(
                FXCollections.observableArrayList(list)
        );

        totalItemsLabel.setText(String.valueOf(list.size()));
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
            stage.initOwner(stage.getOwner());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}