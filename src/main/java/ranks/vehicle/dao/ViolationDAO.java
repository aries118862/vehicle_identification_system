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

public class ViolationDAO {
    private final DatabaseManager db;
    public ViolationDAO(DatabaseManager db) { this.db = db; }

    public void addViolation(int vehicleId, String violationType, double fineAmount, String status) {
        String sql = "INSERT INTO violation (vehicle_id, violation_date, violation_type, fine_amount, status) VALUES (?, CURRENT_DATE, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setString(2, violationType);
            ps.setDouble(3, fineAmount);
            ps.setString(4, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Add violation failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getViolationStatusCounts() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS total FROM violation GROUP BY status ORDER BY status";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString("status"), rs.getInt("total"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load violation analytics: " + e.getMessage(), e);
        }

        return data;
    }

    public ObservableList<ViolationModel> getAllViolations() {
        ObservableList<ViolationModel> list = FXCollections.observableArrayList();
        String sql = "SELECT vi.violation_id, v.registration_number, vi.violation_date, vi.violation_type, vi.fine_amount, vi.status FROM violation vi JOIN vehicle v ON vi.vehicle_id = v.vehicle_id ORDER BY vi.violation_id DESC";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ViolationModel(
                        rs.getInt("violation_id"),
                        rs.getString("registration_number"),
                        rs.getDate("violation_date").toLocalDate(),
                        rs.getString("violation_type"),
                        rs.getDouble("fine_amount"),
                        rs.getString("status")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Violations query failed: " + e.getMessage(), e);
        }
    }
}
