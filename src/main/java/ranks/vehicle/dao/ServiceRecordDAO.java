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

public class ServiceRecordDAO {
    private final DatabaseManager db;
    public ServiceRecordDAO(DatabaseManager db) { this.db = db; }

    public ObservableList<ServiceRecordModel> getAllServiceRecords() {
        ObservableList<ServiceRecordModel> list = FXCollections.observableArrayList();
        String sql = "SELECT sr.service_id, v.registration_number, sr.service_date, sr.service_type, sr.description, sr.cost FROM service_record sr JOIN vehicle v ON sr.vehicle_id = v.vehicle_id ORDER BY sr.service_id DESC";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ServiceRecordModel(
                        rs.getInt("service_id"),
                        rs.getString("registration_number"),
                        rs.getDate("service_date").toLocalDate(),
                        rs.getString("service_type"),
                        rs.getString("description"),
                        rs.getDouble("cost")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Service records query failed: " + e.getMessage(), e);
        }
    }

    public ObservableList<ServiceRecordModel> getServiceHistoryForCustomer(int customerId) {
        ObservableList<ServiceRecordModel> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vw_service_history WHERE owner_id = ? ORDER BY service_id DESC";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ServiceRecordModel(
                        rs.getInt("service_id"),
                        rs.getString("registration_number"),
                        rs.getDate("service_date").toLocalDate(),
                        rs.getString("service_type"),
                        rs.getString("description"),
                        rs.getDouble("cost")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Customer service history failed: " + e.getMessage(), e);
        }
    }

    public void addServiceRecord(int vehicleId, LocalDate serviceDate, String serviceType, String description, double cost) {
        String sql = "CALL add_service_record_proc(?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setDate(2, Date.valueOf(serviceDate));
            ps.setString(3, serviceType);
            ps.setString(4, description);
            ps.setDouble(5, cost);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Add service record failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getServiceTypeCounts() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = """
                SELECT service_type, COUNT(*) AS total
                FROM service_record
                GROUP BY service_type
                ORDER BY total DESC
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString("service_type"), rs.getInt("total"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load service type analytics: " + e.getMessage(), e);
        }

        return data;
    }

    public Map<String, Double> getMonthlyRevenue() {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = """
                SELECT TO_CHAR(service_date, 'YYYY-MM') AS month_label,
                       COALESCE(SUM(cost), 0) AS total_revenue
                FROM service_record
                GROUP BY TO_CHAR(service_date, 'YYYY-MM')
                ORDER BY TO_CHAR(service_date, 'YYYY-MM')
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.put(rs.getString("month_label"), rs.getDouble("total_revenue"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load monthly revenue analytics: " + e.getMessage(), e);
        }

        return data;
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(cost), 0) FROM service_record";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
