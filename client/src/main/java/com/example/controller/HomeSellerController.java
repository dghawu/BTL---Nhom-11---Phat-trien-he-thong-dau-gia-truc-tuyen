package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * HomeSellerController - HomeSeller.fxml
 */
public class HomeSellerController extends com.example.controller.BaseController {

    @FXML private Label welcomeLabel;

    public void initData(String username, String userId) {
        currentUsername = username;
        currentRole = "SELLER";
        currentUserId = userId;
        welcomeLabel.setText("CHÀO MỪNG Seller @" + username + "\nĐẾN VỚI ABCXYZ");
    }

    @FXML private void handleHome()         { /* đã ở Home */ }
    @FXML private void handleAuctions()     { goAuctions(getStage(welcomeLabel)); }
    @FXML private void handleSellerCentre() { navigateTo("/fxml/SellerAddProduct.fxml", getStage(welcomeLabel)); }
    @FXML private void handleSettings()     { goSettings(getStage(welcomeLabel)); }

    @FXML private void handleGoAuctions()      { handleAuctions(); }
    @FXML private void handleGoSellerCentre()  { handleSellerCentre(); }
    @FXML private void handleGoSettings()      { handleSettings(); }
}
