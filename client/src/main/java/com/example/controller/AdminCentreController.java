package com.example.controller;

import com.example.socket.ServerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public class AdminCentreController extends BaseController {

    @FXML private Button btnNguoiDung;
    @FXML private Button btnSanPham;
    @FXML private Button btnPhien;
    @FXML private Button btnGiaoDich;

    @FXML private TableView<JSONObject> dataTable;
    @FXML private Button btnBan;
    @FXML private Button btnMakeAdmin;

    private String currentTab = "NGUOIDUNG";

    // ================================================================ //
    //  Init
    // ================================================================ //
    @FXML
    public void initialize() {
        dataTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        Platform.runLater(this::handleShowNguoiDung);
    }

    // ================================================================ //
    //  Sidebar tabs
    // ================================================================ //
    @FXML
    private void handleShowNguoiDung() {
        currentTab = "NGUOIDUNG";
        setActiveTab(btnNguoiDung);
        setVis(btnBan, true);
        setVis(btnMakeAdmin, true);

        dataTable.getColumns().clear();
        dataTable.getColumns().addAll(
                strCol("ID",            90,  o -> truncate(o.optString("id"), 10)),
                strCol("Tên đăng nhập",200,  o -> o.optString("name")),
                strCol("Vai trò",       150, o -> o.optString("role"))
        );
        new Thread(() -> populate(ServerService.getAllUsers())).start();
    }

    @FXML
    private void handleShowSanPham() {
        currentTab = "SANPHAM";
        setActiveTab(btnSanPham);
        setVis(btnBan, false);
        setVis(btnMakeAdmin, false);

        dataTable.getColumns().clear();
        dataTable.getColumns().addAll(
                strCol("ID",           90,  o -> truncate(o.optString("id"), 10)),
                linkCol("Tên sản phẩm",190, o -> o.optString("name"),  this::openProductDetail),
                strCol("Loại",         120, o -> o.optString("type")),
                strCol("Giá khởi điểm",150, o -> String.format("%,.0f đ", o.optDouble("startPrice", 0))),
                strCol("Seller",       110, o -> truncate(o.optString("sellerId"), 10)),
                strCol("Trạng thái",   120, o -> o.optString("status"))
        );
        new Thread(() -> populate(ServerService.getAllItems())).start();
    }

    @FXML
    private void handleShowPhien() {
        currentTab = "PHIEN";
        setActiveTab(btnPhien);
        setVis(btnBan, false);
        setVis(btnMakeAdmin, false);

        dataTable.getColumns().clear();
        dataTable.getColumns().addAll(
                strCol("ID",             90,  o -> truncate(o.optString("id"), 10)),
                linkCol("Sản phẩm",      160, o -> o.optString("itemName"), this::openSessionDetail),
                strCol("Thời gian mở",   170, o -> o.optString("startTime","").replace("T"," ")),
                strCol("Thời gian đóng", 170, o -> o.optString("endTime","").replace("T"," ")),
                strCol("Giá khởi điểm",  150, o -> String.format("%,.0f đ", o.optDouble("startPrice", 0))),
                strCol("Bước giá",       130, o -> String.format("%,.0f đ", o.optDouble("stepPrice", 0))),
                strCol("Trạng thái",     120, o -> o.optString("status"))
        );
        new Thread(() -> populate(ServerService.getAllSessions("ALL"))).start();
    }

    @FXML
    private void handleShowGiaoDich() {
        currentTab = "GIAODICH";
        setActiveTab(btnGiaoDich);
        setVis(btnBan, false);
        setVis(btnMakeAdmin, false);

        dataTable.getColumns().clear();
        dataTable.getColumns().addAll(
                strCol("ID",        90,  o -> truncate(o.optString("id"), 10)),
                strCol("Sản phẩm", 200,  o -> o.optString("itemName")),
                strCol("Người đặt",180,  o -> o.optString("bidderName", o.optString("bidderId",""))),
                strCol("Số tiền",  150,  o -> String.format("%,.0f đ", o.optDouble("amount", 0))),
                strCol("Thời gian",200,  o -> o.optString("timestamp","").replace("T"," "))
        );
        new Thread(() -> populate(ServerService.getAllTransactions())).start();
    }

    @FXML
    private void handleRefresh() {
        switch (currentTab) {
            case "NGUOIDUNG" -> handleShowNguoiDung();
            case "SANPHAM"   -> handleShowSanPham();
            case "PHIEN"     -> handleShowPhien();
            case "GIAODICH"  -> handleShowGiaoDich();
        }
    }

    // ================================================================ //
    //  Action buttons (Users tab only)
    // ================================================================ //
    @FXML
    private void handleBan() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showNotification(getStage(dataTable), "Vui lòng chọn người dùng!"); return; }
        boolean ok = ServerService.banUser(selected.optString("id"));
        showNotification(getStage(dataTable), ok ? "Đã ban: " + selected.optString("name") : "Ban thất bại!");
        if (ok) handleShowNguoiDung();
    }

    @FXML
    private void handleMakeAdmin() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showNotification(getStage(dataTable), "Vui lòng chọn người dùng!"); return; }
        boolean ok = ServerService.makeAdmin(selected.optString("id"));
        showNotification(getStage(dataTable), ok ? "Đã cấp quyền ADMIN!" : "Thao tác thất bại!");
        if (ok) handleShowNguoiDung();
    }

    // ================================================================ //
    //  Navigate to detail pages
    // ================================================================ //
    private void openProductDetail(JSONObject product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminProductDetail.fxml"));
            Parent root = loader.load();
            AdminProductDetailController ctrl = loader.getController();
            ctrl.currentUsername = currentUsername;
            ctrl.currentRole     = currentRole;
            ctrl.currentUserId   = currentUserId;
            ctrl.initData(product);
            Stage stage = getStage(dataTable);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSessionDetail(JSONObject session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminSessionDetail.fxml"));
            Parent root = loader.load();
            AdminSessionDetailController ctrl = loader.getController();
            ctrl.currentUsername = currentUsername;
            ctrl.currentRole     = currentRole;
            ctrl.currentUserId   = currentUserId;
            ctrl.initData(session);
            Stage stage = getStage(dataTable);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================================================ //
    //  Helpers
    // ================================================================ //

    /** Đổ JSONArray vào TableView trên FX thread. */
    private void populate(JSONArray arr) {
        ObservableList<JSONObject> items = FXCollections.observableArrayList();
        if (arr != null)
            for (int i = 0; i < arr.length(); i++) items.add(arr.getJSONObject(i));
        Platform.runLater(() -> dataTable.setItems(items));
    }

    /** Cột text đơn giản. */
    private TableColumn<JSONObject, String> strCol(String header, int width,
                                                   Function<JSONObject, String> fn) {
        TableColumn<JSONObject, String> col = new TableColumn<>(header);
        col.setPrefWidth(width);
        col.setCellValueFactory(c -> new SimpleStringProperty(
                fn.apply(c.getValue()) == null ? "" : fn.apply(c.getValue())));
        return col;
    }

    /** Cột tên — hiển thị dạng Hyperlink, click → callback với JSONObject của row đó. */
    private TableColumn<JSONObject, String> linkCol(String header, int width,
                                                    Function<JSONObject, String> textFn,
                                                    java.util.function.Consumer<JSONObject> onClick) {
        TableColumn<JSONObject, String> col = new TableColumn<>(header);
        col.setPrefWidth(width);
        col.setCellValueFactory(c -> new SimpleStringProperty(textFn.apply(c.getValue())));
        col.setCellFactory(tc -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setStyle("-fx-text-fill: #0044CC;");
                link.setOnAction(e -> {
                    JSONObject row = getTableRow().getItem();
                    if (row != null) onClick.accept(row);
                });
            }
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); }
                else { link.setText(s); setGraphic(link); }
            }
        });
        return col;
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s != null ? s : "";
        return s.substring(0, max) + "…";
    }

    private void setActiveTab(Button active) {
        for (Button b : new Button[]{btnNguoiDung, btnSanPham, btnPhien, btnGiaoDich}) {
            b.getStyleClass().remove("sidebar-item-active");
            if (!b.getStyleClass().contains("sidebar-item")) b.getStyleClass().add("sidebar-item");
        }
        active.getStyleClass().remove("sidebar-item");
        if (!active.getStyleClass().contains("sidebar-item-active"))
            active.getStyleClass().add("sidebar-item-active");
    }

    private void setVis(Button btn, boolean v) {
        btn.setVisible(v);
        btn.setManaged(v);
    }

    // ── Nav ───────────────────────────────────────────────────────────
    @FXML private void handleHome()        { goHome(getStage(dataTable)); }
    @FXML private void handleAdminCentre() { /* đã ở đây */ }
    @FXML private void handleUserReport()  { /* TODO */ }
    @FXML private void handleSettings()    { goSettings(getStage(dataTable)); }
}
