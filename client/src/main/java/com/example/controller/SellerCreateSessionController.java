package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * SellerCreateSessionController - SellerCreateSession.fxml
 * Tạo phiên đấu giá mới. Khi chọn sản phẩm, tự điền ảnh và mô tả.
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

    @FXML
    public void initialize() {
    }

    @Override
    protected void onReady() {
        org.json.JSONArray items = com.example.socket.ServerService.getMyItems();
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            org.json.JSONObject item = items.getJSONObject(i);
            String name = item.getString("name");
            String id = item.getString("id");
            itemNameToId.put(name, id);
            sanPhamBox.getItems().add(name);
        }
    }

    /**
     * Khi chọn sản phẩm → tự điền mô tả + ảnh
     */
    @FXML
    private void handleSelectSanPham() {
        String sp = sanPhamBox.getValue();
        if (sp == null) return;
        // Điền mô tả nếu muốn — có thể để trống
        moTaArea.setText("Sản phẩm: " + sp);
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
