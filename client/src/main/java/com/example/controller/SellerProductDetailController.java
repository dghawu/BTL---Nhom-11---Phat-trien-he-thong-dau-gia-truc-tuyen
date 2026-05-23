package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * SellerProductDetailController - SellerProductDetail.fxml
 * Nhận data từ SellerProductListController.openDetail()
 * Có thể edit thông tin sản phẩm + upload ảnh mới
 */
public class SellerProductDetailController extends com.example.controller.BaseController {

    @FXML
    private Label lblTen;
    @FXML
    private Label lblId;
    @FXML
    private Label lblPhanLoai;
    @FXML
    private Label lblNgayMoBan;
    @FXML
    private Label lblGia;
    @FXML
    private Label lblMoTa;
    @FXML
    private Label lblTinhTrang;
    @FXML
    private Pane imgPane;
    @FXML
    private javafx.scene.control.Button btnChooseImage;

    // TextFields cho edit mode
    private TextField txtTen;
    private TextField txtGia;
    private TextField txtMoTa;
    private TextField txtTinhTrang;
    private String currentImageBase64 = "";

    private boolean isEditMode = false;
    private String currentId;
    private byte[] newImageData = null;

    public void initData(String id, String ten, String phanLoai, String gia, String moTa, String tinhTrang, String imageBase64) {
        this.currentId = id;
        this.currentImageBase64 = imageBase64 != null ? imageBase64 : "";
        lblId.setText(id);
        lblTen.setText(ten);
        lblPhanLoai.setText(phanLoai != null && !phanLoai.isEmpty() ? phanLoai : "N/A");
        lblGia.setText(gia);
        lblMoTa.setText(moTa != null && !moTa.isEmpty() ? moTa : "Chưa có mô tả");
        lblTinhTrang.setText(tinhTrang);
        displayCurrentImage();
    }

    @FXML
    private void handleHome() {
        goHome(getStage(lblTen));
    }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(lblTen));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(lblTen));
    }

    @FXML
    private void handleThemSanPham() {
        navigateTo("/fxml/SellerAddProduct.fxml", getStage(lblTen));
    }

    @FXML
    private void handleXemSanPham() {
        navigateTo("/fxml/SellerProductList.fxml", getStage(lblTen));
    }

    @FXML
    private void handleTaoPhien() {
        navigateTo("/fxml/SellerCreateSession.fxml", getStage(lblTen));
    }

    @FXML
    private void handleXemPhien() {
        navigateTo("/fxml/SellerSessionList.fxml", getStage(lblTen));
    }

    @FXML
    private void handleEdit() {
        if (!isEditMode) {
            // Chuyển sang edit mode
            enableEditMode();
            isEditMode = true;
            btnChooseImage.setVisible(true);
        } else {
            // Quay lại view mode (cancel)
            disableEditMode();
            isEditMode = false;
            newImageData = null;
            btnChooseImage.setVisible(false);
        }
    }

    private void enableEditMode() {
        // Tạo TextFields với giá trị hiện tại
        txtTen = new TextField(lblTen.getText());
        txtGia = new TextField(lblGia.getText());
        txtMoTa = new TextField(lblMoTa.getText());
        txtTinhTrang = new TextField(lblTinhTrang.getText());

        // Set style cho TextFields
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

    private void displayCurrentImage() {
        if (currentImageBase64 != null && !currentImageBase64.isEmpty()) {
            try {
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(currentImageBase64);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(decodedBytes);
                Image img = new Image(bais);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(200);
                iv.setFitHeight(200);
                iv.setPreserveRatio(true);
                imgPane.getChildren().setAll(iv);
            } catch (Exception e) {
                System.err.println("[SellerProductDetailController] Lỗi decode image: " + e.getMessage());
                imgPane.getChildren().clear();
            }
        } else {
            imgPane.getChildren().clear();
        }
    }

    /**
     * Cho phép chọn ảnh sản phẩm mới trong edit mode
     */
    @FXML
    private void handleChooseNewImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh sản phẩm mới");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fc.showOpenDialog(getStage(imgPane));

        if (file != null) {
            try {
                newImageData = java.nio.file.Files.readAllBytes(file.toPath());

                // Hiển thị preview ảnh mới
                Image img = new Image(file.toURI().toString());
                ImageView iv = new ImageView(img);
                iv.setFitWidth(200);
                iv.setFitHeight(200);
                iv.setPreserveRatio(true);
                imgPane.getChildren().setAll(iv);

            } catch (Exception e) {
                e.printStackTrace();
                showNotification(getStage(lblTen), "Lỗi: Không thể đọc file ảnh!");
                newImageData = null;
            }
        }
    }

    @FXML
    private void handleSave() {
        if (isEditMode) {
            // Lấy dữ liệu từ TextFields
            String tenMoi = txtTen.getText().trim();
            String giaMoi = txtGia.getText().trim();
            String moTaMoi = txtMoTa.getText().trim();
            String tinhTrangMoi = txtTinhTrang.getText().trim();

            // Kiểm tra dữ liệu không để trống
            if (tenMoi.isEmpty() || giaMoi.isEmpty() || tinhTrangMoi.isEmpty()) {
                showNotification(getStage(lblTen), "Vui lòng điền đầy đủ thông tin!");
                return;
            }

            // Gửi update lên server
            try {
                boolean ok;

                // Nếu có ảnh mới → gửi cả ảnh
                if (newImageData != null) {
                    ok = ServerService.updateItemWithImage(currentId, tenMoi, moTaMoi, giaMoi, newImageData);
                } else {
                    if (newImageData != null) {
                        ok = ServerService.updateItemWithImage(currentId, tenMoi, moTaMoi, giaMoi, newImageData);
                        if (ok) {
                            currentImageBase64 = java.util.Base64.getEncoder().encodeToString(newImageData);
                        }
                    } else {
                        ok = ServerService.updateItem(currentId, tenMoi, moTaMoi, giaMoi, tinhTrangMoi);
                    }
                }

                if (ok) {
                    // Cập nhật UI
                    lblTen.setText(tenMoi);
                    lblGia.setText(giaMoi);
                    lblMoTa.setText(moTaMoi);
                    lblTinhTrang.setText(tinhTrangMoi);

                    disableEditMode();
                    isEditMode = false;
                    newImageData = null;
                    btnChooseImage.setVisible(false);
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