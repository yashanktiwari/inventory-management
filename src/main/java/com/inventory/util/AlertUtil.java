package com.inventory.util;

import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Modality;

import java.util.Optional;

public class AlertUtil {

    private static Alert createAlert(Alert.AlertType type, String title, String message) {

        Alert alert = new Alert(type);
        alert.initModality(Modality.APPLICATION_MODAL);

        alert.setTitle(title);
        alert.setHeaderText(title);

        Label content = new Label(message);
        content.setWrapText(true);
        content.setFont(Font.font(14));
        content.setMaxWidth(420);

        alert.getDialogPane().setContent(content);

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setPrefWidth(450);

        alert.getDialogPane().setStyle("""
                -fx-background-color: white;
                -fx-font-size: 14px;
                """);

        return alert;
    }

    public static void showInfo(String title, String message) {

        Alert alert = createAlert(Alert.AlertType.INFORMATION, title, message);

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("""
                -fx-background-color:#2ecc71;
                -fx-text-fill:white;
                -fx-font-weight:bold;
                """);

        alert.showAndWait();
    }

    public static void showError(String title, String message) {

        Alert alert = createAlert(Alert.AlertType.ERROR, title, message);

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("""
                -fx-background-color:#e74c3c;
                -fx-text-fill:white;
                -fx-font-weight:bold;
                """);

        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {

        Alert alert = createAlert(Alert.AlertType.WARNING, title, message);

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("""
                -fx-background-color:#f39c12;
                -fx-text-fill:white;
                -fx-font-weight:bold;
                """);

        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {

        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, title, message);

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Confirm");
        okButton.setStyle("""
                -fx-background-color:#3498db;
                -fx-text-fill:white;
                -fx-font-weight:bold;
                """);

        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Cancel");

        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;
    }
}