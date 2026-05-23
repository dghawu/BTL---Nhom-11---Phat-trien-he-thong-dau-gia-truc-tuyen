package com.example.socket;

import com.example.config.ServerConfig;

import java.io.*;
import java.net.Socket;

/**
 * SocketClient - quản lý kết nối TCP từ client đến server (port API).
 * Dùng Singleton để toàn app chỉ có 1 kết nối duy nhất.
 *
 * ── Thay đổi so với bản gốc ──────────────────────────────────────────────
 * Bỏ hardcode "localhost" → đọc từ ServerConfig (hỗ trợ ngrok/internet).
 * Thêm reconnect() để tạo lại kết nối sau khi đổi server config.
 */
public class SocketClient {

    // Singleton instance
    private static SocketClient instance;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected = false;

    private SocketClient() {}

    public static SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    /**
     * Reset singleton — gọi sau khi đổi ServerConfig để tạo kết nối mới.
     */
    public static void resetInstance() {
        if (instance != null) {
            instance.disconnect();
            instance = null;
        }
    }

    // ── Kết nối / ngắt kết nối ──────────────────────────────────────────

    /**
     * Mở kết nối đến server. Gọi 1 lần khi khởi động app.
     * Host và port lấy từ ServerConfig (đã load từ server.properties).
     */
    public boolean connect() {
        String host = ServerConfig.getApiHost();
        int    port = ServerConfig.getApiPort();
        try {
            socket  = new Socket(host, port);
            reader  = new BufferedReader(new InputStreamReader(socket.getInputStream(),  "UTF-8"));
            writer  = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            connected = true;
            System.out.println("[Client] Đã kết nối đến server " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Không thể kết nối đến " + host + ":" + port
                    + " — " + e.getMessage());
            connected = false;
            return false;
        }
    }

    /**
     * Ngắt kết nối hiện tại rồi kết nối lại với config mới.
     * Gọi sau khi user lưu ServerConfig mới từ dialog.
     */
    public boolean reconnect() {
        disconnect();
        return connect();
    }

    /**
     * Đóng kết nối khi thoát app.
     */
    public void disconnect() {
        try {
            connected = false;
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[Client] Đã ngắt kết nối.");
        } catch (IOException e) {
            System.err.println("[Client] Lỗi khi đóng socket: " + e.getMessage());
        }
    }

    // ── Gửi request - nhận response ─────────────────────────────────────

    /**
     * Gửi 1 dòng JSON lên server và nhận về 1 dòng JSON response.
     */
    public synchronized String sendRequest(String jsonRequest) {
        if (!connected) {
            System.err.println("[Client] Chưa kết nối server!");
            return null;
        }
        try {
            writer.println(jsonRequest);
            String response = reader.readLine();
            System.out.println("[Client] Gửi:  " + jsonRequest);
            System.out.println("[Client] Nhận: " + response);
            return response;
        } catch (IOException e) {
            System.err.println("[Client] Lỗi khi gửi/nhận: " + e.getMessage());
            connected = false;
            return null;
        }
    }

    public boolean isConnected() { return connected; }
}
