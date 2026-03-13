package com.inventory.util;

import javafx.application.Platform;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class NotificationUtil {

    private static TrayIcon trayIcon;
    private static boolean initialized = false;

    private static final Set<String> notifiedItems = new HashSet<>();

    private static void initTray() {

        if (initialized) return;

        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray not supported");
            return;
        }

        try {

            SystemTray tray = SystemTray.getSystemTray();

            // 🔹 Load your application icon
            Image image = Toolkit.getDefaultToolkit()
                    .getImage(NotificationUtil.class.getResource("/icons/inventory_icon.png"));

            trayIcon = new TrayIcon(image, "Inventory Management");

            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);

            trayIcon.addActionListener(e -> openApplication());

            initialized = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showLowStockNotification(
            String itemName,
            double currentStock,
            double minimumStock
    ) {

        initTray();

        if (trayIcon == null) return;

        if (notifiedItems.contains(itemName)) return;

        notifiedItems.add(itemName);

        String message =
                itemName + " inventory is below minimum level.\n" +
                        "Stock: " + currentStock + "  |  Min: " + minimumStock;


        trayIcon.displayMessage(
                "Inventory Alert",
                message,
                TrayIcon.MessageType.WARNING
        );
    }

    public static void resetNotification(String itemName) {
        notifiedItems.remove(itemName);
    }

    private static void openApplication() {

        Platform.runLater(() -> {

            try {

                if (com.inventory.MainApp.getPrimaryStage() != null) {

                    com.inventory.MainApp.getPrimaryStage().show();
                    com.inventory.MainApp.getPrimaryStage().toFront();
                    com.inventory.MainApp.getPrimaryStage().requestFocus();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }
}