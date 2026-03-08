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

    private File selectedAttachment;
    private ObservableList<String> masterItemList;
    private Popup suggestionsPopup;
    private ListView<String> suggestionsListView;

    private final ItemDAO itemDAO = new ItemDAO();
    private final PersonDAO personDAO = new PersonDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    public void initialize() {


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
        String unit = unitComboBox.getValue();



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
                unit
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