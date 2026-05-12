package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * SettingsController v3 - thêm nav handlers cho cả 3 role.
 */
public class SettingsController extends BaseController {

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
        if (currentUsername != null) {
            accountTitleLabel.setText("Your account @" + currentUsername);
            displayUsername.setText(currentUsername);
            displayRole.setText(currentRole);
            displayTimeCreate.setText("-- / -- / ----");
        }
    }

    // ------------------------------------------------------------------ //
    //  Nav handlers - dùng chung cho cả 3 role
    // ------------------------------------------------------------------ //
    @FXML private void handleHome()         { goHome(getStage(displayUsername)); }
    @FXML private void handleAdminCentre()  { navigateTo("/fxml/AdminCentre.fxml",      getStage(displayUsername)); }
    @FXML private void handleUserReport()   { navigateTo("/fxml/AdminCentre.fxml",      getStage(displayUsername)); }
    @FXML private void handleAuctions()     { goAuctions(getStage(displayUsername)); }
    @FXML private void handleSellerCentre() { navigateTo("/fxml/SellerAddProduct.fxml", getStage(displayUsername)); }
    @FXML private void handleBidderCentre() { navigateTo("/fxml/BidderCentre.fxml",     getStage(displayUsername)); }

    // ------------------------------------------------------------------ //
    //  Đổi username
    // ------------------------------------------------------------------ //
    @FXML
    private void handleSaveUsername() {
        String newName = newUsernameField.getText().trim();
        String pass    = passwordForUsernameField.getText();

        if (newName.isEmpty() || pass.isEmpty()) {
            showMsg("Vui lòng điền đầy đủ.", false);
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
            showNotification(getStage(displayUsername), "ĐỔI TÊN THÀNH CÔNG!");
        } else {
            showMsg(result.message.isEmpty() ? "Đổi tên thất bại." : result.message, false);
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
            showMsg("Vui lòng điền đầy đủ.", false);
            return;
        }
        if (newPass.length() < 6) {
            showMsg("Mật khẩu mới phải có ít nhất 6 ký tự.", false);
            return;
        }

        ServerService.UserResult result =
                ServerService.changePassword(oldPass, newPass);

        if (result.success) {
            oldPasswordField.clear();
            newPasswordField.clear();
            showNotification(getStage(displayUsername), "ĐỔI MẬT KHẨU THÀNH CÔNG!");
        } else {
            showMsg(result.message.isEmpty() ? "Đổi mật khẩu thất bại." : result.message, false);
        }
    }

    // ------------------------------------------------------------------ //
    //  Logout
    // ------------------------------------------------------------------ //
    @FXML
    private void handleLogout() {
        ServerService.clearToken();  // ✅ Thêm dòng này
        com.example.socket.SocketClient.getInstance().disconnect();
        currentUsername = null;
        currentRole     = null;
        currentUserId   = null;
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