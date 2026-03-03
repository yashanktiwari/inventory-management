package com.inventory.dao;

import com.inventory.database.DBConnection;
import com.inventory.model.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonDAO {

    // 🔹 Save person if not already existing (based on employee_id)
    public Person saveIfNotExists(String employeeId,
                                  String personName,
                                  String department) {

        Person existing = findByEmployeeId(employeeId);
        if (existing != null) {
            return existing;
        }

        String sql = """
                INSERT INTO persons(employee_id, person_name, department)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, employeeId);
            pstmt.setString(2, personName);
            pstmt.setString(3, department);

            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return new Person(
                        keys.getInt(1),
                        employeeId,
                        personName,
                        department
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 🔹 Find person by employee ID
    public Person findByEmployeeId(String employeeId) {

        String sql = "SELECT * FROM persons WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employeeId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Person(
                        rs.getInt("person_id"),
                        rs.getString("employee_id"),
                        rs.getString("person_name"),
                        rs.getString("department")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 🔹 Get all persons
    public List<Person> getAllPersons() {

        List<Person> persons = new ArrayList<>();

        String sql = "SELECT * FROM persons ORDER BY employee_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                persons.add(
                        new Person(
                                rs.getInt("person_id"),
                                rs.getString("employee_id"),
                                rs.getString("person_name"),
                                rs.getString("department")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return persons;
    }

    // 🔹 Update person details
    public void updatePerson(int personId,
                             String employeeId,
                             String personName,
                             String department) {

        String sql = """
                UPDATE persons
                SET employee_id = ?, person_name = ?, department = ?
                WHERE person_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employeeId);
            pstmt.setString(2, personName);
            pstmt.setString(3, department);
            pstmt.setInt(4, personId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Delete person
    public void deletePerson(int personId) {

        String sql = "DELETE FROM persons WHERE person_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, personId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}