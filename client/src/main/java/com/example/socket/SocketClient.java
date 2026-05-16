package com.example.socket;

import java.io.*;
import java.net.Socket;

/**
 * SocketClient - quản lý kết nối TCP từ client đến server.
 * Dùng Singleton để toàn app chỉ có 1 kết nối duy nhất.
 * <p>
 * Cách dùng:
 * SocketClient.getInstance().connect();
 * String response = SocketClient.getInstance().sendRequest("{\"action\":\"login\",...}");
 */
public class SocketClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    // Singleton instance
    private static SocketClient instance;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected = false;

    private SocketClient() {
    }

    public static SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    // ------------------------------------------------------------------ //
    //  Kết nối / ngắt kết nối
    // ------------------------------------------------------------------ //

    /**
     * Mở kết nối đến server. Gọi 1 lần khi khởi động app.
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            connected = true;
            System.out.println("[Client] Đã kết nối đến server " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Không thể kết nối đến server: " + e.getMessage());
            connected = false;
            return false;
        }
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

    // ------------------------------------------------------------------ //
    //  Gửi request - nhận response
    // ------------------------------------------------------------------ //

    /**
     * Gửi 1 dòng JSON lên server và nhận về 1 dòng JSON response.
     *
     * @param jsonRequest chuỗi JSON request (1 dòng, không xuống hàng)
     * @return chuỗi JSON response từ server, hoặc null nếu lỗi
     */
    public synchronized String sendRequest(String jsonRequest) {
        if (!connected) {
            System.err.println("[Client] Chưa kết nối server!");
            return null;
        }
        try {
            writer.println(jsonRequest);       // gửi đi
            String response = reader.readLine(); // đợi nhận về
            System.out.println("[Client] Gửi:  " + jsonRequest);
            System.out.println("[Client] Nhận: " + response);
            return response;
        } catch (IOException e) {
            System.err.println("[Client] Lỗi khi gửi/nhận: " + e.getMessage());
            connected = false;
            return null;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}