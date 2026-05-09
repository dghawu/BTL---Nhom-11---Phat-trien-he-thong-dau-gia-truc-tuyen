package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * SettingsController - Settings.fxml
 * Dùng chung cho Admin, Seller, Bidder.
 */
public class SettingsController extends com.example.controller.BaseController {

    @FXML private Label         accountTitleLabel;
    @FXML private TextField     displayUsername;
    @FXML private TextField     displayRole;
    @FXML private TextField     displayTimeCreate;

    @FXML private TextField     newUsernameField;
    @FXML private PasswordField passwordForUsernameField;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;

    @FXML private Label         messageLabel;

    @FXML
    public void initialize() {
        // Điền thông tin từ session (set bởi BaseController.navigateTo)
        if (currentUsername != null) {
            accountTitleLabel.setText("Your account @" + currentUsername);
            displayUsername.setText(currentUsername);
            displayRole.setText(currentRole);
            displayTimeCreate.setText("-- / -- / ----");
            // TODO: lấy thời gian tạo tài khoản từ server
        }
    }

    // ------------------------------------------------------------------ //
    //  Nav
    // ------------------------------------------------------------------ //
    @FXML private void handleHome() { goHome(getStage(displayUsername)); }

    // ------------------------------------------------------------------ //
    //  Change Username
    // ------------------------------------------------------------------ //
    @FXML
    private void handleSaveUsername() {
        String newName = newUsernameField.getText().trim();
        String pass    = passwordForUsernameField.getText();

        if (newName.isEmpty() || pass.isEmpty()) {
            showMsg("Vui lòng điền đầy đủ.", false);
            return;
        }

        // TODO: ServerService.changeUsername(currentUsername, newName, pass);
        // Nếu thành công:
        currentUsername = newName;
        displayUsername.setText(newName);
        accountTitleLabel.setText("Your account @" + newName);
        newUsernameField.clear();
        passwordForUsernameField.clear();

        showNotification(getStage(displayUsername), "ĐỔI TÊN THÀNH CÔNG!");
    }

    // ------------------------------------------------------------------ //
    //  Change Password
    // ------------------------------------------------------------------ //
    @FXML
    private void handleSavePassword() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            showMsg("Vui lòng điền đầy đủ.", false);
            return;
        }
        if (newPass.length() < 6) {
            showMsg("Mật khẩu mới phải có ít nhất 6 ký tự.", false);
            return;
        }

        // TODO: ServerService.changePassword(currentUsername, oldPass, newPass);
        oldPasswordField.clear();
        newPasswordField.clear();
        showNotification(getStage(displayUsername), "ĐỔI MẬT KHẨU THÀNH CÔNG!");
    }

    // ------------------------------------------------------------------ //
    //  Logout
    // ------------------------------------------------------------------ //
    @FXML
    private void handleLogout() {
        // TODO: ServerService.logout(currentUsername);
        currentUsername = null;
        currentRole = null;
        navigateTo("/fxml/Login.fxml", getStage(displayUsername));
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //
    private void showMsg(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-padding: 10 0 0 0; -fx-text-fill: "
                + (success ? "#1A8A1A;" : "#CC0000;"));
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
