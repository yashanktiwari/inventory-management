package com.inventory;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import com.inventory.database.AppConfig;
import com.inventory.database.DBConnection;
import com.inventory.ui.controller.DashboardController;
import com.inventory.util.StoragePathDialog;


public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        // 🔹 Try auto load MySQL config
//        java.util.prefs.Preferences.userNodeForPackage(
//                com.inventory.ui.controller.DashboardController.class
//        ).removeNode();
        boolean loaded = AppConfig.loadDatabaseConfig();

        if (loaded) {
            try {
                DBConnection.createDatabaseIfNotExists();
                DBConnection.initializeDatabase();
            } catch (Exception ignored) {}
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dashboard.fxml")
        );

        Parent root = loader.load();   // 🔹 LOAD ONLY ONCE

        DashboardController controller = loader.getController();

        Scene scene = new Scene(root, 1200, 650);

        scene.getStylesheets().add(
                getClass().getResource("/css/table-filter.css").toExternalForm()
        );
        stage.setTitle("Inventory Management System");
        stage.setScene(scene);

        // 🔹 Save column order when app closes
        stage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.saveFilters();
                controller.saveColumnOrder();
                controller.shutdownConnectionMonitor();
            }
        });

        stage.show();

        Platform.runLater(() -> {
            String attachmentPath = AppConfig.getAttachmentPath();
            if (attachmentPath == null || attachmentPath.isBlank()) {
                StoragePathDialog.show(stage);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
