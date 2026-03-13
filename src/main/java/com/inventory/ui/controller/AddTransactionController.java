package com.inventory.ui.controller;

import com.inventory.cache.MasterCache;
import com.inventory.dao.ItemDAO;
import com.inventory.dao.PersonDAO;
import com.inventory.dao.TransactionDAO;
import com.inventory.database.AppConfig;
import com.inventory.model.Item;
import com.inventory.model.TransactionHistory;
import com.inventory.model.master.EmployeeMaster;
import com.inventory.model.master.ItemMaster;
import com.inventory.util.AlertUtil;
import com.inventory.util.StoragePathDialog;
import com.inventory.util.TransactionPrefill;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class AddTransactionController {

    @FXML private VBox sellFieldsContainer;

    @FXML private TextField itemNameField;
    @FXML private TextField departmentField;
    @FXML private TextArea remarksField;
    @FXML private ComboBox<String> buySellBox;
    @FXML private ComboBox<String> conditionBox;
    @FXML private TextField plantField;
    @FXML private TextField locationField;

    @FXML private TextField employeeCodeField;
    @FXML private TextField employeeNameField;

    @FXML private TextField ipField;

    @FXML private TextField itemCodeField;
    @FXML private TextField itemMakeField;
    @FXML private TextField itemModelField;
    @FXML private TextField itemSerialField;
    @FXML private TextField itemLocationField;
    @FXML private TextField itemCategoryField;


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
    @FXML private Label titleLabel;

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
    private Popup categoryPopup;
    private ListView<String> categoryListView;


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

        conditionBox.setItems(FXCollections.observableArrayList(
                "New",
                "Old and Used"
        ));

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
                statusBox.setItems(FXCollections.observableArrayList("IN STOCK"));
                statusBox.setValue("IN STOCK");
                statusBox.setDisable(true);

            } else {
                statusBox.setItems(FXCollections.observableArrayList("ISSUED", "SCRAPPED"));
                statusBox.setValue("Issued");
                statusBox.setDisable(false);
            }
        });

        unitComboBox.getItems().addAll(
                "PIECE",
                "METER",
                "BOX",
                "KG"
        );
        unitComboBox.setValue("PIECE");
        setupEmployeeAutocomplete();
        setupItemAutocomplete();
        setupCategoryAutocomplete();
        setupPlantAutocomplete();
        setupDepartmentAutocomplete();
        setupAutoComplete();

        // Force uppercase input
        enforceUpperCase(employeeCodeField);
        enforceUpperCase(employeeNameField);

        enforceUpperCase(itemCodeField);
        enforceUpperCase(itemNameField);
        enforceUpperCase(itemMakeField);
        enforceUpperCase(itemModelField);
        enforceUpperCase(itemSerialField);
        enforceUpperCase(itemCategoryField);

        enforceUpperCase(plantField);
        enforceUpperCase(departmentField);
        enforceUpperCase(locationField);

        enforceUpperCase(ipField);
        enforceUpperCase(poField);
        enforceUpperCase(partyField);
        enforceUpperCase(simField);
        enforceUpperCase(imeiField);

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
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")

        );
        File file = chooser.showOpenDialog(
                attachmentField.getScene().getWindow()
        );
        if (file == null) return;
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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Save");
        confirm.setHeaderText("Save Transaction?");
        confirm.setContentText("Are you sure you want to save this transaction?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return null;
        }

        String buySell = buySellBox.getValue();
        String itemCondition = conditionBox.getValue();
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
        String itemLocation = itemLocationField.getText();
        String itemCategory = itemCategoryField.getText();

        String imei = imeiField.getText();
        String sim = simField.getText();

        String po = poField.getText();
        String party = partyField.getText();

        String status = statusBox.getValue();
        if (status != null) {
            status = status.toUpperCase();
        }
        String remarks = remarksField.getText();

        String itemCountText = itemCountField.getText();
        java.math.BigDecimal itemCount;

        if (buySell == null) {
            AlertUtil.showError("Validation Error", "Please mention BUY OR SELL");
            return null;
        }

        if (itemCondition == null) {
            AlertUtil.showError("Validation Error", "Please mention item condition");
            return null;
        }

        if(status == null || status.isEmpty()) {
            AlertUtil.showError("Validation Error", "Please mention the status");
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

            String attachmentToSave = existingAttachmentFile;

            if(selectedAttachment != null) {
                String newFileName = copyAttachmentFile(editTransactionId, selectedAttachment);
                if(newFileName != null) {
                    attachmentToSave = newFileName;
                }
            }
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
                    itemCondition,
                    itemLocation,
                    itemCategory,
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

            if ("Sell".equalsIgnoreCase(buySell)) {
                double currentStock = transactionDAO.getCurrentStock(itemName);
                double sellQty = itemCount.doubleValue();
                if (sellQty > currentStock) {
                    AlertUtil.showError(
                            "Stock Error",
                            "Not enough stock available.\n\n" +
                                    "Available: " + currentStock + " " + unit +
                                    "\nRequested: " + sellQty + " " + unit
                    );

                    return null;
                }
                String serial = itemSerialField.getText();
                if (serial != null && !serial.isBlank()) {
                    boolean available = transactionDAO.isItemAvailableBySerial(serial);
                    if (!available) {
                        AlertUtil.showError(
                                "Item Not Available",
                                "This item is already issued or scrapped.\n\n" +
                                        "Serial: " + serial
                        );
                        return null;
                    }
                }
            }


            transactionId = transactionDAO.createTransaction(
                    null,
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
                    itemCondition,
                    itemLocation,
                    itemCategory,
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


        if(selectedAttachment != null && editTransactionId == null) {
            String newFileName = copyAttachmentFile(transactionId, selectedAttachment);
            if(newFileName != null) {
                transactionDAO.updateAttachment(transactionId, newFileName);
            }
        }
        return transactionId;
    }

    private String copyAttachmentFile(int transactionId, File file) {
        if (file == null) {
            return null;
        }

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
                    file.toPath(),
                    target.toPath()
            );

            return newName;

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.showError(
                    "Attachment Error",
                    "Failed to copy attachment file."
            );
            return null;
        }
    }

    private void applySelection(String selected) {
        itemNameField.setText(selected);
        itemNameField.positionCaret(selected.length());
        suggestionsPopup.hide();
        itemNameField.requestFocus();
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
        itemLocationField.setText(safe(data.itemLocation));

        if(data.itemCategory != null && !data.itemCategory.isEmpty()) {
            itemCategoryField.setText(safe(data.itemCategory));
            itemCategoryField.setDisable(true);
        }

        if(data.itemCondition != null && !data.itemCondition.isEmpty()) {
            conditionBox.setValue(safe(data.itemCondition));
            conditionBox.setDisable(true);
        }

        imeiField.setText(safe(data.imeiNo));
        simField.setText(safe(data.simNo));

        poField.setText(safe(data.poNo));
        partyField.setText(safe(data.partyName));

        unitComboBox.setValue(safe(data.unit));
        statusBox.setValue(safe(data.status).toUpperCase());

        remarksField.setText(safe(data.remarks));

        if (data.attachmentFile != null && !data.attachmentFile.isBlank()) {
            attachmentField.setText(data.attachmentFile);
            existingAttachmentFile = data.attachmentFile;
        }

    }

    public void prefillSell(TransactionPrefill data) {

        prefill(data);   // reuse the normal prefill

        buySellBox.setValue("Sell");
        buySellBox.setDisable(true);

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

    public void hideSaveAndAddAnotherButton() {
        if (saveAndAddAnotherButton != null) {
            buySellBox.setDisable(true);
            saveAndAddAnotherButton.setVisible(false);
            saveAndAddAnotherButton.setManaged(false);
        }
    }

    private void setupEmployeeAutocomplete() {

        List<String> employeeCodes =
                MasterCache.employeeCache.keySet().stream().toList();

        setupSuggestionField(
                employeeCodeField,
                employeeCodes,
                code -> {

                    EmployeeMaster emp =
                            MasterCache.employeeCache.get(code.toLowerCase());

                    if (emp != null) {
                        employeeNameField.setText(emp.getEmployeeName());
                    }
                }
        );
    }


    private void setupItemAutocomplete() {

        List<String> itemCodes =
                MasterCache.itemCache.keySet().stream().toList();

        setupSuggestionField(
                itemCodeField,
                itemCodes,
                code -> {

                    ItemMaster item =
                            MasterCache.itemCache.get(code.toLowerCase());

                    if (item != null) {

                        itemNameField.setText(item.getItemName());

                        if(item.getItemCategory() != null) {
                            itemCategoryField.setText(item.getItemCategory());
                        }
                    }
                }
        );
    }

    private void setupPlantAutocomplete() {

        List<String> plants =
                MasterCache.plantCache.values()
                        .stream()
                        .map(p -> p.getPlantName())
                        .toList();

        setupSuggestionField(
                plantField,
                plants,
                plant -> {}
        );
    }

    private void setupCategoryAutocomplete() {
        List<String> categories =
                MasterCache.categoryCache.values()
                        .stream()
                        .map(c -> c.getCategoryName())
                        .toList();

        setupSuggestionField(
                itemCategoryField,
                categories,
                category -> {}
        );
    }

    private void setupDepartmentAutocomplete() {

        List<String> departments =
                MasterCache.departmentCache.values()
                        .stream()
                        .map(d -> d.getDepartmentName())
                        .toList();

        setupSuggestionField(
                departmentField,
                departments,
                department -> {}
        );
    }


    private void setupSuggestionField(
            TextField field,
            List<String> data,
            java.util.function.Consumer<String> onSelect
    ) {

        Popup popup = new Popup();
        popup.setAutoHide(true);

        ListView<String> listView = new ListView<>();
        listView.setFocusTraversable(false);

        popup.getContent().add(listView);

        field.textProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null || newVal.isBlank()) {
                popup.hide();
                return;
            }

            List<String> filtered = data.stream()
                    .filter(v -> v.toLowerCase().contains(newVal.toLowerCase()))
                    .sorted()
                    .toList();

            if (filtered.isEmpty()) {
                popup.hide();
                return;
            }

            listView.getItems().setAll(filtered);

            int visibleRows = Math.min(filtered.size(), 6);
            listView.setPrefHeight(visibleRows * 26 + 2);
            listView.setPrefWidth(field.getWidth());

            if (!popup.isShowing()) {

                Point2D p = field.localToScreen(0, field.getHeight());

                if (p == null) {
                    return;
                }

                popup.show(field, p.getX(), p.getY());
            }
        });

        // ======================
        // Mouse Selection
        // ======================

        listView.setOnMouseClicked(e -> {

            String selected = listView.getSelectionModel().getSelectedItem();

            if (selected != null) {

                field.setText(selected);
                popup.hide();

                onSelect.accept(selected);
            }
        });

        // ======================
        // Keyboard Navigation
        // ======================

        field.setOnKeyPressed(event -> {

            switch (event.getCode()) {

                case DOWN -> {

                    if (popup.isShowing() && !listView.getItems().isEmpty()) {

                        listView.requestFocus();

                        if (listView.getSelectionModel().isEmpty()) {
                            listView.getSelectionModel().selectFirst();
                        }
                    }
                }

                case ESCAPE -> popup.hide();
            }
        });

        listView.setOnKeyPressed(event -> {

            switch (event.getCode()) {

                case ENTER -> {

                    String selected =
                            listView.getSelectionModel().getSelectedItem();

                    if (selected != null) {

                        field.setText(selected);
                        popup.hide();

                        onSelect.accept(selected);
                        field.requestFocus();
                    }
                }

                case ESCAPE -> {

                    popup.hide();
                    field.requestFocus();
                }
            }
        });

        // ======================
        // Popup reposition
        // ======================

        field.caretPositionProperty().addListener((obs, o, n) -> {

            if (popup.isShowing()) {

                popup.hide();

                Point2D p = field.localToScreen(0, field.getHeight());

                if (p == null) {
                    return;
                }

                popup.show(field, p.getX(), p.getY());

            }
        });
    }



    public void setTransactionType(String type) {

        buySellBox.setValue(type);
        buySellBox.setDisable(true);

        if ("Buy".equalsIgnoreCase(type)) {
            hideBuyFields(true);
        } else {
            statusBox.setValue("Issued");
            hideBuyFields(false);
        }
        changeLabelAndTitle(type);
    }

    public void changeLabelAndTitle(String type) {
        // Update label inside window
        if (titleLabel != null) {
            titleLabel.setText(type + " Item");
        }

        // Update window title
        Platform.runLater(() -> {
            Stage stage = (Stage) buySellBox.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(type + " Item");
            }
        });
    }

    private void enforceUpperCase(TextField field) {

        field.textProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null) return;

            String upper = newVal.toUpperCase();

            if (!upper.equals(newVal)) {
                field.setText(upper);
            }
        });
    }

}