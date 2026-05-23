package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BidPushServer — lắng nghe kết nối persistent từ client trên port 8889.
 * <p>
 * Mỗi client join vào một phiên đấu giá sẽ:
 * 1. Kết nối vào port 8889 (kết nối này tồn tại suốt phiên)
 * 2. Gửi joinSession request
 * 3. Nhận BID_UPDATE / AUCTION_CLOSED realtime từ SocketBroadcaster
 * <p>
 * Phân biệt với SocketServer (port 8888):
 * - Port 8888: request/response — gửi → nhận → đóng logic (nhưng giữ TCP)
 * - Port 8889: persistent push — server chủ động gửi khi có sự kiện
 * <p>
 * Khởi động trong ServerMain:
 * new BidPushServer(8889).startInBackground();
 */
public class BidPushServer {

    private static final int MAX_CLIENTS = 200; // nhiều hơn API server vì giữ lâu hơn

    private final int port;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    private ServerSocket serverSocket;
    private boolean running = false;

    public BidPushServer(int port) {
        this.port = port;
    }

    /**
     * Chạy BidPushServer trong background thread riêng.
     * Gọi method này trong ServerMain để không block main thread.
     */
    public void startInBackground() {
        Thread t = new Thread(this::start, "BidPushServer-Main");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Chạy blocking — thường gọi gián tiếp qua startInBackground().
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("[BidPushServer] Đang lắng nghe tại cổng " + port + " (push realtime)...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[BidPushServer] Client mới: "
                        + clientSocket.getInetAddress().getHostAddress());
                threadPool.submit(new BidPushHandler(clientSocket));
            }
        } catch (IOException e) {
            if (running) System.err.println("[BidPushServer] Lỗi: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("[BidPushServer] Lỗi khi đóng: " + e.getMessage());
        }
        System.out.println("[BidPushServer] Đã dừng.");
    }
}