package com.example.controller;

import com.example.socket.BidSocketClient;
import com.example.socket.BidSocketClient.BidEvent;
import com.example.socket.ServerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuctionRoomController - AuctionRoom.fxml
 * Quản lý phòng đấu giá realtime.
 * - Nhận cập nhật từ server qua listener/thread
 * - Cho phép đấu giá thủ công và tự động
 * Thay đổi so với phiên bản cũ:
 * - initialize(): gọi BidSocketClient.joinSession() để nhận push realtime
 * - handleBidEvent(): xử lý BID_UPDATE và AUCTION_CLOSED từ Push Server
 * - handleBack() và các nav: gọi BidSocketClient.leave() trước khi thoát
 */
public class AuctionRoomController extends BaseController {

    // Left: product info
    @FXML
    private Label lblProductName;
    @FXML
    private Label lblTenSP;
    @FXML
    private Label lblIdSP;
    @FXML
    private Label lblGiaMoBan;
    @FXML
    private Label lblTinhTrang;
    @FXML
    private Label lblMoTa;
    @FXML
    private Pane imgPane;
    @FXML
    private VBox attributesContainer;

    // Middle: bid history
    @FXML
    private VBox bidHistoryBox;
    @FXML private Button btnTabFeed;
    @FXML private Button btnTabChart;
    @FXML private javafx.scene.layout.Region tabFeedUnderline;
    @FXML private javafx.scene.layout.Region tabChartUnderline;

    @FXML private ScrollPane scrollFeed;
    @FXML private VBox chartPane;

    private javafx.scene.chart.LineChart<String, Number> priceChart;
    private javafx.scene.chart.XYChart.Series<String, Number> priceSeries;

    // Right: timer + price
    @FXML
    private Label lblThoiGianBatDau;
    @FXML
    private Label lblThoiGianKetThuc;
    @FXML
    private Label lblThoiGianConLai;
    @FXML
    private Label lblGiaKhoiDiem;
    @FXML
    private Label lblBuocGia;
    @FXML
    private Label lblGiaHienTai;
    @FXML
    private Label lblNguoiGiuGia;

    // Manual bid
    @FXML
    private Label lblManualToggle;
    @FXML
    private TextField manualBidField;

    // Auto bid
    @FXML
    private Label lblAutoToggle;
    @FXML
    private TextField autoBuocGiaField;
    @FXML
    private TextField autoMaxGiaField;

    private boolean manualEnabled = true;
    private boolean autoEnabled = false;

    private String sessionId;
    private double currentPrice = 0;
    private double stepPrice = 0;
    private java.time.LocalDateTime endDateTime;
    private javafx.animation.Timeline countdownTimer;
    private String currentCategory;
    private String currentAttr1;
    private String currentAttr2;

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
     * 1. Load thông tin phiên từ API server (port 8888)
     * 2. Kết nối Push Server (port 8889) để nhận BID_UPDATE realtime
     */
    public void initSession(String sessionId, String productName) {
        this.sessionId = sessionId;
        //lblProductName.setText(productName.toUpperCase());

        // 1. Load thông tin phiên ban đầu
        initChart();
        loadSessionInfo();
        // Nếu là Seller → ẩn/disable toàn bộ phần đặt giá
        if ("SELLER".equalsIgnoreCase(currentRole)) {
            manualBidField.setDisable(true);
            autoBuocGiaField.setDisable(true);
            autoMaxGiaField.setDisable(true);
            lblManualToggle.setDisable(true);
            lblAutoToggle.setDisable(true);
            // Đổi text nút submit để rõ ràng
            manualBidField.setPromptText("You are a seller and cannot place a bid.");
        }

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
                stepPrice = s.getDouble("stepPrice");

                lblGiaKhoiDiem.setText(String.format("%,.0f", s.getDouble("startPrice")));
                lblBuocGia.setText(String.format("%,.0f", s.getDouble("stepPrice")));
                lblGiaHienTai.setText(String.format("%,.0f", currentPrice));
                lblTenSP.setText("Product name: " + s.getString("itemName"));
                lblIdSP.setText("Product id: " + s.getString("itemId"));
                lblGiaMoBan.setText("Starting price: " + String.format("%,.0f", s.getDouble("startPrice")));
                lblTinhTrang.setText("Status: " + s.getString("status"));
                lblMoTa.setText("Description: " + s.optString("description", ""));

                currentCategory = s.optString("category", "");
                currentAttr1 = s.optString("attr1", "");
                currentAttr2 = s.optString("attr2", "");

                displayAttributes();

                String imageBase64 = s.optString("itemImage", "");
                if (!imageBase64.isEmpty()) {
                    try {
                        byte[] bytes = java.util.Base64.getDecoder().decode(imageBase64);
                        javafx.scene.image.Image img = new javafx.scene.image.Image(
                                new java.io.ByteArrayInputStream(bytes));
                        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                        iv.setFitWidth(260);
                        iv.setFitHeight(200);
                        iv.setPreserveRatio(true);
                        imgPane.getChildren().setAll(iv);
                    } catch (Exception ex) {
                        System.err.println("[AuctionRoom] Lỗi load ảnh: " + ex.getMessage());
                    }
                }

                // Format thời gian hiển thị
                String startTime = s.getString("startTime");
                String endTime = s.getString("endTime");
                lblThoiGianBatDau.setText(formatTime(startTime));
                lblThoiGianKetThuc.setText(formatTime(endTime));
                try {
                    String normalized = endTime.length() == 16 ? endTime + ":00" : endTime;
                    endDateTime = java.time.LocalDateTime.parse(normalized);
                    startCountdown();
                } catch (Exception ex) {
                    System.err.println("[AuctionRoom] Lỗi parse endTime countdown: " + ex.getMessage());
                }
                String winner = s.optString("currentWinner", "");
                lblNguoiGiuGia.setText("Current winner: " + (winner.isEmpty() ? "Chưa có" : winner));
                break;
            }
            new Thread(() -> {
                JSONObject res = ServerService.getAutoBidStatus(sessionId);
                if (res == null || !res.getBoolean("success")) return;
                boolean isAutoActive = res.optBoolean("active", false);
                Platform.runLater(() -> {
                    if (isAutoActive) {
                        autoEnabled = true;
                        lblAutoToggle.setText("ON");
                        lblAutoToggle.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white;"
                                + "-fx-font-size: 11px; -fx-font-weight: bold;"
                                + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
                        autoBuocGiaField.setDisable(false);
                        autoMaxGiaField.setDisable(false);

                        // Điền lại số cũ
                        autoBuocGiaField.setText(String.valueOf((long) res.getDouble("increment")));
                        autoMaxGiaField.setText(String.valueOf((long) res.getDouble("maxBid")));

                        // Khoá manual
                        manualEnabled = false;
                        lblManualToggle.setText("OFF");
                        lblManualToggle.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;"
                                + "-fx-font-size: 11px; -fx-font-weight: bold;"
                                + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
                        manualBidField.setDisable(true);
                        lblManualToggle.setDisable(true);
                    }
                });
            }).start();
            new Thread(() -> {
                System.out.println("[AuctionRoom] Đang load history cho: " + sessionId);
                JSONArray history = ServerService.getBidHistory(sessionId);
                if (history == null) return;
                Platform.runLater(() -> {
                    // Thêm từ cũ → mới (index 0 là mới nhất nên add ngược)
                    for (int j = 0; j < history.length(); j++) {
                        JSONObject h = history.getJSONObject(j);
                        String ts = h.getString("timestamp");
                        String time = formatTime(ts); // dùng lại method có sẵn
                        addBidEntry(h.getString("bidderName") + " bid "
                                        + String.format("%,.0f đ", h.getDouble("amount"))
                                        + " at " + time,
                                h.getString("bidderName").equals(currentUsername));

                        String chartTime = ts.length() >= 19 ? ts.substring(11, 19) : ts;
                        final String ct = chartTime;
                        final double amt = h.getDouble("amount");
                        Platform.runLater(() ->
                                priceSeries.getData().add(
                                        new javafx.scene.chart.XYChart.Data<>(ct, amt)));
                    }
                });
            }).start();
        });
    }
    private void displayAttributes() {
        if (attributesContainer == null) {
            // Nếu chưa có container trong FXML, tạo mới
            attributesContainer = new VBox(6);
            attributesContainer.setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 5; " +
                    "-fx-background-color: #f9f9f9; -fx-background-radius: 5;");
            // Tìm vị trí để thêm vào (ví dụ sau lblMoTa)
            VBox leftInfoBox = (VBox) lblMoTa.getParent();
            if (leftInfoBox != null) {
                leftInfoBox.getChildren().add(attributesContainer);
            }
        }

        attributesContainer.getChildren().clear();

        if (currentCategory == null || currentCategory.isEmpty()) return;

        switch (currentCategory.toUpperCase()) {
            case "FASHION" -> {
                addAttributeRow("Brand", currentAttr1);
                addAttributeRow("Size", currentAttr2);
            }
            case "ART" -> {
                addAttributeRow("Artist", currentAttr1);
                addAttributeRow("Medium", currentAttr2);
            }
            case "VEHICLE" -> {
                addAttributeRow("Brand", currentAttr1);
                addAttributeRow("Mileage (km)", currentAttr2);
            }
            case "ELECTRONICS" -> {
                addAttributeRow("Brand", currentAttr1);
                addAttributeRow("Warranty (months)", currentAttr2);
            }
        }
    }

    private void addAttributeRow(String label, String value) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 4 0;");
        Label keyLabel = new Label(label + ":");
        keyLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 100; -fx-font-size: 12px;");
        Label valueLabel = new Label(value != null && !value.isEmpty() ? value : "N/A");
        valueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        row.getChildren().addAll(keyLabel, valueLabel);
        attributesContainer.getChildren().add(row);
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
                lblNguoiGiuGia.setText("Current winner: " + event.bidderName);

                // Cập nhật endTime nếu bị anti-snipe kéo dài
                if (!event.endTime.isBlank()) {
                    lblThoiGianKetThuc.setText(formatTime(event.endTime));
                    // Cập nhật endDateTime để countdown chạy đúng
                    try {
                        String normalized = event.endTime.length() == 16 ? event.endTime + ":00" : event.endTime;
                        endDateTime = java.time.LocalDateTime.parse(normalized);
                    } catch (Exception ex) { /* ignore */ }
                }

                // Thêm vào feed lịch sử
                String now = java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy"));
                addBidEntry(event.bidderName + " bid "
                                + String.format("%,.0f", event.price)
                                + " at " + now,
                        event.bidderName.equals(currentUsername));
                addChartPoint(event.price);
            });

            case AUCTION_CLOSED -> Platform.runLater(() -> {
                addBidEntry("AUCTION ENDED", false);
                if (!event.bidderName.isEmpty() && !"NO_WINNER".equals(event.bidderName)) {
                    showNotification(getStage(bidHistoryBox),
                            "THE AUCTION HAS ENDED!\nWinner: " + event.bidderName
                                    + "\nFinal Price: " + String.format("%,.0f", event.price));
                } else {
                    showNotification(getStage(bidHistoryBox),
                            "THE AUCTION HAS ENDED!\nThere is no winner.");
                }
                // Vô hiệu hoá nút đặt giá
                manualBidField.setDisable(true);
                autoBuocGiaField.setDisable(true);
                autoMaxGiaField.setDisable(true);
            });

            case ANTI_SNIPE -> Platform.runLater(() -> {
                addBidEntry("Session extended by " + event.extendMinutes
                        + " min — " + event.bidderName
                        + " bid at " + formatTime(event.snipeTime), false);
            });

            default -> System.out.println("[AuctionRoom] Event không xác định: " + event);
        }
    }

    // ------------------------------------------------------------------ //
    //  Thêm dòng vào feed lịch sử (dùng nội bộ)
    // ------------------------------------------------------------------ //

    private void addBidEntry(String message, boolean isMe) {
        Label entry = new Label("▶ " + message);
        entry.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (isMe ? "#16A34A; -fx-font-weight: bold;" : "#333;"));
        entry.setWrapText(true);
        bidHistoryBox.getChildren().add(0, entry);
    }

    // ── Public — giữ lại để tương thích nếu có chỗ khác gọi ────────────
    public void addBidEvent(String message) {
        addBidEntry(message, false);
    }

    public void updateCurrentPrice(double price, String holderName) {
        currentPrice = price;
        lblGiaHienTai.setText(String.format("%,.0f", price));
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
        // Nếu bật manual thì tắt auto
        if (manualEnabled && autoEnabled) {
            autoEnabled = false;
            lblAutoToggle.setText("OFF");
            lblAutoToggle.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;"
                    + "-fx-font-size: 11px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
            autoBuocGiaField.setDisable(true);
            autoMaxGiaField.setDisable(true);
        }
        lblManualToggle.setText(manualEnabled ? "ON" : "OFF");
        lblManualToggle.setStyle("-fx-background-color: " + (manualEnabled ? "#22C55E" : "#EF4444") + ";"
                + "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
        manualBidField.setDisable(!manualEnabled);
    }

    @FXML
    private void handleManualBid() {
        if ("SELLER".equalsIgnoreCase(currentRole)) {
            showNotification(getStage(bidHistoryBox),
                    "YOU ARE A SELLER!\nYOU CANNOT PLACE A BID IN YOUR OWN AUCTION.");
            return;
        }

        if (!manualEnabled) return;

        //không cho đặt nếu đang là current winner
        if (currentUsername != null && currentUsername.equals(
                lblNguoiGiuGia.getText().replace("Current winner: ", "").trim())) {
            showNotification(getStage(bidHistoryBox),
                    "“YOU ARE CURRENTLY THE HIGHEST BIDDER!\nYOU CANNOT BID AGAIN");
            return;
        }


        String input = manualBidField.getText().trim();
        if (input.isEmpty()) return;

        double bid;
        try {
            bid = Double.parseDouble(input.replace(",", "").replace("đ", "").trim());
        } catch (NumberFormatException e) {
            showNotification(getStage(bidHistoryBox), "“INVALID BID PRICE!");
            return;
        }

        double minValidBid = currentPrice + stepPrice;
        if (bid < minValidBid) {
            showNotification(getStage(bidHistoryBox),
                    "THE BID PRICE MUST BE AT LEAST " + String.format("%,.0f", minValidBid)
                            + "\n(Current Price: " + String.format("%,.0f", currentPrice)
                            + " + Bid increment: " + String.format("%,.0f", stepPrice) + ")");
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
            showNotification(getStage(bidHistoryBox), "BID PLACEMENT FAILED! Please try again.");
        }
    }

    // ------------------------------------------------------------------ //
    //  Auto bid
    // ------------------------------------------------------------------ //

    @FXML
    private void handleToggleAuto() {
        autoEnabled = !autoEnabled;
        if (autoEnabled && manualEnabled) {
            manualEnabled = false;
            lblManualToggle.setText("OFF");
            lblManualToggle.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;"
                    + "-fx-font-size: 11px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
            manualBidField.setDisable(true);
        }
        if (!autoEnabled) {
            // Tắt auto → enable lại manual toggle, huỷ auto-bid trên server
            lblManualToggle.setDisable(false);
            ServerService.cancelAutoBid(sessionId); // THÊM — huỷ trên server
        }
        lblAutoToggle.setText(autoEnabled ? "ON" : "OFF");
        lblAutoToggle.setStyle("-fx-background-color: " + (autoEnabled ? "#22C55E" : "#EF4444") + ";"
                + "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
        autoBuocGiaField.setDisable(!autoEnabled);
        autoMaxGiaField.setDisable(!autoEnabled);
    }

    @FXML
    private void handleSaveAutoConfig() {
        if (!autoEnabled) return;
        String buocGia = autoBuocGiaField.getText().trim();
        String maxGia = autoMaxGiaField.getText().trim();
        if (buocGia.isEmpty() || maxGia.isEmpty()) {
            showNotification(getStage(bidHistoryBox), "PLEASE FILL IN ALL FIELDS!");
            return;
        }
        boolean ok = ServerService.setAutoBid(
                sessionId,
                Double.parseDouble(buocGia),
                Double.parseDouble(maxGia)
        );
        if (ok) {
            showNotification(getStage(bidHistoryBox), "AUTO BIDDING ENABLED!");
            // Khoá hẳn manual khi auto đang chạy
            manualEnabled = false;
            lblManualToggle.setText("OFF");
            lblManualToggle.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;"
                    + "-fx-font-size: 11px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 20px; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
            manualBidField.setDisable(true);
            lblManualToggle.setDisable(true); // disable luôn nút toggle
        } else {
            showNotification(getStage(bidHistoryBox), "AUTO-BID SETUP FAILED!");
        }
    }

    // ------------------------------------------------------------------ //
    //  Report
    // ------------------------------------------------------------------ //

    @FXML
    private void handleReport() {
        showNotification(getStage(bidHistoryBox), "REPORT SUBMITTED!");
    }

    // ------------------------------------------------------------------ //
    //  Navigation — leave() trước khi rời phòng
    // ------------------------------------------------------------------ //

    @FXML
    private void handleBack() {
        BidSocketClient.getInstance().leave();
        navigateTo("/fxml/Auctions.fxml", getStage(bidHistoryBox));
        if (countdownTimer != null) countdownTimer.stop();
    }

    @FXML
    private void handleHome() {
        BidSocketClient.getInstance().leave();
        goHome(getStage(bidHistoryBox));
        if (countdownTimer != null) countdownTimer.stop();
    }

    @FXML
    private void handleAuctions() {
        BidSocketClient.getInstance().leave();
        navigateTo("/fxml/Auctions.fxml", getStage(bidHistoryBox));
        if (countdownTimer != null) countdownTimer.stop();
    }

    @FXML
    private void handleBidderCentre() {
        BidSocketClient.getInstance().leave();
        navigateTo("/fxml/BidderCentre.fxml", getStage(bidHistoryBox));
        if (countdownTimer != null) countdownTimer.stop();
    }

    @FXML
    private void handleSettings() {
        BidSocketClient.getInstance().leave();
        goSettings(getStage(bidHistoryBox));
        if (countdownTimer != null) countdownTimer.stop();
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    /**
     * Format chuỗi ISO datetime từ server sang dạng hiển thị dễ đọc.
     */
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

    private void startCountdown() {
        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    if (endDateTime == null) return;
                    java.time.Duration remaining = java.time.Duration.between(
                            java.time.LocalDateTime.now(), endDateTime);
                    if (remaining.isNegative() || remaining.isZero()) {
                        lblThoiGianConLai.setText("00:00");
                        countdownTimer.stop();
                    } else {
                        long h = remaining.toHours();
                        long m = remaining.toMinutesPart();
                        long s = remaining.toSecondsPart();
                        String txt = h > 0
                                ? String.format("%02d:%02d:%02d", h, m, s)
                                : String.format("%02d:%02d", m, s);
                        lblThoiGianConLai.setText(txt);
                    }
                })
        );
        countdownTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        countdownTimer.play();
    }
    private void initChart() {
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Price (đ)");

        priceChart = new javafx.scene.chart.LineChart<>(xAxis, yAxis);
        priceChart.setTitle("Realtime Price Curve");
        priceChart.setAnimated(false); // tắt animation để update nhanh
        priceChart.setCreateSymbols(true);

        priceSeries = new javafx.scene.chart.XYChart.Series<>();
        priceSeries.setName("Bid price");
        priceChart.getData().add(priceSeries);

        VBox.setVgrow(priceChart, javafx.scene.layout.Priority.ALWAYS);
        chartPane.getChildren().setAll(priceChart);
    }
    private void addChartPoint(double price) {
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        priceSeries.getData().add(
                new javafx.scene.chart.XYChart.Data<>(time, price));
    }
    @FXML
    private void handleTabFeed() {
        scrollFeed.setVisible(true);  scrollFeed.setManaged(true);
        chartPane.setVisible(false);  chartPane.setManaged(false);
        // Đổi underline tab
        tabFeedUnderline.getStyleClass().setAll("auction-tab-underline");
        tabChartUnderline.getStyleClass().setAll("auction-tab-underline-inactive");
        // Giữ lại style inline cho btnTabFeed/btnTabChart (không làm gì vì hidden)
    }

    @FXML
    private void handleTabChart() {
        chartPane.setVisible(true);   chartPane.setManaged(true);
        scrollFeed.setVisible(false); scrollFeed.setManaged(false);
        tabChartUnderline.getStyleClass().setAll("auction-tab-underline");
        tabFeedUnderline.getStyleClass().setAll("auction-tab-underline-inactive");
    }

}
