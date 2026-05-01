package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class BasePerson {
    public int id;
    public String name;

    protected BasePerson(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getDisplayName() {
        return name + " - " + getRoleDescription();
    }

    public abstract String getRoleDescription();
}
