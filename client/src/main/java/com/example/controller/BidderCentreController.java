package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * BidderCentreController - BidderCentre.fxml
 * Sidebar: Quản lý giao dịch | Sản phẩm đã đấu giá thành công
 */
public class BidderCentreController extends com.example.controller.BaseController {

    @FXML
    private Button btnGiaoDich;
    @FXML
    private Button btnSanPhamDauGia;
    @FXML
    private StackPane contentStack;
    @FXML
    private VBox viewGiaoDich;
    @FXML
    private VBox viewSanPham;
    @FXML
    private TableView<Object> giaoDichTable;
    @FXML
    private TableColumn<Object, String> colMa;
    @FXML
    private TableColumn<Object, String> colPhien;
    @FXML
    private TableColumn<Object, String> colSanPham;
    @FXML
    private TableColumn<Object, String> colTinhTrang;
    @FXML
    private TableColumn<Object, String> colThoiGian;
    @FXML
    private TableColumn<Object, String> colBaoCao;
    @FXML
    private FlowPane wonProductGrid;

    @FXML
    public void initialize() {
        handleShowGiaoDich();
    }

    // ------------------------------------------------------------------ //
    //  Nav
    // ------------------------------------------------------------------ //
    @FXML
    private void handleHome() {
        goHome(getStage(contentStack));
    }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(contentStack));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(contentStack));
    }

    // ------------------------------------------------------------------ //
    //  Sidebar
    // ------------------------------------------------------------------ //
    @FXML
    private void handleShowGiaoDich() {
        viewGiaoDich.setVisible(true);
        viewGiaoDich.setManaged(true);
        viewSanPham.setVisible(false);
        viewSanPham.setManaged(false);
        setActiveTab(btnGiaoDich, btnSanPhamDauGia);
        loadGiaoDich();
    }

    @FXML
    private void handleShowSanPhamDauGia() {
        viewSanPham.setVisible(true);
        viewSanPham.setManaged(true);
        viewGiaoDich.setVisible(false);
        viewGiaoDich.setManaged(false);
        setActiveTab(btnSanPhamDauGia, btnGiaoDich);
        loadWonProducts();
    }

    // ------------------------------------------------------------------ //
    //  Data
    // ------------------------------------------------------------------ //
    private void loadGiaoDich() {
        giaoDichTable.getItems().clear();
        // TODO: List<Transaction> txList = ServerService.getMyTransactions(currentUsername);
        // Dùng custom TableCell để hiển thị nút THANH TOÁN khi tình trạng "Chưa thanh toán"

        // Cấu hình cột Tình trạng: hiển thị button nếu chưa thanh toán
        colTinhTrang.setCellFactory(col -> new TableCell<>() {
            private final Button btnThanhToan = new Button("THANH TOÁN");

            {
                btnThanhToan.getStyleClass().add("btn-primary");
                btnThanhToan.setOnAction(e -> {
                    Object item = getTableView().getItems().get(getIndex());
                    handleThanhToan(item);
                });
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                if (status.contains("Chưa thanh toán")) {
                    VBox box = new VBox(4);
                    box.getChildren().addAll(new Label(status), btnThanhToan);
                    setGraphic(box);
                    setText(null);
                } else {
                    setText(status);
                    setGraphic(null);
                }
            }
        });

        // Cột Báo cáo: link text
        colBaoCao.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setGraphic(null);
                    return;
                }
                Label lbl = new Label(val);
                lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px; -fx-cursor: hand;");
                lbl.setOnMouseClicked(e -> handleBaoCao(getIndex()));
                setGraphic(lbl);
                setText(null);
            }
        });
    }

    private void loadWonProducts() {
        wonProductGrid.getChildren().clear();
        new Thread(() -> {
            org.json.JSONArray sessions = com.example.socket.ServerService.getAllSessions("ALL");
            // Lấy thêm FINISHED/PAYING/PAID cho bidder centre
            org.json.JSONArray myFinished = com.example.socket.ServerService.getMyWonSessions();
            if (myFinished == null) return;
            javafx.application.Platform.runLater(() -> {
                for (int i = 0; i < myFinished.length(); i++) {
                    org.json.JSONObject s = myFinished.getJSONObject(i);
                    wonProductGrid.getChildren().add(buildWonCard(s));
                }
            });
        }).start();
    }

    private VBox buildWonCard(org.json.JSONObject s) {
        String sessionId = s.getString("id");
        String ten = s.getString("itemName");
        String status = s.getString("status");
        double finalPrice = s.getDouble("currentPrice");
        String imageBase64 = s.optString("itemImage", "");

        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(260);

        Label title = new Label(ten.toUpperCase());
        title.getStyleClass().add("product-card-title");
        title.setMaxWidth(Double.MAX_VALUE);

        Pane img = new Pane();
        img.getStyleClass().add("product-card-image");
        img.setPrefHeight(160);

        if (!imageBase64.isEmpty()) {
            try {
                byte[] bytes = java.util.Base64.getDecoder().decode(imageBase64);
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(
                        new javafx.scene.image.Image(new java.io.ByteArrayInputStream(bytes)));
                iv.setFitWidth(260);
                iv.setFitHeight(160);
                iv.setPreserveRatio(true);
                img.getChildren().setAll(iv);
            } catch (Exception ignored) {
            }
        }

        VBox info = new VBox(4);
        info.getStyleClass().add("product-card-info");
        info.getChildren().addAll(
                new Label("Sản phẩm: " + ten),
                new Label("Giá thắng: " + String.format("%,.0f đ", finalPrice)),
                new Label("Trạng thái: " + status)
        );

        // Nút theo trạng thái
        if ("FINISHED".equals(status)) {
            Button btnConfirm = new Button("XÁC NHẬN THẮNG");
            btnConfirm.getStyleClass().add("btn-primary");
            btnConfirm.setMaxWidth(Double.MAX_VALUE);
            btnConfirm.setOnAction(e -> {
                boolean ok = com.example.socket.ServerService.confirmWin(sessionId);
                if (ok) {
                    showNotification(getStage(contentStack), "Đã xác nhận! Bạn có 24h để thanh toán.");
                    loadWonProducts();
                }
            });
            info.getChildren().add(btnConfirm);
        } else if ("PAYING".equals(status)) {
            Button btnPay = new Button("THANH TOÁN");
            btnPay.getStyleClass().add("btn-primary");
            btnPay.setMaxWidth(Double.MAX_VALUE);
            btnPay.setOnAction(e -> showPayDialog(sessionId, finalPrice));
            info.getChildren().add(btnPay);
        } else if ("PAID".equals(status)) {
            Label lbl = new Label("✅ Đã thanh toán");
            lbl.setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold;");
            info.getChildren().add(lbl);
        }

        card.getChildren().addAll(title, img, info);
        return card;
    }

    private void showPayDialog(String sessionId, double amount) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Thanh toán");

        javafx.scene.control.TextField tfAmount = new javafx.scene.control.TextField();
        tfAmount.setPromptText("Nhập số tiền: " + String.format("%,.0f đ", amount));

        javafx.scene.control.Button btnOk = new javafx.scene.control.Button("THANH TOÁN");
        btnOk.setStyle("-fx-background-color: #111; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        btnOk.setOnAction(e -> {
            boolean ok = com.example.socket.ServerService.pay(sessionId, amount);
            dialog.close();
            showNotification(getStage(contentStack), ok ? "THANH TOÁN THÀNH CÔNG!" : "THANH TOÁN THẤT BẠI!");
            if (ok) loadWonProducts();
        });

        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(12,
                new javafx.scene.control.Label("Số tiền cần thanh toán: " + String.format("%,.0f đ", amount)),
                tfAmount, btnOk);
        layout.setStyle("-fx-padding: 24; -fx-alignment: center;");
        dialog.setScene(new javafx.scene.Scene(layout, 320, 160));
        dialog.showAndWait();
    }

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //
    private void handleThanhToan(Object transaction) {
        // TODO: ServerService.processPayment(transactionId)
        showNotification(getStage(contentStack), "THANH TOÁN THÀNH CÔNG!");
        loadGiaoDich();
    }

    private void handleBaoCao(int index) {
        // TODO: mở dialog báo cáo sự cố cho giao dịch tại index
        showNotification(getStage(contentStack), "ĐÃ GỬI BÁO CÁO SỰ CỐ!");
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //
    private void setActiveTab(Button active, Button inactive) {
        active.getStyleClass().remove("sidebar-item");
        if (!active.getStyleClass().contains("sidebar-item-active"))
            active.getStyleClass().add("sidebar-item-active");
        inactive.getStyleClass().remove("sidebar-item-active");
        if (!inactive.getStyleClass().contains("sidebar-item"))
            inactive.getStyleClass().add("sidebar-item");
    }
}
