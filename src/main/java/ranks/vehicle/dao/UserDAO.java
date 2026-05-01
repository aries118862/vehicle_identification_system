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

public class UserDAO {
    private final DatabaseManager db;
    public UserDAO(DatabaseManager db) { this.db = db; }

    public User login(String username, String password, String role) {
        String sql = "SELECT * FROM app_user WHERE username = ? AND password = ? AND role = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        (Integer) rs.getObject("customer_id")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Login query failed: " + e.getMessage(), e);
        }
    }

    public void createCustomerUser(String username, String password, int customerId) {
        String sql = "INSERT INTO app_user (username, password, role, customer_id) VALUES (?, ?, 'customer', ?)";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setInt(3, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("username")) {
                throw new RuntimeException("Username already exists. Choose another username.");
            }
            throw new RuntimeException("User account creation failed: " + e.getMessage(), e);
        }
    }
}
