package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ViolationModel {
    public int violationId;
    public String registrationNumber;
    public LocalDate violationDate;
    public String violationType;
    public double fineAmount;
    public String status;

    public ViolationModel(int violationId, String registrationNumber, LocalDate violationDate, String violationType, double fineAmount, String status) {
        this.violationId = violationId;
        this.registrationNumber = registrationNumber;
        this.violationDate = violationDate;
        this.violationType = violationType;
        this.fineAmount = fineAmount;
        this.status = status;
    }
}
