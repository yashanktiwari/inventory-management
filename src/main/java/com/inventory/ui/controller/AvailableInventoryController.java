package com.inventory.ui.controller;

import com.inventory.dao.IndentDAO;
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
    private TableColumn<TransactionHistory, String> itemConditionCol;

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
    private final IndentDAO indentDAO = new IndentDAO();

    public void loadData(String itemName) {

        List<TransactionHistory> list =
                dao.getAvailableSerialItems(itemName);

        table.setItems(FXCollections.observableArrayList(list));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem addIndentItem = new MenuItem("Add Indent");

        contextMenu.getItems().add(addIndentItem);

        table.setRowFactory(tv -> {

            TableRow<TransactionHistory> row = new TableRow<>();

            row.setOnContextMenuRequested(event -> {

                if (!row.isEmpty()) {

                    table.getSelectionModel().select(row.getIndex());

                    contextMenu.show(
                            row,
                            event.getScreenX(),
                            event.getScreenY()
                    );
                }
            });

            return row;
        });

        addIndentItem.setOnAction(e -> {

            TransactionHistory selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                return;
            }

            TextInputDialog dialog = new TextInputDialog();

            dialog.setTitle("Add Indent");
            dialog.setHeaderText(selected.getItemName());
            dialog.setContentText("Enter Quantity:");

            dialog.showAndWait().ifPresent(value -> {

                try {

                    double quantity = Double.parseDouble(value);

                    indentDAO.insertIndent(
                            selected.getItemCode(),
                            selected.getItemName(),
                            quantity
                    );

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Indent added successfully.");
                    alert.showAndWait();

                } catch (Exception ex) {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid quantity.");
                    alert.showAndWait();
                }
            });
        });

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

        itemConditionCol.setCellValueFactory(
                new PropertyValueFactory<>("itemCondition"));

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