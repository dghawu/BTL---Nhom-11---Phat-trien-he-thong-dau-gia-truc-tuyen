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
        // TODO: List<Item> wonItems = ServerService.getWonProducts(currentUsername);
        // Mock
        for (int i = 1; i <= 3; i++) {
            wonProductGrid.getChildren().add(buildWonCard("Sản phẩm " + i, "SP00" + i));
        }
    }

    private VBox buildWonCard(String ten, String id) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(260);

        Label title = new Label(ten.toUpperCase());
        title.getStyleClass().add("product-card-title");
        title.setMaxWidth(Double.MAX_VALUE);

        Pane img = new Pane();
        img.getStyleClass().add("product-card-image");
        img.setPrefHeight(160);

        VBox info = new VBox(4);
        info.getStyleClass().add("product-card-info");
        info.getChildren().addAll(
                new Label("Tên sản phẩm: " + ten),
                new Label("Id sản phẩm: " + id)
        );

        card.getChildren().addAll(title, img, info);
        return card;
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
