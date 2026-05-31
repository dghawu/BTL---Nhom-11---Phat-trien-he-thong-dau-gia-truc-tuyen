package com.example.controller;

import com.example.config.ServerConfig;
import com.example.socket.SocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * ServerSetupController — màn hình nhập địa chỉ server khi khởi động.
 * <p>
 * Hiển thị trước Login nếu chưa có config hoặc người dùng muốn đổi server.
 * Hỗ trợ cả 2 chế độ:
 * - localhost  : chạy server trên cùng máy (demo nội bộ)
 * - ngrok/internet : nhập host + port riêng cho API và Push server
 */
public class ServerSetupController {

    // ── FXML fields ──────────────────────────────────────────────────────
    @FXML
    private RadioButton localRadio;
    @FXML
    private RadioButton ngrokRadio;
    @FXML
    private ToggleGroup modeGroup;

    @FXML
    private Label apiHostLabel;
    @FXML
    private Label apiPortLabel;
    @FXML
    private Label pushHostLabel;
    @FXML
    private Label pushPortLabel;

    @FXML
    private TextField apiHostField;
    @FXML
    private TextField apiPortField;
    @FXML
    private TextField pushHostField;
    @FXML
    private TextField pushPortField;

    @FXML
    private Button connectBtn;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    // ── Callback khi connect thành công ──────────────────────────────────
    private Runnable onConnectSuccess;

    public void setOnConnectSuccess(Runnable r) {
        this.onConnectSuccess = r;
    }

    // ── Initialize ───────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Điền sẵn giá trị đã lưu
        apiHostField.setText(ServerConfig.getApiHost());
        apiPortField.setText(String.valueOf(ServerConfig.getApiPort()));
        pushHostField.setText(ServerConfig.getPushHost());
        pushPortField.setText(String.valueOf(ServerConfig.getPushPort()));

        // Chọn chế độ dựa theo config hiện tại
        if (ServerConfig.isLocalhost()) {
            localRadio.setSelected(true);
            setNgrokMode(false);
        } else {
            ngrokRadio.setSelected(true);
            setNgrokMode(true);
        }

        loadingIndicator.setVisible(false);
        statusLabel.setVisible(false);
    }

    // ── Radio button handlers ─────────────────────────────────────────────

    @FXML
    private void handleLocalMode() {
        setNgrokMode(false);
        apiHostField.setText("localhost");
        apiPortField.setText("8888");
        pushHostField.setText("localhost");
        pushPortField.setText("8889");
    }

    @FXML
    private void handleNgrokMode() {
        setNgrokMode(true);
        // Xoá để user điền ngrok address vào
        apiHostField.clear();
        apiPortField.clear();
        pushHostField.clear();
        pushPortField.clear();
        apiHostField.setPromptText("vd: 0.tcp.ap.ngrok.io");
        apiPortField.setPromptText("vd: 12345");
        pushHostField.setPromptText("vd: 0.tcp.ap.ngrok.io");
        pushPortField.setPromptText("vd: 67890");
    }

    private void setNgrokMode(boolean ngrok) {
        // Ở chế độ localhost, ẩn các field (tự điền)
        boolean show = ngrok;
        apiHostLabel.setVisible(show);
        apiHostLabel.setManaged(show);
        apiPortLabel.setVisible(show);
        apiPortLabel.setManaged(show);
        pushHostLabel.setVisible(show);
        pushHostLabel.setManaged(show);
        pushPortLabel.setVisible(show);
        pushPortLabel.setManaged(show);
        apiHostField.setVisible(show);
        apiHostField.setManaged(show);
        apiPortField.setVisible(show);
        apiPortField.setManaged(show);
        pushHostField.setVisible(show);
        pushHostField.setManaged(show);
        pushPortField.setVisible(show);
        pushPortField.setManaged(show);
    }

    // ── Connect button ────────────────────────────────────────────────────

    @FXML
    private void handleConnect() {
        String apiHost, pushHost;
        int apiPort, pushPort;

        if (localRadio.isSelected()) {
            apiHost = "localhost";
            apiPort = 8888;
            pushHost = "localhost";
            pushPort = 8889;
        } else {
            // Validate ngrok fields
            apiHost = apiHostField.getText().trim();
            pushHost = pushHostField.getText().trim();

            if (apiHost.isEmpty() || pushHost.isEmpty()) {
                showError("Please enter the full host address.");
                return;
            }

            try {
                apiPort = Integer.parseInt(apiPortField.getText().trim());
                pushPort = Integer.parseInt(pushPortField.getText().trim());
            } catch (NumberFormatException e) {
                showError("Port must be an integer (e.g., 12345).");
                return;
            }

            if (apiPort <= 0 || apiPort > 65535 || pushPort <= 0 || pushPort > 65535) {
                showError("Port must be in the range 1–65535");
                return;
            }
        }

        // Lưu config
        ServerConfig.set(apiHost, apiPort, pushHost, pushPort);

        // Kết nối (chạy trên background thread để không đơ UI)
        setLoading(true);
        showStatus("Connecting...", "#333333");

        final String fApiHost = apiHost;
        final int fApiPort = apiPort;

        new Thread(() -> {
            // Reset và kết nối lại SocketClient
            SocketClient.resetInstance();
            boolean ok = SocketClient.getInstance().connect();

            Platform.runLater(() -> {
                setLoading(false);
                if (ok) {
                    showStatus("✓ “Connection successful!", "#1A8A1A");
                    // Chuyển sang Login sau 600ms
                    new Thread(() -> {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException ignored) {
                        }
                        Platform.runLater(() -> {
                            if (onConnectSuccess != null) onConnectSuccess.run();
                        });
                    }).start();
                } else {
                    showStatus("✗ Unable to connect " + fApiHost + ":" + fApiPort
                            + ". Please check the ngrok address again.", "#CC0000");
                }
            });
        }, "ServerSetup-Connect").start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        connectBtn.setDisable(loading);
    }

    private void showStatus(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void showError(String msg) {
        showStatus("✗ " + msg, "#CC0000");
    }
}
