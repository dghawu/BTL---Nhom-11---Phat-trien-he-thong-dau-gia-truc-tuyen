package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * SellerCentreController - SellerAddProduct.fxml
 * Màn hình thêm sản phẩm mới + Upload ảnh.
 */
public class SellerCentreController extends com.example.controller.BaseController {

    @FXML
    private TextField tenField;
    @FXML
    private ComboBox<String> phanLoaiBox;
    @FXML
    private TextField moTaField;
    @FXML
    private TextField giaField;
    @FXML
    private Pane imagePane;

    // Lưu dữ liệu ảnh hiện tại
    private String currentImagePath = null;
    private byte[] imageData = null;

    // ------------------------------------------------------------------ //
    //  Nav
    // ------------------------------------------------------------------ //
    @FXML
    private void handleHome() {
        goHome(getStage(tenField));
    }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(tenField));
    }

    @FXML
    private void handleSellerCentre() { /* đã ở đây */ }

    @FXML
    private void handleSettings() {
        goSettings(getStage(tenField));
    }

    // ------------------------------------------------------------------ //
    //  Sidebar
    // ------------------------------------------------------------------ //
    @FXML
    private void handleThemSanPham() { /* đã ở đây */ }

    @FXML
    private void handleXemSanPham() {
        navigateTo("/fxml/SellerProductList.fxml", getStage(tenField));
    }

    @FXML
    private void handleTaoPhien() {
        navigateTo("/fxml/SellerCreateSession.fxml", getStage(tenField));
    }

    @FXML
    private void handleXemPhien() {
        navigateTo("/fxml/SellerSessionList.fxml", getStage(tenField));
    }

    @FXML
    private void handleThongKe() { /* TODO: Thống kê */ }

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //

    /**
     * Mở FileChooser để chọn ảnh + hiển thị preview
     */
    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh sản phẩm");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fc.showOpenDialog(getStage(imagePane));

        if (file != null) {
            try {
                // Đọc file thành byte array
                imageData = java.nio.file.Files.readAllBytes(file.toPath());
                currentImagePath = file.getAbsolutePath();

                // Hiển thị preview ảnh
                Image img = new Image(file.toURI().toString());
                ImageView iv = new ImageView(img);
                iv.setFitWidth(200);
                iv.setFitHeight(200);
                iv.setPreserveRatio(true);
                imagePane.getChildren().setAll(iv);

            } catch (Exception e) {
                e.printStackTrace();
                showNotification(getStage(imagePane), "Lỗi: Không thể đọc file ảnh!");
                imageData = null;
                currentImagePath = null;
            }
        }
    }

    @FXML
    private void handleEdit() {
        // Cho phép sửa lại các field
        tenField.setEditable(true);
        moTaField.setEditable(true);
        giaField.setEditable(true);
    }

    @FXML
    private void handleSave() {
        String ten = tenField.getText().trim();
        String phanLoai = phanLoaiBox.getValue();
        String moTa = moTaField.getText().trim();
        String giaStr = giaField.getText().trim();

        // Kiểm tra dữ liệu bắt buộc
        if (ten.isEmpty() || phanLoai == null || giaStr.isEmpty()) {
            showNotification(getStage(tenField), "VUI LÒNG ĐIỀN ĐỦ THÔNG TIN!");
            return;
        }

        if (imageData == null) {
            showNotification(getStage(tenField), "VUI LÒNG CHỌN ẢNH SẢN PHẨM!");
            return;
        }

        double gia;
        try {
            gia = Double.parseDouble(giaStr.replace(",", "").replace(".", ""));
        } catch (NumberFormatException e) {
            showNotification(getStage(tenField), "GIÁ KHÔNG HỢP LỆ!");
            return;
        }

        // Gọi ServerService với image data
        boolean ok = ServerService.addItemWithImage(ten, phanLoai, moTa, gia, imageData);

        if (ok) {
            showNotification(getStage(tenField), "THÊM SẢN PHẨM THÀNH CÔNG!");
            clearForm();
        } else {
            showNotification(getStage(tenField), "THÊM SẢN PHẨM THẤT BẠI!");
        }
    }

    private void clearForm() {
        tenField.clear();
        moTaField.clear();
        giaField.clear();
        phanLoaiBox.setValue(null);
        imageData = null;
        currentImagePath = null;
        imagePane.getChildren().clear();
    }
}