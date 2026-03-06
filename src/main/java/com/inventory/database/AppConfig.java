package com.inventory.database;

import java.io.*;
import java.util.Properties;

public class AppConfig {

    private static final String CONFIG_FOLDER =
            System.getProperty("user.home")
                    + File.separator + "AppData"
                    + File.separator + "Local"
                    + File.separator + "InventoryManagement";

    private static final String CONFIG_FILE =
            CONFIG_FOLDER + File.separator + "config.properties";

    public static void saveDatabaseConfig(
            String host,
            String port,
            String dbName,
            String user,
            String pass
    ) {
        try {
            File folder = new File(CONFIG_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            Properties props = new Properties();
            props.setProperty("db.host", host);
            props.setProperty("db.port", port);
            props.setProperty("db.name", dbName);
            props.setProperty("db.user", user);
            props.setProperty("db.pass", pass);

            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "Inventory Management MySQL Config");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean loadDatabaseConfig() {

        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) return false;

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            }

            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            String dbName = props.getProperty("db.name");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.pass");

            DBConnection.setDatabaseConfig(host, port, dbName, user, pass);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}