package com.inventory.ui.table;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.AuditEntry;
import com.inventory.model.TransactionHistory;
import com.inventory.util.AttachmentManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

public class DashboardActionColumns {

    public static void configure(

            TableColumn<TransactionHistory, Integer> serialColumn,
            TableColumn<TransactionHistory, Void> actionColumn,
            TableColumn<TransactionHistory, Void> deleteColumn,
            TableColumn<TransactionHistory, String> auditColumn,
            TableColumn<TransactionHistory, Void> attachmentColumn,

            TableView<TransactionHistory> table,
            TransactionDAO transactionDAO,
            AttachmentManager attachmentManager,
            Consumer<Void> reload,
            DateTimeFormatter formatter,
            String currentTab,
            Consumer<TransactionHistory> openHistory   // ⭐ FIX
    ) {

        // ================= SERIAL COLUMN =================

        serialColumn.setCellFactory(col -> new TableCell<>() {

            private final Hyperlink link = new Hyperlink();

            {
                link.setStyle("-fx-text-fill:black; -fx-underline:false;");

                link.setOnAction(e -> {

                    TransactionHistory row =
                            getTableView().getItems().get(getIndex());

                    openHistory.accept(row);
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
            protected void updateItem(Integer item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                link.setText(String.valueOf(getIndex() + 1));
                setGraphic(link);
            }
        });

        // ================= ACTION COLUMN =================

        actionColumn.setCellFactory(col -> new TableCell<>() {

            private final Button updateBtn = new Button("Update");

            {
                updateBtn.setOnAction(event -> {

                    TransactionHistory history =
                            getTableView().getItems().get(getIndex());

                    if (!"ISSUED".equals(currentTab)) return;

                    Dialog<Pair<String, String>> dialog = new Dialog<>();
                    dialog.setTitle("Update Status");

                    ChoiceBox<String> statusChoice = new ChoiceBox<>();
                    statusChoice.getItems().addAll("RETURNED", "SCRAPPED");
                    statusChoice.setValue(history.getStatus());

                    TextArea remarksArea = new TextArea(history.getRemarks());
                    remarksArea.setPrefRowCount(3);

                    VBox content = new VBox(10,
                            new Label("Status"),
                            statusChoice,
                            new Label("Remarks"),
                            remarksArea
                    );

                    dialog.getDialogPane().setContent(content);

                    ButtonType updateBtnType =
                            new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);

                    dialog.getDialogPane().getButtonTypes().addAll(
                            updateBtnType,
                            ButtonType.CANCEL
                    );

                    dialog.setResultConverter(button -> {

                        if (button == updateBtnType) {
                            return new Pair<>(
                                    statusChoice.getValue(),
                                    remarksArea.getText()
                            );
                        }

                        return null;
                    });

                    Optional<Pair<String, String>> result =
                            dialog.showAndWait();

                    result.ifPresent(pair -> {

                        transactionDAO.updateTransactionStatus(
                                history.getTransactionId(),
                                pair.getKey(),
                                pair.getValue()
                        );

                        reload.accept(null);
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                TransactionHistory history =
                        getTableView().getItems().get(getIndex());

                String buySell = history.getBuySell();
                String status = history.getStatus();

                if ("Buy".equalsIgnoreCase(buySell)) {
                    updateBtn.setDisable(true);
                    updateBtn.setText("IN STOCK");
                } else {

                    switch (status.toUpperCase()) {

                        case "ISSUED" -> {
                            updateBtn.setDisable(false);
                            updateBtn.setText("ISSUED");
                        }

                        case "RETURNED" -> {
                            updateBtn.setDisable(true);
                            updateBtn.setText("RETURNED");
                        }

                        case "SCRAPPED" -> {
                            updateBtn.setDisable(true);
                            updateBtn.setText("SCRAPPED");
                        }
                    }
                }

                setGraphic(updateBtn);
            }
        });

        // ================= DELETE COLUMN =================

        deleteColumn.setCellFactory(param -> new TableCell<>() {

            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle(
                        "-fx-background-color:#ff4d4d;" +
                                "-fx-text-fill:white;" +
                                "-fx-font-weight:bold;"
                );

                deleteButton.setOnAction(event -> {

                    TransactionHistory data =
                            getTableView().getItems().get(getIndex());

                    Alert confirm =
                            new Alert(Alert.AlertType.CONFIRMATION);

                    confirm.setHeaderText("Delete Transaction?");

                    confirm.showAndWait().ifPresent(res -> {

                        if (res == ButtonType.OK) {

                            transactionDAO.deleteTransaction(
                                    data.getTransactionId()
                            );

                            reload.accept(null);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : deleteButton);
            }
        });

        // ================= AUDIT COLUMN =================

        auditColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLastModifiedBy()
                )
        );

        auditColumn.setCellFactory(col -> new TableCell<>() {

            private final Popup popup = new Popup();
            private final VBox container = new VBox(10);

            {
                ScrollPane scroll = new ScrollPane(container);
                scroll.setPrefWidth(420);
                scroll.setFitToWidth(true);

                popup.getContent().add(scroll);

                setOnMouseEntered(e -> {

                    if (!container.getChildren().isEmpty()) {

                        popup.show(
                                getScene().getWindow(),
                                e.getScreenX() + 10,
                                e.getScreenY() + 10
                        );
                    }
                });

                setOnMouseExited(e -> popup.hide());
            }

            @Override
            protected void updateItem(String user, boolean empty) {

                super.updateItem(user, empty);

                container.getChildren().clear();

                if (empty || user == null) {
                    setText(null);
                    return;
                }

                TransactionHistory t =
                        getTableView().getItems().get(getIndex());

                setText(user.toUpperCase());

                if (t.getAuditEntries() == null) return;

                for (AuditEntry entry : t.getAuditEntries()) {

                    Label line = new Label(
                            entry.getFieldName() +
                                    " : " +
                                    entry.getOldValue() +
                                    " → " +
                                    entry.getNewValue()
                    );

                    container.getChildren().add(line);
                }
            }
        });

        // ================= ATTACHMENT COLUMN =================

        attachmentColumn.setCellFactory(col -> new TableCell<>() {

            private final Button btn = new Button();

            {
                btn.setStyle("-fx-background-color:transparent;");

                btn.setOnAction(e -> {

                    TransactionHistory row =
                            getTableView().getItems().get(getIndex());

                    Stage stage =
                            (Stage) btn.getScene().getWindow();

                    attachmentManager.handleAttachment(
                            row,
                            stage,
                            transactionDAO,
                            () -> reload.accept(null)
                    );
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                TransactionHistory history =
                        getTableView().getItems().get(getIndex());

                String file = history.getAttachmentFile();

                if (file == null || file.isBlank()) {
                    btn.setText("📎 ATTACH");
                } else {
                    btn.setText("👁 VIEW");
                }

                setGraphic(btn);
            }
        });
    }
}