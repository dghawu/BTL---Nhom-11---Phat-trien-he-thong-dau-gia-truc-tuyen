package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * HomeBidderController - HomeBidder.fxml
 */
public class HomeBidderController extends com.example.controller.BaseController {

    @FXML
    private Label welcomeLabel;

    public void initData(String username, String userId) {
        currentUsername = username;
        currentRole = "BIDDER";
        currentUserId = userId;
        welcomeLabel.setText("WELCOME BIDDER @" + username + " TO ABCXYZ");
    }

    @FXML
    private void handleHome() { /* đã ở Home */ }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(welcomeLabel));
    }

    @FXML
    private void handleBidderCentre() {
        navigateTo("/fxml/BidderCentre.fxml", getStage(welcomeLabel));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(welcomeLabel));
    }

    @FXML
    private void handleGoAuctions() {
        handleAuctions();
    }

    @FXML
    private void handleGoBidderCentre() {
        handleBidderCentre();
    }

    @FXML
    private void handleGoSettings() {
        handleSettings();
    }
}
