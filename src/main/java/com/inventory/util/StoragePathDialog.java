package com.inventory.util;

import com.inventory.database.AppConfig;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
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
        stage.initModality(Modality.NONE);

        Label info = new Label(
                "Please select the folder where transaction attachments will be stored.\n" +
                        "This folder may be on a server or local drive."
        );

        TextField pathField = new TextField();
        pathField.setPrefWidth(350);

        Button browseBtn = new Button("Browse");

        browseBtn.setOnAction(e -> {

            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Storage Folder");

            File dir = chooser.showDialog(stage);

            if (dir != null) {
                pathField.setText(dir.getAbsolutePath());
            }
        });

        HBox pathBox = new HBox(10, pathField, browseBtn);

        Button testBtn = new Button("Test Connection");
        Label resultLabel = new Label();

        testBtn.setOnAction(e -> {

            String path = pathField.getText();

            try {

                Path p = Path.of(path);

                if (Files.exists(p) && Files.isDirectory(p) && Files.isWritable(p)) {

                    resultLabel.setText("Connection successful");
                    resultLabel.setStyle("-fx-text-fill: green;");

                } else {

                    resultLabel.setText("Folder not accessible");
                    resultLabel.setStyle("-fx-text-fill: red;");

                }

            } catch (Exception ex) {

                resultLabel.setText("Invalid path");
                resultLabel.setStyle("-fx-text-fill: red;");

            }

        });

        Button saveBtn = new Button("Save & Continue");

        saveBtn.setOnAction(e -> {

            String path = pathField.getText();

            try {

                Path p = Path.of(path);

                if (!Files.exists(p)) {
                    Files.createDirectories(p);
                }

                if (!Files.isWritable(p)) {
                    resultLabel.setText("Folder not writable");
                    return;
                }

                // create transactions folder
                Files.createDirectories(p.resolve("transactions"));

                AppConfig.saveAttachmentPath(path);

                stage.close();

            } catch (Exception ex) {

                resultLabel.setText("Unable to access path");
                resultLabel.setStyle("-fx-text-fill:red;");

            }

        });

        VBox root = new VBox(15,
                info,
                pathBox,
                testBtn,
                resultLabel,
                saveBtn
        );

        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root));
        stage.setWidth(500);
        stage.showAndWait();
    }
}