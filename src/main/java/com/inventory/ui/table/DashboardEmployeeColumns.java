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

public class DashboardEmployeeColumns {

    public static void configure(
            TableColumn<TransactionHistory, String> employeeCodeColumn,
            TableColumn<TransactionHistory, String> employeeNameColumn,
            Consumer<TransactionHistory> openEmployeeCodeHistory,
            Consumer<TransactionHistory> openEmployeeNameHistory
    ) {

        employeeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeCode"));

        employeeCodeColumn.setCellFactory(column -> new TableCell<>() {

            private final Hyperlink link = new Hyperlink();

            {
                link.setStyle("-fx-text-fill: black; -fx-underline: false;");
                link.setCursor(Cursor.HAND);

                link.setOnAction(event -> {
                    TransactionHistory row = getTableView().getItems().get(getIndex());
                    openEmployeeCodeHistory.accept(row);
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
            protected void updateItem(String employeeCode, boolean empty) {

                super.updateItem(employeeCode, empty);

                if (empty || employeeCode == null) {
                    setGraphic(null);
                } else {
                    link.setText(employeeCode.toUpperCase());
                    setGraphic(link);
                }
            }
        });


        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        employeeNameColumn.setCellFactory(column -> new TableCell<>() {

            private final Hyperlink link = new Hyperlink();

            {
                link.setStyle("-fx-text-fill: black; -fx-underline: false;");
                link.setCursor(Cursor.HAND);

                link.setOnAction(event -> {
                    TransactionHistory row = getTableView().getItems().get(getIndex());
                    openEmployeeNameHistory.accept(row);
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
            protected void updateItem(String employeeName, boolean empty) {

                super.updateItem(employeeName, empty);

                if (empty || employeeName == null) {
                    setGraphic(null);
                } else {
                    link.setText(employeeName.toUpperCase());
                    setGraphic(link);
                }
            }
        });
    }
}