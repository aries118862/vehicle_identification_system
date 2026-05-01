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

public class PoliceReportDAO {
    private final DatabaseManager db;
    public PoliceReportDAO(DatabaseManager db) { this.db = db; }

    public int countReports() {
        String sql = "SELECT COUNT(*) FROM police_report";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPoliceReport(int vehicleId, String reportType, String description, String officerName) {
        String sql = "CALL add_police_report_proc(?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setString(2, reportType);
            ps.setString(3, description);
            ps.setString(4, officerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Add police report failed: " + e.getMessage(), e);
        }
    }

    public void addCustomerTheftReport(int customerId, int vehicleId, String description) {
        String customerNameSql = "SELECT name FROM customer WHERE customer_id = ?";
        String insertSql = "INSERT INTO police_report (vehicle_id, report_date, report_type, description, officer_name) VALUES (?, CURRENT_DATE, 'Theft', ?, ?)";

        try (Connection conn = db.getConnection()) {
            String reporterName = "Customer Report";
            try (PreparedStatement ps = conn.prepareStatement(customerNameSql)) {
                ps.setInt(1, customerId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    reporterName = "Customer: " + rs.getString("name");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, vehicleId);
                ps.setString(2, description);
                ps.setString(3, reporterName);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Add customer theft report failed: " + e.getMessage(), e);
        }
    }

    public ObservableList<PoliceReportModel> getAllReports() {
        ObservableList<PoliceReportModel> list = FXCollections.observableArrayList();
        String sql = "SELECT pr.report_id, v.registration_number, pr.report_date, pr.report_type, pr.description, pr.officer_name FROM police_report pr JOIN vehicle v ON pr.vehicle_id = v.vehicle_id ORDER BY pr.report_id DESC";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PoliceReportModel(
                        rs.getInt("report_id"),
                        rs.getString("registration_number"),
                        rs.getDate("report_date").toLocalDate(),
                        rs.getString("report_type"),
                        rs.getString("description"),
                        rs.getString("officer_name")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Reports query failed: " + e.getMessage(), e);
        }
    }
}
