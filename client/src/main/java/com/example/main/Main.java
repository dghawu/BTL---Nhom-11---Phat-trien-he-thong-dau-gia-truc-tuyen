package com.example.main;

import com.example.socket.SocketClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main - entry point.
 * Kết nối socket khi khởi động, ngắt khi tắt app.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Kết nối đến server ngay khi app mở
        boolean connected = SocketClient.getInstance().connect();
        if (!connected) {
            System.err.println("[App] Cảnh báo: Không kết nối được server. " +
                    "Một số tính năng sẽ không hoạt động.");
            // Vẫn cho mở app, lỗi sẽ hiện ở màn Login
        }

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene scene = new Scene(root, 1280, 720);

        primaryStage.setTitle("Auction System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);

        // Ngắt kết nối socket khi đóng app
        primaryStage.setOnCloseRequest(e -> {
            SocketClient.getInstance().disconnect();
            Platform.exit();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}