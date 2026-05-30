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
        // Cấu hình cột
        colMa.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                ((org.json.JSONObject) c.getValue()).optString("id", "").substring(0, Math.min(8,
                        ((org.json.JSONObject) c.getValue()).optString("id", "").length())) + "..."));
        colPhien.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                ((org.json.JSONObject) c.getValue()).optString("auctionId", "").substring(0, Math.min(8,
                        ((org.json.JSONObject) c.getValue()).optString("auctionId", "").length())) + "..."));
        colSanPham.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                ((org.json.JSONObject) c.getValue()).optString("itemName", "")));
        colThoiGian.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                ((org.json.JSONObject) c.getValue()).optString("timestamp", "").replace("T", " ")));
        colTinhTrang.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                ((org.json.JSONObject) c.getValue()).optString("status", "")));
        colBaoCao.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("Báo cáo"));

        // Load data
        new Thread(() -> {
            org.json.JSONArray txList = com.example.socket.ServerService.getMyTransactions();
            if (txList == null) return;
            javafx.application.Platform.runLater(() -> {
                javafx.collections.ObservableList<Object> items =
                        javafx.collections.FXCollections.observableArrayList();
                for (int i = 0; i < txList.length(); i++) {
                    items.add(txList.getJSONObject(i));
                }
                giaoDichTable.setItems(items);
            });
        }).start();
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
                new Label("Product: " + ten),
                new Label("Winning bid: " + String.format("%,.0f đ", finalPrice)),
                new Label("Status: " + status)
        );

        // Nút theo trạng thái
        if ("FINISHED".equals(status)) {
            Button btnConfirm = new Button("CONFIRM WIN");
            btnConfirm.getStyleClass().add("btn-primary");
            btnConfirm.setMaxWidth(Double.MAX_VALUE);
            btnConfirm.setOnAction(e -> {
                boolean ok = com.example.socket.ServerService.confirmWin(sessionId);
                if (ok) {
                    showNotification(getStage(contentStack), "Confirmed! You have 24 hours to make the payment..");
                    loadWonProducts();
                }
            });
            info.getChildren().add(btnConfirm);
        } else if ("PAYING".equals(status)) {
            Button btnPay = new Button("PAYMENT");
            btnPay.getStyleClass().add("btn-primary");
            btnPay.setMaxWidth(Double.MAX_VALUE);
            btnPay.setOnAction(e -> showPayDialog(sessionId, finalPrice));
            info.getChildren().add(btnPay);
        } else if ("PAID".equals(status)) {
            Label lbl = new Label("✅ Paid");
            lbl.setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold;");
            info.getChildren().add(lbl);
        }

        card.getChildren().addAll(title, img, info);
        return card;
    }

    private void showPayDialog(String sessionId, double amount) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Payment");

        javafx.scene.control.TextField tfAmount = new javafx.scene.control.TextField();
        tfAmount.setPromptText("Enter amount: " + String.format("%,.0f đ", amount));

        javafx.scene.control.Button btnOk = new javafx.scene.control.Button("PAYMENT");

        btnOk.setStyle("""
    -fx-background-color: #111111;
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-background-radius: 25;
    -fx-border-radius: 25;
    -fx-padding: 10 30 10 30;
""");
        btnOk.setOnAction(e -> {
            boolean ok = com.example.socket.ServerService.pay(sessionId, amount);
            dialog.close();
            showNotification(getStage(contentStack), ok ? "PAYMENT SUCCESSFUL!" : "PAYMENT FAILED!");
            if (ok) loadWonProducts();
        });

        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(12,
                new javafx.scene.control.Label("Amount to pay: " + String.format("%,.0f đ", amount)),
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
        showNotification(getStage(contentStack), "PAYMENT SUCCESSFUL!");
        loadGiaoDich();
    }

    private void handleBaoCao(int index) {
        // TODO: mở dialog báo cáo sự cố cho giao dịch tại index
        showNotification(getStage(contentStack), "INCIDENT REPORT SUBMITTED!");
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
