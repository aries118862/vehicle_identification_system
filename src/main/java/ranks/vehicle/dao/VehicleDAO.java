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

public class VehicleDAO {
    private final DatabaseManager db;
    public VehicleDAO(DatabaseManager db) { this.db = db; }

    public int countVehicles() {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM vehicle"); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ObservableList<Vehicle> getAllVehicles() {
        ObservableList<Vehicle> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vw_vehicle_full_details ORDER BY vehicle_id";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("owner_name") == null ? "No Owner" : rs.getString("owner_name"),
                        rs.getString("status")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Vehicles query failed: " + e.getMessage(), e);
        }
    }

    public void addVehicle(String regNo, String make, String model, int year, int ownerId, String status) {
        String sql = "CALL add_vehicle_proc(?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo);
            ps.setString(2, make);
            ps.setString(3, model);
            ps.setInt(4, year);
            ps.setInt(5, ownerId);
            ps.setString(6, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Add vehicle failed: " + e.getMessage(), e);
        }
    }

    public void deleteVehicle(int vehicleId) {
        String sql = "DELETE FROM vehicle WHERE vehicle_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete vehicle failed: " + e.getMessage(), e);
        }
    }

    public void updateVehicleStatus(int vehicleId, String status) {
        String sql = "UPDATE vehicle SET status = ? WHERE vehicle_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, vehicleId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Vehicle not found.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Update vehicle status failed: " + e.getMessage(), e);
        }
    }

    public void updateVehicleStatusByRegistration(String registrationNumber, String status) {
        String sql = "UPDATE vehicle SET status = ? WHERE registration_number = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, registrationNumber);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("No vehicle found with registration number: " + registrationNumber);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Update vehicle status by registration failed: " + e.getMessage(), e);
        }
    }

    public Vehicle searchVehicle(String searchText) {
        String sql = """
                SELECT * FROM vw_vehicle_full_details
                WHERE LOWER(registration_number) LIKE LOWER(?)
                   OR LOWER(make) LIKE LOWER(?)
                   OR LOWER(model) LIKE LOWER(?)
                   OR LOWER(COALESCE(owner_name, '')) LIKE LOWER(?)
                LIMIT 1
                """;
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String value = "%" + searchText + "%";
            ps.setString(1, value);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.setString(4, value);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("owner_name") == null ? "No Owner" : rs.getString("owner_name"),
                        rs.getString("status")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Search vehicle failed: " + e.getMessage(), e);
        }
    }

    public Vehicle getVehicleByCustomerId(int customerId) {
        String sql = "SELECT * FROM vw_vehicle_full_details WHERE customer_id = ? LIMIT 1";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("owner_name") == null ? "No Owner" : rs.getString("owner_name"),
                        rs.getString("status")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Get vehicle by customer failed: " + e.getMessage(), e);
        }
    }
}
