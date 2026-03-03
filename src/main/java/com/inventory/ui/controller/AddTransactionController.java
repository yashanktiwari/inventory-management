package com.inventory.ui.controller;

import com.inventory.dao.ItemDAO;
import com.inventory.dao.PersonDAO;
import com.inventory.dao.TransactionDAO;
import com.inventory.model.Item;
import com.inventory.model.Person;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.List;

public class AddTransactionController {

    @FXML private TextField itemNameField;
    @FXML private TextField itemIdField;
    @FXML private TextField employeeIdField;
    @FXML private TextField personNameField;
    @FXML private TextField departmentField;
    @FXML private TextArea remarksField;

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

        setupAutoComplete();
    }

    // ================= SAVE =================

    @FXML
    private void handleSave() {

        String selectedItemName = itemNameField.getText().trim();
        String itemId = itemIdField.getText().trim();
        String employeeId = employeeIdField.getText().trim();
        String personName = personNameField.getText().trim();
        String department = departmentField.getText().trim();
        String remarks = remarksField.getText().trim();

        if (selectedItemName.isBlank() || itemId.isBlank()
                || employeeId.isBlank() || personName.isBlank()) {
            showError("All required fields must be filled.");
            return;
        }

        // 1️⃣ Find or create item
        Item item = itemDAO.findById(itemId);

        if (item == null) {
            item = itemDAO.createItemWithId(itemId, selectedItemName);
            if (item == null) {
                showError("Failed to create item.");
                return;
            }
        }

        // 2️⃣ Check availability
        if (transactionDAO.isItemCurrentlyIssued(itemId)) {
            showError("This specific item is already issued.");
            return;
        }

        // 3️⃣ Save person
        Person person = personDAO.saveIfNotExists(
                employeeId,
                personName,
                department
        );

        if (person == null) {
            showError("Failed to save person.");
            return;
        }

        // 4️⃣ Issue item
        transactionDAO.issueItem(itemId, person.getPersonId(), remarks);

        closeWindow();
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

    // ================= UTIL =================

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        stage.close();
    }
}