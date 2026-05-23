package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SocketServer - lắng nghe kết nối TCP từ client.
 * <p>
 * Mỗi client kết nối vào sẽ được xử lý bởi 1 thread riêng (ClientHandler).
 * Dùng thread pool để giới hạn số lượng kết nối đồng thời.
 * <p>
 * Khởi động trong Main.java:
 * new SocketServer(8888).start();
 */
public class SocketServer {

    private static final int MAX_CLIENTS = 50;

    private final int port;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    private ServerSocket serverSocket;
    private boolean running = false;

    public SocketServer(int port) {
        this.port = port;
    }

    /**
     * Bắt đầu lắng nghe. Chạy vô hạn cho đến khi gọi stop().
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("[Server] Đang lắng nghe tại cổng " + port + "...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Client mới kết nối: "
                        + clientSocket.getInetAddress().getHostAddress());
                // Giao cho thread pool xử lý
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            if (running) System.err.println("[Server] Lỗi: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("[Server] Lỗi khi đóng server: " + e.getMessage());
        }
    }
}