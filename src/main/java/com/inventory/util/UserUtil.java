package com.inventory.util;

public class UserUtil {

    public static String getCurrentUser() {
        return System.getProperty("user.name").toUpperCase();
    }

}