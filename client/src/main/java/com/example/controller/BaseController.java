package com.example.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * BaseController - lớp cha cho tất cả controller.
 * Chứa các method điều hướng (navigate) và helper dùng chung.
 */
public abstract class BaseController {

    /**
     * Username đang đăng nhập - set khi login thành công
     */
    protected String currentUsername;
    /**
     * Role hiện tại: ADMIN / SELLER / BIDDER
     */
    protected String currentRole;
    protected String currentUserId;

    // ------------------------------------------------------------------ //
    //  Navigation
    // ------------------------------------------------------------------ //
    protected void navigateTo(String fxmlPath, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            //Set controller TRƯỚC khi load
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof BaseController bc) {
                bc.currentUsername = this.currentUsername;
                bc.currentRole = this.currentRole;
                bc.currentUserId = this.currentUserId;

                // cập nhật UI sau khi truyền dữ liệu
                if (ctrl instanceof HomeAdminController homeAdmin) {
                    homeAdmin.initData(currentUsername, currentUserId);
                }

                if (ctrl instanceof HomeSellerController homeSeller) {
                    homeSeller.initData(currentUsername, currentUserId);
                }

                if (ctrl instanceof HomeBidderController homeBidder) {
                    homeBidder.initData(currentUsername, currentUserId);
                }

                //Gọi onReady() sau khi set xong
                bc.onReady();
            }
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onReady() {
    }

    protected Stage getStage(javafx.scene.Node node) {
        return (Stage) node.getScene().getWindow();
    }

    protected void showNotification(Stage owner, String message) {
        NotificationController.show(owner, message);
    }

    // ------------------------------------------------------------------ //
    //  Common navigation
    // ------------------------------------------------------------------ //
    protected void goHome(Stage stage) {
        switch (currentRole == null ? "" : currentRole.toUpperCase()) {
            case "ADMIN" -> navigateTo("/fxml/HomeAdmin.fxml", stage);
            case "SELLER" -> navigateTo("/fxml/HomeSeller.fxml", stage);
            default -> navigateTo("/fxml/HomeBidder.fxml", stage);
        }
    }

    /**
     * Settings theo đúng role
     */
    protected void goSettings(Stage stage) {
        switch (currentRole == null ? "" : currentRole.toUpperCase()) {
            case "ADMIN" -> navigateTo("/fxml/SettingsAdmin.fxml", stage);
            case "SELLER" -> navigateTo("/fxml/SettingsSeller.fxml", stage);
            default -> navigateTo("/fxml/SettingsBidder.fxml", stage);
        }
    }

    protected void goAuctions(Stage stage) {
        navigateTo("/fxml/Auctions.fxml", stage);
    }
}