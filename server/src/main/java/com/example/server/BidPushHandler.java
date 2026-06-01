package com.example.server;

import com.example.auth.AuthResult;
import com.example.auth.TokenGuard;
import com.example.observer.SocketBroadcaster;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * BidPushHandler — xử lý 1 client kết nối vào Push Server (port 8889).
 * <p>
 * Khác với ClientHandler (req/res), BidPushHandler giữ kết nối persistent:
 * Client kết nối → gửi joinSession → server đăng ký client
 * vào SocketBroadcaster
 * → Server push BID_UPDATE / AUCTION_CLOSED trực tiếp qua kết nối này.
 * <p>
 * Protocol (client → server):
 * Bước 1 — joinSession:
 * {"action": "joinSession", "sessionId": "AUC-001", "token": "eyJ..."}
 * Bước 2 — server reply:
 * {"success": true, "message": "Joined AUC-001"}  (hoặc fail)
 * Bước 3 — server push (bất cứ lúc nào có bid mới):
 * BID_UPDATE:AUC-001:16000000.0:userId:2025-05-20T21:00
 * AUCTION_CLOSED:AUC-001:winnerId:16000000.0
 * <p>
 * Client chỉ cần giữ kết nối đọc (blocking readLine()).
 * Nếu client ngắt kết nối, SocketBroadcaster sẽ tự dọn dẹp
 * lần broadcast tiếp theo.
 */
public final class BidPushHandler implements Runnable {

    /**
     * Kết nối client push.
     */
    private final Socket clientSocket;

    /**
     * Tạo handler cho kết nối client push.
     *
     * @param socket socket client push
     */
    public BidPushHandler(final Socket socket) {
        this.clientSocket = socket;
    }

    /**
     * Chạy vòng lặp đọc dữ liệu từ client push và duy trì kết nối.
     */
    @Override
    public void run() {
        String clientAddr = clientSocket.getInetAddress().getHostAddress();
        System.out.println("[BidPushHandler] Client kết nối: " + clientAddr);

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream(),
                                "UTF-8"));
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(
                                clientSocket.getOutputStream(),
                                "UTF-8"), true)
        ) {
            // Bước 1: nhận joinSession request
            String line = reader.readLine();
            if (line == null) {
                System.out.println(
                            "[BidPushHandler] Client ngắt kết nối ngay sau "
                                    + "khi connect.");
                return;
            }

            System.out.println("[BidPushHandler] Nhận: " + line);

            JSONObject req;
            try {
                req = new JSONObject(line);
            } catch (Exception e) {
                writer.println(fail("Request không hợp lệ: " + e.getMessage()));
                return;
            }

            // Kiểm tra action
            String action = req.optString("action", "");
            if (!"joinSession".equals(action)) {
                writer.println(fail("Lệnh đầu tiên phải là joinSession."));
                return;
            }

            // Kiểm tra token
            AuthResult auth = TokenGuard.check(req);
            if (!auth.isOk()) {
                writer.println(fail(auth.getErrorMessage()));
                return;
            }

            // Lấy sessionId
            String sessionId = req.optString("sessionId", "");
            if (sessionId.isBlank()) {
                writer.println(fail("Thiếu sessionId."));
                return;
            }

            // Đăng ký client vào SocketBroadcaster của phiên
            SocketBroadcaster broadcaster = BidRegistry.getInstance()
                    .getOrCreate(sessionId);
            broadcaster.addSubscriber(clientSocket);

            // Báo thành công
            writer.println(success("Joined " + sessionId));
            String joinMessage = "[BidPushHandler] " + auth.getUserId()
                    + " đã join phiên " + sessionId
                    + " | Subscribers hiện tại: "
                    + broadcaster.getSubscriberCount();
            System.out.println(joinMessage);

            // Bước 2: giữ kết nối đọc (để phát hiện khi client ngắt kết nối)
            // Client không cần gửi thêm gì, chỉ cần duy trì socket.
            // Khi client đóng kết nối, readLine() trả về null.
            String extraLine;
            while ((extraLine = reader.readLine()) != null) {
                // Bỏ qua các dòng client gửi thêm (heartbeat nếu cần)
                continue;
            }

            String leaveMessage = "[BidPushHandler] Client " + auth.getUserId()
                    + " rời phiên " + sessionId;
            System.out.println(leaveMessage);

        } catch (IOException e) {
            String errMsg = "[BidPushHandler] Mất kết nối: " + e.getMessage();
            System.out.println(errMsg);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Tạo phản hồi success cho push client.
     */
    private String success(final String message) {
        return new JSONObject()
                .put("success", true)
                .put("message", message)
                .toString();
    }

    /**
     * Tạo phản hồi fail cho push client.
     */
    private String fail(final String message) {
        return new JSONObject()
                .put("success", false)
                .put("message", message)
                .toString();
    }
}
