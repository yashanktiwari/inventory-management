package com.inventory.ui.table;

import com.inventory.dao.TransactionDAO;
import com.inventory.database.AppConfig;
import com.inventory.model.AuditEntry;
import com.inventory.model.TransactionHistory;
import com.inventory.ui.dialog.DialogManager;
import com.inventory.util.AlertUtil;
import com.inventory.util.AttachmentManager;
import com.inventory.util.PasswordUtil;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.awt.MouseInfo;
import java.awt.geom.Point2D;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
                TooltipCellFactory.create();
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

            private final Hyperlink updateLink = new Hyperlink();
            private final Label statusLabel = new Label();

            {
                updateLink.setUnderline(false);
                updateLink.setCursor(Cursor.HAND);
                updateLink.getStyleClass().add("status-action-link");

                statusLabel.getStyleClass().add("status-action-label");

                updateLink.setOnAction(event -> {

                    TransactionHistory history =
                            getTableView().getItems().get(getIndex());

                    if (!"ISSUED".equals(history.getStatus())) return;

                    Optional<Pair<String, String>> result =
                            DialogManager.showStatusDialog(
                                    history.getStatus(),
                                    history.getRemarks()
                            );

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

                updateLink.getStyleClass().removeAll(
                        "status-action-stock",
                        "status-action-issued",
                        "status-action-returned",
                        "status-action-scrapped"
                );

                if ("BUY".equalsIgnoreCase(buySell)) {

                    updateLink.setDisable(true);
                    updateLink.setText("IN STOCK");
                    updateLink.getStyleClass().add("status-action-stock");

                } else {

                    switch (status.toUpperCase()) {

                        case "ISSUED" -> {
                            updateLink.setDisable(false);
                            updateLink.setText("ISSUED");
                            updateLink.getStyleClass().add("status-action-issued");
                        }

                        case "RETURNED" -> {
                            updateLink.setDisable(true);
                            updateLink.setText("RETURNED");
                            updateLink.getStyleClass().add("status-action-returned");
                        }

                        case "SCRAPPED" -> {
                            updateLink.setDisable(true);
                            updateLink.setText("SCRAPPED");
                            updateLink.getStyleClass().add("status-action-scrapped");
                        }
                    }
                }

                setGraphic(updateLink);
                setAlignment(Pos.CENTER);
            }
        });

        // ================= DELETE COLUMN =================

        deleteColumn.setCellFactory(param -> new TableCell<>() {

            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.getStyleClass().add("delete-action-button");

                deleteButton.setOnAction(event -> {

                    TransactionHistory data =
                            getTableView().getItems().get(getIndex());

                    Optional<String> passwordResult =
                            DialogManager.showAdminPasswordDialog();

                    if (passwordResult.isEmpty()) {
                        return;
                    }

                    String password = passwordResult.get().trim();

                    if (password.isEmpty()) {
                        AlertUtil.showError(
                                "Authorization Failed",
                                "Password cannot be empty."
                        );
                        return;
                    }

                    String enteredHash = PasswordUtil.hashPassword(password);
                    String storedHash = AppConfig.getAdminPasswordHash();

                    if (!enteredHash.equals(storedHash)) {

                        AlertUtil.showError(
                                "Authorization Failed",
                                "Invalid admin password. You are not allowed to delete this transaction."
                        );

                        return;
                    }

                    boolean confirmed = AlertUtil.showConfirmation(
                            "Confirm Delete",
                            "Are you sure you want to delete this transaction?\n\nThis action cannot be undone."
                    );

                    if (confirmed) {

                        transactionDAO.deleteTransaction(
                                data.getTransactionId()
                        );

                        reload.accept(null);

                        AlertUtil.showInfo(
                                "Deleted",
                                "Transaction deleted successfully."
                        );
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : deleteButton);
                setAlignment(Pos.CENTER);
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
            private final VBox container = new VBox(8);
            private boolean mouseInsidePopup = false;

            {
                container.getStyleClass().add("audit-popup");

                ScrollPane scroll = new ScrollPane(container);
                scroll.setPrefWidth(420);
                scroll.setMaxHeight(300);
                scroll.setFitToWidth(true);

                scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                popup.getContent().add(scroll);
                popup.setAutoHide(false);

                // show popup
                setOnMouseEntered(e -> {

                    if (container.getChildren().isEmpty()) return;

                    Bounds cellBounds = localToScreen(getBoundsInLocal());

                    double popupHeight = scroll.prefHeight(-1);
                    double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

                    double x = cellBounds.getMinX();
                    double yBelow = cellBounds.getMaxY() + 4;
                    double yAbove = cellBounds.getMinY() - popupHeight - 4;

                    double y = (yBelow + popupHeight > screenHeight) ? yAbove : yBelow;

                    popup.show(getScene().getWindow(), x, y);
                });

                // hide popup when leaving cell
                setOnMouseExited(e -> {

                    PauseTransition delay = new PauseTransition(Duration.millis(150));

                    delay.setOnFinished(ev -> {
                        if (!mouseInsidePopup) {
                            popup.hide();
                        }
                    });

                    delay.play();
                });

                scroll.setOnMouseEntered(e -> mouseInsidePopup = true);

                scroll.setOnMouseExited(e -> {
                    mouseInsidePopup = false;

                    PauseTransition delay = new PauseTransition(Duration.millis(150));
                    delay.setOnFinished(ev -> popup.hide());
                    delay.play();
                });

                // hide popup on table scroll
                table.skinProperty().addListener((obs, oldSkin, newSkin) -> {

                    ScrollBar vBar = (ScrollBar) table.lookup(".scroll-bar:vertical");
                    ScrollBar hBar = (ScrollBar) table.lookup(".scroll-bar:horizontal");

                    if (vBar != null) {
                        vBar.valueProperty().addListener((o, oldVal, newVal) -> popup.hide());
                    }

                    if (hBar != null) {
                        hBar.valueProperty().addListener((o, oldVal, newVal) -> popup.hide());
                    }
                });

                // hide popup on mouse wheel scroll
                table.setOnScroll(e -> popup.hide());
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

                Map<String, List<AuditEntry>> grouped = new LinkedHashMap<>();

                for (AuditEntry entry : t.getAuditEntries()) {

                    String key = entry.getModifiedBy() + "|" + entry.getModifiedAt();

                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
                }

                int groupIndex = 0;
                int totalGroups = grouped.size();

                for (var group : grouped.entrySet()) {

                    List<AuditEntry> groupEntries = group.getValue();
                    AuditEntry first = groupEntries.get(0);

                    Label header = new Label(
                            first.getModifiedBy() +
                                    " | " +
                                    formatter.format(first.getModifiedAt())
                    );

                    header.getStyleClass().add("audit-header");

                    VBox block = new VBox(2);
                    block.getChildren().add(header);

                    for (AuditEntry entry : groupEntries) {

                        Label change = new Label(
                                entry.getFieldName() +
                                        " : " +
                                        entry.getOldValue() +
                                        " → " +
                                        entry.getNewValue()
                        );

                        change.getStyleClass().add("audit-change");

                        block.getChildren().add(change);
                    }

                    container.getChildren().add(block);

                    if (groupIndex < totalGroups - 1) {
                        Separator sep = new Separator();
                        sep.getStyleClass().add("audit-separator");
                        container.getChildren().add(sep);
                    }

                    groupIndex++;
                }
            }
        });

        // ================= ATTACHMENT COLUMN =================

        attachmentColumn.setPrefWidth(120);
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