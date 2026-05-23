package com.example.socket;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ServerService — giao tiếp với API Server qua Socket (port 8888).
 */
public class ServerService {

    private static final SocketClient client = SocketClient.getInstance();

    private static String currentToken = null;

    public static void clearToken() {
        currentToken = null;
    }

    /**
     * Lấy token hiện tại — dùng cho BidSocketClient.joinSession().
     * Gọi sau khi login thành công.
     */
    public static String getToken() {
        return currentToken;
    }

    public static void setToken(String token) {
        currentToken = token;
    }

    private static JSONObject req(String action) {
        JSONObject r = new JSONObject();
        r.put("action", action);
        if (currentToken != null) r.put("token", currentToken);
        return r;
    }

    private static JSONObject send(JSONObject req) {
        String raw = client.sendRequest(req.toString());
        if (raw == null)
            return new JSONObject().put("success", false).put("message", "Không thể kết nối server.");
        return new JSONObject(raw);
    }

    // ================================================================== //
    //  AUTH
    // ================================================================== //

    public static UserResult login(String username, String password) {
        JSONObject req = new JSONObject();
        req.put("action", "login");
        req.put("username", username);
        req.put("password", password);

        JSONObject res = send(req);
        if (res.getBoolean("success")) {
            UserResult r = new UserResult(true, "OK");
            r.userId = res.getString("userId");
            r.username = res.getString("username");
            r.role = res.getString("role");
            setToken(res.getString("token"));
            return r;
        }
        return new UserResult(false, res.optString("message", "Đăng nhập thất bại."));
    }

    public static UserResult register(String name, String password, String role) {
        JSONObject req = new JSONObject();
        req.put("action", "register");
        req.put("name", name);
        req.put("password", password);
        req.put("role", role);

        JSONObject res = send(req);
        if (res.getBoolean("success") && res.has("token")) setToken(res.getString("token"));
        return new UserResult(res.getBoolean("success"), res.optString("message", ""));
    }

    public static UserResult changeUsername(String newUsername, String password) {
        JSONObject req = req("changeUsername");
        req.put("newUsername", newUsername);
        req.put("password", password);
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

    public static JSONArray getMyItems() {
        JSONObject res = send(req("getMyItems"));
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("items");
    }

    // ================================================================== //
    //  ITEMS
    // ================================================================== //

    public static JSONArray getAllItems() {
        JSONObject res = send(req("getAllItems"));
        return res.getBoolean("success") ? res.optJSONArray("items") : new JSONArray();
    }

    public static boolean addItem(String name, String category,
                                  String description, double startPrice) {
        JSONObject req = req("addItem");
        req.put("name", name);
        req.put("category", category);
        req.put("description", description);
        req.put("startPrice", startPrice);
        return send(req).getBoolean("success");
    }

    public static boolean updateItem(String itemId, String name, String description,
                                     String price, String status) {
        JSONObject req = req("updateItem");
        req.put("itemId", itemId);
        req.put("name", name);
        req.put("description", description);
        try {
            req.put("startPrice",
                    Double.parseDouble(price.replace(",", "").replace("đ", "").trim()));
        } catch (NumberFormatException e) {
            return false;
        }
        req.put("status", status);
        return send(req).getBoolean("success");
    }

    public static boolean approveItem(String itemId) {
        return send(req("approveItem").put("itemId", itemId)).getBoolean("success");
    }

    public static boolean rejectItem(String itemId) {
        return send(req("rejectItem").put("itemId", itemId)).getBoolean("success");
    }

    public static boolean addItemWithImage(String name, String category,
                                           String description, double startPrice,
                                           byte[] imageData) {
        JSONObject req = req("addItemWithImage");
        req.put("name", name);
        req.put("category", category);
        req.put("description", description);
        req.put("startPrice", startPrice);
        // Encode image to base64
        req.put("imageData", java.util.Base64.getEncoder().encodeToString(imageData));
        return send(req).getBoolean("success");
    }

    public static boolean updateItemWithImage(String itemId, String name, String description,
                                              String price, byte[] imageData) {
        JSONObject req = req("updateItemWithImage");
        req.put("itemId", itemId);
        req.put("name", name);
        req.put("description", description);
        try {
            req.put("startPrice",
                    Double.parseDouble(price.replace(",", "").replace("đ", "").trim()));
        } catch (NumberFormatException e) {
            return false;
        }
        // Encode image to base64
        req.put("imageData", java.util.Base64.getEncoder().encodeToString(imageData));
        return send(req).getBoolean("success");
    }

    public static JSONArray getAllSessions(String category) {
        JSONObject req = req("getAllSessions");
        req.put("category", category);
        JSONObject res = send(req);
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("sessions");
    }


    // ================================================================== //
    //  SESSIONS
    // ================================================================== //

    public static JSONArray getMySessions() {
        JSONObject res = send(req("getMySessions"));
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("sessions");
    }

    public static boolean createSession(String itemId, String startTime,
                                        String endTime, double stepPrice) {
        JSONObject req = req("createSession");
        req.put("itemId", itemId);
        req.put("startTime", startTime);
        req.put("endTime", endTime);
        req.put("stepPrice", stepPrice);
        return send(req).getBoolean("success");
    }

    public static boolean approveSession(String sessionId) {
        return send(req("approveSession").put("sessionId", sessionId)).getBoolean("success");
    }

    public static boolean rejectSession(String sessionId) {
        return send(req("rejectSession").put("sessionId", sessionId)).getBoolean("success");
    }

    public static boolean updateSession(String sessionId, String endTime, double stepPrice) {
        JSONObject req = req("updateSession");
        req.put("sessionId", sessionId);
        req.put("endTime", endTime);
        req.put("stepPrice", stepPrice);
        return send(req).getBoolean("success");
    }

    public static JSONObject getSessionDetail(String sessionId) {
        JSONObject req = req("getSessionDetail");
        req.put("sessionId", sessionId);
        JSONObject res = send(req);
        if (!res.getBoolean("success")) return new JSONObject();
        return res.optJSONObject("session");
    }

    public static boolean placeBid(String sessionId, double bidAmount) {
        JSONObject req = req("placeBid");
        req.put("sessionId", sessionId);
        req.put("bidAmount", bidAmount);
        return send(req).getBoolean("success");
    }

    // ================================================================== //
    //  BIDDING
    // ================================================================== //

    public static boolean setAutoBid(String sessionId, double stepPrice, double maxPrice) {
        JSONObject req = req("setAutoBid");
        req.put("sessionId", sessionId);
        req.put("stepPrice", stepPrice);
        req.put("maxPrice", maxPrice);
        return send(req).getBoolean("success");
    }

    public static JSONArray getBidHistory(String sessionId) {
        JSONObject req = req("getBidHistory");
        req.put("sessionId", sessionId);
        JSONObject res = send(req);
        return (res != null && res.getBoolean("success")) ? res.getJSONArray("history") : null;
    }

    public static boolean confirmWin(String sessionId) {
        JSONObject req = req("confirmWin");
        req.put("sessionId", sessionId);
        return send(req).getBoolean("success");
    }

    public static boolean pay(String sessionId, double amount) {
        JSONObject req = req("pay");
        req.put("sessionId", sessionId);
        req.put("amount", amount);
        return send(req).getBoolean("success");
    }

    public static JSONArray getMyWonSessions() {
        JSONObject req = req("getMyWonSessions");
        JSONObject res = send(req);
        return res.getBoolean("success") ? res.optJSONArray("sessions") : null;
    }

    public static JSONArray getMyTransactions() {
        JSONObject res = send(req("getMyTransactions"));
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("transactions");
    }

    // ================================================================== //
    //  TRANSACTIONS
    // ================================================================== //

    public static boolean pay(String transactionId) {
        JSONObject req = req("pay");
        req.put("transactionId", transactionId);
        return send(req).getBoolean("success");
    }

    public static JSONArray getAllTransactions() {
        JSONObject res = send(req("getAllTransactions"));
        return res.getBoolean("success") ? res.optJSONArray("transactions") : new JSONArray();
    }

    public static JSONArray getAllUsers() {
        JSONObject res = send(req("getAllUsers"));
        if (!res.getBoolean("success")) return new JSONArray();
        return res.optJSONArray("users");
    }

    // ================================================================== //
    //  ADMIN
    // ================================================================== //

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

    public static boolean cancelAutoBid(String sessionId) {
        JSONObject req = req("cancelAutoBid");
        req.put("sessionId", sessionId);
        return send(req).getBoolean("success");
    }

    public static class UserResult {
        public boolean success;
        public String message;
        public String username;
        public String role;
        public String userId;

        public UserResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}