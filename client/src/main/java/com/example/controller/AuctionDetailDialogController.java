package com.example.controller;

import com.example.socket.ServerService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

/**
 * AuctionDetailDialogController - AuctionDetailDialog.fxml
 * Popup hiển thị chi tiết phiên trước khi tham gia.
 */
public class AuctionDetailDialogController extends com.example.controller.BaseController {

    @FXML
    private Label lblTenSP;
    @FXML
    private Label lblIdSP;
    @FXML
    private Label lblMoTa;
    @FXML
    private Label lblIdPhien;
    @FXML
    private Label lblThoiGianMo;
    @FXML
    private Label lblThoiGianDong;
    @FXML
    private Label lblGiaKhoiBan;
    @FXML
    private Label lblBuocGia;
    @FXML
    private Label lblIdNguoiBan;
    @FXML
    private Label lblPhanLoai;
    @FXML
    private VBox attributesContainer;  // ← THÊM DÒNG NÀY
    @FXML
    private Pane imgPane;

    private AuctionsController parentController;
    private String sessionId;
    private ImageView currentImageView;

    /**
     * Khởi tạo dữ liệu từ JSONObject
     */
    public void initData(JSONObject sessionData) {
        this.sessionId = sessionData.getString("id");

        // Lấy thông tin từ JSON
        String tenSP = sessionData.optString("itemName", "Không có tên");
        String idSP = sessionData.optString("itemId", "Không có ID");
        String moTa = sessionData.optString("description", "Không có mô tả");
        String thoiGianMo = formatDateTime(sessionData.optString("startTime", ""));
        String thoiGianDong = formatDateTime(sessionData.optString("endTime", ""));
        String giaKhoiDiem = formatPrice(sessionData.optDouble("startPrice", 0));
        String buocGia = formatPrice(sessionData.optDouble("stepPrice", 0));
        String sellerId = sessionData.optString("sellerId", "Không có");
        String phanLoai = sessionData.optString("category", "Không có");

        // Set các label
        lblTenSP.setText(tenSP);
        lblIdSP.setText(idSP);
        lblMoTa.setText(moTa);
        lblIdPhien.setText(sessionId);
        lblThoiGianMo.setText(thoiGianMo);
        lblThoiGianDong.setText(thoiGianDong);
        lblGiaKhoiBan.setText(giaKhoiDiem);
        lblBuocGia.setText(buocGia);
        lblIdNguoiBan.setText(sellerId);
        lblPhanLoai.setText(phanLoai);

        // ===== HIỂN THỊ ATTRIBUTES =====
        displayAttributes(sessionData, phanLoai);

        // Hiển thị ảnh
        displayImage(sessionData);
    }

    /**
     * Hiển thị attributes theo loại sản phẩm
     */
    private void displayAttributes(JSONObject sessionData, String category) {
        if (attributesContainer == null) {
            System.err.println("[DEBUG] attributesContainer is NULL!");
            return;
        }

        attributesContainer.getChildren().clear();

        String attr1 = sessionData.optString("attr1", "");
        String attr2 = sessionData.optString("attr2", "");

        System.out.println("[DEBUG] category: " + category);
        System.out.println("[DEBUG] attr1: '" + attr1 + "'");
        System.out.println("[DEBUG] attr2: '" + attr2 + "'");

        if (!attr1.isEmpty()) {
            String label1 = getAttributeLabel1(category);
            Label lbl1 = new Label(label1 + ": " + attr1);
            lbl1.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-padding: 2 0;");
            attributesContainer.getChildren().add(lbl1);
        }

        if (!attr2.isEmpty()) {
            String label2 = getAttributeLabel2(category);
            Label lbl2 = new Label(label2 + ": " + attr2);
            lbl2.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-padding: 2 0;");
            attributesContainer.getChildren().add(lbl2);
        }

        if (attributesContainer.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Không có thông tin chi tiết");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-padding: 2 0;");
            attributesContainer.getChildren().add(emptyLabel);
        }
    }

    private String getAttributeLabel1(String category) {
        if (category == null) return "Thuộc tính";
        return switch (category.toUpperCase()) {
            case "FASHION" -> "Thương hiệu";
            case "ART" -> "Họa sĩ";
            case "VEHICLE" -> "Hãng xe";
            case "ELECTRONICS" -> "Thương hiệu";
            default -> "Thuộc tính 1";
        };
    }

    private String getAttributeLabel2(String category) {
        if (category == null) return "Thuộc tính";
        return switch (category.toUpperCase()) {
            case "FASHION" -> "Size";
            case "ART" -> "Chất liệu";
            case "VEHICLE" -> "Số km đã đi";
            case "ELECTRONICS" -> "Bảo hành (tháng)";
            default -> "Thuộc tính 2";
        };
    }

    private String formatDateTime(String raw) {
        if (raw == null || raw.isEmpty()) return "Chưa có";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(raw);
            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dt.format(fmt);
        } catch (Exception e) {
            return raw;
        }
    }

    private String formatPrice(double price) {
        return String.format("%,.0f₫", price);
    }

    private void displayImage(JSONObject sessionData) {
        if (currentImageView == null) {
            currentImageView = new ImageView();
            currentImageView.setFitWidth(260);
            currentImageView.setFitHeight(220);
            currentImageView.setPreserveRatio(true);
            imgPane.getChildren().clear();
            imgPane.getChildren().add(currentImageView);
        }

        String imageBase64 = sessionData.optString("imageBase64", "");

        if (!imageBase64.isEmpty()) {
            try {
                byte[] bytes = java.util.Base64.getDecoder().decode(imageBase64);
                Image image = new Image(new java.io.ByteArrayInputStream(bytes));
                currentImageView.setImage(image);
                imgPane.setStyle("-fx-background-color: transparent;");
                return;
            } catch (Exception e) {
                System.err.println("Lỗi decode ảnh: " + e.getMessage());
            }
        }

        String itemId = sessionData.optString("itemId", "");
        if (!itemId.isEmpty() && parentController != null) {
            String imageFromParent = parentController.getItemImage(itemId);
            if (imageFromParent != null && !imageFromParent.isEmpty()) {
                try {
                    byte[] bytes = java.util.Base64.getDecoder().decode(imageFromParent);
                    Image image = new Image(new java.io.ByteArrayInputStream(bytes));
                    currentImageView.setImage(image);
                    imgPane.setStyle("-fx-background-color: transparent;");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        setPlaceholderImage();
    }

    private void setPlaceholderImage() {
        currentImageView.setImage(null);
        imgPane.setStyle("-fx-background-color: #F5EEC8; -fx-border-color: #CCCCCC; -fx-border-width: 1px;");

        if (imgPane.getChildren().size() > 1) {
            imgPane.getChildren().remove(1);
        }

        Label placeholder = new Label("Chưa có ảnh");
        placeholder.setStyle("-fx-text-fill: #888888;");
        placeholder.setPrefWidth(260);
        placeholder.setPrefHeight(220);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        imgPane.getChildren().add(placeholder);
    }

    public void setParentController(AuctionsController parent) {
        this.parentController = parent;
        if (parent != null) {
            this.currentRole = parent.currentRole;
            this.currentUsername = parent.currentUsername;
            this.currentUserId = parent.currentUserId;
        }
    }

    @FXML
    private void handleDauGia() {
        ((Stage) lblTenSP.getScene().getWindow()).close();
        if (parentController != null) {
            parentController.handleJoinAuction(sessionId, lblTenSP.getText());
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) lblTenSP.getScene().getWindow()).close();
    }
}