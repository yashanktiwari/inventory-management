package com.inventory.ui.factory;

import com.inventory.model.TransactionHistory;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import java.util.function.Consumer;

public class DashboardRowFactory {

    public static TableRow<TransactionHistory> createRow(
            TableView<TransactionHistory> table,
            String currentTab,
            Consumer<TransactionHistory> sellHandler,
            Consumer<TransactionHistory> editHandler
    ) {

        TableRow<TransactionHistory> row = new TableRow<>();

        ContextMenu contextMenu = new ContextMenu();

        MenuItem sellItem = new MenuItem("Sell");
        MenuItem editItem = new MenuItem("Edit");

        sellItem.setOnAction(e -> {
            TransactionHistory transaction = row.getItem();
            if (transaction != null) {
                sellHandler.accept(transaction);
            }
        });

        editItem.setOnAction(e -> {
            TransactionHistory transaction = row.getItem();
            if (transaction != null) {
                editHandler.accept(transaction);
            }
        });

        row.itemProperty().addListener((obs, oldItem, newItem) -> {

            contextMenu.getItems().clear();

            if (newItem == null) return;

            switch (currentTab) {

                case "BUY" -> {
                    contextMenu.getItems().add(editItem);
                }

                case "IN STOCK" -> {

                    if (newItem.isAvailable()) {
                        contextMenu.getItems().add(sellItem);
                    }

                    contextMenu.getItems().add(editItem);
                }

                case "ISSUED", "SCRAPPED", "RETURNED" -> {
                    contextMenu.getItems().add(editItem);
                }
            }
        });

        row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu)
        );

        // Double click → copy cell value
        row.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2 && !row.isEmpty()) {

                TablePosition<?, ?> pos = table.getFocusModel().getFocusedCell();
                if (pos == null) return;

                TableColumn<?, ?> column = pos.getTableColumn();
                if (column == null) return;

                Object value = column.getCellData(row.getIndex());

                if (value != null) {

                    ClipboardContent content = new ClipboardContent();
                    content.putString(value.toString());
                    Clipboard.getSystemClipboard().setContent(content);

                    Tooltip tip = new Tooltip("Copied!");
                    tip.setAutoHide(true);

                    tip.show(
                            table.getScene().getWindow(),
                            event.getScreenX(),
                            event.getScreenY()
                    );

                    PauseTransition delay = new PauseTransition(Duration.seconds(1));
                    delay.setOnFinished(e -> tip.hide());
                    delay.play();
                }
            }
        });

        return row;
    }
}