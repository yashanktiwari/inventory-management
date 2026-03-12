package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.AuditEntry;
import com.inventory.model.TransactionHistory;
import com.inventory.util.AttachmentManager;
import com.inventory.util.ExportUtil;
import com.inventory.util.TableColumnPreferenceManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.collections.ListChangeListener;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class ItemHistoryController {

    @FXML private Label titleLabel;

    @FXML private TableView<TransactionHistory> itemHistoryTable;

    @FXML private TableColumn<TransactionHistory, Integer> serialColumn;

    @FXML private TableColumn<TransactionHistory, String> issuedColumn;
    @FXML private TableColumn<TransactionHistory, String> returnedColumn;
    @FXML private TableColumn<TransactionHistory, String> remarksColumn;
    @FXML private TableColumn<TransactionHistory, String> buySellColumn;
    @FXML private TableColumn<TransactionHistory, String> plantColumn;
    @FXML private TableColumn<TransactionHistory, String> departmentColumn;
    @FXML private TableColumn<TransactionHistory, String> locationColumn;

    @FXML private TableColumn<TransactionHistory, String> employeeIdColumn;
    @FXML private TableColumn<TransactionHistory, String> employeeNameColumn;

    @FXML private TableColumn<TransactionHistory, String> ipColumn;

    @FXML private TableColumn<TransactionHistory, String> itemCodeColumn;
    @FXML private TableColumn<TransactionHistory, String> itemNameColumn;
    @FXML private TableColumn<TransactionHistory, String> itemMakeColumn;
    @FXML private TableColumn<TransactionHistory, String> itemModelColumn;
    @FXML private TableColumn<TransactionHistory, String> itemSerialColumn;
    @FXML private TableColumn<TransactionHistory, Double> itemCountColumn;
    @FXML private TableColumn<TransactionHistory, String> itemLocationColumn;
    @FXML private TableColumn<TransactionHistory, String> itemCategoryColumn;
    @FXML private TableColumn<TransactionHistory, String> unitColumn;

    @FXML private TableColumn<TransactionHistory, String> imeiColumn;
    @FXML private TableColumn<TransactionHistory, String> simColumn;

    @FXML private TableColumn<TransactionHistory, String> poColumn;
    @FXML private TableColumn<TransactionHistory, String> partyColumn;

    @FXML private TableColumn<TransactionHistory, String> statusColumn;
    @FXML private TableColumn<TransactionHistory, String> auditColumn;

    @FXML
    private TableColumn<TransactionHistory, Void> attachmentColumn;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final Preferences prefs =
            Preferences.userNodeForPackage(ItemHistoryController.class);

    private static final String COLUMN_ORDER_KEY = "itemHistoryColumnOrder";
    private AttachmentManager attachmentManager;
    private String currentField;
    private String currentValue;
    private String currentTitle;

    @FXML
    public void initialize() {
        TableColumnPreferenceManager<TransactionHistory> columnPrefs =
                new TableColumnPreferenceManager<>(
                        itemHistoryTable,
                        "itemHistoryColumnOrder",
                        ItemHistoryController.class,
                        null
                );

        columnPrefs.initialize();

        attachmentManager = new AttachmentManager();
        itemHistoryTable.setFixedCellSize(28);

        serialColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        itemHistoryTable.getItems().indexOf(cellData.getValue()) + 1
                ).asObject()
        );

        buySellColumn.setCellValueFactory(new PropertyValueFactory<>("buySell"));
        buySellColumn.setCellFactory(column -> new TableCell<TransactionHistory, String>() {

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {

                    setText(value);
                    setAlignment(javafx.geometry.Pos.CENTER);

                    if ("Buy".equalsIgnoreCase(value)) {
                        setStyle("-fx-background-color:#d4edda; -fx-text-fill:black;");
                    } else if ("Sell".equalsIgnoreCase(value)) {
                        setStyle("-fx-background-color:#f8d7da; -fx-text-fill:black;");
                    }
                }
            }
        });

        plantColumn.setCellValueFactory(new PropertyValueFactory<>("plant"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeCode"));
        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemMakeColumn.setCellValueFactory(new PropertyValueFactory<>("itemMake"));
        itemModelColumn.setCellValueFactory(new PropertyValueFactory<>("itemModel"));
        itemSerialColumn.setCellValueFactory(new PropertyValueFactory<>("itemSerial"));
        itemLocationColumn.setCellValueFactory(new PropertyValueFactory<>("itemLocation"));
        itemCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("itemCategory"));

        itemCountColumn.setCellValueFactory(new PropertyValueFactory<>("itemCount"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        imeiColumn.setCellValueFactory(new PropertyValueFactory<>("imeiNo"));
        simColumn.setCellValueFactory(new PropertyValueFactory<>("simNo"));

        poColumn.setCellValueFactory(new PropertyValueFactory<>("poNo"));
        partyColumn.setCellValueFactory(new PropertyValueFactory<>("partyName"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {

                    setText(value);
                    setAlignment(javafx.geometry.Pos.CENTER);

                    switch (value.toLowerCase()) {

                        case "issued" ->
                                setStyle("-fx-background-color:#d6eaff; -fx-text-fill:black;");

                        case "returned" ->
                                setStyle("-fx-background-color:#d4edda; -fx-text-fill:black;");

                        case "scrap" ->
                                setStyle("-fx-background-color:#e0e0e0; -fx-text-fill:black;");

                        case "in stock" ->
                                setStyle("-fx-background-color:#fff3cd; -fx-text-fill:black;");
                    }
                }
            }
        });

        issuedColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getIssuedDateTime().format(formatter))
        );
        returnedColumn.setCellValueFactory(cellData -> {

            LocalDateTime returned = cellData.getValue().getReturnedDateTime();

            if (returned == null) {
                return new SimpleStringProperty("Not Returned");
            }

            return new SimpleStringProperty(returned.format(formatter));
        });

        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        remarksColumn.setCellFactory(column -> new TableCell<>() {

            private final Label textLabel = new Label();
            private final Label iconLabel = new Label("‼");   // info icon
            private final Tooltip tooltip = new Tooltip();
            private final HBox container = new HBox(5);

            {
                textLabel.setMaxWidth(Double.MAX_VALUE);
                textLabel.setWrapText(false);
                textLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

                iconLabel.setStyle("-fx-font-size: 12px;");
                iconLabel.setVisible(true);

                tableRowProperty().addListener((obs, oldRow, newRow) -> {
                    if (newRow != null) {
                        iconLabel.textFillProperty().bind(
                                javafx.beans.binding.Bindings.when(newRow.selectedProperty())
                                        .then(javafx.scene.paint.Color.WHITE)
                                        .otherwise(javafx.scene.paint.Color.web("#000000"))
                        );
                    }
                });

                tooltip.setWrapText(true);
                tooltip.setMaxWidth(400);
                tooltip.setStyle("-fx-font-size:14px; -fx-padding:8px;");

                HBox.setHgrow(textLabel, Priority.ALWAYS);
                container.getChildren().addAll(textLabel, iconLabel);
                container.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null || value.isBlank()) {
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }

                textLabel.setText(value);
                tooltip.setText(value);

                // show icon if text is long OR multi-line
                if (value.length() > 25 || value.contains("\n")) {
                    iconLabel.setVisible(true);
                    setTooltip(tooltip);
                } else {
                    iconLabel.setVisible(false);
                    setTooltip(null);
                }

                setGraphic(container);
            }
        });

        attachmentColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                btn.setOnAction(e -> {

                    TransactionHistory row =
                            getTableView().getItems().get(getIndex());

                    Stage stage = (Stage) btn.getScene().getWindow();

                    attachmentManager.handleAttachment(row, stage, transactionDAO, () -> loadHistory(currentField, currentValue, currentTitle));
                });

                tableRowProperty().addListener((obs, oldRow, newRow) -> {
                    if (newRow != null) {
                        btn.textFillProperty().bind(
                                javafx.beans.binding.Bindings.when(newRow.selectedProperty())
                                        .then(javafx.scene.paint.Color.WHITE)
                                        .otherwise(javafx.scene.paint.Color.web("#000000"))
                        );
                    }
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
                if (history.getAttachmentFile() == null ||
                        history.getAttachmentFile().isBlank()) {
                    btn.setText("📎 Attach"); // upload icon
                } else {
                    btn.setText("👁 View"); // view icon
                }
                setGraphic(btn);
            }
        });

        auditColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLastModifiedBy()
                ));
        auditColumn.setCellFactory(col -> new TableCell<>() {

            private final Popup popup = new Popup();
            private final VBox container = new VBox(12);
            private final ScrollPane scrollPane = new ScrollPane(container);

            {
                popup.setAutoHide(true);

                container.setStyle("-fx-padding: 10;");

                scrollPane.setFitToWidth(true);
                scrollPane.setMaxHeight(400);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                popup.getContent().add(scrollPane);

                // Hide popup when leaving popup area
                scrollPane.setOnMouseExited(e -> popup.hide());

                // Show popup when entering cell
                setOnMouseEntered(e -> {

                    if (container.getChildren().isEmpty()) {
                        return; // no audit entries -> no popup
                    }

                    if (!popup.isShowing() && getScene() != null) {

                        popup.show(
                                getScene().getWindow(),
                                e.getScreenX() + 10,
                                e.getScreenY() + 10
                        );
                    }
                });

                // Hide popup when leaving cell (unless hovering popup)
                setOnMouseExited(e -> {

                    if (!scrollPane.isHover()) {
                        popup.hide();
                    }
                });
            }

            @Override
            protected void updateItem(String user, boolean empty) {

                super.updateItem(user, empty);

                popup.hide();
                container.getChildren().clear();

                if (empty || user == null) {
                    setText(null);
                    return;
                }

                TransactionHistory transaction =
                        getTableView().getItems().get(getIndex());

                if (transaction.getAuditEntries() == null ||
                        transaction.getAuditEntries().isEmpty()) {

                    setText(user); // no icon
                    return;
                }

                setText(user + " ⓘ");

                Map<String, List<AuditEntry>> grouped = new LinkedHashMap<>();

                for (AuditEntry entry : transaction.getAuditEntries()) {

                    String key = entry.getModifiedBy() + "|" + entry.getModifiedAt();

                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
                }

                for (List<AuditEntry> group : grouped.values()) {

                    AuditEntry first = group.get(0);

                    VBox block = new VBox(4);
                    block.setStyle("-fx-padding: 4 0 8 0;");

                    Label header = new Label(
                            first.getModifiedBy() + " | " +
                                    first.getModifiedAt().format(formatter)
                    );

                    header.setStyle(
                            "-fx-font-weight: bold;" +
                                    "-fx-font-size: 14px;"
                    );

                    block.getChildren().add(header);

                    for (AuditEntry e : group) {

                        Label line = new Label(
                                e.getFieldName() +
                                        " : " +
                                        e.getOldValue() +
                                        " → " +
                                        e.getNewValue()
                        );

                        line.setStyle("-fx-font-size: 13px;");
                        block.getChildren().add(line);
                    }

                    container.getChildren().add(block);

                    Separator separator = new Separator();
                    separator.setStyle(
                            "-fx-background-color: #bbbbbb;" +
                                    "-fx-opacity: 0.6;"
                    );

                    container.getChildren().add(separator);
                }

                if (!container.getChildren().isEmpty()) {
                    container.getChildren().remove(container.getChildren().size() - 1);
                }
            }
        });
    }

    @FXML
    private void handleExportExcel() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(itemHistoryTable.getScene().getWindow());

        if (file != null) {
            ExportUtil.exportToExcel(
                    itemHistoryTable.getItems(),
                    file.getAbsolutePath()
            );
        }
    }

    @FXML
    private void handleExportPDF() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(itemHistoryTable.getScene().getWindow());

        if (file != null) {
            ExportUtil.exportToPDF(
                    itemHistoryTable.getItems(),
                    file.getAbsolutePath()
            );
        }
    }

    public void loadHistory(String field, String value, String title) {

        this.currentField = field;
        this.currentValue = value;
        this.currentTitle = title;

        titleLabel.setText("History for: " + value);

        List<TransactionHistory> historyList =
                transactionDAO.getTransactionsByField(field, value);

        itemHistoryTable.setItems(
                FXCollections.observableArrayList(historyList)
        );
    }

    private void centerAllColumns(TableView<TransactionHistory> table) {

        for (TableColumn<?, ?> column : table.getColumns()) {

            column.setCellFactory(col -> new TableCell() {

                @Override
                protected void updateItem(Object item, boolean empty) {

                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }

                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            });
        }
    }
}