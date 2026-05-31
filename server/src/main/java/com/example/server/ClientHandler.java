package com.example.server;

import com.example.server.handler.RequestRouter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ClientHandler — chỉ chịu trách nhiệm đọc/ghi socket và gọi router.
 * <p>
 * Toàn bộ business logic đã được tách vào:
 * handler/AuthHandler.java
 * handler/ItemHandler.java
 * handler/SessionHandler.java
 * handler/BidHandler.java
 * handler/TransactionHandler.java
 * handler/AdminHandler.java
 * handler/RequestRouter.java   ← nơi đăng ký action → handler
 */
public final class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final RequestRouter router = new RequestRouter();

    public ClientHandler(final Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Server] Nhận: " + line);
                String response = handleRequest(line);
                writer.println(response);
                System.out.println("[Server] Gửi:  " + response);
            }
        } catch (IOException e) {
            System.err.println("[Server] Client ngắt kết nối: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String handleRequest(final String jsonStr) {
        try {
            return router.route(new JSONObject(jsonStr));
        } catch (Exception e) {
            System.err.println("[Server] Lỗi parse request: " + e.getMessage());
            return new JSONObject()
                    .put("success", false)
                    .put("message", "Lỗi server: " + e.getMessage())
                    .toString();
        }
    }
}