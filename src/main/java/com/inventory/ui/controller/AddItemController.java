package com.inventory.ui.controller;

import com.inventory.dao.ItemDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddItemController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descField;

    private final ItemDAO itemDAO = new ItemDAO();

    @FXML
    private void handleSave() {

        String name = nameField.getText();
        String desc = descField.getText();

        if (name == null || name.isBlank()) {
            System.out.println("Item name is required.");
            return;
        }

        itemDAO.saveIfNotExists(name, desc);

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}