package com.inventory.ui.controller;

import com.inventory.dao.IndentDAO;
import com.inventory.dao.TransactionDAO;
import com.inventory.model.InventoryItem;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private TableColumn<InventoryItem, String> itemCodeCol;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private VBox indentCard;

    @FXML
    private Label indentCountLabel;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final IndentDAO indentDAO = new IndentDAO();
    private static final int MAX_VISIBLE_ROWS = 12;
    private static final double ROW_HEIGHT = 36;
    private FilteredList<InventoryItem> filterPipeline;


    @FXML
    public void initialize() {
        inventoryTable.getStylesheets().add(
                getClass().getResource("/css/inventory.css").toExternalForm()
        );

        indentCard.setOnMouseClicked(e -> openIndentList());

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

                        // Low stock highlight
                        setStyle("-fx-background-color: #ffe6e6;");
                    }
                    else {
                        setStyle("");
                    }
                }
            };

            ContextMenu menu = new ContextMenu();
            MenuItem addIndent = new MenuItem("Add Indent");

            menu.getItems().add(addIndent);

            addIndent.setOnAction(e -> {

                InventoryItem item = row.getItem();
                if (item == null) return;

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Add Indent");
                dialog.setHeaderText(item.getItemName());
                dialog.setContentText("Enter Quantity:");

                dialog.showAndWait().ifPresent(value -> {

                    try {

                        double qty = Double.parseDouble(value);

                        indentDAO.insertIndent(
                                item.getItemCode(),
                                item.getItemName(),
                                qty
                        );

                        loadIndentCount(); // refresh dashboard card

                    } catch (Exception ex) {

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Invalid quantity");
                        alert.show();
                    }
                });
            });

            row.setOnContextMenuRequested(event -> {

                if (!row.isEmpty()) {
                    menu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

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

        itemCodeCol.setCellValueFactory(
                new PropertyValueFactory<>("itemCode")
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

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {

            if (filterPipeline == null) return;

            filterPipeline.setPredicate(item -> {

                if (newValue == null || newValue.isBlank()) {
                    return true;
                }

                String keyword = newValue.toLowerCase();

                return
                        (item.getItemCode() != null &&
                                item.getItemCode().toLowerCase().contains(keyword))
                                ||
                                (item.getItemName() != null &&
                                        item.getItemName().toLowerCase().contains(keyword));
            });
        });

        loadIndentCount();

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

        filterPipeline = new FilteredList<>(
                FXCollections.observableArrayList(list), p -> true
        );

        inventoryTable.setItems(filterPipeline);

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

    private void openIndentList() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/indent-list-view.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Indent List");
            stage.setScene(new Scene(root));

            stage.initOwner(inventoryTable.getScene().getWindow());

            // 🔥 Refresh indent count when window closes
            stage.setOnHidden(e -> loadIndentCount());

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadIndentCount() {

        int count = indentDAO.getIndentCount();

        indentCountLabel.setText(String.valueOf(count));
    }
}