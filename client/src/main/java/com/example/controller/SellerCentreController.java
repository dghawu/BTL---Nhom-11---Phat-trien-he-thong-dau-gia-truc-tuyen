package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    @FXML
    private VBox categoryAttributesContainer;

    // Lưu dữ liệu ảnh hiện tại
    private String currentImagePath = null;
    private byte[] imageData = null;
    private TextField categoryAttr1;
    private TextField categoryAttr2;

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
    public void initialize() {
        // Lắng nghe khi user chọn category
        if (phanLoaiBox != null) {
            phanLoaiBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateCategoryFields(newVal);  // Gọi hàm hiển thị attributes
            });
        }
    }
    private void updateCategoryFields(String category) {
        // Xoá các field cũ
        if (categoryAttributesContainer != null) {
            categoryAttributesContainer.getChildren().clear();
        }
        categoryAttr1 = null;
        categoryAttr2 = null;

        if (category == null || category.isEmpty()) return;

        // Tạo HBox chứa 2 cột
        HBox row = new HBox(20);
        row.setStyle("-fx-padding: 10 0 5 0;");

        // Cột 1
        VBox col1 = new VBox(8);
        col1.setStyle("-fx-pref-width: 250;");
        HBox.setHgrow(col1, Priority.ALWAYS);

        // Cột 2
        VBox col2 = new VBox(8);
        col2.setStyle("-fx-pref-width: 250;");
        HBox.setHgrow(col2, Priority.ALWAYS);

        switch (category.toUpperCase()) {
            case "FASHION" -> {
                Label brandLabel = new Label("Brand");
                brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #555;");
                categoryAttr1 = new TextField();
                categoryAttr1.setPromptText("Nhập thương hiệu (VD: Nike, Adidas)");
                categoryAttr1.setPrefHeight(38);

                Label sizeLabel = new Label("Size");
                sizeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #555;");
                categoryAttr2 = new TextField();
                categoryAttr2.setPromptText("Nhập kích cỡ (VD: S, M, L, XL)");
                categoryAttr2.setPrefHeight(38);

                col1.getChildren().addAll(brandLabel, categoryAttr1);
                col2.getChildren().addAll(sizeLabel, categoryAttr2);
            }
            case "ART" -> {
                Label artistLabel = new Label("Artist");
                artistLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #555;");
                categoryAttr1 = new TextField();
                categoryAttr1.setPromptText("Nhập tên tác giả");
                categoryAttr1.setPrefHeight(38);

                Label mediumLabel = new Label("Medium (Chất liệu)");
                mediumLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #555;");
                categoryAttr2 = new TextField();
                categoryAttr2.setPromptText("VD: Sơn dầu, Màu nước, Điêu khắc");
                categoryAttr2.setPrefHeight(38);

                col1.getChildren().addAll(artistLabel, categoryAttr1);
                col2.getChildren().addAll(mediumLabel, categoryAttr2);
            }
            case "VEHICLE" -> {
                Label brandLabel = new Label("Brand");
                brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #555;");
                categoryAttr1 = new TextField();
                categoryAttr1.setPromptText("Nhập thương hiệu xe (VD: Toyota, Honda)");
                categoryAttr1.setPrefHeight(38);

                Label mileageLabel = new Label("Mileage (km)");
                mileageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #555;");
                categoryAttr2 = new TextField();
                categoryAttr2.setPromptText("Nhập số km đã đi (VD: 50000)");
                categoryAttr2.setPrefHeight(38);

                col1.getChildren().addAll(brandLabel, categoryAttr1);
                col2.getChildren().addAll(mileageLabel, categoryAttr2);
            }
            case "ELECTRONICS" -> {
                Label brandLabel = new Label("Brand");
                brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #555;");
                categoryAttr1 = new TextField();
                categoryAttr1.setPromptText("Nhập thương hiệu (VD: Apple, Samsung)");
                categoryAttr1.setPrefHeight(38);

                Label warrantyLabel = new Label("Warranty (months)");
                warrantyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #555;");
                categoryAttr2 = new TextField();
                categoryAttr2.setPromptText("Nhập số tháng bảo hành (VD: 12)");
                categoryAttr2.setPrefHeight(38);

                col1.getChildren().addAll(brandLabel, categoryAttr1);
                col2.getChildren().addAll(warrantyLabel, categoryAttr2);
            }
            default -> {
                return;
            }
        }

        row.getChildren().addAll(col1, col2);
        categoryAttributesContainer.getChildren().add(row);
    }

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
        String attr1 = categoryAttr1 != null ? categoryAttr1.getText().trim() : "";
        String attr2 = categoryAttr2 != null ? categoryAttr2.getText().trim() : "";

        switch (phanLoai.toUpperCase()) {
            case "FASHION" -> {
                if (attr1.isEmpty()) {
                    showNotification(getStage(tenField), "VUI LÒNG NHẬP BRAND!");
                    return;
                }
                if (attr2.isEmpty()) {
                    showNotification(getStage(tenField), "VUI LÒNG NHẬP SIZE!");
                    return;
                }
            }
            case "ART" -> {
                if (attr1.isEmpty()) {
                    showNotification(getStage(tenField), "VUI LÒNG NHẬP ARTIST!");
                    return;
                }
                if (attr2.isEmpty()) {
                    showNotification(getStage(tenField), "VUI LÒNG NHẬP MEDIUM!");
                    return;
                }
            }
            case "VEHICLE" -> {
                if (attr1.isEmpty()) {
                    showNotification(getStage(tenField), "VUI LÒNG NHẬP BRAND!");
                    return;
                }
                if (attr2.isEmpty()) {
                    showNotification(getStage(tenField), "VUI LÒNG NHẬP MILEAGE!");
                    return;
                }
            }
            case "ELECTRONICS" -> {
                if (attr1.isEmpty()) {
                    showNotification(getStage(tenField), "VUI LÒNG NHẬP BRAND!");
                    return;
                }
                if (attr2.isEmpty()) {
                    showNotification(getStage(tenField), "VUI LÒNG NHẬP WARRANTY MONTHS!");
                    return;
                }
            }
            case "ETC" -> {
                // ETC không yêu cầu attributes
            }
            default -> {
            }
        }

        double gia;
        try {
            gia = Double.parseDouble(giaStr.replace(",", "").replace(".", ""));
        } catch (NumberFormatException e) {
            showNotification(getStage(tenField), "GIÁ KHÔNG HỢP LỆ!");
            return;
        }

        // Gọi ServerService với image data
        boolean ok = ServerService.addItemWithImageAndAttributes(ten, phanLoai, moTa, gia, imageData, attr1, attr2);

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

        if (categoryAttributesContainer != null) {
            categoryAttributesContainer.getChildren().clear();
        }
        categoryAttr1 = null;
        categoryAttr2 = null;
    }
}