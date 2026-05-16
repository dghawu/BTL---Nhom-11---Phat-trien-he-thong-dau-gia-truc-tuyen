package com.example.socket;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * BidSocketClient — kết nối persistent đến Push Server (port 8889).
 * Cách dùng trong AuctionRoomController:
 *
 *   // Khi mở phòng đấu giá
 *   BidSocketClient.getInstance().joinSession(
 *       sessionId,
 *       ServerService.getToken(),
 *       event -> handleBidEvent(event)   // callback chạy trên background thread
 *   );
 *
 *   // Khi thoát phòng
 *   BidSocketClient.getInstance().leave();
 *
 * Message format server → client:
 *   BID_UPDATE:sessionId:price:bidderName:endTime
 *   AUCTION_CLOSED:sessionId:winnerName:finalPrice
 */
public class BidSocketClient {

    private static final String SERVER_HOST = "localhost";
    private static final int    PUSH_PORT   = 8889;

    // Singleton
    private static BidSocketClient instance;

    private Socket       socket;
    private PrintWriter  writer;
    private Thread       listenerThread;
    private boolean      running = false;

    // Callback nhận event — set mỗi lần joinSession
    private Consumer<BidEvent> onEvent;

    private BidSocketClient() {}

    public static synchronized BidSocketClient getInstance() {
        if (instance == null) instance = new BidSocketClient();
        return instance;
    }

    // ------------------------------------------------------------------ //
    //  Public API
    // ------------------------------------------------------------------ //

    /**
     * Kết nối đến Push Server và join vào phiên đấu giá.
     *
     * @param sessionId  ID phiên đấu giá
     * @param token      JWT token (lấy từ ServerService.getToken())
     * @param callback   hàm xử lý event — gọi trên background thread,
     *                   dùng Platform.runLater() trong callback nếu cập nhật UI
     */
    public void joinSession(String sessionId, String token, Consumer<BidEvent> callback) {
        // Nếu đang kết nối phiên khác thì leave trước
        leave();

        this.onEvent = callback;

        try {
            socket = new Socket(SERVER_HOST, PUSH_PORT);
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

            System.out.println("[BidSocketClient] Đã join phiên " + sessionId);

            // Khởi động background thread lắng nghe push
            running = true;
            listenerThread = new Thread(() -> listenLoop(reader), "BidPush-Listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

        } catch (IOException e) {
            System.err.println("[BidSocketClient] Không thể kết nối Push Server: " + e.getMessage());
        }
    }

    /**
     * Rời phiên — đóng kết nối, dừng listener thread.
     * Gọi khi người dùng thoát AuctionRoom.
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

    // ------------------------------------------------------------------ //
    //  Internal
    // ------------------------------------------------------------------ //

    /** Vòng lặp đọc push từ server — chạy trên listenerThread. */
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

    // ------------------------------------------------------------------ //
    //  BidEvent — dữ liệu parse từ 1 dòng push
    // ------------------------------------------------------------------ //

    /**
     * Đại diện cho 1 sự kiện nhận từ Push Server.
     *
     * Hai loại event:
     *   BID_UPDATE      → type=BID_UPDATE,  sessionId, price, bidderName, endTime
     *   AUCTION_CLOSED  → type=AUCTION_CLOSED, sessionId, winnerName, finalPrice
     */
    public static class BidEvent {

        public enum Type { BID_UPDATE, AUCTION_CLOSED, UNKNOWN }

        public final Type   type;
        public final String sessionId;
        public final double price;        // giá mới (BID_UPDATE) hoặc giá cuối (AUCTION_CLOSED)
        public final String bidderName;   // người đặt (BID_UPDATE) hoặc người thắng (AUCTION_CLOSED)
        public final String endTime;      // endTime mới (có thể thay đổi vì anti-snipe)

        private BidEvent(Type type, String sessionId, double price,
                         String bidderName, String endTime) {
            this.type       = type;
            this.sessionId  = sessionId;
            this.price      = price;
            this.bidderName = bidderName;
            this.endTime    = endTime;
        }

        /**
         * Parse 1 dòng push từ server.
         *
         * Format BID_UPDATE:     "BID_UPDATE:sessionId:price:bidderName:endTime"
         * Format AUCTION_CLOSED: "AUCTION_CLOSED:sessionId:winnerName:finalPrice"
         *
         * @return BidEvent hoặc null nếu không parse được
         */
        public static BidEvent parse(String line) {
            if (line == null || line.isBlank()) return null;

            String[] parts = line.split(":", 5);   // tối đa 5 phần
            if (parts.length < 4) return null;

            try {
                return switch (parts[0]) {
                    case "BID_UPDATE" -> new BidEvent(
                            Type.BID_UPDATE,
                            parts[1],                        // sessionId
                            Double.parseDouble(parts[2]),    // price
                            parts[3],                        // bidderName
                            parts.length > 4 ? parts[4] : ""// endTime (optional)
                    );
                    case "AUCTION_CLOSED" -> new BidEvent(
                            Type.AUCTION_CLOSED,
                            parts[1],                        // sessionId
                            Double.parseDouble(parts[3]),    // finalPrice
                            parts[2],                        // winnerName
                            ""
                    );
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