package com.example.main;

import com.example.server.BidPushServer;
import com.example.server.SocketServer;

/**
 * ServerMain - khởi động cả 2 server:
 * - SocketServer  (port 8888): API request/response (login, bid, ...)
 * - BidPushServer (port 8889): Push realtime (BID_UPDATE, AUCTION_CLOSED)
 * <p>
 * Chạy file này TRƯỚC khi chạy client.
 */
public class ServerMain {

    private static final int API_PORT = 8888;
    private static final int PUSH_PORT = 8889;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("   AUCTION SYSTEM - SERVER");
        System.out.println("=================================");

        SocketServer apiServer = new SocketServer(API_PORT);
        BidPushServer pushServer = new BidPushServer(PUSH_PORT);

        // BidPushServer chạy trong background thread riêng
        pushServer.startInBackground();

        // Xử lý Ctrl+C để tắt cả 2 server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Đang tắt...");
            pushServer.stop();
            apiServer.stop();
        }));

        // SocketServer chạy blocking ở main thread
        apiServer.start();
    }
}