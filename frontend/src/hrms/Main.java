package hrms;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage stage;
    private BorderPane root;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("HRMS");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        LoginView login = new LoginView(this::showMain);
        Scene scene = new Scene(login.getView(), 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    private void showMain() {
        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(new DashboardView().getView());

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1A3D63;");

        VBox logoBox = new VBox(4);
        logoBox.setPadding(new Insets(32, 24, 24, 24));
        Label logo = new Label("HRMS");
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label logoSub = new Label("Human Resources");
        logoSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #7A99B8;");
        logoBox.getChildren().addAll(logo, logoSub);

        VBox nav = new VBox(4);
        nav.setPadding(new Insets(8, 12, 8, 12));

        Button[] navItems = {
            navItem("Dashboard",  "◉"),
            navItem("Employees",  "◎"),
            navItem("Attendance", "◷"),
            navItem("Payroll",    "◈")
        };

        navItems[0].setStyle(navActiveStyle());

        Node[] cache = new Node[4];

        for (int i = 0; i < navItems.length; i++) {
            final int index = i;
            Button btn = navItems[i];
            nav.getChildren().add(btn);
            btn.setOnAction(e -> {
                for (Button b : navItems) b.setStyle(navDefaultStyle());
                btn.setStyle(navActiveStyle());
                if (cache[index] == null) {
                    cache[index] = switch (index) {
                        case 0  -> new DashboardView().getView();
                        case 1  -> new EmployeeView().getView();
                        case 2  -> new AttendanceView().getView();
                        case 3  -> new PayrollView().getView();
                        default -> new DashboardView().getView();
                    };
                }
                root.setCenter(cache[index]);
            });
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox userBox = new VBox(8);
        userBox.setPadding(new Insets(16, 20, 16, 20));
        userBox.setStyle("-fx-border-color: #24538A; -fx-border-width: 1 0 0 0;");

        Label userName = new Label(Session.name);
        userName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        userName.setWrapText(true);

        Label roleBadge = new Label(Session.role.toUpperCase());
        roleBadge.setStyle("""
            -fx-font-size: 9px;
            -fx-font-weight: bold;
            -fx-text-fill: #1A3D63;
            -fx-background-color: #A8D4F5;
            -fx-background-radius: 4;
            -fx-padding: 2 6 2 6;
        """);

        Button logout = new Button("Sign out");
        logout.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #7A99B8;
            -fx-font-size: 12px;
            -fx-cursor: hand;
            -fx-padding: 4 0 0 0;
        """);
        logout.setOnAction(e -> {
            try { ApiClient.post("/auth/logout", "{}"); } catch (Exception ignored) {}
            ApiClient.setToken("");
            Session.name = "";
            Session.role = "";
            showLogin();
        });

        userBox.getChildren().addAll(userName, roleBadge, logout);
        sidebar.getChildren().addAll(logoBox, nav, spacer, userBox);
        return sidebar;
    }

    private Button navItem(String label, String icon) {
        Button btn = new Button(icon + "  " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(44);
        btn.setStyle(navDefaultStyle());
        return btn;
    }

    private String navDefaultStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #7A99B8;
            -fx-font-size: 13px;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-padding: 0 12 0 12;
        """;
    }

    private String navActiveStyle() {
        return """
            -fx-background-color: #24538A;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-padding: 0 12 0 12;
        """;
    }

    public static void main(String[] args) {
        launch(args);
    }
}