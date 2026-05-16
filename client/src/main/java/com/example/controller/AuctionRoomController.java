package com.example.controller;

import com.example.socket.BidSocketClient;
import com.example.socket.BidSocketClient.BidEvent;
import com.example.socket.ServerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuctionRoomController - AuctionRoom.fxml
 * Quản lý phòng đấu giá realtime.
 * - Nhận cập nhật từ server qua listener/thread
 * - Cho phép đấu giá thủ công và tự động
 * Thay đổi so với phiên bản cũ:
 *   - initialize(): gọi BidSocketClient.joinSession() để nhận push realtime
 *   - handleBidEvent(): xử lý BID_UPDATE và AUCTION_CLOSED từ Push Server
 *   - handleBack() và các nav: gọi BidSocketClient.leave() trước khi thoát
 */
public class AuctionRoomController extends BaseController {

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

    private String sessionId;
    private double currentPrice = 0;

    private static final DateTimeFormatter DT_DISPLAY =
            DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");

    // ------------------------------------------------------------------ //
    //  Khởi tạo
    // ------------------------------------------------------------------ //

    @FXML
    public void initialize() {
        // Không làm gì ở đây — chờ initSession() được gọi sau navigateTo()
        // để có sessionId trước khi joinSession
    }

    /**
     * Gọi ngay sau navigateTo() để truyền session vào controller.
     * Thứ tự:
     *   1. Load thông tin phiên từ API server (port 8888)
     *   2. Kết nối Push Server (port 8889) để nhận BID_UPDATE realtime
     */
    public void initSession(String sessionId, String productName) {
        this.sessionId = sessionId;
        lblProductName.setText(productName.toUpperCase());

        // 1. Load thông tin phiên ban đầu
        loadSessionInfo();

        // 2. Join Push Server để nhận realtime
        BidSocketClient.getInstance().joinSession(
                sessionId,
                ServerService.getToken(),
                event -> handleBidEvent(event)   // callback — chạy trên background thread
        );
    }

    // ------------------------------------------------------------------ //
    //  Load thông tin phiên từ API
    // ------------------------------------------------------------------ //

    private void loadSessionInfo() {
        Platform.runLater(() -> {
            org.json.JSONArray sessions = ServerService.getAllSessions("ALL");
            if (sessions == null) return;

            for (int i = 0; i < sessions.length(); i++) {
                org.json.JSONObject s = sessions.getJSONObject(i);
                if (!s.getString("id").equals(sessionId)) continue;

                currentPrice = s.getDouble("currentPrice");

                lblGiaKhoiDiem.setText(String.format("%,.0f đ", s.getDouble("startPrice")));
                lblBuocGia.setText(String.format("%,.0f đ", s.getDouble("stepPrice")));
                lblGiaHienTai.setText(String.format("%,.0f đ", currentPrice));

                // Format thời gian hiển thị
                String startTime = s.getString("startTime");
                String endTime   = s.getString("endTime");
                lblThoiGianBatDau.setText(formatTime(startTime));
                lblThoiGianKetThuc.setText(formatTime(endTime));

                String winner = s.optString("currentWinner", "");
                lblNguoiGiuGia.setText(winner.isEmpty() ? "Chưa có" : winner);
                break;
            }
        });
    }

    // ------------------------------------------------------------------ //
    //  Xử lý event realtime từ Push Server
    // ------------------------------------------------------------------ //

    /**
     * Callback nhận BidEvent từ BidSocketClient.
     * Chạy trên background thread → phải dùng Platform.runLater() để update UI.
     */
    private void handleBidEvent(BidEvent event) {
        switch (event.type) {
            case BID_UPDATE -> Platform.runLater(() -> {
                // Cập nhật giá và người giữ giá
                currentPrice = event.price;
                lblGiaHienTai.setText(String.format("%,.0f đ", event.price));
                lblNguoiGiuGia.setText(event.bidderName);

                // Cập nhật endTime nếu bị anti-snipe kéo dài
                if (!event.endTime.isBlank()) {
                    lblThoiGianKetThuc.setText(formatTime(event.endTime));
                }

                // Thêm vào feed lịch sử
                addBidEntry(event.bidderName + " đặt giá "
                        + String.format("%,.0f đ", event.price));
            });

            case AUCTION_CLOSED -> Platform.runLater(() -> {
                addBidEntry("🏁 Phiên kết thúc!");
                if (!event.bidderName.isEmpty() && !"NO_WINNER".equals(event.bidderName)) {
                    showNotification(getStage(bidHistoryBox),
                            "PHIÊN ĐẤU GIÁ KẾT THÚC!\nNgười thắng: " + event.bidderName
                                    + "\nGiá cuối: " + String.format("%,.0f đ", event.price));
                } else {
                    showNotification(getStage(bidHistoryBox),
                            "PHIÊN ĐẤU GIÁ KẾT THÚC!\nKhông có người thắng.");
                }
                // Vô hiệu hoá nút đặt giá
                manualBidField.setDisable(true);
                autoBuocGiaField.setDisable(true);
                autoMaxGiaField.setDisable(true);
            });

            default -> System.out.println("[AuctionRoom] Event không xác định: " + event);
        }
    }

    // ------------------------------------------------------------------ //
    //  Thêm dòng vào feed lịch sử (dùng nội bộ)
    // ------------------------------------------------------------------ //

    private void addBidEntry(String message) {
        Label entry = new Label("▶ " + message);
        entry.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        entry.setWrapText(true);
        bidHistoryBox.getChildren().add(0, entry); // mới nhất lên trên
    }

    // ── Public — giữ lại để tương thích nếu có chỗ khác gọi ────────────
    public void addBidEvent(String message)                              { addBidEntry(message); }
    public void updateCurrentPrice(double price, String holderName)     {
        currentPrice = price;
        lblGiaHienTai.setText(String.format("%,.0f đ", price));
        lblNguoiGiuGia.setText(holderName);
    }
    public void onSessionEnd() {
        showNotification(getStage(bidHistoryBox), "PHIÊN ĐẤU GIÁ ĐÃ KẾT THÚC!!!");
    }

    // ------------------------------------------------------------------ //
    //  Manual bid
    // ------------------------------------------------------------------ //

    @FXML
    private void handleToggleManual() {
        manualEnabled = !manualEnabled;
        lblManualToggle.setText(manualEnabled ? "ON" : "OFF");
        lblManualToggle.setStyle(
                "-fx-background-color: " + (manualEnabled ? "#22C55E" : "#9CA3AF") + ";"
                        + "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;"
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
            bid = Double.parseDouble(input.replace(",", "").replace("đ", "").trim());
        } catch (NumberFormatException e) {
            showNotification(getStage(bidHistoryBox), "GIÁ KHÔNG HỢP LỆ!");
            return;
        }

        if (bid <= currentPrice) {
            showNotification(getStage(bidHistoryBox),
                    "GIÁ ĐẶT PHẢI CAO HƠN GIÁ HIỆN TẠI (" + String.format("%,.0f đ", currentPrice) + ")!");
            return;
        }

        // Gửi lên API server (port 8888) — response/request như cũ
        // Push realtime sẽ đến qua BidSocketClient (port 8889)
        boolean ok = ServerService.placeBid(sessionId, bid);
        if (ok) {
            manualBidField.clear();
            // Không cần update UI ở đây — BID_UPDATE sẽ đến qua push
            // và handleBidEvent() sẽ cập nhật
        } else {
            showNotification(getStage(bidHistoryBox), "ĐẶT GIÁ THẤT BẠI! Vui lòng thử lại.");
        }
    }

    // ------------------------------------------------------------------ //
    //  Auto bid
    // ------------------------------------------------------------------ //

    @FXML
    private void handleToggleAuto() {
        autoEnabled = !autoEnabled;
        lblAutoToggle.setText(autoEnabled ? "ON" : "OFF");
        lblAutoToggle.setStyle(
                "-fx-background-color: " + (autoEnabled ? "#22C55E" : "#EF4444") + ";"
                        + "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;"
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
        boolean ok = ServerService.setAutoBid(
                sessionId,
                Double.parseDouble(buocGia),
                Double.parseDouble(maxGia)
        );
        showNotification(getStage(bidHistoryBox),
                ok ? "ĐÃ BẬT ĐẤU GIÁ TỰ ĐỘNG!" : "CÀI ĐẶT TỰ ĐỘNG THẤT BẠI!");
    }

    // ------------------------------------------------------------------ //
    //  Report
    // ------------------------------------------------------------------ //

    @FXML
    private void handleReport() {
        showNotification(getStage(bidHistoryBox), "ĐÃ GỬI BÁO CÁO SỰ CỐ!");
    }

    // ------------------------------------------------------------------ //
    //  Navigation — leave() trước khi rời phòng
    // ------------------------------------------------------------------ //

    @FXML private void handleBack() {
        BidSocketClient.getInstance().leave();
        navigateTo("/fxml/Auctions.fxml", getStage(bidHistoryBox));
    }

    @FXML private void handleHome() {
        BidSocketClient.getInstance().leave();
        goHome(getStage(bidHistoryBox));
    }

    @FXML private void handleAuctions() {
        BidSocketClient.getInstance().leave();
        navigateTo("/fxml/Auctions.fxml", getStage(bidHistoryBox));
    }

    @FXML private void handleBidderCentre() {
        BidSocketClient.getInstance().leave();
        navigateTo("/fxml/BidderCentre.fxml", getStage(bidHistoryBox));
    }

    @FXML private void handleSettings() {
        BidSocketClient.getInstance().leave();
        goSettings(getStage(bidHistoryBox));
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    /** Format chuỗi ISO datetime từ server sang dạng hiển thị dễ đọc. */
    private String formatTime(String raw) {
        if (raw == null || raw.isBlank()) return "-";
        try {
            // Server trả về dạng "2025-05-20T21:00" hoặc "2025-05-20T21:00:00"
            String normalized = raw.length() == 16 ? raw + ":00" : raw;
            LocalDateTime dt = LocalDateTime.parse(normalized);
            return dt.format(DT_DISPLAY);
        } catch (Exception e) {
            return raw; // fallback hiển thị raw nếu parse lỗi
        }
    }
}
