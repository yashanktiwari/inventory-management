package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.master.CategoryMaster;
import com.inventory.model.master.EmployeeMaster;
import com.inventory.model.master.ItemMaster;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MasterDAO {

    public List<ItemMaster> getAllItems() {

        List<ItemMaster> list = new ArrayList<>();

        String sql = "SELECT * FROM master_items";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                list.add(new ItemMaster(
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getString("item_make"),
                        rs.getString("item_model"),
                        rs.getString("item_category")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<EmployeeMaster> getAllEmployees() {

        List<EmployeeMaster> list = new ArrayList<>();

        String sql = "SELECT * FROM master_employees";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                list.add(new EmployeeMaster(
                        rs.getString("employee_code"),
                        rs.getString("employee_name")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<CategoryMaster> getAllCategories() {

        List<CategoryMaster> list = new ArrayList<>();

        String sql = "SELECT category_name FROM master_categories";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {
                list.add(new CategoryMaster(
                        rs.getString("category_name")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void addItem(ItemMaster item) {

        String sql = """
        INSERT INTO master_items
        (item_code, item_name, item_make, item_model, item_category)
        VALUES (?, ?, ?, ?, ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemCode());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getItemMake());
            ps.setString(4, item.getItemModel());
            ps.setString(5, item.getItemCategory());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(String code) {

        String sql = "DELETE FROM master_items WHERE item_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEmployee(EmployeeMaster emp) {

        String sql = """
        INSERT INTO master_employees
        (employee_code, employee_name)
        VALUES (?, ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emp.getEmployeeCode());
            ps.setString(2, emp.getEmployeeName());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteEmployee(String code) {

        String sql = "DELETE FROM master_employees WHERE employee_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCategory(String category) {

        String sql = """
        INSERT INTO master_categories
        (category_name)
        VALUES (?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteCategory(String category) {

        String sql = "DELETE FROM master_categories WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
