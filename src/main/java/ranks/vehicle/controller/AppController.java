package ranks.vehicle.controller;

import javafx.collections.ObservableList;
import ranks.vehicle.dao.*;
import ranks.vehicle.db.DatabaseManager;
import ranks.vehicle.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AppController {
    private final DatabaseManager db = new DatabaseManager();
    private final UserDAO userDAO = new UserDAO(db);
    private final CustomerDAO customerDAO = new CustomerDAO(db);
    private final VehicleDAO vehicleDAO = new VehicleDAO(db);
    private final ServiceRecordDAO serviceDAO = new ServiceRecordDAO(db);
    private final CustomerQueryDAO queryDAO = new CustomerQueryDAO(db);
    private final PoliceReportDAO reportDAO = new PoliceReportDAO(db);
    private final ViolationDAO violationDAO = new ViolationDAO(db);
    private final InsurancePolicyDAO policyDAO = new InsurancePolicyDAO(db);

    public void initializeDatabase() {
        db.initializeDatabase();
    }

    public User login(String username, String password, String selectedRole) {
        String roleKey = selectedRole.equalsIgnoreCase("Administrator") ? "admin" : selectedRole.toLowerCase();
        return userDAO.login(username.trim(), password.trim(), roleKey);
    }

    public DashboardStats getStats() {
        return new DashboardStats(vehicleDAO.countVehicles(), customerDAO.countCustomers(), reportDAO.countReports(), policyDAO.countPolicies());
    }

    public List<String> getRecentActivities() {
        return db.getRecentActivities();
    }

    public Map<String, Integer> getViolationStatusCounts() {
        return violationDAO.getViolationStatusCounts();
    }

    public Map<String, Integer> getServiceTypeCounts() {
        return serviceDAO.getServiceTypeCounts();
    }

    public Map<String, Double> getMonthlyRevenue() {
        return serviceDAO.getMonthlyRevenue();
    }

    public ObservableList<Vehicle> getVehicles() { return vehicleDAO.getAllVehicles(); }
    public ObservableList<Customer> getCustomers() { return customerDAO.getAllCustomers(); }
    public ObservableList<ServiceRecordModel> getServiceRecords() { return serviceDAO.getAllServiceRecords(); }
    public ObservableList<CustomerQueryModel> getQueriesForCustomer(int customerId) { return queryDAO.getQueriesForCustomer(customerId); }
    public ObservableList<PoliceReportModel> getReports() { return reportDAO.getAllReports(); }
    public ObservableList<ViolationModel> getViolations() { return violationDAO.getAllViolations(); }
    public ObservableList<InsurancePolicyModel> getPolicies() { return policyDAO.getAllPolicies(); }

    public ObservableList<ServiceRecordModel> getServiceHistoryForCustomer(int customerId) {
        return serviceDAO.getServiceHistoryForCustomer(customerId);
    }

    public Vehicle searchVehicle(String text) {
        return vehicleDAO.searchVehicle(text);
    }

    public Vehicle getVehicleByCustomerId(int customerId) {
        return vehicleDAO.getVehicleByCustomerId(customerId);
    }

    public void submitCustomerTheftReport(int customerId, int vehicleId, String description) {
        if (description == null || description.isBlank()) {
            throw new RuntimeException("Please enter theft report details.");
        }
        reportDAO.addCustomerTheftReport(customerId, vehicleId, description);
        vehicleDAO.updateVehicleStatus(vehicleId, "Flagged");
    }

    public void updateVehicleStatus(int vehicleId, String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Please choose a valid vehicle status.");
        }
        vehicleDAO.updateVehicleStatus(vehicleId, status);
    }

    public void updateVehicleStatusByRegistration(String registrationNumber, String status) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new RuntimeException("Please enter registration number.");
        }
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Please choose a valid vehicle status.");
        }
        vehicleDAO.updateVehicleStatusByRegistration(registrationNumber.trim(), status);
    }

    public void addVehicle(String regNo, String make, String model, int year, int ownerId, String status) {
        if (regNo == null || regNo.isBlank() || make == null || make.isBlank() || model == null || model.isBlank() || status == null) {
            throw new RuntimeException("Complete all vehicle fields.");
        }
        vehicleDAO.addVehicle(regNo, make, model, year, ownerId, status);
    }

    public void deleteVehicle(int vehicleId) {
        vehicleDAO.deleteVehicle(vehicleId);
    }

    public void addServiceRecord(int vehicleId, LocalDate serviceDate, String serviceType, String description, double cost) {
        if (serviceType == null || serviceType.isBlank()) throw new RuntimeException("Service type is required.");
        serviceDAO.addServiceRecord(vehicleId, serviceDate, serviceType, description, cost);
    }

    public void addCustomerQuery(int customerId, int vehicleId, String queryText) {
        if (queryText == null || queryText.isBlank()) throw new RuntimeException("Query text is required.");
        queryDAO.addCustomerQuery(customerId, vehicleId, queryText);
    }

    public void addPoliceReport(int vehicleId, String reportType, String description, String officerName) {
        if (reportType == null || reportType.isBlank() || officerName == null || officerName.isBlank()) {
            throw new RuntimeException("Report type and officer name are required.");
        }
        reportDAO.addPoliceReport(vehicleId, reportType, description, officerName);
    }

    public void addViolation(int vehicleId, String violationType, double fineAmount, String status) {
        if (violationType == null || violationType.isBlank() || status == null) {
            throw new RuntimeException("Complete all violation fields.");
        }
        violationDAO.addViolation(vehicleId, violationType, fineAmount, status);
    }

    public void addPolicy(int vehicleId, String policyNumber, LocalDate expiryDate, String status) {
        if (policyNumber == null || policyNumber.isBlank() || status == null) {
            throw new RuntimeException("Complete all policy fields.");
        }
        policyDAO.addPolicy(vehicleId, policyNumber, expiryDate, status);
    }

    public void registerCustomer(String name, String address, String phone, String email, String username, String password) {
        if (name == null || name.isBlank() ||
                address == null || address.isBlank() ||
                phone == null || phone.isBlank() ||
                email == null || email.isBlank() ||
                username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            throw new RuntimeException("All registration fields are required.");
        }

        int customerId = customerDAO.addCustomer(name.trim(), address.trim(), phone.trim(), email.trim());
        userDAO.createCustomerUser(username.trim(), password.trim(), customerId);
    }

    public double getTotalServiceRevenue() {
        return serviceDAO.getTotalRevenue();
    }
}
