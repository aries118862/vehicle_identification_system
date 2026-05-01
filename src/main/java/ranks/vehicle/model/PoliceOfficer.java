package ranks.vehicle.model;

public class PoliceOfficer extends BasePerson {
    private final String badgeNumber;

    public PoliceOfficer(int id, String name, String badgeNumber) {
        super(id, name);
        this.badgeNumber = badgeNumber;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    @Override
    public String getRoleDescription() {
        return "Police Officer - Badge " + badgeNumber;
    }
}
