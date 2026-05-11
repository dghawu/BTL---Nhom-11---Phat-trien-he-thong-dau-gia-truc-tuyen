package com.example.socket;
import org.json.JSONObject;

/**
 * ServerService - tầng trung gian giữa Controller và SocketClient.
 *
 * Mỗi method tương ứng 1 action:
 *   - Đóng gói tham số thành JSON
 *   - Gửi qua SocketClient
 *   - Parse JSON response trả về object Java
 *
 * Controller chỉ cần gọi ServerService, không cần biết gì về socket hay JSON.
 *
 * Cách dùng trong Controller:
 *   ServerService.UserResult result = ServerService.login("admin", "123");
 *   if (result.success) navigateToHome(result.role, result.username);
 *   else showError(result.message);
 */
public class ServerService {

    private static final SocketClient client = SocketClient.getInstance();

    //  AUTH
    /** Kết quả trả về cho các action liên quan đến User */
    public static class UserResult {
        public boolean success;
        public String  message;
        public String  username;
        public String  role;      // "ADMIN" / "SELLER" / "BIDDER"
        public String  userId;

        public UserResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    /**
     * Đăng nhập.
     * Request:  {"action":"login","username":"...","password":"..."}
     * Response: {"success":true,"userId":1,"username":"abc","role":"SELLER"}
     *        or {"success":false,"message":"Sai mật khẩu"}
     */
    public static UserResult login(String username, String password) {
        JSONObject req = new JSONObject();
        req.put("action",   "login");
        req.put("username", username);
        req.put("password", password);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return new UserResult(false, "Không thể kết nối server.");

        JSONObject res = new JSONObject(raw);
        if (res.getBoolean("success")) {
            UserResult r = new UserResult(true, "OK");
            r.userId   = res.getString("userId");
            r.username = res.getString("username");
            r.role     = res.getString("role");
            return r;
        } else {
            return new UserResult(false, res.optString("message", "Đăng nhập thất bại."));
        }
    }

    /**
     * Đăng ký tài khoản mới.
     * Request:  {"action":"register","name":"...","email":"...","password":"...","role":"BIDDER"}
     * Response: {"success":true} or {"success":false,"message":"..."}
     */
    public static UserResult register(String name, String email, String password, String role) {
        JSONObject req = new JSONObject();
        req.put("action",   "register");
        req.put("name",     name);
        req.put("email",    email);
        req.put("password", password);
        req.put("role",     role);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return new UserResult(false, "Không thể kết nối server.");

        JSONObject res = new JSONObject(raw);
        return new UserResult(
                res.getBoolean("success"),
                res.optString("message", "")
        );
    }

    /**
     * Đổi username.
     * Request:  {"action":"changeUsername","userId":1,"newUsername":"...","password":"..."}
     */
    public static UserResult changeUsername(String userId, String newUsername, String password) {
        JSONObject req = new JSONObject();
        req.put("action",      "changeUsername");
        req.put("userId",      userId);
        req.put("newUsername", newUsername);
        req.put("password",    password);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return new UserResult(false, "Không thể kết nối server.");
        JSONObject res = new JSONObject(raw);
        return new UserResult(res.getBoolean("success"), res.optString("message", ""));
    }

    /**
     * Đổi mật khẩu.
     * Request:  {"action":"changePassword","userId":1,"oldPassword":"...","newPassword":"..."}
     */
    public static UserResult changePassword(String userId, String oldPassword, String newPassword) {
        JSONObject req = new JSONObject();
        req.put("action",      "changePassword");
        req.put("userId",      userId);
        req.put("oldPassword", oldPassword);
        req.put("newPassword", newPassword);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return new UserResult(false, "Không thể kết nối server.");
        JSONObject res = new JSONObject(raw);
        return new UserResult(res.getBoolean("success"), res.optString("message", ""));
    }

    //  ITEMS
    /**
     * Lấy danh sách sản phẩm của seller.
     * Request:  {"action":"getMyItems","sellerId":1}
     * Response: {"success":true,"items":[{...},{...}]}
     */
    public static org.json.JSONArray getMyItems(String sellerId) {
        JSONObject req = new JSONObject();
        req.put("action",   "getMyItems");
        req.put("sellerId", sellerId);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return new org.json.JSONArray();
        JSONObject res = new JSONObject(raw);
        return res.optJSONArray("items");
    }

    /**
     * Thêm sản phẩm mới.
     * Request:  {"action":"addItem","sellerId":1,"name":"...","category":"...","description":"...","startPrice":500000}
     */
    public static boolean addItem(String sellerId, String name, String category,
                                  String description, double startPrice) {
        JSONObject req = new JSONObject();
        req.put("action",      "addItem");
        req.put("sellerId",    sellerId);
        req.put("name",        name);
        req.put("category",    category);
        req.put("description", description);
        req.put("startPrice",  startPrice);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return false;
        return new JSONObject(raw).getBoolean("success");
    }

    //  SESSIONS (Phiên đấu giá)
    /**
     * Lấy tất cả phiên đang mở (cho màn Auctions).
     * Request:  {"action":"getAllSessions","category":"ALL"}
     * Response: {"success":true,"sessions":[{...}]}
     */
    public static org.json.JSONArray getMySessions(String sellerId) {
        JSONObject req = new JSONObject();
        req.put("action",   "getMySessions");
        req.put("sellerId", sellerId);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return new org.json.JSONArray();
        return new JSONObject(raw).optJSONArray("sessions");
    }

    public static org.json.JSONArray getAllSessions(String category) {
        JSONObject req = new JSONObject();
        req.put("action",   "getAllSessions");
        req.put("category", category);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return new org.json.JSONArray();
        return new JSONObject(raw).optJSONArray("sessions");
    }

    /**
     * Tạo phiên đấu giá mới.
     * Request:  {"action":"createSession","itemId":1,"startTime":"...","endTime":"...","stepPrice":50000}
     */
    public static boolean createSession(String itemId, String startTime,
                                        String endTime, double stepPrice) {
        JSONObject req = new JSONObject();
        req.put("action",    "createSession");
        req.put("itemId",    itemId);
        req.put("startTime", startTime);
        req.put("endTime",   endTime);
        req.put("stepPrice", stepPrice);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return false;
        return new JSONObject(raw).getBoolean("success");
    }

    //  BIDDING (Đấu giá)
    /**
     * Đặt giá thủ công.
     * Request:  {"action":"placeBid","sessionId":1,"bidderId":2,"bidAmount":370000}
     */
    public static boolean placeBid(String sessionId, String bidderId, double bidAmount) {
        JSONObject req = new JSONObject();
        req.put("action",    "placeBid");
        req.put("sessionId", sessionId);
        req.put("bidderId",  bidderId);
        req.put("bidAmount", bidAmount);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return false;
        return new JSONObject(raw).getBoolean("success");
    }

    /**
     * Cấu hình đấu giá tự động.
     * Request:  {"action":"setAutoBid","sessionId":1,"bidderId":2,"stepPrice":50000,"maxPrice":500000}
     */
    public static boolean setAutoBid(String sessionId, String bidderId,
                                     double stepPrice, double maxPrice) {
        JSONObject req = new JSONObject();
        req.put("action",    "setAutoBid");
        req.put("sessionId", sessionId);
        req.put("bidderId",  bidderId);
        req.put("stepPrice", stepPrice);
        req.put("maxPrice",  maxPrice);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return false;
        return new JSONObject(raw).getBoolean("success");
    }

    //  TRANSACTIONS (Giao dịch)
    /**
     * Lấy danh sách giao dịch của bidder.
     * Request:  {"action":"getMyTransactions","bidderId":2}
     */
    public static org.json.JSONArray getMyTransactions(String bidderId) {
        JSONObject req = new JSONObject();
        req.put("action",   "getMyTransactions");
        req.put("bidderId", bidderId);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return new org.json.JSONArray();
        return new JSONObject(raw).optJSONArray("transactions");
    }

    /**
     * Thanh toán giao dịch.
     * Request:  {"action":"pay","transactionId":5}
     */
    public static boolean pay(String transactionId) {
        JSONObject req = new JSONObject();
        req.put("action",        "pay");
        req.put("transactionId", transactionId);

        String raw = client.sendRequest(req.toString());
        if (raw == null) return false;
        return new JSONObject(raw).getBoolean("success");
    }

    //  ADMIN
    /** Lấy tất cả users (Admin). */
    public static org.json.JSONArray getAllUsers() {
        JSONObject req = new JSONObject();
        req.put("action", "getAllUsers");
        String raw = client.sendRequest(req.toString());
        if (raw == null) return new org.json.JSONArray();
        return new JSONObject(raw).optJSONArray("users");
    }

    /** Ban user. */
    public static boolean banUser(String userId) {
        JSONObject req = new JSONObject();
        req.put("action", "banUser");
        req.put("userId", userId);
        String raw = client.sendRequest(req.toString());
        if (raw == null) return false;
        return new JSONObject(raw).getBoolean("success");
    }

    /** Cấp quyền Admin. */
    public static boolean makeAdmin(String userId) {
        JSONObject req = new JSONObject();
        req.put("action", "makeAdmin");
        req.put("userId", userId);
        String raw = client.sendRequest(req.toString());
        if (raw == null) return false;
        return new JSONObject(raw).getBoolean("success");
    }
}