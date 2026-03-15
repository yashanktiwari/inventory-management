package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.database.ConnectionState;
import com.inventory.database.DBConnection;
import com.inventory.model.TransactionHistory;
import com.inventory.service.DatabaseConnectionMonitor;
import com.inventory.ui.dialog.*;
import com.inventory.ui.table.*;
import com.inventory.util.*;
import com.inventory.util.TableFreezeManager;
import com.inventory.ui.factory.DashboardRowFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.SplitPane;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.collections.ListChangeListener;
import org.controlsfx.control.table.ColumnFilter;
import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.textfield.CustomTextField;

public class DashboardController {

    @FXML private CustomTextField searchField;
    @FXML private TableView<TransactionHistory> historyTable;
    @FXML private TableColumn<TransactionHistory, Integer> serialColumn;
    @FXML private VBox tableContainer;

    // 🔹 Transaction Info
    @FXML private TableColumn<TransactionHistory, String> buySellColumn;
    @FXML private TableColumn<TransactionHistory, String> plantColumn;
    @FXML private TableColumn<TransactionHistory, String> departmentColumn;
    @FXML private TableColumn<TransactionHistory, String> locationColumn;
    @FXML private TableColumn<TransactionHistory, String> itemLocationColumn;
    @FXML private TableColumn<TransactionHistory, String> itemCategoryColumn;

    // 🔹 Employee Info
    @FXML private TableColumn<TransactionHistory, String> employeeCodeColumn;
    @FXML private TableColumn<TransactionHistory, String> employeeNameColumn;

    // 🔹 Network / Device
    @FXML private TableColumn<TransactionHistory, String> ipAddressColumn;

    // 🔹 Item Info
    @FXML private TableColumn<TransactionHistory, String> itemCodeColumn;
    @FXML private TableColumn<TransactionHistory, String> itemNameColumn;
    @FXML private TableColumn<TransactionHistory, String> itemMakeColumn;
    @FXML private TableColumn<TransactionHistory, String> itemModelColumn;
    @FXML private TableColumn<TransactionHistory, String> itemSerialColumn;
    @FXML private TableColumn<TransactionHistory, String> itemConditionColumn;
    @FXML private TableColumn<TransactionHistory, Double> itemCountColumn;
    @FXML private TableColumn<TransactionHistory, String> unitColumn;

    // 🔹 SIM / IMEI
    @FXML private TableColumn<TransactionHistory, String> imeiColumn;
    @FXML private TableColumn<TransactionHistory, String> simColumn;

    // 🔹 Purchase Info
    @FXML private TableColumn<TransactionHistory, String> poColumn;
    @FXML private TableColumn<TransactionHistory, String> partyColumn;

    // 🔹 Status
    @FXML private TableColumn<TransactionHistory, String> statusColumn;

    // 🔹 Dates
    @FXML private TableColumn<TransactionHistory, String> issuedColumn;
    @FXML private TableColumn<TransactionHistory, String> returnedColumn;

    // 🔹 Remarks
    @FXML private TableColumn<TransactionHistory, String> remarksColumn;

    // 🔹 Actions
    @FXML private TableColumn<TransactionHistory, Void> actionColumn;
    @FXML private TableColumn<TransactionHistory, Void> deleteColumn;
    @FXML private TableColumn<TransactionHistory, String> auditColumn;

    // 🔹 Connection Status
    @FXML private javafx.scene.shape.Circle statusDot;
    @FXML private Label statusLabel;

    // 🔹 Buttons
    @FXML private Button addTransactionButton;
    @FXML private Button exportExcelButton;
    @FXML private Button exportPDFButton;
    @FXML private Button refreshButton;
    @FXML private Button resetFiltersButton;
    @FXML private MenuItem freezeColumnsMenuItem;
    @FXML private MenuItem unfreezeColumnsMenuItem;
    @FXML private MenuItem setupDatabase;
    @FXML private MenuItem backupDatabase;
    @FXML private MenuItem restoreDatabase;
    @FXML public MenuItem importFromExcel;
    @FXML private TableColumn<TransactionHistory, Void> attachmentColumn;
    @FXML private Label recordCountLabel;

    @FXML private TabPane dashboardTabs;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    private String currentTab = "BUY";
    private ObservableList<TransactionHistory> masterData;
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private final Preferences prefs =
            Preferences.userNodeForPackage(DashboardController.class);
    private boolean columnsFrozen = false;
    private Node originalCenter;
    private boolean lastConnectionState = false;
    private TableFreezeManager<TransactionHistory> freezeManager;
    private BorderPane rootPane;
    private TableFilter<TransactionHistory> tableFilter;
    private TableFilter<TransactionHistory> frozenTableFilter;
    private final Map<String, Set<String>> activeFilters = new HashMap<>();
    private FilteredList<TransactionHistory> filterPipeline;
    private AttachmentManager attachmentManager;
    private TableView<TransactionHistory> summarySourceTable;
    private ListChangeListener<TransactionHistory> currentFilterListener;
    private ObservableList<TransactionHistory> currentFilteredItems;
    private final ListChangeListener<TransactionHistory> summaryListListener =
            change -> updateSummary();
    private final DatabaseConnectionMonitor connectionMonitor =
            new DatabaseConnectionMonitor();
    private TableColumnPreferenceManager<TransactionHistory> columnPrefs;
    private final ChangeListener<ObservableList<TransactionHistory>> summaryItemsListener =
            (obs, oldItems, newItems) -> {
                if (oldItems != null) {
                    oldItems.removeListener(summaryListListener);
                }
                if (newItems != null) {
                    newItems.addListener(summaryListListener);
                }
                updateSummary();
            };

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Scene scene = historyTable.getScene();
            if (scene != null && scene.getStylesheets().stream()
                    .noneMatch(s -> s.contains("dashboard.css"))) {
                scene.getStylesheets().add(
                        getClass()
                                .getResource("/css/dashboard.css")
                                .toExternalForm()
                );
            }
        });

        initializeColumnPreferences();

        ToggleGroup tabGroup = new ToggleGroup();

        dashboardTabs.getSelectionModel()
                .selectedIndexProperty()
                .addListener((obs, oldVal, newVal) -> {

                    switch (newVal.intValue()) {

                        case 0 -> {
                            currentTab = "BUY";
                        }
                        case 1 -> {
                            currentTab = "IN STOCK";
                        }
                        case 2 -> {
                            currentTab = "ISSUED";
                        }
                        case 3 -> {
                            currentTab = "SCRAPPED";
                        }
                        case 4 -> {
                            currentTab = "RETURNED";
                        }
                    }

                    searchField.clear();

                    if (tableFilter != null) {
                        tableFilter.getColumnFilters().forEach(ColumnFilter::selectAllValues);
                        tableFilter.executeFilter();
                        updateSummary();
                    }

                    historyTable.getProperties().put(
                            "columnOrderKey",
                            currentTab + "_dashboardColumnOrder"
                    );

                    columnPrefs.setSavingEnabled(false);
                    historyTable.getColumns().setAll(
                            serialColumn,
                            buySellColumn,
                            plantColumn,
                            departmentColumn,
                            locationColumn,
                            itemLocationColumn,
                            itemCategoryColumn,
                            employeeCodeColumn,
                            employeeNameColumn,
                            ipAddressColumn,
                            itemCodeColumn,
                            itemNameColumn,
                            itemMakeColumn,
                            itemModelColumn,
                            itemSerialColumn,
                            itemConditionColumn,
                            itemCountColumn,
                            unitColumn,
                            imeiColumn,
                            simColumn,
                            poColumn,
                            partyColumn,
                            statusColumn,
                            issuedColumn,
                            returnedColumn,
                            remarksColumn,
                            actionColumn,
                            deleteColumn,
                            auditColumn,
                            attachmentColumn
                    );
                    columnPrefs.setSavingEnabled(true);
                    columnPrefs.restoreForKey(currentTab + "_dashboardColumnOrder");
                    restoreColumnVisibility();
                    Platform.runLater(this::rebuildTableFilter);
                    loadHistory();
                });


        dashboardTabs.widthProperty().addListener((obs, oldVal, newVal) -> {

            int tabCount = dashboardTabs.getTabs().size();

            double tabWidth = newVal.doubleValue() / tabCount;

            dashboardTabs.setTabMinWidth(tabWidth);
            dashboardTabs.setTabMaxWidth(tabWidth);

        });

        attachmentManager = new AttachmentManager();
        historyTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        historyTable.setRowFactory(table ->
                DashboardRowFactory.createRow(
                        table,
                        currentTab,
                        this::openSellTransaction,
                        this::openEditTransaction
                )
        );

        freezeManager = new TableFreezeManager<>(historyTable);

        Platform.runLater(() -> {
            rootPane = (BorderPane) historyTable.getScene().getRoot();
            originalCenter = rootPane.getCenter();
        });

        historyTable.setFixedCellSize(28);
        centerAllColumns(historyTable);

        DashboardTableConfigurator.configureBasicColumns(
                buySellColumn,
                plantColumn,
                departmentColumn,
                locationColumn,
                itemLocationColumn,
                itemCategoryColumn,
                statusColumn,
                remarksColumn,
                row -> openHistoryPage("buy_sell", row.getBuySell(), row.getBuySell()),
                row -> openHistoryPage("plant", row.getPlant(), row.getPlant()),
                row -> openHistoryPage("department", row.getDepartment(), row.getDepartment()),
                row -> openHistoryPage("location", row.getLocation(), row.getLocation()),
                row -> openHistoryPage("item_location", row.getItemLocation(), row.getItemLocation()),
                row -> openHistoryPage("item_category", row.getItemCategory(), row.getItemCategory())
        );

        DashboardTableConfigurator.configureDateColumns(
                issuedColumn,
                returnedColumn,
                formatter
        );

        DashboardEmployeeColumns.configure(
                employeeCodeColumn,
                employeeNameColumn,
                row -> openHistoryPage("employee_id", row.getEmployeeCode(), row.getEmployeeCode()),
                row -> openHistoryPage("employee_name", row.getEmployeeName(), row.getEmployeeName())
        );

        DashboardItemColumns.configure(
                itemCodeColumn,
                itemNameColumn,
                itemMakeColumn,
                itemModelColumn,
                itemSerialColumn,
                itemConditionColumn,
                itemCountColumn,
                unitColumn,
                row -> openHistoryPage("item_code", row.getItemCode(), row.getItemCode()),
                row -> openHistoryPage("item_name", row.getItemName(), row.getItemName()),
                row -> openHistoryPage("item_make", row.getItemMake(), row.getItemMake()),
                row -> openHistoryPage("item_model", row.getItemModel(), row.getItemModel()),
                row -> openHistoryPage("item_serial", row.getItemSerial(), row.getItemSerial()),
                row -> openHistoryPage("item_condition", row.getItemCondition(), row.getItemCondition())
        );

        DashboardDeviceColumns.configure(
                ipAddressColumn,
                imeiColumn,
                simColumn,
                row -> openHistoryPage("ip_address", row.getIpAddress(), row.getIpAddress()),
                row -> openHistoryPage("imei_no", row.getImeiNo(), row.getImeiNo()),
                row -> openHistoryPage("sim_no", row.getSimNo(), row.getSimNo())
        );

        DashboardPurchaseColumns.configure(
                poColumn,
                partyColumn,
                row -> openHistoryPage("po_no", row.getPoNo(), row.getPoNo()),
                row -> openHistoryPage("party_name", row.getPartyName(), row.getPartyName())
        );

        DashboardActionColumns.configure(
                serialColumn,
                actionColumn,
                deleteColumn,
                auditColumn,
                attachmentColumn,

                historyTable,
                transactionDAO,
                attachmentManager,
                v -> loadHistory(),
                formatter,
                currentTab,
                row -> openHistoryPage(
                        "transaction_id",
                        String.valueOf(row.getTransactionId()),
                        "Transaction " + row.getTransactionId()
                )
        );

        // Search Field
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyDateFilter());
        Button clearButton = new Button("✖");
        clearButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #888;" +
                        "-fx-font-size: 11;"
        );
        clearButton.setCursor(Cursor.HAND);
        clearButton.setOnAction(e -> searchField.clear());
        // show only when text exists
        clearButton.visibleProperty().bind(searchField.textProperty().isNotEmpty());
        searchField.setRight(clearButton);

        restoreColumnVisibility();

        masterData = FXCollections.observableArrayList();

        filterPipeline = new FilteredList<>(masterData, p -> true);

        SortedList<TransactionHistory> sortedData =
                new SortedList<>(filterPipeline);

        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());

        historyTable.setItems(sortedData);

        attachSummaryListeners(historyTable);
        updateSummary();

        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyDateFilter());

        loadHistory();

        Platform.runLater(() -> {
            tableFilter = TableFilter.forTableView(historyTable).apply();
            attachFilterListener(historyTable);
            
            // Listen to column changes to detect reordering and rebuild filter like freeze/unfreeze does
            historyTable.getColumns().addListener((ListChangeListener<TableColumn<TransactionHistory, ?>>) change -> {
                if (tableFilter != null) {
                    tableFilter.getColumnFilters().forEach(ColumnFilter::selectAllValues);
                    tableFilter.executeFilter();
                }
                Platform.runLater(this::rebuildTableFilter);
            });

            Platform.runLater(() -> {
                restoreFilters();
                updateSummary();
            });

            historyTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                Platform.runLater(this::updateSummary);
            });

            historyTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) return;
                newScene.getWindow().setOnShown(e -> {

                    newScene.getRoot().lookupAll(".list-view").forEach(node -> {
                        if (node instanceof ListView<?> list) {
                            int size = list.getItems().size();
                            int visibleRows = Math.min(size, 7);
                            list.setFixedCellSize(24);
                            list.setPrefHeight(visibleRows * 24 + 4);
                        }
                    });
                });
            });
        });
        unfreezeColumnsMenuItem.setDisable(true);

        historyTable.comparatorProperty().addListener((obs, o, n) -> updateSummary());

        connectionMonitor.start(connected -> {

            if (connected) {

                statusDot.setStyle("-fx-fill:#2ecc71;");
                statusLabel.setText("Database connected");

                if (!lastConnectionState) {
                    loadHistory();
                }

            } else {

                statusDot.setStyle("-fx-fill:#e74c3c;");
                statusLabel.setText("Not Connected");

            }

            lastConnectionState = connected;

            updateUIState(connected);
            refreshButton.setDisable(false);

        });

        ConnectionState.connectedProperty().addListener((obs, oldVal, connected) -> {
            updateUIState(connected);
        });

        historyTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            Platform.runLater(() -> {
                ScrollBar hBar =
                        (ScrollBar) historyTable.lookup(".scroll-bar:horizontal");
                if (hBar != null) {
                    hBar.setDisable(false);
                    hBar.setVisible(true);
                    hBar.setManaged(true);
                }
            });
        });

        updateUIState(ConnectionState.isConnected());
    }

    @FXML
    private void handleRefresh() {
        runDbTask(this::loadHistory);
    }

    @FXML
    private void handleAddItem() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/add-item.fxml")
            );

            Scene scene = new Scene(loader.load(), 400, 250);
            scene.getRoot().disableProperty().bind(
                    ConnectionState.connectedProperty().not()
            );
            Stage stage = new Stage();
            stage.setTitle("Add Item");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadHistory(); // refresh after closing popup

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTransaction() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/add-transaction.fxml")
            );

//            Scene scene = new Scene(loader.load(), 700, 650);
            Scene scene = new Scene(loader.load());

            var css = getClass().getResource("/css/add-transaction.css");

            System.out.println(css);

            scene.getStylesheets().add(css.toExternalForm());

//            scene.getStylesheets().add(
//                    getClass().getResource("/css/add-transaction.css").toExternalForm()
//            );

            AddTransactionController controller = loader.getController();
            controller.setOnTransactionSaved(this::loadHistory);

            controller.setTransactionType("Buy");

            scene.getRoot().disableProperty().bind(
                    ConnectionState.connectedProperty().not()
            );
            Stage stage = new Stage();
            stage.setTitle("Add Transaction");
            stage.setScene(scene);
            stage.sizeToScene();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            loadHistory(); // refresh table

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportExcel() {


        try {
            Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
            choiceDialog.setTitle("Export Options");
            choiceDialog.setHeaderText("Choose export scope");
            choiceDialog.setContentText("What would you like to export");

            ButtonType allDataButton = new ButtonType("All Transactions");
            ButtonType currentTableButton = new ButtonType("Current Table only");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            choiceDialog.getButtonTypes().setAll(allDataButton, currentTableButton, cancelButton);

            Optional<ButtonType> result = choiceDialog.showAndWait();

            if(result.isEmpty() || result.get() == cancelButton) {
                return;
            }

            List<TransactionHistory> datatoExport;

            if(result.get() == allDataButton) {
                datatoExport = transactionDAO.getAllTransactions();
            } else {
                datatoExport = new ArrayList<>(historyTable.getItems());
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );

            File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());

            if (file == null) {
                return;
            }


            ExportUtil.exportToExcel(
                    datatoExport,
                    file.getAbsolutePath()
            );

            AlertUtil.showInfo("Export Completed", "Excel file exported successfully.");

        } catch (Exception e) {
            e.printStackTrace();

            AlertUtil.showError(
                    "Export Failed",
                    e.getClass().getSimpleName() + "\n" + e.getMessage()
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

        File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());

        if (file == null) {
            return;
        }

        String path = file.getAbsolutePath();

        if (!path.toLowerCase().endsWith(".pdf")) {
            path += ".pdf";
        }

        try {

            ExportUtil.exportToPDF(
                    historyTable.getItems(),
                    path
            );

            showExportSuccessDialog("PDF", path);

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.showError(
                    "Export Failed",
                    "Unable to export PDF file."
            );
        }
    }

    @FXML
    private void handleBackupDatabase() {

        DatabaseBackupDialog.showBackupDialog(
                historyTable,
                historyTable.getScene().getWindow()
        );
    }

    @FXML
    private void handleRestoreDatabase() {

        DatabaseBackupDialog.showRestoreDialog(
                historyTable,
                historyTable.getScene().getWindow(),
                this::loadHistory
        );
    }

    @FXML
    private void handleImportExcel() {
        ExcelImportDialog.showImportDialog(
                historyTable.getScene().getWindow(),
                this::loadHistory
        );
    }

    @FXML
    private void handleFreezeColumns() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Freeze Columns");
        dialog.setHeaderText("Freeze first N columns");
        dialog.getDialogPane().setPrefWidth(420);

        ButtonType freezeBtn =
                new ButtonType("Freeze", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(freezeBtn, ButtonType.CANCEL);

        Label icon = new Label("📌");
        icon.setStyle("-fx-font-size: 20;");

        TextField inputField = new TextField("2");
        inputField.setPromptText("Number of columns");
        inputField.setPrefWidth(200);

        HBox content = new HBox(12);
        content.getChildren().addAll(icon, inputField);
        content.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.getChildren().add(content);

        dialog.getDialogPane().setContent(container);

        Button freezeButton =
                (Button) dialog.getDialogPane().lookupButton(freezeBtn);

        freezeButton.setStyle(
                "-fx-background-color: #0078D7;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );

        inputField.requestFocus();

        dialog.setResultConverter(btn -> {
            if (btn == freezeBtn) {
                return inputField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(input -> {

            try {

                int count = Integer.parseInt(input);

                int totalColumns = historyTable.getColumns().size();

                if (count <= 0 || count >= totalColumns) {
                    throw new IllegalArgumentException();
                }

                if (tableFilter != null) {
                    tableFilter.getColumnFilters().forEach(cf -> cf.selectAllValues());
                    tableFilter.executeFilter();
                    updateSummary();
                }
                
                // Clear saved filters so they don't get restored after freezing
                tableFilter.getColumnFilters().forEach(columnFilter -> {
                    String columnId = columnFilter.getTableColumn().getId();
                    if (columnId != null) {
                        prefs.remove("filter_" + columnId);
                    }
                });

                // Reset filterPipeline to show all data temporarily
                filterPipeline.setPredicate(p -> true);

                SplitPane pane = freezeManager.freezeColumns(count);
                VBox.setVgrow(pane, Priority.ALWAYS);

                VBox centerBox = (VBox) rootPane.getCenter();

                int tableIndex = centerBox.getChildren().indexOf(historyTable);

                if (tableIndex == -1) {
                    tableIndex = centerBox.getChildren().size() - 1;
                }

                centerBox.getChildren().set(tableIndex, pane);

                Platform.runLater(this::rebuildTableFilter);

                columnsFrozen = true;
                updateUIState(ConnectionState.isConnected());

                exportExcelButton.setDisable(true);
                exportPDFButton.setDisable(true);
                addTransactionButton.setDisable(true);

                freezeColumnsMenuItem.setDisable(true);
                unfreezeColumnsMenuItem.setDisable(false);

            } catch (NumberFormatException e) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please enter a valid number.");
                alert.show();

            } catch (IllegalArgumentException e) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid number of columns.");
                alert.show();
            }
        });
    }

    @FXML
    private void handleUnfreezeColumns() {
        // Clear saved filters so they don't get restored after unfreezing
        if (tableFilter != null) {
            tableFilter.getColumnFilters().forEach(columnFilter -> {
                String columnId = columnFilter.getTableColumn().getId();
                if (columnId != null) {
                    prefs.remove("filter_" + columnId);
                }
            });
        }
        
        freezeManager.restoreOriginalTable();

        VBox centerBox = (VBox) rootPane.getCenter();

        int paneIndex = centerBox.getChildren().size() - 1;

        centerBox.getChildren().set(paneIndex, historyTable);

        columnsFrozen = false;
        updateUIState(ConnectionState.isConnected());
        addTransactionButton.setDisable(false);
        exportExcelButton.setDisable(false);
        exportPDFButton.setDisable(false);
        freezeColumnsMenuItem.setDisable(false);
        unfreezeColumnsMenuItem.setDisable(true);
        Platform.runLater(this::rebuildTableFilter);
    }

    @FXML
    private void handleResetFilters() {

        if (tableFilter == null) return;

        tableFilter.getColumnFilters().forEach(columnFilter -> {
            columnFilter.selectAllValues();
        });

        tableFilter.executeFilter();

        historyTable.refresh();

        updateSummary();

        tableFilter.getColumnFilters().forEach(columnFilter -> {
            String columnId = columnFilter.getTableColumn().getId();
            if (columnId != null) {
                prefs.remove("filter_" + columnId);
            }
        });
    }

    @FXML
    private void handleChangeAttachmentPath() {

        StoragePathDialog.show(
                (Stage) historyTable.getScene().getWindow()
        );
    }

    @FXML
    public void handleChangeAdminPassword() {
        AdminPasswordDialog.show(
                historyTable.getScene().getWindow()
        );
    }

    @FXML
    public void handleOpenInventory() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/inventory.fxml")
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Inventory");
            stage.setScene(new Scene(root, 950, 650));
            stage.initOwner(stage.getOwner());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearDateFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
    }

    @FXML
    private void handleColumnVisibility() {

        ColumnVisibilityDialog.show(
                historyTable,
                DashboardController.class,
                currentTab,
                List.of("BUY", "IN STOCK", "ISSUED", "SCRAPPED", "RETURNED")
        );

        // reset filters safely
        if (tableFilter != null) {
            tableFilter.getColumnFilters().forEach(ColumnFilter::selectAllValues);
        }

        // rebuild filter to sync hidden columns
        Platform.runLater(this::rebuildTableFilter);

        historyTable.refresh();
    }

    @FXML
    private void handleOpenMasterDialog() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/master-dialog.fxml")
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Master Data Management");
            stage.setScene(new Scene(root, 650, 450));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSetupDatabase() {
        DatabaseSetupDialog.show(
                historyTable.getScene().getWindow(),
                this::loadHistory
        );
    }

    @FXML
    private void handleWorkLog() {
        WorkLogDialog.show();
    }

    private void applyDateFilter() {

        if (filterPipeline == null) return;

        filterPipeline.setPredicate(history -> {

            // Search filter
            String searchText = searchField.getText();
            boolean matchesSearch = DashboardSearchFilter.matches(history, searchText);

            // Date filter
            if (fromDatePicker.getValue() == null && toDatePicker.getValue() == null) {
                return matchesSearch;
            }

            if (history.getIssuedDateTime() == null) {
                return false;
            }

            LocalDate txDate = history.getIssuedDateTime().toLocalDate();

            LocalDate from = fromDatePicker.getValue();
            LocalDate to = toDatePicker.getValue();

            boolean afterFrom = (from == null) || !txDate.isBefore(from);
            boolean beforeTo = (to == null) || !txDate.isAfter(to);

            return matchesSearch && afterFrom && beforeTo;
        });
    }

    private void rebuildTableFilter() {
        if (columnsFrozen) {
            // Apply TableFilter only to scroll table
            TableView<TransactionHistory> scrollTable = freezeManager.getScrollTable();
            TableView<TransactionHistory> frozenTable = freezeManager.getFrozenTable();

            if (scrollTable != null && frozenTable != null) {
                // Apply filter to scroll table only
                tableFilter = TableFilter.forTableView(scrollTable).apply();
                attachSummaryListeners(scrollTable);
                attachFilterListener(scrollTable);

                frozenTable.setItems(scrollTable.getItems());

                // Add listener to sync frozen table when scroll table change (due to sorting)
                scrollTable.itemsProperty().addListener((obs, oldItems, newItems) -> {
                    if(newItems != null) {
                        frozenTable.setItems(newItems);
                    }
                });
                
                // Listen to column changes to detect reordering and re-attach filter listener
                scrollTable.getColumns().addListener((ListChangeListener<TableColumn<TransactionHistory, ?>>) change -> {
                    attachFilterListener(scrollTable);
                });

                // Restore saved filter state
                restoreFilters();

                // Execute filters
                tableFilter.executeFilter();

                // Sync frozen table items after filter executes
                Platform.runLater(() -> {
                    frozenTable.setItems(scrollTable.getItems());
                });
            }
        } else {
            // Normal mode - single table
            historyTable.refresh();
            tableFilter = TableFilter.forTableView(historyTable).apply();
            attachSummaryListeners(historyTable);
            attachFilterListener(historyTable);
            
            // Listen to column changes to detect reordering and re-attach filter listener
            historyTable.getColumns().addListener((ListChangeListener<TableColumn<TransactionHistory, ?>>) change -> {
                attachFilterListener(historyTable);
            });
            
            restoreFilters();
            tableFilter.executeFilter();
            updateSummary();
        }
    }

    private void attachSummaryListeners(TableView<TransactionHistory> table) {
        if (summarySourceTable == table) {
            return;
        }

        if (summarySourceTable != null) {
            summarySourceTable.itemsProperty().removeListener(summaryItemsListener);
            ObservableList<TransactionHistory> oldItems = summarySourceTable.getItems();
            if (oldItems != null) {
                oldItems.removeListener(summaryListListener);
            }
        }

        summarySourceTable = table;
        table.itemsProperty().addListener(summaryItemsListener);

        ObservableList<TransactionHistory> items = table.getItems();
        if (items != null) {
            items.addListener(summaryListListener);
        }
    }

    private void attachFilterListener(TableView<TransactionHistory> table) {
        ObservableList<TransactionHistory> filteredItems = table.getItems();
        if (filteredItems != null) {
            // Remove old listener from the old items object if it exists
            if (currentFilteredItems != null && currentFilterListener != null) {
                currentFilteredItems.removeListener(currentFilterListener);
            }
            
            // Create and attach new listener
            currentFilterListener = (ListChangeListener<TransactionHistory>) change -> {
                updateSummary();
            };
            filteredItems.addListener(currentFilterListener);
            currentFilteredItems = filteredItems;
        }
    }

    private void loadHistory() {
        if (!DBConnection.isDatabaseSet()) return;
        List<TransactionHistory> data;
        switch (currentTab) {
            case "BUY" -> data = transactionDAO.getBuyTransactions();
            case "IN STOCK" -> data = transactionDAO.getInStockTransactions();
            case "ISSUED" -> data = transactionDAO.getIssuedTransactions();
            case "SCRAPPED" -> data = transactionDAO.getScrappedTransactions();
            case "RETURNED" -> data = transactionDAO.getReturnedTransactions();
            default -> data = transactionDAO.getAllTransactions();
        }

        Platform.runLater(() -> {
            masterData.setAll(data);

            if (tableFilter == null) {
                Platform.runLater(this::rebuildTableFilter);
            } else {
                tableFilter.executeFilter();
                updateSummary();
            }

            if(columnsFrozen) {
                TableView<TransactionHistory> scrollTable = freezeManager.getScrollTable();
                TableView<TransactionHistory> frozenTable = freezeManager.getFrozenTable();
                if(scrollTable != null && frozenTable != null) {
                    frozenTable.setItems(scrollTable.getItems());
                }
            }

            historyTable.refresh();

            Platform.runLater(this::updateSummary);
        });
    }

    private void openHistoryPage(String field, String value, String title) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/item-history.fxml")
            );

            Parent root = loader.load();

            ItemHistoryController controller = loader.getController();
            controller.loadHistory(field, value, title);

            Stage stage = new Stage();
            stage.setTitle("Transaction History");
            stage.setScene(new Scene(root, 1200, 650));
            stage.setOnCloseRequest(event -> {
                loadHistory();
            });
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleExit() {
        connectionMonitor.stop();
        Platform.exit();
        System.exit(0);
    }

    public void handleAbout() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setGraphic(null);

        Label icon = new Label("📦");
        icon.setStyle("-fx-font-size: 36;");

        Label appName = new Label("Inventory Management System");
        appName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label version = new Label("Version 1.1.0");
        version.setStyle("-fx-font-size: 13px;");

        Label author = new Label("Developed by Yashank Tiwari");
        author.setStyle("-fx-font-size: 13px;");

        Label copyright = new Label("© 2026 Yashank Tiwari. All rights reserved.");
        copyright.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        VBox textBox = new VBox(6, appName, version, author, copyright);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox content = new HBox(20, icon, textBox);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(22, 28, 22, 28));

        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

    private <T> void centerColumn(TableColumn<TransactionHistory, T> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }

                setAlignment(Pos.CENTER);
            }
        });
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

                    setAlignment(Pos.CENTER);
                }
            });

        }
    }

    public void saveFilters() {

        if (tableFilter == null) return;

        tableFilter.getColumnFilters().forEach(columnFilter -> {

            String columnId = columnFilter.getTableColumn().getId();
            if (columnId == null) return;

            if (columnId.equals("serialColumn")
                    || columnId.equals("actionColumn")
                    || columnId.equals("deleteColumn")
                    || columnId.equals("attachmentColumn")
            ) {
                return;
            }

            var selected = columnFilter.getFilterValues().stream()
                    .filter(fv -> fv.selectedProperty().get())
                    .map(fv -> fv.getValue().toString())
                    .toList();

            var all = columnFilter.getFilterValues().stream()
                    .map(fv -> fv.getValue().toString())
                    .toList();

            // If everything is selected → remove preference (means no filter)
            if (selected.size() == all.size()) {
                prefs.remove("filter_" + columnId);
            } else {
                prefs.put("filter_" + columnId, String.join("|", selected));
            }
        });
    }

    private void restoreFilters() {

        if (tableFilter == null) return;

        tableFilter.getColumnFilters().forEach(columnFilter -> {

            String columnId = columnFilter.getTableColumn().getId();
            if (columnId == null) return;
            if (columnId.equals("serialColumn")
                    || columnId.equals("actionColumn")
                    || columnId.equals("deleteColumn")
                    || columnId.equals("attachmentColumn")
            ) {
                return;
            }

            String saved = prefs.get("filter_" + columnId, null);
            if (saved == null) return;

            var allowed = new HashSet<>(Arrays.asList(saved.split("\\|")));

            columnFilter.getFilterValues().forEach(fv -> {
                String value = fv.getValue().toString();
                fv.selectedProperty().set(allowed.contains(value));
            });
        });

        tableFilter.executeFilter();
    }

    private void captureFilters() {

        activeFilters.clear();

        tableFilter.getColumnFilters().forEach(columnFilter -> {


            String columnId = columnFilter.getTableColumn().getId();
            if (columnId == null) return;
            if (columnId.equals("serialColumn")
                    || columnId.equals("actionColumn")
                    || columnId.equals("deleteColumn")
                    || columnId.equals("attachmentColumn")
            ) {
                return;
            }

            Set<String> selected = columnFilter.getFilterValues().stream()
                    .filter(v -> v.selectedProperty().get())
                    .map(v -> v.getValue().toString())
                    .collect(Collectors.toSet());

            activeFilters.put(columnId, selected);

            prefs.put("filter_" + columnId, String.join("|", selected));
        });
    }

    private void runDbTask(Runnable task) {

        Thread thread = new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Platform.runLater(() ->
                        AlertUtil.showError("Database Error", e.getMessage()));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void updateUIState(boolean connected) {

        boolean frozen = columnsFrozen;

        addTransactionButton.setDisable(!connected || frozen);
        exportExcelButton.setDisable(!connected || frozen);
        exportPDFButton.setDisable(!connected || frozen);

        freezeColumnsMenuItem.setDisable(!connected || frozen);
        unfreezeColumnsMenuItem.setDisable(!connected || !frozen);

        searchField.setDisable(!connected);
        setupDatabase.setDisable(false);
        backupDatabase.setDisable(!connected);
        restoreDatabase.setDisable(!connected);
        resetFiltersButton.setDisable(!connected);

        if(rootPane != null) {
            rootPane.getCenter().setDisable(!connected);
        }
    }

    private TransactionPrefill convertToSellPrefill(TransactionHistory t) {
        TransactionPrefill p = new TransactionPrefill();

        p.buySell = "Sell";

        p.plant = t.getPlant();
        p.department = t.getDepartment();
        p.location = t.getLocation();

        p.employeeCode = t.getEmployeeCode();
        p.employeeName = t.getEmployeeName();

        p.ipAddress = t.getIpAddress();

        p.itemCode = t.getItemCode();
        p.itemName = t.getItemName();
        p.itemMake = t.getItemMake();
        p.itemModel = t.getItemModel();
        p.itemSerial = t.getItemSerial();
        p.itemCount = t.getItemCount();
        p.itemCondition = t.getItemCondition();
        p.itemLocation = t.getItemLocation();
        p.itemCategory = t.getItemCategory();

        p.imeiNo = t.getImeiNo();
        p.simNo = t.getSimNo();

        p.poNo = t.getPoNo();
        p.partyName = t.getPartyName();

        p.unit = t.getUnit();
        p.remarks = t.getRemarks();

        return p;
    }

    private TransactionPrefill convertToPrefill(TransactionHistory t) {

        TransactionPrefill p = new TransactionPrefill();

        p.buySell = t.getBuySell();

        p.plant = t.getPlant();
        p.department = t.getDepartment();
        p.location = t.getLocation();

        p.employeeCode = t.getEmployeeCode();
        p.employeeName = t.getEmployeeName();

        p.ipAddress = t.getIpAddress();

        p.itemCode = t.getItemCode();
        p.itemName = t.getItemName();
        p.itemMake = t.getItemMake();
        p.itemModel = t.getItemModel();
        p.itemSerial = t.getItemSerial();
        p.itemCount = t.getItemCount();
        p.itemCondition = t.getItemCondition();
        p.itemLocation = t.getItemLocation();
        p.itemCategory = t.getItemCategory();

        p.imeiNo = t.getImeiNo();
        p.simNo = t.getSimNo();

        p.poNo = t.getPoNo();
        p.partyName = t.getPartyName();

        p.status = t.getStatus();
        p.unit = t.getUnit();
        p.attachmentFile = t.getAttachmentFile();

        p.remarks = t.getRemarks();

        return p;
    }

    private void openSellTransaction(TransactionHistory t) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/add-transaction.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/add-transaction.css")).toExternalForm()
            );

            AddTransactionController controller = loader.getController();

            TransactionPrefill prefill = convertToSellPrefill(t);

            controller.prefillSell(prefill);
            controller.hideSaveAndAddAnotherButton();
            controller.setTransactionType("Sell");

            Stage stage = new Stage();
            stage.setTitle("Sell Item");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadHistory();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditTransaction(TransactionHistory t) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/add-transaction.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/add-transaction.css")).toExternalForm()
            );

            AddTransactionController controller = loader.getController();

            TransactionPrefill prefill = convertToPrefill(t);

            controller.prefill(prefill);

            controller.hideFieldsIfBuyTransaction(t);
            controller.setEditTransactionId(t.getTransactionId()); // ⭐ IMPORTANT
            controller.changeLabelAndTitle("Edit");

            Stage stage = new Stage();
            stage.setTitle("Edit Transaction");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadHistory();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MenuItem createMenuItem(String text) {

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 13px;");

        HBox box = new HBox(10, textLabel);
        box.setAlignment(Pos.CENTER_LEFT);

        // padding left/right for nicer spacing
        box.setPadding(new Insets(4, 18, 4, 18));

        MenuItem item = new MenuItem();
        item.setGraphic(box);

        return item;
    }

    private void updateSummary() {

        ObservableList<TransactionHistory> visibleItems;

        if (columnsFrozen) {
            TableView<TransactionHistory> scrollTable = freezeManager.getScrollTable();
            visibleItems = scrollTable == null ? FXCollections.observableArrayList() : scrollTable.getItems();
        } else {
            visibleItems = historyTable.getItems();
        }

        int totalRecords = visibleItems == null ? 0 : visibleItems.size();

        recordCountLabel.setText(String.valueOf(totalRecords));
    }

    private void showBuySellColumn(boolean visible) {
        buySellColumn.setVisible(visible);
//        buySellColumn.setManaged(visible);
    }

    private void spreadTabsEvenly() {

        int tabCount = dashboardTabs.getTabs().size();

        double tabPaneWidth = dashboardTabs.getWidth();

        double tabWidth = tabPaneWidth / tabCount;

        dashboardTabs.lookupAll(".tab").forEach(node -> {
            node.setStyle("-fx-pref-width: " + tabWidth + ";");
        });

    }

    private void showExportSuccessDialog(String type, String path) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Export Completed");
        alert.setHeaderText(type + " export completed successfully");

        alert.setContentText(
                "File saved at:\n\n" + path
        );

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);

        okButton.setStyle(
                "-fx-background-color:#2ecc71;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-weight:bold;"
        );

        alert.getDialogPane().setStyle(
                "-fx-font-size:14px;"
        );

        alert.showAndWait();
    }

    private void restoreColumnVisibility() {

        Preferences prefs =
                Preferences.userNodeForPackage(DashboardController.class);

        for (TableColumn<?, ?> col : historyTable.getColumns()) {

            if (col.getId() == null) continue;

            boolean visible =
                    prefs.getBoolean(currentTab + "_column_visible_" + col.getId(), true);

            col.setVisible(visible);
        }
    }

    private void initializeColumnPreferences() {

        if (columnPrefs != null) return;

        columnPrefs = new TableColumnPreferenceManager<>(
                historyTable,
                currentTab + "_dashboardColumnOrder",
                DashboardController.class,
                () -> !columnsFrozen
        );

        columnPrefs.initialize();
    }
}
