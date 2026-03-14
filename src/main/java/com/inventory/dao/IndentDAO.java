package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.Indent;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class IndentDAO {

    public void insertIndent(String itemCode, String itemName, double quantity) {

        if (indentExists(itemCode)) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Indent already exists for this item.");
            alert.show();

            return;
        }

        String sql = """
        INSERT INTO indents (item_code, item_name, quantity)
        VALUES (?, ?, ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemCode);
            ps.setString(2, itemName);
            ps.setDouble(3, quantity);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Indent> getAllIndents() {

        List<Indent> list = new ArrayList<>();

        String sql = "SELECT * FROM indents";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Indent i = new Indent();

                i.setId(rs.getInt("id"));
                i.setItemCode(rs.getString("item_code"));
                i.setItemName(rs.getString("item_name"));
                i.setQuantity(rs.getDouble("quantity"));

                list.add(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void deleteIndent(int id) {

        String sql = "DELETE FROM indents WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getIndentCount() {

        String sql = "SELECT COUNT(*) FROM indents";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean indentExists(String itemCode) {

        String sql = "SELECT COUNT(*) FROM indents WHERE item_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemCode);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}