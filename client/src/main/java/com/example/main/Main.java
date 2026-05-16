package com.example.main;

import com.example.socket.SocketClient;
import dao.UserDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void init() throws Exception {
        UserDAO userDAO = new UserDAO();
        userDAO.initAdminIfNotExists();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        boolean connected = SocketClient.getInstance().connect();
        if (!connected) {
            System.err.println("[App] Cảnh báo: Không kết nối được server. " +
                    "Một số tính năng sẽ không hoạt động.");
        }

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene scene = new Scene(root, 1280, 720);

        primaryStage.setTitle("Auction System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);

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