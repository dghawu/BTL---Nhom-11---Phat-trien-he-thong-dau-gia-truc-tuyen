package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
            // Lấy attributes từ JSON
            String attr1 = s.optString("attr1", "");
            String attr2 = s.optString("attr2", "");
            sessionGrid.getChildren().add(buildCard(s, attr1, attr2));
        }
    }

    private VBox buildCard(org.json.JSONObject s, String attr1, String attr2) {
        String id = s.getString("id");
        String ten = s.getString("itemName");
        String startTime = s.getString("startTime");
        String status = s.getString("status");
        String gia = String.format("%,.0fđ", s.getDouble("startPrice"));
        String category = s.getString("category");
        String imageBase64 = s.optString("itemImage", "");

        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(280);

        Label title = new Label(ten);
        title.getStyleClass().add("product-card-title");
        title.setMaxWidth(Double.MAX_VALUE);

        Pane img = new Pane();
        img.getStyleClass().add("product-card-image");
        img.setPrefHeight(160);

        VBox info = new VBox(4);
        info.getStyleClass().add("product-card-info");
        info.getChildren().addAll(
                new Label("Product name: " + ten),
                new Label("Opening time: " + startTime),
                new Label("Status: " + status),
                new Label("Starting price: " + gia),
                new Label("Category: " + category)
        );

        // Thêm attributes vào card
        if (!attr1.isEmpty() || !attr2.isEmpty()) {
            VBox attrBox = new VBox(3);
            attrBox.setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 5; " +
                    "-fx-background-color: #f9f9f9; -fx-background-radius: 5;");

            switch (category.toUpperCase()) {
                case "FASHION" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("   Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("   Size: " + attr2));
                }
                case "ART" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("   Artist: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("   Medium: " + attr2));
                }
                case "VEHICLE" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("   Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("   Mileage: " + attr2 + " km"));
                }
                case "ELECTRONICS" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("   Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("   Warranty: " + attr2 + " tháng"));
                }
                default -> {
                    // ETC hoặc category khác không có attributes đặc biệt
                }
            }

            if (!attrBox.getChildren().isEmpty()) {
                info.getChildren().add(attrBox);
            }
        }

        Hyperlink link = new Hyperlink("View details");
        link.setOnAction(e -> openDetail(s, attr1, attr2));
        link.getStyleClass().add("link-text");

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(imageBase64);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(decodedBytes);
                javafx.scene.image.Image imageObj = new javafx.scene.image.Image(bais);
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(imageObj);
                imageView.setFitWidth(280);
                imageView.setFitHeight(160);
                imageView.setPreserveRatio(true);
                img.getChildren().setAll(imageView);
            } catch (Exception e) {
                System.err.println("[SellerSessionListController] Image decode error: " + e.getMessage());
            }
        }

        info.getChildren().add(link);

        card.getChildren().addAll(title, img, info);
        return card;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void openDetail(org.json.JSONObject s, String attr1, String attr2) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SellerSessionDetail.fxml"));
            Parent root = loader.load();
            SellerSessionDetailController ctrl = loader.getController();
            ctrl.currentUsername = currentUsername;
            ctrl.currentUserId = currentUserId;
            ctrl.currentRole = currentRole;
            ctrl.initData(
                    s.optString("id", "---"),
                    s.getString("itemName"),
                    s.getString("startTime"),
                    s.optString("endTime", "---"),
                    String.format("%,.0fđ", s.getDouble("startPrice")),
                    String.format("%,.0f", s.optDouble("stepPrice", 0)),
                    s.getString("status"),
                    s.optString("itemImage", ""),
                    s.optString("category", ""),
                    attr1,
                    attr2,
                    s.optString("currentWinner", ""),
                    String.format("%,.0f đ", s.optDouble("currentPrice", 0))
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