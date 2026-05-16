package com.example.controller;

import com.example.socket.ServerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AdminCentreController - AdminCentre.fxml
 * Sidebar chuyển đổi giữa 4 bảng: Người dùng / Sản phẩm / Phiên / Giao dịch.
 */
@SuppressWarnings("unchecked")
public class AdminCentreController extends BaseController {

    // ── Nav sidebar ───────────────────────────────────────────────────
    @FXML private Button btnNguoiDung;
    @FXML private Button btnSanPham;
    @FXML private Button btnPhien;
    @FXML private Button btnGiaoDich;

    // ── Tiêu đề tab ───────────────────────────────────────────────────
    @FXML private Label lblTabTitle;

    // ── Table ─────────────────────────────────────────────────────────
    // JavaFX inject TableView<?> rồi cast — an toàn vì ta kiểm soát data.
    @FXML private TableView<JSONObject>          dataTable;
    @FXML private TableColumn<JSONObject,String> colId;
    @FXML private TableColumn<JSONObject,String> colTen;
    @FXML private TableColumn<JSONObject,String> colThongTin;
    @FXML private TableColumn<JSONObject,String> colExtra;
    @FXML private TableColumn<JSONObject,String> colTrangThai;

    // ── Action buttons ────────────────────────────────────────────────
    @FXML private Button btnBan;
    @FXML private Button btnMakeAdmin;
    @FXML private Button btnEdit;   // tái dụng: APPROVE
    @FXML private Button btnSave;   // tái dụng: REJECT

    private String currentTab = "NGUOIDUNG";

    // ================================================================ //
    //  Init
    // ================================================================ //

    @FXML
    public void initialize() {
        dataTable.setPlaceholder(new Label("Không có dữ liệu."));
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
        //lblTabTitle.setText("Users");

        // Cột
        colId.setText("ID");
        colTen.setText("Tên đăng nhập");
        colThongTin.setText("Vai trò");
        colExtra.setText("");
        colTrangThai.setVisible(false);

        // Buttons: BAN + MAKE ADMIN, ẩn APPROVE/REJECT
        showButtons(true, true, false, false);

        loadNguoiDung();
    }

    @FXML
    private void handleShowSanPham() {
        currentTab = "SANPHAM";
        setActiveTab(btnSanPham);
        //lblTabTitle.setText("Products");

        colId.setText("ID");
        colTen.setText("Tên sản phẩm");
        colThongTin.setText("Loại  |  Giá khởi điểm");
        colExtra.setText("Seller");
        colTrangThai.setVisible(true);
        colTrangThai.setText("Trạng thái");

        // Buttons: APPROVE + REJECT, ẩn BAN/ADMIN
        showButtons(false, false, true, true);

        loadSanPham();
    }

    @FXML
    private void handleShowPhien() {
        currentTab = "PHIEN";
        setActiveTab(btnPhien);
        // ... setup cột như trên ...
        showButtons(false, false, false, false); // ẩn hết nút dưới

        // Click vào row → mở detail
        dataTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {          // double-click
                JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
                if (selected != null) showSessionDetail(selected);
            }
        });

        loadPhien();
    }

    private void showSessionDetail(JSONObject session) {
        // Tạo dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết phiên đấu giá");
        dialog.initOwner(getStage(dataTable));

        // Nội dung
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20));

        String[][] rows = {
                {"Sản phẩm",      session.optString("itemName")},
                {"ID phiên",      session.optString("id")},
                {"Trạng thái",    session.optString("status")},
                {"Giá khởi điểm", String.format("%,.0f đ", session.optDouble("startPrice",0))},
                {"Bước giá",      String.format("%,.0f đ", session.optDouble("stepPrice",0))},
                {"Giá hiện tại",  String.format("%,.0f đ", session.optDouble("currentPrice",0))},
                {"Thời gian mở",  session.optString("startTime","").replace("T"," ")},
                {"Thời gian đóng",session.optString("endTime","").replace("T"," ")},
                {"Người thắng",   session.optString("currentWinner","—")},
        };

        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0]);
            key.setStyle("-fx-font-weight: bold;");
            Label val = new Label(rows[i][1]);
            grid.add(key, 0, i);
            grid.add(val, 1, i);
        }

        dialog.getDialogPane().setContent(grid);

        // Nút APPROVE / REJECT (chỉ hiện khi PENDING)
        boolean isPending = "PENDING".equals(session.optString("status"));
        if (isPending) {
            ButtonType approveBtn = new ButtonType("✔ APPROVE", ButtonBar.ButtonData.OK_DONE);
            ButtonType rejectBtn  = new ButtonType("✘ REJECT",  ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(approveBtn, rejectBtn);

            dialog.showAndWait().ifPresent(result -> {
                String id = session.optString("id");
                if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    new Thread(() -> {
                        boolean ok = ServerService.approveSession(id);
                        Platform.runLater(() -> {
                            showNotification(getStage(dataTable),
                                    ok ? "Đã duyệt phiên!" : "Duyệt thất bại!");
                            if (ok) loadPhien();
                        });
                    }).start();
                } else {
                    new Thread(() -> {
                        boolean ok = ServerService.rejectSession(id);
                        Platform.runLater(() -> {
                            showNotification(getStage(dataTable),
                                    ok ? "Đã từ chối phiên!" : "Thao tác thất bại!");
                            if (ok) loadPhien();
                        });
                    }).start();
                }
            });
        } else {
            // Không pending → chỉ xem
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        }
    }

    @FXML
    private void handleShowGiaoDich() {
        currentTab = "GIAODICH";
        setActiveTab(btnGiaoDich);
        //lblTabTitle.setText("Transactions");

        colId.setText("ID");
        colTen.setText("Sản phẩm");
        colThongTin.setText("Người đặt");
        colExtra.setText("Số tiền");
        colTrangThai.setVisible(true);
        colTrangThai.setText("Thời gian");

        // Transactions: chỉ xem, không có action
        showButtons(false, false, false, false);

        loadGiaoDich();
    }

    // ── Refresh button ────────────────────────────────────────────────

    @FXML
    private void handleRefresh() {
        switch (currentTab) {
            case "NGUOIDUNG" -> loadNguoiDung();
            case "SANPHAM"   -> loadSanPham();
            case "PHIEN"     -> loadPhien();
            case "GIAODICH"  -> loadGiaoDich();
        }
    }

    // ================================================================ //
    //  Action buttons
    // ================================================================ //

    @FXML
    private void handleBan() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(getStage(dataTable), "Vui lòng chọn người dùng!");
            return;
        }
        String name = selected.optString("name", "?");
        boolean ok = ServerService.banUser(selected.optString("id"));
        showNotification(getStage(dataTable),
                ok ? "Đã ban người dùng: " + name : "Ban thất bại!");
        if (ok) loadNguoiDung();
    }

    @FXML
    private void handleMakeAdmin() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(getStage(dataTable), "Vui lòng chọn người dùng!");
            return;
        }
        boolean ok = ServerService.makeAdmin(selected.optString("id"));
        showNotification(getStage(dataTable),
                ok ? "Đã cấp quyền ADMIN!" : "Thao tác thất bại!");
        if (ok) loadNguoiDung();
    }

    /**
     * btnEdit → APPROVE
     * Dùng cho tab Products (approveItem) và Sessions (approveSession).
     */
    @FXML
    private void handleEdit() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(getStage(dataTable), "Vui lòng chọn một mục!");
            return;
        }
        switch (currentTab) {
            case "SANPHAM" -> {
                boolean ok = ServerService.approveItem(selected.optString("id"));
                showNotification(getStage(dataTable),
                        ok ? "Đã duyệt sản phẩm: " + selected.optString("name")
                                : "Duyệt thất bại — chỉ duyệt được sản phẩm PENDING.");
                if (ok) loadSanPham();
            }
            case "PHIEN" -> {
                boolean ok = ServerService.approveSession(selected.optString("id"));
                showNotification(getStage(dataTable),
                        ok ? "Đã duyệt phiên: " + selected.optString("itemName")
                                : "Duyệt thất bại — chỉ duyệt được phiên PENDING.");
                if (ok) loadPhien();
            }
        }
    }

    /**
     * btnSave → REJECT
     * Dùng cho tab Products (rejectItem) và Sessions (rejectSession).
     */
    @FXML
    private void handleSave() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(getStage(dataTable), "Vui lòng chọn một mục!");
            return;
        }
        switch (currentTab) {
            case "SANPHAM" -> {
                boolean ok = ServerService.rejectItem(selected.optString("id"));
                showNotification(getStage(dataTable),
                        ok ? "Đã từ chối sản phẩm: " + selected.optString("name")
                                : "Thao tác thất bại.");
                if (ok) loadSanPham();
            }
            case "PHIEN" -> {
                boolean ok = ServerService.rejectSession(selected.optString("id"));
                showNotification(getStage(dataTable),
                        ok ? "Đã từ chối phiên: " + selected.optString("itemName")
                                : "Thao tác thất bại.");
                if (ok) loadPhien();
            }
        }
    }

    // ================================================================ //
    //  Data loaders — mỗi tab gán CellValueFactory riêng
    // ================================================================ //

    private void loadNguoiDung() {
        colId.setCellValueFactory(c -> str(truncate(c.getValue().optString("id"), 10)));
        colTen.setCellValueFactory(c -> str(c.getValue().optString("name")));
        colThongTin.setCellValueFactory(c -> str(c.getValue().optString("role")));
        colExtra.setCellValueFactory(c -> str(""));
        colTrangThai.setCellValueFactory(c -> str(""));

        new Thread(() -> populate(ServerService.getAllUsers())).start();
    }

    private void loadSanPham() {
        colId.setCellValueFactory(c -> str(truncate(c.getValue().optString("id"), 10)));
        colTen.setCellValueFactory(c -> str(c.getValue().optString("name")));
        colThongTin.setCellValueFactory(c -> {
            JSONObject o = c.getValue();
            return str(o.optString("type") + "  |  "
                    + String.format("%,.0f đ", o.optDouble("startPrice", 0)));
        });
        colExtra.setCellValueFactory(c -> str(truncate(c.getValue().optString("sellerId"), 10)));
        colTrangThai.setCellValueFactory(c -> str(c.getValue().optString("status")));

        new Thread(() -> populate(ServerService.getAllItems())).start();
    }

    private void loadPhien() {
        colId.setText("ID");
        colTen.setText("Sản phẩm");
        colThongTin.setText("Thời gian mở → Đóng");
        colExtra.setText("Giá khởi điểm  |  Bước giá");
        colTrangThai.setVisible(true);
        colTrangThai.setText("Trạng thái");

        colId.setCellValueFactory(c ->
                str(truncate(c.getValue().optString("id"), 10)));
        colTen.setCellValueFactory(c ->
                str(c.getValue().optString("itemName")));
        colThongTin.setCellValueFactory(c -> {
            JSONObject o = c.getValue();
            return str(o.optString("startTime", "").replace("T", " ")
                    + "  →  "
                    + o.optString("endTime", "").replace("T", " "));
        });
        colExtra.setCellValueFactory(c -> {
            JSONObject o = c.getValue();
            return str(String.format("%,.0f đ", o.optDouble("startPrice", 0))
                    + "  |  "
                    + String.format("%,.0f đ", o.optDouble("stepPrice", 0)));
        });
        colTrangThai.setCellValueFactory(c ->
                str(c.getValue().optString("status")));

        new Thread(() -> populate(ServerService.getAllSessions("ALL"))).start();
    }

    private void loadGiaoDich() {
        colId.setCellValueFactory(c -> str(truncate(c.getValue().optString("id"), 10)));
        colTen.setCellValueFactory(c -> str(c.getValue().optString("itemName")));
        colThongTin.setCellValueFactory(c ->
                str(c.getValue().optString("bidderName", c.getValue().optString("bidderId", ""))));
        colExtra.setCellValueFactory(c ->
                str(String.format("%,.0f đ", c.getValue().optDouble("amount", 0))));
        colTrangThai.setCellValueFactory(c ->
                str(c.getValue().optString("timestamp", "").replace("T", " ")));

        new Thread(() -> populate(ServerService.getAllTransactions())).start();
    }

    // ================================================================ //
    //  Helpers
    // ================================================================ //

    /** Đưa JSONArray vào TableView trên FX thread. */
    private void populate(JSONArray arr) {
        ObservableList<JSONObject> items = FXCollections.observableArrayList();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++)
                items.add(arr.getJSONObject(i));
        }
        Platform.runLater(() -> dataTable.setItems(items));
    }

    /** Shorthand tạo SimpleStringProperty. */
    private SimpleStringProperty str(String value) {
        return new SimpleStringProperty(value == null ? "" : value);
    }

    /** Rút ngắn UUID để cột ID không bị tràn. */
    private String truncate(String s, int maxLen) {
        if (s == null || s.length() <= maxLen) return s != null ? s : "";
        return s.substring(0, maxLen) + "…";
    }

    /** Đổi tab active trong sidebar. */
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

    /** Hiện/ẩn 4 action button tùy theo tab. */
    private void showButtons(boolean ban, boolean admin, boolean approve, boolean reject) {
        setVis(btnBan,       ban);
        setVis(btnMakeAdmin, admin);
        setVis(btnEdit,      approve);
        setVis(btnSave,      reject);
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
