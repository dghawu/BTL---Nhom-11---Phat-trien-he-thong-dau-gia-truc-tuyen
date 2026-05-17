package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * SellerCreateSessionController - SellerCreateSession.fxml
 * Tạo phiên đấu giá mới. Khi chọn sản phẩm, tự điền:
 * - Descriptions (mô tả sản phẩm)
 * - Ảnh sản phẩm
 */
public class SellerCreateSessionController extends com.example.controller.BaseController {

    @FXML
    private ComboBox<String> sanPhamBox;
    @FXML
    private TextField thoiGianMoField;
    @FXML
    private TextField thoiGianDongField;
    @FXML
    private TextField buocGiaField;
    @FXML
    private TextArea moTaArea;
    @FXML
    private Pane imgPreviewPane;

    private final java.util.Map<String, String> itemNameToId = new java.util.HashMap<>();
    private final java.util.Map<String, JSONObject> itemCache = new java.util.HashMap<>();

    @FXML
    public void initialize() {
    }

    @Override
    protected void onReady() {
        JSONArray items = com.example.socket.ServerService.getMyItems();
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String name = item.getString("name");
            String id = item.getString("id");
            itemNameToId.put(name, id);
            itemCache.put(id, item);  // Cache toàn bộ item data
            sanPhamBox.getItems().add(name);
        }
    }

    /**
     * Khi chọn sản phẩm → tự điền descriptions + ảnh từ data sản phẩm
     */
    @FXML
    private void handleSelectSanPham() {
        String sp = sanPhamBox.getValue();
        if (sp == null) return;

        String itemId = itemNameToId.get(sp);
        JSONObject item = itemCache.get(itemId);

        if (item != null) {
            // Auto fill descriptions từ product description
            String desc = item.optString("description", "");
            moTaArea.setText(desc != null && !desc.isEmpty() ? desc : "");

            // Auto hiển thị ảnh sản phẩm
            String imageData = item.optString("image", "");
            if (imageData != null && !imageData.isEmpty()) {
                try {
                    // Decode base64 → hiển thị
                    byte[] decodedBytes = java.util.Base64.getDecoder().decode(imageData);
                    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(decodedBytes);
                    Image img = new Image(bais);

                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(250);
                    iv.setFitHeight(250);
                    iv.setPreserveRatio(true);
                    imgPreviewPane.getChildren().setAll(iv);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Nếu decode fail → để trống
                    imgPreviewPane.getChildren().clear();
                }
            } else {
                imgPreviewPane.getChildren().clear();
            }
        }
    }

    @FXML
    private void handleEdit() {
        thoiGianMoField.setEditable(true);
        thoiGianDongField.setEditable(true);
        buocGiaField.setEditable(true);
    }

    private java.time.LocalDateTime parseTime(String input) {
        try {
            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return java.time.LocalDateTime.parse(input.trim(), fmt);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void handleSave() {
        String selectedName = sanPhamBox.getValue();
        String startStr = thoiGianMoField.getText().trim();
        String endStr = thoiGianDongField.getText().trim();
        String buocGiaStr = buocGiaField.getText().trim();

        if (selectedName == null || startStr.isEmpty() || endStr.isEmpty() || buocGiaStr.isEmpty()) {
            showNotification(getStage(buocGiaField), "VUI LÒNG ĐIỀN ĐỦ THÔNG TIN!");
            return;
        }

        java.time.LocalDateTime startDT = parseTime(startStr);
        java.time.LocalDateTime endDT = parseTime(endStr);

        if (startDT == null || endDT == null) {
            showNotification(getStage(buocGiaField), "SAI ĐỊNH DẠNG THỜI GIAN!\nVD: 2025-05-15 20:00");
            return;
        }
        if (endDT.isBefore(startDT)) {
            showNotification(getStage(buocGiaField), "THỜI GIAN ĐÓNG PHẢI SAU THỜI GIAN MỞ!");
            return;
        }

        String itemId = itemNameToId.get(selectedName);
        double buocGia;
        try {
            buocGia = Double.parseDouble(buocGiaStr);
        } catch (NumberFormatException e) {
            showNotification(getStage(buocGiaField), "BƯỚC GIÁ KHÔNG HỢP LỆ!");
            return;
        }

        boolean ok = com.example.socket.ServerService.createSession(
                itemId, startDT.toString(), endDT.toString(), buocGia);

        if (ok) {
            showNotification(getStage(buocGiaField), "TẠO PHIÊN THÀNH CÔNG!");
            navigateTo("/fxml/SellerSessionList.fxml", getStage(buocGiaField));
        } else {
            showNotification(getStage(buocGiaField), "TẠO PHIÊN THẤT BẠI!");
        }
    }

    @FXML
    private void handleHome() {
        goHome(getStage(buocGiaField));
    }

    @FXML
    private void handleAuctions() {
        goAuctions(getStage(buocGiaField));
    }

    @FXML
    private void handleSettings() {
        goSettings(getStage(buocGiaField));
    }

    @FXML
    private void handleThemSanPham() {
        navigateTo("/fxml/SellerAddProduct.fxml", getStage(buocGiaField));
    }

    @FXML
    private void handleXemSanPham() {
        navigateTo("/fxml/SellerProductList.fxml", getStage(buocGiaField));
    }

    @FXML
    private void handleTaoPhien() { /* đã ở đây */ }

    @FXML
    private void handleXemPhien() {
        navigateTo("/fxml/SellerSessionList.fxml", getStage(buocGiaField));
    }
}