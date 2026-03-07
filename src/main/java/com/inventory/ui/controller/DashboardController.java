package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.database.AppConfig;
import com.inventory.database.DBConnection;
import com.inventory.model.Transaction;
import com.inventory.model.TransactionHistory;
import com.inventory.util.AlertUtil;
import com.inventory.util.TableFreezeManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.SplitPane;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import com.inventory.util.ExportUtil;
import org.controlsfx.control.table.TableFilter;

public class DashboardController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<TransactionHistory> historyTable;

    @FXML
    private TableColumn<TransactionHistory, Integer> serialColumn;

    // 🔹 Transaction Info
    @FXML
    private TableColumn<TransactionHistory, String> buySellColumn;

    @FXML
    private TableColumn<TransactionHistory, String> plantColumn;

    @FXML
    private TableColumn<TransactionHistory, String> departmentColumn;

    @FXML
    private TableColumn<TransactionHistory, String> locationColumn;

    // 🔹 Employee Info
    @FXML
    private TableColumn<TransactionHistory, String> employeeIdColumn;

    @FXML
    private TableColumn<TransactionHistory, String> employeeNameColumn;

    // 🔹 Network / Device
    @FXML
    private TableColumn<TransactionHistory, String> ipAddressColumn;

    // 🔹 Item Info
    @FXML
    private TableColumn<TransactionHistory, String> itemCodeColumn;

    @FXML
    private TableColumn<TransactionHistory, String> itemNameColumn;

    @FXML
    private TableColumn<TransactionHistory, String> itemMakeColumn;

    @FXML
    private TableColumn<TransactionHistory, String> itemModelColumn;

    @FXML
    private TableColumn<TransactionHistory, String> itemSerialColumn;

    // 🔹 SIM / IMEI
    @FXML
    private TableColumn<TransactionHistory, String> imeiColumn;

    @FXML
    private TableColumn<TransactionHistory, String> simColumn;

    // 🔹 Purchase Info
    @FXML
    private TableColumn<TransactionHistory, String> poColumn;

    @FXML
    private TableColumn<TransactionHistory, String> partyColumn;

    // 🔹 Status
    @FXML
    private TableColumn<TransactionHistory, String> statusColumn;

    // 🔹 Dates
    @FXML
    private TableColumn<TransactionHistory, String> issuedColumn;

    @FXML
    private TableColumn<TransactionHistory, String> returnedColumn;

    // 🔹 Remarks
    @FXML
    private TableColumn<TransactionHistory, String> remarksColumn;

    // 🔹 Actions
    @FXML
    private TableColumn<TransactionHistory, Void> actionColumn;

    @FXML
    private TableColumn<TransactionHistory, Void> deleteColumn;


    // 🔹 Connection Status
    @FXML
    private javafx.scene.shape.Circle statusDot;

    @FXML
    private Label statusLabel;


    // 🔹 Buttons
    @FXML
    private Button addTransactionButton;

    @FXML
    private Button exportExcelButton;

    @FXML
    private Button exportPDFButton;

    @FXML
    private Button refreshButton;

    @FXML
    private MenuItem freezeColumnsMenuItem;

    @FXML
    private MenuItem unfreezeColumnsMenuItem;


    private ObservableList<TransactionHistory> masterData;
    private FilteredList<TransactionHistory> filteredData;
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private String mysqldumpPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";
    private String mysqlPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";
    private static final String RESTORE_PASSWORD = "admin123";
    private final Preferences prefs =
            Preferences.userNodeForPackage(DashboardController.class);
    private boolean columnsFrozen = false;
    private Node originalCenter;

    private static final String COLUMN_ORDER_KEY = "dashboardColumnOrder";
    private TableFreezeManager<TransactionHistory> freezeManager;
    private BorderPane rootPane;

    @FXML
    public void initialize() {
        freezeManager = new TableFreezeManager<>(historyTable);

        Platform.runLater(() -> {
            rootPane = (BorderPane) historyTable.getScene().getRoot();
            originalCenter = rootPane.getCenter();
            restoreColumnOrder();
        });

        historyTable.getColumns().addListener(
                (javafx.collections.ListChangeListener<TableColumn<TransactionHistory, ?>>) change -> {
                    if (!columnsFrozen) {
                        saveColumnOrder();
                    }
                }

        );
        centerAllColumns(historyTable);

        actionColumn.setCellFactory(col -> new TableCell<>() {

                    private final Button updateBtn = new Button("Update");

                    {
                        updateBtn.setOnAction(event -> {

                            TransactionHistory history =
                                    getTableView().getItems().get(getIndex());

                            if (!"Sell".equalsIgnoreCase(history.getBuySell())) {
                                return;
                            }

                            ChoiceDialog<String> dialog =
                                    new ChoiceDialog<>("Returned", "Returned", "Scrap");

                            dialog.setTitle("Update Status");
                            dialog.setHeaderText("Select new status");

                            Optional<String> result = dialog.showAndWait();

                            result.ifPresent(status -> {

                                transactionDAO.updateTransactionStatus(
                                        history.getTransactionId(),
                                        status
                                );

                                loadHistory();
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
                        // BUY → always disabled
                        if ("Buy".equalsIgnoreCase(buySell)) {
                            updateBtn.setDisable(true);
                            updateBtn.setText("In Stock");

                        }
                        // SELL → depends on status
                        else if ("Sell".equalsIgnoreCase(buySell)) {
                            if ("Issued".equalsIgnoreCase(status)) {
                                updateBtn.setDisable(false);
                                updateBtn.setText("Issued");
                            } else if ("Returned".equalsIgnoreCase(status)) {
                                updateBtn.setDisable(true);
                                updateBtn.setText("Returned");
                            } else if ("Scrap".equalsIgnoreCase(status) || "Scrapped".equalsIgnoreCase(status)) {
                                updateBtn.setDisable(true);
                                updateBtn.setText("Scrapped");
                            } else {
                                updateBtn.setDisable(true);
                                updateBtn.setText(status);
                            }
                        }
                        setGraphic(updateBtn);
                    }
                });

        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setStyle(
                        "-fx-background-color: #ff4d4d;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;"
                );
                deleteButton.setOnAction(event -> {
                    TransactionHistory data =
                            getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirm Delete");
                    alert.setHeaderText("Delete Transaction?");
                    alert.setContentText("Are you sure you want to delete this record?");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            transactionDAO.deleteTransaction(
                                    data.getTransactionId()
                            );
                            loadHistory();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });


        // 🔹 Serial Column
        serialColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        historyTable.getItems().indexOf(cellData.getValue()) + 1
                ).asObject()
        );


        // 🔹 Bind Columns to New Model
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
                    setAlignment(Pos.CENTER);
                    if ("Buy".equalsIgnoreCase(value)) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: black;");
                    } else if ("Sell".equalsIgnoreCase(value)) {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: black;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        plantColumn.setCellValueFactory(new PropertyValueFactory<>("plant"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        itemCodeColumn.setCellFactory(column -> new TableCell<>() {

            private final Hyperlink link = new Hyperlink();

            {
                link.setOnAction(event -> {

                    TransactionHistory history =
                            getTableView().getItems().get(getIndex());

                    openItemHistoryPage(
                            history.getItemCode(),
                            history.getItemName()
                    );
                });
            }

            @Override
            protected void updateItem(String itemCode, boolean empty) {

                super.updateItem(itemCode, empty);

                if (empty || itemCode == null) {
                    setGraphic(null);
                } else {
                    link.setText(itemCode);
                    setGraphic(link);
                }
            }
        });
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemMakeColumn.setCellValueFactory(new PropertyValueFactory<>("itemMake"));
        itemModelColumn.setCellValueFactory(new PropertyValueFactory<>("itemModel"));
        itemSerialColumn.setCellValueFactory(new PropertyValueFactory<>("itemSerial"));

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
                    setAlignment(Pos.CENTER);

                    switch (value.toLowerCase()) {

                        case "issued" ->
                                setStyle("-fx-background-color:#d6eaff; -fx-text-fill: black;");

                        case "returned" ->
                                setStyle("-fx-background-color:#d4edda; -fx-text-fill: black;");

                        case "scrap" ->
                                setStyle("-fx-background-color:#e0e0e0; -fx-text-fill: black;");

                        case "in stock" ->
                                setStyle("-fx-background-color:#fff3cd; -fx-text-fill: black;");
                    }
                }
            }
        });

        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        // 🔹 Date Formatting
        issuedColumn.setCellValueFactory(cellData -> {

            String raw = cellData.getValue().getIssuedDateTime();

            if (raw == null) {
                return new SimpleStringProperty("");
            }

            LocalDateTime dateTime =
                    Timestamp.valueOf(raw).toLocalDateTime();

            return new SimpleStringProperty(
                    dateTime.format(formatter)
            );
        });

        returnedColumn.setCellValueFactory(cellData -> {

            TransactionHistory history = cellData.getValue();
            // If item was bought → show --
            if ("Buy".equalsIgnoreCase(history.getBuySell())) {
                return new SimpleStringProperty("--");
            }
            String raw = history.getReturnedDateTime();
            // If Sell but not yet returned
            if (raw == null) {
                return new SimpleStringProperty("Not Returned");
            }
            LocalDateTime dateTime =
                    Timestamp.valueOf(raw).toLocalDateTime();

            return new SimpleStringProperty(
                    dateTime.format(formatter)
            );
        });

        // 🔹 Search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {

            if (filteredData == null) return;

            filteredData.setPredicate(history -> {
                if (newValue == null || newValue.isBlank()) {
                    return true;
                }
                String keyword = newValue.toLowerCase();
                return
                        (history.getBuySell() != null &&
                                history.getBuySell().toLowerCase().contains(keyword)
                                || (history.getPlant() != null &&
                                history.getPlant().toLowerCase().contains(keyword))
                                || (history.getDepartment() != null &&
                                history.getDepartment().toLowerCase().contains(keyword))
                                ||      (history.getLocation() != null &&
                                history.getLocation().toLowerCase().contains(keyword))
                                || (history.getEmployeeId() != null &&
                                history.getEmployeeId().toLowerCase().contains(keyword))
                                ||      (history.getEmployeeName() != null &&
                                history.getEmployeeName().toLowerCase().contains(keyword))
                                || (history.getIpAddress() != null &&
                                history.getIpAddress().toLowerCase().contains(keyword))
                                || history.getItemCode() != null &&
                                history.getItemCode().toLowerCase().contains(keyword))
                                ||      (history.getItemName() != null &&
                                history.getItemName().toLowerCase().contains(keyword))
                                || (history.getItemMake() != null &&
                                history.getItemMake().toLowerCase().contains(keyword))
                                || (history.getItemModel() != null &&
                                history.getItemModel().toLowerCase().contains(keyword))
                                ||      (history.getItemSerial() != null &&
                                history.getItemSerial().toLowerCase().contains(keyword))
                                || (history.getImeiNo() != null &&
                                history.getImeiNo().toLowerCase().contains(keyword))
                                || (history.getSimNo() != null &&
                                history.getSimNo().toLowerCase().contains(keyword))
                                || (history.getPoNo() != null &&
                                history.getPoNo().toLowerCase().contains(keyword))
                                || (history.getPartyName() != null &&
                                history.getPartyName().toLowerCase().contains(keyword))
                                || (history.getStatus() != null &&
                                history.getStatus().toLowerCase().contains(keyword));
            });
        });

        updateConnectionStatus();
        loadHistory();
        TableFilter<TransactionHistory> filter = TableFilter.forTableView(historyTable).apply();

        Platform.runLater(() -> {
            historyTable.getScene().getRoot().lookupAll(".filter-panel").forEach(panel -> {
                panel.lookupAll(".list-view").forEach(node -> {
                    if (node instanceof ListView<?> list) {

                        int size = list.getItems().size();

                        // shrink when few items, limit when many
                        int visibleRows = Math.min(size, 8);

                        list.setFixedCellSize(24);
                        list.setPrefHeight(visibleRows * 24 + 2);
                        list.setMaxHeight(8 * 24 + 2);
                    }
                });
            });
        });
        startConnectionMonitor();
    }


    @FXML
    private void handleRefresh() {
        loadHistory();
    }

    @FXML
    private void handleAddItem() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/add-item.fxml")
            );

            Scene scene = new Scene(loader.load(), 400, 250);

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

            Scene scene = new Scene(loader.load(), 700, 650);

            Stage stage = new Stage();
            stage.setTitle("Add Transaction");
            stage.setScene(scene);
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

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());

        if (file != null) {
            ExportUtil.exportToExcel(
                    historyTable.getItems(),
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

        File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());

        if (file != null) {
            ExportUtil.exportToPDF(
                    historyTable.getItems(),
                    file.getAbsolutePath()
            );
        }
    }

    @FXML
    private void handleBackupDatabase() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Backup Database");
        dialog.setHeaderText("Select location to save backup file");

        ButtonType createBtn =
                new ButtonType("Create Backup", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        // Make dialog wider
        dialog.getDialogPane().setPrefWidth(500);

        TextField pathField = new TextField();
        pathField.setPromptText("Select folder to generate backup file");
        pathField.setPrefWidth(350);

        Button browseBtn = new Button("Browse");
        browseBtn.setMinWidth(90);

        browseBtn.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Backup Folder");

            File folder = directoryChooser.showDialog(
                    historyTable.getScene().getWindow()
            );

            if (folder != null) {

                String date = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter
                                .ofPattern("ddMMyyyy_HHmmss"));

                String filePath = folder.getAbsolutePath()
                        + File.separator
                        + "backup_" + date + ".sql";

                pathField.setText(filePath);
            }
        });

        // Layout container
        HBox pathBox = new HBox(10);
        pathBox.getChildren().addAll(pathField, browseBtn);
        HBox.setHgrow(pathField, Priority.ALWAYS);

        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.getChildren().add(pathBox);

        dialog.getDialogPane().setContent(container);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {
                return pathField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(path -> {

            if (path == null || path.isBlank()) {
                AlertUtil.showError("Error", "Please select a folder.");
                return;
            }

            if (!path.endsWith(".sql")) {
                path += ".sql";
            }

            createBackup(path);
        });
    }

    @FXML
    private void handleRestoreDatabase() {

        if (!verifyRestorePassword()) {
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Restore Database");
        dialog.setHeaderText("Select backup file to restore");

        ButtonType restoreBtn =
                new ButtonType("Restore Backup", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(restoreBtn, ButtonType.CANCEL);

        dialog.getDialogPane().setPrefWidth(500);

        TextField pathField = new TextField();
        pathField.setPromptText("Select backup (.sql) file");
        pathField.setPrefWidth(350);

        Button browseBtn = new Button("Browse");
        browseBtn.setMinWidth(90);

        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Backup File");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("SQL Files", "*.sql")
            );

            File file = fc.showOpenDialog(historyTable.getScene().getWindow());

            if (file != null) {
                pathField.setText(file.getAbsolutePath());
            }
        });

        HBox pathBox = new HBox(10);
        pathBox.getChildren().addAll(pathField, browseBtn);
        HBox.setHgrow(pathField, Priority.ALWAYS);

        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.getChildren().add(pathBox);

        dialog.getDialogPane().setContent(container);

        // Disable restore button if no file selected
        Node restoreButton = dialog.getDialogPane().lookupButton(restoreBtn);
        restoreButton.setDisable(true);

        pathField.textProperty().addListener((obs, oldVal, newVal) -> {
            restoreButton.setDisable(newVal == null || newVal.isBlank());
        });

        dialog.setResultConverter(btn -> {
            if (btn == restoreBtn) {
                return pathField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(path -> {

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Warning");
            confirm.setHeaderText("All current data will be replaced!");
            confirm.setContentText(
                    "Restoring this backup will overwrite all existing data.\n\nDo you want to continue?"
            );

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    restoreBackup(path);
                }
            });
        });
    }

    @FXML
    private void handleFreezeColumns() {

        TextInputDialog dialog = new TextInputDialog("2");
        dialog.setTitle("Freeze Columns");
        dialog.setHeaderText("Freeze first N columns");

        dialog.showAndWait().ifPresent(input -> {

            try {

                int count = Integer.parseInt(input);

                SplitPane pane = freezeManager.freezeColumns(count);
                VBox.setVgrow(pane, Priority.ALWAYS);

                VBox centerBox = (VBox) rootPane.getCenter();

                int tableIndex = centerBox.getChildren().indexOf(historyTable);

                centerBox.getChildren().set(tableIndex, pane);

                columnsFrozen = true;
                exportExcelButton.setDisable(columnsFrozen);
                exportPDFButton.setDisable(columnsFrozen);
                freezeColumnsMenuItem.setDisable(true);
                unfreezeColumnsMenuItem.setDisable(false);

            } catch (Exception e) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid number of columns.");
                alert.show();
            }

        });
    }

    @FXML
    private void handleUnfreezeColumns() {

        freezeManager.restoreOriginalTable();

        VBox centerBox = (VBox) rootPane.getCenter();

        int paneIndex = centerBox.getChildren().size() - 1;

        centerBox.getChildren().set(paneIndex, historyTable);

        columnsFrozen = false;
        exportExcelButton.setDisable(columnsFrozen);
        exportPDFButton.setDisable(columnsFrozen);
        freezeColumnsMenuItem.setDisable(false);
        unfreezeColumnsMenuItem.setDisable(true);

    }

    private void loadHistory() {

        // 🔒 If no database selected, show empty table
        if (!DBConnection.isDatabaseSet()) {
            historyTable.setItems(FXCollections.observableArrayList());
            return;
        }

        masterData = FXCollections.observableArrayList(
                transactionDAO.getAllTransactions()
        );

        filteredData = new FilteredList<>(masterData, p -> true);

        SortedList<TransactionHistory> sortedData =
                new SortedList<>(filteredData);

        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());

        historyTable.setItems(sortedData);
    }

    private void openItemHistoryPage(String itemId, String itemName) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/item-history.fxml")
            );

            Parent root = loader.load();

            ItemHistoryController controller = loader.getController();
            controller.loadItemHistory(itemId, itemName);

            Stage stage = new Stage();
            stage.setTitle("Item History");

            Scene scene = new Scene(root, 1200, 650); // fixed window size

            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleSetupDatabase() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Setup Database");
        dialog.setHeaderText("Enter Server Details");

        ButtonType connectBtn = new ButtonType("Use Database", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectBtn, ButtonType.CANCEL);

        // 🔹 Form Fields
        TextField hostField = new TextField("localhost");
        TextField portField = new TextField("3306");
        TextField dbNameField = new TextField();
        TextField userField = new TextField("root");
        PasswordField passField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Host:"), 0, 0);
        grid.add(hostField, 1, 0);

        grid.add(new Label("Port:"), 0, 1);
        grid.add(portField, 1, 1);

        grid.add(new Label("Database Name:"), 0, 2);
        grid.add(dbNameField, 1, 2);

        grid.add(new Label("Username:"), 0, 3);
        grid.add(userField, 1, 3);

        grid.add(new Label("Password:"), 0, 4);
        grid.add(passField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {

            if (response == ButtonType.CANCEL) return;

            String host = hostField.getText();
            String port = portField.getText();
            String dbName = dbNameField.getText();
            String user = userField.getText();
            String pass = passField.getText();

            try {

                DBConnection.setDatabaseConfig(host, port, dbName, user, pass);

                DBConnection.initializeDatabase();

                // 🔥 SAVE CONFIG
                AppConfig.saveDatabaseConfig(host, port, dbName, user, pass);

                loadHistory();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("Database connected successfully.");
                success.showAndWait();

            } catch (Exception e) {

                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Connection Failed");
                error.setHeaderText("Could not connect to MySQL");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        });
    }

    public void handleExit() {
        System.exit(0);
    }

    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Inventory Management System");
        alert.setContentText("Developed by Yashank Tiwari\nVersion 1.0");
        alert.showAndWait();
    }

    // For live database connection status
    private void updateConnectionStatus() {

        boolean connected = false;

        try (var conn = DBConnection.getConnection()) {
            connected = conn != null && conn.isValid(2);
        } catch (Exception ignored) {
            connected = false;
        }

        if (connected) {
            statusDot.setStyle("-fx-fill: #2ecc71;"); // green
            statusLabel.setText("Database connected");
        } else {
            statusDot.setStyle("-fx-fill: #e74c3c;"); // red
            statusLabel.setText("Not Connected");
        }

        boolean disabled = !connected;
        historyTable.setDisable(disabled);
        addTransactionButton.setDisable(disabled);
        exportExcelButton.setDisable(disabled || columnsFrozen);
        exportPDFButton.setDisable(disabled || columnsFrozen);
        searchField.setDisable(disabled);
        refreshButton.setDisable(false);
    }

    private void startConnectionMonitor() {

        javafx.animation.Timeline timeline =
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(5),
                                event -> updateConnectionStatus()
                        )
                );

        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void createBackup(String filePath) {

        new Thread(() -> {

            try {

                Platform.runLater(() -> historyTable.setDisable(true));

                if (mysqldumpPath == null) {
                    Platform.runLater(() -> {
                        AlertUtil.showError("Error", "mysqldump path not configured.");
                        historyTable.setDisable(false);
                    });
                    return;
                }

                String host = DBConnection.getHost();
                String port = DBConnection.getPort();
                String db = DBConnection.getDatabaseName();
                String user = DBConnection.getUsername();
                String pass = DBConnection.getPassword();

                File backupFile = new File(filePath);

                ProcessBuilder pb = new ProcessBuilder(
                        mysqldumpPath,
                        "-h", host,
                        "-P", port,
                        "-u", user,
                        "-p" + pass,
                        db
                );

                // 🔥 DO NOT merge error stream (prevents corrupt backup)
                pb.redirectOutput(backupFile);

                Process process = pb.start();

                // 🔥 Read error stream separately
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream())
                );

                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }

                int exitCode = process.waitFor();

                Platform.runLater(() -> {
                    historyTable.setDisable(false);

                    if (exitCode == 0) {
                        AlertUtil.showInfo(
                                "Success",
                                "Backup created:\n" + backupFile.getAbsolutePath()
                        );
                    } else {
                        AlertUtil.showError(
                                "Backup Failed",
                                errorOutput.toString()
                        );
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                Platform.runLater(() -> {
                    historyTable.setDisable(false);
                    AlertUtil.showError("Error", "Backup failed.");
                });
            }

        }).start();
    }

    private void restoreBackup(String filePath) {

        new Thread(() -> {

            try {

                Platform.runLater(() -> historyTable.setDisable(true));

                if (mysqlPath == null) {
                    Platform.runLater(() -> {
                        AlertUtil.showError("Error", "mysql path not configured.");
                        historyTable.setDisable(false);
                    });
                    return;
                }

                String host = DBConnection.getHost();
                String port = DBConnection.getPort();
                String db = DBConnection.getDatabaseName();
                String user = DBConnection.getUsername();
                String pass = DBConnection.getPassword();

                // 🔴 STEP 1 — Drop & recreate database
                ProcessBuilder dropDb = new ProcessBuilder(
                        mysqlPath,
                        "-h", host,
                        "-P", port,
                        "-u", user,
                        "-p" + pass,
                        "-e",
                        "DROP DATABASE IF EXISTS " + db + "; CREATE DATABASE " + db + ";"
                );

                Process dropProcess = dropDb.start();

                BufferedReader dropReader = new BufferedReader(
                        new InputStreamReader(dropProcess.getErrorStream())
                );

                StringBuilder dropErrors = new StringBuilder();
                String line;

                while ((line = dropReader.readLine()) != null) {
                    dropErrors.append(line).append("\n");
                }

                int dropExit = dropProcess.waitFor();

                if (dropExit != 0) {
                    Platform.runLater(() -> {
                        historyTable.setDisable(false);
                        AlertUtil.showError("Drop Failed", dropErrors.toString());
                    });
                    return;
                }

                // 🔴 STEP 2 — Restore SQL file
                ProcessBuilder restorePb = new ProcessBuilder(
                        mysqlPath,
                        "-h", host,
                        "-P", port,
                        "-u", user,
                        "-p" + pass,
                        db
                );

                restorePb.redirectInput(new File(filePath));

                Process restoreProcess = restorePb.start();

                BufferedReader restoreReader = new BufferedReader(
                        new InputStreamReader(restoreProcess.getErrorStream())
                );

                StringBuilder restoreErrors = new StringBuilder();

                while ((line = restoreReader.readLine()) != null) {
                    restoreErrors.append(line).append("\n");
                }

                int restoreExit = restoreProcess.waitFor();

                Platform.runLater(() -> {

                    historyTable.setDisable(false);

                    if (restoreExit == 0) {

                        loadHistory();   // 🔥 Auto refresh

                        AlertUtil.showInfo(
                                "Success",
                                "Database restored successfully."
                        );

                    } else {

                        AlertUtil.showError(
                                "Restore Failed",
                                restoreErrors.toString()
                        );
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                Platform.runLater(() -> {
                    historyTable.setDisable(false);
                    AlertUtil.showError("Error", "Restore failed.");
                });
            }

        }).start();
    }

    private boolean verifyRestorePassword() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Authorization Required");
        dialog.setHeaderText("Enter Restore Password");

        ButtonType loginBtn = new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        VBox container = new VBox(10, passwordField);
        container.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(container);

        dialog.setResultConverter(btn -> {
            if (btn == loginBtn) {
                return passwordField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return false;
        }

        if (!RESTORE_PASSWORD.equals(result.get())) {

            AlertUtil.showError(
                    "Access Denied",
                    "Incorrect restore password."
            );

            return false;
        }

        return true;
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

    private void restoreColumnOrder() {

        String order = prefs.get(COLUMN_ORDER_KEY, null);

        if (order == null) return;

        String[] ids = order.split(",");

        ObservableList<TableColumn<TransactionHistory, ?>> columns =
                historyTable.getColumns();

        for (String id : ids) {

            columns.stream()
                    .filter(c -> id.equals(c.getId()))
                    .findFirst()
                    .ifPresent(col -> {

                        columns.remove(col);
                        columns.add(col);

                    });
        }
    }

    public void saveColumnOrder() {

        if (columnsFrozen) {
            freezeManager.restoreOriginalTable();
        }

        StringBuilder order = new StringBuilder();

        for (TableColumn<?, ?> column : historyTable.getColumns()) {

            if (column.getId() != null) {
                order.append(column.getId()).append(",");
            }
        }

        prefs.put(COLUMN_ORDER_KEY, order.toString());
    }
}