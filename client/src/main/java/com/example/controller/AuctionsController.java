package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * AuctionsController - Auctions.fxml
 * Hiển thị danh sách phiên đấu giá, lọc theo danh mục.
 * Dùng chung cho Seller (chỉ xem) và Bidder (có thể tham gia).
 */
public class AuctionsController extends com.example.controller.BaseController {

    @FXML private Button   middleTabBtn;
    @FXML private FlowPane auctionGrid;

    // Category filter buttons
    @FXML private Button btnTatCa;
    @FXML private Button btnDienTu;
    @FXML private Button btnPhuongTien;
    @FXML private Button btnThoiTrang;
    @FXML private Button btnNgheThuat;
    @FXML private Button btnMucKhac;

    private String activeFilter = "ALL";

    @FXML
    public void initialize() { }
    @Override
    public void onReady() {
        loadAuctions("ALL");
    }

    /** Gọi sau navigateTo để cấu hình theo role */
    public void setupForRole(String role, String username) {
        currentRole = role;
        currentUsername = username;
        if ("SELLER".equalsIgnoreCase(role)) {
            middleTabBtn.setText("Seller centre");
        } else {
            middleTabBtn.setText("Bidder centre");
        }
    }

    // ------------------------------------------------------------------ //
    //  Nav
    // ------------------------------------------------------------------ //
    @FXML private void handleHome()       { goHome(getStage(auctionGrid)); }
    @FXML private void handleMiddleTab()  {
        if ("SELLER".equalsIgnoreCase(currentRole))
            navigateTo("/fxml/SellerAddProduct.fxml", getStage(auctionGrid));
        else
            navigateTo("/fxml/BidderCentre.fxml", getStage(auctionGrid));
    }
    @FXML private void handleSettings()   { goSettings(getStage(auctionGrid)); }

    // ------------------------------------------------------------------ //
    //  Category filters
    // ------------------------------------------------------------------ //
    @FXML private void handleFilterTatCa()      { setFilter("ALL",       btnTatCa); }
    @FXML private void handleFilterDienTu()     { setFilter("DIEN_TU",   btnDienTu); }
    @FXML private void handleFilterPhuongTien() { setFilter("PHUONG_TIEN", btnPhuongTien); }
    @FXML private void handleFilterThoiTrang()  { setFilter("THOI_TRANG", btnThoiTrang); }
    @FXML private void handleFilterNgheThuat()  { setFilter("NGHE_THUAT", btnNgheThuat); }
    @FXML private void handleFilterMucKhac()    { setFilter("MUC_KHAC",   btnMucKhac); }

    private void setFilter(String filter, Button activeBtn) {
        activeFilter = filter;
        // Reset tất cả filter button về style mặc định
        for (Button b : new Button[]{btnTatCa, btnDienTu, btnPhuongTien, btnThoiTrang, btnNgheThuat, btnMucKhac}) {
            b.setStyle("-fx-background-color: transparent; -fx-font-size: 15px; " +
                    "-fx-padding: 14 22 14 22; -fx-background-radius: 0; -fx-cursor: hand; " +
                    "-fx-border-color: transparent transparent transparent #DDDDDD; " +
                    "-fx-border-width: 0 0 0 1px; -fx-font-weight: normal;");
        }
        activeBtn.setStyle("-fx-background-color: #F5E9A0; -fx-font-weight: bold; " +
                "-fx-font-size: 15px; -fx-padding: 14 22 14 22; " +
                "-fx-background-radius: 0; -fx-cursor: hand;");
        loadAuctions(filter);
    }

    // ------------------------------------------------------------------ //
    //  Load data
    // ------------------------------------------------------------------ //
    private void loadAuctions(String filter) {
        auctionGrid.getChildren().clear();

        org.json.JSONArray sessions = com.example.socket.ServerService.getAllSessions(filter);
        if (sessions == null) return;

        for (int i = 0; i < sessions.length(); i++) {
            org.json.JSONObject s = sessions.getJSONObject(i);
            auctionGrid.getChildren().add(buildCard(s));
        }
    }

    private VBox buildCard(org.json.JSONObject s) {
        String id        = s.getString("id");
        String ten       = s.getString("itemName");
        String gia       = String.format("%,.0fđ", s.getDouble("startPrice"));
        String giaHT     = String.format("%,.0fđ", s.getDouble("currentPrice"));
        String sellerId  = s.getString("sellerId");
        String category  = s.getString("category");
        String endTime   = s.getString("endTime");
        String status    = s.getString("status");

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
                new Label("Tên: " + ten),
                new Label("Giá khởi điểm: " + gia),
                new Label("Giá hiện tại: " + giaHT),
                new Label("Kết thúc: " + endTime),
                new Label("Phân loại: " + category),
                new Label("Trạng thái: " + status)
        );

        Hyperlink linkDetail = new Hyperlink("Xem chi tiết");
        linkDetail.setStyle("-fx-text-fill: #0044CC;");
        linkDetail.setOnAction(e -> openDetailDialog(s));
        info.getChildren().add(linkDetail);

        Button btnJoin = new Button("THAM GIA ĐẤU GIÁ");
        btnJoin.getStyleClass().add("btn-primary");
        btnJoin.setMaxWidth(Double.MAX_VALUE);
        btnJoin.setOnAction(e -> handleJoinAuction(id, ten));
        VBox.setMargin(btnJoin, new javafx.geometry.Insets(8, 10, 10, 10));

        card.getChildren().addAll(title, img, info, btnJoin);
        return card;
    }

    private void openDetailDialog(org.json.JSONObject s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionDetailDialog.fxml"));
            Parent root = loader.load();
            AuctionDetailDialogController ctrl = loader.getController();
            ctrl.initData(
                    s.getString("id"),
                    s.getString("itemName"),
                    String.format("%,.0fđ", s.getDouble("currentPrice")),
                    s.getString("sellerId"),
                    s.getString("category")
            );
            ctrl.setParentController(this);
            Stage dialog = new Stage();
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.initOwner(getStage(auctionGrid));
            dialog.setScene(new Scene(root));
            dialog.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleAddToWatchList(String ten) {
        // TODO: ServerService.addToWatchList(currentUsername, sessionId);
        showNotification(getStage(auctionGrid), "ĐÃ THÊM VÀO WATCH LIST!");
    }

    public void handleJoinAuction(String sessionId, String productName) {
        if (!"BIDDER".equalsIgnoreCase(currentRole)) {
            showNotification(getStage(auctionGrid),
                    "BẠN PHẢI ĐĂNG NHẬP VỚI VAI TRÒ BIDDER\nMỚI CÓ THỂ THAM GIA ĐẤU GIÁ");
            return;
        }
        // Truyền sessionId vào AuctionRoom
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionRoom.fxml"));
            Parent root = loader.load();
            AuctionRoomController ctrl = loader.getController();
            // Truyền thông tin base
            ctrl.currentUsername = this.currentUsername;
            ctrl.currentUserId   = this.currentUserId;
            ctrl.currentRole     = this.currentRole;
            ctrl.initSession(sessionId, productName);
            getStage(auctionGrid).setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
