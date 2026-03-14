package com.inventory.service;

import com.inventory.database.ConnectionState;
import com.inventory.database.DBConnection;
import javafx.application.Platform;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DatabaseConnectionMonitor {

    private ScheduledExecutorService scheduler;
    private boolean lastConnectionState = false;

    public void start(Consumer<Boolean> uiCallback) {

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {

            boolean connected = isDatabaseReachable();

            ConnectionState.setConnected(connected);

            Platform.runLater(() -> {

                uiCallback.accept(connected);

                lastConnectionState = connected;
            });

        }, 0, 2, TimeUnit.SECONDS);
    }

    public void stop() {

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private boolean isDatabaseReachable() {

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(
                            DBConnection.getHost(),
                            Integer.parseInt(DBConnection.getPort())
                    ),
                    1000
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}