package com.inventory;

import com.inventory.database.AppConfig;
import com.inventory.database.DBConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.File;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // 🔥 Try auto load MySQL config
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

        Scene scene = new Scene(loader.load(), 800, 500);

        stage.setTitle("Inventory Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
