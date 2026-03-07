package com.inventory.database;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ConnectionState {

    private static final BooleanProperty connected = new SimpleBooleanProperty(false);

    public static BooleanProperty connectedProperty() {
        return connected;
    }

    public static boolean isConnected() {
        return connected.get();
    }

    public static void setConnected(boolean value) {
        connected.set(value);
    }
}