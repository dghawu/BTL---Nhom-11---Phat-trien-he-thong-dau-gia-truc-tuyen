package com.example.controller;

import com.example.socket.ServerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * SellerProductListController - SellerProductList.fxml
 * Hiển thị danh sách sản phẩm dưới dạng card grid.
 * Mỗi card có link "Xem chi tiết" → mở SellerProductDetail.
 */
public class SellerProductListController extends com.example.controller.BaseController {

    @FXML private FlowPane productGrid;

    @FXML
    public void initialize() { }

    @Override
    public void onReady() {
        loadProducts();
    }

    private void loadProducts() {
        productGrid.getChildren().clear();

        //Gọi thật thay vì mock
        org.json.JSONArray items = ServerService.getMyItems();
        if (items == null) return;

        for (int i = 0; i < items.length(); i++) {
            org.json.JSONObject item = items.getJSONObject(i);
            String id       = item.getString("id");
            String name     = item.getString("name");
            String category     = item.optString("type", "N/A");
            String description  = item.optString("description", "");
            String status   = item.getString("status");
            double price    = item.getDouble("startPrice");
            String priceStr = String.format("%,.0fđ", price);

            productGrid.getChildren().add(buildMockCard(name, id, priceStr, status, category, description));
        }
    }

    private VBox buildMockCard(String ten, String id, String gia, String tinhTrang, String category, String description ) {
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
                new Label("Id sản phẩm: " + id),
                new Label("Giá mở bán: " + gia),
                new Label("Tình trạng: " + tinhTrang)
        );

        Hyperlink link = new Hyperlink("Xem chi tiết");
        link.getStyleClass().add("link-text");
        link.setStyle("-fx-text-fill: #0044CC;");
        link.setOnAction(e -> openDetail(id, ten, category, gia, description, tinhTrang));
        info.getChildren().add(link);

        card.getChildren().addAll(title, img, info);
        return card;
    }

    private void openDetail(String id, String ten, String category, String gia, String moTa, String tinhTrang) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SellerProductDetail.fxml"));
            Parent root = loader.load();
            com.example.controller.SellerProductDetailController ctrl = loader.getController();
            ctrl.currentUsername = currentUsername;
            ctrl.currentRole = currentRole;
            ctrl.initData(id, ten, category, gia, moTa, tinhTrang);
            Stage stage = getStage(productGrid);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Nav & Sidebar
    @FXML private void handleHome()         { goHome(getStage(productGrid)); }
    @FXML private void handleAuctions()     { goAuctions(getStage(productGrid)); }
    @FXML private void handleSettings()     { goSettings(getStage(productGrid)); }
    @FXML private void handleThemSanPham()  { navigateTo("/fxml/SellerAddProduct.fxml", getStage(productGrid)); }
    @FXML private void handleXemSanPham()   { /* đã ở đây */ }
    @FXML private void handleTaoPhien()     { navigateTo("/fxml/SellerCreateSession.fxml", getStage(productGrid)); }
    @FXML private void handleXemPhien()     { navigateTo("/fxml/SellerSessionList.fxml", getStage(productGrid)); }
    @FXML private void handleEdit()         { /* TODO */ }
    @FXML private void handleSave()         { /* TODO */ }
}
