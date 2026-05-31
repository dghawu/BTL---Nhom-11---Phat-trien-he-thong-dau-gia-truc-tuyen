package com.example.main;

import com.example.controller.ServerSetupController;
import com.example.socket.SocketClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Main — điểm khởi động ứng dụng client.
 * <p>
 * ── Thay đổi so với bản gốc ──────────────────────────────────────────────
 * 1. Hiển thị màn hình ServerSetup trước khi Login (nếu chưa có config
 * hoặc config là localhost).
 * 2. Sau khi kết nối thành công → chuyển sang Login.
 * 3. Bỏ UserDAO.initAdminIfNotExists() ở client (việc này thuộc server).
 */
public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        // ── Load custom fonts (phải gọi trước khi load bất kỳ scene nào) ──
        Font.loadFont(getClass().getResourceAsStream("/fonts/ZTBrosOskon90s-Regular.otf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/ZTBrosOskon90s-Italic.otf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond-Italic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond-BoldItalic.ttf"), 14);

        stage.setTitle("Auction System");
        stage.setMinWidth(1000);
        stage.setMinHeight(600);

        stage.setOnCloseRequest(e -> {
            SocketClient.getInstance().disconnect();
            Platform.exit();
        });

        // Luôn hiển thị màn hình cài đặt server trước
        showServerSetup();

        stage.show();
    }

    // ── Màn hình cài đặt server ─────────────────────────────────────────

    private void showServerSetup() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ServerSetup.fxml"));
        Parent root = loader.load();

        ServerSetupController ctrl = loader.getController();
        // Khi kết nối thành công → chuyển sang Login
        ctrl.setOnConnectSuccess(() -> {
            try {
                showLogin();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        primaryStage.setScene(new Scene(root, 1280, 720));
    }

    // ── Màn hình Login ──────────────────────────────────────────────────

    private void showLogin() throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/Login.fxml"));
        primaryStage.setScene(new Scene(root, 1280, 720));
    }

    public static void main(String[] args) {
        launch(args);
    }
}