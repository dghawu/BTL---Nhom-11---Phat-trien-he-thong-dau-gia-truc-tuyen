package com.example.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main - Entry point của Auction System JavaFX App
 *
 * Cách chạy:
 *   - Maven: mvn javafx:run
 *   - Hoặc run Main.java trực tiếp từ IDE (cần thêm VM option:
 *     --module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml)
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL fxmlUrl = getClass().getResource("/fxml/Login.fxml");
        System.out.println("FXML URL: " + fxmlUrl); // debug xem có null không
        Parent root = FXMLLoader.load(fxmlUrl);

        Scene scene = new Scene(root, 1000, 720);
        primaryStage.setTitle("Auction System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
