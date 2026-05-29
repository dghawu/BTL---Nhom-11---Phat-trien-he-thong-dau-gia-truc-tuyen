package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

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
    private Label lblMoTa2;
    @FXML
    private Pane imgPane;

    private AuctionsController parentController;
    private String sessionId;

    public void initData(String sessionId, String ten, String gia, String sellerId, String phanLoai) {
        this.sessionId = sessionId;
        lblTenSP.setText(ten);
        lblGiaKhoiBan.setText(gia);
        lblIdNguoiBan.setText(sellerId);
        lblPhanLoai.setText(phanLoai);
        // TODO: điền đầy đủ từ AuctionSession object
    }

    public void setParentController(AuctionsController parent) {
        this.parentController = parent;
        this.currentRole = parent.currentRole;
        this.currentUsername = parent.currentUsername;
    }

    @FXML
    private void handleDauGia() {
        handleClose();
        if (parentController != null) {
            parentController.handleJoinAuction(sessionId, lblTenSP.getText());
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) lblTenSP.getScene().getWindow()).close();
    }

    @FXML
    private void handleAddToWatchList() {
        // TODO: ServerService.addToWatchList(...)
        showNotification((Stage) lblTenSP.getScene().getWindow(), "ADDED TO WATCHLIST!");
        handleClose();
    }
}
