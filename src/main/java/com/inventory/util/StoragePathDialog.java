package com.inventory.util;

import com.inventory.database.AppConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class StoragePathDialog {

    public static void show(Stage owner) {

        Stage stage = new Stage();
        stage.setTitle("Attachment Storage Setup");
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);

        Label icon = new Label("📂");
        icon.setStyle("-fx-font-size: 24;");

        Label info = new Label(
                "Please select the folder where transaction attachments will be stored.\n" +
                        "This folder may be on a server or local drive."
        );
        info.setWrapText(true);

        HBox header = new HBox(10, icon, info);
        header.setAlignment(Pos.CENTER_LEFT);

        /* ---------------- Attachment Path ---------------- */

        TextField pathField = new TextField();
        pathField.setPrefWidth(350);
        pathField.setPromptText("Select attachment storage folder");

        String existingPath = AppConfig.getAttachmentPath();
        if (existingPath != null && !existingPath.isBlank()) {
            pathField.setText(existingPath);
        }

        Button browseBtn = new Button("Browse");
        browseBtn.setMinWidth(90);

        browseBtn.setOnAction(e -> {

            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Storage Folder");

            File dir = chooser.showDialog(stage);

            if (dir != null) {
                pathField.setText(dir.getAbsolutePath());
            }
        });

        HBox pathBox = new HBox(10, pathField, browseBtn);
        HBox.setHgrow(pathField, Priority.ALWAYS);


        /* ---------------- MYSQL PATH ---------------- */

        TextField mysqlField = new TextField();
        mysqlField.setPrefWidth(350);
        mysqlField.setPromptText("Select mysql.exe");

        String mysqlPath = AppConfig.getMysqlPath();
        if (mysqlPath != null && !mysqlPath.isBlank()) {
            mysqlField.setText(mysqlPath);
        }

        Button browseMysqlBtn = new Button("Browse");
        browseMysqlBtn.setMinWidth(90);

        browseMysqlBtn.setOnAction(e -> {

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select mysql.exe");

            File file = chooser.showOpenDialog(stage);

            if (file != null) {
                mysqlField.setText(file.getAbsolutePath());
            }
        });

        HBox mysqlBox = new HBox(10, mysqlField, browseMysqlBtn);
        HBox.setHgrow(mysqlField, Priority.ALWAYS);


        /* ---------------- MYSQLDUMP PATH ---------------- */

        TextField dumpField = new TextField();
        dumpField.setPrefWidth(350);
        dumpField.setPromptText("Select mysqldump.exe");

        String dumpPath = AppConfig.getMysqlDumpPath();
        if (dumpPath != null && !dumpPath.isBlank()) {
            dumpField.setText(dumpPath);
        }

        Button browseDumpBtn = new Button("Browse");
        browseDumpBtn.setMinWidth(90);

        browseDumpBtn.setOnAction(e -> {

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select mysqldump.exe");

            File file = chooser.showOpenDialog(stage);

            if (file != null) {
                dumpField.setText(file.getAbsolutePath());
            }
        });

        HBox dumpBox = new HBox(10, dumpField, browseDumpBtn);
        HBox.setHgrow(dumpField, Priority.ALWAYS);


        /* ---------------- TEST STORAGE ---------------- */

        Button testBtn = new Button("Test Connection");

        Label resultLabel = new Label();

        testBtn.setOnAction(e -> {

            String path = pathField.getText();

            try {

                Path p = Path.of(path);

                if (Files.exists(p) && Files.isDirectory(p) && Files.isWritable(p)) {

                    resultLabel.setText("✓ Connection successful");
                    resultLabel.setStyle("-fx-text-fill: green;");

                } else {

                    resultLabel.setText("✗ Folder not accessible");
                    resultLabel.setStyle("-fx-text-fill: red;");
                }

            } catch (Exception ex) {

                resultLabel.setText("✗ Invalid path");
                resultLabel.setStyle("-fx-text-fill: red;");
            }
        });


        /* ---------------- SAVE BUTTON ---------------- */

        Button saveBtn = new Button("Save & Continue");

        saveBtn.setStyle(
                "-fx-background-color:#0078D7;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-weight:bold;"
        );

        saveBtn.setOnAction(e -> {

            String newPath = pathField.getText();
            String mysql = mysqlField.getText();
            String dump = dumpField.getText();

            try {

                Path newDir = Path.of(newPath);

                if (!Files.exists(newDir)) {
                    Files.createDirectories(newDir);
                }

                if (!Files.isWritable(newDir)) {
                    resultLabel.setText("Folder not writable");
                    resultLabel.setStyle("-fx-text-fill:red;");
                    return;
                }

                Path newTransactions = newDir.resolve("transactions");
                Files.createDirectories(newTransactions);

                String oldPath = AppConfig.getAttachmentPath();

                if (oldPath != null && !oldPath.equals(newPath)) {

                    if (!verifyPassword()) {
                        return;
                    }

                    Path oldTransactions = Path.of(oldPath, "transactions");

                    if (Files.exists(oldTransactions)) {

                        migrateAttachments(
                                stage,
                                oldTransactions,
                                newTransactions,
                                newPath
                        );
                    }
                }

                /* SAVE CONFIG */
                AppConfig.saveAttachmentPath(newPath);
                AppConfig.saveMysqlPath(mysql);
                AppConfig.saveMysqlDumpPath(dump);

                stage.close();

            } catch (Exception ex) {

                resultLabel.setText("Unable to access path");
                resultLabel.setStyle("-fx-text-fill:red;");
            }
        });


        /* ---------------- ACTIONS ---------------- */

        HBox actions = new HBox(10, testBtn, saveBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);


        /* ---------------- ROOT ---------------- */

        VBox root = new VBox(15,
                header,
                pathBox,
                mysqlBox,
                dumpBox,
                resultLabel,
                actions
        );

        root.setPadding(new Insets(20));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(520);

        stage.showAndWait();
    }

    private static void migrateAttachments(
            Stage parentStage,
            Path oldTransactions,
            Path newTransactions,
            String newPath
    ) {
        Stage progressStage = new Stage();
        progressStage.setTitle("Migrating Attachments");
        progressStage.initOwner(parentStage.getOwner());
        progressStage.initModality(Modality.APPLICATION_MODAL);
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(320);
        Label statusLabel = new Label("Preparing migration...");
        Button finishBtn = new Button("Finish");
        finishBtn.setDisable(true);
        VBox root = new VBox(15, statusLabel, progressBar, finishBtn);
        root.setPadding(new Insets(20));
        progressStage.setScene(new Scene(root, 380, 160));
        progressStage.setAlwaysOnTop(true);
        progressStage.show();
        progressStage.requestFocus();
        new Thread(() -> {
            try {
                var files = Files.list(oldTransactions).toList();
                int total = files.size();
                int count = 0;
                if (total == 0) {
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(1);
                        statusLabel.setText("No attachments found.");
                        finishBtn.setDisable(false);
                    });
                    return;
                }
                for (Path file : files) {
                    Path target =
                            newTransactions.resolve(file.getFileName());
                    Files.copy(
                            file,
                            target,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                    count++;
                    double progress = (double) count / total;
                    int current = count;
                    // small animation delay like Windows
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(progress);
                        statusLabel.setText(
                                "Copied " + current + " / " + total + " files"
                        );
                    });
                }
                javafx.application.Platform.runLater(() -> {
                    progressBar.setProgress(1);
                    statusLabel.setText("Migration completed successfully.");
                    finishBtn.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Migration failed.");
                    finishBtn.setDisable(false);
                });
            }
        }).start();
        finishBtn.setOnAction(e -> {
            // Save new path only AFTER migration completes
            AppConfig.saveAttachmentPath(newPath);
            progressStage.close();
            parentStage.close();
        });
    }

    private static boolean verifyPassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Authorization Required");
        dialog.setHeaderText("Enter admin password");
        ButtonType verifyBtn =
                new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(verifyBtn, ButtonType.CANCEL);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        VBox box = new VBox(10, passwordField);
        box.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(btn -> {
            if (btn == verifyBtn) {
                return passwordField.getText();
            }
            return null;
        });
        String result = dialog.showAndWait().orElse(null);
        if (result == null) return false;
        if (!AppConfig.getAdminPasswordHash().equals(result)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setContentText("Incorrect password");
            alert.showAndWait();
            return false;
        }
        return true;
    }
}