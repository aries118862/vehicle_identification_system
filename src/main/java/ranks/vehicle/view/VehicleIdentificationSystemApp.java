package ranks.vehicle.view;

import ranks.vehicle.controller.AppController;
import ranks.vehicle.model.*;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.chart.*;

import java.sql.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class VehicleIdentificationSystemApp extends Application {

    private Stage primaryStage;
    private Scene loginScene;
    private Scene dashboardScene;

    private Label dashboardTitle;
    private Label dashboardSubtitle;
    private Label currentRoleLabel;
    private Label currentUserLabel;
    private VBox navMenu;
    private StackPane dashboardContent;
    private TextField globalSearchField;

    private final AppController controller = new AppController();
    private User loggedInUser;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        controller.initializeDatabase();
        loginScene = new Scene(createLoginView(), 1450, 860);
        primaryStage.setTitle("Vehicle Identification System - Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private Parent createLoginView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #020617, #0f172a);");

        VBox loginCard = new VBox(16);
        loginCard.setAlignment(Pos.CENTER_LEFT);
        loginCard.setPadding(new Insets(35));
        loginCard.setMaxWidth(440);
        loginCard.setStyle(
                "-fx-background-color: #0f172a;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: #1e293b;" +
                        "-fx-border-radius: 24;"
        );
        loginCard.setEffect(new DropShadow(25, Color.rgb(0, 0, 0, 0.35)));

        Label logo = new Label("VIS");
        logo.setTextFill(Color.web("#60a5fa"));
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        Label title = new Label("Vehicle Identification System");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Label subtitle = new Label("");
        subtitle.setTextFill(Color.web("#94a3b8"));
        subtitle.setFont(Font.font(14));

        VBox header = new VBox(5, logo, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setMaxWidth(Double.MAX_VALUE);

        TextField usernameField = styledTextField("Enter username");
        PasswordField passwordField = styledPasswordField("Enter password");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Administrator", "Customer", "Police", "Workshop", "Insurance");
        roleCombo.setPromptText("Choose role");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setStyle(comboStyle());

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(primaryButtonStyle());
        loginButton.setEffect(new DropShadow(18, Color.rgb(37, 99, 235, 0.25)));

        FadeTransition ft = new FadeTransition(Duration.seconds(1.2), loginButton);
        ft.setFromValue(1.0);
        ft.setToValue(0.45);
        ft.setCycleCount(FadeTransition.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();

        loginButton.setOnAction(e -> {
            try {
                handleLogin(usernameField.getText(), passwordField.getText(), roleCombo.getValue());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Login Error", ex.getMessage());
            }
        });

        Button signUpButton = new Button("Create Customer Account");
        signUpButton.setMaxWidth(Double.MAX_VALUE);
        signUpButton.setStyle(successButtonStyle());
        signUpButton.setOnAction(e -> {
            Scene signUpScene = new Scene(createCustomerSignUpView(), 1450, 860);
            primaryStage.setTitle("Vehicle Identification System - Customer Sign Up");
            primaryStage.setScene(signUpScene);
        });

        VBox form = new VBox(10,
                createFieldBox("Username", usernameField),
                createFieldBox("Password", passwordField),
                createFieldBox("Select Role", roleCombo),
                loginButton,
                signUpButton
        );

        VBox demoBox = new VBox(6,
                infoLabel("Demo accounts", true),
                infoLabel("admin / 123 / Administrator", false),
                infoLabel("kabelo / 123 / Customer", false),
                infoLabel("police1 / 123 / Police", false),
                infoLabel("workshop1 / 123 / Workshop", false),
                infoLabel("insurance1 / 123 / Insurance", false)
        );
        demoBox.setPadding(new Insets(16));
        demoBox.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 16;"
        );

        loginCard.getChildren().addAll(header, form, demoBox);

        StackPane center = new StackPane(loginCard);
        center.setPadding(new Insets(30));
        root.setCenter(center);
        return root;
    }


    private Parent createCustomerSignUpView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #020617, #0f172a);");

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(35));
        card.setMaxWidth(520);
        card.setStyle(
                "-fx-background-color: #0f172a;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: #1e293b;" +
                        "-fx-border-radius: 24;"
        );
        card.setEffect(new DropShadow(25, Color.rgb(0, 0, 0, 0.35)));

        Label logo = new Label("VIS");
        logo.setTextFill(Color.web("#60a5fa"));
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        Label title = new Label("Customer Sign Up");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label subtitle = new Label("Create customer account and login credentials");
        subtitle.setTextFill(Color.web("#94a3b8"));
        subtitle.setFont(Font.font(14));

        VBox header = new VBox(5, logo, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setMaxWidth(Double.MAX_VALUE);

        TextField fullNameField = styledTextField("Enter full name");
        TextField addressField = styledTextField("Enter address");
        TextField phoneField = styledTextField("Enter phone number");
        TextField emailField = styledTextField("Enter email address");
        TextField usernameField = styledTextField("Choose username");
        PasswordField passwordField = styledPasswordField("Choose password");
        PasswordField confirmPasswordField = styledPasswordField("Confirm password");

        Button registerButton = new Button("Register Customer");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setStyle(primaryButtonStyle());

        Button backButton = new Button("Back to Login");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(dangerButtonStyle());
        backButton.setOnAction(e -> {
            primaryStage.setTitle("Vehicle Identification System - Login");
            primaryStage.setScene(loginScene);
        });

        registerButton.setOnAction(e -> {
            try {
                String fullName = fullNameField.getText();
                String address = addressField.getText();
                String phone = phoneField.getText();
                String email = emailField.getText();
                String username = usernameField.getText();
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                if (fullName == null || fullName.isBlank() ||
                        address == null || address.isBlank() ||
                        phone == null || phone.isBlank() ||
                        email == null || email.isBlank() ||
                        username == null || username.isBlank() ||
                        password == null || password.isBlank() ||
                        confirmPassword == null || confirmPassword.isBlank()) {
                    showAlert(Alert.AlertType.WARNING, "Missing Details", "Please complete all fields.");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    showAlert(Alert.AlertType.ERROR, "Password Error", "Passwords do not match.");
                    return;
                }

                controller.registerCustomer(fullName, address, phone, email, username, password);

                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Customer account created successfully.\nYou can now login as Customer.");

                fullNameField.clear();
                addressField.clear();
                phoneField.clear();
                emailField.clear();
                usernameField.clear();
                passwordField.clear();
                confirmPasswordField.clear();

                primaryStage.setTitle("Vehicle Identification System - Login");
                primaryStage.setScene(loginScene);

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Registration Error", ex.getMessage());
            }
        });

        VBox form = new VBox(10,
                createFieldBox("Full Name", fullNameField),
                createFieldBox("Address", addressField),
                createFieldBox("Phone", phoneField),
                createFieldBox("Email", emailField),
                createFieldBox("Username", usernameField),
                createFieldBox("Password", passwordField),
                createFieldBox("Confirm Password", confirmPasswordField),
                registerButton,
                backButton
        );

        card.getChildren().addAll(header, form);

        StackPane center = new StackPane(card);
        center.setPadding(new Insets(30));
        root.setCenter(center);

        return root;
    }

    private void handleLogin(String username, String password, String selectedRole) {
        if (username == null || username.isBlank() || password == null || password.isBlank() || selectedRole == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Details", "Please enter username, password, and select role.");
            return;
        }

        User found = controller.login(username, password, selectedRole);
        if (found == null) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid login details or role mismatch.");
            return;
        }

        loggedInUser = found;
        dashboardScene = new Scene(createDashboardView(found), 1450, 860);
        primaryStage.setTitle("Vehicle Identification System - Main Window");
        primaryStage.setScene(dashboardScene);
    }

    private Parent createDashboardView(User user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #020617;");

        MenuBar menuBar = new MenuBar();
        Menu systemMenu = new Menu("System");
        Menu fileMenu = new Menu("File");
        MenuItem exportItem = new MenuItem("Export Activity Log");
        exportItem.setOnAction(e -> exportActivityLog());
        fileMenu.getItems().add(exportItem);

        MenuItem dashboardItem = new MenuItem("Refresh Dashboard");
        dashboardItem.setOnAction(e -> openRoleDashboard(loggedInUser));

        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> {
            loggedInUser = null;
            primaryStage.setTitle("Vehicle Identification System - Login");
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Vehicle Identification System - Login");
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> confirmExit());

        systemMenu.getItems().addAll(dashboardItem, new SeparatorMenuItem(), logoutItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().addAll(fileMenu, systemMenu);

        VBox sidebar = new VBox(18);
        sidebar.setPadding(new Insets(22));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: #0f172a; -fx-border-color: #1e293b; -fx-border-width: 0 1 0 0;");

        Label sideLogo = new Label("VIS");
        sideLogo.setTextFill(Color.web("#60a5fa"));
        sideLogo.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        Label sideDesc = new Label("Vehicle Identification System");
        sideDesc.setTextFill(Color.web("#94a3b8"));
        sideDesc.setFont(Font.font(13));

        VBox sideHeader = new VBox(4, sideLogo, sideDesc);

        navMenu = new VBox(10);

        currentRoleLabel = new Label(capitalize(user.role));
        currentRoleLabel.setTextFill(Color.WHITE);
        currentRoleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        currentUserLabel = new Label(user.username);
        currentUserLabel.setTextFill(Color.web("#94a3b8"));
        currentUserLabel.setFont(Font.font(12));

        VBox userBox = new VBox(6,
                makeSmallLabel("Logged in as"),
                currentRoleLabel,
                currentUserLabel
        );
        userBox.setPadding(new Insets(16));
        userBox.setStyle(cardStyle());

        Button logoutButton = new Button("Logout");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setStyle(dangerButtonStyle());
        logoutButton.setOnAction(e -> {
            loggedInUser = null;
            primaryStage.setTitle("Vehicle Identification System - Login");
            primaryStage.setScene(loginScene);
        });

        sidebar.getChildren().addAll(sideHeader, navMenu, userBox, logoutButton);
        VBox.setVgrow(navMenu, Priority.ALWAYS);

        BorderPane mainArea = new BorderPane();
        mainArea.setPadding(new Insets(24));

        dashboardTitle = new Label();
        dashboardTitle.setTextFill(Color.WHITE);
        dashboardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        dashboardSubtitle = new Label();
        dashboardSubtitle.setTextFill(Color.web("#94a3b8"));
        dashboardSubtitle.setFont(Font.font(14));

        VBox headingBox = new VBox(6, dashboardTitle, dashboardSubtitle);

        globalSearchField = styledTextField("Search registration / owner / report...");
        globalSearchField.setPrefWidth(320);

        Button searchButton = new Button("Search");
        searchButton.setStyle(primaryButtonStyle());
        searchButton.setOnAction(e -> runGlobalSearch());

        HBox searchBox = new HBox(10, globalSearchField, searchButton);
        searchBox.setAlignment(Pos.CENTER_RIGHT);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(headingBox);
        topBar.setRight(searchBox);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        dashboardContent = new StackPane();
        dashboardContent.setAlignment(Pos.TOP_LEFT);

        mainArea.setTop(topBar);
        mainArea.setCenter(dashboardContent);

        BorderPane centerWrapper = new BorderPane();
        centerWrapper.setTop(menuBar);
        centerWrapper.setCenter(mainArea);

        root.setLeft(sidebar);
        root.setCenter(centerWrapper);

        openRoleDashboard(user);
        return root;
    }

    private void openRoleDashboard(User user) {
        switch (user.role) {
            case "admin" -> {
                dashboardTitle.setText("Administrator Dashboard");
                dashboardSubtitle.setText("Manage all modules and monitor the full system");
                renderNav(List.of("Overview", "Vehicles", "Customers", "Reports", "Violations", "Policies", "Operations Center"));
                showAdminPage("Overview");
            }
            case "customer" -> {
                dashboardTitle.setText("Customer Dashboard");
                dashboardSubtitle.setText("View your vehicle, service history, and queries");
                renderNav(List.of("My Vehicle", "Service History", "Queries", "Report Theft"));
                showCustomerPage("My Vehicle");
            }
            case "police" -> {
                dashboardTitle.setText("Police Dashboard");
                dashboardSubtitle.setText("Search vehicles, submit reports, and manage violations");
                renderNav(List.of("Search Record", "Reports", "Violations", "Vehicle Status"));
                showPolicePage("Search Record");
            }
            case "workshop" -> {
                dashboardTitle.setText("Workshop Dashboard");
                dashboardSubtitle.setText("Manage service records and workshop activity");
                renderNav(List.of("Services", "History", "Revenue"));
                showWorkshopPage("Services");
            }
            case "insurance" -> {
                dashboardTitle.setText("Insurance Dashboard");
                dashboardSubtitle.setText("Manage policies and verification details");
                renderNav(List.of("Policies", "Verification", "Claims"));
                showInsurancePage("Policies");
            }
            default -> dashboardContent.getChildren().setAll(simpleCard("Unknown role"));
        }
    }

    private void renderNav(List<String> items) {
        navMenu.getChildren().clear();

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            Button button = new Button(item);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setStyle(i == 0 ? activeNavStyle() : navStyle());
            button.setOnAction(e -> {
                for (var node : navMenu.getChildren()) {
                    if (node instanceof Button b) b.setStyle(navStyle());
                }
                button.setStyle(activeNavStyle());
                openModule(item);
            });
            navMenu.getChildren().add(button);
        }
    }

    private void openModule(String item) {
        if (loggedInUser == null) return;

        switch (loggedInUser.role) {
            case "admin" -> showAdminPage(item);
            case "customer" -> showCustomerPage(item);
            case "police" -> showPolicePage(item);
            case "workshop" -> showWorkshopPage(item);
            case "insurance" -> showInsurancePage(item);
        }
    }

    private void showAdminPage(String item) {
        switch (item) {
            case "Overview" -> dashboardContent.getChildren().setAll(createAdminOverview());
            case "Vehicles" -> dashboardContent.getChildren().setAll(createAdminVehiclesPage());
            case "Customers" -> dashboardContent.getChildren().setAll(createAdminCustomersPage());
            case "Reports" -> dashboardContent.getChildren().setAll(createAdminReportsPage());
            case "Violations" -> dashboardContent.getChildren().setAll(createAdminViolationsPage());
            case "Policies" -> dashboardContent.getChildren().setAll(createAdminPoliciesPage());
            case "Operations Center" -> dashboardContent.getChildren().setAll(createOperationsCenterPage());
            default -> dashboardContent.getChildren().setAll(createAdminOverview());
        }
    }

    private void showCustomerPage(String item) {
        switch (item) {
            case "My Vehicle" -> dashboardContent.getChildren().setAll(createCustomerVehiclePage());
            case "Service History" -> dashboardContent.getChildren().setAll(createCustomerServicePage());
            case "Queries" -> dashboardContent.getChildren().setAll(createCustomerQueriesPage());
            case "Report Theft" -> dashboardContent.getChildren().setAll(createCustomerTheftReportPage());
            default -> dashboardContent.getChildren().setAll(createCustomerVehiclePage());
        }
    }

    private void showPolicePage(String item) {
        switch (item) {
            case "Search Record" -> dashboardContent.getChildren().setAll(createPoliceSearchPage());
            case "Reports" -> dashboardContent.getChildren().setAll(createPoliceReportsPage());
            case "Violations" -> dashboardContent.getChildren().setAll(createPoliceViolationsPage());
            case "Vehicle Status" -> dashboardContent.getChildren().setAll(createPoliceVehicleStatusPage());
            default -> dashboardContent.getChildren().setAll(createPoliceSearchPage());
        }
    }

    private void showWorkshopPage(String item) {
        switch (item) {
            case "Services" -> dashboardContent.getChildren().setAll(createWorkshopServicesPage());
            case "History" -> dashboardContent.getChildren().setAll(createWorkshopHistoryPage());
            case "Revenue" -> dashboardContent.getChildren().setAll(createWorkshopRevenuePage());
            default -> dashboardContent.getChildren().setAll(createWorkshopServicesPage());
        }
    }

    private void showInsurancePage(String item) {
        switch (item) {
            case "Policies" -> dashboardContent.getChildren().setAll(createInsurancePoliciesPage());
            case "Verification" -> dashboardContent.getChildren().setAll(createInsuranceVerificationPage());
            case "Claims" -> dashboardContent.getChildren().setAll(createInsuranceClaimsPage());
            default -> dashboardContent.getChildren().setAll(createInsurancePoliciesPage());
        }
    }

    private Parent createAdminOverview() {
        DashboardStats stats = controller.getStats();

        HBox cards = new HBox(16,
                makeStatCard("Vehicles", String.valueOf(stats.vehicleCount)),
                makeStatCard("Customers", String.valueOf(stats.customerCount)),
                makeStatCard("Reports", String.valueOf(stats.reportCount)),
                makeStatCard("Policies", String.valueOf(stats.policyCount))
        );
        cards.setAlignment(Pos.CENTER_LEFT);

        VBox recentActivityBox = new VBox(12);
        recentActivityBox.getChildren().add(sectionTitle("Recent System Activity"));
        List<String> activities = controller.getRecentActivities();
        if (activities.isEmpty()) {
            recentActivityBox.getChildren().add(activityItem("No recent activity found."));
        } else {
            for (String activity : activities) {
                recentActivityBox.getChildren().add(activityItem(activity));
            }
        }
        recentActivityBox.setPadding(new Insets(20));
        recentActivityBox.setStyle(cardStyle());

        PieChart violationChart = createViolationStatusChart();
        VBox violationChartBox = new VBox(12, sectionTitle("Violation Status Analysis"), violationChart);
        violationChartBox.setPadding(new Insets(20));
        violationChartBox.setStyle(cardStyle());
        VBox.setVgrow(violationChart, Priority.ALWAYS);

        BarChart<String, Number> serviceChart = createServiceTypeChart();
        VBox serviceChartBox = new VBox(12, sectionTitle("Service Type Frequency"), serviceChart);
        serviceChartBox.setPadding(new Insets(20));
        serviceChartBox.setStyle(cardStyle());
        VBox.setVgrow(serviceChart, Priority.ALWAYS);

        LineChart<String, Number> revenueChart = createMonthlyRevenueChart();
        VBox revenueChartBox = new VBox(12, sectionTitle("Monthly Service Revenue"), revenueChart);
        revenueChartBox.setPadding(new Insets(20));
        revenueChartBox.setStyle(cardStyle());
        VBox.setVgrow(revenueChart, Priority.ALWAYS);

        HBox chartRow1 = new HBox(16, violationChartBox, serviceChartBox);
        HBox.setHgrow(violationChartBox, Priority.ALWAYS);
        HBox.setHgrow(serviceChartBox, Priority.ALWAYS);

        VBox rootContainer = new VBox(20, cards, chartRow1, revenueChartBox, recentActivityBox);
        return wrap(rootContainer);
    }

    private Parent createOperationsCenterPage() {
        VBox root = new VBox(20);

        HBox progressRow = new HBox(16);
        ProgressBar progressBar = new ProgressBar(0.85);
        progressBar.setPrefWidth(420);
        ProgressIndicator progressIndicator = new ProgressIndicator(0.85);
        Label progressText = new Label("System readiness: 85% active monitoring coverage");
        progressText.setTextFill(Color.web("#e2e8f0"));
        progressRow.setAlignment(Pos.CENTER_LEFT);
        progressRow.getChildren().addAll(progressBar, progressIndicator, progressText);

        VBox auditItems = new VBox(8);
        for (int i = 1; i <= 20; i++) {
            Label item = new Label("Inspection queue item " + i + " - pending vehicle verification record");
            item.setTextFill(Color.web("#e2e8f0"));
            item.setPadding(new Insets(10));
            item.setMaxWidth(Double.MAX_VALUE);
            item.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10;");
            auditItems.getChildren().add(item);
        }
        ScrollPane scrollPane = new ScrollPane(auditItems);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(260);
        scrollPane.setStyle("-fx-background: #0f172a; -fx-background-color: #0f172a;");

        Pagination pagination = new Pagination(4, 0);
        pagination.setPageFactory(pageIndex -> {
            VBox page = new VBox(10);
            page.setPadding(new Insets(14));
            page.setStyle(cardStyle());
            Label heading = sectionTitle("Operations Page " + (pageIndex + 1));
            String text;
            if (pageIndex == 0) text = "Workshop desk: records maintenance work, diagnostics, and service costs.";
            else if (pageIndex == 1) text = "Customer desk: handles owner requests and vehicle condition updates.";
            else if (pageIndex == 2) text = "Police desk: verifies registration records, reports incidents, and tracks violations.";
            else text = "Insurance desk: manages active policy records and vehicle cover verification.";
            Label content = new Label(text);
            content.setTextFill(Color.web("#cbd5e1"));
            content.setWrapText(true);
            page.getChildren().addAll(heading, content);
            return page;
        });

        VBox polymorphismBox = new VBox(8);
        polymorphismBox.getChildren().add(sectionTitle("Staff Directory"));
        BasePerson[] people = new BasePerson[] {
                new Customer(1, "Customer User", "Maseru", "58000000", "customer@vis.local"),
                new PoliceOfficer(2, "Police User", "LP-204"),
                new WorkshopTechnician(3, "Workshop User", "Engine Diagnostics")
        };
        for (BasePerson person : people) {
            Label lbl = new Label("Authorized user profile: " + person.getDisplayName());
            lbl.setTextFill(Color.web("#cbd5e1"));
            polymorphismBox.getChildren().add(lbl);
        }
        Label note = new Label("The system stores different staff and customer profiles under a shared person structure while still keeping role-specific information.");
        note.setTextFill(Color.web("#93c5fd"));
        note.setWrapText(true);
        polymorphismBox.getChildren().add(note);
        polymorphismBox.setPadding(new Insets(20));
        polymorphismBox.setStyle(cardStyle());

        Button exportButton = new Button("Export Activity Log");
        exportButton.setStyle(successButtonStyle());
        exportButton.setOnAction(e -> exportActivityLog());

        VBox topCard = new VBox(12, sectionTitle("System Monitoring"), progressRow, exportButton);
        topCard.setPadding(new Insets(20));
        topCard.setStyle(cardStyle());

        VBox scrollCard = new VBox(12, sectionTitle("Inspection Queue"), scrollPane);
        scrollCard.setPadding(new Insets(20));
        scrollCard.setStyle(cardStyle());

        VBox paginationCard = new VBox(12, sectionTitle("Department Workflow Pages"), pagination);
        paginationCard.setPadding(new Insets(20));
        paginationCard.setStyle(cardStyle());

        root.getChildren().addAll(topCard, scrollCard, paginationCard, polymorphismBox);
        return wrap(root);
    }

    private void exportActivityLog() {
        Path output = Path.of("vis_activity_log.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            DashboardStats stats = controller.getStats();
            writer.write("Vehicle Identification System Activity Log");
            writer.newLine();
            writer.write("Generated from the operational dashboard");
            writer.newLine();
            writer.write("Logged in user: " + (loggedInUser == null ? "None" : loggedInUser.username + " (" + loggedInUser.role + ")"));
            writer.newLine();
            writer.write("Vehicles: " + stats.vehicleCount);
            writer.newLine();
            writer.write("Customers: " + stats.customerCount);
            writer.newLine();
            writer.write("Police reports: " + stats.reportCount);
            writer.newLine();
            writer.write("Insurance policies: " + stats.policyCount);
            writer.newLine();
            writer.write("Recent activities:");
            writer.newLine();
            for (String activity : controller.getRecentActivities()) {
                writer.write("- " + activity);
                writer.newLine();
            }
            showAlert(Alert.AlertType.INFORMATION, "File Exported", "Activity log exported to: " + output.toAbsolutePath());
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "File Handling Error", ex.getMessage());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Export Error", ex.getMessage());
        }
    }

    private PieChart createViolationStatusChart() {
        Map<String, Integer> data = controller.getViolationStatusCounts();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        PieChart chart = new PieChart(pieData);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setPrefHeight(350);
        styleChart(chart);
        return chart;
    }

    private BarChart<String, Number> createServiceTypeChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Service Type");
        yAxis.setLabel("Total");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCategoryGap(20);
        chart.setBarGap(5);
        chart.setPrefHeight(350);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> data = controller.getServiceTypeCounts();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        styleChart(chart);
        return chart;
    }

    private LineChart<String, Number> createMonthlyRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Revenue (M)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(350);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> data = controller.getMonthlyRevenue();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        styleChart(chart);
        return chart;
    }

    private void styleChart(Chart chart) {
        chart.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
    }

    private Parent createAdminVehiclesPage() {
        VBox root = new VBox(20);

        HBox formRow = new HBox(12);
        TextField regField = styledTextField("Registration");
        TextField makeField = styledTextField("Make");
        TextField modelField = styledTextField("Model");
        TextField yearField = styledTextField("Year");
        TextField ownerIdField = styledTextField("Owner ID");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Valid", "Service Due", "Flagged");
        statusBox.setPromptText("Status");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(comboStyle());

        HBox.setHgrow(regField, Priority.ALWAYS);
        HBox.setHgrow(makeField, Priority.ALWAYS);
        HBox.setHgrow(modelField, Priority.ALWAYS);
        HBox.setHgrow(yearField, Priority.ALWAYS);
        HBox.setHgrow(ownerIdField, Priority.ALWAYS);
        HBox.setHgrow(statusBox, Priority.ALWAYS);
        formRow.getChildren().addAll(regField, makeField, modelField, yearField, ownerIdField, statusBox);

        TableView<Vehicle> table = createVehicleTable();
        refreshVehicleTable(table);

        Button addVehicle = new Button("Add Vehicle");
        addVehicle.setStyle(successButtonStyle());
        addVehicle.setOnAction(e -> {
            try {
                controller.addVehicle(
                        regField.getText(),
                        makeField.getText(),
                        modelField.getText(),
                        Integer.parseInt(yearField.getText()),
                        Integer.parseInt(ownerIdField.getText()),
                        statusBox.getValue()
                );
                refreshVehicleTable(table);
                regField.clear();
                makeField.clear();
                modelField.clear();
                yearField.clear();
                ownerIdField.clear();
                statusBox.setValue(null);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Vehicle added successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Add Vehicle Error", ex.getMessage());
            }
        });

        ComboBox<String> updateStatusBox = new ComboBox<>();
        updateStatusBox.getItems().addAll("Valid", "Service Due", "Flagged");
        updateStatusBox.setPromptText("Choose updated status");
        updateStatusBox.setMaxWidth(Double.MAX_VALUE);
        updateStatusBox.setStyle(comboStyle());

        Button updateSelected = new Button("Update Selected Vehicle Status");
        updateSelected.setStyle(primaryButtonStyle());
        updateSelected.setOnAction(e -> {
            Vehicle v = table.getSelectionModel().getSelectedItem();
            if (v == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a vehicle from the table.");
                return;
            }
            try {
                controller.updateVehicleStatus(v.getVehicleId(), updateStatusBox.getValue());
                refreshVehicleTable(table);
                updateStatusBox.setValue(null);
                showAlert(Alert.AlertType.INFORMATION, "Updated", "Vehicle status updated successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Update Error", ex.getMessage());
            }
        });

        Button deleteSelected = new Button("Remove Selected");
        deleteSelected.setStyle(dangerButtonStyle());
        deleteSelected.setOnAction(e -> {
            Vehicle v = table.getSelectionModel().getSelectedItem();
            if (v == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a vehicle.");
                return;
            }
            try {
                controller.deleteVehicle(v.getVehicleId());
                refreshVehicleTable(table);
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Vehicle removed successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Delete Error", ex.getMessage());
            }
        });

        VBox formBox = new VBox(12, sectionTitle("Add Vehicle"), formRow, addVehicle);
        formBox.setPadding(new Insets(20));
        formBox.setStyle(cardStyle());

        VBox statusBoxPane = new VBox(12, sectionTitle("Edit Vehicle Status"), updateStatusBox, updateSelected);
        statusBoxPane.setPadding(new Insets(20));
        statusBoxPane.setStyle(cardStyle());

        VBox tableBox = new VBox(12, sectionTitle("Vehicle Records"), table, deleteSelected);
        tableBox.setPadding(new Insets(20));
        tableBox.setStyle(cardStyle());
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(formBox, statusBoxPane, tableBox);
        return wrap(root);
    }


    private Parent createAdminCustomersPage() {
        TableView<Customer> table = createCustomerTable();
        table.setItems(controller.getCustomers());
        VBox root = new VBox(12, sectionTitle("Customers"), table);
        root.setPadding(new Insets(20));
        root.setStyle(cardStyle());
        return wrap(root);
    }

    private Parent createAdminReportsPage() {
        TableView<PoliceReportModel> table = createReportTable();
        table.setItems(controller.getReports());
        VBox root = new VBox(12, sectionTitle("Police Reports"), table);
        root.setPadding(new Insets(20));
        root.setStyle(cardStyle());
        return wrap(root);
    }

    private Parent createAdminViolationsPage() {
        TableView<ViolationModel> table = createViolationTable();
        table.setItems(controller.getViolations());
        VBox root = new VBox(12, sectionTitle("Violations"), table);
        root.setPadding(new Insets(20));
        root.setStyle(cardStyle());
        return wrap(root);
    }

    private Parent createAdminPoliciesPage() {
        TableView<InsurancePolicyModel> table = createPolicyTable();
        table.setItems(controller.getPolicies());
        VBox root = new VBox(12, sectionTitle("Insurance Policies"), table);
        root.setPadding(new Insets(20));
        root.setStyle(cardStyle());
        return wrap(root);
    }

    private Parent createCustomerVehiclePage() {
        VBox root = new VBox(20);
        if (loggedInUser.customerId == null) {
            root.getChildren().add(simpleCard("No customer profile linked."));
            return wrap(root);
        }

        Vehicle v = controller.getVehicleByCustomerId(loggedInUser.customerId);
        if (v == null) {
            root.getChildren().add(simpleCard("No vehicle found for this customer."));
            return wrap(root);
        }

        HBox cards = new HBox(16,
                makeInfoCard("My Vehicle", v.getMake() + " " + v.getModel(), "Reg: " + v.getRegistrationNumber()),
                makeInfoCard("Status", v.getStatus(), "Year: " + v.getYear()),
                makeInfoCard("Owner", v.getOwnerName(), "Customer linked")
        );

        root.getChildren().add(cards);
        return wrap(root);
    }

    private Parent createCustomerServicePage() {
        TableView<ServiceRecordModel> table = createServiceTable();
        if (loggedInUser.customerId != null) {
            table.setItems(controller.getServiceHistoryForCustomer(loggedInUser.customerId));
        }
        VBox root = new VBox(12, sectionTitle("My Service History"), table);
        root.setPadding(new Insets(20));
        root.setStyle(cardStyle());
        return wrap(root);
    }

    private Parent createCustomerQueriesPage() {
        VBox root = new VBox(20);

        TextArea queryArea = new TextArea();
        queryArea.setPromptText("Ask about your vehicle condition, insurance, or service...");
        queryArea.setPrefRowCount(6);
        queryArea.setStyle(textAreaStyle());

        Button submit = new Button("Submit Query");
        submit.setStyle(primaryButtonStyle());
        submit.setOnAction(e -> {
            try {
                if (loggedInUser.customerId == null) throw new RuntimeException("Customer is not linked to this user.");
                Vehicle v = controller.getVehicleByCustomerId(loggedInUser.customerId);
                if (v == null) throw new RuntimeException("No vehicle linked to this customer.");
                controller.addCustomerQuery(loggedInUser.customerId, v.getVehicleId(), queryArea.getText());
                queryArea.clear();
                showAlert(Alert.AlertType.INFORMATION, "Submitted", "Your query was submitted.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Query Error", ex.getMessage());
            }
        });

        TableView<CustomerQueryModel> table = createQueryTable();
        if (loggedInUser.customerId != null) {
            table.setItems(controller.getQueriesForCustomer(loggedInUser.customerId));
        }

        VBox form = new VBox(12, sectionTitle("Submit Query"), queryArea, submit);
        form.setPadding(new Insets(20));
        form.setStyle(cardStyle());

        VBox list = new VBox(12, sectionTitle("My Queries"), table);
        list.setPadding(new Insets(20));
        list.setStyle(cardStyle());

        root.getChildren().addAll(form, list);
        return wrap(root);
    }


    private Parent createCustomerTheftReportPage() {
        VBox root = new VBox(20);

        if (loggedInUser.customerId == null) {
            root.getChildren().add(simpleCard("No customer profile linked."));
            return wrap(root);
        }

        Vehicle vehicle = controller.getVehicleByCustomerId(loggedInUser.customerId);
        if (vehicle == null) {
            root.getChildren().add(simpleCard("No vehicle found for this customer."));
            return wrap(root);
        }

        Label vehicleInfo = new Label("Vehicle: " + vehicle.getRegistrationNumber() + " | " + vehicle.getMake() + " " + vehicle.getModel());
        vehicleInfo.setTextFill(Color.web("#e2e8f0"));

        TextArea theftDescription = new TextArea();
        theftDescription.setPromptText("Describe when and where the vehicle was stolen, and any important details...");
        theftDescription.setPrefRowCount(6);
        theftDescription.setStyle(textAreaStyle());

        Button reportButton = new Button("Report Theft");
        reportButton.setStyle(dangerButtonStyle());
        reportButton.setOnAction(e -> {
            try {
                controller.submitCustomerTheftReport(loggedInUser.customerId, vehicle.getVehicleId(), theftDescription.getText());
                theftDescription.clear();
                showAlert(Alert.AlertType.INFORMATION, "Submitted", "Theft report submitted successfully. Police and admin can now see it.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Theft Report Error", ex.getMessage());
            }
        });

        TableView<PoliceReportModel> table = createReportTable();
        table.setItems(controller.getReports());

        VBox form = new VBox(12, sectionTitle("Report Vehicle Theft"), vehicleInfo, theftDescription, reportButton);
        form.setPadding(new Insets(20));
        form.setStyle(cardStyle());

        VBox list = new VBox(12, sectionTitle("Police Reports Log"), table);
        list.setPadding(new Insets(20));
        list.setStyle(cardStyle());

        root.getChildren().addAll(form, list);
        return wrap(root);
    }

    private Parent createPoliceSearchPage() {
        VBox root = new VBox(20);

        TextField regField = styledTextField("Enter registration number");
        Label resultLabel = new Label("Search result will appear here.");
        resultLabel.setTextFill(Color.web("#cbd5e1"));

        Button checkButton = new Button("Check Record");
        checkButton.setStyle(primaryButtonStyle());
        checkButton.setOnAction(e -> {
            try {
                Vehicle v = controller.searchVehicle(regField.getText());
                if (v == null) {
                    resultLabel.setText("No vehicle found for registration: " + regField.getText());
                } else {
                    resultLabel.setText("Found: " + v.getRegistrationNumber() + " | " + v.getMake() + " " + v.getModel() +
                            " | Owner: " + v.getOwnerName() + " | Status: " + v.getStatus());
                }
            } catch (Exception ex) {
                resultLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox box = new VBox(12, sectionTitle("Search Vehicle Record"), regField, checkButton, resultLabel);
        box.setPadding(new Insets(20));
        box.setStyle(cardStyle());
        root.getChildren().add(box);
        return wrap(root);
    }

    private Parent createPoliceReportsPage() {
        VBox root = new VBox(20);

        TextField vehicleId = styledTextField("Vehicle ID");
        TextField officerName = styledTextField("Officer Name");
        ComboBox<String> reportType = new ComboBox<>();
        reportType.getItems().addAll("Accident", "Theft", "Inspection");
        reportType.setPromptText("Report Type");
        reportType.setMaxWidth(Double.MAX_VALUE);
        reportType.setStyle(comboStyle());

        TextArea desc = new TextArea();
        desc.setPromptText("Report description...");
        desc.setPrefRowCount(4);
        desc.setStyle(textAreaStyle());

        TableView<PoliceReportModel> table = createReportTable();
        table.setItems(controller.getReports());

        Button submit = new Button("Submit Report");
        submit.setStyle(dangerButtonStyle());
        submit.setOnAction(e -> {
            try {
                controller.addPoliceReport(
                        Integer.parseInt(vehicleId.getText()),
                        reportType.getValue(),
                        desc.getText(),
                        officerName.getText()
                );
                table.setItems(controller.getReports());
                vehicleId.clear();
                officerName.clear();
                desc.clear();
                reportType.setValue(null);
                showAlert(Alert.AlertType.INFORMATION, "Submitted", "Police report saved.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Report Error", ex.getMessage());
            }
        });

        VBox form = new VBox(12, sectionTitle("Create Police Report"), vehicleId, officerName, reportType, desc, submit);
        form.setPadding(new Insets(20));
        form.setStyle(cardStyle());

        VBox list = new VBox(12, sectionTitle("Reports"), table);
        list.setPadding(new Insets(20));
        list.setStyle(cardStyle());

        root.getChildren().addAll(form, list);
        return wrap(root);
    }

    private Parent createPoliceViolationsPage() {
        VBox root = new VBox(20);

        TextField vehicleId = styledTextField("Vehicle ID");
        TextField violationType = styledTextField("Violation Type");
        TextField fineAmount = styledTextField("Fine Amount");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Paid", "Unpaid");
        statusBox.setPromptText("Status");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(comboStyle());

        TableView<ViolationModel> table = createViolationTable();
        table.setItems(controller.getViolations());

        Button submit = new Button("Add Violation");
        submit.setStyle(primaryButtonStyle());
        submit.setOnAction(e -> {
            try {
                controller.addViolation(
                        Integer.parseInt(vehicleId.getText()),
                        violationType.getText(),
                        Double.parseDouble(fineAmount.getText()),
                        statusBox.getValue()
                );
                table.setItems(controller.getViolations());
                vehicleId.clear();
                violationType.clear();
                fineAmount.clear();
                statusBox.setValue(null);
                showAlert(Alert.AlertType.INFORMATION, "Saved", "Violation added.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Violation Error", ex.getMessage());
            }
        });

        VBox form = new VBox(12, sectionTitle("Add Violation"), vehicleId, violationType, fineAmount, statusBox, submit);
        form.setPadding(new Insets(20));
        form.setStyle(cardStyle());

        VBox list = new VBox(12, sectionTitle("Violations"), table);
        list.setPadding(new Insets(20));
        list.setStyle(cardStyle());

        root.getChildren().addAll(form, list);
        return wrap(root);
    }


    private Parent createPoliceVehicleStatusPage() {
        VBox root = new VBox(20);

        TextField registrationField = styledTextField("Enter registration number");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Valid", "Service Due", "Flagged");
        statusBox.setPromptText("Select new status");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(comboStyle());

        Button updateButton = new Button("Update Vehicle Status");
        updateButton.setStyle(primaryButtonStyle());

        TableView<Vehicle> table = createVehicleTable();
        refreshVehicleTable(table);

        updateButton.setOnAction(e -> {
            try {
                controller.updateVehicleStatusByRegistration(registrationField.getText(), statusBox.getValue());
                refreshVehicleTable(table);
                registrationField.clear();
                statusBox.setValue(null);
                showAlert(Alert.AlertType.INFORMATION, "Updated", "Vehicle status updated successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Update Error", ex.getMessage());
            }
        });

        VBox form = new VBox(12, sectionTitle("Police Vehicle Status Update"), registrationField, statusBox, updateButton);
        form.setPadding(new Insets(20));
        form.setStyle(cardStyle());

        VBox list = new VBox(12, sectionTitle("All Vehicles"), table);
        list.setPadding(new Insets(20));
        list.setStyle(cardStyle());

        root.getChildren().addAll(form, list);
        return wrap(root);
    }

    private Parent createWorkshopServicesPage() {
        VBox root = new VBox(20);

        HBox stats = new HBox(16,
                makeStatCard("Today Services", String.valueOf(controller.getServiceRecords().size())),
                makeStatCard("Pending Repairs", "7"),
                makeStatCard("Revenue", String.format("M %.2f", controller.getTotalServiceRevenue()))
        );

        TextField vehicleIdField = styledTextField("Vehicle ID");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        TextField serviceTypeField = styledTextField("Service Type");
        TextField costField = styledTextField("Cost");

        TextArea desc = new TextArea();
        desc.setPromptText("Service description...");
        desc.setPrefRowCount(5);
        desc.setStyle(textAreaStyle());

        TableView<ServiceRecordModel> table = createServiceTable();
        table.setItems(controller.getServiceRecords());

        Button save = new Button("Save Service Record");
        save.setStyle(successButtonStyle());
        save.setOnAction(e -> {
            try {
                controller.addServiceRecord(
                        Integer.parseInt(vehicleIdField.getText()),
                        datePicker.getValue(),
                        serviceTypeField.getText(),
                        desc.getText(),
                        Double.parseDouble(costField.getText())
                );
                table.setItems(controller.getServiceRecords());
                vehicleIdField.clear();
                serviceTypeField.clear();
                costField.clear();
                desc.clear();
                showAlert(Alert.AlertType.INFORMATION, "Saved", "Service record saved successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Service Error", ex.getMessage());
            }
        });

        VBox form = new VBox(12,
                sectionTitle("Add Service Record"),
                vehicleIdField,
                datePicker,
                serviceTypeField,
                costField,
                desc,
                save
        );
        form.setPadding(new Insets(20));
        form.setStyle(cardStyle());

        VBox list = new VBox(12, sectionTitle("Service Records"), table);
        list.setPadding(new Insets(20));
        list.setStyle(cardStyle());

        root.getChildren().addAll(stats, form, list);
        return wrap(root);
    }

    private Parent createWorkshopHistoryPage() {
        TableView<ServiceRecordModel> table = createServiceTable();
        table.setItems(controller.getServiceRecords());
        VBox root = new VBox(12, sectionTitle("Workshop Service History"), table);
        root.setPadding(new Insets(20));
        root.setStyle(cardStyle());
        return wrap(root);
    }

    private Parent createWorkshopRevenuePage() {
        HBox cards = new HBox(16,
                makeInfoCard("Today", String.format("M %.2f", controller.getTotalServiceRevenue()), "Current total recorded revenue"),
                makeInfoCard("This Week", "M 12400.00", "Workshop performance"),
                makeInfoCard("This Month", "M 41850.00", "Projected mock total")
        );
        VBox root = new VBox(12, cards);
        return wrap(root);
    }

    private Parent createInsurancePoliciesPage() {
        VBox root = new VBox(20);

        TextField vehicleId = styledTextField("Vehicle ID");
        TextField policyNumber = styledTextField("Policy Number");
        DatePicker expiryDate = new DatePicker(LocalDate.now().plusMonths(6));
        expiryDate.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Pending", "Claim Review");
        statusBox.setPromptText("Status");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(comboStyle());

        TableView<InsurancePolicyModel> table = createPolicyTable();
        table.setItems(controller.getPolicies());

        Button save = new Button("Save Policy");
        save.setStyle(primaryButtonStyle());
        save.setOnAction(e -> {
            try {
                controller.addPolicy(
                        Integer.parseInt(vehicleId.getText()),
                        policyNumber.getText(),
                        expiryDate.getValue(),
                        statusBox.getValue()
                );
                table.setItems(controller.getPolicies());
                vehicleId.clear();
                policyNumber.clear();
                statusBox.setValue(null);
                showAlert(Alert.AlertType.INFORMATION, "Saved", "Policy saved successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Policy Error", ex.getMessage());
            }
        });

        VBox form = new VBox(12, sectionTitle("Register Insurance"), vehicleId, policyNumber, expiryDate, statusBox, save);
        form.setPadding(new Insets(20));
        form.setStyle(cardStyle());

        VBox list = new VBox(12, sectionTitle("Policies"), table);
        list.setPadding(new Insets(20));
        list.setStyle(cardStyle());

        root.getChildren().addAll(form, list);
        return wrap(root);
    }

    private Parent createInsuranceVerificationPage() {
        TableView<InsurancePolicyModel> table = createPolicyTable();
        table.setItems(controller.getPolicies());
        VBox root = new VBox(12, sectionTitle("Verification Requests / Policies"), table);
        root.setPadding(new Insets(20));
        root.setStyle(cardStyle());
        return wrap(root);
    }

    private Parent createInsuranceClaimsPage() {
        VBox box = new VBox(12,
                sectionTitle("Claims"),
                activityItem("Vehicle A 8882 - Claim under review"),
                activityItem("Vehicle C 1021 - Awaiting verification"),
                activityItem("Vehicle B 4567 - No active claim")
        );
        box.setPadding(new Insets(20));
        box.setStyle(cardStyle());
        return wrap(box);
    }

    private void runGlobalSearch() {
        try {
            String value = globalSearchField.getText();
            if (value == null || value.isBlank()) {
                showAlert(Alert.AlertType.INFORMATION, "Search", "Type something to search.");
                return;
            }
            Vehicle v = controller.searchVehicle(value);
            if (v == null) {
                showAlert(Alert.AlertType.INFORMATION, "Search Result", "No matching vehicle record found.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Search Result",
                        "Vehicle Found\n\nReg No: " + v.getRegistrationNumber() +
                                "\nMake: " + v.getMake() +
                                "\nModel: " + v.getModel() +
                                "\nOwner: " + v.getOwnerName() +
                                "\nStatus: " + v.getStatus());
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Search Error", ex.getMessage());
        }
    }

    private void refreshVehicleTable(TableView<Vehicle> table) {
        table.setItems(controller.getVehicles());
    }

    private TableView<Vehicle> createVehicleTable() {
        TableView<Vehicle> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(tableStyle());

        TableColumn<Vehicle, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getVehicleId()));

        TableColumn<Vehicle, String> regCol = new TableColumn<>("Reg No");
        regCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRegistrationNumber()));

        TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
        makeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMake()));

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModel()));

        TableColumn<Vehicle, Number> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getYear()));

        TableColumn<Vehicle, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOwnerName()));

        TableColumn<Vehicle, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        table.getColumns().addAll(idCol, regCol, makeCol, modelCol, yearCol, ownerCol, statusCol);
        styleTableHeaders(table);
        return table;
    }

    private TableView<Customer> createCustomerTable() {
        TableView<Customer> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(tableStyle());

        TableColumn<Customer, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().customerId));
        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().phone));
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().email));

        table.getColumns().addAll(idCol, nameCol, phoneCol, emailCol);
        styleTableHeaders(table);
        return table;
    }

    private TableView<ServiceRecordModel> createServiceTable() {
        TableView<ServiceRecordModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(tableStyle());

        TableColumn<ServiceRecordModel, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getServiceId()));
        TableColumn<ServiceRecordModel, String> regCol = new TableColumn<>("Vehicle");
        regCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRegistrationNumber()));
        TableColumn<ServiceRecordModel, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getServiceDate()));
        TableColumn<ServiceRecordModel, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getServiceType()));
        TableColumn<ServiceRecordModel, Number> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCost()));

        table.getColumns().addAll(idCol, regCol, dateCol, typeCol, costCol);
        styleTableHeaders(table);
        return table;
    }

    private TableView<PoliceReportModel> createReportTable() {
        TableView<PoliceReportModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(tableStyle());

        TableColumn<PoliceReportModel, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().reportId));
        TableColumn<PoliceReportModel, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().registrationNumber));
        TableColumn<PoliceReportModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().reportType));
        TableColumn<PoliceReportModel, String> officerCol = new TableColumn<>("Officer");
        officerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().officerName));
        TableColumn<PoliceReportModel, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().reportDate));

        table.getColumns().addAll(idCol, vehicleCol, typeCol, officerCol, dateCol);
        styleTableHeaders(table);
        return table;
    }

    private TableView<ViolationModel> createViolationTable() {
        TableView<ViolationModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(tableStyle());

        TableColumn<ViolationModel, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().violationId));
        TableColumn<ViolationModel, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().registrationNumber));
        TableColumn<ViolationModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().violationType));
        TableColumn<ViolationModel, Number> fineCol = new TableColumn<>("Fine");
        fineCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().fineAmount));
        TableColumn<ViolationModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));

        table.getColumns().addAll(idCol, vehicleCol, typeCol, fineCol, statusCol);
        styleTableHeaders(table);
        return table;
    }

    private TableView<InsurancePolicyModel> createPolicyTable() {
        TableView<InsurancePolicyModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(tableStyle());

        TableColumn<InsurancePolicyModel, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().policyId));
        TableColumn<InsurancePolicyModel, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().registrationNumber));
        TableColumn<InsurancePolicyModel, String> policyCol = new TableColumn<>("Policy Number");
        policyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().policyNumber));
        TableColumn<InsurancePolicyModel, LocalDate> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().expiryDate));
        TableColumn<InsurancePolicyModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));

        table.getColumns().addAll(idCol, vehicleCol, policyCol, expiryCol, statusCol);
        styleTableHeaders(table);
        return table;
    }

    private TableView<CustomerQueryModel> createQueryTable() {
        TableView<CustomerQueryModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(tableStyle());

        TableColumn<CustomerQueryModel, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().queryId));
        TableColumn<CustomerQueryModel, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().queryDate));
        TableColumn<CustomerQueryModel, String> textCol = new TableColumn<>("Query");
        textCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().queryText));
        TableColumn<CustomerQueryModel, String> responseCol = new TableColumn<>("Response");
        responseCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().responseText));

        table.getColumns().addAll(idCol, dateCol, textCol, responseCol);
        styleTableHeaders(table);
        return table;
    }

    private ScrollPane wrap(Parent content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return sp;
    }

    private VBox createFieldBox(String labelText, Control field) {
        Label label = new Label(labelText);
        label.setTextFill(Color.web("#cbd5e1"));
        label.setFont(Font.font(13));
        return new VBox(7, label, field);
    }

    private TextField styledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(inputStyle());
        return field;
    }

    private PasswordField styledPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setStyle(inputStyle());
        return field;
    }

    private VBox makeStatCard(String title, String value) {
        Label top = makeSmallLabel(title);
        Label bottom = new Label(value);
        bottom.setTextFill(Color.WHITE);
        bottom.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        VBox box = new VBox(10, top, bottom);
        box.setPadding(new Insets(20));
        box.setStyle(cardStyle());
        box.setPrefWidth(250);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private VBox makeInfoCard(String title, String value, String desc) {
        Label top = makeSmallLabel(title);
        Label mid = new Label(value);
        mid.setTextFill(Color.WHITE);
        mid.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        Label bottom = new Label(desc);
        bottom.setTextFill(Color.web("#64748b"));
        VBox box = new VBox(8, top, mid, bottom);
        box.setPadding(new Insets(20));
        box.setStyle(cardStyle());
        box.setPrefWidth(250);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private VBox simpleCard(String text) {
        VBox box = new VBox(12, sectionTitle(text));
        box.setPadding(new Insets(20));
        box.setStyle(cardStyle());
        return box;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        return label;
    }

    private Label makeSmallLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#94a3b8"));
        label.setFont(Font.font(13));
        return label;
    }

    private HBox activityItem(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setTextFill(Color.web("#e2e8f0"));
        HBox box = new HBox(label);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 14; -fx-border-color: #334155; -fx-border-radius: 14;");
        return box;
    }

    private Label infoLabel(String text, boolean heading) {
        Label label = new Label(text);
        label.setTextFill(heading ? Color.WHITE : Color.web("#94a3b8"));
        label.setFont(Font.font("Arial", heading ? FontWeight.BOLD : FontWeight.NORMAL, heading ? 13 : 12));
        return label;
    }

    private void confirmExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit System");
        alert.setHeaderText("Close Vehicle Identification System?");
        alert.setContentText("Choose OK to exit the application.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                primaryStage.close();
            }
        });
    }

    private <T> void styleTableHeaders(TableView<T> table) {
        Platform.runLater(() -> {
            table.lookupAll(".column-header .label").forEach(node -> {
                if (node instanceof Label label) {
                    label.setTextFill(Color.BLACK);
                    label.setStyle("-fx-font-weight: bold;");
                }
            });

            table.lookupAll(".table-cell").forEach(node ->
                    node.setStyle("-fx-text-fill: black;")
            );
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String inputStyle() {
        return "-fx-background-color: #1e293b;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #64748b;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-border-color: #334155;" +
                "-fx-padding: 12;";
    }

    private String comboStyle() {
        return inputStyle();
    }

    private String cardStyle() {
        return "-fx-background-color: #0f172a;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #1e293b;" +
                "-fx-border-radius: 18;";
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 18 12 18;";
    }

    private String successButtonStyle() {
        return "-fx-background-color: #16a34a;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 18 12 18;";
    }

    private String dangerButtonStyle() {
        return "-fx-background-color: #dc2626;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 18 12 18;";
    }

    private String navStyle() {
        return "-fx-background-color: #1e293b;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 14 12 14;";
    }

    private String activeNavStyle() {
        return "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 14 12 14;";
    }

    private String textAreaStyle() {
        return "-fx-control-inner-background: #1e293b;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #64748b;" +
                "-fx-background-color: #1e293b;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #334155;" +
                "-fx-border-radius: 12;";
    }

    private String tableStyle() {
        return "-fx-background-color: white;" +
                "-fx-control-inner-background: white;" +
                "-fx-table-cell-border-color: #d1d5db;" +
                "-fx-text-background-color: black;" +
                "-fx-selection-bar: #bfdbfe;" +
                "-fx-selection-bar-text: black;";
    }

    public static void main(String[] args) {
        launch(args);
    }

}
