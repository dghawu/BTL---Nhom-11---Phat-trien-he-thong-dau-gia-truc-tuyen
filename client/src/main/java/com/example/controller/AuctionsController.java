package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
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
    public void initialize() {
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
        // TODO: List<AuctionSession> list = ServerService.getSessionsByCategory(filter);
        // for (AuctionSession s : list) auctionGrid.getChildren().add(buildCard(s));

        // Mock: 4 card mẫu
        for (int i = 1; i <= 4; i++) {
            auctionGrid.getChildren().add(buildMockCard(
                    "Sản phẩm " + i, "10/5/2025", "1.000.000đ", "seller001", "Điện tử"
            ));
        }
    }

    private VBox buildMockCard(String ten, String thoiGianMo, String gia, String sellerId, String phanLoai) {
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
                new Label("Giá khởi điểm: " + gia),
                new Label("Id người bán: " + sellerId),
                new Label("Phân loại: " + phanLoai),
                new Label("Mô tả: _______________")
        );

        Hyperlink linkDetail = new Hyperlink("Xem chi tiết");
        linkDetail.setStyle("-fx-text-fill: #0044CC;");
        linkDetail.setOnAction(e -> openDetailDialog(ten, gia, sellerId, phanLoai));

        Hyperlink linkWatch = new Hyperlink("Add to Watch List");
        linkWatch.setStyle("-fx-text-fill: #CC00CC;");
        linkWatch.setOnAction(e -> handleAddToWatchList(ten));

        info.getChildren().addAll(linkDetail, linkWatch);

        Button btnJoin = new Button("THAM GIA ĐẤU GIÁ");
        btnJoin.getStyleClass().add("btn-primary");
        btnJoin.setMaxWidth(Double.MAX_VALUE);
        btnJoin.setOnAction(e -> handleJoinAuction(ten));
        VBox.setMargin(btnJoin, new javafx.geometry.Insets(8, 10, 10, 10));

        card.getChildren().addAll(title, img, info, btnJoin);
        return card;
    }

    private void openDetailDialog(String ten, String gia, String sellerId, String phanLoai) {
        try {
            var url = getClass().getResource("/fxml/AuctionDetailDialog.fxml");
            System.out.println("Dialog URL: " + url);

            FXMLLoader loader = new FXMLLoader(url);
            //FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionDetailDialog.fxml"));
            Parent root = loader.load();
            com.example.controller.AuctionDetailDialogController ctrl = loader.getController();
            ctrl.initData(ten, gia, sellerId, phanLoai);
            ctrl.setParentController(this);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(getStage(auctionGrid));
            dialog.setScene(new Scene(root));
            dialog.setTitle("Chi tiết phiên đấu giá");
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddToWatchList(String ten) {
        // TODO: ServerService.addToWatchList(currentUsername, sessionId);
        showNotification(getStage(auctionGrid), "ĐÃ THÊM VÀO WATCH LIST!");
    }

    public void handleJoinAuction(String sessionName) {
        if (!"BIDDER".equalsIgnoreCase(currentRole)) {
            showNotification(getStage(auctionGrid),
                    "BẠN PHẢI ĐĂNG NHẬP VỚI VAI TRÒ BIDDER\nMỚI CÓ THỂ THAM GIA ĐẤU GIÁ");
            return;
        }
        navigateTo("/fxml/AuctionRoom.fxml", getStage(auctionGrid));
    }
}
