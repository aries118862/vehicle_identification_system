package ranks.vehicle.model;

import java.time.LocalDate;

public class ServiceRecordModel {
    private int serviceId;
    private String registrationNumber;
    private LocalDate serviceDate;
    private String serviceType;
    private String description;
    private double cost;

    public ServiceRecordModel(int serviceId, String registrationNumber, LocalDate serviceDate, String serviceType, String description, double cost) {
        this.serviceId = serviceId;
        this.registrationNumber = registrationNumber;
        this.serviceDate = serviceDate;
        this.serviceType = serviceType;
        this.description = description;
        this.cost = cost;
    }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}
