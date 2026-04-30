package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller cho màn hình Login (Login.fxml)
 * Xử lý đăng nhập và điều hướng đến Home theo role.
 */
public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //

    /** Nút LOG IN */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // TODO: gọi server để xác thực
        // String role = ServerService.login(username, password);
        // Tạm thời dùng mock để test UI:
        String role = mockLogin(username, password);

        if (role == null) {
            showError("Sai tên đăng nhập hoặc mật khẩu.");
            return;
        }

        navigateToHome(role, username);
    }

    /** Nút "Creat new account" → sang Register */
    @FXML
    private void handleGoRegister() {
        loadScene("/fxml/Register.fxml");
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Điều hướng đến Home tương ứng với role.
     * Truyền username sang controller tiếp theo qua setter.
     */
    private void navigateToHome(String role, String username) {
        try {
            String fxml;
            switch (role.toUpperCase()) {
                case "ADMIN"  -> fxml = "/fxml/HomeAdmin.fxml";
                case "SELLER" -> fxml = "/fxml/HomeSeller.fxml";
                default       -> fxml = "/fxml/HomeBidder.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Truyền username vào Home controller
            Object ctrl = loader.getController();
            if (ctrl instanceof com.example.controller.HomeAdminController)
                ((com.example.controller.HomeAdminController) ctrl).initData(username);
            else if (ctrl instanceof com.example.controller.HomeSellerController)
                ((com.example.controller.HomeSellerController) ctrl).initData(username);
            else if (ctrl instanceof com.example.controller.HomeBidderController)
                ((com.example.controller.HomeBidderController) ctrl).initData(username);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tải màn hình.");
        }
    }

    /** Mock login - xóa khi có server thật */
    private String mockLogin(String username, String password) {
        if (username.startsWith("admin"))  return "ADMIN";
        if (username.startsWith("seller")) return "SELLER";
        if (username.startsWith("bidder")) return "BIDDER";
        return null;
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
