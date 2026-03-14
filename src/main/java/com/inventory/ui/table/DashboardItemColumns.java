package com.inventory.ui.table;

import com.inventory.model.TransactionHistory;
import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class DashboardItemColumns {

    public static void configure(
            TableColumn<TransactionHistory, String> itemCodeColumn,
            TableColumn<TransactionHistory, String> itemNameColumn,
            TableColumn<TransactionHistory, String> itemMakeColumn,
            TableColumn<TransactionHistory, String> itemModelColumn,
            TableColumn<TransactionHistory, String> itemSerialColumn,
            TableColumn<TransactionHistory, String> itemConditionColumn,
            TableColumn<TransactionHistory, Double> itemCountColumn,
            TableColumn<TransactionHistory, String> unitColumn,
            Consumer<TransactionHistory> openItemCodeHistory,
            Consumer<TransactionHistory> openItemNameHistory,
            Consumer<TransactionHistory> openItemMakeHistory,
            Consumer<TransactionHistory> openItemModelHistory,
            Consumer<TransactionHistory> openItemSerialHistory,
            Consumer<TransactionHistory> openItemConditionHistory
    ) {

        // ITEM CODE
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        createHyperlinkColumn(itemCodeColumn, openItemCodeHistory);

        // ITEM NAME
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        createHyperlinkColumn(itemNameColumn, openItemNameHistory);

        // ITEM MAKE
        itemMakeColumn.setCellValueFactory(new PropertyValueFactory<>("itemMake"));
        createHyperlinkColumn(itemMakeColumn, openItemMakeHistory);

        // ITEM MODEL
        itemModelColumn.setCellValueFactory(new PropertyValueFactory<>("itemModel"));
        createHyperlinkColumn(itemModelColumn, openItemModelHistory);

        // ITEM SERIAL
        itemSerialColumn.setCellValueFactory(new PropertyValueFactory<>("itemSerial"));
        createHyperlinkColumn(itemSerialColumn, openItemSerialHistory);

        // ITEM CONDITION
        itemConditionColumn.setCellValueFactory(new PropertyValueFactory<>("itemCondition"));
        createHyperlinkColumn(itemConditionColumn, openItemConditionHistory);

        // ITEM COUNT
        itemCountColumn.setCellValueFactory(new PropertyValueFactory<>("itemCount"));

        // UNIT
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String unit, boolean empty) {

                super.updateItem(unit, empty);

                if (empty || unit == null) {
                    setText(null);
                } else {
                    setText(unit.toUpperCase());
                }
            }
        });
    }

    private static void createHyperlinkColumn(
            TableColumn<TransactionHistory, String> column,
            Consumer<TransactionHistory> action
    ) {

        column.setCellFactory(col -> new TableCell<>() {

            private final Hyperlink link = new Hyperlink();

            {
                link.setStyle("-fx-text-fill: black; -fx-underline: false;");
                link.setCursor(Cursor.HAND);

                link.setOnAction(event -> {
                    TransactionHistory row = getTableView().getItems().get(getIndex());
                    action.accept(row);
                });

                tableRowProperty().addListener((obs, oldRow, newRow) -> {

                    if (newRow != null) {

                        link.textFillProperty().bind(
                                Bindings.when(newRow.selectedProperty())
                                        .then(Color.WHITE)
                                        .otherwise(Color.web("#000000"))
                        );
                    }
                });
            }

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    link.setText(value.toUpperCase());
                    setGraphic(link);
                }
            }
        });
    }
}