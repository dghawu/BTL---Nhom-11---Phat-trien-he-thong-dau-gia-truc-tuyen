package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import com.example.socket.ServerService;

/**
 * SellerProductDetailController - SellerProductDetail.fxml
 * Nhận data từ SellerProductListController.openDetail()
 */
public class SellerProductDetailController extends com.example.controller.BaseController {

    @FXML private Label lblTen;
    @FXML private Label lblId;
    @FXML private Label lblPhanLoai;
    @FXML private Label lblNgayMoBan;
    @FXML private Label lblGia;
    @FXML private Label lblMoTa;
    @FXML private Label lblTinhTrang;
    @FXML private Pane  imgPane;

    // TextFields cho edit mode
    private TextField txtTen;
    private TextField txtGia;
    private TextField txtMoTa;
    private TextField txtTinhTrang;
    private boolean isEditMode = false;
    private String currentId;

    public void initData(String id, String ten, String phanLoai, String gia, String moTa, String tinhTrang) {
        this.currentId = id;
        lblId.setText(id);
        lblTen.setText(ten);
        lblPhanLoai.setText(phanLoai != null && !phanLoai.isEmpty() ? phanLoai : "N/A");
        lblGia.setText(gia);
        lblMoTa.setText(moTa != null && !moTa.isEmpty() ? moTa : "Chưa có mô tả");
        lblTinhTrang.setText(tinhTrang);
    }

    @FXML private void handleHome()        { goHome(getStage(lblTen)); }
    @FXML private void handleAuctions()    { goAuctions(getStage(lblTen)); }
    @FXML private void handleSettings()    { goSettings(getStage(lblTen)); }
    @FXML private void handleThemSanPham() { navigateTo("/fxml/SellerAddProduct.fxml", getStage(lblTen)); }
    @FXML private void handleXemSanPham()  { navigateTo("/fxml/SellerProductList.fxml", getStage(lblTen)); }
    @FXML private void handleTaoPhien()    { navigateTo("/fxml/SellerCreateSession.fxml", getStage(lblTen)); }
    @FXML private void handleXemPhien()    { navigateTo("/fxml/SellerSessionList.fxml", getStage(lblTen)); }

    @FXML
    private void handleEdit() {
        if (!isEditMode) {
            // Chuyển sang edit mode
            enableEditMode();
            isEditMode = true;
        } else {
            // Quay lại view mode (cancel)
            disableEditMode();
            isEditMode = false;
        }
    }

    private void enableEditMode() {
        // Tạo TextFields với giá trị hiện tại
        txtTen = new TextField(lblTen.getText());
        txtGia = new TextField(lblGia.getText());
        txtMoTa = new TextField(lblMoTa.getText());
        txtTinhTrang = new TextField(lblTinhTrang.getText());

        // Set style cho TextFields (nếu cần)
        txtTen.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        txtGia.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        txtMoTa.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        txtTinhTrang.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

        // Thay thế Labels bằng TextFields
        lblTen.setGraphic(txtTen);
        lblTen.setText("");

        lblGia.setGraphic(txtGia);
        lblGia.setText("");

        lblMoTa.setGraphic(txtMoTa);
        lblMoTa.setText("");

        lblTinhTrang.setGraphic(txtTinhTrang);
        lblTinhTrang.setText("");
    }

    private void disableEditMode() {
        // Quay lại view mode: hiển thị Labels như cũ
        lblTen.setGraphic(null);
        lblTen.setText(txtTen != null ? txtTen.getText() : "");

        lblGia.setGraphic(null);
        lblGia.setText(txtGia != null ? txtGia.getText() : "");

        lblMoTa.setGraphic(null);
        lblMoTa.setText(txtMoTa != null ? txtMoTa.getText() : "");

        lblTinhTrang.setGraphic(null);
        lblTinhTrang.setText(txtTinhTrang != null ? txtTinhTrang.getText() : "");
    }

    @FXML
    private void handleSave() {
        if (isEditMode) {
            // Lấy dữ liệu từ TextFields
            String tenMoi = txtTen.getText().trim();
            String giaMoi = txtGia.getText().trim();
            String moTaMoi = txtMoTa.getText().trim();
            String tinhTrangMoi = txtTinhTrang.getText().trim();
            String productId = lblId.getText();

            // Kiểm tra dữ liệu không để trống
            if (tenMoi.isEmpty() || giaMoi.isEmpty() || tinhTrangMoi.isEmpty()) {
                showNotification(getStage(lblTen), "Vui lòng điền đầy đủ thông tin!");
                return;
            }

            // Gửi update lên server
            try {
                boolean ok = ServerService.updateItem(currentId, tenMoi, moTaMoi, giaMoi, tinhTrangMoi);

                if (ok) {
                    // Cập nhật UI
                    lblTen.setText(tenMoi);
                    lblGia.setText(giaMoi);
                    lblMoTa.setText(moTaMoi);
                    lblTinhTrang.setText(tinhTrangMoi);

                    disableEditMode();
                    isEditMode = false;
                    showNotification(getStage(lblTen), "Cập nhật sản phẩm thành công!");
                } else {
                    showNotification(getStage(lblTen), "Cập nhật thất bại!");
                }
            } catch (Exception e) {
                showNotification(getStage(lblTen), "Lỗi: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
