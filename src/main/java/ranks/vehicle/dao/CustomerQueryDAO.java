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

public class CustomerQueryDAO {
    private final DatabaseManager db;
    public CustomerQueryDAO(DatabaseManager db) { this.db = db; }

    public void addCustomerQuery(int customerId, int vehicleId, String queryText) {
        String sql = "INSERT INTO customer_query (customer_id, vehicle_id, query_text, response_text) VALUES (?, ?, ?, 'Pending response')";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, vehicleId);
            ps.setString(3, queryText);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Add customer query failed: " + e.getMessage(), e);
        }
    }

    public ObservableList<CustomerQueryModel> getQueriesForCustomer(int customerId) {
        ObservableList<CustomerQueryModel> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM customer_query WHERE customer_id = ? ORDER BY query_id DESC";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("query_date");
                list.add(new CustomerQueryModel(
                        rs.getInt("query_id"),
                        ts == null ? LocalDateTime.now() : ts.toLocalDateTime(),
                        rs.getString("query_text"),
                        rs.getString("response_text") == null ? "Pending" : rs.getString("response_text")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Get customer queries failed: " + e.getMessage(), e);
        }
    }
}
