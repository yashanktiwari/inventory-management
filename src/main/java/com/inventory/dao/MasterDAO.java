package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.master.*;

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

    public List<PlantMaster> getAllPlants() {

        List<PlantMaster> list = new ArrayList<>();

        String sql = "SELECT plant_name FROM master_plants";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {
                list.add(new PlantMaster(
                        rs.getString("plant_name")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<DepartmentMaster> getAllDepartments() {

        List<DepartmentMaster> list = new ArrayList<>();

        String sql = "SELECT department_name FROM master_departments";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {
                list.add(new DepartmentMaster(
                        rs.getString("department_name")
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
        (item_code, item_name)
        VALUES (?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemCode());
            ps.setString(2, item.getItemName());

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

    public void addPlant(String plant) {

        String sql = """
        INSERT INTO master_plants
        (plant_name)
        VALUES (?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plant);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePlant(String plant) {

        String sql = "DELETE FROM master_plants WHERE plant_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plant);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDepartment(String department) {

        String sql = """
        INSERT INTO master_departments
        (department_name)
        VALUES (?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, department);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteDepartment(String department) {

        String sql = "DELETE FROM master_departments WHERE department_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, department);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int bulkInsertItems(List<ItemMaster> items) {

        if (items == null || items.isEmpty()) {
            return 0;
        }

        String sql = """
            INSERT IGNORE INTO master_items
            (item_code, item_name)
            VALUES (?, ?)
        """;

        int count = 0;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(sql);

            for (ItemMaster item : items) {
                ps.setString(1, item.getItemCode());
                ps.setString(2, item.getItemName());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result > 0 || result == Statement.SUCCESS_NO_INFO) count++;
            }

            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Failed to import items: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return count;
    }

    public int bulkInsertEmployees(List<EmployeeMaster> employees) {

        if (employees == null || employees.isEmpty()) {
            return 0;
        }

        String sql = """
        INSERT IGNORE INTO master_employees
        (employee_code, employee_name)
        VALUES (?, ?)
        """;

        int count = 0;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(sql);

            for (EmployeeMaster emp : employees) {
                ps.setString(1, emp.getEmployeeCode());
                ps.setString(2, emp.getEmployeeName());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result > 0 || result == Statement.SUCCESS_NO_INFO) count++;
            }

            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Failed to import employees: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return count;
    }

    public int bulkInsertCategories(List<String> categories) {

        if (categories == null || categories.isEmpty()) {
            return 0;
        }

        String sql = """
        INSERT IGNORE INTO master_plants
        (plant_name)
        VALUES (?)
        """;

        int count = 0;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(sql);

            for (String category : categories) {
                ps.setString(1, category);
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result > 0 || result == Statement.SUCCESS_NO_INFO) count++;
            }

            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Failed to import categories: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return count;
    }

    public int bulkInsertPlants(List<String> plants) {

        if (plants == null || plants.isEmpty()) {
            return 0;
        }

        String sql = """
        INSERT IGNORE INTO master_departments
        (department_name)
        VALUES (?)
        """;

        int count = 0;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(sql);

            for (String plant : plants) {
                ps.setString(1, plant);
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result > 0 || result == Statement.SUCCESS_NO_INFO) count++;
            }

            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Failed to import plants: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return count;
    }

    public int bulkInsertDepartments(List<String> departments) {

        if (departments == null || departments.isEmpty()) {
            return 0;
        }

        String sql = """
        INSERT OR IGNORE INTO master_departments
        (department_name)
        VALUES (?)
        """;

        int count = 0;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(sql);

            for (String department : departments) {
                ps.setString(1, department);
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result > 0 || result == Statement.SUCCESS_NO_INFO) count++;
            }

            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Failed to import departments: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return count;
    }
}
