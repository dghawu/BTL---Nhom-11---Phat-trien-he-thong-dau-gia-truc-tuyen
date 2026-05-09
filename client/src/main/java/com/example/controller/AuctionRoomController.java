package com.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * AuctionRoomController - AuctionRoom.fxml
 * Quản lý phòng đấu giá realtime.
 * - Nhận cập nhật từ server qua listener/thread
 * - Cho phép đấu giá thủ công và tự động
 */
public class AuctionRoomController extends com.example.controller.BaseController {

    // Left: product info
    @FXML private Label lblProductName;
    @FXML private Label lblTenSP;
    @FXML private Label lblIdSP;
    @FXML private Label lblGiaMoBan;
    @FXML private Label lblTinhTrang;
    @FXML private Label lblMoTa;
    @FXML private Pane  imgPane;

    // Middle: bid history
    @FXML private VBox bidHistoryBox;

    // Right: timer + price
    @FXML private Label lblThoiGianBatDau;
    @FXML private Label lblThoiGianKetThuc;
    @FXML private Label lblThoiGianConLai;
    @FXML private Label lblGiaKhoiDiem;
    @FXML private Label lblBuocGia;
    @FXML private Label lblGiaHienTai;
    @FXML private Label lblNguoiGiuGia;

    // Manual bid
    @FXML private Label     lblManualToggle;
    @FXML private TextField manualBidField;

    // Auto bid
    @FXML private Label     lblAutoToggle;
    @FXML private TextField autoBuocGiaField;
    @FXML private TextField autoMaxGiaField;

    private boolean manualEnabled = true;
    private boolean autoEnabled   = false;

    // Session info - set từ AuctionsController
    private String sessionId;
    private double currentPrice = 0;

    @FXML
    public void initialize() {
        // TODO: kết nối đến server socket và lắng nghe events
        // ServerService.joinSession(sessionId, this::onBidEvent);
    }

    /** Gọi sau navigateTo để set session */
    public void initSession(String sessionId, String productName) {
        this.sessionId = sessionId;
        lblProductName.setText(productName.toUpperCase());
        // TODO: load toàn bộ thông tin phiên từ server
    }

    // ------------------------------------------------------------------ //
    //  Realtime update - gọi từ background thread khi nhận event từ server
    // ------------------------------------------------------------------ //

    /** Thêm một dòng vào feed lịch sử đấu giá */
    public void addBidEvent(String message) {
        Platform.runLater(() -> {
            Label entry = new Label("▶ " + message);
            entry.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            bidHistoryBox.getChildren().add(entry);
        });
    }

    /** Cập nhật giá hiện tại */
    public void updateCurrentPrice(double price, String holderName) {
        Platform.runLater(() -> {
            currentPrice = price;
            lblGiaHienTai.setText(String.format("%.0f", price));
            lblNguoiGiuGia.setText("Người giữ giá cao nhất: " + holderName);
        });
    }

    /** Thông báo phiên kết thúc */
    public void onSessionEnd() {
        Platform.runLater(() ->
                showNotification(getStage(bidHistoryBox), "PHIÊN ĐẤU GIÁ ĐÃ KẾT THÚC!!!")
        );
    }

    // ------------------------------------------------------------------ //
    //  Manual bid
    // ------------------------------------------------------------------ //

    @FXML
    private void handleToggleManual() {
        manualEnabled = !manualEnabled;
        lblManualToggle.setText(manualEnabled ? "ON" : "OFF");
        lblManualToggle.setStyle(
                "-fx-background-color: " + (manualEnabled ? "#22C55E" : "#9CA3AF") + ";" +
                        "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;"
        );
        manualBidField.setDisable(!manualEnabled);
    }

    @FXML
    private void handleManualBid() {
        if (!manualEnabled) return;

        String input = manualBidField.getText().trim();
        if (input.isEmpty()) return;

        double bid;
        try {
            bid = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            showNotification(getStage(bidHistoryBox), "GIÁ KHÔNG HỢP LỆ!");
            return;
        }

        if (bid <= currentPrice) {
            showNotification(getStage(bidHistoryBox), "GIÁ ĐẶT PHẢI CAO HƠN GIÁ HIỆN TẠI!");
            return;
        }

        // TODO: ServerService.placeBid(sessionId, currentUsername, bid);
        manualBidField.clear();
    }

    // ------------------------------------------------------------------ //
    //  Auto bid
    // ------------------------------------------------------------------ //

    @FXML
    private void handleToggleAuto() {
        autoEnabled = !autoEnabled;
        lblAutoToggle.setText(autoEnabled ? "ON" : "OFF");
        lblAutoToggle.setStyle(
                "-fx-background-color: " + (autoEnabled ? "#22C55E" : "#EF4444") + ";" +
                        "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;"
        );
        autoBuocGiaField.setDisable(!autoEnabled);
        autoMaxGiaField.setDisable(!autoEnabled);
    }

    @FXML
    private void handleSaveAutoConfig() {
        if (!autoEnabled) return;

        String buocGia = autoBuocGiaField.getText().trim();
        String maxGia  = autoMaxGiaField.getText().trim();

        if (buocGia.isEmpty() || maxGia.isEmpty()) {
            showNotification(getStage(bidHistoryBox), "VUI LÒNG NHẬP ĐẦY ĐỦ!");
            return;
        }

        // TODO: ServerService.setAutoBid(sessionId, currentUsername, Double.parseDouble(buocGia), Double.parseDouble(maxGia));
        showNotification(getStage(bidHistoryBox), "ĐÃ BẬT ĐẤU GIÁ TỰ ĐỘNG!");
    }

    // ------------------------------------------------------------------ //
    //  Report
    // ------------------------------------------------------------------ //
    @FXML
    private void handleReport() {
        // TODO: mở dialog báo cáo sự cố
        // ServerService.reportIssue(sessionId, currentUsername, ...);
        showNotification(getStage(bidHistoryBox), "ĐÃ GỬI BÁO CÁO SỰ CỐ!");
    }

    // ------------------------------------------------------------------ //
    //  Nav
    // ------------------------------------------------------------------ //
    @FXML private void handleBack()         { navigateTo("/fxml/Auctions.fxml", getStage(bidHistoryBox)); }
    @FXML private void handleHome()         { goHome(getStage(bidHistoryBox)); }
    @FXML private void handleAuctions()     { navigateTo("/fxml/Auctions.fxml", getStage(bidHistoryBox)); }
    @FXML private void handleBidderCentre() { navigateTo("/fxml/BidderCentre.fxml", getStage(bidHistoryBox)); }
    @FXML private void handleSettings()     { goSettings(getStage(bidHistoryBox)); }
}
