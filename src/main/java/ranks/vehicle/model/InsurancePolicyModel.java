package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InsurancePolicyModel {
    public int policyId;
    public String registrationNumber;
    public String policyNumber;
    public LocalDate expiryDate;
    public String status;

    public InsurancePolicyModel(int policyId, String registrationNumber, String policyNumber, LocalDate expiryDate, String status) {
        this.policyId = policyId;
        this.registrationNumber = registrationNumber;
        this.policyNumber = policyNumber;
        this.expiryDate = expiryDate;
        this.status = status;
    }
}
