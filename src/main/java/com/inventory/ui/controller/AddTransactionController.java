package com.inventory.ui.controller;

import com.inventory.dao.ItemDAO;
import com.inventory.dao.PersonDAO;
import com.inventory.dao.TransactionDAO;
import com.inventory.database.AppConfig;
import com.inventory.model.Item;
import com.inventory.model.TransactionHistory;
import com.inventory.util.AlertUtil;
import com.inventory.util.StoragePathDialog;
import com.inventory.util.TransactionPrefill;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AddTransactionController {

    @FXML private VBox sellFieldsContainer;

    @FXML private TextField itemNameField;
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

    @FXML private Button saveAndAddAnotherButton;

    private File selectedAttachment;
    private ObservableList<String> masterItemList;
    private Popup suggestionsPopup;
    private ListView<String> suggestionsListView;

    private final ItemDAO itemDAO = new ItemDAO();
    private final PersonDAO personDAO = new PersonDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private Runnable onTransactionSaved;

    private Integer editTransactionId = null;
    private String existingAttachmentFile;

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
                hideBuyFields(true);
            } else {
                hideBuyFields(false);
            }

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

        Integer transactionId = saveTransaction();

        if (transactionId != null) {
            closeWindow();
        }
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

        boolean confirm = showDeleteAttachmentDialog();

        if (!confirm) return;

        selectedAttachment = null;
        existingAttachmentFile = null;
        attachmentField.clear();
    }

    // ================ SAVE AND ADD ANOTHER ===========
    @FXML
    private void handleSaveAndAddAnother() {
        Integer transactionId = saveTransaction();
        if (transactionId == null) {
            return;
        }

        if (onTransactionSaved != null) {
            onTransactionSaved.run();
        }

        // Clear only fields that must change
        itemSerialField.clear();
        imeiField.clear();
        simField.clear();
        remarksField.clear();

        // reset attachment
        selectedAttachment = null;
        attachmentField.clear();

        // focus serial for next entry
        itemSerialField.requestFocus();
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

    private Integer saveTransaction() {

        if (transactionDAO.isDemoLimitReached()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Demo Limit Reached");
            alert.setHeaderText(null);
            alert.setContentText(
                    "This demo version allows only 75 transactions.\n" +
                            "Please contact the administrator for the full version."
            );
            alert.showAndWait();
            return null;
        }

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

        if (buySell == null) {
            AlertUtil.showError("Validation Error", "Please mention BUY OR SELL");
            return null;
        }

        try {
            itemCount = new java.math.BigDecimal(itemCountText.trim());
        } catch (Exception e) {
            AlertUtil.showError("Validation Error", "Invalid item count value.");
            return null;
        }

        if (itemCount.compareTo(java.math.BigDecimal.ZERO) < 0) {
            AlertUtil.showError("Validation Error", "Item count cannot be negative.");
            return null;
        }

        LocalDate date = transactionDatePicker.getValue();

        int hour12 = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        String ampm = ampmBox.getValue();

        int hour24 = hour12;

        if ("PM".equals(ampm) && hour12 != 12) hour24 += 12;
        if ("AM".equals(ampm) && hour12 == 12) hour24 = 0;

        LocalDateTime transactionTime =
                LocalDateTime.of(date, LocalTime.of(hour24, minute));

        String unit = unitComboBox.getValue();

        int transactionId;

        if (editTransactionId != null) {

            String attachmentToSave =
                    selectedAttachment != null
                            ? selectedAttachment.getName()
                            : existingAttachmentFile;
            transactionDAO.updateTransaction(
                    editTransactionId,
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
                    attachmentToSave
            );

            transactionId = editTransactionId;

        } else {

            transactionId = transactionDAO.createTransaction(
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
        }

        saveAttachment(transactionId);

        return transactionId;
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

    public void prefill(TransactionPrefill data) {

        if (data.buySell != null) {
            buySellBox.setValue(data.buySell);
        }

        plantField.setText(safe(data.plant));
        departmentField.setText(safe(data.department));
        locationField.setText(safe(data.location));

        employeeCodeField.setText(safe(data.employeeCode));
        employeeNameField.setText(safe(data.employeeName));

        ipField.setText(safe(data.ipAddress));

        itemCodeField.setText(safe(data.itemCode));
        itemNameField.setText(safe(data.itemName));
        itemMakeField.setText(safe(data.itemMake));
        itemModelField.setText(safe(data.itemModel));
        itemCountField.setText(data.itemCount == null ? "" : data.itemCount + "");

        itemSerialField.setText(safe(data.itemSerial));

        imeiField.setText(safe(data.imeiNo));
        simField.setText(safe(data.simNo));

        poField.setText(safe(data.poNo));
        partyField.setText(safe(data.partyName));

        unitComboBox.setValue(safe(data.unit));

        if (data.attachmentFile != null && !data.attachmentFile.isBlank()) {
            attachmentField.setText(data.attachmentFile);
            existingAttachmentFile = data.attachmentFile;
        }

    }

    public void prefillSell(TransactionPrefill data) {

        prefill(data);   // reuse the normal prefill

        buySellBox.setValue("Sell");
        buySellBox.setDisable(true);

        itemCountField.setDisable(true);
        itemCodeField.setDisable(true);
        itemNameField.setDisable(true);
        itemMakeField.setDisable(true);
        itemModelField.setDisable(true);
        itemSerialField.setDisable(true);
        unitComboBox.setDisable(true);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void closeWindow() {
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        stage.close();
    }

    public void setEditTransactionId(int id) {
        this.editTransactionId = id;

        if (saveAndAddAnotherButton != null) {
            buySellBox.setDisable(true);
            saveAndAddAnotherButton.setVisible(false);
            saveAndAddAnotherButton.setManaged(false);
        }
    }

    public void hideFieldsIfBuyTransaction(TransactionHistory t) {

        if ("Buy".equalsIgnoreCase(t.getBuySell())) {
            hideBuyFields(true);
        }

    }

    private void hideBuyFields(boolean hide) {
        // This one call collapses the entire "Sell" section
        sellFieldsContainer.setVisible(!hide);
        sellFieldsContainer.setManaged(!hide);

        // Forces the window to shrink/grow to fit the new content
        Platform.runLater(() -> {
            Stage stage = (Stage) buySellBox.getScene().getWindow();
            if (stage != null) stage.sizeToScene();
        });
    }

    public void setOnTransactionSaved(Runnable callback) {
        this.onTransactionSaved = callback;
    }

    private boolean showDeleteAttachmentDialog() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Delete Attachment");
        alert.setHeaderText("Remove existing attachment?");
        alert.setContentText("This will permanently delete the attachment from the system.");

        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType deleteBtn = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);

        alert.getButtonTypes().setAll(cancelBtn, deleteBtn);

        // Style delete button red
        Platform.runLater(() -> {
            Button deleteButton = (Button) alert.getDialogPane().lookupButton(deleteBtn);
            deleteButton.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white;");
        });

        return alert.showAndWait().orElse(cancelBtn) == deleteBtn;
    }
}