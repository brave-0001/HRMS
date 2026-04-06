package hrms;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardView {

    public VBox getView() {
        VBox root = new VBox(36);
        root.setPadding(new Insets(44, 52, 44, 52));
        root.setStyle("-fx-background-color: #F6FAFD;");

        VBox header = new VBox(6);
        Label greeting = new Label(greeting());
        greeting.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1A3D63;");
        Label date = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d  yyyy")));
        date.setStyle("-fx-font-size: 13px; -fx-text-fill: #7A99B8;");
        header.getChildren().addAll(greeting, date);

        HBox cards = new HBox(24);
        cards.setAlignment(Pos.CENTER_LEFT);
        cards.getChildren().addAll(
            card("Departments",   fetchCount("/departments/"), "#1A3D63"),
            card("Employees",     fetchCount("/employees/"),   "#24538A"),
            card("Present Today", fetchPresent(),              "#3A7DC9")
        );

        root.getChildren().addAll(header, cards);
        return root;
    }

    private VBox card(String label, String value, String accent) {
        VBox card = new VBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(28, 32, 28, 32));
        card.setPrefWidth(230);
        card.setPrefHeight(140);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 16;
            -fx-effect: dropshadow(gaussian, rgba(26,61,99,0.08), 16, 0, 0, 4);
        """);

        Label val = new Label(value);
        val.setStyle(String.format(
            "-fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: %s;", accent));

        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #7A99B8; -fx-font-weight: bold;");

        card.getChildren().addAll(val, lbl);
        return card;
    }

    private String greeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning.";
        if (hour < 17) return "Good afternoon.";
        return "Good evening.";
    }

    private String fetchCount(String path) {
        try {
            JSONArray arr = new JSONArray(ApiClient.get(path));
            return String.valueOf(arr.length());
        } catch (Exception e) {
            return "—";
        }
    }

    private String fetchPresent() {
        try {
            JSONObject obj = new JSONObject(ApiClient.get("/attendance/today"));
            return String.valueOf(obj.getInt("count"));
        } catch (Exception e) {
            return "—";
        }
    }
}