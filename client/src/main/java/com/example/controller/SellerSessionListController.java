package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * SellerSessionListController - SellerSessionList.fxml
 */
public class SellerSessionListController extends com.example.controller.BaseController {

    @FXML
    private FlowPane sessionGrid;

    @FXML
    public void initialize() {
    }

    @Override
    protected void onReady() {
        loadSessions();
    }

    private void loadSessions() {
        sessionGrid.getChildren().clear();

        org.json.JSONArray sessions = com.example.socket.ServerService.getMySessions();
        if (sessions == null) return;

        for (int i = 0; i < sessions.length(); i++) {
            org.json.JSONObject s = sessions.getJSONObject(i);
            sessionGrid.getChildren().add(buildCard(s));
        }
    }

    private VBox buildCard(org.json.JSONObject s) {
        String id = s.getString("id");
        String ten = s.getString("itemName");
        String startTime = s.getString("startTime");
        String status = s.getString("status");
        String gia = String.format("%,.0fđ", s.getDouble("startPrice"));
        String category = s.getString("category");

        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(280);

        Label title = new Label("SẢN PHẨM " + ten.toUpperCase());
        title.getStyleClass().add("product-card-title");
        title.setMaxWidth(Double.MAX_VALUE);

        Pane img = new Pane();
        img.getStyleClass().add("product-card-image");
        img.setPrefHeight(160);

        VBox info = new VBox(3);
        info.getStyleClass().add("product-card-info");
        info.getChildren().addAll(
                new Label("Tên sản phẩm: " + ten),
                new Label("Thời gian mở: " + startTime),
                new Label("Trạng thái: " + status),
                new Label("Giá khởi điểm: " + gia),
                new Label("Phân loại: " + category)
        );

        Hyperlink link = new Hyperlink("Xem chi tiết");
        link.getStyleClass().add("link-text");
        link.setOnAction(e -> openDetail(s));
        info.getChildren().add(link);

        card.getChildren().addAll(title, img, info);
        return card;
    }

    private void openDetail(org.json.JSONObject s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SellerSessionDetail.fxml"));
            Parent root = loader.load();
            SellerSessionDetailController ctrl = loader.getController();
            ctrl.currentUsername = currentUsername;
            ctrl.currentUserId = currentUserId;
            ctrl.currentRole = currentRole;
            ctrl.initData(
                    s.getString("itemName"),
                    s.getString("startTime"),
                    s.getString("status"),
                    String.format("%,.0fđ", s.getDouble("startPrice"))
            );
            getStage(sessionGrid).setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHome() {
        goHome(getStage(sessionGrid));
    }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(sessionGrid));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(sessionGrid));
    }

    @FXML
    private void handleThemSanPham() {
        navigateTo("/fxml/SellerAddProduct.fxml", getStage(sessionGrid));
    }

    @FXML
    private void handleXemSanPham() {
        navigateTo("/fxml/SellerProductList.fxml", getStage(sessionGrid));
    }

    @FXML
    private void handleTaoPhien() {
        navigateTo("/fxml/SellerCreateSession.fxml", getStage(sessionGrid));
    }

    @FXML
    private void handleXemPhien() { /* đã ở đây */ }

    @FXML
    private void handleEdit() { /* TODO */ }

    @FXML
    private void handleSave() { /* TODO */ }
}
