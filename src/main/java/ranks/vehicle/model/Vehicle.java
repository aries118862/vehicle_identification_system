package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Vehicle {
    private final int vehicleId;
    private final String registrationNumber;
    private final String make;
    private final String model;
    private final int year;
    private final String ownerName;
    private final String status;

    public Vehicle(int vehicleId, String registrationNumber, String make, String model, int year, String ownerName, String status) {
        this.vehicleId = vehicleId;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.ownerName = ownerName;
        this.status = status;
    }

    public int getVehicleId() { return vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getOwnerName() { return ownerName; }
    public String getStatus() { return status; }
}
