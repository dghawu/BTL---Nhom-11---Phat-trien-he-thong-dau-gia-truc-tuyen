package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * HomeAdminController - HomeAdmin.fxml
 */
public class HomeAdminController extends com.example.controller.BaseController {

    @FXML
    private Label welcomeLabel;

    /**
     * Gọi sau khi load để set tên user
     */
    public void initData(String username, String userId) {
        currentUsername = username;
        currentRole = "ADMIN";
        currentUserId = userId;
        welcomeLabel.setText("WELCOME ADMIN @" + username);
    }

    @FXML
    private void handleHome() { /* đã ở Home */ }

    @FXML
    private void handleAdminCentre() {
        navigateTo("/fxml/AdminCentre.fxml", getStage(welcomeLabel));
    }

    @FXML
    private void handleUserReport() {
        navigateTo("/fxml/AdminCentre.fxml", getStage(welcomeLabel));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(welcomeLabel));
    }
}
