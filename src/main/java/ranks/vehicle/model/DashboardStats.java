package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DashboardStats {
    public int vehicleCount;
    public int customerCount;
    public int reportCount;
    public int policyCount;

    public DashboardStats(int vehicleCount, int customerCount, int reportCount, int policyCount) {
        this.vehicleCount = vehicleCount;
        this.customerCount = customerCount;
        this.reportCount = reportCount;
        this.policyCount = policyCount;
    }
}
