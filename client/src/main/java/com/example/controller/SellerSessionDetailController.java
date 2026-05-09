package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * SellerSessionDetailController - SellerSessionDetail.fxml
 */
public class SellerSessionDetailController extends com.example.controller.BaseController {

    @FXML private Label lblSessionId;
    @FXML private Label lblTenSP;
    @FXML private Label lblThoiGianMo;
    @FXML private Label lblThoiGianDong;
    @FXML private Label lblGiaKhoiDiem;
    @FXML private Label lblBuocGia;
    @FXML private Label lblTrangThai;
    @FXML private Pane  imgPane;

    public void initData(String tenSP, String thoiGianMo, String trangThai, String gia) {
        lblTenSP.setText(tenSP);
        lblThoiGianMo.setText(thoiGianMo);
        lblTrangThai.setText(trangThai);
        lblGiaKhoiDiem.setText(gia);
    }

    @FXML private void handleHome()        { goHome(getStage(lblTenSP)); }
    @FXML private void handleAuctions()    { goAuctions(getStage(lblTenSP)); }
    @FXML private void handleSettings()    { goSettings(getStage(lblTenSP)); }
    @FXML private void handleThemSanPham() { navigateTo("/fxml/SellerAddProduct.fxml", getStage(lblTenSP)); }
    @FXML private void handleXemSanPham()  { navigateTo("/fxml/SellerProductList.fxml", getStage(lblTenSP)); }
    @FXML private void handleTaoPhien()    { navigateTo("/fxml/SellerCreateSession.fxml", getStage(lblTenSP)); }
    @FXML private void handleXemPhien()    { navigateTo("/fxml/SellerSessionList.fxml", getStage(lblTenSP)); }

    @FXML private void handleEdit() { /* TODO: bật mode edit */ }
    @FXML private void handleSave() {
        // TODO: ServerService.updateSession(...)
        showNotification(getStage(lblTenSP), "ĐÃ LƯU THAY ĐỔI!");
    }
}
