package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * SettingsController v3 - thêm nav handlers cho cả 3 role.
 */
public class SettingsController extends BaseController {

    @FXML
    private Label accountTitleLabel;
    @FXML
    private TextField displayUsername;
    @FXML
    private TextField displayRole;
    @FXML
    private TextField displayTimeCreate;
    @FXML
    private TextField newUsernameField;
    @FXML
    private PasswordField passwordForUsernameField;
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
    }
    public void loadUserData() {
        if (currentUsername != null) {
            accountTitleLabel.setText("Your account @" + currentUsername);
            displayUsername.setText(currentUsername);
            displayRole.setText(currentRole);
        }
    }

    // ------------------------------------------------------------------ //
    //  Nav handlers - dùng chung cho cả 3 role
    // ------------------------------------------------------------------ //
    @FXML
    private void handleHome() {
        goHome(getStage(displayUsername));
    }

    @FXML
    private void handleAdminCentre() {
        navigateTo("/fxml/AdminCentre.fxml", getStage(displayUsername));
    }

    @FXML
    private void handleUserReport() {
        navigateTo("/fxml/AdminCentre.fxml", getStage(displayUsername));
    }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(displayUsername));
    }

    @FXML
    private void handleSellerCentre() {
        navigateTo("/fxml/SellerAddProduct.fxml", getStage(displayUsername));
    }

    @FXML
    private void handleBidderCentre() {
        navigateTo("/fxml/BidderCentre.fxml", getStage(displayUsername));
    }

    // ------------------------------------------------------------------ //
    //  Đổi username
    // ------------------------------------------------------------------ //
    @FXML
    private void handleSaveUsername() {
        String newName = newUsernameField.getText().trim();
        String pass = passwordForUsernameField.getText();

        if (newName.isEmpty() || pass.isEmpty()) {
            showMsg("Please fill in all required fields", false);
            return;
        }

        ServerService.UserResult result =
                ServerService.changeUsername(newName, pass);

        if (result.success) {
            currentUsername = newName;
            displayUsername.setText(newName);
            accountTitleLabel.setText("Your account @" + newName);
            newUsernameField.clear();
            passwordForUsernameField.clear();
            showNotification(getStage(displayUsername), "NAME CHANGED SUCCESSFULLY!");
        } else {
            showMsg(result.message.isEmpty() ? "Name change failed." : result.message, false);
        }
    }

    // ------------------------------------------------------------------ //
    //  Đổi mật khẩu
    // ------------------------------------------------------------------ //
    @FXML
    private void handleSavePassword() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            showMsg("VPlease fill in all required fields.", false);
            return;
        }
        if (newPass.length() < 6) {
            showMsg("The new password must be at least 6 characters long.", false);
            return;
        }

        ServerService.UserResult result =
                ServerService.changePassword(oldPass, newPass);

        if (result.success) {
            oldPasswordField.clear();
            newPasswordField.clear();
            showNotification(getStage(displayUsername), "PASSWORD CHANGED SUCCESSFULLY!");
        } else {
            showMsg(result.message.isEmpty() ? "Password change failed." : result.message, false);
        }
    }

    // ------------------------------------------------------------------ //
    //  Logout
    // ------------------------------------------------------------------ //
    @FXML
    private void handleLogout() {
        ServerService.clearToken();
        com.example.socket.SocketClient.getInstance().disconnect();
        currentUsername = null;
        currentRole = null;
        currentUserId = null;
        navigateTo("/fxml/Login.fxml", getStage(displayUsername));
    }

    private void showMsg(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-padding: 10 0 0 0; -fx-text-fill: "
                + (success ? "#1A8A1A;" : "#CC0000;"));
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}