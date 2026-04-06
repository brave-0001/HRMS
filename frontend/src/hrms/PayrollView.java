package hrms;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import org.json.*;

public class PayrollView {

    private TableView<JSONObject> table = new TableView<>();
    private ObservableList<JSONObject> data = FXCollections.observableArrayList();

    public VBox getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #F6FAFD;");

        Label title = new Label("Payroll");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A3D63;");

        setupTable();
        loadData();

        HBox form = buildForm();

        root.getChildren().addAll(title, form, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    private void setupTable() {
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No payroll records found."));

        String[][] cols = {
            {"employee_id", "EMP ID"},
            {"first_name",  "FIRST NAME"},
            {"last_name",   "LAST NAME"},
            {"period_start","PERIOD START"},
            {"period_end",  "PERIOD END"},
            {"gross_pay",   "GROSS"},
            {"deductions",  "DEDUCTIONS"},
            {"net_pay",     "NET PAY"},
            {"state",       "STATUS"}
        };

        for (String[] col : cols) {
            TableColumn<JSONObject, String> tc = new TableColumn<>(col[1]);
            String key = col[0];
            tc.setCellValueFactory(cell -> {
                String val = cell.getValue().optString(key, "");
                return new javafx.beans.property.SimpleStringProperty(val);
            });
            table.getColumns().add(tc);
        }
    }

    private HBox buildForm() {
        TextField empId       = new TextField(); empId.setPromptText("Employee ID");
        TextField periodStart = new TextField(); periodStart.setPromptText("Period start (YYYY-MM-DD)");
        TextField periodEnd   = new TextField(); periodEnd.setPromptText("Period end (YYYY-MM-DD)");

        empId.setPrefWidth(120);
        periodStart.setPrefWidth(180);
        periodEnd.setPrefWidth(180);

        Button run = new Button("Run Payroll");
        run.setOnAction(e -> {
            String id    = empId.getText().trim();
            String start = periodStart.getText().trim();
            String end   = periodEnd.getText().trim();

            if (id.isEmpty() || start.isEmpty() || end.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "All three fields are required.").show();
                return;
            }

            try {
                JSONObject body = new JSONObject();
                body.put("employee_id",  Integer.parseInt(id));
                body.put("period_start", start);
                body.put("period_end",   end);

                String response = ApiClient.post("/payroll/run", body.toString());
                JSONObject result = new JSONObject(response);

                if (result.has("error")) {
                    new Alert(Alert.AlertType.ERROR, result.getString("error")).show();
                    return;
                }

                String summary = String.format(
                    "Payroll processed.\n\nGross:      %.2f\nDeductions: %.2f\nNet Pay:    %.2f",
                    result.getDouble("gross"),
                    result.getDouble("deductions"),
                    result.getDouble("net")
                );
                new Alert(Alert.AlertType.INFORMATION, summary).show();
                loadData();
                empId.clear(); periodStart.clear(); periodEnd.clear();

            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Employee ID must be a number.").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed: " + ex.getMessage()).show();
            }
        });

        HBox form = new HBox(8, empId, periodStart, periodEnd, run);
        form.setPadding(new Insets(0, 0, 8, 0));
        return form;
    }

    private void loadData() {
        try {
            data.clear();
            JSONArray arr = new JSONArray(ApiClient.get("/payroll/"));
            for (int i = 0; i < arr.length(); i++) data.add(arr.getJSONObject(i));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load payroll records.").show();
        }
    }
}