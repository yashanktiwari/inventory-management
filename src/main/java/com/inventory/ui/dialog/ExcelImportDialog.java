package com.inventory.ui.dialog;

import com.inventory.util.ExcelImportTask;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import java.io.File;

public class ExcelImportDialog {

    public static void showImportDialog(
            Window owner,
            Runnable onFinish
    ) {

        FileChooser chooser = new FileChooser();

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        File file = chooser.showOpenDialog(owner);

        if (file == null) return;

        ExcelImportTask task = new ExcelImportTask(file);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(350);
        progressBar.progressProperty().bind(task.progressProperty());

        Label progressLabel = new Label();
        progressLabel.textProperty().bind(task.messageProperty());

        Button finishButton = new Button("Finish");
        finishButton.setDisable(true);

        VBox layout = new VBox(15, progressBar, progressLabel, finishButton);
        layout.setStyle("-fx-padding:20; -fx-alignment:center;");

        Stage dialog = new Stage();
        dialog.setTitle("Import Excel");
        dialog.setScene(new Scene(layout));
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        dialog.setOnCloseRequest(event -> {
            if (task.isRunning()) {
                event.consume();
            }
        });

        dialog.show();

        finishButton.setOnAction(e -> {
            dialog.close();
            onFinish.run();
        });

        task.setOnSucceeded(e -> {

            progressLabel.textProperty().unbind();

            progressLabel.setText(
                    "Import completed: " + task.getValue() + " rows imported."
            );

            finishButton.setDisable(false);
        });

        task.setOnFailed(e -> {

            progressLabel.textProperty().unbind();

            progressLabel.setText(
                    "Import failed: " + task.getException().getMessage()
            );

            finishButton.setDisable(false);
        });

        new Thread(task).start();
    }
}