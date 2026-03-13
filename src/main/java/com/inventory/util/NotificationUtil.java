package com.inventory.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class NotificationUtil {

    public static void showSuccess(String title, String message) {
        Platform.runLater(() ->
                Notifications.create()
                        .title(title)
                        .text(message)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.BOTTOM_RIGHT)
                        .showInformation()
        );
    }

    public static void showError(String title, String message) {
        Platform.runLater(() ->
                Notifications.create()
                        .title(title)
                        .text(message)
                        .hideAfter(Duration.seconds(8))
                        .position(Pos.BOTTOM_RIGHT)
                        .showError()
        );
    }

    public static void showWarning(String title, String message) {
        Platform.runLater(() ->
                Notifications.create()
                        .title(title)
                        .text(message)
                        .hideAfter(Duration.seconds(6))
                        .position(Pos.BOTTOM_RIGHT)
                        .showWarning()
        );
    }

    // ✅ Add this method back
    public static void showLowStockNotification(
            String itemName,
            double currentStock,
            double minimumStock
    ) {

        String message =
                "Item: " + itemName +
                        "\nCurrent Stock: " + currentStock +
                        "\nMinimum Required: " + minimumStock;

        Platform.runLater(() ->
                Notifications.create()
                        .title("⚠ Low Stock Alert")
                        .text(message)
                        .hideAfter(Duration.seconds(6))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle()
                        .showWarning()
        );
    }
}
