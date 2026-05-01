package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    public int userId;
    public String username;
    public String password;
    public String role;
    public Integer customerId;

    public User(int userId, String username, String password, String role, Integer customerId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.customerId = customerId;
    }
}
