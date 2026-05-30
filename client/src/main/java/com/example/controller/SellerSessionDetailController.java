package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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
    @FXML
    private VBox attributesContainer;
    @FXML private Label lblWinner;
    @FXML private Label lblGiaHienTai;
    @FXML private javafx.scene.layout.VBox bidHistoryBox;

    // TextFields cho edit mode
    private TextField txtThoiDong;
    private TextField txtBuocGia;
    private TextField txtAttr1;
    private TextField txtAttr2;

    private boolean isEditMode = false;
    private String currentSessionId;
    private String currentCategory;
    private String attr1Value = "";
    private String attr2Value = "";
    private byte[] newImageData = null;

    /**
     * Init data với tất cả thông tin cần thiết (có attributes)
     */
    public void initData(String sessionId, String tenSP, String thoiGianMo,
                         String thoiGianDong, String gia, String buocGia,
                         String trangThai, String imageDataBase64,
                         String category, String attr1, String attr2,
                         String currentWinner, String currentPrice) {
        this.currentSessionId = sessionId;
        this.currentCategory = category;
        this.attr1Value = attr1 != null ? attr1 : "";
        this.attr2Value = attr2 != null ? attr2 : "";

        lblSessionId.setText(sessionId != null && !sessionId.isEmpty() ? sessionId : "---");
        lblTenSP.setText(tenSP);
        lblThoiGianMo.setText(thoiGianMo);
        lblThoiGianDong.setText(thoiGianDong != null && !thoiGianDong.isEmpty() ? thoiGianDong : "---");
        lblGiaKhoiDiem.setText(gia);
        lblBuocGia.setText(buocGia != null && !buocGia.isEmpty() ? buocGia : "---");
        lblTrangThai.setText(trangThai);

        // Hiển thị attributes
        displayAttributes();

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
        if (lblWinner != null) {
            lblWinner.setText("Winner: " + (currentWinner == null || currentWinner.isEmpty()
                    ? "Chưa có" : currentWinner));
        }
        if (lblGiaHienTai != null) {
            lblGiaHienTai.setText("Giá hiện tại: " + currentPrice);
        }
        if (bidHistoryBox != null) {
            new Thread(() -> {
                org.json.JSONArray history = com.example.socket.ServerService.getBidHistory(sessionId);
                if (history == null) return;
                javafx.application.Platform.runLater(() -> {
                    bidHistoryBox.getChildren().clear();
                    for (int i = history.length() - 1; i >= 0; i--) {
                        org.json.JSONObject h = history.getJSONObject(i);
                        String time = h.getString("timestamp").replace("T", " ").substring(0, 16);
                        javafx.scene.control.Label entry = new javafx.scene.control.Label(
                                "▶ " + h.getString("bidderName")
                                        + " bid " + String.format("%,.0f đ", h.getDouble("amount"))
                                        + " lúc " + time);
                        entry.setStyle("-fx-font-size: 13px;");
                        bidHistoryBox.getChildren().add(entry);
                    }
                });
            }).start();
        }
    }

    /**
     * Hiển thị attributes dựa vào category
     */
    private void displayAttributes() {
        if (attributesContainer == null) return;
        attributesContainer.getChildren().clear();

        if (currentCategory == null || currentCategory.isEmpty()) return;

        switch (currentCategory.toUpperCase()) {
            case "FASHION" -> {
                addAttributeRow("Brand", attr1Value);
                addAttributeRow("Size", attr2Value);
            }
            case "ART" -> {
                addAttributeRow("Artist", attr1Value);
                addAttributeRow("Medium", attr2Value);
            }
            case "VEHICLE" -> {
                addAttributeRow("Brand", attr1Value);
                addAttributeRow("Mileage (km)", attr2Value);
            }
            case "ELECTRONICS" -> {
                addAttributeRow("Brand", attr1Value);
                addAttributeRow("Warranty (months)", attr2Value);
            }
        }
    }

    /**
     * Thêm một dòng attribute vào container
     */
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

        // Bật edit mode cho attributes
        enableAttributesEditMode();
    }

    /**
     * Bật edit mode cho attributes
     */
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

    /**
     * Tắt edit mode: hiển thị Labels như cũ
     */
    private void disableEditMode() {
        lblThoiGianDong.setGraphic(null);
        lblThoiGianDong.setText(txtThoiDong != null ? txtThoiDong.getText() : "---");

        lblBuocGia.setGraphic(null);
        lblBuocGia.setText(txtBuocGia != null ? txtBuocGia.getText() : "---");

        // Lưu giá trị attributes nếu có thay đổi
        if (txtAttr1 != null) attr1Value = txtAttr1.getText();
        if (txtAttr2 != null) attr2Value = txtAttr2.getText();

        // Hiển thị lại attributes dưới dạng label
        displayAttributes();
    }

    /**
     * Cho phép chọn ảnh mới trong edit mode
     */
    @FXML
    private void handleChooseNewImage() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Select a new auction image");
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
                showNotification(getStage(lblTenSP), "Error: Unable to read image file!");
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
                showNotification(getStage(lblTenSP), "Please fill in all required information!");
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

                    // Cập nhật attributes nếu có thay đổi
                    if (txtAttr1 != null) attr1Value = txtAttr1.getText();
                    if (txtAttr2 != null) attr2Value = txtAttr2.getText();
                    displayAttributes();

                    disableEditMode();
                    isEditMode = false;
                    newImageData = null;
                    showNotification(getStage(lblTenSP), "Auction updated successfully!");
                } else {
                    showNotification(getStage(lblTenSP), "Update failed!");
                }
            } catch (NumberFormatException e) {
                showNotification(getStage(lblTenSP), "Bid increment must be a number!");
            }
        }
    }
}