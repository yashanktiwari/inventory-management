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

public class DashboardPurchaseColumns {

    public static void configure(

            TableColumn<TransactionHistory, String> poColumn,
            TableColumn<TransactionHistory, String> partyColumn,

            Consumer<TransactionHistory> openPoHistory,
            Consumer<TransactionHistory> openPartyHistory
    ) {

        // PO NUMBER
        poColumn.setCellValueFactory(new PropertyValueFactory<>("poNo"));
        poColumn.setCellFactory(col -> createHyperlinkCell(openPoHistory));

        // PARTY NAME
        partyColumn.setCellValueFactory(new PropertyValueFactory<>("partyName"));
        partyColumn.setCellFactory(col -> createHyperlinkCell(openPartyHistory));
    }

    private static TableCell<TransactionHistory, String> createHyperlinkCell(
            Consumer<TransactionHistory> action
    ) {

        return new TableCell<>() {

            private final Hyperlink link = new Hyperlink();

            {
                link.setStyle("-fx-text-fill:black; -fx-underline:false;");
                link.setCursor(Cursor.HAND);

                link.setOnAction(e -> {

                    TransactionHistory row =
                            getTableView().getItems().get(getIndex());

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
        };
    }
}