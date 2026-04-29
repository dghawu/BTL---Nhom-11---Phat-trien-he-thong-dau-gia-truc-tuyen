package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller cho Register.fxml
 * Bước 1: Nhập Name, Email, Password, Confirm Password → Submit
 * Bước 2: Hiện ComboBox chọn Role (Bidder / Seller) → Submit lần 2
 */
public class RegisterController {

    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         roleLabelVisibility;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label         errorLabel;

    private boolean waitingForRole = false;

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
            String name     = nameField.getText().trim();
            String email    = emailField.getText().trim();
            String password = passwordField.getText();
            String confirm  = confirmPasswordField.getText();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showError("Vui lòng điền đầy đủ thông tin.");
                return;
            }
            if (!password.equals(confirm)) {
                showError("Mật khẩu xác nhận không khớp.");
                return;
            }

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
                showError("Vui lòng chọn vai trò.");
                return;
            }

            // TODO: gọi server đăng ký tài khoản
            // ServerService.register(name, email, password, role);

            // Sau khi đăng ký thành công → về Login
            com.example.controller.NotificationController.show(
                    (Stage) nameField.getScene().getWindow(),
                    "ĐĂNG KÝ THÀNH CÔNG!"
            );
            handleGoLogin();
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
