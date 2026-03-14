package com.inventory.ui.dialog;

import com.inventory.database.AppConfig;
import com.inventory.util.AlertUtil;
import com.inventory.util.PasswordUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class AdminPasswordDialog {

    public static void show(Window owner) {

        Stage stage = new Stage();
        stage.setTitle("Change Admin Password");

        Label description = new Label(
                "Update the administrator password used to access restricted settings."
        );
        description.setWrapText(true);

        PasswordField currentPass = new PasswordField();
        currentPass.setPromptText("Current Password");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm New Password");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.add(new Label("Current Password"), 0, 0);
        form.add(currentPass, 1, 0);

        form.add(new Label("New Password"), 0, 1);
        form.add(newPass, 1, 1);

        form.add(new Label("Confirm Password"), 0, 2);
        form.add(confirmPass, 1, 2);

        Button cancelBtn = new Button("Cancel");
        Button saveBtn = new Button("Save Password");

        cancelBtn.setOnAction(e -> stage.close());

        saveBtn.setOnAction(e -> {

            String storedHash = AppConfig.getAdminPasswordHash();

            if (!PasswordUtil.hashPassword(currentPass.getText()).equals(storedHash)) {
                AlertUtil.showError("Error", "Current password is incorrect.");
                return;
            }

            if (newPass.getText().isEmpty()) {
                AlertUtil.showError("Error", "New password cannot be empty.");
                return;
            }

            if (!newPass.getText().equals(confirmPass.getText())) {
                AlertUtil.showError("Error", "Passwords do not match.");
                return;
            }

            AppConfig.saveAdminPassword(newPass.getText());

            AlertUtil.showInfo(
                    "Password Changed",
                    "Admin password updated successfully."
            );

            stage.close();
        });

        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(20, description, form, buttons);
        root.setPadding(new Insets(25));

        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}