package com.inventory.ui.dialog;

import com.inventory.database.AppConfig;
import com.inventory.service.DatabaseBackupService;
import com.inventory.util.AlertUtil;
import com.inventory.util.PasswordUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.File;
import java.util.Optional;

public class DatabaseBackupDialog {

    public static void showBackupDialog(
            TableView<?> table,
            Window owner
    ) {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Backup Database");
        dialog.setHeaderText("Select location to save backup file");

        ButtonType createBtn =
                new ButtonType("Create Backup", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(520);

        Label icon = new Label("💾");
        icon.setStyle("-fx-font-size: 20;");

        TextField pathField = new TextField();
        pathField.setPromptText("Select folder to generate backup file");

        Button browseBtn = new Button("Browse");

        browseBtn.setOnAction(e -> {

            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Backup Folder");

            File folder = chooser.showDialog(owner);

            if (folder != null) {

                String date = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter
                                .ofPattern("ddMMyyyy_HHmmss"));

                String filePath = folder.getAbsolutePath()
                        + File.separator
                        + "backup_" + date + ".sql";

                pathField.setText(filePath);
            }
        });

        HBox pathBox = new HBox(10, pathField, browseBtn);
        HBox.setHgrow(pathField, Priority.ALWAYS);

        HBox content = new HBox(12, icon, pathBox);
        content.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(15, content);
        container.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(container);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) return pathField.getText();
            return null;
        });

        dialog.showAndWait().ifPresent(path -> {

            if (path == null || path.isBlank()) {
                AlertUtil.showError("Error", "Please select a folder.");
                return;
            }

            if (!path.endsWith(".sql")) {
                path += ".sql";
            }

            DatabaseBackupService.createBackup(table, path);
        });
    }


    public static void showRestoreDialog(
            TableView<?> table,
            Window owner,
            Runnable reload
    ) {

        if (!verifyPassword(owner)) return;

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Restore Database");
        dialog.setHeaderText("Select backup file");

        ButtonType restoreBtn =
                new ButtonType("Restore", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(restoreBtn, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(520);

        Label icon = new Label("♻");
        icon.setStyle("-fx-font-size: 20;");

        TextField pathField = new TextField();
        pathField.setPromptText("Select backup (.sql) file");

        Button browseBtn = new Button("Browse");

        browseBtn.setOnAction(e -> {

            FileChooser fc = new FileChooser();

            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("SQL Files", "*.sql")
            );

            File file = fc.showOpenDialog(owner);

            if (file != null) {
                pathField.setText(file.getAbsolutePath());
            }
        });

        HBox pathBox = new HBox(10, pathField, browseBtn);
        HBox.setHgrow(pathField, Priority.ALWAYS);

        HBox content = new HBox(12, icon, pathBox);
        content.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(15, content);
        container.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(container);

        dialog.setResultConverter(btn -> {
            if (btn == restoreBtn) return pathField.getText();
            return null;
        });

        dialog.showAndWait().ifPresent(path -> {

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

            confirm.setHeaderText("All current data will be replaced!");
            confirm.setContentText(
                    "Restoring this backup will overwrite all existing data.\n\nContinue?"
            );

            confirm.showAndWait().ifPresent(res -> {

                if (res == ButtonType.OK) {
                    DatabaseBackupService.restoreBackup(table, path, reload);
                }
            });
        });
    }


    private static boolean verifyPassword(Window owner) {

        Dialog<String> dialog = new Dialog<>();

        dialog.setTitle("Authorization Required");

        ButtonType loginBtn =
                new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();

        VBox box = new VBox(10,
                new Label("Enter admin password"),
                passwordField
        );

        box.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn == loginBtn) return passwordField.getText();
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) return false;

        if (!AppConfig.getAdminPasswordHash()
                .equals(PasswordUtil.hashPassword(result.get()))) {

            AlertUtil.showError(
                    "Access Denied",
                    "Incorrect restore password."
            );

            return false;
        }

        return true;
    }
}