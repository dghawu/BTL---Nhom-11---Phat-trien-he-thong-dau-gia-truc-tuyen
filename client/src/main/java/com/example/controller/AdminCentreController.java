package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * AdminCentreController - AdminCentre.fxml
 * Sidebar chuyển đổi giữa 4 bảng: Người dùng / Sản phẩm / Phiên / Giao dịch.
 */
public class AdminCentreController extends com.example.controller.BaseController {

    // Nav
    @FXML private Button btnNguoiDung;
    @FXML private Button btnSanPham;
    @FXML private Button btnPhien;
    @FXML private Button btnGiaoDich;

    // Table
    @FXML private TableView<Object>   dataTable;
    @FXML private TableColumn<Object,String> colId;
    @FXML private TableColumn<Object,String> colTen;
    @FXML private TableColumn<Object,String> colThongTin;
    @FXML private TableColumn<Object,String> colExtra;
    @FXML private TableColumn<Object,String> colTrangThai;

    // Buttons
    @FXML private Button btnBan;
    @FXML private Button btnMakeAdmin;
    @FXML private Button btnEdit;
    @FXML private Button btnSave;

    private String currentTab = "NGUOIDUNG";

    @FXML
    public void initialize() {
        handleShowNguoiDung(); // mặc định hiện tab Người dùng
    }

    // ------------------------------------------------------------------ //
    //  Nav bar
    // ------------------------------------------------------------------ //
    @FXML private void handleHome()         { goHome(getStage(dataTable)); }
    @FXML private void handleAdminCentre()  { /* đã ở đây */ }
    @FXML private void handleUserReport()   { /* chưa làm */}
    @FXML private void handleSettings()     { goSettings(getStage(dataTable)); }

    // ------------------------------------------------------------------ //
    //  Sidebar tabs
    // ------------------------------------------------------------------ //
    @FXML
    private void handleShowNguoiDung() {
        currentTab = "NGUOIDUNG";
        setActiveTab(btnNguoiDung);
        // Buttons cho Người dùng: BAN + ADMIN
        setVisible(btnBan, true);
        setVisible(btnMakeAdmin, true);
        setVisible(btnEdit, false);
        colTrangThai.setVisible(false);
        loadNguoiDung();
    }

    @FXML
    private void handleShowSanPham() {
        currentTab = "SANPHAM";
        setActiveTab(btnSanPham);
        setVisible(btnBan, false);
        setVisible(btnMakeAdmin, false);
        setVisible(btnEdit, true);
        colTrangThai.setVisible(true);
        loadSanPham();
    }

    @FXML
    private void handleShowPhien() {
        currentTab = "PHIEN";
        setActiveTab(btnPhien);
        setVisible(btnBan, false);
        setVisible(btnMakeAdmin, false);
        setVisible(btnEdit, true);
        colTrangThai.setVisible(true);
        loadPhien();
    }

    @FXML
    private void handleShowGiaoDich() {
        currentTab = "GIAODICH";
        setActiveTab(btnGiaoDich);
        setVisible(btnBan, false);
        setVisible(btnMakeAdmin, false);
        setVisible(btnEdit, false);
        colTrangThai.setVisible(false);
        loadGiaoDich();
    }

    // ------------------------------------------------------------------ //
    //  Action buttons
    // ------------------------------------------------------------------ //
    @FXML
    private void handleBan() {
        Object selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // TODO: gọi server ban user
        // ServerService.banUser(getUserId(selected));
        showNotification(getStage(dataTable), "ĐÃ BAN NGƯỜI DÙNG!");
        loadNguoiDung();
    }

    @FXML
    private void handleMakeAdmin() {
        Object selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // TODO: ServerService.makeAdmin(getUserId(selected));
        showNotification(getStage(dataTable), "ĐÃ CẤP QUYỀN ADMIN!");
        loadNguoiDung();
    }

    @FXML
    private void handleEdit() {
        Object selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // Mở trạng thái edit trong row (enable inline editing)
        dataTable.setEditable(true);
    }

    @FXML
    private void handleSave() {
        // TODO: gửi thay đổi lên server
        dataTable.setEditable(false);
        showNotification(getStage(dataTable), "ĐÃ LƯU THAY ĐỔI!");
    }

    // ------------------------------------------------------------------ //
    //  Data loaders - TODO: thay bằng call thật từ server
    // ------------------------------------------------------------------ //
    private void loadNguoiDung() {
        dataTable.getItems().clear();
        // TODO: dataTable.setItems(FXCollections.observableList(ServerService.getAllUsers()));
    }

    private void loadSanPham() {
        dataTable.getItems().clear();
        // TODO: dataTable.setItems(FXCollections.observableList(ServerService.getAllItems()));
    }

    private void loadPhien() {
        dataTable.getItems().clear();
        // TODO: dataTable.setItems(FXCollections.observableList(ServerService.getAllSessions()));
    }

    private void loadGiaoDich() {
        dataTable.getItems().clear();
        // TODO: dataTable.setItems(FXCollections.observableList(ServerService.getAllTransactions()));
    }

    // ------------------------------------------------------------------ //
    //  UI helpers
    // ------------------------------------------------------------------ //
    private void setActiveTab(Button active) {
        for (Button b : new Button[]{btnNguoiDung, btnSanPham, btnPhien, btnGiaoDich}) {
            b.getStyleClass().remove("sidebar-item-active");
            if (!b.getStyleClass().contains("sidebar-item"))
                b.getStyleClass().add("sidebar-item");
        }
        active.getStyleClass().remove("sidebar-item");
        if (!active.getStyleClass().contains("sidebar-item-active"))
            active.getStyleClass().add("sidebar-item-active");
    }

    private void setVisible(Button btn, boolean v) {
        btn.setVisible(v);
        btn.setManaged(v);
    }
}
