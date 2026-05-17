package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * SellerSessionDetailController - SellerSessionDetail.fxml
 * Hiển thị chi tiết phiên đấu giá + cho phép edit
 */
public class SellerSessionDetailController extends com.example.controller.BaseController {

    @FXML
    private Label lblSessionId;
    @FXML
    private Label lblTenSP;
    @FXML
    private Label lblThoiGianMo;
    @FXML
    private Label lblThoiGianDong;
    @FXML
    private Label lblGiaKhoiDiem;
    @FXML
    private Label lblBuocGia;
    @FXML
    private Label lblTrangThai;
    @FXML
    private Pane imgPane;

    // TextFields cho edit mode
    private TextField txtThoiDong;
    private TextField txtBuocGia;

    private boolean isEditMode = false;
    private String currentSessionId;
    private byte[] newImageData = null;

    /**
     * Init data với tất cả thông tin cần thiết
     */
    public void initData(
            String sessionId,
            String tenSP,
            String thoiGianMo,
            String thoiGianDong,
            String gia,
            String buocGia,
            String trangThai,
            String imageDataBase64
    ) {
        this.currentSessionId = sessionId;

        lblSessionId.setText(sessionId != null && !sessionId.isEmpty() ? sessionId : "---");
        lblTenSP.setText(tenSP);
        lblThoiGianMo.setText(thoiGianMo);
        lblThoiGianDong.setText(thoiGianDong != null && !thoiGianDong.isEmpty() ? thoiGianDong : "---");
        lblGiaKhoiDiem.setText(gia);
        lblBuocGia.setText(buocGia != null && !buocGia.isEmpty() ? buocGia : "---");
        lblTrangThai.setText(trangThai);

        // Load image từ base64
        if (imageDataBase64 != null && !imageDataBase64.isEmpty()) {
            try {
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(imageDataBase64);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(decodedBytes);
                Image img = new Image(bais);

                ImageView iv = new ImageView(img);
                iv.setFitWidth(200);
                iv.setFitHeight(200);
                iv.setPreserveRatio(true);
                imgPane.getChildren().setAll(iv);
            } catch (Exception e) {
                e.printStackTrace();
                imgPane.getChildren().clear();
            }
        }
    }

    @FXML
    private void handleHome() {
        goHome(getStage(lblTenSP));
    }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(lblTenSP));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(lblTenSP));
    }

    @FXML
    private void handleThemSanPham() {
        navigateTo("/fxml/SellerAddProduct.fxml", getStage(lblTenSP));
    }

    @FXML
    private void handleXemSanPham() {
        navigateTo("/fxml/SellerProductList.fxml", getStage(lblTenSP));
    }

    @FXML
    private void handleTaoPhien() {
        navigateTo("/fxml/SellerCreateSession.fxml", getStage(lblTenSP));
    }

    @FXML
    private void handleXemPhien() {
        navigateTo("/fxml/SellerSessionList.fxml", getStage(lblTenSP));
    }

    @FXML
    private void handleEdit() {
        if (!isEditMode) {
            enableEditMode();
            isEditMode = true;
        } else {
            disableEditMode();
            isEditMode = false;
            newImageData = null;
        }
    }

    /**
     * Bật edit mode: hiển thị TextFields thay Labels
     */
    private void enableEditMode() {
        // Tạo TextFields với giá trị hiện tại
        txtThoiDong = new TextField(lblThoiGianDong.getText());
        txtBuocGia = new TextField(lblBuocGia.getText());

        // Set style
        txtThoiDong.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        txtBuocGia.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

        // Thay Labels bằng TextFields
        lblThoiGianDong.setGraphic(txtThoiDong);
        lblThoiGianDong.setText("");

        lblBuocGia.setGraphic(txtBuocGia);
        lblBuocGia.setText("");
    }

    /**
     * Tắt edit mode: hiển thị Labels như cũ
     */
    private void disableEditMode() {
        lblThoiGianDong.setGraphic(null);
        lblThoiGianDong.setText(txtThoiDong != null ? txtThoiDong.getText() : "---");

        lblBuocGia.setGraphic(null);
        lblBuocGia.setText(txtBuocGia != null ? txtBuocGia.getText() : "---");
    }

    /**
     * Cho phép chọn ảnh mới trong edit mode
     */
    @FXML
    private void handleChooseNewImage() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Chọn ảnh phiên đấu giá mới");
        fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File file = fc.showOpenDialog(getStage(imgPane));

        if (file != null) {
            try {
                newImageData = java.nio.file.Files.readAllBytes(file.toPath());

                Image img = new Image(file.toURI().toString());
                ImageView iv = new ImageView(img);
                iv.setFitWidth(200);
                iv.setFitHeight(200);
                iv.setPreserveRatio(true);
                imgPane.getChildren().setAll(iv);

            } catch (Exception e) {
                e.printStackTrace();
                showNotification(getStage(lblTenSP), "Lỗi: Không thể đọc file ảnh!");
                newImageData = null;
            }
        }
    }

    @FXML
    private void handleSave() {
        if (isEditMode) {
            String newEndTime = txtThoiDong.getText().trim();
            String newStepPriceStr = txtBuocGia.getText().trim();

            if (newEndTime.isEmpty() || newStepPriceStr.isEmpty()) {
                showNotification(getStage(lblTenSP), "Vui lòng điền đầy đủ thông tin!");
                return;
            }

            try {
                double newStepPrice = Double.parseDouble(newStepPriceStr);

                // Gọi update session
                boolean ok = com.example.socket.ServerService.updateSession(
                        currentSessionId, newEndTime, newStepPrice
                );

                if (ok) {
                    // Cập nhật UI
                    lblThoiGianDong.setText(newEndTime);
                    lblBuocGia.setText(newStepPriceStr);

                    disableEditMode();
                    isEditMode = false;
                    newImageData = null;
                    showNotification(getStage(lblTenSP), "Cập nhật phiên thành công!");
                } else {
                    showNotification(getStage(lblTenSP), "Cập nhật thất bại!");
                }
            } catch (NumberFormatException e) {
                showNotification(getStage(lblTenSP), "Bước giá phải là số!");
            }
        }
    }
}