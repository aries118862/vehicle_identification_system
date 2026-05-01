package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PoliceReportModel {
    public int reportId;
    public String registrationNumber;
    public LocalDate reportDate;
    public String reportType;
    public String description;
    public String officerName;

    public PoliceReportModel(int reportId, String registrationNumber, LocalDate reportDate, String reportType, String description, String officerName) {
        this.reportId = reportId;
        this.registrationNumber = registrationNumber;
        this.reportDate = reportDate;
        this.reportType = reportType;
        this.description = description;
        this.officerName = officerName;
    }
}
