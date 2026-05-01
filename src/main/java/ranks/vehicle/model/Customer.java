package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer extends BasePerson {
    public int customerId;
    public String address;
    public String phone;
    public String email;

    public Customer(int customerId, String name, String address, String phone, String email) {
        super(customerId, name);
        this.customerId = customerId;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    @Override
    public String getRoleDescription() {
        return "Vehicle Owner";
    }
}
