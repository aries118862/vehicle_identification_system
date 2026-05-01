package ranks.vehicle.db;

import java.sql.*;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5433/vehicle_identification_system";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public List<String> getRecentActivities() {
        List<String> activities = new java.util.ArrayList<>();

        String sql = """
                SELECT activity_text
                FROM (
                    SELECT 'Vehicle added: ' || registration_number || ' (' || make || ' ' || model || ')' AS activity_text,
                           vehicle_id AS sort_id
                    FROM vehicle

                    UNION ALL

                    SELECT 'Police report: ' || report_type || ' by ' || officer_name AS activity_text,
                           report_id AS sort_id
                    FROM police_report

                    UNION ALL

                    SELECT 'Service record: ' || service_type || ' for vehicle ID ' || vehicle_id AS activity_text,
                           service_id AS sort_id
                    FROM service_record

                    UNION ALL

                    SELECT 'Insurance policy created: ' || policy_number AS activity_text,
                           policy_id AS sort_id
                    FROM insurance_policy
                ) x
                ORDER BY sort_id DESC
                LIMIT 10
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                activities.add(rs.getString("activity_text"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load recent activities: " + e.getMessage(), e);
        }

        return activities;
    }

    public void initializeDatabase() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS customer (
                        customer_id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        address TEXT,
                        phone VARCHAR(20),
                        email VARCHAR(100) UNIQUE
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS vehicle (
                        vehicle_id SERIAL PRIMARY KEY,
                        registration_number VARCHAR(20) UNIQUE NOT NULL,
                        make VARCHAR(50) NOT NULL,
                        model VARCHAR(50) NOT NULL,
                        year INTEGER NOT NULL,
                        owner_id INTEGER REFERENCES customer(customer_id) ON DELETE SET NULL,
                        status VARCHAR(30) DEFAULT 'Valid'
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS service_record (
                        service_id SERIAL PRIMARY KEY,
                        vehicle_id INTEGER NOT NULL REFERENCES vehicle(vehicle_id) ON DELETE CASCADE,
                        service_date DATE NOT NULL,
                        service_type VARCHAR(100) NOT NULL,
                        description TEXT,
                        cost NUMERIC(10,2) DEFAULT 0
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS customer_query (
                        query_id SERIAL PRIMARY KEY,
                        customer_id INTEGER NOT NULL REFERENCES customer(customer_id) ON DELETE CASCADE,
                        vehicle_id INTEGER NOT NULL REFERENCES vehicle(vehicle_id) ON DELETE CASCADE,
                        query_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        query_text TEXT NOT NULL,
                        response_text TEXT
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS police_report (
                        report_id SERIAL PRIMARY KEY,
                        vehicle_id INTEGER NOT NULL REFERENCES vehicle(vehicle_id) ON DELETE CASCADE,
                        report_date DATE NOT NULL DEFAULT CURRENT_DATE,
                        report_type VARCHAR(50) NOT NULL,
                        description TEXT,
                        officer_name VARCHAR(100) NOT NULL
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS violation (
                        violation_id SERIAL PRIMARY KEY,
                        vehicle_id INTEGER NOT NULL REFERENCES vehicle(vehicle_id) ON DELETE CASCADE,
                        violation_date DATE NOT NULL DEFAULT CURRENT_DATE,
                        violation_type VARCHAR(100) NOT NULL,
                        fine_amount NUMERIC(10,2) DEFAULT 0,
                        status VARCHAR(20) CHECK (status IN ('Paid','Unpaid')) DEFAULT 'Unpaid'
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS insurance_policy (
                        policy_id SERIAL PRIMARY KEY,
                        vehicle_id INTEGER NOT NULL REFERENCES vehicle(vehicle_id) ON DELETE CASCADE,
                        policy_number VARCHAR(50) UNIQUE NOT NULL,
                        expiry_date DATE NOT NULL,
                        status VARCHAR(30) DEFAULT 'Active'
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS app_user (
                        user_id SERIAL PRIMARY KEY,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        password VARCHAR(100) NOT NULL,
                        role VARCHAR(30) NOT NULL,
                        customer_id INTEGER REFERENCES customer(customer_id) ON DELETE SET NULL
                    )
                    """);

            st.execute("""
                    CREATE OR REPLACE VIEW vw_vehicle_full_details AS
                    SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.status,
                           c.customer_id, c.name AS owner_name, c.phone, c.email
                    FROM vehicle v
                    LEFT JOIN customer c ON v.owner_id = c.customer_id
                    """);

            st.execute("""
                    CREATE OR REPLACE VIEW vw_service_history AS
                    SELECT sr.service_id, v.registration_number, sr.service_date, sr.service_type, sr.description, sr.cost, v.owner_id
                    FROM service_record sr
                    JOIN vehicle v ON sr.vehicle_id = v.vehicle_id
                    """);

            st.execute("""
                    CREATE OR REPLACE VIEW vw_unpaid_violations AS
                    SELECT violation_id, vehicle_id, violation_date, violation_type, fine_amount, status
                    FROM violation
                    WHERE status = 'Unpaid'
                    """);

            st.execute("""
                    CREATE OR REPLACE PROCEDURE add_vehicle_proc(
                        p_registration_number VARCHAR,
                        p_make VARCHAR,
                        p_model VARCHAR,
                        p_year INTEGER,
                        p_owner_id INTEGER,
                        p_status VARCHAR)
                    LANGUAGE plpgsql
                    AS $$
                    BEGIN
                        INSERT INTO vehicle (registration_number, make, model, year, owner_id, status)
                        VALUES (p_registration_number, p_make, p_model, p_year, p_owner_id, p_status);
                    END;
                    $$
                    """);

            st.execute("""
                    CREATE OR REPLACE PROCEDURE add_service_record_proc(
                        p_vehicle_id INTEGER,
                        p_service_date DATE,
                        p_service_type VARCHAR,
                        p_description TEXT,
                        p_cost NUMERIC)
                    LANGUAGE plpgsql
                    AS $$
                    BEGIN
                        INSERT INTO service_record (vehicle_id, service_date, service_type, description, cost)
                        VALUES (p_vehicle_id, p_service_date, p_service_type, p_description, p_cost);
                    END;
                    $$
                    """);

            st.execute("""
                    CREATE OR REPLACE PROCEDURE add_police_report_proc(
                        p_vehicle_id INTEGER,
                        p_report_type VARCHAR,
                        p_description TEXT,
                        p_officer_name VARCHAR)
                    LANGUAGE plpgsql
                    AS $$
                    BEGIN
                        INSERT INTO police_report (vehicle_id, report_date, report_type, description, officer_name)
                        VALUES (p_vehicle_id, CURRENT_DATE, p_report_type, p_description, p_officer_name);
                    END;
                    $$
                    """);

            insertDemoData();
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private void insertDemoData() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("""
                    INSERT INTO customer (name, address, phone, email) VALUES
                    ('Kabelo Selomo', 'Maseru, Lesotho', '58000001', 'kabelo@email.com'),
                    ('Lineo Mokoena', 'Teyateyaneng, Lesotho', '58000002', 'lineo@email.com'),
                    ('Thabo Nteso', 'Mafeteng, Lesotho', '58000003', 'thabo@email.com')
                    ON CONFLICT (email) DO NOTHING
                    """);

            st.executeUpdate("""
                    INSERT INTO vehicle (registration_number, make, model, year, owner_id, status) VALUES
                    ('B 4567', 'Toyota', 'Corolla', 2018, 1, 'Valid'),
                    ('C 1021', 'Ford', 'Ranger', 2021, 2, 'Service Due'),
                    ('A 8882', 'BMW', 'X3', 2020, 3, 'Flagged')
                    ON CONFLICT (registration_number) DO NOTHING
                    """);

            st.executeUpdate("""
                    INSERT INTO service_record (vehicle_id, service_date, service_type, description, cost)
                    SELECT 1, '2026-03-10', 'Oil Change', 'Engine oil changed', 850.00
                    WHERE NOT EXISTS (SELECT 1 FROM service_record WHERE vehicle_id = 1 AND service_type = 'Oil Change')
                    """);
            st.executeUpdate("""
                    INSERT INTO service_record (vehicle_id, service_date, service_type, description, cost)
                    SELECT 1, '2026-01-25', 'Brake Inspection', 'Brake pads checked', 450.00
                    WHERE NOT EXISTS (SELECT 1 FROM service_record WHERE vehicle_id = 1 AND service_type = 'Brake Inspection')
                    """);
            st.executeUpdate("""
                    INSERT INTO service_record (vehicle_id, service_date, service_type, description, cost)
                    SELECT 2, '2026-04-11', 'Tyre Rotation', 'Tyres rotated and balanced', 300.00
                    WHERE NOT EXISTS (SELECT 1 FROM service_record WHERE vehicle_id = 2 AND service_type = 'Tyre Rotation')
                    """);

            st.executeUpdate("""
                    INSERT INTO police_report (vehicle_id, report_date, report_type, description, officer_name)
                    SELECT 3, '2026-04-10', 'Theft', 'Vehicle linked to theft investigation', 'Officer Pitseng'
                    WHERE NOT EXISTS (SELECT 1 FROM police_report WHERE vehicle_id = 3 AND report_type = 'Theft')
                    """);
            st.executeUpdate("""
                    INSERT INTO police_report (vehicle_id, report_date, report_type, description, officer_name)
                    SELECT 2, '2026-04-11', 'Inspection', 'Routine inspection conducted', 'Officer Mpho'
                    WHERE NOT EXISTS (SELECT 1 FROM police_report WHERE vehicle_id = 2 AND report_type = 'Inspection')
                    """);

            st.executeUpdate("""
                    INSERT INTO violation (vehicle_id, violation_date, violation_type, fine_amount, status)
                    SELECT 2, '2026-04-12', 'Speeding', 500.00, 'Unpaid'
                    WHERE NOT EXISTS (SELECT 1 FROM violation WHERE vehicle_id = 2 AND violation_type = 'Speeding')
                    """);
            st.executeUpdate("""
                    INSERT INTO violation (vehicle_id, violation_date, violation_type, fine_amount, status)
                    SELECT 3, '2026-04-12', 'Unregistered Modification', 1200.00, 'Paid'
                    WHERE NOT EXISTS (SELECT 1 FROM violation WHERE vehicle_id = 3 AND violation_type = 'Unregistered Modification')
                    """);

            st.executeUpdate("""
                    INSERT INTO insurance_policy (vehicle_id, policy_number, expiry_date, status)
                    VALUES (1, 'POL-1001', '2026-12-12', 'Active')
                    ON CONFLICT (policy_number) DO NOTHING
                    """);
            st.executeUpdate("""
                    INSERT INTO insurance_policy (vehicle_id, policy_number, expiry_date, status)
                    VALUES (2, 'POL-1002', '2026-11-09', 'Pending')
                    ON CONFLICT (policy_number) DO NOTHING
                    """);
            st.executeUpdate("""
                    INSERT INTO insurance_policy (vehicle_id, policy_number, expiry_date, status)
                    VALUES (3, 'POL-1003', '2026-08-17', 'Claim Review')
                    ON CONFLICT (policy_number) DO NOTHING
                    """);

            st.executeUpdate("""
                    INSERT INTO customer_query (customer_id, vehicle_id, query_text, response_text)
                    SELECT 1, 1, 'My vehicle makes noise when braking', 'Please visit the workshop for inspection'
                    WHERE NOT EXISTS (SELECT 1 FROM customer_query WHERE customer_id = 1 AND vehicle_id = 1)
                    """);

            st.executeUpdate("""
                    INSERT INTO app_user (username, password, role, customer_id)
                    VALUES ('admin', '123', 'admin', NULL)
                    ON CONFLICT (username) DO NOTHING
                    """);
            st.executeUpdate("""
                    INSERT INTO app_user (username, password, role, customer_id)
                    VALUES ('kabelo', '123', 'customer', 1)
                    ON CONFLICT (username) DO NOTHING
                    """);
            st.executeUpdate("""
                    INSERT INTO app_user (username, password, role, customer_id)
                    VALUES ('police1', '123', 'police', NULL)
                    ON CONFLICT (username) DO NOTHING
                    """);
            st.executeUpdate("""
                    INSERT INTO app_user (username, password, role, customer_id)
                    VALUES ('workshop1', '123', 'workshop', NULL)
                    ON CONFLICT (username) DO NOTHING
                    """);
            st.executeUpdate("""
                    INSERT INTO app_user (username, password, role, customer_id)
                    VALUES ('insurance1', '123', 'insurance', NULL)
                    ON CONFLICT (username) DO NOTHING
                    """);
        } catch (SQLException e) {
            throw new RuntimeException("Demo data insertion failed: " + e.getMessage(), e);
        }
    }
}
