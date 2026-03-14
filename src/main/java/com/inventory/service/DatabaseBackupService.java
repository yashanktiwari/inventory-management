package com.inventory.service;

import com.inventory.database.AppConfig;
import com.inventory.database.DBConnection;
import com.inventory.util.AlertUtil;
import javafx.application.Platform;
import javafx.scene.control.TableView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class DatabaseBackupService {

    public static void createBackup(TableView<?> table, String filePath) {

        new Thread(() -> {

            try {

                Platform.runLater(() -> table.setDisable(true));

                String mysqldumpPath = AppConfig.getMysqlDumpPath();

                if (mysqldumpPath == null) {
                    Platform.runLater(() -> {
                        AlertUtil.showError("Error", "mysqldump path not configured.");
                        table.setDisable(false);
                    });
                    return;
                }

                String host = DBConnection.getHost();
                String port = DBConnection.getPort();
                String db = DBConnection.getDatabaseName();
                String user = DBConnection.getUsername();
                String pass = DBConnection.getPassword();

                File backupFile = new File(filePath);

                ProcessBuilder pb = new ProcessBuilder(
                        mysqldumpPath,
                        "-h", host,
                        "-P", port,
                        "-u", user,
                        "-p" + pass,
                        db
                );

                pb.redirectOutput(backupFile);

                Process process = pb.start();

                BufferedReader errorReader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));

                StringBuilder errorOutput = new StringBuilder();
                String line;

                while ((line = errorReader.readLine()) != null) {

                    if (!line.contains("Using a password on the command line interface can be insecure")) {
                        errorOutput.append(line).append("\n");
                    }
                }

                int exitCode = process.waitFor();

                Platform.runLater(() -> {

                    table.setDisable(false);

                    if (exitCode == 0) {

                        AlertUtil.showInfo(
                                "Success",
                                "Backup created:\n" + backupFile.getAbsolutePath()
                        );

                    } else {

                        AlertUtil.showError(
                                "Backup Failed",
                                errorOutput.isEmpty() ?
                                        "Unknown mysqldump error." :
                                        errorOutput.toString()
                        );
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                Platform.runLater(() -> {
                    table.setDisable(false);
                    AlertUtil.showError("Error", "Backup failed.");
                });
            }

        }).start();
    }


    public static void restoreBackup(TableView<?> table, String filePath, Runnable reload) {

        new Thread(() -> {

            try {

                Platform.runLater(() -> table.setDisable(true));

                String mysqlPath = AppConfig.getMysqlPath();

                if (mysqlPath == null) {

                    Platform.runLater(() -> {
                        AlertUtil.showError("Error", "mysql path not configured.");
                        table.setDisable(false);
                    });

                    return;
                }

                String host = DBConnection.getHost();
                String port = DBConnection.getPort();
                String db = DBConnection.getDatabaseName();
                String user = DBConnection.getUsername();
                String pass = DBConnection.getPassword();

                ProcessBuilder restorePb = new ProcessBuilder(
                        mysqlPath,
                        "-h", host,
                        "-P", port,
                        "-u", user,
                        "-p" + pass,
                        db
                );

                restorePb.redirectInput(new File(filePath));

                Process restoreProcess = restorePb.start();

                BufferedReader restoreReader =
                        new BufferedReader(new InputStreamReader(restoreProcess.getErrorStream()));

                StringBuilder restoreErrors = new StringBuilder();
                String line;

                while ((line = restoreReader.readLine()) != null) {
                    restoreErrors.append(line).append("\n");
                }

                int restoreExit = restoreProcess.waitFor();

                Platform.runLater(() -> {

                    table.setDisable(false);

                    if (restoreExit == 0) {

                        DBConnection.initializeDatabase();

                        reload.run();

                        AlertUtil.showInfo(
                                "Success",
                                "Database restored successfully."
                        );

                    } else {

                        AlertUtil.showError(
                                "Restore Failed",
                                restoreErrors.toString()
                        );
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                Platform.runLater(() -> {
                    table.setDisable(false);
                    AlertUtil.showError("Error", "Restore failed.");
                });
            }

        }).start();
    }
}