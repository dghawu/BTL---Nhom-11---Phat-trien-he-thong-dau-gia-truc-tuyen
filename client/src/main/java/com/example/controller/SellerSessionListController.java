package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * SellerSessionListController - SellerSessionList.fxml
 */
public class SellerSessionListController extends com.example.controller.BaseController {

    @FXML private FlowPane sessionGrid;

    @FXML
    public void initialize() {
        loadSessions();
    }

    private void loadSessions() {
        sessionGrid.getChildren().clear();
        // TODO: List<AuctionSession> sessions = ServerService.getMySessions(currentUsername);
        // for (AuctionSession s : sessions) sessionGrid.getChildren().add(buildCard(s));

        // Mock cards
        for (int i = 1; i <= 3; i++) {
            sessionGrid.getChildren().add(buildMockCard(
                    "Sản phẩm " + i, "10/5/2025 09:00", "PENDING", "1.000.000đ", "Điện tử"
            ));
        }
    }

    private VBox buildMockCard(String ten, String thoiGianMo, String trangThai, String gia, String phanLoai) {
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
                new Label("Thời gian mở phiên: " + thoiGianMo),
                new Label("Trạng thái phiên: " + trangThai),
                new Label("Giá khởi điểm: " + gia),
                new Label("Phân loại: " + phanLoai)
        );

        Hyperlink link = new Hyperlink("Xem chi tiết");
        link.getStyleClass().add("link-text");
        link.setOnAction(e -> openDetail(ten, thoiGianMo, trangThai, gia));
        info.getChildren().add(link);

        card.getChildren().addAll(title, img, info);
        return card;
    }

    private void openDetail(String ten, String thoiGianMo, String trangThai, String gia) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SellerSessionDetail.fxml"));
            Parent root = loader.load();
            com.example.controller.SellerSessionDetailController ctrl = loader.getController();
            ctrl.currentUsername = currentUsername;
            ctrl.currentRole = currentRole;
            ctrl.initData(ten, thoiGianMo, trangThai, gia);
            Stage stage = getStage(sessionGrid);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleHome()        { goHome(getStage(sessionGrid)); }
    @FXML private void handleAuctions()    { goAuctions(getStage(sessionGrid)); }
    @FXML private void handleSettings()    { goSettings(getStage(sessionGrid)); }
    @FXML private void handleThemSanPham() { navigateTo("/fxml/SellerAddProduct.fxml", getStage(sessionGrid)); }
    @FXML private void handleXemSanPham()  { navigateTo("/fxml/SellerProductList.fxml", getStage(sessionGrid)); }
    @FXML private void handleTaoPhien()    { navigateTo("/fxml/SellerCreateSession.fxml", getStage(sessionGrid)); }
    @FXML private void handleXemPhien()    { /* đã ở đây */ }
    @FXML private void handleEdit()        { /* TODO */ }
    @FXML private void handleSave()        { /* TODO */ }
}
