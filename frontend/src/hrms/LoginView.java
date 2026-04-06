package hrms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView {

    private Runnable onLoginSuccess;
    private VBox root;

    public LoginView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public VBox getView() {
        root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F6FAFD;");
        showLoginForm();
        return root;
    }

    private void showLoginForm() {
        root.getChildren().clear();

        VBox card = new VBox(20);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(40, 48, 40, 48));
        card.setMaxWidth(420);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 20;
            -fx-effect: dropshadow(gaussian, rgba(26,61,99,0.10), 24, 0, 0, 6);
        """);

        Label appName = new Label("HRMS");
        appName.setStyle("-fx-font-size: 13px; -fx-text-fill: #7A99B8; -fx-font-weight: bold;");

        Label title = new Label("Welcome back.");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1A3D63;");

        Label subtitle = new Label("Sign in to continue.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #7A99B8;");

        VBox emailGroup = fieldGroup("Email", false);
        TextField emailField = (TextField) emailGroup.getChildren().get(1);

        VBox passGroup = fieldGroup("Password", true);
        PasswordField passField = (PasswordField) passGroup.getChildren().get(1);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #C0392B; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button loginBtn = new Button("Sign in");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(44);
        loginBtn.setStyle("""
            -fx-background-color: #1A3D63;
            -fx-text-fill: white;
            -fx-background-radius: 10;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """);

        Button forgotBtn = new Button("Forgot password?");
        forgotBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #7A99B8;
            -fx-font-size: 12px;
            -fx-cursor: hand;
            -fx-padding: 0;
        """);

        loginBtn.setOnAction(e -> handleLogin(
            emailField.getText().trim(),
            passField.getText().trim(),
            errorLabel
        ));

        passField.setOnAction(e -> loginBtn.fire());
        forgotBtn.setOnAction(e -> showForgotForm());

        card.getChildren().addAll(appName, title, subtitle, emailGroup, passGroup, errorLabel, loginBtn, forgotBtn);
        root.getChildren().add(card);
    }

    private void showForgotForm() {
        root.getChildren().clear();

        VBox card = new VBox(20);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(40, 48, 40, 48));
        card.setMaxWidth(420);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 20;
            -fx-effect: dropshadow(gaussian, rgba(26,61,99,0.10), 24, 0, 0, 6);
        """);

        Label appName = new Label("HRMS");
        appName.setStyle("-fx-font-size: 13px; -fx-text-fill: #7A99B8; -fx-font-weight: bold;");

        Label title = new Label("Reset password.");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1A3D63;");

        Label subtitle = new Label("Enter your email and a new password.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #7A99B8;");

        VBox emailGroup = fieldGroup("Email", false);
        TextField emailField = (TextField) emailGroup.getChildren().get(1);

        VBox passGroup = fieldGroup("New password", true);
        PasswordField passField = (PasswordField) passGroup.getChildren().get(1);

        VBox confirmGroup = fieldGroup("Confirm password", true);
        PasswordField confirmField = (PasswordField) confirmGroup.getChildren().get(1);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #C0392B; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button resetBtn = new Button("Reset password");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setPrefHeight(44);
        resetBtn.setStyle("""
            -fx-background-color: #1A3D63;
            -fx-text-fill: white;
            -fx-background-radius: 10;
            -fx-font-size: 14px;
            -fx-cursor: hand;
        """);

        Button backBtn = new Button("Back to sign in");
        backBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #7A99B8;
            -fx-font-size: 12px;
            -fx-cursor: hand;
            -fx-padding: 0;
        """);

        resetBtn.setOnAction(e -> {
            String email    = emailField.getText().trim();
            String password = passField.getText().trim();
            String confirm  = confirmField.getText().trim();

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }
            if (!password.equals(confirm)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }
            if (password.length() < 6) {
                errorLabel.setText("Password must be at least 6 characters.");
                return;
            }

            try {
                org.json.JSONObject body = new org.json.JSONObject();
                body.put("email", email);
                body.put("new_password", password);

                String response = ApiClient.post("/auth/reset-password", body.toString());
                org.json.JSONObject result = new org.json.JSONObject(response);

                if (result.has("error")) {
                    errorLabel.setText(result.getString("error"));
                    return;
                }

                errorLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 12px;");
                errorLabel.setText("Password reset. You can now sign in.");
                resetBtn.setDisable(true);

            } catch (Exception ex) {
                errorLabel.setText("Could not connect to server.");
            }
        });

        backBtn.setOnAction(e -> showLoginForm());

        card.getChildren().addAll(appName, title, subtitle, emailGroup, passGroup, confirmGroup, errorLabel, resetBtn, backBtn);
        root.getChildren().add(card);
    }

    private VBox fieldGroup(String labelText, boolean isPassword) {
        VBox group = new VBox(6);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #1A3D63; -fx-font-weight: bold;");

        if (isPassword) {
            PasswordField field = new PasswordField();
            field.setPromptText("Enter " + labelText.toLowerCase());
            field.setPrefHeight(40);
            group.getChildren().addAll(label, field);
        } else {
            TextField field = new TextField();
            field.setPromptText("Enter your email");
            field.setPrefHeight(40);
            group.getChildren().addAll(label, field);
        }
        return group;
    }

    private void handleLogin(String email, String password, Label errorLabel) {
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }
        try {
            org.json.JSONObject body = new org.json.JSONObject();
            body.put("email", email);
            body.put("password", password);

            String response = ApiClient.post("/auth/login", body.toString());
            org.json.JSONObject result = new org.json.JSONObject(response);

            if (result.has("error")) {
                errorLabel.setText(result.getString("error"));
                return;
            }

            ApiClient.setToken(result.getString("token"));
            Session.name = result.getString("name");
            Session.role = result.getString("role");

            onLoginSuccess.run();

        } catch (Exception ex) {
            errorLabel.setText("Could not connect to server.");
        }
    }
}