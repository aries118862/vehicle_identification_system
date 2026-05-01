package ranks.vehicle.model;

public class WorkshopTechnician extends BasePerson {
    private final String specialization;

    public WorkshopTechnician(int id, String name, String specialization) {
        super(id, name);
        this.specialization = specialization;
    }

    public String getSpecialization() {
        return specialization;
    }

    @Override
    public String getRoleDescription() {
        return "Workshop Technician - " + specialization;
    }
}
