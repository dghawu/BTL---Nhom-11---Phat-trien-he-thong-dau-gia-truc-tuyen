package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * AuctionsController - Auctions.fxml
 * Hiển thị danh sách phiên đấu giá, lọc theo danh mục.
 * Dùng chung cho Seller (chỉ xem) và Bidder (có thể tham gia).
 */
public class AuctionsController extends com.example.controller.BaseController {

    @FXML
    private Button middleTabBtn;
    @FXML
    private FlowPane auctionGrid;

    // Category filter buttons
    @FXML
    private Button btnTatCa;
    @FXML
    private Button btnDienTu;
    @FXML
    private Button btnPhuongTien;
    @FXML
    private Button btnThoiTrang;
    @FXML
    private Button btnNgheThuat;
    @FXML
    private Button btnMucKhac;

    private String activeFilter = "ALL";

    @FXML
    public void initialize() {
    }

    @Override
    public void onReady() {
        loadAuctions("ALL");
    }

    /**
     * Gọi sau navigateTo để cấu hình theo role
     */
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
    @FXML
    private void handleHome() {
        goHome(getStage(auctionGrid));
    }

    @FXML
    private void handleMiddleTab() {
        if ("SELLER".equalsIgnoreCase(currentRole))
            navigateTo("/fxml/SellerAddProduct.fxml", getStage(auctionGrid));
        else
            navigateTo("/fxml/BidderCentre.fxml", getStage(auctionGrid));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(auctionGrid));
    }

    // ------------------------------------------------------------------ //
    //  Category filters
    // ------------------------------------------------------------------ //
    @FXML
    private void handleFilterTatCa() {
        setFilter("ALL", btnTatCa);
    }

    @FXML
    private void handleFilterDienTu() {
        setFilter("ELECTRONICS", btnDienTu);
    }

    @FXML
    private void handleFilterPhuongTien() {
        setFilter("VEHICLE", btnPhuongTien);
    }

    @FXML
    private void handleFilterThoiTrang() {
        setFilter("FASHION", btnThoiTrang);
    }

    @FXML
    private void handleFilterNgheThuat() {
        setFilter("ART", btnNgheThuat);
    }

    @FXML
    private void handleFilterMucKhac() {
        setFilter("ETC", btnMucKhac);
    }

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
    private Map<String, String> itemImageMap = new HashMap<>();

    private void loadAuctions(String filter) {
        auctionGrid.getChildren().clear();
        itemImageMap.clear();

        org.json.JSONArray sessions = ServerService.getAllSessions(filter);
        if (sessions == null) return;

        for (int i = 0; i < sessions.length(); i++) {
            org.json.JSONObject s = sessions.getJSONObject(i);

            auctionGrid.getChildren().add(buildCard(s));
            // Lưu imageBase64 vào map nếu có
            String itemId = s.optString("itemId", "");
            String imageBase64 = s.optString("itemImage", "");
            if (!itemId.isEmpty() && !imageBase64.isEmpty()) {
                itemImageMap.put(itemId, imageBase64);
            }
        }

    }

    public String getItemImage(String itemId) {
        return itemImageMap.get(itemId);
    }

    private VBox buildCard(org.json.JSONObject s) {
        String id = s.getString("id");
        String ten = s.getString("itemName");
        String gia = String.format("%,.0fđ", s.getDouble("startPrice"));
        String giaHT = String.format("%,.0fđ", s.getDouble("currentPrice"));
        String startTime = s.optString("startTime", "");
        String sellerId = s.getString("sellerId");
        String category = s.getString("category");
        String endTime = s.getString("endTime");
        String status = s.getString("status");
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

        VBox info = new VBox(3);
        info.getStyleClass().add("product-card-info");
        info.getChildren().addAll(
                new Label("Name: " + ten),
                new Label("Starting price: " + gia),
                new Label("Current Price: " + giaHT),
                new Label("Start: " + startTime),
                new Label("End: " + endTime),
                new Label("Category: " + category),
                new Label("Status: " + status)
        );

        Hyperlink linkDetail = new Hyperlink("View Details");
        linkDetail.setStyle("-fx-text-fill: #0044CC;");
        linkDetail.setOnAction(e -> openDetailDialog(s));
        info.getChildren().add(linkDetail);

        Button btnJoin = new Button("JOIN AUCTION");
        btnJoin.getStyleClass().add("btn-dephon");
        btnJoin.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(btnJoin, new javafx.geometry.Insets(8, 10, 10, 10));
        if ("RUNNING".equalsIgnoreCase(status)) {
            btnJoin.setOnAction(e -> handleJoinAuction(id, ten));
        } else {
            btnJoin.setText("NOT START YET");
            btnJoin.setDisable(true);
            btnJoin.setStyle("-fx-opacity: 0.5;");
        }

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
                System.err.println("[AuctionsController] Lỗi decode image: " + e.getMessage());
            }
        }

        javafx.scene.layout.HBox sepBox = new javafx.scene.layout.HBox();
        javafx.scene.layout.Region sep = new javafx.scene.layout.Region();
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.setStyle("-fx-background-color: #aaaaaa; -fx-pref-height: 1px; -fx-max-height: 1px;");
        javafx.scene.layout.HBox.setHgrow(sep, javafx.scene.layout.Priority.ALWAYS);
        sepBox.setStyle("-fx-padding: 0 16 0 16;");
        sepBox.getChildren().add(sep);

        card.getChildren().addAll(title, sepBox, img, info, btnJoin);
        return card;
    }

    private void openDetailDialog(org.json.JSONObject s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionDetailDialog.fxml"));
            Parent root = loader.load();
            AuctionDetailDialogController ctrl = loader.getController();
            ctrl.initData(s);
            ctrl.setParentController(this);
            Stage dialog = new Stage();
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.initOwner(getStage(auctionGrid));
            dialog.setScene(new Scene(root));
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleJoinAuction(String sessionId, String productName) {
        // Seller được vào xem nhưng không đặt giá
        // Không phải BIDDER cũng không phải SELLER → chặn
        if (!"BIDDER".equalsIgnoreCase(currentRole) && !"SELLER".equalsIgnoreCase(currentRole)) {
            showNotification(getStage(auctionGrid),
                    "YOU MUST LOG IN AS A BIDDER TO\nPARTICIPATE IN THE AUCTION.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionRoom.fxml"));
            Parent root = loader.load();
            AuctionRoomController ctrl = loader.getController();
            ctrl.currentUsername = this.currentUsername;
            ctrl.currentUserId = this.currentUserId;
            ctrl.currentRole = this.currentRole;
            ctrl.initSession(sessionId, productName);
            getStage(auctionGrid).setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
