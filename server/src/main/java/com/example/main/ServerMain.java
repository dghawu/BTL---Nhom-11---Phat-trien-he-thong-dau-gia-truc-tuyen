package com.example;

import com.example.server.SocketServer;

/**
 * ServerMain - khởi động server.
 * Chạy file này TRƯỚC khi chạy client.
 *
 * Trong IntelliJ: Run → ServerMain (tạo Run Configuration riêng)
 */
public class ServerMain {

    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   AUCTION SYSTEM - SERVER");
        System.out.println("=================================");

        SocketServer server = new SocketServer(PORT);

        // Xử lý Ctrl+C để tắt server gọn
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Đang tắt server...");
            server.stop();
        }));

        server.start(); // chạy vô hạn
    }
}