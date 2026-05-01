module ranks.vehicle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ranks.vehicle to javafx.fxml, javafx.graphics;
    opens ranks.vehicle.view to javafx.fxml, javafx.graphics;
    opens ranks.vehicle.controller to javafx.fxml;

    exports ranks.vehicle;
    exports ranks.vehicle.view;
    exports ranks.vehicle.controller;
    exports ranks.vehicle.model;
    exports ranks.vehicle.dao;
    exports ranks.vehicle.db;
}
