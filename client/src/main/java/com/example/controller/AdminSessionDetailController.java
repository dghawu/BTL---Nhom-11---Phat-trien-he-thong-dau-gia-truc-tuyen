package com.example.controller;

import com.example.socket.ServerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.json.JSONObject;

/**
 * AdminSessionDetailController - AdminSessionDetail.fxml
 * Hiển thị chi tiết phiên đấu giá cho admin xem.
 * Nếu phiên đang PENDING → hiện nút APPROVE / REJECT.
 */
public class AdminSessionDetailController extends BaseController {

    @FXML private Label lblSessionId;
    @FXML private Label lblTenSP;
    @FXML private Label lblCategory;
    @FXML private Label lblSeller;
    @FXML private Label lblThoiGianMo;
    @FXML private Label lblThoiGianDong;
    @FXML private Label lblGiaKhoiDiem;
    @FXML private Label lblBuocGia;
    @FXML private Label lblGiaHienTai;
    @FXML private Label lblNguoiThang;
    @FXML private Label lblTrangThai;
    @FXML private Button btnApprove;
    @FXML private Button btnReject;

    private String currentSessionId;

    // ------------------------------------------------------------------ //
    //  Init
    // ------------------------------------------------------------------ //
    public void initData(JSONObject session) {
        currentSessionId = session.optString("id");

        lblSessionId.setText(currentSessionId);
        lblTenSP.setText(session.optString("itemName", "—"));
        lblCategory.setText(session.optString("category", "—"));
        lblSeller.setText(session.optString("sellerId", "—"));
        lblThoiGianMo.setText(session.optString("startTime", "—").replace("T", " "));
        lblThoiGianDong.setText(session.optString("endTime", "—").replace("T", " "));
        lblGiaKhoiDiem.setText(String.format("%,.0f đ", session.optDouble("startPrice", 0)));
        lblBuocGia.setText(String.format("%,.0f đ", session.optDouble("stepPrice", 0)));
        lblGiaHienTai.setText(String.format("%,.0f đ", session.optDouble("currentPrice", 0)));

        String winner = session.optString("currentWinner", "");
        lblNguoiThang.setText(winner.isEmpty() ? "Chưa có" : winner);
        lblTrangThai.setText(session.optString("status", "—"));

        // Chỉ hiện APPROVE/REJECT khi phiên đang PENDING
        boolean isPending = "PENDING".equalsIgnoreCase(session.optString("status"));
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
            boolean ok = ServerService.approveSession(currentSessionId);
            Platform.runLater(() -> {
                showNotification(getStage(lblTenSP),
                        ok ? "Đã duyệt phiên đấu giá!" : "Duyệt thất bại!");
                if (ok) {
                    lblTrangThai.setText("RUNNING");
                    btnApprove.setVisible(false); btnApprove.setManaged(false);
                    btnReject.setVisible(false);  btnReject.setManaged(false);
                }
            });
        }).start();
    }

    @FXML
    private void handleReject() {
        new Thread(() -> {
            boolean ok = ServerService.rejectSession(currentSessionId);
            Platform.runLater(() -> {
                showNotification(getStage(lblTenSP),
                        ok ? "Đã từ chối phiên!" : "Thao tác thất bại!");
                if (ok) {
                    lblTrangThai.setText("CANCELED");
                    btnApprove.setVisible(false); btnApprove.setManaged(false);
                    btnReject.setVisible(false);  btnReject.setManaged(false);
                }
            });
        }).start();
    }

    @FXML
    private void handleBackToAdmin() {
        navigateTo("/fxml/AdminCentre.fxml", getStage(lblTenSP));
    }

    // ── Nav ───────────────────────────────────────────────────────────
    @FXML private void handleHome()       { goHome(getStage(lblTenSP)); }
    @FXML private void handleUserReport() { navigateTo("/fxml/AdminCentre.fxml", getStage(lblTenSP)); }
    @FXML private void handleSettings()   { goSettings(getStage(lblTenSP)); }
}
