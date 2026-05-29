package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;

/**
 * SellerProductListController - SellerProductList.fxml
 * Hiển thị danh sách sản phẩm dưới dạng card grid.
 * Mỗi card có link "Xem chi tiết" → mở SellerProductDetail.
 */
public class SellerProductListController extends com.example.controller.BaseController {

    @FXML
    private FlowPane productGrid;

    @FXML
    public void initialize() {
    }

    @Override
    public void onReady() {
        loadProducts();
    }

    private void loadProducts() {
        productGrid.getChildren().clear();

        org.json.JSONArray items = ServerService.getMyItems();
        if (items == null) return;

        for (int i = 0; i < items.length(); i++) {
            org.json.JSONObject item = items.getJSONObject(i);
            String id = item.getString("id");
            String name = item.getString("name");
            String category = item.optString("type", "N/A");
            String description = item.optString("description", "");
            String status = item.getString("status");
            double price = item.getDouble("startPrice");
            String priceStr = String.format("%,.0fđ", price);
            String imageBase64 = item.optString("image", "");


            String attr1 = item.optString("attr1", "");
            String attr2 = item.optString("attr2", "");


            productGrid.getChildren().add(buildMockCard(name, id, priceStr, status, category,
                    description, imageBase64, attr1, attr2));
        }
    }

    private VBox buildMockCard(String ten, String id, String gia, String tinhTrang,
                               String category, String description, String imageBase64,
                               String attr1, String attr2) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(280);

        Label title = new Label("PRODUCT " + ten.toUpperCase());
        title.getStyleClass().add("product-card-title");
        title.setMaxWidth(Double.MAX_VALUE);

        Pane img = new Pane();
        img.getStyleClass().add("product-card-image");
        img.setPrefHeight(160);

        VBox info = new VBox(4);
        info.getStyleClass().add("product-card-info");
        info.getChildren().addAll(
                new Label("Product name: " + ten),
                new Label("Product id: " + id),
                new Label("Starting price: " + gia),
                new Label("Status: " + tinhTrang),
                new Label("Category: " + category)
        );


        if (!attr1.isEmpty() || !attr2.isEmpty()) {
            VBox attrBox = new VBox(3);
            attrBox.setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 5; " +
                    "-fx-background-color: #f9f9f9; -fx-background-radius: 5;");

            switch (category.toUpperCase()) {
                case "FASHION" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  Size: " + attr2));
                }
                case "ART" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  Artist: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  Medium: " + attr2));
                }
                case "VEHICLE" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  Mileage: " + attr2 + " km"));
                }
                case "ELECTRONICS" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  Warranty: " + attr2 + " tháng"));
                }
            }

            if (!attrBox.getChildren().isEmpty()) {
                info.getChildren().add(attrBox);
            }
        }

        Hyperlink link = new Hyperlink("View details");
        link.getStyleClass().add("link-text");
        link.setStyle("-fx-text-fill: #0044CC;");
        link.setOnAction(e -> openDetail(id, ten, category, gia, description, tinhTrang, imageBase64, attr1, attr2));

        Button cancelBtn = new Button("Cancel product");

        info.getChildren().addAll(link, cancelBtn);

        cancelBtn.getStyleClass().add("btn-danger");

        cancelBtn.setOnAction(e -> {

            String st = tinhTrang.toUpperCase();

            if (st.equals("IN_AUCTION") || st.equals("SOLD")) {

                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setTitle("Unable to cancel");
                alert.setHeaderText(null);
                alert.setContentText("This product cannot be cancelled.");

                alert.showAndWait();

                return;
            }

            boolean ok = ServerService.cancelItem(id);

            if (ok) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);

                alert.setTitle("Successfully");
                alert.setHeaderText(null);
                alert.setContentText("Product cancelled.");

                alert.showAndWait();

                loadProducts();

            } else {

                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("This product has been cancelled.");

                alert.showAndWait();
            }
        });

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
                System.err.println("[SellerProductListController] Image decode error: " + e.getMessage());
            }
        }

        card.getChildren().addAll(title, img, info);
        return card;
    }

    private void openDetail(String id, String ten, String category, String gia,
                            String moTa, String tinhTrang, String imageBase64,
                            String attr1, String attr2) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SellerProductDetail.fxml"));
            Parent root = loader.load();
            SellerProductDetailController ctrl = loader.getController();
            ctrl.currentUsername = currentUsername;
            ctrl.currentRole = currentRole;
            ctrl.currentUserId = currentUserId;
            // Truyền đầy đủ 9 tham số (có attr1, attr2)
            ctrl.initData(id, ten, category, gia, moTa, tinhTrang, imageBase64, attr1, attr2);
            Stage stage = getStage(productGrid);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Nav & Sidebar
    @FXML
    private void handleHome() {
        goHome(getStage(productGrid));
    }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(productGrid));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(productGrid));
    }

    @FXML
    private void handleThemSanPham() {
        navigateTo("/fxml/SellerAddProduct.fxml", getStage(productGrid));
    }

    @FXML
    private void handleXemSanPham() { /* đã ở đây */ }

    @FXML
    private void handleTaoPhien() {
        navigateTo("/fxml/SellerCreateSession.fxml", getStage(productGrid));
    }

    @FXML
    private void handleXemPhien() {
        navigateTo("/fxml/SellerSessionList.fxml", getStage(productGrid));
    }

    @FXML
    private void handleEdit() { /* TODO */ }

    @FXML
    private void handleSave() { /* TODO */ }
}
