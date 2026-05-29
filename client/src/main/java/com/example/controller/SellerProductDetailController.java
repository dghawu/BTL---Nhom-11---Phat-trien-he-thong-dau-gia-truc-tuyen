package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
    private VBox attributesContainer;
    @FXML
    private Pane imgPane;
    @FXML
    private javafx.scene.control.Button btnChooseImage;

    // TextFields cho edit mode
    private TextField txtTen;
    private TextField txtGia;
    private TextField txtMoTa;
    private String currentImageBase64 = "";

    private boolean isEditMode = false;
    private String currentId;
    private byte[] newImageData = null;

    private String currentCategory;
    private String currentAttr1;
    private String currentAttr2;
    private TextField txtAttr1;
    private TextField txtAttr2;


    public void initData(String id, String ten, String phanLoai, String gia, String moTa,
                         String tinhTrang, String imageBase64, String attr1, String attr2) {
        this.currentId = id;
        this.currentImageBase64 = imageBase64 != null ? imageBase64 : "";


        this.currentCategory = phanLoai;
        this.currentAttr1 = attr1 != null ? attr1 : "";
        this.currentAttr2 = attr2 != null ? attr2 : "";

        lblId.setText(id);
        lblTen.setText(ten);
        lblPhanLoai.setText(phanLoai != null && !phanLoai.isEmpty() ? phanLoai : "N/A");
        lblGia.setText(gia);
        lblMoTa.setText(moTa != null && !moTa.isEmpty() ? moTa : "No description available.");
        lblTinhTrang.setText(tinhTrang);

        displayCurrentImage();
        displayAttributes();  // Bây giờ currentCategory, currentAttr1, currentAttr2 đã có giá trị
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

        // Set style cho TextFields
        txtTen.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        txtGia.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        txtMoTa.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

        // Thay thế Labels bằng TextFields
        lblTen.setGraphic(txtTen);
        lblTen.setText("");

        lblGia.setGraphic(txtGia);
        lblGia.setText("");

        lblMoTa.setGraphic(txtMoTa);
        lblMoTa.setText("");

        enableAttributesEditMode();
    }
    private void enableAttributesEditMode() {
        if (attributesContainer == null || attributesContainer.getChildren().isEmpty()) return;

        for (javafx.scene.Node node : attributesContainer.getChildren()) {
            if (node instanceof HBox hbox && hbox.getChildren().size() >= 2) {
                Label keyLabel = (Label) hbox.getChildren().get(0);
                String key = keyLabel.getText().replace(":", "");
                javafx.scene.Node valueNode = hbox.getChildren().get(1);

                if (valueNode instanceof Label valueLabel) {
                    String currentValue = valueLabel.getText();
                    TextField txtField = new TextField(currentValue.equals("N/A") ? "" : currentValue);
                    txtField.setStyle("-fx-padding: 5px; -fx-font-size: 13px;");
                    hbox.getChildren().set(1, txtField);

                    if (key.equals("Brand") || key.equals("Artist")) {
                        txtAttr1 = txtField;
                    } else if (key.equals("Size") || key.equals("Medium") ||
                            key.equals("Mileage (km)") || key.equals("Warranty (months)")) {
                        txtAttr2 = txtField;
                    }
                }
            }
        }
    }

    private void disableEditMode() {
        // Quay lại view mode: hiển thị Labels như cũ
        lblTen.setGraphic(null);
        lblTen.setText(txtTen != null ? txtTen.getText() : "");

        lblGia.setGraphic(null);
        lblGia.setText(txtGia != null ? txtGia.getText() : "");

        lblMoTa.setGraphic(null);
        lblMoTa.setText(txtMoTa != null ? txtMoTa.getText() : "");

        if (txtAttr1 != null) currentAttr1 = txtAttr1.getText();
        if (txtAttr2 != null) currentAttr2 = txtAttr2.getText();
        displayAttributes();
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
                System.err.println("[SellerProductDetailController] Image decode error: " + e.getMessage());
                imgPane.getChildren().clear();
            }
        } else {
            imgPane.getChildren().clear();
        }
    }
    private void displayAttributes() {
        if (attributesContainer == null) return;
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
        keyLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 120; -fx-font-size: 13px;");
        Label valueLabel = new Label(value != null && !value.isEmpty() ? value : "N/A");
        valueLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        row.getChildren().addAll(keyLabel, valueLabel);
        attributesContainer.getChildren().add(row);
    }


    /**
     * Cho phép chọn ảnh sản phẩm mới trong edit mode
     */
    @FXML
    private void handleChooseNewImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select a new product image");
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
                showNotification(getStage(lblTen), "Error: Unable to read image file!");
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

            // Kiểm tra dữ liệu không để trống
            if (tenMoi.isEmpty() || giaMoi.isEmpty()) {
                showNotification(getStage(lblTen), "Please fill in all required information!");
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
                        ok = ServerService.updateItem(currentId, tenMoi, moTaMoi, giaMoi);
                    }
                }

                if (ok) {
                    // Cập nhật UI
                    lblTen.setText(tenMoi);
                    lblGia.setText(giaMoi);
                    lblMoTa.setText(moTaMoi);

                    if (txtAttr1 != null) currentAttr1 = txtAttr1.getText();
                    if (txtAttr2 != null) currentAttr2 = txtAttr2.getText();
                    displayAttributes();

                    disableEditMode();
                    isEditMode = false;
                    newImageData = null;
                    btnChooseImage.setVisible(false);
                    showNotification(getStage(lblTen), "Product updated successfully!");
                } else {
                    showNotification(getStage(lblTen), "Update failed!");
                }
            } catch (Exception e) {
                showNotification(getStage(lblTen), "Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}