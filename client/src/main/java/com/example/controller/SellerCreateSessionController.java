package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

/**
 * SellerCreateSessionController - SellerCreateSession.fxml
 * Tạo phiên đấu giá mới. Khi chọn sản phẩm, tự điền ảnh và mô tả.
 */
public class SellerCreateSessionController extends com.example.controller.BaseController {

    @FXML private ComboBox<String> sanPhamBox;
    @FXML private TextField  thoiGianMoField;
    @FXML private TextField  thoiGianDongField;
    @FXML private TextField  buocGiaField;
    @FXML private TextArea   moTaArea;
    @FXML private Pane       imgPreviewPane;

    @FXML
    public void initialize() {
        // TODO: load sản phẩm của seller từ server
        // sanPhamBox.setItems(FXCollections.observableList(ServerService.getMyProductNames()));
        sanPhamBox.getItems().addAll("Sản phẩm A", "Sản phẩm B", "Sản phẩm C"); // mock
    }

    /** Khi chọn sản phẩm → tự điền mô tả + ảnh */
    @FXML
    private void handleSelectSanPham() {
        String sp = sanPhamBox.getValue();
        if (sp == null) return;
        // TODO: lấy thông tin đầy đủ từ server
        // Item item = ServerService.getItemByName(sp);
        // moTaArea.setText("Giá khởi điểm: " + item.getStartPrice() + "\nMô tả: " + item.getDescription());
        moTaArea.setText("Giá khởi điểm: 1.000.000đ\nMô tả: Đây là mô tả sản phẩm " + sp);
    }

    @FXML
    private void handleEdit() {
        thoiGianMoField.setEditable(true);
        thoiGianDongField.setEditable(true);
        buocGiaField.setEditable(true);
    }

    @FXML
    private void handleSave() {
        if (sanPhamBox.getValue() == null || thoiGianMoField.getText().isEmpty()
                || thoiGianDongField.getText().isEmpty() || buocGiaField.getText().isEmpty()) {
            showNotification(getStage(buocGiaField), "VUI LÒNG ĐIỀN ĐỦ THÔNG TIN!");
            return;
        }
        // TODO: ServerService.createSession(...)
        showNotification(getStage(buocGiaField), "TẠO PHIÊN THÀNH CÔNG!");
        navigateTo("/fxml/SellerSessionList.fxml", getStage(buocGiaField));
    }

    @FXML private void handleHome()        { goHome(getStage(buocGiaField)); }
    @FXML private void handleAuctions()    { goAuctions(getStage(buocGiaField)); }
    @FXML private void handleSettings()    { goSettings(getStage(buocGiaField)); }
    @FXML private void handleThemSanPham() { navigateTo("/fxml/SellerAddProduct.fxml", getStage(buocGiaField)); }
    @FXML private void handleXemSanPham()  { navigateTo("/fxml/SellerProductList.fxml", getStage(buocGiaField)); }
    @FXML private void handleTaoPhien()    { /* đã ở đây */ }
    @FXML private void handleXemPhien()    { navigateTo("/fxml/SellerSessionList.fxml", getStage(buocGiaField)); }
}
