package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * SellerProductDetailController - SellerProductDetail.fxml
 * Nhận data từ SellerProductListController.openDetail()
 */
public class SellerProductDetailController extends com.example.controller.BaseController {

    @FXML private Label lblTen;
    @FXML private Label lblId;
    @FXML private Label lblPhanLoai;
    @FXML private Label lblNgayMoBan;
    @FXML private Label lblGia;
    @FXML private Label lblMoTa;
    @FXML private Label lblTinhTrang;
    @FXML private Pane  imgPane;

    public void initData(String ten, String id, String gia, String tinhTrang) {
        lblTen.setText(ten);
        lblId.setText(id);
        lblGia.setText(gia);
        lblTinhTrang.setText(tinhTrang);
        // TODO: điền thêm từ server nếu cần
    }

    @FXML private void handleHome()        { goHome(getStage(lblTen)); }
    @FXML private void handleAuctions()    { goAuctions(getStage(lblTen)); }
    @FXML private void handleSettings()    { goSettings(getStage(lblTen)); }
    @FXML private void handleThemSanPham() { navigateTo("/fxml/SellerAddProduct.fxml", getStage(lblTen)); }
    @FXML private void handleXemSanPham()  { navigateTo("/fxml/SellerProductList.fxml", getStage(lblTen)); }
    @FXML private void handleTaoPhien()    { navigateTo("/fxml/SellerCreateSession.fxml", getStage(lblTen)); }
    @FXML private void handleXemPhien()    { navigateTo("/fxml/SellerSessionList.fxml", getStage(lblTen)); }

    @FXML
    private void handleEdit() {
        // TODO: chuyển sang mode edit (enable TextField thay Label)
    }

    @FXML
    private void handleSave() {
        // TODO: gửi cập nhật sản phẩm lên server
        showNotification(getStage(lblTen), "ĐÃ LƯU THAY ĐỔI!");
    }
}
