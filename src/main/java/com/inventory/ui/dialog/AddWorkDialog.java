package com.inventory.ui.dialog;

import com.inventory.MainApp;
import com.inventory.dao.WorkLogDAO;
import com.inventory.model.WorkLog;
import com.inventory.model.WorkType;
import com.inventory.util.UserUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;

public class AddWorkDialog {

    public static void show(Runnable onSave) {

        Stage stage = new Stage();
        stage.setTitle("Add Work");
        stage.initModality(Modality.APPLICATION_MODAL);

        // Controls
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Regular", "Achievement");
        typeBox.setValue("Regular");
        typeBox.setPrefWidth(220);

        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Describe the work...");
        detailsArea.setPrefHeight(120);
        detailsArea.setWrapText(true);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("primary-button");
        Button cancelBtn = new Button("Cancel");

        // Form layout
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        Label typeLabel = new Label("Type:");
        Label detailsLabel = new Label("Work Details:");

        form.add(typeLabel, 0, 0);
        form.add(typeBox, 1, 0);

        form.add(detailsLabel, 0, 1);
        form.add(detailsArea, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(col1, col2);

        // Button bar
        HBox buttonBar = new HBox(10, cancelBtn, saveBtn);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        // Title
        Label title = new Label("Add Work Entry");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        // Container
        VBox container = new VBox(16, title, form, buttonBar);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("dialog-card");

        // Scene
        Scene scene = new Scene(container, 420, 260);

        scene.getStylesheets().add(MainApp.GLOBAL_CSS);
        scene.getStylesheets().add(
                Objects.requireNonNull(AddWorkDialog.class.getResource("/css/worklog.css")).toExternalForm()
        );

        stage.setScene(scene);

        // Button actions
        saveBtn.setOnAction(e -> {

            String details = detailsArea.getText().trim();

            if (details.isEmpty()) {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("Details Required");
                alert.setContentText("Please enter work details.");
                alert.showAndWait();

                return;
            }

            WorkType type = "Achievement".equals(typeBox.getValue())
                    ? WorkType.ACHIEVEMENT
                    : WorkType.REGULAR;

            WorkLog log = new WorkLog(
                    UserUtil.getCurrentUser(),
                    type,
                    details
            );

            boolean success = new WorkLogDAO().insertWorkLog(log);

            if (success) {

                if (onSave != null) {
                    onSave.run();
                }

                stage.close();

            } else {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Save Failed");
                alert.setContentText("Could not save work log.");
                alert.showAndWait();
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        stage.showAndWait();
    }
}