package com.inventory.ui.dialog;

import com.inventory.database.AppConfig;
import com.inventory.database.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

public class DatabaseSetupDialog {

    public static void show(Window owner, Runnable onSuccess) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Setup Database");
        dialog.setHeaderText("Enter Server Details");
        dialog.getDialogPane().setPrefWidth(450);

        ButtonType connectBtn =
                new ButtonType("Use Database", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(connectBtn, ButtonType.CANCEL);

        Label icon = new Label("🗄");
        icon.setStyle("-fx-font-size: 20;");

        TextField hostField = new TextField("localhost");
        TextField portField = new TextField("3306");
        TextField dbNameField = new TextField();
        TextField userField = new TextField("root");
        PasswordField passField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Host:"), 0, 0);
        grid.add(hostField, 1, 0);

        grid.add(new Label("Port:"), 0, 1);
        grid.add(portField, 1, 1);

        grid.add(new Label("Database Name:"), 0, 2);
        grid.add(dbNameField, 1, 2);

        grid.add(new Label("Username:"), 0, 3);
        grid.add(userField, 1, 3);

        grid.add(new Label("Password:"), 0, 4);
        grid.add(passField, 1, 4);

        HBox content = new HBox(12, icon, grid);
        content.setAlignment(Pos.TOP_LEFT);

        VBox container = new VBox(15, content);
        container.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(container);

        dialog.showAndWait().ifPresent(response -> {

            if (response == ButtonType.CANCEL) return;

            try {

                DBConnection.setDatabaseConfig(
                        hostField.getText(),
                        portField.getText(),
                        dbNameField.getText(),
                        userField.getText(),
                        passField.getText()
                );

                DBConnection.initializeDatabase();

                AppConfig.saveDatabaseConfig(
                        hostField.getText(),
                        portField.getText(),
                        dbNameField.getText(),
                        userField.getText(),
                        passField.getText()
                );

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Database connected successfully.");
                alert.showAndWait();

                onSuccess.run();

            } catch (Exception e) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Could not connect to MySQL");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }
}