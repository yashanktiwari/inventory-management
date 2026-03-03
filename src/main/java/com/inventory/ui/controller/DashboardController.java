package com.inventory.ui.controller;

import com.inventory.dao.TransactionDAO;
import com.inventory.model.TransactionHistory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import com.inventory.util.ExportUtil;
import javafx.stage.FileChooser;

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


    private ObservableList<TransactionHistory> masterData;
    private FilteredList<TransactionHistory> filteredData;
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

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
            LocalDateTime dateTime = LocalDateTime.parse(raw);
            return new javafx.beans.property.SimpleStringProperty(
                    dateTime.format(formatter)
            );
        });
        returnedColumn.setCellValueFactory(cellData -> {

            String raw = cellData.getValue().getReturnedDateTime();

            if (raw == null) {
                return new javafx.beans.property.SimpleStringProperty("Not Returned");
            }

            LocalDateTime dateTime = LocalDateTime.parse(raw);

            return new javafx.beans.property.SimpleStringProperty(
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

        loadHistory();
    }

    private void loadHistory() {

        masterData = FXCollections.observableArrayList(
                transactionDAO.getAllTransactions()
        );

        filteredData = new FilteredList<>(masterData, p -> true);

        SortedList<TransactionHistory> sortedData =
                new SortedList<>(filteredData);

        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());

        historyTable.setItems(sortedData);
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
}