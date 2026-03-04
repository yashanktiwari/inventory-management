package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.database.AppConfig;
import com.inventory.database.DBConnection;
import com.inventory.model.TransactionHistory;
import com.inventory.util.AlertUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import com.inventory.util.ExportUtil;

public class DashboardController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<TransactionHistory> historyTable;

    @FXML
    private TableColumn<TransactionHistory, Integer> serialColumn;

    @FXML
    private TableColumn<TransactionHistory, String> itemIdColumn;

    @FXML
    private TableColumn<TransactionHistory, String> itemColumn;

    @FXML
    private TableColumn<TransactionHistory, String> employeeIdColumn;

    @FXML
    private TableColumn<TransactionHistory, String> personColumn;

    @FXML
    private TableColumn<TransactionHistory, String> issuedColumn;

    @FXML
    private TableColumn<TransactionHistory, String> returnedColumn;

    @FXML
    private TableColumn<TransactionHistory, String> remarksColumn;

    @FXML
    private TableColumn<TransactionHistory, Void> actionColumn;

    @FXML
    private TableColumn<TransactionHistory, Void> deleteColumn;

    @FXML
    private javafx.scene.shape.Circle statusDot;

    @FXML
    private Label statusLabel;

    @FXML
    private Button addTransactionButton;

    @FXML
    private Button exportExcelButton;

    @FXML
    private Button exportPDFButton;

    @FXML
    private Button refreshButton;


    private ObservableList<TransactionHistory> masterData;
    private FilteredList<TransactionHistory> filteredData;
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private String mysqldumpPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";
    private String mysqlPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";

    @FXML
    public void initialize() {
        actionColumn.setCellFactory(col -> new javafx.scene.control.TableCell<>() {

            private final javafx.scene.control.Button returnBtn =
                    new javafx.scene.control.Button("Return");

            {
                returnBtn.setOnAction(event -> {

                    TransactionHistory history =
                            getTableView().getItems().get(getIndex());

                    if (history.getReturnedDateTime() == null) {
                        transactionDAO.returnItemByTransactionId(
                                history.getTransactionId()
                        );
                        loadHistory();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {

                    TransactionHistory history =
                            getTableView().getItems().get(getIndex());

                    if (history.getReturnedDateTime() != null) {
                        returnBtn.setDisable(true);
                        returnBtn.setText("Returned");
                    } else {
                        returnBtn.setDisable(false);
                        returnBtn.setText("Return");
                    }

                    setGraphic(returnBtn);
                }
            }
        });

        deleteColumn.setCellFactory(param -> new TableCell<>() {

            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle(
                        "-fx-background-color: #ff4d4d; " +
                                "-fx-text-fill: white; " +
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

        employeeIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("employeeId")
        );
        serialColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        historyTable.getItems().indexOf(cellData.getValue()) + 1
                ).asObject()
        );
        itemIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("itemId")
        );

        itemIdColumn.setCellFactory(col ->
                new TableCell<TransactionHistory, String>() {

                    private final Hyperlink link = new Hyperlink();
                    {
                        link.setBorder(null);
                        link.setPadding(javafx.geometry.Insets.EMPTY);
                        link.setUnderline(false);

                        link.setOnAction(event -> {
                            TransactionHistory history =
                                    getTableView().getItems().get(getIndex());

                            openItemHistoryPage(
                                    history.getItemId(),
                                    history.getItemName()
                            );
                        });
                    }

                    @Override
                    protected void updateItem(String itemId, boolean empty) {
                        super.updateItem(itemId, empty);

                        if (empty || itemId == null) {
                            setGraphic(null);
                        } else {
                            link.setText(itemId);

                            getTableRow().selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                                if (isNowSelected) {
                                    link.setStyle("-fx-text-fill: white;");
                                } else {
                                    link.setStyle("-fx-text-fill: #1a73e8;");
                                }
                            });

                            setGraphic(link);
                        }
                    }
                });

        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        personColumn.setCellValueFactory(new PropertyValueFactory<>("personName"));
        issuedColumn.setCellValueFactory(cellData -> {

            String raw = cellData.getValue().getIssuedDateTime();
            if (raw == null) return new SimpleStringProperty("");

            LocalDateTime dateTime =
                    java.sql.Timestamp.valueOf(raw).toLocalDateTime();

            return new SimpleStringProperty(
                    dateTime.format(formatter)
            );
        });
        returnedColumn.setCellValueFactory(cellData -> {

            String raw = cellData.getValue().getReturnedDateTime();

            if (raw == null) {
                return new SimpleStringProperty("Not Returned");
            }

            LocalDateTime dateTime =
                    java.sql.Timestamp.valueOf(raw).toLocalDateTime();

            return new SimpleStringProperty(
                    dateTime.format(formatter)
            );
        });
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {

            if (filteredData == null) return;

            filteredData.setPredicate(history -> {

                if (newValue == null || newValue.isBlank()) {
                    return true;
                }

                String keyword = newValue.toLowerCase();

                return (history.getItemId() != null &&
                        history.getItemId().toLowerCase().contains(keyword))
                        || (history.getItemName() != null &&
                        history.getItemName().toLowerCase().contains(keyword))
                        || (history.getEmployeeId() != null &&
                        history.getEmployeeId().toLowerCase().contains(keyword))
                        || (history.getPersonName() != null &&
                        history.getPersonName().toLowerCase().contains(keyword));
            });
        });
        updateConnectionStatus();
        loadHistory();
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

            Scene scene = new Scene(loader.load(), 400, 500);

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
            stage.setScene(new Scene(root));
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
        exportExcelButton.setDisable(disabled);
        exportPDFButton.setDisable(disabled);
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
}