package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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

    @FXML private DatePicker ngayMoPicker;
    @FXML private DatePicker ngayDongPicker;
    @FXML private ComboBox<String> gioMoBox;
    @FXML private ComboBox<String> gioDongBox;
    @FXML private ComboBox<String> phutMoBox;
    @FXML private ComboBox<String> phutDongBox;

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

        for (int i = 0; i < 24; i++) {

            String gio = String.format("%02d", i);

            gioMoBox.getItems().add(gio);
            gioDongBox.getItems().add(gio);
        }

        for (int i = 0; i < 60; i++) {

            String phut = String.format("%02d", i);

            phutMoBox.getItems().add(phut);
            phutDongBox.getItems().add(phut);
        }
    }

    @Override
    protected void onReady() {
        new Thread(() -> {
            JSONArray items = com.example.socket.ServerService.getMyItems();
            if (items == null) return;
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                if (!"APPROVED".equals(item.optString("status"))) continue;

                String name = item.getString("name");
                String id   = item.getString("id");
                itemNameToId.put(name, id);
                itemCache.put(id, item);

                javafx.application.Platform.runLater(() ->
                        sanPhamBox.getItems().add(name));
            }
        }).start();
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
            // Lấy dữ liệu từ item
            String desc = item.optString("description", "");
            double startPrice = item.optDouble("startPrice", 0);
            String type = item.optString("type", "");
            String attr1 = item.optString("attr1", "");
            String attr2 = item.optString("attr2", "");


            StringBuilder content = new StringBuilder();

            // 1. Starting price
            content.append("* STARTING PRICE: ").append(String.format("%,.0f đ", startPrice)).append("\n\n");

            // 2. Description
            content.append("* DESCRIPTION:\n").append(desc != null && !desc.isEmpty() ? desc : "Chưa có mô tả").append("\n\n");

            // 3. Attributes (nếu có)
            if (!attr1.isEmpty() || !attr2.isEmpty()) {
                content.append("* ATTRIBUTES:\n");
                switch (type.toUpperCase()) {
                    case "FASHION" -> {
                        content.append("   Brand: ").append(attr1.isEmpty() ? "N/A" : attr1).append("\n");
                        content.append("   Size: ").append(attr2.isEmpty() ? "N/A" : attr2);
                    }
                    case "ART" -> {
                        content.append("   Artist: ").append(attr1.isEmpty() ? "N/A" : attr1).append("\n");
                        content.append("   Medium: ").append(attr2.isEmpty() ? "N/A" : attr2);
                    }
                    case "VEHICLE" -> {
                        content.append("   Brand: ").append(attr1.isEmpty() ? "N/A" : attr1).append("\n");
                        content.append("   Mileage: ").append(attr2.isEmpty() ? "N/A" : attr2).append(" km");
                    }
                    case "ELECTRONICS" -> {
                        content.append("   Brand: ").append(attr1.isEmpty() ? "N/A" : attr1).append("\n");
                        content.append("   Warranty: ").append(attr2.isEmpty() ? "N/A" : attr2).append(" tháng");
                    }
                    default -> {
                        content.append("   Không có thuộc tính đặc biệt");
                    }
                }
            }

            // Set nội dung vào TextArea
            moTaArea.setText(content.toString());

            // Bước giá field (vẫn giữ promptText)
            buocGiaField.setPromptText("Bid increment");

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

        ngayMoPicker.setDisable(false);
        ngayDongPicker.setDisable(false);

        gioMoBox.setDisable(false);
        gioDongBox.setDisable(false);

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

        String gioMo = gioMoBox.getValue();
        String phutMo = phutMoBox.getValue();

        String gioDong = gioDongBox.getValue();
        String phutDong = phutDongBox.getValue();

        String buocGiaStr = buocGiaField.getText().trim();

        // Ghép ngày + giờ
        String startStr =
                ngayMoPicker.getValue() + " " + gioMo + ":" + phutMo;

        String endStr =
                ngayDongPicker.getValue() + " " + gioDong + ":" + phutDong;

        if (selectedName == null
                || ngayMoPicker.getValue() == null
                || gioMo == null
                || ngayDongPicker.getValue() == null
                || gioDong == null
                || buocGiaStr.isEmpty()) {

            showNotification(getStage(buocGiaField),
                    "PLEASE FILL IN ALL REQUIRED INFORMATION!");

            return;
        }

        java.time.LocalDateTime startDT = parseTime(startStr);
        java.time.LocalDateTime endDT   = parseTime(endStr);

        if (startDT == null || endDT == null) {

            showNotification(getStage(buocGiaField),
                    "INVALID TIME FORMAT!");

            return;
        }

        if (endDT.isBefore(startDT)) {

            showNotification(getStage(buocGiaField),
                    "CLOSING TIME MUST BE AFTER OPENING TIME!");

            return;
        }

        String itemId = itemNameToId.get(selectedName);

        double buocGia;

        try {
            buocGia = Double.parseDouble(buocGiaStr);
        }
        catch (NumberFormatException e) {

            showNotification(getStage(buocGiaField),
                    "INVALID BID INCREMENT!");

            return;
        }

        double finalBuocGia = buocGia;

        new Thread(() -> {

            boolean ok = com.example.socket.ServerService.createSession(
                    itemId,
                    startDT.toString(),
                    endDT.toString(),
                    finalBuocGia
            );

            javafx.application.Platform.runLater(() -> {

                if (ok) {

                    showNotification(getStage(buocGiaField),
                            "“AUCTION CREATED SUCCESSFULLY!");

                    navigateTo("/fxml/SellerSessionList.fxml",
                            getStage(buocGiaField));

                } else {

                    showNotification(getStage(buocGiaField),
                            "AUCTION CREATION FAILED!\n" +
                                    "(Product not approved or server error)");
                }
            });

        }).start();
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