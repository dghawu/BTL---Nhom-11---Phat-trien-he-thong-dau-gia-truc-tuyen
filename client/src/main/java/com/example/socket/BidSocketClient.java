package com.example.socket;

import com.example.config.ServerConfig;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * BidSocketClient — kết nối persistent đến Push Server (port 8889).
 *
 * ── Thay đổi so với bản gốc ──────────────────────────────────────────────
 * Bỏ hardcode "localhost":8889 → đọc từ ServerConfig.
 * Hỗ trợ kết nối qua ngrok (host và port riêng biệt cho push server).
 */
public class BidSocketClient {

    // Singleton
    private static BidSocketClient instance;

    private Socket socket;
    private PrintWriter writer;
    private Thread listenerThread;
    private boolean running = false;

    private Consumer<BidEvent> onEvent;

    private BidSocketClient() {}

    public static synchronized BidSocketClient getInstance() {
        if (instance == null) instance = new BidSocketClient();
        return instance;
    }

    // ── Public API ───────────────────────────────────────────────────────

    /**
     * Kết nối đến Push Server và join vào phiên đấu giá.
     * Host/port lấy từ ServerConfig (hỗ trợ ngrok).
     */
    public void joinSession(String sessionId, String token, Consumer<BidEvent> callback) {
        leave();

        this.onEvent = callback;

        String pushHost = ServerConfig.getPushHost();
        int    pushPort = ServerConfig.getPushPort();

        try {
            socket = new Socket(pushHost, pushPort);
            writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Gửi joinSession request
            JSONObject req = new JSONObject();
            req.put("action",    "joinSession");
            req.put("sessionId", sessionId);
            req.put("token",     token);
            writer.println(req.toString());

            // Đọc response xác nhận join
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String ackLine = reader.readLine();
            if (ackLine == null) {
                System.err.println("[BidSocketClient] Server không phản hồi joinSession.");
                disconnect();
                return;
            }

            JSONObject ack = new JSONObject(ackLine);
            if (!ack.getBoolean("success")) {
                System.err.println("[BidSocketClient] joinSession thất bại: "
                        + ack.optString("message"));
                disconnect();
                return;
            }

            System.out.println("[BidSocketClient] Đã join phiên " + sessionId
                    + " qua " + pushHost + ":" + pushPort);

            running = true;
            listenerThread = new Thread(() -> listenLoop(reader), "BidPush-Listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

        } catch (IOException e) {
            System.err.println("[BidSocketClient] Không thể kết nối Push Server "
                    + pushHost + ":" + pushPort + " — " + e.getMessage());
        }
    }

    /**
     * Rời phiên — đóng kết nối, dừng listener thread.
     */
    public void leave() {
        running = false;
        disconnect();
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
        onEvent = null;
        System.out.println("[BidSocketClient] Đã rời phiên.");
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && running;
    }

    // ── Internal ─────────────────────────────────────────────────────────

    private void listenLoop(BufferedReader reader) {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                System.out.println("[BidSocketClient] Push nhận: " + line);
                BidEvent event = BidEvent.parse(line);
                if (event != null && onEvent != null) {
                    onEvent.accept(event);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[BidSocketClient] Mất kết nối Push Server: " + e.getMessage());
            }
        } finally {
            running = false;
        }
    }

    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        socket = null;
        writer = null;
    }

    // ── BidEvent ─────────────────────────────────────────────────────────

    public static class BidEvent {

        public enum Type { BID_UPDATE, AUCTION_CLOSED, ANTI_SNIPE, UNKNOWN }

        public final Type   type;
        public final String sessionId;
        public final double price;
        public final String bidderName;
        public final String endTime;
        public final long   extendMinutes;
        public final String snipeTime;

        private BidEvent(Type type, String sessionId, double price,
                         String bidderName, String endTime, long extendMinutes, String snipeTime) {
            this.type       = type;
            this.sessionId  = sessionId;
            this.price      = price;
            this.bidderName = bidderName;
            this.endTime    = endTime;
            this.extendMinutes = extendMinutes;
            this.snipeTime     = snipeTime;
        }


        public static BidEvent parse(String line) {
            if (line == null || line.isBlank()) return null;
            try {
                // ANTI_SNIPE dùng | để tránh conflict với dấu : trong datetime
                if (line.startsWith("ANTI_SNIPE|")) {
                    String[] parts = line.split("\\|", 5);
                    return new BidEvent(
                            Type.ANTI_SNIPE, parts[1], 0, parts[2], "",
                            parts.length > 4 ? Long.parseLong(parts[4]) : 0,
                            parts.length > 3 ? parts[3] : "");
                }

                String[] parts = line.split(":", 5);
                if (parts.length < 4) return null;

                return switch (parts[0]) {
                    case "BID_UPDATE" -> new BidEvent(
                            Type.BID_UPDATE, parts[1],
                            Double.parseDouble(parts[2]), parts[3],
                            parts.length > 4 ? parts[4] : "", 0, "");
                    case "AUCTION_CLOSED" -> new BidEvent(
                            Type.AUCTION_CLOSED, parts[1],
                            Double.parseDouble(parts[3]), parts[2],
                            "", 0, "");
                    default -> {
                        System.err.println("[BidSocketClient] Event không xác định: " + parts[0]);
                        yield null;
                    }
                };
            } catch (NumberFormatException e) {
                System.err.println("[BidSocketClient] Lỗi parse event: " + line);
                return null;
            }
        }

        @Override
        public String toString() {
            return "BidEvent{type=" + type + ", session=" + sessionId
                    + ", price=" + price + ", bidder=" + bidderName + "}";
        }
    }
}
