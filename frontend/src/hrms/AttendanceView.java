package hrms;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import org.json.*;

public class AttendanceView {

    private TableView<JSONObject> table = new TableView<>();
    private ObservableList<JSONObject> data = FXCollections.observableArrayList();

    public VBox getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #F6FAFD;");

        Label title = new Label("Attendance");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A3D63;");

        setupTable();
        loadData(null);

        HBox toolbar = buildToolbar();

        root.getChildren().addAll(title, toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    private void setupTable() {
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No attendance records found."));

        String[] cols = {"employee_id", "first_name", "last_name", "work_date", "clock_in", "clock_out", "status"};
        for (String col : cols) {
            TableColumn<JSONObject, String> tc = new TableColumn<>(col.replace("_", " ").toUpperCase());
            tc.setCellValueFactory(cell -> {
                String val = cell.getValue().optString(col, "");
                return new javafx.beans.property.SimpleStringProperty(val);
            });
            table.getColumns().add(tc);
        }
    }

    private HBox buildToolbar() {
        TextField empId    = new TextField(); empId.setPromptText("Employee ID");
        TextField workDate = new TextField(); workDate.setPromptText("Date (YYYY-MM-DD)");
        TextField clockIn  = new TextField(); clockIn.setPromptText("Clock in (HH:MM)");
        TextField clockOut = new TextField(); clockOut.setPromptText("Clock out (HH:MM)");

        ComboBox<String> status = new ComboBox<>();
        status.getItems().addAll("present", "absent", "half_day", "holiday");
        status.setValue("present");
        status.setPrefWidth(120);

        Button log = new Button("Log Attendance");
        log.setOnAction(e -> {
            try {
                JSONObject body = new JSONObject();
                body.put("employee_id", Integer.parseInt(empId.getText().trim()));
                body.put("work_date",   workDate.getText().trim());
                body.put("clock_in",    clockIn.getText().trim());
                body.put("clock_out",   clockOut.getText().trim());
                body.put("status",      status.getValue());
                ApiClient.post("/attendance/", body.toString());
                loadData(null);
                empId.clear(); workDate.clear(); clockIn.clear(); clockOut.clear();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to log: " + ex.getMessage()).show();
            }
        });

        TextField filterDate = new TextField();
        filterDate.setPromptText("Filter by date");
        filterDate.setPrefWidth(140);

        Button filter = new Button("Filter");
        filter.setOnAction(e -> {
            String d = filterDate.getText().trim();
            loadData(d.isEmpty() ? null : d);
        });

        HBox toolbar = new HBox(8, empId, workDate, clockIn, clockOut, status, log, filterDate, filter);
        toolbar.setPadding(new Insets(0, 0, 8, 0));
        return toolbar;
    }

    private void loadData(String date) {
        try {
            data.clear();
            String path = (date != null) ? "/attendance/?date=" + date : "/attendance/";
            JSONArray arr = new JSONArray(ApiClient.get(path));
            for (int i = 0; i < arr.length(); i++) data.add(arr.getJSONObject(i));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load attendance").show();
        }
    }
}