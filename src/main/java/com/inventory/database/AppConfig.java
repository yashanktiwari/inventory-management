package com.inventory.database;

import java.io.*;
import java.util.Properties;
import com.inventory.util.PasswordUtil;

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

            Properties props = loadProperties();

            props.setProperty("db.host", host);
            props.setProperty("db.port", port);
            props.setProperty("db.name", dbName);
            props.setProperty("db.user", user);
            props.setProperty("db.pass", pass);

            saveProperties(props);

        } catch (Exception e) {
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

    public static void saveAttachmentPath(String path) {

        try {

            Properties props = loadProperties();

            props.setProperty("attachment.path", path);

            saveProperties(props);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAttachmentPath() {

        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) return null;

            Properties props = new Properties();

            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            }

            return props.getProperty("attachment.path");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Properties loadProperties() throws IOException {

        File file = new File(CONFIG_FILE);
        Properties props = new Properties();

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            }
        }

        return props;
    }

    private static void saveProperties(Properties props) throws IOException {

        File folder = new File(CONFIG_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Inventory Config");
        }
    }

    public static void saveMysqlPath(String path) {

        try {
            Properties props = loadProperties();
            props.setProperty("mysql.path", path);
            saveProperties(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getMysqlPath() {

        try {
            Properties props = loadProperties();
            return props.getProperty("mysql.path");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveMysqlDumpPath(String path) {

        try {
            Properties props = loadProperties();
            props.setProperty("mysqldump.path", path);
            saveProperties(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getMysqlDumpPath() {

        try {
            Properties props = loadProperties();
            return props.getProperty("mysqldump.path");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveAdminPassword(String password) {
        try {
            Properties props = loadProperties();
            String hashedPassword = PasswordUtil.hashPassword(password);
            props.setProperty("admin.password.hash", hashedPassword);
            saveProperties(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAdminPasswordHash() {
        try {
            Properties props = loadProperties();
            String storedHash = props.getProperty("admin.password.hash");
            if (storedHash == null || storedHash.isEmpty()) {
                return PasswordUtil.hashPassword("admin123");
            }
            return storedHash;
        } catch (Exception e) {
            e.printStackTrace();
            return PasswordUtil.hashPassword("admin123");
        }
    }
}