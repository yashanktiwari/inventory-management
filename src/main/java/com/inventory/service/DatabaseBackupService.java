package com.inventory.service;

import com.inventory.database.AppConfig;
import com.inventory.database.DBConnection;
import com.inventory.util.AlertUtil;
import com.inventory.util.FileUtil;
import javafx.application.Platform;
import javafx.scene.control.TableView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class DatabaseBackupService {

//    public static void createBackup(TableView<?> table, String filePath) {
//
//        new Thread(() -> {
//
//            try {
//
//                Platform.runLater(() -> table.setDisable(true));
//
//                String mysqldumpPath = AppConfig.getMysqlDumpPath();
//
//                if (mysqldumpPath == null) {
//                    Platform.runLater(() -> {
//                        AlertUtil.showError("Error", "mysqldump path not configured.");
//                        table.setDisable(false);
//                    });
//                    return;
//                }
//
//                String host = DBConnection.getHost();
//                String port = DBConnection.getPort();
//                String db = DBConnection.getDatabaseName();
//                String user = DBConnection.getUsername();
//                String pass = DBConnection.getPassword();
//
//                File backupFile = new File(filePath);
//
//                ProcessBuilder pb = new ProcessBuilder(
//                        mysqldumpPath,
//                        "--routines",
//                        "--triggers",
//                        "--single-transaction",
//                        "--add-drop-table",
//                        "-h", host,
//                        "-P", port,
//                        "-u", user,
//                        "-p" + pass,
//                        db
//                );
//
//                pb.redirectOutput(backupFile);
//
//                Process process = pb.start();
//
//                BufferedReader errorReader =
//                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
//
//                StringBuilder errorOutput = new StringBuilder();
//                String line;
//
//                while ((line = errorReader.readLine()) != null) {
//
//                    if (!line.contains("Using a password on the command line interface can be insecure")) {
//                        errorOutput.append(line).append("\n");
//                    }
//                }
//
//                int exitCode = process.waitFor();
//
//                Platform.runLater(() -> {
//
//                    table.setDisable(false);
//
//                    if (exitCode == 0) {
//
//                        AlertUtil.showInfo(
//                                "Success",
//                                "Backup created:\n" + backupFile.getAbsolutePath()
//                        );
//
//                    } else {
//
//                        AlertUtil.showError(
//                                "Backup Failed",
//                                errorOutput.isEmpty() ?
//                                        "Unknown mysqldump error." :
//                                        errorOutput.toString()
//                        );
//                    }
//                });
//
//            } catch (Exception e) {
//
//                e.printStackTrace();
//
//                Platform.runLater(() -> {
//                    table.setDisable(false);
//                    AlertUtil.showError("Error", "Backup failed.");
//                });
//            }
//
//        }).start();
//    }

    public static void createBackup(TableView<?> table, String filePath) {

        final String selectedPath = filePath;

        new Thread(() -> {

            File tempDir = new File(System.getProperty("java.io.tmpdir"), "inventory_backup");

            try {

                Platform.runLater(() -> table.setDisable(true));

                if (tempDir.exists()) {
                    FileUtil.deleteDirectory(tempDir);
                }
                tempDir.mkdirs();

                System.out.println("Temp backup dir: " + tempDir.getAbsolutePath());

                File sqlFile = new File(tempDir, "database.sql");

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

                ProcessBuilder pb = new ProcessBuilder(
                        mysqldumpPath,
                        "--routines",
                        "--triggers",
                        "--single-transaction",
                        "--add-drop-table",
                        "-h", host,
                        "-P", port,
                        "-u", user,
                        "-p" + pass,
                        db
                );

                pb.redirectOutput(sqlFile);

                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    throw new RuntimeException("mysqldump failed");
                }

                // copy attachments
                String attachmentPath = AppConfig.getAttachmentPath();

                System.out.println("Attachment path: " + attachmentPath);


                if (attachmentPath != null) {

                    File src = new File(attachmentPath, "transactions");

                    if (src.exists() && src.isDirectory()) {

                        File dest = new File(tempDir, "attachments");

                        FileUtil.copyDirectory(src, dest);

                        System.out.println("Attachments copied: " + dest.exists());
                        System.out.println("Files inside: " + (dest.listFiles() == null ? 0 : dest.listFiles().length));

                    } else {
                        System.out.println("Attachment folder not found: " + attachmentPath);
                    }
                }

                String zipPath = selectedPath;

                if (zipPath.toLowerCase().endsWith(".sql")) {
                    zipPath = zipPath.substring(0, zipPath.length() - 4) + ".zip";
                } else if (!zipPath.toLowerCase().endsWith(".zip")) {
                    zipPath = zipPath + ".zip";
                }

                File zipFile = new File(zipPath);

                File parent = zipFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                for (File f : tempDir.listFiles()) {
                    System.out.println("Temp contains: " + f.getName());
                }

                FileUtil.zipFolderContents(tempDir, zipFile);

                FileUtil.deleteDirectory(tempDir);

                Platform.runLater(() -> {
                    table.setDisable(false);
                    AlertUtil.showInfo("Success", "Backup created:\n" + zipFile.getAbsolutePath());
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


//    public static void restoreBackup(TableView<?> table, String filePath, Runnable reload) {
//
//        new Thread(() -> {
//
//            try {
//
//                Platform.runLater(() -> table.setDisable(true));
//
//                String mysqlPath = AppConfig.getMysqlPath();
//
//                if (mysqlPath == null) {
//
//                    Platform.runLater(() -> {
//                        AlertUtil.showError("Error", "mysql path not configured.");
//                        table.setDisable(false);
//                    });
//
//                    return;
//                }
//
//                String host = DBConnection.getHost();
//                String port = DBConnection.getPort();
//                String db = DBConnection.getDatabaseName();
//                String user = DBConnection.getUsername();
//                String pass = DBConnection.getPassword();
//
//                ProcessBuilder restorePb = new ProcessBuilder(
//                        mysqlPath,
//                        "-h", host,
//                        "-P", port,
//                        "-u", user,
//                        "-p" + pass,
//                        db
//                );
//
//                restorePb.redirectInput(new File(filePath));
//
//                Process restoreProcess = restorePb.start();
//
//                BufferedReader restoreReader =
//                        new BufferedReader(new InputStreamReader(restoreProcess.getErrorStream()));
//
//                StringBuilder restoreErrors = new StringBuilder();
//                String line;
//
//                while ((line = restoreReader.readLine()) != null) {
//                    restoreErrors.append(line).append("\n");
//                }
//
//                int restoreExit = restoreProcess.waitFor();
//
//                Platform.runLater(() -> {
//
//                    table.setDisable(false);
//
//                    if (restoreExit == 0) {
//
//                        DBConnection.initializeDatabase();
//
//                        reload.run();
//
//                        AlertUtil.showInfo(
//                                "Success",
//                                "Database restored successfully."
//                        );
//
//                    } else {
//
//                        AlertUtil.showError(
//                                "Restore Failed",
//                                restoreErrors.toString()
//                        );
//                    }
//                });
//
//            } catch (Exception e) {
//
//                e.printStackTrace();
//
//                Platform.runLater(() -> {
//                    table.setDisable(false);
//                    AlertUtil.showError("Error", "Restore failed.");
//                });
//            }
//
//        }).start();
//    }

    public static void restoreBackup(TableView<?> table, String filePath, Runnable reload) {

        new Thread(() -> {

            File tempDir = new File(System.getProperty("java.io.tmpdir"), "inventory_restore");

            try {

                Platform.runLater(() -> table.setDisable(true));

                FileUtil.unzip(filePath, tempDir);

                File sqlFile = new File(tempDir, "database.sql");

                String mysqlPath = AppConfig.getMysqlPath();

                String host = DBConnection.getHost();
                String port = DBConnection.getPort();
                String db = DBConnection.getDatabaseName();
                String user = DBConnection.getUsername();
                String pass = DBConnection.getPassword();

                ProcessBuilder pb = new ProcessBuilder(
                        mysqlPath,
                        "-h", host,
                        "-P", port,
                        "-u", user,
                        "-p" + pass,
                        db
                );

                pb.redirectInput(sqlFile);

                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    throw new RuntimeException("restore failed");
                }

                // restore attachments
                File attachments = new File(tempDir, "attachments");
                if (attachments.exists()) {

                    String attachmentPath = AppConfig.getAttachmentPath();

                    if (attachmentPath == null) {
                        Platform.runLater(() -> {
                            table.setDisable(false);
                            AlertUtil.showError("Error", "Attachment folder not configured.");
                        });
                        return;
                    }

                    File target = new File(attachmentPath, "transactions");

                    // remove old images
                    if (target.exists()) {
                        FileUtil.deleteDirectory(target);
                    }

                    // restore images
                    FileUtil.copyDirectory(attachments, target);
                }

                FileUtil.deleteDirectory(tempDir);

                Platform.runLater(() -> {

                    table.setDisable(false);

                    DBConnection.initializeDatabase();

                    reload.run();

                    AlertUtil.showInfo("Success", "Database restored successfully.");

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