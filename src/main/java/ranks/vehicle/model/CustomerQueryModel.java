package ranks.vehicle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerQueryModel {
    public int queryId;
    public LocalDateTime queryDate;
    public String queryText;
    public String responseText;

    public CustomerQueryModel(int queryId, LocalDateTime queryDate, String queryText, String responseText) {
        this.queryId = queryId;
        this.queryDate = queryDate;
        this.queryText = queryText;
        this.responseText = responseText;
    }
}
