package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public Item saveIfNotExists(String itemName, String description) {

        Item existing = findByName(itemName);
        if (existing != null) {
            return existing;
        }

        String sql = """
            INSERT INTO items(item_name, description)
            VALUES (?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, itemName);
            pstmt.setString(2, description);
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return new Item(keys.getString(1), itemName, description);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Item findByName(String itemName) {

        String sql = "SELECT * FROM items WHERE LOWER(item_name) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemName);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Item(
                        rs.getString("item_id"),
                        rs.getString("item_name"),
                        rs.getString("description")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Item findById(String itemId) {

        String sql = "SELECT * FROM items WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Item(
                        rs.getString("item_id"),
                        rs.getString("item_name"),
                        rs.getString("description")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Item item = new Item(
                        rs.getString("item_id"),
                        rs.getString("item_name"),
                        rs.getString("description")
                );
                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public Item createItemWithId(String itemId, String itemName) {

        String sql = "INSERT INTO items(item_id, item_name) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            pstmt.setString(2, itemName);
            pstmt.executeUpdate();

            return new Item(itemId, itemName, "");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}