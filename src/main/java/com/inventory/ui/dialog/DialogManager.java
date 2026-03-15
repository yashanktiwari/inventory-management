package com.inventory.ui.dialog;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.Objects;
import java.util.Optional;

public class DialogManager {

    public static Optional<Pair<String,String>> showStatusDialog(String currentStatus, String remarks) {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Update Status");

        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(DialogManager.class.getResource("/css/dialog.css")).toExternalForm()
        );

        ChoiceBox<String> statusChoice = new ChoiceBox<>();
        statusChoice.getItems().addAll("RETURNED", "SCRAPPED");
        statusChoice.setValue(currentStatus);

        TextArea remarksArea = new TextArea(remarks);
        remarksArea.setPrefRowCount(3);

        VBox content = new VBox(10,
                new Label("Status"),
                statusChoice,
                new Label("Remarks"),
                remarksArea
        );

        dialog.getDialogPane().setContent(content);

        ButtonType updateBtnType =
                new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                updateBtnType,
                ButtonType.CANCEL
        );

        dialog.setResultConverter(button -> {

            if (button == updateBtnType) {

                return new Pair<>(
                        statusChoice.getValue(),
                        remarksArea.getText()
                );
            }

            return null;
        });

        return dialog.showAndWait();
    }

    public static Optional<String> showAdminPasswordDialog() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Admin Authorization");

        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(
                        DialogManager.class.getResource("/css/dialog.css")
                ).toExternalForm()
        );

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter admin password");

        Platform.runLater(passwordField::requestFocus);

        VBox content = new VBox(10,
                new Label("Admin Password Required"),
                passwordField
        );

        dialog.getDialogPane().setContent(content);

        ButtonType confirmBtn =
                new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                confirmBtn,
                ButtonType.CANCEL
        );

        dialog.setResultConverter(button -> {

            if (button == confirmBtn) {
                return passwordField.getText();
            }

            return null;
        });

        return dialog.showAndWait();
    }
}