package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * NotificationController - NotificationPopup.fxml
 * <p>
 * Dùng chung cho tất cả thông báo trong app.
 * Cách dùng:
 * NotificationController.show(ownerStage, "PHIÊN ĐẤU GIÁ ĐÃ KẾT THÚC!!!");
 */
public class NotificationController {

    @FXML
    private Label messageLabel;

    private Stage popupStage;

    // ------------------------------------------------------------------ //
    //  Static factory - gọi từ bất kỳ controller nào
    // ------------------------------------------------------------------ //

    /**
     * Hiển thị popup thông báo.
     *
     * @param owner   Stage cha (để căn giữa popup)
     * @param message Nội dung thông báo (VIẾT HOA như wireframe)
     */
    public static void show(Stage owner, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NotificationController.class.getResource("/fxml/NotificationPopup.fxml")
            );
            Parent root = loader.load();
            NotificationController ctrl = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) stage.initOwner(owner);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            ctrl.popupStage = stage;
            ctrl.messageLabel.setText(message);

            // Tự động đóng sau 3 giây
            javafx.animation.PauseTransition delay =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            delay.setOnFinished(e -> stage.close());
            delay.play();

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ //
    //  FXML handler - nút X trên popup
    // ------------------------------------------------------------------ //
    @FXML
    private void handleClose() {
        if (popupStage != null) popupStage.close();
    }
}
