package com.example.socket;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ServerService - giao tiếp với server qua Socket.
 *
 * Sau khi login thành công, token được lưu trong currentToken.
 * Mọi request tiếp theo tự động gửi kèm token (không cần làm thủ công).
 */
public class ServerService {

    private static final SocketClient client = SocketClient.getInstance();

    // Token lưu sau khi login/register thành công
    private static String currentToken = null;

    /** Lưu token sau khi login */
    public static void setToken(String token) {
        currentToken = token;
    }

    /** Xóa token khi logout */
    public static void clearToken() {
        currentToken = null;
    }

    /**
     * Tạo request JSON với token tự động đính kèm.
     * Dùng cho mọi request cần xác thực.
     */
    private static JSONObject req(String action) {
        JSONObject r = new JSONObject();
        r.put("action", action);
        if (currentToken != null) {
            r.put("token", currentToken);
        }
        return r;
    }

    /** Gửi request và nhận response dạng JSONObject */
    private static JSONObject send(JSONObject req) {
        String raw = client.sendRequest(req.toString());
        if (raw == null) return new JSONObject().put("success", false).put("message", "Không thể kết nối server.");
        return new JSONObject(raw);
    }

    // ================================================================== //
    //  AUTH
    // ================================================================== //

    public static class UserResult {
        public boolean success;
        public String  message;
        public String  username;
        public String  role;
        public String  userId;

        public UserResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static UserResult login(String username, String password) {
        JSONObject req = new JSONObject();
        req.put("action",   "login");
        req.put("username", username);
        req.put("password", password);

        JSONObject res = send(req);
        if (res.getBoolean("success")) {
            UserResult r = new UserResult(true, "OK");
            r.userId   = res.getString("userId");
            r.username = res.getString("username");
            r.role     = res.getString("role");
            // ✅ Lưu token ngay sau khi login
            setToken(res.getString("token"));
            return r;
        }
        return new UserResult(false, res.optString("message", "Đăng nhập thất bại."));
    }

    public static UserResult register(String name, String password, String role) {
        JSONObject req = new JSONObject();
        req.put("action",   "register");
        req.put("name",     name);
        req.put("password", password);
        req.put("role",     role);

        JSONObject res = send(req);
        if (res.getBoolean("success")) {
            // Token cũng được trả về khi register (nếu server có)
            if (res.has("token")) setToken(res.getString("token"));
        }
        return new UserResult(res.getBoolean("success"), res.optString("message", ""));
    }

    public static UserResult changeUsername(String newUsername, String password) {
        JSONObject req = req("changeUsername");
        req.put("newUsername", newUsername);
        req.put("password",    password);

        JSONObject res = send(req);
        return new UserResult(res.getBoolean("success"), res.optString("message", ""));
    }

    public static UserResult changePassword(String oldPassword, String newPassword) {
        JSONObject req = req("changePassword");
        req.put("oldPassword", oldPassword);
        req.put("newPassword", newPassword);

        JSONObject res = send(req);
        return new UserResult(res.getBoolean("success"), res.optString("message", ""));
    }

    // ================================================================== //
    //  ITEMS
    // ================================================================== //

    /** Server tự lấy sellerId từ token, không cần truyền */
    public static JSONArray getMyItems() {
        JSONObject res = send(req("getMyItems"));
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("items");
    }

    public static boolean addItem(String name, String category,
                                  String description, double startPrice) {
        JSONObject req = req("addItem");
        req.put("name",        name);
        req.put("category",    category);
        req.put("description", description);
        req.put("startPrice",  startPrice);

        return send(req).getBoolean("success");
    }

    // ================================================================== //
    //  SESSIONS
    // ================================================================== //

    public static JSONArray getAllSessions(String category) {
        JSONObject req = req("getAllSessions");
        req.put("category", category);

        JSONObject res = send(req);
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("sessions");
    }

    /** Server tự lấy sellerId từ token, không cần truyền */
    public static JSONArray getMySessions() {
        JSONObject res = send(req("getMySessions"));
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("sessions");
    }

    public static boolean createSession(String itemId, String startTime,
                                        String endTime, double stepPrice) {
        JSONObject req = req("createSession");
        req.put("itemId",    itemId);
        req.put("startTime", startTime);
        req.put("endTime",   endTime);
        req.put("stepPrice", stepPrice);

        return send(req).getBoolean("success");
    }

    // ================================================================== //
    //  BIDDING
    // ================================================================== //

    /** Server tự lấy bidderId từ token, không cần truyền */
    public static boolean placeBid(String sessionId, double bidAmount) {
        JSONObject req = req("placeBid");
        req.put("sessionId", sessionId);
        req.put("bidAmount", bidAmount);

        return send(req).getBoolean("success");
    }

    public static boolean setAutoBid(String sessionId, double stepPrice, double maxPrice) {
        JSONObject req = req("setAutoBid");
        req.put("sessionId", sessionId);
        req.put("stepPrice", stepPrice);
        req.put("maxPrice",  maxPrice);

        return send(req).getBoolean("success");
    }

    // ================================================================== //
    //  TRANSACTIONS
    // ================================================================== //

    /** Server tự lấy bidderId từ token */
    public static JSONArray getMyTransactions() {
        JSONObject res = send(req("getMyTransactions"));
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("transactions");
    }

    public static boolean pay(String transactionId) {
        JSONObject req = req("pay");
        req.put("transactionId", transactionId);

        return send(req).getBoolean("success");
    }

    // ================================================================== //
    //  ADMIN
    // ================================================================== //

    public static JSONArray getAllUsers() {
        JSONObject res = send(req("getAllUsers"));
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("users");
    }

    public static boolean banUser(String userId) {
        JSONObject req = req("banUser");
        req.put("userId", userId);
        return send(req).getBoolean("success");
    }

    public static boolean makeAdmin(String userId) {
        JSONObject req = req("makeAdmin");
        req.put("userId", userId);
        return send(req).getBoolean("success");
    }
}