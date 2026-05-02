package com.example.controller;

import com.example.socket.ServerService;
import com.example.socket.SocketClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * LoginController - đã kết nối thật với ServerService.
 * Thay thế file LoginController.java cũ.
 */
public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // Kết nối server nếu chưa kết nối
        if (!SocketClient.getInstance().isConnected()) {
            boolean ok = SocketClient.getInstance().connect();
            if (!ok) {
                showError("Không thể kết nối đến server. Vui lòng thử lại.");
                return;
            }
        }

        // Gọi ServerService
        ServerService.UserResult result = ServerService.login(username, password);

        if (result.success) {
            navigateToHome(result.role, result.username, result.userId);
        } else {
            showError(result.message);
        }
    }

    @FXML
    private void handleGoRegister() {
        loadScene("/fxml/Register.fxml");
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void navigateToHome(String role, String username, int userId) {
        try {
            String fxml = switch (role.toUpperCase()) {
                case "ADMIN"  -> "/fxml/HomeAdmin.fxml";
                case "SELLER" -> "/fxml/HomeSeller.fxml";
                default       -> "/fxml/HomeBidder.fxml";
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Truyền thông tin user vào Home controller
            Object ctrl = loader.getController();
            if (ctrl instanceof HomeAdminController)
                ((HomeAdminController) ctrl).initData(username, userId);
            else if (ctrl instanceof HomeSellerController)
                ((HomeSellerController) ctrl).initData(username, userId);
            else if (ctrl instanceof HomeBidderController)
                ((HomeBidderController) ctrl).initData(username, userId);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tải màn hình.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
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