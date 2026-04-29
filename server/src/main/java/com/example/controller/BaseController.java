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

    /** Username đang đăng nhập - set khi login thành công */
    protected String currentUsername;
    /** Role hiện tại: ADMIN / SELLER / BIDDER */
    protected String currentRole;

    // ------------------------------------------------------------------ //
    //  Navigation helpers
    // ------------------------------------------------------------------ //

    protected void navigateTo(String fxmlPath, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Truyền session info sang controller mới
            Object ctrl = loader.getController();
            if (ctrl instanceof BaseController bc) {
                bc.currentUsername = this.currentUsername;
                bc.currentRole     = this.currentRole;
            }

            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Lấy Stage từ bất kỳ node nào trong scene */
    protected Stage getStage(javafx.scene.Node node) {
        return (Stage) node.getScene().getWindow();
    }

    /** Hiển thị notification popup với message tùy ý */
    protected void showNotification(Stage owner, String message) {
        com.example.controller.NotificationController.show(owner, message);
    }

    // ------------------------------------------------------------------ //
    //  Common nav handlers - override nếu cần logic đặc biệt
    // ------------------------------------------------------------------ //

    protected void goHome(Stage stage) {
        switch (currentRole == null ? "" : currentRole.toUpperCase()) {
            case "ADMIN"  -> navigateTo("/fxml/HomeAdmin.fxml",  stage);
            case "SELLER" -> navigateTo("/fxml/HomeSeller.fxml", stage);
            default       -> navigateTo("/fxml/HomeBidder.fxml", stage);
        }
    }

    protected void goSettings(Stage stage) {
        navigateTo("/fxml/Settings.fxml", stage);
    }

    protected void goAuctions(Stage stage) {
        navigateTo("/fxml/Auctions.fxml", stage);
    }
}
