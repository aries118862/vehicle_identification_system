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

public class InsurancePolicyDAO {
    private final DatabaseManager db;
    public InsurancePolicyDAO(DatabaseManager db) { this.db = db; }

    public int countPolicies() {
        String sql = "SELECT COUNT(*) FROM insurance_policy";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPolicy(int vehicleId, String policyNumber, LocalDate expiryDate, String status) {
        String sql = "INSERT INTO insurance_policy (vehicle_id, policy_number, expiry_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setString(2, policyNumber);
            ps.setDate(3, Date.valueOf(expiryDate));
            ps.setString(4, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Add policy failed: " + e.getMessage(), e);
        }
    }

    public ObservableList<InsurancePolicyModel> getAllPolicies() {
        ObservableList<InsurancePolicyModel> list = FXCollections.observableArrayList();
        String sql = "SELECT ip.policy_id, v.registration_number, ip.policy_number, ip.expiry_date, ip.status FROM insurance_policy ip JOIN vehicle v ON ip.vehicle_id = v.vehicle_id ORDER BY ip.policy_id DESC";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new InsurancePolicyModel(
                        rs.getInt("policy_id"),
                        rs.getString("registration_number"),
                        rs.getString("policy_number"),
                        rs.getDate("expiry_date").toLocalDate(),
                        rs.getString("status")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Policies query failed: " + e.getMessage(), e);
        }
    }
}
