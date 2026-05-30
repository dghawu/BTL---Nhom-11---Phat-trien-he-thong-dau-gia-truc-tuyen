package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller cho Register.fxml
 * Bước 1: Nhập Name, Email, Password, Confirm Password → Submit
 * Bước 2: Hiện ComboBox chọn Role (Bidder / Seller) → Submit lần 2
 */
public class RegisterController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label roleLabelVisibility;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label errorLabel;

    private boolean waitingForRole = false;
    private String savedName;
    private String savedEmail;
    private String savedPassword;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Bidder", "Seller");
    }

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //

    @FXML
    private void handleSubmit() {
        if (!waitingForRole) {
            // --- Bước 1: validate form ---
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmPasswordField.getText();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showError("Please complete all required fields.");
                return;
            }
            if (!password.equals(confirm)) {
                showError("Passwords do not match.");
                return;
            }

            // Lưu lại để dùng ở bước 2
            savedName = name;
            savedEmail = email;
            savedPassword = password;

            // Hiện ComboBox chọn role
            roleLabelVisibility.setVisible(true);
            roleLabelVisibility.setManaged(true);
            roleComboBox.setVisible(true);
            roleComboBox.setManaged(true);
            hideError();
            waitingForRole = true;

        } else {
            // --- Bước 2: chọn role và đăng ký ---
            String role = roleComboBox.getValue();
            if (role == null) {
                showError("Please select a role.");
                return;
            }

            // Gọi server đăng ký
            ServerService.UserResult result = ServerService.register(
                    savedName, savedPassword, role.toUpperCase()
            );

            if (result.success) {
                com.example.controller.NotificationController.show(
                        (Stage) nameField.getScene().getWindow(),
                        "SIGN UP SUCCESSFUL!"
                );
                handleGoLogin();
            } else {
                showError(result.message.isEmpty() ? "Sign up failed." : result.message);
            }
        }
    }

    @FXML
    private void handleGoLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
