package com.inventory.ui.table;

import com.inventory.model.TransactionHistory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class DashboardTableConfigurator {

    public static void configureBasicColumns(

            TableColumn<TransactionHistory, String> buySellColumn,
            TableColumn<TransactionHistory, String> plantColumn,
            TableColumn<TransactionHistory, String> departmentColumn,
            TableColumn<TransactionHistory, String> locationColumn,
            TableColumn<TransactionHistory, String> itemLocationColumn,
            TableColumn<TransactionHistory, String> itemCategoryColumn,
            TableColumn<TransactionHistory, String> statusColumn,
            TableColumn<TransactionHistory, String> remarksColumn,

            Consumer<TransactionHistory> openBuySell,
            Consumer<TransactionHistory> openPlant,
            Consumer<TransactionHistory> openDepartment,
            Consumer<TransactionHistory> openLocation,
            Consumer<TransactionHistory> openItemLocation,
            Consumer<TransactionHistory> openItemCategory
    ) {

        // BUY / SELL
        buySellColumn.setCellValueFactory(new PropertyValueFactory<>("buySell"));

        buySellColumn.setCellFactory(column -> new TableCell<>() {

            private final Hyperlink badge = new Hyperlink();

            {
                badge.getStyleClass().add("buy-sell-badge");
                badge.setAlignment(Pos.CENTER);
                badge.setCursor(Cursor.HAND);
                badge.setUnderline(false);

                badge.setOnAction(e -> {

                    TransactionHistory row =
                            getTableView().getItems().get(getIndex());

                    openBuySell.accept(row);
                });

//                tableRowProperty().addListener((obs, oldRow, newRow) -> {
//
//                    if (newRow != null) {
//
//                        badge.textFillProperty().bind(
//                                Bindings.when(newRow.selectedProperty())
//                                        .then(Color.WHITE)
//                                        .otherwise(Color.BLACK)
//                        );
//                    }
//                });
            }

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                    badge.getStyleClass().removeAll("buy-badge","sell-badge");
                    return;
                }

                badge.setText(value.toUpperCase());

                badge.getStyleClass().removeAll(
                        "buy-badge",
                        "sell-badge"
                );

                if ("BUY".equalsIgnoreCase(value)) {
                    badge.getStyleClass().add("buy-badge");
                } else if ("SELL".equalsIgnoreCase(value)) {
                    badge.getStyleClass().add("sell-badge");
                }

                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // PLANT
        plantColumn.setCellValueFactory(new PropertyValueFactory<>("plant"));
        plantColumn.setCellFactory(col ->
                createHyperlinkCell(openPlant));

        // DEPARTMENT
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        departmentColumn.setCellFactory(col ->
                createHyperlinkCell(openDepartment));

        // LOCATION
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationColumn.setCellFactory(col ->
                createHyperlinkCell(openLocation));

        // ITEM LOCATION
        itemLocationColumn.setCellValueFactory(new PropertyValueFactory<>("itemLocation"));
        itemLocationColumn.setCellFactory(col ->
                createHyperlinkCell(openItemLocation));

        // ITEM CATEGORY
        itemCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("itemCategory"));
        itemCategoryColumn.setCellFactory(col ->
                createHyperlinkCell(openItemCategory));

        // STATUS (NOT clickable)
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusColumn.setCellFactory(column -> new TableCell<>() {

            private final Label badge = new Label();

            {
                badge.getStyleClass().add("status-badge");
                badge.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String status, boolean empty) {

                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    badge.getStyleClass().removeAll(
                            "status-instock",
                            "status-issued",
                            "status-returned",
                            "status-scrapped"
                    );
                    return;
                }

                badge.setText(status.toUpperCase());

                badge.getStyleClass().removeAll(
                        "status-instock",
                        "status-issued",
                        "status-returned",
                        "status-scrapped"
                );

                switch (status.toUpperCase()) {

                    case "IN STOCK" -> badge.getStyleClass().add("status-instock");
                    case "ISSUED" -> badge.getStyleClass().add("status-issued");
                    case "RETURNED" -> badge.getStyleClass().add("status-returned");
                    case "SCRAPPED" -> badge.getStyleClass().add("status-scrapped");

                }

                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // REMARKS (NOT clickable)
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        remarksColumn.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value.toUpperCase());
                }
            }
        });
    }

    private static TableCell<TransactionHistory, String> createHyperlinkCell(
            Consumer<TransactionHistory> action
    ) {

        return new TableCell<>() {

            private final Hyperlink link = new Hyperlink();

            {
                link.setUnderline(false);
                link.setStyle("-fx-text-fill:black;");
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
                    setAlignment(Pos.CENTER);
                }
            }
        };
    }

    private static TableCell<TransactionHistory, String> createStyledHyperlinkCell(
            Consumer<TransactionHistory> action,
            java.util.function.Function<String, String> styleProvider
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
            }

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                link.setText(value.toUpperCase());
                setGraphic(link);
                setAlignment(Pos.CENTER);

                setStyle(styleProvider.apply(value));
            }
        };
    }

    public static void configureDateColumns(
            TableColumn<TransactionHistory, String> issuedColumn,
            TableColumn<TransactionHistory, String> returnedColumn,
            DateTimeFormatter formatter
    ) {

        issuedColumn.setCellValueFactory(data -> {

            LocalDateTime date = data.getValue().getIssuedDateTime();

            return new SimpleStringProperty(
                    date == null ? "" : date.format(formatter)
            );
        });

        returnedColumn.setCellValueFactory(data -> {

            TransactionHistory history = data.getValue();

            if ("BUY".equalsIgnoreCase(history.getBuySell())) {
                return new SimpleStringProperty("--");
            }

            LocalDateTime date = history.getReturnedDateTime();

            if (date == null) {
                return new SimpleStringProperty("NOT RETURNED");
            }

            return new SimpleStringProperty(
                    date.format(formatter)
            );
        });
    }
}