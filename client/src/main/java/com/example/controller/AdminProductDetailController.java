package com.example.controller;

import com.example.socket.ServerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.json.JSONObject;

/**
 * AdminProductDetailController - AdminProductDetail.fxml
 * Hiển thị chi tiết sản phẩm cho admin xem.
 * Nếu sản phẩm đang PENDING → hiện nút APPROVE / REJECT.
 */
public class AdminProductDetailController extends BaseController {

    @FXML private Label lblTen;
    @FXML private Label lblId;
    @FXML private Label lblLoai;
    @FXML private Label lblGia;
    @FXML private Label lblSeller;
    @FXML private Label lblMoTa;
    @FXML private Label lblTrangThai;
    @FXML private Button btnApprove;
    @FXML private Button btnReject;

    private String currentItemId;

    // ------------------------------------------------------------------ //
    //  Init
    // ------------------------------------------------------------------ //
    public void initData(JSONObject product) {
        currentItemId = product.optString("id");

        lblId.setText(currentItemId);
        lblTen.setText(product.optString("name", "—"));
        lblLoai.setText(product.optString("type", "—"));
        lblGia.setText(String.format("%,.0f đ", product.optDouble("startPrice", 0)));
        lblSeller.setText(product.optString("sellerId", "—"));
        lblMoTa.setText(product.optString("description", "Chưa có mô tả"));
        lblTrangThai.setText(product.optString("status", "—"));

        // Chỉ hiện APPROVE/REJECT khi sản phẩm đang PENDING
        boolean isPending = "PENDING".equalsIgnoreCase(product.optString("status"));
        btnApprove.setVisible(isPending);
        btnApprove.setManaged(isPending);
        btnReject.setVisible(isPending);
        btnReject.setManaged(isPending);
    }

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //
    @FXML
    private void handleApprove() {
        new Thread(() -> {
            boolean ok = ServerService.approveItem(currentItemId);
            Platform.runLater(() -> {
                showNotification(getStage(lblTen),
                        ok ? "Đã duyệt sản phẩm!" : "Duyệt thất bại!");
                if (ok) {
                    lblTrangThai.setText("APPROVED");
                    btnApprove.setVisible(false); btnApprove.setManaged(false);
                    btnReject.setVisible(false);  btnReject.setManaged(false);
                }
            });
        }).start();
    }

    @FXML
    private void handleReject() {
        new Thread(() -> {
            boolean ok = ServerService.rejectItem(currentItemId);
            Platform.runLater(() -> {
                showNotification(getStage(lblTen),
                        ok ? "Đã từ chối sản phẩm!" : "Thao tác thất bại!");
                if (ok) {
                    lblTrangThai.setText("REJECTED");
                    btnApprove.setVisible(false); btnApprove.setManaged(false);
                    btnReject.setVisible(false);  btnReject.setManaged(false);
                }
            });
        }).start();
    }

    @FXML
    private void handleBackToAdmin() {
        navigateTo("/fxml/AdminCentre.fxml", getStage(lblTen));
    }

    // ── Nav ───────────────────────────────────────────────────────────
    @FXML private void handleHome()       { goHome(getStage(lblTen)); }
    @FXML private void handleUserReport() { navigateTo("/fxml/AdminCentre.fxml", getStage(lblTen)); }
    @FXML private void handleSettings()   { goSettings(getStage(lblTen)); }
}
