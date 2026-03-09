package com.inventory.ui.controller;

import com.inventory.dao.ItemDAO;
import com.inventory.dao.PersonDAO;
import com.inventory.dao.TransactionDAO;
import com.inventory.database.AppConfig;
import com.inventory.model.Item;
import com.inventory.model.Person;
import com.inventory.util.AlertUtil;
import com.inventory.util.StoragePathDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class AddTransactionController {

    @FXML private TextField itemNameField;
    @FXML private TextField itemIdField;
    @FXML private TextField employeeIdField;
    @FXML private TextField personNameField;
    @FXML private TextField departmentField;
    @FXML private TextArea remarksField;
    @FXML private ComboBox<String> buySellBox;
    @FXML private TextField plantField;
    @FXML private TextField locationField;

    @FXML private TextField employeeCodeField;
    @FXML private TextField employeeNameField;

    @FXML private TextField ipField;

    @FXML private TextField itemCodeField;
    @FXML private TextField itemMakeField;
    @FXML private TextField itemModelField;
    @FXML private TextField itemSerialField;

    @FXML private TextField imeiField;
    @FXML private TextField simField;

    @FXML private TextField poField;
    @FXML private TextField partyField;

    @FXML private ComboBox<String> statusBox;
    @FXML private TextField itemCountField;
    @FXML private ComboBox<String> unitComboBox;

    @FXML private TextField attachmentField;

    @FXML private DatePicker transactionDatePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private ComboBox<String> ampmBox;

    private File selectedAttachment;
    private ObservableList<String> masterItemList;
    private Popup suggestionsPopup;
    private ListView<String> suggestionsListView;

    private final ItemDAO itemDAO = new ItemDAO();
    private final PersonDAO personDAO = new PersonDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    public void initialize() {

        transactionDatePicker.setValue(java.time.LocalDate.now());

        // Disable future dates
        transactionDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isAfter(java.time.LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        // Hour (1–12)
        hourSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12,
                        java.time.LocalTime.now().getHour() % 12 == 0 ? 12 :
                                java.time.LocalTime.now().getHour() % 12)
        );

        // Minute (0–59)
        minuteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59,
                        java.time.LocalTime.now().getMinute())
        );

        // AM PM
        ampmBox.getItems().addAll("AM", "PM");
        ampmBox.setValue(java.time.LocalTime.now().getHour() >= 12 ? "PM" : "AM");

        masterItemList = FXCollections.observableArrayList(
                itemDAO.getAllItems()
                        .stream()
                        .map(Item::getItemName)
                        .distinct()
                        .toList()
        );
        buySellBox.setItems(FXCollections.observableArrayList(
                "Buy",
                "Sell"
        ));

        buySellBox.valueProperty().addListener((obs, oldVal, newVal) -> {

            if ("Buy".equalsIgnoreCase(newVal)) {
                statusBox.setItems(FXCollections.observableArrayList("In Stock"));
                statusBox.setValue("In Stock");
                statusBox.setDisable(true);

            } else {
                statusBox.setItems(FXCollections.observableArrayList("Issued", "Scrap"));
                statusBox.setValue("Issued");
                statusBox.setDisable(false);
            }
        });

        unitComboBox.getItems().addAll(
                "Piece",
                "Meter",
                "Box",
                "Kg"
        );
        unitComboBox.setValue("Piece");
        setupAutoComplete();
    }

    // ================= SAVE =================

    @FXML
    private void handleSave() {

        String buySell = buySellBox.getValue();
        String plant = plantField.getText();
        String department = departmentField.getText();
        String location = locationField.getText();

        String employeeCode = employeeCodeField.getText();
        String employeeName = employeeNameField.getText();

        String ip = ipField.getText();

        String itemCode = itemCodeField.getText();
        String itemName = itemNameField.getText();
        String itemMake = itemMakeField.getText();
        String itemModel = itemModelField.getText();
        String itemSerial = itemSerialField.getText();

        String imei = imeiField.getText();
        String sim = simField.getText();

        String po = poField.getText();
        String party = partyField.getText();

        String status = statusBox.getValue();
        String remarks = remarksField.getText();

        String itemCountText = itemCountField.getText();
        java.math.BigDecimal itemCount;

        try {
            itemCount = new java.math.BigDecimal(itemCountText.trim());
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Invalid item count value.");
            return;
        }

        if (itemCount.compareTo(java.math.BigDecimal.ZERO) < 0) {
            AlertUtil.showError("Validation Error", "Item count cannot be negative.");
            return;
        }

        if (itemCount.compareTo(new java.math.BigDecimal("99999999.99")) > 0) {
            AlertUtil.showError("Validation Error",
                    "Item count exceeds maximum allowed value (99,999,999.99).");
            return;
        }

        if (itemCount.scale() > 2) {
            AlertUtil.showError("Validation Error",
                    "Item count can have maximum 2 decimal places.");
            return;
        }

        LocalDate date = transactionDatePicker.getValue();

        int hour12 = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        String ampm = ampmBox.getValue();

        int hour24 = hour12;

        if ("PM".equals(ampm) && hour12 != 12) {
            hour24 += 12;
        }

        if ("AM".equals(ampm) && hour12 == 12) {
            hour24 = 0;
        }

        LocalDateTime transactionTime =
                LocalDateTime.of(date, LocalTime.of(hour24, minute));

        if (transactionTime.isAfter(LocalDateTime.now())) {
            AlertUtil.showError(
                    "Invalid Date",
                    "Transaction time cannot be in the future."
            );
            return;
        }

        String unit = unitComboBox.getValue();

        // Check whether the units for items match
        String existingUnit = transactionDAO.getUnitForItem(itemName);
        if (existingUnit != null && !existingUnit.equalsIgnoreCase(unit)) {
            AlertUtil.showError(
                    "Unit Mismatch",
                    "This item already uses unit: " + existingUnit +
                            "\nYou cannot change it to: " + unit
            );
            return;
        }

        if ("Sell".equalsIgnoreCase(buySell)) {

            if (itemName == null || itemName.isBlank()) {
                AlertUtil.showError(
                        "Validation Error",
                        "Item Name is required for Sell transactions."
                );
                return;
            }

            if (itemSerial == null || itemSerial.isBlank()) {
                AlertUtil.showError(
                        "Validation Error",
                        "Item Serial is required for Sell transactions."
                );
                return;
            }

            boolean exists = transactionDAO.itemExistsInInventory(
                    itemCode,
                    itemName,
                    itemMake,
                    itemModel,
                    itemSerial
            );
            if (!exists) {
                AlertUtil.showError(
                        "Invalid Transaction",
                        "This item does not exist in inventory.\n" +
                                "Please check Item Code / Name / Make / Model / Serial Number."
                );
                return;
            }
            double currentStock = transactionDAO.getCurrentStock(itemName);
            if (itemCount.doubleValue() > currentStock) {
                AlertUtil.showError(
                        "Stock Error",
                        "Not enough inventory.\nAvailable stock: " + currentStock
                );
                return;
            }
        }

        int transactionId = transactionDAO.createTransaction(
                buySell,
                plant,
                department,
                location,
                employeeCode,
                employeeName,
                ip,
                itemCode,
                itemName,
                itemMake,
                itemModel,
                itemSerial,
                imei,
                sim,
                po,
                party,
                status,
                remarks,
                itemCountText,
                unit,
                transactionTime
        );
        saveAttachment(transactionId);
        closeWindow();
    }

    // ================= CANCEL ================
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // ================= BROWSE ATTACHMENT ============
    @FXML
    private void handleBrowseAttachment() {
        String path = AppConfig.getAttachmentPath();
        if (path == null || path.isBlank()) {
            StoragePathDialog.show(
                    (Stage) attachmentField.getScene().getWindow()
            );
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Attachment");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = chooser.showOpenDialog(
                attachmentField.getScene().getWindow()
        );
        if (file == null) return;
        if (file.length() > 204800) {
            AlertUtil.showError(
                    "File Too Large",
                    "Maximum allowed size is 200KB"
            );
            return;
        }
        selectedAttachment = file;
        attachmentField.setText(file.getName());
    }

    // ================ CLEAR ATTACHMENT ==============
    @FXML
    private void handleClearAttachment() {

        selectedAttachment = null;
        attachmentField.clear();
    }

    // ================= AUTOCOMPLETE =================
    private void setupAutoComplete() {

        suggestionsPopup = new Popup();
        suggestionsPopup.setAutoHide(true);

        suggestionsListView = new ListView<>();
        suggestionsListView.setFocusTraversable(false);

        suggestionsPopup.getContent().add(suggestionsListView);

        // 🔹 Filter while typing
        itemNameField.textProperty().addListener((obs, oldText, newText) -> {

            if (newText == null || newText.isBlank()) {
                suggestionsPopup.hide();
                return;
            }

            List<String> filtered = masterItemList.stream()
                    .filter(item ->
                            item.toLowerCase().contains(newText.toLowerCase()))
                    .toList();

            if (filtered.isEmpty()) {
                suggestionsPopup.hide();
                return;
            }

            suggestionsListView.getItems().setAll(filtered);

            // 🔹 Always select first item
//            suggestionsListView.getSelectionModel().selectFirst();
            suggestionsListView.scrollTo(0);

            // 🔹 Dynamic height (max 5 rows)
            int visibleRows = Math.min(filtered.size(), 5);
            suggestionsListView.setPrefHeight(visibleRows * 28);

            // 🔹 Match width to textfield
            suggestionsListView.setPrefWidth(itemNameField.getWidth());

            if (!suggestionsPopup.isShowing()) {
                suggestionsPopup.show(
                        itemNameField,
                        itemNameField.localToScreen(0,
                                itemNameField.getHeight()).getX(),
                        itemNameField.localToScreen(0,
                                itemNameField.getHeight()).getY()
                );
            }
        });

        // 🔹 Mouse selection
        suggestionsListView.setOnMouseClicked(event -> {
            String selected = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                applySelection(selected);
            }
        });

        // 🔹 Keyboard navigation from TextField
        itemNameField.setOnKeyPressed(event -> {

            switch (event.getCode()) {

                case DOWN -> {
                    if (suggestionsPopup.isShowing()
                            && !suggestionsListView.getItems().isEmpty()) {

                        suggestionsListView.requestFocus();

                        if (suggestionsListView.getSelectionModel().isEmpty()) {
                            suggestionsListView.getSelectionModel().selectFirst();
                        }
                    }
                }

                case ESCAPE -> suggestionsPopup.hide();
            }
        });

        // 🔹 Keyboard inside ListView
        suggestionsListView.setOnKeyPressed(event -> {

            switch (event.getCode()) {

                case ENTER -> {
                    String selected = suggestionsListView
                            .getSelectionModel()
                            .getSelectedItem();

                    if (selected != null) {
                        applySelection(selected);
                    }
                }

                case ESCAPE -> {
                    suggestionsPopup.hide();
                    itemNameField.requestFocus();
                }
            }
        });
    }

    private void applySelection(String selected) {
        itemNameField.setText(selected);
        itemNameField.positionCaret(selected.length());
        suggestionsPopup.hide();
        itemNameField.requestFocus();
    }

    private void saveAttachment(int transactionId) {

        if (selectedAttachment == null) return;

        try {

            String storagePath = AppConfig.getAttachmentPath();

            String extension =
                    selectedAttachment.getName()
                            .substring(selectedAttachment.getName().lastIndexOf("."));

            String timestamp =
                    java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String newName =
                    transactionId + "_" + timestamp + extension;

            File target = new File(
                    storagePath + File.separator +
                            "transactions" + File.separator + newName
            );

            java.nio.file.Files.copy(
                    selectedAttachment.toPath(),
                    target.toPath()
            );

            transactionDAO.updateAttachment(transactionId, newName);

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.showError(
                    "Attachment Error",
                    "Transaction saved but attachment failed."
            );
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        stage.close();
    }
}