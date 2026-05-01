package ranks.vehicle.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ranks.vehicle.db.DatabaseManager;
import ranks.vehicle.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerDAO {
    private final DatabaseManager db;
    public CustomerDAO(DatabaseManager db) { this.db = db; }

    public int countCustomers() {
        return count("SELECT COUNT(*) FROM customer");
    }

    public ObservableList<Customer> getAllCustomers() {
        ObservableList<Customer> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM customer ORDER BY customer_id";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("email")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Customers query failed: " + e.getMessage(), e);
        }
    }

    public int addCustomer(String name, String address, String phone, String email) {
        String sql = "INSERT INTO customer (name, address, phone, email) VALUES (?, ?, ?, ?) RETURNING customer_id";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, phone);
            ps.setString(4, email);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
            throw new RuntimeException("Failed to create customer record.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("email")) {
                throw new RuntimeException("This email is already registered.");
            }
            throw new RuntimeException("Customer registration failed: " + e.getMessage(), e);
        }
    }

    private int count(String sql) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
