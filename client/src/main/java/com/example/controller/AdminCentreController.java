package com.example.controller;

import com.example.socket.ServerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * AdminCentreController - AdminCentre.fxml
 * Sidebar chuyển đổi giữa 4 bảng: Người dùng / Sản phẩm / Phiên / Giao dịch.
 */
@SuppressWarnings("unchecked")
public class AdminCentreController extends BaseController {

    // ── Nav sidebar ───────────────────────────────────────────────────
    @FXML
    private Button btnNguoiDung;
    @FXML
    private Button btnSanPham;
    @FXML
    private Button btnPhien;
    @FXML
    private Button btnGiaoDich;

    // ── Tiêu đề tab ───────────────────────────────────────────────────
    @FXML
    private Label lblTabTitle;

    // ── Table ─────────────────────────────────────────────────────────
    @FXML
    private TableView<JSONObject> dataTable;
    @FXML
    private TableColumn<JSONObject, String> colId;
    @FXML
    private TableColumn<JSONObject, Node> colImage;
    @FXML
    private TableColumn<JSONObject, String> colTen;
    @FXML
    private TableColumn<JSONObject, String> colThongTin;
    @FXML
    private TableColumn<JSONObject, String> colExtra;
    @FXML
    private TableColumn<JSONObject, String> colExtra2;
    @FXML
    private TableColumn<JSONObject, String> colTrangThai;

    // ── Action buttons ────────────────────────────────────────────────
    @FXML
    private Button btnBan;
    @FXML
    private Button btnMakeAdmin;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnSave;

    private String currentTab = "NGUOIDUNG";

    // ================================================================ //
    //  Init
    // ================================================================ //

    @FXML
    public void initialize() {
        dataTable.setPlaceholder(new Label("No data available."));
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

        colId.setText("ID");
        colTen.setText("User Name");
        colThongTin.setText("Role");
        colExtra.setText("");
        colTrangThai.setVisible(false);
        colImage.setVisible(false);

        showButtons(true, true, false, false);

        loadNguoiDung();
    }

    @FXML
    private void handleShowSanPham() {
        currentTab = "SANPHAM";
        setActiveTab(btnSanPham);

        colId.setText("ID");
        colTen.setText("Product name");
        colThongTin.setText("Category  |  Starting price");
        colExtra.setText("Seller");
        colTrangThai.setVisible(true);
        colTrangThai.setText("Status");
        colImage.setVisible(true);

        showButtons(false, false, true, true);

        dataTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
                if (selected != null) showProductDetail(selected);
            }
        });

        loadSanPham();
    }
    /**
     * Dialog duyệt sản phẩm với hình ảnh và attributes
     */
    private void showProductDetail(JSONObject product) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Product details");
        dialog.initOwner(getStage(dataTable));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20));

        // Hiển thị ảnh sản phẩm
        VBox imageBox = new VBox();
        imageBox.setStyle("-fx-alignment: CENTER;");
        String imageBase64 = product.optString("image", "");
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(imageBase64);
                ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
                Image img = new Image(bais);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(250);
                iv.setFitHeight(250);
                iv.setPreserveRatio(true);
                imageBox.getChildren().add(iv);
            } catch (Exception e) {
                System.err.println("[AdminCentre] Image decode error: " + e.getMessage());
                imageBox.getChildren().add(new Label("Unable to load image"));
            }
        } else {
            imageBox.getChildren().add(new Label("No image available"));
        }
        grid.add(imageBox, 0, 0, 2, 1);

        // Lấy attributes từ product
        String attr1 = product.optString("attr1", "");
        String attr2 = product.optString("attr2", "");
        String category = product.optString("type", "");

        // Tạo VBox cho attributes
        VBox attrBox = new VBox(5);
        attrBox.setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 5; " +
                "-fx-background-color: #f9f9f9; -fx-background-radius: 5;");

        if (!attr1.isEmpty() || !attr2.isEmpty()) {
            Label attrTitle = new Label("Product Attributes:");
            attrTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            attrBox.getChildren().add(attrTitle);

            switch (category.toUpperCase()) {
                case "FASHION" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • Size: " + attr2));
                }
                case "ART" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • Artist: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • Medium: " + attr2));
                }
                case "VEHICLE" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • Mileage: " + attr2 + " km"));
                }
                case "ELECTRONICS" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • Warranty: " + attr2 + " months"));
                }
                default -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • " + attr2));
                }
            }
        }

        String[][] rows = {
                {"Product name", product.optString("name", "")},

                {"Product ID", product.optString("id", "")},
                {"Category", category},

                {"Product id", product.optString("id", "")},
                {"Category", product.optString("type", "")},

                {"Starting price", String.format("%,.0f đ", product.optDouble("startPrice", 0))},
                {"Seller", product.optString("sellerName", product.optString("sellerId", ""))},
                {"Status", product.optString("status", "")},
        };

        int rowIndex = 1;
        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0]);
            key.setStyle("-fx-font-weight: bold;");
            Label val = new Label(rows[i][1]);
            grid.add(key, 0, rowIndex);
            grid.add(val, 1, rowIndex);
            rowIndex++;
        }

        // Thêm attributes nếu có
        if (!attrBox.getChildren().isEmpty()) {
            grid.add(attrBox, 0, rowIndex, 2, 1);
        }

        dialog.getDialogPane().setContent(grid);

        boolean isPending = "PENDING".equals(product.optString("status"));
        if (isPending) {
            ButtonType approveBtn = new ButtonType("✔ APPROVE", ButtonBar.ButtonData.OK_DONE);
            ButtonType rejectBtn = new ButtonType("✘ REJECT", ButtonBar.ButtonData.CANCEL_CLOSE);

            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(approveBtn, rejectBtn, cancelBtn);

            ButtonType cancelBtn = new ButtonType("CLOSE", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(
                    approveBtn, rejectBtn, cancelBtn
            );

            for (ButtonType bt : dialog.getDialogPane().getButtonTypes()) {
                Button btn = (Button) dialog.getDialogPane().lookupButton(bt);

                btn.setStyle("""
        -fx-background-radius: 20;
        -fx-border-radius: 20;
        -fx-padding: 8 20 8 20;
    """);
            }


            dialog.showAndWait().ifPresent(result -> {
                String id = product.optString("id");
                if (result == approveBtn) {
                    new Thread(() -> {
                        boolean ok = ServerService.approveItem(id);
                        Platform.runLater(() -> {
                            showNotification(getStage(dataTable),
                                    ok ? "Product approved!" : "Approval failed!");
                            if (ok) loadSanPham();
                        });
                    }).start();
                } else if (result == rejectBtn) {
                    new Thread(() -> {
                        boolean ok = ServerService.rejectItem(id);
                        Platform.runLater(() -> {
                            showNotification(getStage(dataTable),
                                    ok ? "Product rejected!" : "Operation failed!");
                            if (ok) loadSanPham();
                        });
                    }).start();
                }
            });
        } else {
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            Button closeBtn =
                    (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);

            closeBtn.setStyle("""
        -fx-background-radius: 20;
        -fx-border-radius: 20;
        -fx-padding: 8 25 8 25;
        -fx-background-color: white;
        -fx-border-color: #CCCCCC;
        -fx-border-width: 1;
    """);

            dialog.showAndWait();
        }
    }

    @FXML
    private void handleShowPhien() {
        currentTab = "PHIEN";
        setActiveTab(btnPhien);

        colId.setText("ID");
        colTen.setText("Product");
        colThongTin.setText("Opening Time → Closing Time");
        colExtra.setText("Starting price  |  Bid increment");
        colTrangThai.setVisible(true);
        colTrangThai.setText("Status");
        colImage.setVisible(true);

        showButtons(false, false, false, false);

        dataTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
                if (selected != null) showSessionDetail(selected);
            }
        });

        loadPhien();
    }

    /**
     * Dialog duyệt phiên với hình ảnh và attributes
     */
    private void showSessionDetail(JSONObject session) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Auction details");
        dialog.initOwner(getStage(dataTable));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20));

        // Hiển thị ảnh sản phẩm
        VBox imageBox = new VBox();
        imageBox.setStyle("-fx-alignment: CENTER;");
        String imageBase64 = session.optString("itemImage", "");
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(imageBase64);
                ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
                Image img = new Image(bais);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(200);
                iv.setFitHeight(200);
                iv.setPreserveRatio(true);
                imageBox.getChildren().add(iv);
            } catch (Exception e) {
                System.err.println("[AdminCentre] Image decode error: " + e.getMessage());
                imageBox.getChildren().add(new Label("Unable to load image"));
            }
        } else {
            imageBox.getChildren().add(new Label("No image available"));
        }
        grid.add(imageBox, 0, 0, 2, 1);

        // Lấy attributes từ session
        String attr1 = session.optString("attr1", "");
        String attr2 = session.optString("attr2", "");
        String category = session.optString("category", "");

        // Tạo VBox cho attributes
        VBox attrBox = new VBox(5);
        attrBox.setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 5; " +
                "-fx-background-color: #f9f9f9; -fx-background-radius: 5;");

        if (!attr1.isEmpty() || !attr2.isEmpty()) {
            Label attrTitle = new Label("Product Attributes:");
            attrTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            attrBox.getChildren().add(attrTitle);

            switch (category.toUpperCase()) {
                case "FASHION" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • Size: " + attr2));
                }
                case "ART" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • Artist: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • Medium: " + attr2));
                }
                case "VEHICLE" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • Mileage: " + attr2 + " km"));
                }
                case "ELECTRONICS" -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • Brand: " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • Warranty: " + attr2 + " months"));
                }
                default -> {
                    if (!attr1.isEmpty()) attrBox.getChildren().add(new Label("  • " + attr1));
                    if (!attr2.isEmpty()) attrBox.getChildren().add(new Label("  • " + attr2));
                }
            }
        }

        String[][] rows = {
                {"Product", session.optString("itemName", "")},
                {"Session ID", session.optString("id", "")},
                {"Seller", session.optString("sellerName", session.optString("sellerId", "—"))},
                {"Status", session.optString("status", "")},
                {"Starting price", String.format("%,.0f đ", session.optDouble("startPrice", 0))},
                {"Bid increment", String.format("%,.0f đ", session.optDouble("stepPrice", 0))},
                {"Current price", String.format("%,.0f đ", session.optDouble("currentPrice", 0))},
                {"Opening time", session.optString("startTime", "").replace("T", " ")},
                {"Closing time", session.optString("endTime", "").replace("T", " ")},
                {"Winner", session.optString("currentWinner", "—")},
        };

        int rowIndex = 1;
        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0]);
            key.setStyle("-fx-font-weight: bold;");
            Label val = new Label(rows[i][1]);
            grid.add(key, 0, rowIndex);
            grid.add(val, 1, rowIndex);
            rowIndex++;
        }

        // Thêm attributes nếu có
        if (!attrBox.getChildren().isEmpty()) {
            grid.add(attrBox, 0, rowIndex, 2, 1);
        }

        dialog.getDialogPane().setContent(grid);

        boolean isPending = "PENDING".equals(session.optString("status"));
        if (isPending) {
            ButtonType approveBtn = new ButtonType("✔ APPROVE", ButtonBar.ButtonData.OK_DONE);
            ButtonType rejectBtn = new ButtonType("✘ REJECT", ButtonBar.ButtonData.CANCEL_CLOSE);

            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(approveBtn, rejectBtn, cancelBtn);

            ButtonType cancelBtn = new ButtonType("CLOSE", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(
                    approveBtn, rejectBtn, cancelBtn
            );

            for (ButtonType bt : dialog.getDialogPane().getButtonTypes()) {
                Button btn = (Button) dialog.getDialogPane().lookupButton(bt);

                btn.setStyle("""
        -fx-background-radius: 20;
        -fx-border-radius: 20;
        -fx-padding: 8 20 8 20;
    """);
            }


            java.util.Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent()) {
                String id = session.optString("id");
                ButtonType clicked = result.get();

                if (clicked == approveBtn) {
                    new Thread(() -> {
                        boolean ok = ServerService.approveSession(id);
                        Platform.runLater(() -> {
                            showNotification(getStage(dataTable),
                                    ok ? "Auction approved!" : "Approval failed!");
                            if (ok) loadPhien();
                        });
                    }).start();
                } else if (clicked == rejectBtn) {
                    new Thread(() -> {
                        boolean ok = ServerService.rejectSession(id);
                        Platform.runLater(() -> {
                            showNotification(getStage(dataTable),
                                    ok ? "Auction rejected!" : "Operation failed!");
                            if (ok) loadPhien();
                        });
                    }).start();
                }
            }
        } else {
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            Button closeBtn =
                    (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);

            closeBtn.setStyle("""
        -fx-background-radius: 20;
        -fx-border-radius: 20;
        -fx-padding: 8 25 8 25;
        -fx-background-color: white;
        -fx-border-color: #CCCCCC;
        -fx-border-width: 1;
    """);

            dialog.showAndWait();
        }
    }

    @FXML
    private void handleShowGiaoDich() {
        currentTab = "GIAODICH";
        setActiveTab(btnGiaoDich);

        colId.setText("ID");
        colTen.setText("Product");
        colThongTin.setText("Bidder");
        colExtra.setText("Amount");
        colTrangThai.setVisible(true);
        colTrangThai.setText("Time");
        colImage.setVisible(true);

        showButtons(false, false, false, false);

        loadGiaoDich();
    }

    // ================================================================ //
    //  Action handlers
    // ================================================================ //

    @FXML
    private void handleBan() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(getStage(dataTable), "Please select a user!");
            return;
        }
        boolean ok = ServerService.banUser(selected.optString("id"));
        showNotification(getStage(dataTable),
                ok ? "User locked!" : "Operation failed!");
        if (ok) loadNguoiDung();
    }

    @FXML
    private void handleMakeAdmin() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(getStage(dataTable), "Please select a user!");
            return;
        }
        boolean ok = ServerService.makeAdmin(selected.optString("id"));
        showNotification(getStage(dataTable),
                ok ? "ADMIN role granted!" : "Operation failed!");
        if (ok) loadNguoiDung();
    }

    @FXML
    private void handleEdit() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(getStage(dataTable), "Please select an item!");
            return;
        }
        switch (currentTab) {
            case "PHIEN" -> {
                boolean ok = ServerService.approveSession(selected.optString("id"));
                showNotification(getStage(dataTable),
                        ok ? "Auction approved: " + selected.optString("itemName")
                                : "Approval failed — only PENDING auctions can be approved.");
                if (ok) loadPhien();
            }
        }
    }

    @FXML
    private void handleSave() {
        JSONObject selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification(getStage(dataTable), "Please select an item!");
            return;
        }
        switch (currentTab) {
            case "PHIEN" -> {
                boolean ok = ServerService.rejectSession(selected.optString("id"));
                showNotification(getStage(dataTable),
                        ok ? "Auction rejected: " + selected.optString("itemName")
                                : "Operation failed.");
                if (ok) loadPhien();
            }
        }
    }

    // ================================================================ //
    //  Data loaders
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
        colThongTin.setCellValueFactory(c -> str(c.getValue().optString("type")));  // Loại
        colExtra.setCellValueFactory(c ->                                            // Giá khởi điểm
                str(String.format("%,.0f đ", c.getValue().optDouble("startPrice", 0))));
        colExtra2.setCellValueFactory(c ->                                           // Seller
                str(c.getValue().optString("sellerName", c.getValue().optString("sellerId"))));
        colTrangThai.setCellValueFactory(c -> str(c.getValue().optString("status")));

        colThongTin.setText("Category");
        colExtra.setText("Starting price");
        colExtra2.setText("Seller");
        colExtra2.setVisible(true);

        colImage.setCellValueFactory(c -> {
            String imageBase64 = c.getValue().optString("image", "");
            VBox imgBox = createImageBox(imageBase64);
            return new SimpleObjectProperty<Node>(imgBox);
        });

        new Thread(() -> populate(ServerService.getAllItems())).start();
    }

    private void loadPhien() {
        colId.setCellValueFactory(c ->
                str(truncate(c.getValue().optString("id"), 10)));
        colTen.setCellValueFactory(c ->
                str(c.getValue().optString("itemName")));
        colThongTin.setCellValueFactory(c -> {
            JSONObject o = c.getValue();
            String start = o.optString("startTime", "").replace("T", " ");
            String end = o.optString("endTime", "").replace("T", " ");
            return str("Open: " + start + "\nClose: " + end);
        });
        colThongTin.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else { setText(item); setWrapText(true); }
            }
        });
        colExtra.setCellValueFactory(c -> {
            JSONObject o = c.getValue();
            return str("Starting price: " + String.format("%,.0f đ", o.optDouble("startPrice", 0))
                    + "\nBid increment: " + String.format("%,.0f đ", o.optDouble("stepPrice", 0)));
        });
        colExtra.setText("Price");
        colExtra.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else { setText(item); setWrapText(true); }
            }
        });
        colExtra2.setCellValueFactory(c ->
                str(c.getValue().optString("sellerName", c.getValue().optString("sellerId"))));
        colExtra2.setText("Seller");
        colExtra2.setVisible(true);
        colTrangThai.setCellValueFactory(c ->
                str(c.getValue().optString("status")));

        colImage.setCellValueFactory(c -> {
            String imageBase64 = c.getValue().optString("itemImage", "");
            VBox imgBox = createImageBox(imageBase64);
            return new SimpleObjectProperty<Node>(imgBox);
        });

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

        colImage.setCellValueFactory(c -> {
            String imageBase64 = c.getValue().optString("itemImage", "");
            VBox imgBox = createImageBox(imageBase64);
            return new SimpleObjectProperty<Node>(imgBox);
        });

        new Thread(() -> populate(ServerService.getAllTransactions())).start();
    }

    // ================================================================ //
    //  Helpers
    // ================================================================ //

    private VBox createImageBox(String imageBase64) {
        VBox imgBox = new VBox();
        imgBox.setStyle("-fx-alignment: CENTER; -fx-padding: 5;");

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(imageBase64);
                ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
                Image img = new Image(bais);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                iv.setPreserveRatio(true);
                imgBox.getChildren().add(iv);
            } catch (Exception e) {
                System.err.println("[AdminCentre] Image decode error: " + e.getMessage());
                imgBox.getChildren().add(new Label("—"));
            }
        } else {
            imgBox.getChildren().add(new Label("—"));
        }
        return imgBox;
    }

    private void populate(JSONArray arr) {
        ObservableList<JSONObject> items = FXCollections.observableArrayList();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++)
                items.add(arr.getJSONObject(i));
        }
        Platform.runLater(() -> dataTable.setItems(items));
    }

    private SimpleStringProperty str(String value) {
        return new SimpleStringProperty(value == null ? "" : value);
    }

    private String truncate(String s, int maxLen) {
        if (s == null || s.length() <= maxLen) return s != null ? s : "";
        return s.substring(0, maxLen) + "…";
    }

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

    private void showButtons(boolean ban, boolean admin, boolean approve, boolean reject) {
        setVis(btnBan, ban);
        setVis(btnMakeAdmin, admin);
        setVis(btnEdit, approve);
        setVis(btnSave, reject);
    }

    private void setVis(Button btn, boolean v) {
        btn.setVisible(v);
        btn.setManaged(v);
    }

    @FXML
    private void handleHome() {
        goHome(getStage(dataTable));
    }

    @FXML
    private void handleAdminCentre() { /* đã ở đây */ }

    @FXML
    private void handleUserReport() { /* TODO */ }

    @FXML
    private void handleSettings() {
        goSettings(getStage(dataTable));
    }
}