package hrms;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import org.json.*;

public class EmployeeView {

    private TableView<JSONObject> table = new TableView<>();
    private ObservableList<JSONObject> data = FXCollections.observableArrayList();
    private ComboBox<String> deptBox = new ComboBox<>();
    private ComboBox<String> jobBox  = new ComboBox<>();
    private JSONArray departments    = new JSONArray();
    private JSONArray jobTitles      = new JSONArray();

    public VBox getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(28, 32, 28, 32));
        root.setStyle("-fx-background-color: #F6FAFD;");

        Label title = new Label("Employees");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A3D63;");

        loadDropdownData();
        setupTable();
        loadData();

        VBox form = buildForm();

        root.getChildren().addAll(title, form, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    private void loadDropdownData() {
        try {
            departments = new JSONArray(ApiClient.get("/departments/"));
            for (int i = 0; i < departments.length(); i++)
                deptBox.getItems().add(departments.getJSONObject(i).getString("name"));
            if (!deptBox.getItems().isEmpty()) deptBox.setValue(deptBox.getItems().get(0));
        } catch (Exception ignored) {}

        try {
            jobTitles = new JSONArray(ApiClient.get("/job_titles/"));
            for (int i = 0; i < jobTitles.length(); i++)
                jobBox.getItems().add(jobTitles.getJSONObject(i).getString("title"));
            if (!jobBox.getItems().isEmpty()) jobBox.setValue(jobBox.getItems().get(0));
        } catch (Exception ignored) {}
    }

    private void setupTable() {
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No employees found."));

        String[] cols = {"id", "first_name", "last_name", "email", "department", "job_title", "status"};
        for (String col : cols) {
            TableColumn<JSONObject, String> tc = new TableColumn<>(col.replace("_", " ").toUpperCase());
            tc.setCellValueFactory(cell -> {
                String val = cell.getValue().optString(col, "");
                return new javafx.beans.property.SimpleStringProperty(val);
            });
            table.getColumns().add(tc);
        }
    }

    private VBox buildForm() {
        TextField firstName = new TextField(); firstName.setPromptText("First name");
        TextField lastName  = new TextField(); lastName.setPromptText("Last name");
        TextField email     = new TextField(); email.setPromptText("Email");
        TextField phone     = new TextField(); phone.setPromptText("Phone");
        TextField hireDate  = new TextField(); hireDate.setPromptText("Hire date (YYYY-MM-DD)");

        deptBox.setPromptText("Department");
        jobBox.setPromptText("Job title");

        for (TextField f : new TextField[]{firstName, lastName, email, phone, hireDate})
            HBox.setHgrow(f, Priority.ALWAYS);

        HBox.setHgrow(deptBox, Priority.ALWAYS);
        HBox.setHgrow(jobBox, Priority.ALWAYS);

        Button add = new Button("Add Employee");
        add.setPrefHeight(36);
        add.setPrefWidth(140);

        add.setOnAction(e -> {
            try {
                int deptId = getDeptId(deptBox.getValue());
                int jobId  = getJobId(jobBox.getValue());

                if (deptId == -1 || jobId == -1) {
                    new Alert(Alert.AlertType.ERROR, "Select a valid department and job title.").show();
                    return;
                }

                JSONObject body = new JSONObject();
                body.put("first_name",    firstName.getText().trim());
                body.put("last_name",     lastName.getText().trim());
                body.put("email",         email.getText().trim());
                body.put("phone",         phone.getText().trim());
                body.put("hire_date",     hireDate.getText().trim());
                body.put("department_id", deptId);
                body.put("job_title_id",  jobId);
                ApiClient.post("/employees/", body.toString());
                loadData();
                firstName.clear(); lastName.clear(); email.clear();
                phone.clear(); hireDate.clear();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to add: " + ex.getMessage()).show();
            }
        });

        HBox row1 = new HBox(10, firstName, lastName, email, phone, hireDate);
        HBox row2 = new HBox(10, deptBox, jobBox, add);
        row2.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox form = new VBox(10, row1, row2);
        form.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-padding: 16;
            -fx-effect: dropshadow(gaussian, rgba(26,61,99,0.06), 10, 0, 0, 2);
        """);
        return form;
    }

    private int getDeptId(String name) {
        for (int i = 0; i < departments.length(); i++) {
            JSONObject d = departments.optJSONObject(i);
            if (d != null && d.getString("name").equals(name)) return d.getInt("id");
        }
        return -1;
    }

    private int getJobId(String title) {
        for (int i = 0; i < jobTitles.length(); i++) {
            JSONObject j = jobTitles.optJSONObject(i);
            if (j != null && j.getString("title").equals(title)) return j.getInt("id");
        }
        return -1;
    }

    private void loadData() {
        try {
            data.clear();
            JSONArray arr = new JSONArray(ApiClient.get("/employees/"));
            for (int i = 0; i < arr.length(); i++) data.add(arr.getJSONObject(i));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load employees").show();
        }
    }
}